/**
 * Copyright (C) 2015 Orange
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.francetelecom.clara.cloud.environment.impl;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.DateHelper;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.SecurityUtils;
import com.francetelecom.clara.cloud.core.service.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.EnvironmentNotFoundException;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.environment.log.LogService;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.paas.activation.ManagePaasActivation;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentOpsDetailsDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.*;

/**
 * Manages environment
 */
@Service
public class ManageEnvironmentImpl implements ManageEnvironment {

    private static final Logger log = LoggerFactory.getLogger(ManageEnvironmentImpl.class);

    /**
     * Key in logback context for environment label when available
     */
    private static final String LOG_KEY_ENVNAME = "env_name";

    /**
     * Key in logback context for environment UID when available
     */
    private static final String LOG_KEY_ENVUID = "env_uid";

    @Autowired
    private ManageEnvironmentImplUtils utils;

    @Value("${paas.schedule.databasePurge.retentionDelayInDay}")
    private Integer purgeRetentionDelayInDay;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private ManagePaasActivation managePaasActivation;

    @Autowired
    private ApplicationReleaseRepository applicationReleaseRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private EnvironmentMapper environmentMapper;

    @Override
    public String createEnvironment(String releaseUID, EnvironmentTypeEnum requestedType, String ownerSsoId, String label, List<String> configRoleUIDs) throws BusinessException {
        Validate.notNull(releaseUID, "cannot create TDI : releaseUid should not be null");
        long start = System.currentTimeMillis();
        EnvironmentTypeEnum type = requestedType;
        if (requestedType != EnvironmentTypeEnum.PRODUCTION && requestedType != EnvironmentTypeEnum.DEVELOPMENT) {
            type = EnvironmentTypeEnum.PRODUCTION;
            log.warn("Requested environment type " + requestedType + " is not currently supported; actual type will be " + type);
        }
        String environmentUID;
        try {
            MDC.put(LOG_KEY_ENVNAME, label);
            log.debug("createEnvironment: releaseUID={} type={} label={}", new Object[]{releaseUID, type.name(), label});
            synchronized (releaseUID.intern()) {
                // Synchronized this part to avoid pultiple creation of the same
                // environment
                Validate.notNull(type, "cannot create TDI : environment type should not be null");
                environmentUID = utils.createTDI(releaseUID, DeploymentProfileEnum.valueOf(type.name()), ownerSsoId, label, configRoleUIDs);
            }
            MDC.put(LOG_KEY_ENVUID, environmentUID);
            // this log is used by splunk dashboard
            log.info("[STATS] Duration : " + (System.currentTimeMillis() - start) + "ms for createEnvironment#1(" + releaseUID + ", " + type + ", " + ownerSsoId + ", " + label
                    + ")");
            start = System.currentTimeMillis();

            Environment justCreatedEnvironment = environmentRepository.findByUid(environmentUID);

            //
            // Start Activate Here
            //
            managePaasActivation.activate(justCreatedEnvironment.getTechnicalDeploymentInstance().getId());
            // this log is used by splunk dashboard
            log.info("[STATS] Duration : " + (System.currentTimeMillis() - start) + "ms for createEnvironment#2(" + releaseUID + ", " + type + ", " + ownerSsoId + ", " + label
                    + ")");
            log.info(justCreatedEnvironment.toLogString());
        } finally {
            MDC.remove(LOG_KEY_ENVNAME);
            MDC.remove(LOG_KEY_ENVUID);
        }
        return environmentUID;
    }

