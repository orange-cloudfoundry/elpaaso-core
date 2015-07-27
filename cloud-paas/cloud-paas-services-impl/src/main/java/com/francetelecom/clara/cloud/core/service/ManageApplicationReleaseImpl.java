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
package com.francetelecom.clara.cloud.core.service;

import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import com.francetelecom.clara.cloud.core.service.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.core.service.exception.PaasUserNotFoundException;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplateRepository;
import com.francetelecom.clara.cloud.paas.projection.ProjectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.List;

/**
 * Business implementation for ApplicationRelease management
 * <p/>
 * All methods are defined as transactional. If no transaction is in progress
 * during method call, then it will start a new transaction.
 *
 * @author Clara
 */
public class ManageApplicationReleaseImpl implements ManageApplicationRelease {

    private static final Logger log = LoggerFactory.getLogger(ManageApplicationReleaseImpl.class);

    @Autowired(required = true)
    private ApplicationReleaseRepository applicationReleaseRepository;

    @Autowired(required = true)
    private PaasUserRepository paasUserRepository;

    @Autowired(required = true)
    private ApplicationRepository applicationRepository;

    @Autowired(required = true)
    private EnvironmentRepository environmentRepository;

    @Autowired(required = true)
    private ProjectionService projectionService;

    @Autowired(required = true)
    private TechnicalDeploymentTemplateRepository technicalDeploymentTemplateRepository;

