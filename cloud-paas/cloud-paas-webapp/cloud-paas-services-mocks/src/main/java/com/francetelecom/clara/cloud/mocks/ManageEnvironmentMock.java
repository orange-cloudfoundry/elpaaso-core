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
package com.francetelecom.clara.cloud.mocks;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.EnvironmentStatus;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.exception.*;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.paas.projection.ProjectionService;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentOpsDetailsDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.google.common.base.Predicate;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import static com.francetelecom.clara.cloud.mocks.SecurityUtils.currentUser;
import static java.text.MessageFormat.format;

/**
 * Created by IntelliJ IDEA. User: lzxv3002 Date: 09/06/11 Time: 17:09
 */
@Service("manageEnvironment")
public class ManageEnvironmentMock extends CoreItemServiceMock<Environment> implements ManageEnvironment {

    private static final Logger log = LoggerFactory.getLogger(ManageEnvironmentMock.class);

    private static final String LOG_KEY_ENVNAME = "env_name";

    private static final String LOG_KEY_ENVUID = "env_uid";

    @Autowired(required = true)
    private ProjectionService projectionService;

    @Autowired(required = true)
    private ManageApplicationReleaseMock manageApplicationReleaseMock;

    @Autowired(required = true)
    private ManagePaasUserMock managePaasUserMock;

    private final String splunkIp = "10.170.232.227";

    private final String splunkPort = "8000";

    @Override
    public Long countMyEnvironments() {
        return count(new Predicate<Environment>() {
            @Override
            public boolean apply(Environment environment) {
                return environment.getApplicationRelease().getApplication().hasForMember(currentUser()) && !environment.isRemoved();
            }
        });
    }

    @Override
    public Long countEnvironmentsByApplicationRelease(String releaseUID) {

        long counter = 0;
        try {
            for (EnvironmentDto e : findEnvironmentsByAppRelease(releaseUID)) {
                if (!e.getStatus().equals(EnvironmentStatus.REMOVED)) {
                    counter++;
                }
            }
        } catch (ObjectNotFoundException e) {
            return 0L;
        }
        return counter;

        // throw new
        // UnsupportedOperationException("mock operation should not be called");
    }

    @Override
    public boolean isEnvironmentLabelUniqueForRelease(String ssoid, String searchEnvironmentLabel, String releaseUID) {

        boolean isUnique = true;

        List<EnvironmentDto> environmentList = findAllActiveEnvironmentByRelease(releaseUID);

        for (EnvironmentDto envDto : environmentList) {
            if (envDto.getLabel().equalsIgnoreCase(searchEnvironmentLabel)) {
                isUnique = false;
                break;
            }
        }

        return isUnique;
    }

    @Override
    public EnvironmentDto findEnvironmentByUID(String uid) throws EnvironmentNotFoundException {
        try {
            return createEnvironmentDto(findByUID(uid));
        } catch (ObjectNotFoundException e) {
            throw new EnvironmentNotFoundException(e);
        }
    }

    @Override
    public List<EnvironmentDto> findEnvironmentsByAppRelease(String applicationReleaseName) throws ApplicationReleaseNotFoundException {
        List<EnvironmentDto> dtos = new ArrayList<EnvironmentDto>();
        for (Environment env : findAll()) {

            boolean exist = applicationReleaseName.equals(env.getApplicationRelease().getUID());

            if (exist) {
                dtos.add(createEnvironmentDto(env));
            }
        }
        return dtos;
    }

    @Override
    public List<EnvironmentDto> findEnvironments() {
        List<EnvironmentDto> dtos = new ArrayList<EnvironmentDto>();
        Collection<Environment> allEnv = findAll();
        for (Environment env : allEnv) {
            dtos.add(createEnvironmentDto(env));
        }
        return dtos;
    }

    public List<EnvironmentDto> findAllActiveEnvironmentByRelease(String releaseUID) {
        List<Environment> allEnvs = (List<Environment>) findAll();
        List<Environment> allActiveEnvs = new ArrayList<Environment>();
        List<Environment> allActiveEnvsForRelease = new ArrayList<Environment>();
        for (Environment env : allEnvs) {
            if (!env.isRemoved()) {
                allActiveEnvs.add(env);
            }
        }

        for (Environment env : allActiveEnvs) {
            if (env.getApplicationRelease().getUID().equals(releaseUID)) {
                allActiveEnvsForRelease.add(env);
            }
        }

        return createEnvironmentDtoList(allActiveEnvsForRelease);
    }