    @Override
    public String createEnvironment(String releaseUID, EnvironmentTypeEnum requestedType, String ownerSsoId, String label) throws BusinessException {
        return createEnvironment(releaseUID, requestedType, ownerSsoId, label, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public EnvironmentDto findEnvironmentByUID(String uid) throws EnvironmentNotFoundException {
        Environment environment = environmentRepository.findByUid(uid);
        if (environment == null) {
            String message = "Environment with UID[" + uid + "] does not exist.";
            log.info(message);
            throw new EnvironmentNotFoundException(message);
        }

        // assert user is authorize to perform action
        assertHasReadPermissionFor(environment);

        return environmentMapper.toEnvironmentDto(environment);
    }

    private void assertHasWritePermissionFor(Environment environment) {
        SecurityUtils.assertHasWritePermissionFor(environment);
    }

    private void assertHasReadPermissionFor(Environment environment) {
        SecurityUtils.assertHasReadPermissionFor(environment);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public EnvironmentDetailsDto findEnvironmentDetails(String uid) throws EnvironmentNotFoundException {
        Environment environment = environmentRepository.findByUid(uid);
        if (environment == null) {
            String message = "Environment with UID[" + uid + "] does not exist.";
            log.info(message);
            throw new EnvironmentNotFoundException(message);
        }

        assertHasReadPermissionFor(environment);

        return toEnvironmentDetailsDto(environment);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public EnvironmentOpsDetailsDto findEnvironmentOpsDetailsByUID(String uid) throws EnvironmentNotFoundException {
        Environment environment = environmentRepository.findByUid(uid);
        if (environment == null) {
            String message = "Environment with UID[" + uid + "] does not exist.";
            log.info(message);
            throw new EnvironmentNotFoundException(message);
        }

        assertHasReadPermissionFor(environment);

        return createEnvironmentOpsDetailsDto(environment);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public List<EnvironmentDto> findEnvironments() {
        if (SecurityUtils.currentUserIsAdmin()) {
            return environmentMapper.toEnvironmentDtoList(environmentRepository.findAllActive());
        } else {
            return environmentMapper.toEnvironmentDtoList(environmentRepository.findAllPublicOrPrivateByMember(SecurityUtils.currentUser().getValue()));
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public List<EnvironmentDto> findMyEnvironments() {
        return environmentMapper.toEnvironmentDtoList(environmentRepository.findAllActiveByApplicationMember(SecurityUtils.currentUser().getValue()));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public List<EnvironmentDto> findEnvironmentsByAppRelease(String releaseUID)
            throws ApplicationReleaseNotFoundException {
        ApplicationRelease applicationRelease = applicationReleaseRepository.findByUID(releaseUID);
        if (applicationRelease == null) {
            throw new ApplicationReleaseNotFoundException("ApplicationRelease#" + releaseUID);
        }
        // cannot see if not authorized
        if (SecurityUtils.currentUserIsAdmin()) {
            return environmentMapper.toEnvironmentDtoList(environmentRepository.findAllActiveByApplicationReleaseUid(releaseUID));
        } else {
            return environmentMapper.toEnvironmentDtoList(environmentRepository.findAllPublicOrPrivateByMemberAndByApplicationRelease(releaseUID, SecurityUtils.currentUser().getValue()));
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void startEnvironment(String uid) throws EnvironmentNotFoundException {
        try {
            MDC.put(LOG_KEY_ENVUID, uid);
            log.debug("startEnvironment: uid={}", new Object[]{uid});
            Environment environment = environmentRepository.findByUid(uid);
            assertHasWritePermissionFor(environment);
            MDC.put(LOG_KEY_ENVNAME, environment.getLabel());
            if (environment.isStopped()) {
                managePaasActivation.start(environment.getTechnicalDeploymentInstance().getId());
                // TODO status should be set by managePaasActivation
                environment.setStatus(EnvironmentStatus.STARTING);
            } else if (environment.isStarting() || environment.isRunning()) {
                log.info("Environment '" + environment.getUID() + "' is already started or is starting (ignoring call)");
            } else if (environment.isFailed()) {
                log.warn("Environment '" + environment.getUID() + "' is failed but (anyway) we try to start it...");
                managePaasActivation.start(environment.getTechnicalDeploymentInstance().getId());
                // TODO status should be set by managePaasActivation
                environment.setStatus(EnvironmentStatus.STARTING);
            } else {
                throw new TechnicalException("Calling start on environment which has a bad status: " + environment.getStatus());
            }
        } finally {
            MDC.remove(LOG_KEY_ENVNAME);
            MDC.remove(LOG_KEY_ENVUID);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void stopEnvironment(String uid) throws EnvironmentNotFoundException {
        try {
            MDC.put(LOG_KEY_ENVUID, uid);
            log.debug("stopEnvironment: uid={}", new Object[]{uid});
            Environment environment = environmentRepository.findByUid(uid);
            assertHasWritePermissionFor(environment);
            MDC.put(LOG_KEY_ENVNAME, environment.getLabel());
            if (environment.isRunning()) {
                managePaasActivation.stop(environment.getTechnicalDeploymentInstance().getId());
                // TODO status should be set by managePaasActivation
                environment.setStatus(EnvironmentStatus.STOPPING);
            } else if (environment.isStopping() || environment.isStopped()) {
                log.info("Environment '" + environment.getUID() + "' is already stopped or is stopping (ignoring call)");
            } else if (environment.isFailed()) {
                log.warn("Environment '" + environment.getUID() + "' is failed but (anyway) we try to stop it...");
                managePaasActivation.stop(environment.getTechnicalDeploymentInstance().getId());
                // TODO status should be set by managePaasActivation
                environment.setStatus(EnvironmentStatus.STOPPING);
            } else {
                throw new TechnicalException("Calling stop on environment which has a bad status: " + environment.getStatus());
            }
        } finally {
            MDC.remove(LOG_KEY_ENVNAME);
            MDC.remove(LOG_KEY_ENVUID);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void deleteEnvironment(String uid) throws EnvironmentNotFoundException {
        try {
            MDC.put(LOG_KEY_ENVUID, uid);
            log.debug("deleteEnvironment: uid={}", new Object[]{uid});
            Environment environment = environmentRepository.findByUid(uid);
            assertHasWritePermissionFor(environment);
            MDC.put(LOG_KEY_ENVNAME, environment.getLabel());
            if (environment.isRemoved() || environment.isRemoving()) {
                log.info("Environment '" + environment.getUID() + "' is already deleted or deletion is in progress (ignoring call)");
            } else {
                managePaasActivation.delete(environment.getTechnicalDeploymentInstance().getId());
                // TODO status should be set by managePaasActivation
                environment.setStatus(EnvironmentStatus.REMOVING);
            }
        } finally {
            MDC.remove(LOG_KEY_ENVNAME);
            MDC.remove(LOG_KEY_ENVUID);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void forceStatusForAndEnvironment(String uid, EnvironmentStatus newStatus) throws EnvironmentNotFoundException {
        Environment environment = environmentRepository.findByUid(uid);
        if (environment == null) {
            String message = "Environment with UID[" + uid + "] does not exist.";
            log.info(message);
            throw new EnvironmentNotFoundException(message);
        }
        environment.updateStatus(newStatus, "", 100);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public List<Environment> findOldRemovedEnvironments() {
        log.info("*** find old environment (purgeRetentionDelayInDay={})", purgeRetentionDelayInDay);
        // find removed environment older than N day
        return environmentRepository.findRemovedOlderThanNDays(DateHelper.getDateDeltaDay(-purgeRetentionDelayInDay));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void purgeRemovedEnvironment(String uid) throws EnvironmentNotFoundException {
        Environment environment = environmentRepository.findByUid(uid);
        if (environment == null) {
            String message = "Environment with UID[" + uid + "] does not exist.";
            log.info(message);
            throw new EnvironmentNotFoundException(message);
        }

        assertHasWritePermissionFor(environment);

        environmentRepository.delete(environment);
    }

    //
    // Utils
    //
    // ================================================================================================================================
    // TOM
    private Map<String, List<LinkDto>> getEnvironmentLinkDtos(Environment env) {

        TechnicalDeployment td = env.getTechnicalDeploymentInstance().getTechnicalDeployment();

        Map<String, List<LinkDto>> linkDtoMap = new HashMap<String, List<LinkDto>>();

        linkDtoMap.putAll(getEnvironmentAccessUrlsLinkDtos(td, linkDtoMap));
        linkDtoMap.putAll(getEnvironmentLogsUrlsLinkDtos(env, linkDtoMap));
        linkDtoMap.putAll(getEnvironmentWspLinkDtos(td, linkDtoMap));
        linkDtoMap.putAll(getEnvironmentDiagToolsUrlsLinkDtos(env, linkDtoMap));

        return linkDtoMap;
    }

    private Map<String, List<LinkDto>> getEnvironmentAccessUrlsLinkDtos(TechnicalDeployment td, Map<String, List<LinkDto>> linkDtoMap) {

        for (Route route : td.listXaasSubscriptionTemplates(Route.class)) {
            linkDtoMap.putAll(addLinkDtoForLogicalModelItemInLinkDtosMap(linkDtoMap, route.getLogicalModelId(), route.getFullHttpAccessUrl(), LinkDto.LinkTypeEnum.ACCESS_LINK,
                    LinkDto.TargetUserEnum.PAAS_USER, null));
        }

        return linkDtoMap;
    }

    private Map<String, List<LinkDto>> getEnvironmentLogsUrlsLinkDtos(Environment env, Map<String, List<LinkDto>> linkDtoMap) {

        TechnicalDeployment td = env.getTechnicalDeploymentInstance().getTechnicalDeployment();

        Set<App> apps = td.listXaasSubscriptionTemplates(App.class);

        for (App app : apps) {
            URL logsUrl = logService.getAppLogsUrl(app.getExternalReference());
            linkDtoMap.putAll(addLinkDtoForLogicalModelItemInLinkDtosMap(linkDtoMap, app.getLogicalModelId(), logsUrl, LinkDto.LinkTypeEnum.LOGS_LINK,
                    LinkDto.TargetUserEnum.PAAS_USER, null));
        }

        Set<Route> routes = td.listXaasSubscriptionTemplates(Route.class);
        for (Route route : routes) {
            URL logsUrl = logService.getRouteLogsUrl(route.getUri());
            linkDtoMap.putAll(addLinkDtoForLogicalModelItemInLinkDtosMap(linkDtoMap, route.getLogicalModelId(), logsUrl, LinkDto.LinkTypeEnum.LOGS_LINK,
                    LinkDto.TargetUserEnum.PAAS_USER, null));
        }

        return linkDtoMap;
    }

    private Map<String, List<LinkDto>> getEnvironmentWspLinkDtos(TechnicalDeployment td, Map<String, List<LinkDto>> linkDtoMap) {
        return linkDtoMap;
    }

    private Map<String, List<LinkDto>> getEnvironmentDiagToolsUrlsLinkDtos(Environment env, Map<String, List<LinkDto>> linkDtoMap) {

        TechnicalDeploymentInstance tdi = env.getTechnicalDeploymentInstance();
        TechnicalDeployment td = tdi.getTechnicalDeployment();
        // In fact this is the TD name that is used for the paasEnvName field
        // (see splunk configuration)

        Set<App> apps = td.listXaasSubscriptionTemplates(App.class);

        List<String> appNAmes = new ArrayList<String>();

        for (App app : apps) {
            appNAmes.add(app.getExternalReference());
        }

        String[] array = appNAmes.toArray(new String[appNAmes.size()]);

        URL logsUrl = logService.getEnvironmentLogsUrl(array);

        // paas logs linkDto
        linkDtoMap.putAll(addLinkDtoForLogicalModelItemInLinkDtosMap(linkDtoMap, td.getName(), logsUrl, LinkDto.LinkTypeEnum.LOGS_LINK, LinkDto.TargetUserEnum.PAAS_USER, null));

        return linkDtoMap;
    }

    private Map<String, List<LinkDto>> addLinkDtoForLogicalModelItemInLinkDtosMap(Map<String, List<LinkDto>> linkDtoMap, String key, URL url, LinkDto.LinkTypeEnum linktype,
                                                                                  LinkDto.TargetUserEnum targetUser, Map<String, String> serviceBinding) {

        if (url != null && key != null) {
            List<LinkDto> linkDtoList;

            // Link Dto creation
            LinkDto appsLinksDto = new LinkDto();
            appsLinksDto.setLinkType(linktype);
            appsLinksDto.setTargetUser(targetUser);
            appsLinksDto.setUrl(url);
            appsLinksDto.setServiceBindings(serviceBinding);
            // get list of LinkDto associated with the key
            linkDtoList = getLinkDtoInMap(linkDtoMap, key);

            // Add new LinkDto to the list of LinkDto
            linkDtoList.add(appsLinksDto);

            // Add the List of LinkDtos into the map with "key" as key
            linkDtoMap.put(key, linkDtoList);

        }

        return linkDtoMap;
    }

    /*
     * get LinkDto into List<LinkDto> if no List exist with key, it returns a
     * new empty List else it returns the list corresponding to the key
     */
    private List<LinkDto> getLinkDtoInMap(Map<String, List<LinkDto>> linkDtoMap, String key) {
        List<LinkDto> linkDtoList;

        if (linkDtoMap.get(key) == null) {
            linkDtoList = new ArrayList<LinkDto>();
        } else {
            linkDtoList = linkDtoMap.get(key);
        }

        return linkDtoList;
    }

    private EnvironmentDetailsDto toEnvironmentDetailsDto(Environment env) throws EnvironmentNotFoundException {

        if (env == null) {
            throw new EnvironmentNotFoundException();
        }

        String uid = env.getUID();
        String internalName = env.getInternalName();
        String label = env.getLabel();
        ApplicationRelease applicationRelease = env.getApplicationRelease();
        Application application = applicationRelease.getApplication();
        String appLabel = application.getLabel();
        String appReleaseUid = applicationRelease.getUID();
        String releaseVersion = applicationRelease.getReleaseVersion();
        PaasUser envPaasUser = env.getPaasUser();
        String envPaasUserSsoId = envPaasUser.getSsoId().getValue();
        String paasUserName = envPaasUser.getFirstName();
        Date envCreationDate = env.getCreationDate();
        String envTypeName = env.getType().name();
        EnvironmentTypeEnum envTypeEnum = EnvironmentTypeEnum.valueOf(envTypeName);
        String envStatusName = env.getStatus().name();
        EnvironmentStatusEnum envStatusEnum = EnvironmentStatusEnum.valueOf(envStatusName);
        String envStatusMessage = env.getStatusMessage();
        int envStatusPercent = env.getStatusPercent();
        String envComment = env.getComment();
        TechnicalDeploymentInstance envTdi = env.getTechnicalDeploymentInstance();
        TechnicalDeployment envTdiTd = envTdi.getTechnicalDeployment();
        String envTdiTdName = envTdiTd.getName();
        EnvironmentDetailsDto dto = new EnvironmentDetailsDto(uid, internalName, label, appLabel, appReleaseUid, releaseVersion, envPaasUserSsoId, paasUserName, envCreationDate,
                envTypeEnum, envStatusEnum, envStatusMessage, envStatusPercent, envComment, envTdiTdName);

        dto.setEditable(SecurityUtils.hasWritePermissionFor(env));

        dto.setLinkDtoMap(getEnvironmentLinkDtos(env));

        return dto;
    }

    private EnvironmentOpsDetailsDto createEnvironmentOpsDetailsDto(Environment env) throws EnvironmentNotFoundException {
        EnvironmentOpsDetailsDto dto = new EnvironmentOpsDetailsDto(env.getUID(), null, env.getLabel(), env.getApplicationRelease().getApplication().getLabel(), env
                .getApplicationRelease().getUID(), env.getApplicationRelease().getReleaseVersion(), env.getPaasUser().getSsoId().getValue(), env.getPaasUser().getFirstName(),
                env.getCreationDate(), EnvironmentTypeEnum.valueOf(env.getType().name()), EnvironmentStatusEnum.valueOf(env.getStatus().name()), env.getStatusMessage(),
                env.getStatusPercent(), env.getComment(), env.getTechnicalDeploymentInstance().getName());

        dto.setLinkDtoMap(getEnvironmentLinkDtos(env));

        return dto;
    }

    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    @Override
    public Environment update(EnvironmentDetailsDto environmentDetailsDto) {
        try {
            MDC.put(LOG_KEY_ENVUID, environmentDetailsDto.getUid());
            MDC.put(LOG_KEY_ENVNAME, environmentDetailsDto.getLabel());
            log.debug("updateEnvironment: uid={}", new Object[]{environmentDetailsDto.getUid()});
            Environment environment = environmentRepository.findByUid(environmentDetailsDto.getUid());
            assertHasWritePermissionFor(environment);
            environment.setComment(environmentDetailsDto.getComment());
            return environment;
        } finally {
            MDC.remove(LOG_KEY_ENVNAME);
            MDC.remove(LOG_KEY_ENVUID);
        }
    }

    @Override
    public Long countEnvironments() {
        if (SecurityUtils.currentUserIsAdmin()) {
            return environmentRepository.countActive();
        } else {
            return environmentRepository.countPublicOrPrivateByMember(SecurityUtils.currentUser().getValue());
        }
    }

    @Override
    public Long countMyEnvironments() {
        return environmentRepository.countActiveByApplicationMember(SecurityUtils.currentUser().getValue());
    }

    @Override
    public Long countEnvironmentsByApplicationRelease(String releaseUID) throws ApplicationReleaseNotFoundException {
        ApplicationRelease applicationRelease = applicationReleaseRepository.findByUID(releaseUID);
        if (applicationRelease == null) {
            throw new ApplicationReleaseNotFoundException("ApplicationRelease#" + releaseUID);
        }
        // cannot see if not authorized
        if (SecurityUtils.currentUserIsAdmin()) {
            return environmentRepository.countActiveByApplicationReleaseUid(releaseUID);
        } else {
            return environmentRepository.countAllPublicOrPrivateByMemberAndByApplicationRelease(releaseUID, SecurityUtils.currentUser().getValue());
        }
    }

    @Override
    public boolean isEnvironmentLabelUniqueForRelease(String ssoid, String searchEnvironmentLabel, String releaseUID) {
        return environmentRepository.findByApplicationReleaseUIDAndLabel(releaseUID, searchEnvironmentLabel) == null;
    }

    //
    // Getters and setters that might be necessary for spring injection ?
    // Please remove them if unneeded
    //

    public void setPurgeRetentionDelayInDay(Integer purgeRetentionDelayInDay) {
        this.purgeRetentionDelayInDay = purgeRetentionDelayInDay;
    }

    public void setEnvironmentRepository(EnvironmentRepository repository) {
        this.environmentRepository = repository;
    }

    public void setManagePaasActivation(ManagePaasActivation managePaasActivation) {
        this.managePaasActivation = managePaasActivation;
    }

    public void setApplicationReleaseRepository(ApplicationReleaseRepository repository) {
        this.applicationReleaseRepository = repository;
    }

    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    public void setUtils(ManageEnvironmentImplUtils utils) {
        this.utils = utils;
    }

    public void setEnvironmentMapper(EnvironmentMapper environmentMapper) {
        this.environmentMapper = environmentMapper;
    }

}