    @Override
    public List<ApplicationRelease> findApplicationReleases(int firstIndex, int count) {
        if (SecurityUtils.currentUserIsAdmin()) {
            return applicationReleaseRepository.findAll();
        } else {
            return applicationReleaseRepository.findAllPublicOrPrivateByMember(SecurityUtils.currentUser().getValue());
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public List<ApplicationRelease> findMyApplicationReleases() {
            return (List<ApplicationRelease>) applicationReleaseRepository.findAllByApplicationMember(SecurityUtils.currentUser().getValue());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public List<ApplicationRelease> findApplicationReleasesByAppUID(String applicationUid) throws ApplicationNotFoundException {
        Application application = applicationRepository.findByUid(applicationUid);
        if (application == null) {
            String message = "Application[" + applicationUid + "] does not exist";
            log.info(message);
            throw new ApplicationNotFoundException(message);
        }
        if (SecurityUtils.currentUserIsAdmin()) {
            return applicationReleaseRepository.findApplicationReleasesByAppUID(applicationUid);
        } else {
            return applicationReleaseRepository.findPublicOrPrivateByMemberAndByAppUID(SecurityUtils.currentUser().getValue(), applicationUid);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = {"BusinessException"})
    public String createApplicationRelease(String applicationUID, String ssoId, String version) throws PaasUserNotFoundException, ApplicationNotFoundException,
            DuplicateApplicationReleaseException {
        return createApplicationRelease(applicationUID, ssoId, version, null, null, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = {"BusinessException"})
    public String createApplicationRelease(String applicationUID, String ssoId, String version, String description, URL versionControlUrl, String middlewareProfile)
            throws PaasUserNotFoundException, ApplicationNotFoundException, DuplicateApplicationReleaseException {
        PaasUser paasUser = paasUserRepository.findBySsoId(new SSOId(ssoId));
        if (paasUser == null) {
            String message = "PaasUser[" + ssoId + "] does not exist";
            log.info(message);
            throw new PaasUserNotFoundException(message);
        }

        Application application = applicationRepository.findByUid(applicationUID);
        if (application == null) {
            String message = "Application[" + applicationUID + "] does not exist";
            log.info(message);
            throw new ApplicationNotFoundException(message);
        }

        ApplicationRelease applicationRelease = new ApplicationRelease(application, version);

        if (middlewareProfile != null) {
            applicationRelease.setMiddlewareProfileVersion(middlewareProfile);
        }
        if (description != null) {
            applicationRelease.setDescription(description);
        }
        if (versionControlUrl != null) {
            applicationRelease.setVersionControlUrl(versionControlUrl);
        }

        // cannot create if not authorized
        assertHasWritePermissionFor(applicationRelease);

        // if application version is not unique per application then throw
        // exception
        if (!isReleaseVersionUniqueForApplication(applicationUID, version)) {
            String message = "A release [" + version + "] already exists for the application [" + application.getLabel() + "]";
            log.info(message);
            throw new DuplicateApplicationReleaseException(message);
        }

        // Validate model
        ValidatorUtil.validate(applicationRelease);

        applicationReleaseRepository.save(applicationRelease);

        return applicationRelease.getUID();
    }

    private boolean releaseHasNoActiveEnvironment(String releaseUID) {
        return environmentRepository.countActiveByApplicationReleaseUid(releaseUID) == 0;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = {"BusinessException"})
    public void deleteApplicationRelease(String releaseUID) throws ApplicationReleaseNotFoundException {
        ApplicationRelease applicationRelease = applicationReleaseRepository.findByUID(releaseUID);
        // we should find application release
        if (applicationRelease == null) {
            String message = "ApplicationRelease[" + releaseUID + "] does not exist";
            log.info(message);
            throw new ApplicationReleaseNotFoundException(message);
        }
        // cannot remove if not authorized
        assertHasWritePermissionFor(applicationRelease);

        // we can remove release if it has no active environment
        if (releaseHasNoActiveEnvironment(releaseUID)) {
            applicationRelease.markAsRemoved();
        } else {
            String message = "Cannot delete release with UID[" + releaseUID + "] because it has active environments.";
            log.info(message);
            throw new IllegalStateException(message);
        }
    }

    private void assertHasWritePermissionFor(ApplicationRelease applicationRelease) {
        SecurityUtils.assertHasWritePermissionFor(applicationRelease);
    }

    private boolean hasWritePermissionFor(ApplicationRelease applicationRelease) {
        return SecurityUtils.hasWritePermissionFor(applicationRelease.getApplication());
    }

    private void assertHasReadPermissionFor(ApplicationRelease applicationRelease) {
        SecurityUtils.assertHasReadPermissionFor(applicationRelease);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void purgeOldRemovedReleases() {
        log.info("*** purge old releases");
        // find removed releases
        List<ApplicationRelease> toPurgeReleases = applicationReleaseRepository.findRemovedReleasesWithoutEnvironment();
        if (toPurgeReleases != null) {
            for (ApplicationRelease release : toPurgeReleases) {
                deleteTechnicalDeploymentTemplates(release.getUID());
                applicationReleaseRepository.delete(release);
            }
        }
    }

    private void deleteTechnicalDeploymentTemplates(String releaseId) {
        List<TechnicalDeploymentTemplate> templates = technicalDeploymentTemplateRepository.findAllByReleaseId(releaseId);
        if (templates != null) {
            technicalDeploymentTemplateRepository.delete(templates);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public void deleteAndPurgeApplicationRelease(String uid) throws ApplicationReleaseNotFoundException {
        deleteApplicationRelease(uid);
        ApplicationRelease applicationRelease = applicationReleaseRepository.findByUID(uid);
        deleteTechnicalDeploymentTemplates(applicationRelease.getUID());
        applicationReleaseRepository.delete(applicationRelease);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = {"BusinessException"})
    public boolean canBeDeleted(String releaseUID) throws ApplicationReleaseNotFoundException {
        ApplicationRelease applicationRelease = applicationReleaseRepository.findByUID(releaseUID);
        if (applicationRelease == null) {
            String message = "ApplicationRelease[" + releaseUID + "] does not exist";
            log.info(message);
            throw new ApplicationReleaseNotFoundException(message);
        }
        return environmentRepository.countActiveByApplicationReleaseUid(releaseUID) == 0 && hasWritePermissionFor(applicationRelease);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public ApplicationRelease findApplicationReleaseByUID(String uid) throws ApplicationReleaseNotFoundException {
        log.debug("/******* recherche de l application release - UID[" + uid + "] **********/");
        ApplicationRelease applicationRelease = applicationReleaseRepository.findByUID(uid);
        if (applicationRelease == null) {
            String message = "ApplicationRelease[" + uid + "] does not exist";
            log.info(message);
            throw new ApplicationReleaseNotFoundException(message);
        }

        assertHasReadPermissionFor(applicationRelease);

        applicationRelease.getApplication().setEditable(hasWritePermissionFor(applicationRelease));
        return applicationRelease;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public ApplicationRelease updateApplicationRelease(ApplicationRelease applicationRelease) throws ApplicationReleaseNotFoundException {
        ApplicationRelease existing = applicationReleaseRepository.findByUID(applicationRelease.getUID());
        if (existing == null) {
            String message = "ApplicationRelease with uid[" + applicationRelease.getUID() + "] does not exist.";
            log.info(message);
            throw new ApplicationReleaseNotFoundException(message);
        }
        // cannot remove if not authorized
        assertHasWritePermissionFor(applicationRelease);

        return applicationReleaseRepository.save(applicationRelease);
    }

    @Override
    public long countApplicationReleases() {
        if (SecurityUtils.currentUserIsAdmin()) {
            return applicationReleaseRepository.countApplicationReleases();
        } else {
            return applicationReleaseRepository.countPublicOrPrivateByMember(SecurityUtils.currentUser().getValue());
        }
    }

    @Override
    public long countMyApplicationReleases() {
        return applicationReleaseRepository.countByApplicationMember(SecurityUtils.currentUser().getValue());
    }

    @Override
    public long countApplicationReleasesByAppUID(String applicationUID) throws ApplicationNotFoundException {
        Application application = applicationRepository.findByUid(applicationUID);
        if (application == null) {
            String message = "Application[" + applicationUID + "] does not exist";
            log.info(message);
            throw new ApplicationNotFoundException(message);
        }
        if (SecurityUtils.currentUserIsAdmin()) {
            return applicationReleaseRepository.countApplicationReleasesByApplicationUID(applicationUID);
        } else {
            return applicationReleaseRepository.countPublicOrPrivateByMemberAndByAppUID(SecurityUtils.currentUser().getValue(), applicationUID);
        }
    }

    @Override
    public ApplicationRelease findApplicationReleaseByApplicationAndReleaseVersion(String applicationUID, String releaseVersion) throws ApplicationReleaseNotFoundException {
        ApplicationRelease release = applicationReleaseRepository.findByApplicationUIDAndReleaseVersion(applicationUID, releaseVersion);
        if (release == null) {
            throw new ApplicationReleaseNotFoundException("ApplicationRelease from application " + applicationUID + " and with version " + releaseVersion + " has not been found");
        }

        assertHasReadPermissionFor(release);

        return release;
    }

    @Override
    public boolean isReleaseVersionUniqueForApplication(String applicationUID, String version) {
        return applicationReleaseRepository.findByApplicationUIDAndReleaseVersion(applicationUID, version) == null;
    }

    public void setEnvironmentRepository(EnvironmentRepository repository) {
        this.environmentRepository = repository;
    }

    @Override
    public List<MiddlewareProfile> findAllMiddlewareProfil() {
        return projectionService.findAllMiddlewareProfil();
    }

    public void setApplicationRepository(ApplicationRepository repository) {
        this.applicationRepository = repository;
    }

    public void setPaasUserRepository(PaasUserRepository repository) {
        this.paasUserRepository = repository;
    }

    public void setApplicationReleaseRepository(ApplicationReleaseRepository repository) {
        this.applicationReleaseRepository = repository;
    }

    public void setProjectionService(ProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    public void setTechnicalDeploymentTemplateRepository(TechnicalDeploymentTemplateRepository technicalDeploymentTemplateRepository) {
        this.technicalDeploymentTemplateRepository = technicalDeploymentTemplateRepository;
    }

}