    @Override
    public void startEnvironment(String environmentId) throws EnvironmentNotFoundException {
        Environment environment;
        try {
            environment = findByUID(environmentId);
            environment.setStatus(EnvironmentStatus.RUNNING);
            update(environment);
        } catch (ObjectNotFoundException e) {
            throw new EnvironmentNotFoundException(e);
        }
    }

    @Override
    public void stopEnvironment(String environmentId) throws EnvironmentNotFoundException {
        Environment environment;
        try {
            environment = findByUID(environmentId);
            environment.setStatus(EnvironmentStatus.STOPPED);
            update(environment);
        } catch (ObjectNotFoundException e) {
            throw new EnvironmentNotFoundException(e);
        }
    }

    @Override
    public void deleteEnvironment(String environmentId) throws EnvironmentNotFoundException {
        Environment environment;
        try {
            environment = findByUID(environmentId);
            environment.setStatus(EnvironmentStatus.REMOVED);
            update(environment);
        } catch (ObjectNotFoundException e) {
            throw new EnvironmentNotFoundException(e);
        }
    }

    @Override
    public String createEnvironment(String releaseUID, EnvironmentTypeEnum type, String ownerSsoId, String label, List<String> configRoleUIDs)
            throws BusinessException {
        return createEnvironment(releaseUID, type, ownerSsoId, label);
    }

    @Override
    public String createEnvironment(String releaseUID, EnvironmentTypeEnum type, String ownerSsoId, String label) throws BusinessException {
        Validate.notNull(type);
        Validate.notNull(label);
        Environment environment;
        try {
            MDC.put(LOG_KEY_ENVNAME, label);

            // Fetch an AppRelease from DB which is up-to-date and may be
            // browsed
            // for fetching lazy associations
            ApplicationRelease applicationRelease;
            try {
                applicationRelease = manageApplicationReleaseMock.findApplicationReleaseByUID(releaseUID);
            } catch (ObjectNotFoundException e) {
                throw new ApplicationReleaseNotFoundException("ApplicationRelease#" + releaseUID);
            }
            // Fetch a PaasUser from DB which is up-to-date and may be browsed
            // for
            // fetching lazy associations
            PaasUser owner;
            try {
                owner = managePaasUserMock.findPaasUser(ownerSsoId);
            } catch (ObjectNotFoundException e1) {
                throw new PaasUserNotFoundException("PaasUser#" + ownerSsoId);
            }

            // Force projection and locking of AppRelease
            if (applicationRelease.isEditing()) {
                // TODO: add consistency validation step and reject the request
                applicationRelease.validate();
            }

            DeploymentProfileEnum deploymentProfile = DeploymentProfileEnum.valueOf(type.name());
            if (type != EnvironmentTypeEnum.PRODUCTION && type != EnvironmentTypeEnum.DEVELOPMENT) {
                deploymentProfile = DeploymentProfileEnum.valueOf(EnvironmentTypeEnum.PRODUCTION.name());
                log.warn("Requested environment type " + type + " is not currently supported; actual type will be " + type);
            }

            TechnicalDeploymentTemplate tdt = projectionService.generateNewDeploymentTemplate(applicationRelease, deploymentProfile);

            if (applicationRelease.isValidated()) {
                try {
                    applicationRelease.lock();
                    manageApplicationReleaseMock.updateApplicationRelease(applicationRelease);
                } catch (ObjectNotFoundException e) {
                    throw new ApplicationReleaseNotFoundException("ApplicationRelease#" + releaseUID);
                }
            }


            // FIXME: ensure max size of 150 chars on environment name

            TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, tdt.getTechnicalDeployment());

            environment = new Environment(deploymentProfile, label, applicationRelease, owner, tdi);
            MDC.put(LOG_KEY_ENVUID, environment.getUID());

            environment.setStatus(EnvironmentStatus.RUNNING);
            try {
                create(environment);
            } catch (DuplicateApplicationReleaseException e) {
                throw new TechnicalException(e);
            } catch (DuplicateApplicationException e) {
                throw new TechnicalException(e);
            }
        } finally {
            MDC.remove(LOG_KEY_ENVNAME);
            MDC.remove(LOG_KEY_ENVUID);
        }
        return environment.getUID();
    }

    @Override
    public EnvironmentDetailsDto findEnvironmentDetails(String environmentUid) throws EnvironmentNotFoundException {
        Environment env;
        try {
            env = findByUID(environmentUid);
        } catch (ObjectNotFoundException e) {
            throw new EnvironmentNotFoundException(e);
        }
        EnvironmentDetailsDto dto = new EnvironmentDetailsDto(env.getUID(), env.getInternalName(), env.getLabel(), env.getApplicationRelease().getApplication().getLabel(), env
                .getApplicationRelease().getUID(), env.getApplicationRelease().getReleaseVersion(), env.getPaasUser().getSsoId().getValue(),
                env.getPaasUser().getFirstName(), env.getCreationDate(), EnvironmentTypeEnum.valueOf(env.getType().name()),
                EnvironmentStatusEnum.valueOf(env.getStatus().name()), env.getStatusMessage(), env.getStatusPercent(), env.getComment(), env.getTechnicalDeploymentInstance().getTechnicalDeployment().getName());

        dto.setLinkDtoMap(getEnvironmentLinkDtos(env));

        return dto;
    }

    @Override
    public EnvironmentOpsDetailsDto findEnvironmentOpsDetailsByUID(String environmentUid) throws EnvironmentNotFoundException {
        Environment env;
        try {
            env = findByUID(environmentUid);
        } catch (ObjectNotFoundException e) {
            throw new EnvironmentNotFoundException(e);
        }
        EnvironmentOpsDetailsDto dto = new EnvironmentOpsDetailsDto(env.getUID(), null, env.getLabel(), env.getApplicationRelease().getApplication().getLabel(), env
                .getApplicationRelease().getUID(), env.getApplicationRelease().getReleaseVersion(), env.getPaasUser().getSsoId().getValue(),
                env.getPaasUser().getFirstName(), env.getCreationDate(), EnvironmentTypeEnum.valueOf(env.getType().name()),
                EnvironmentStatusEnum.valueOf(env.getStatus().name()), env.getStatusMessage(), env.getStatusPercent(), env.getComment(), env.getTechnicalDeploymentInstance().getName());

        TechnicalDeployment td = env.getTechnicalDeploymentInstance().getTechnicalDeployment();

        dto.setLinkDtoMap(getEnvironmentLinkDtos(env));

        return dto;
    }

    private static EnvironmentDto createEnvironmentDto(Environment env) {
        EnvironmentStatusEnum status;
        int choix = (int) (Math.random() * 100) % 7;
        switch (choix) {
            case 1:
                status = EnvironmentStatusEnum.CREATING;
                break;
            case 2:
                status = EnvironmentStatusEnum.STOPPED;
                break;
            case 3:
                status = EnvironmentStatusEnum.REMOVED;
                break;
            case 4:
                status = EnvironmentStatusEnum.FAILED;
                break;
            case 5:
                status = EnvironmentStatusEnum.RUNNING;
                break;
            case 6:
                status = EnvironmentStatusEnum.STARTING;
                break;
            case 7:
                status = EnvironmentStatusEnum.STOPPING;
                break;
            default:
                status = EnvironmentStatusEnum.UNKNOWN;
                break;
        }
        EnvironmentDto dto = new EnvironmentDto(env.getUID(), env.getInternalName(), env.getLabel(), env.getApplicationRelease().getApplication().getLabel(), env
                .getApplicationRelease().getUID(), env.getApplicationRelease().getReleaseVersion(), env.getPaasUser().getSsoId().getValue(),
                env.getPaasUser().getFirstName(), env.getCreationDate(), EnvironmentTypeEnum.valueOf(env.getType().name()), status, "", -1, "no comment", env.getTechnicalDeploymentInstance()
                .getTechnicalDeployment().getName());

        return dto;
    }

    private static List<EnvironmentDto> createEnvironmentDtoList(List<Environment> envs) {
        List<EnvironmentDto> dtos = new ArrayList<EnvironmentDto>(envs.size());
        for (Environment env : envs) {
            dtos.add(new EnvironmentDto(env.getUID(), env.getInternalName(), env.getLabel(), env.getApplicationRelease().getApplication().getLabel(), env.getApplicationRelease()
                    .getUID(), env.getApplicationRelease().getReleaseVersion(), env.getPaasUser().getSsoId().getValue(), env.getPaasUser().getFirstName(), env
                    .getCreationDate(), EnvironmentTypeEnum.valueOf(env.getType().name()), EnvironmentStatusEnum.valueOf(env.getStatus().name()), env
                    .getStatusMessage(), env.getStatusPercent(), env.getComment(), env.getTechnicalDeploymentInstance().getTechnicalDeployment().getName()));
        }
        return dtos;
    }

    @Override
    public Environment update(EnvironmentDetailsDto environmentDetailsDto) {
        Environment env = null;
        try {
            env = findByUID(environmentDetailsDto.getUid());
            env.setComment(environmentDetailsDto.getComment());
        } catch (ObjectNotFoundException e) {
            e.printStackTrace(); // To change body of catch statement use File |
            // Settings | File Templates.
        }
        return env;
    }

    // ================================================================================================================================
    // TOM
    private Map<String, List<LinkDto>> getEnvironmentLinkDtos(Environment env) {

        TechnicalDeployment td = env.getTechnicalDeploymentInstance().getTechnicalDeployment();

        Map<String, List<LinkDto>> linkDtoMap = new HashMap<String, List<LinkDto>>();

        linkDtoMap.putAll(getEnvironmentAccessUrlsLinkDtos(td, linkDtoMap));
        linkDtoMap.putAll(getEnvironmentLogsUrlsLinkDtos(env, linkDtoMap));
        linkDtoMap.putAll(getEnvironmentWspLinkDtos(td, linkDtoMap));
        linkDtoMap.putAll(getEnvironmentDiagToolsUrlsLinkDtos(td, linkDtoMap));

        return linkDtoMap;
    }

    private Map<String, List<LinkDto>> getEnvironmentAccessUrlsLinkDtos(TechnicalDeployment td, Map<String, List<LinkDto>> linkDtoMap) {

        return linkDtoMap;
    }

    private Map<String, List<LinkDto>> getEnvironmentLogsUrlsLinkDtos(Environment env, Map<String, List<LinkDto>> linkDtoMap) {

        TechnicalDeployment td = env.getTechnicalDeploymentInstance().getTechnicalDeployment();

        return linkDtoMap;

    }

    private Map<String, List<LinkDto>> getEnvironmentWspLinkDtos(TechnicalDeployment td, Map<String, List<LinkDto>> linkDtoMap) {

        return linkDtoMap;
    }

    private Map<String, List<LinkDto>> getEnvironmentDiagToolsUrlsLinkDtos(TechnicalDeployment td, Map<String, List<LinkDto>> linkDtoMap) {

        URL paasLogsUrl = createPaasLogsUrl(td.getName(), true);
        URL opsLogsUrl = createOpsLogsUrl(td.getName());

        // paas logs linkDto
        linkDtoMap.putAll(addLinkDtoForLogicalModelItemInLinkDtosMap(linkDtoMap, td.getName(), paasLogsUrl, LinkDto.LinkTypeEnum.LOGS_LINK,
                LinkDto.TargetUserEnum.PAAS_USER, null));

        // LinkDto appMetricsLinksDto = new LinkDto();
        // appMetricsLinksDto.setLinkType(LinkDto.LinkTypeEnum.METRICS_LINK);
        // appMetricsLinksDto.setTargetUser(LinkDto.TargetUserEnum.PAAS_USER);
        // appMetricsLinksDto.setUrl(new
        // URL("http://hyperic_for_paas_user.com"));

        // ops logs linkDto
        linkDtoMap.putAll(addLinkDtoForLogicalModelItemInLinkDtosMap(linkDtoMap, td.getName(), opsLogsUrl, LinkDto.LinkTypeEnum.LOGS_LINK,
                LinkDto.TargetUserEnum.PAAS_OPS, null));

        // LinkDto opsMetricsLinksDto = new LinkDto();
        // opsMetricsLinksDto.setLinkType(LinkDto.LinkTypeEnum.METRICS_LINK);
        // opsLogsLinksDto.setTargetUser(LinkDto.TargetUserEnum.PAAS_USER);

        return linkDtoMap;
    }

    private Map<String, List<LinkDto>> addLinkDtoForLogicalModelItemInLinkDtosMap(Map<String, List<LinkDto>> linkDtoMap, String logicalModelId, URL url,
                                                                                  LinkDto.LinkTypeEnum linktype, LinkDto.TargetUserEnum targetUser, Map<String, String> serviceBinding) {

        if (url != null) {
            List<LinkDto> linkDtoList;

            LinkDto appssLinksDto = new LinkDto();
            appssLinksDto.setLinkType(linktype);
            appssLinksDto.setTargetUser(targetUser);
            appssLinksDto.setUrl(url);

            linkDtoList = setLinkDtoInMap(linkDtoMap, logicalModelId);

            linkDtoList.add(appssLinksDto);
            linkDtoMap.put(logicalModelId, linkDtoList);

        }

        return linkDtoMap;
    }

    private List<LinkDto> setLinkDtoInMap(Map<String, List<LinkDto>> linkDtoMap, String logicalModelId) {
        List<LinkDto> linkDtoList;

        if (linkDtoMap.get(logicalModelId) == null) {
            linkDtoList = new ArrayList<LinkDto>();
        } else {
            linkDtoList = linkDtoMap.get(logicalModelId);
        }

        return linkDtoList;
    }

    private URL createPaasLogsUrl(String logicalModelItemId, boolean overalls) {
        URL logsUrl = null;

        String searchIndex = "paasServiceName";
        String paasIndex = "index=\"paasappsindex\"";
        if (overalls) {
            searchIndex = "paasEnvName";
            paasIndex = "index=\"paasmiddlewareindex\"";
        }
        String serversNameFilter = format(paasIndex + searchIndex + "=\"{0}\"", logicalModelItemId);
        StringBuilder splunkServerUrl = new StringBuilder("http://").append(splunkIp).append(":").append(splunkPort)
                .append("/en-US/app/search/flashtimeline?auto_pause=true&q=search%20");

        try {
            logsUrl = new URL(splunkServerUrl.toString() + URLEncoder.encode(serversNameFilter, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding UTF-8 : " + e.getMessage(), e);
            throw new TechnicalException("Unsupported encoding UTF-8 : " + e.getMessage(), e);
        } catch (MalformedURLException e) {
            log.error("Bad URL: " + e.getMessage(), e);
            throw new TechnicalException("Bad URL: " + e.getMessage(), e);
        }
        return logsUrl;
    }

    // TODO create url with filter parameters for OPS
    private URL createOpsLogsUrl(String logicalModelItemId) {
        URL logsUrl = null;

        String searchIndex = "paasEnvName";
        // String paasIndex = "index=\"paasappsindex\"";
        // if (overalls) {
        // searchIndex = SplunkConsumerImpl.SPLUNK_META_NAME1;
        String paasIndex = "";
        // }
        String serversNameFilter = format(paasIndex + searchIndex + "=\"{0}\"", logicalModelItemId);
        StringBuilder splunkServerUrl = new StringBuilder("http://").append(splunkIp).append(":").append(splunkPort)
                .append("/en-US/app/search/flashtimeline?auto_pause=true&q=search%20");

        try {
            logsUrl = new URL(splunkServerUrl.toString() + URLEncoder.encode(serversNameFilter, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding UTF-8 : " + e.getMessage(), e);
            throw new TechnicalException("Unsupported encoding UTF-8 : " + e.getMessage(), e);
        } catch (MalformedURLException e) {
            log.error("Bad URL: " + e.getMessage(), e);
            throw new TechnicalException("Bad URL: " + e.getMessage(), e);
        }
        return null;
    }

    private URL createMomServiceLogsUrl(String searchItem) {
        URL logsUrl = null;

        String searchIndex = "paasEnvName";
        String paasIndex = "index=\"paasappsindex\"";
        String sourceType = "sourcetype=\"joram\"";

        String serversNameFilter = format(paasIndex + " " + sourceType + " " + searchIndex + "=\"{0}\"", searchItem);
        StringBuilder splunkServerUrl = new StringBuilder("http://").append(splunkIp).append(":").append(splunkPort)
                .append("/en-US/app/search/flashtimeline?auto_pause=true&q=search%20");

        try {
            logsUrl = new URL(splunkServerUrl.toString() + URLEncoder.encode(serversNameFilter, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error("Unsupported encoding UTF-8 : " + e.getMessage(), e);
            throw new TechnicalException("Unsupported encoding UTF-8 : " + e.getMessage(), e);
        } catch (MalformedURLException e) {
            log.error("Bad URL: " + e.getMessage(), e);
            throw new TechnicalException("Bad URL: " + e.getMessage(), e);
        }
        return logsUrl;
    }

    @Override
    public void purgeRemovedEnvironment(String envUid) throws EnvironmentNotFoundException {
        // N/A
    }

    @Override
    public List<Environment> findOldRemovedEnvironments() {
        return new ArrayList<Environment>();
    }

    @Override
    public void forceStatusForAndEnvironment(String uid, EnvironmentStatus newStatus) throws EnvironmentNotFoundException {
        // N/A
    }

    @Override
    public List<EnvironmentDto> findMyEnvironments() {
        return createEnvironmentDtoList(find(new Predicate<Environment>() {
            @Override
            public boolean apply(Environment environment) {
                return environment.getApplicationRelease().getApplication().hasForMember(currentUser());
            }
        }));
    }

    @Override
    public Long countEnvironments() {
        return count();
    }

}
