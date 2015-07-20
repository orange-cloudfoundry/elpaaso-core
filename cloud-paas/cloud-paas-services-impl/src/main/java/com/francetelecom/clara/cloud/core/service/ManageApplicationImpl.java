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

import com.francetelecom.clara.cloud.commons.AuthorizationException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import com.francetelecom.clara.cloud.core.domain.ApplicationReleaseRepository;
import com.francetelecom.clara.cloud.core.domain.ApplicationRepository;
import com.francetelecom.clara.cloud.coremodel.ConfigRoleRepository;
import com.francetelecom.clara.cloud.core.domain.PaasUserRepository;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.ConfigValue;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.services.dto.ApplicationDTO;
import com.francetelecom.clara.cloud.services.dto.ConfigOverrideDTO;
import com.francetelecom.clara.cloud.technicalservice.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Business implementation for Application management.
 * 
 * All methods are defined as transactional. If no transaction is in progress
 * during method call, then it will start a new transaction.
 */
public class ManageApplicationImpl implements ManageApplication {

	private static final Logger log = LoggerFactory.getLogger(ManageApplicationImpl.class);

	@Autowired(required = true)
	private ApplicationRepository applicationRepository;

	@Autowired(required = true)
	private ApplicationReleaseRepository applicationReleaseRepository;

	@Autowired(required = true)
	private PaasUserRepository paasUserRepository;

	@Autowired(required = true)
	private ConfigRoleRepository configRoleRepository;

	public void setApplicationRepository(ApplicationRepository repository) {
		this.applicationRepository = repository;
	}

	public void setApplicationReleaseRepository(ApplicationReleaseRepository repository) {
		this.applicationReleaseRepository = repository;
	}

	public void setPaasUserRepository(PaasUserRepository repository) {
		this.paasUserRepository = repository;
	}

	public void setConfigRoleRepository(ConfigRoleRepository configRoleRepository) {
		this.configRoleRepository = configRoleRepository;
	}

	@Override
	public List<Application> findApplications() {
		return findApplications(0, Integer.MAX_VALUE, "label", "ASC");
	}

	@Override
	public List<Application> findApplications(int first, int count, String sortProperty, String sortType) {
		List<Application> applications = applicationRepository.findAll(first, count, sortProperty, sortType);		
		for (Application application : applications) {
			application.setEditable(hasWritePermissionFor(application));
		}
		return applications;
	}

	@Override
	public List<Application> findAccessibleApplications() {
		if (SecurityUtils.currentUserIsAdmin()) {
			return applicationRepository.findAll();
		} else {
			List<Application> applications = applicationRepository.findAllPublicOrPrivateByMember(SecurityUtils.currentUser());
			for (Application application : applications) {
				application.setEditable(hasWritePermissionFor(application));
			}
			return applications;
		}
	}

	@Override
	public List<Application> findMyApplications() {
		if (SecurityUtils.currentUserIsAdmin()) {
			return findApplications();
		} else {
			return findMyApplications(0, Integer.MAX_VALUE, "label", "ASC");
		}
	}

	@Override
	public List<Application> findMyApplications(int first, int count, String sortProperty, String sortType) {
		return applicationRepository.findAllByMember(SecurityUtils.currentUser(), first, count, sortProperty, sortType);
	}

	@Override
	public long countApplications() {
		return applicationRepository.count();
	}

	@Override
	public ApplicationDTO findApplicationByLabel(String label) throws ApplicationNotFoundException {
		Application application = applicationRepository.findByLabel(label);
		if (application == null) {
			String message = "Application with label[" + label + "] does not exist.";
			log.info(message);
			throw new ApplicationNotFoundException(message);
		}

		return new ApplicationDTO(application.getUID(), application.getCode(), application.getLabel(), application.getDescription(),
				application.getApplicationRegistryUrl());
	}

	@Override
	public boolean isApplicationLabelUnique(String label) {
		return applicationRepository.findByLabel(label) == null;
	}

	private boolean isApplicationCodeUnique(String code) {
		return applicationRepository.findByCode(code) == null;
	}

	private boolean applicationHasNoActiveApplicationReleases(String applicationUID) {
		return applicationReleaseRepository.countApplicationReleasesByApplicationUID(applicationUID) == 0;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public String createPublicApplication(String code, String label, String description, URL applicationRegistryUrl, SSOId... members)
			throws DuplicateApplicationException, PaasUserNotFoundException {

		Application application = createApplication(code, label, description, applicationRegistryUrl, members);
		application.setAsPublic();

		applicationRepository.persist(application);

		return application.getUID();
	}

	private Application createApplication(String code, String label, String description, URL applicationRegistryUrl, SSOId... members)
			throws PaasUserNotFoundException, DuplicateApplicationException, AuthorizationException {
		log.debug("/******* create application with label[" + label + "] and code [" + code + "]**********/");

		Application application = new Application(label, code);
		application.setDescription(description);
		if (applicationRegistryUrl != null) {
			application.setApplicationRegistryUrl(applicationRegistryUrl);
		}

		final Set<SSOId> candidates = Arrays.asList(members).stream().collect(Collectors.toSet());

		assertMembersExist(candidates);

		application.setMembers(candidates);

		assertHasPermissionFor(application);

		// Validate model
		ValidatorUtil.validate(application);
		// if label is not unique throw exception
		if (!isApplicationLabelUnique(application.getLabel())) {
			String message = "Application with label[" + label + "] already exists.";
			log.info(message);
			throw new DuplicateApplicationException(message);
		}
		// if code is not unique throw exception
		if (!isApplicationCodeUnique(application.getCode())) {
			String message = "Application with code[" + code + "] already exists.";
			log.info(message);
			throw new DuplicateApplicationException(message);
		}
		return application;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public String createPrivateApplication(String code, String label, String description, URL applicationRegistryUrl, SSOId... members)
			throws DuplicateApplicationException, PaasUserNotFoundException {

		Application application = createApplication(code, label, description, applicationRegistryUrl, members);
		application.setAsPrivate();

		applicationRepository.persist(application);
		return application.getUID();
	}

	private void assertMembersExist(Set<SSOId> candidates) throws PaasUserNotFoundException {
			// assert users with ssoids exist
		for (SSOId candidate : candidates) {
			if (paasUserRepository.findBySsoId(candidate) == null) {
				final String message = "Cannot create application with member list: " + candidates + ". Member " + candidate + " is unknown.";
				log.debug(message);
				throw new PaasUserNotFoundException(message, candidate);
			}
			;
			}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public void deleteApplication(String applicationUID) throws ApplicationNotFoundException, AuthorizationException {
		Application application = applicationRepository.findByUid(applicationUID);
		if (application == null) {
			String message = "Application with UID[" + applicationUID + "] does not exist.";
			log.info(message);
			throw new ApplicationNotFoundException(message);
		}
		assertHasPermissionFor(application);
		// TODO we may to need to enforce this rule in Application entity,
		// perhaps in markAsRemoved() operation
		if (!applicationHasNoActiveApplicationReleases(applicationUID)) {
			String message = "Application with UID[" + applicationUID + "] has active releases.";
			log.info(message);
			throw new IllegalStateException(message);
		}
		application.markAsRemoved();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public boolean canBeDeleted(String applicationUID) throws ApplicationNotFoundException {
		log.debug("/******* find application by UID[" + applicationUID + "] **********/");
		Application application = applicationRepository.findByUid(applicationUID);
		if (application == null) {
			String message = "Application with UID[" + applicationUID + "] does not exist.";
			log.info(message);
			throw new ApplicationNotFoundException(message);
		}
		return applicationHasNoActiveApplicationReleases(applicationUID) && hasWritePermissionFor(application);
	}

	@Override
	public Application findApplicationByUID(String applicationUID) throws ApplicationNotFoundException {
		log.debug("/******* find application by UID[" + applicationUID + "] **********/");
		Application application = applicationRepository.findByUid(applicationUID);
		if (application == null) {
			String message = "Application with UID[" + applicationUID + "] does not exist.";
			log.info(message);
			throw new ApplicationNotFoundException(message);
		}
		application.setEditable(hasWritePermissionFor(application));
		return application;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public Application updateApplication(Application application) throws ApplicationNotFoundException, DuplicateApplicationException,
			PaasUserNotFoundException {
		log.debug("/******* update application with label[" + application.getLabel() + "] **********/");
		Application persisted = applicationRepository.findByUid(application.getUID());
		if (persisted == null) {
			String message = "Application with UID[" + application.getUID() + "] does not exist.";
			log.info(message);
			throw new ApplicationNotFoundException(message);
		}

		// assert members exist
		assertMembersExist(application.listMembers().stream().collect(Collectors.toSet()));

		assertHasPermissionFor(persisted);

		// if label has changed but new label is not unique -> throw
		// exception
		if (labelHasChanged(persisted, application) && !isApplicationLabelUnique(application.getLabel())) {
			String message = "Application with label[" + application.getLabel() + "] already exists.";
			log.info(message);
			throw new DuplicateApplicationException(message);
		}
		// if code has changed but code is not unique throw exception
		if (codeHasChanged(persisted, application) && !isApplicationCodeUnique(application.getCode())) {
			String message = "Application with code[" + application.getCode() + "] already exists.";
			log.info(message);
			throw new DuplicateApplicationException(message);
		}

		return applicationRepository.merge(application);
	}

	private void assertHasPermissionFor(Application application) throws AuthorizationException {
		if (!hasWritePermissionFor(application))
			throw new AuthorizationException();
	}

	private boolean hasWritePermissionFor(Application application) {
		return SecurityUtils.hasWritePermissionFor(application);
	}

	private boolean labelHasChanged(Application persisted, Application updated) {
		return !updated.getLabel().equals(persisted.getLabel());
	}

	private boolean codeHasChanged(Application persisted, Application updated) {
		return !updated.getCode().equals(persisted.getCode());
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public int purgeOldRemovedApplications() {
		log.info("*** purge old application");
		// find removed applications
		List<Application> toPurgeApplications = applicationRepository.findRemovedApplicationWithoutRelease();
		// hard remove them
		return applicationRepository.purgeApplications(toPurgeApplications);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public int deleteAndPurgeApplication(String uid) throws ApplicationNotFoundException {
		deleteApplication(uid);
		Application application = applicationRepository.findByUid(uid, true);
		return applicationRepository.purgeApplication(application);
	}

	@Override
	public long countMyApplications() {
		return applicationRepository.countByMember(SecurityUtils.currentUser());
	}

	@Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public String createConfigRole(String applicationUID, String configRoleLabel, List<ConfigOverrideDTO> overrideConfigs) throws ApplicationNotFoundException, InvalidConfigOverrideException {
        for (ConfigOverrideDTO overrideConfig : overrideConfigs) {
            try {
                ValidatorUtil.validate(overrideConfig);
            } catch (TechnicalException e) {
                throw new InvalidConfigOverrideException(e.getMessage(), e, overrideConfig);
            }
        }
		Application application = applicationRepository.findByUid(applicationUID);
		if (application == null) {
			String message = "Application with UID[" + applicationUID + "] does not exist.";
			log.info(message);
			throw new ApplicationNotFoundException(message);
		}
		assertHasPermissionFor(application);
		
		List<ConfigValue> configValues = new ArrayList<>();
		for (ConfigOverrideDTO configOverrideDTO : overrideConfigs) {
			configValues.add(new ConfigValue(configOverrideDTO.getConfigSet(), configOverrideDTO.getKey(), configOverrideDTO.getValue(),
					configOverrideDTO.getComment()));
		}
		
		ConfigRole configRole = new ConfigRole(applicationUID);
		configRole.setLastModificationComment(configRoleLabel);
		configRole.setValues(configValues);

		application.addConfigRole(configRole);

		return configRole.getUID();
	}

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public ConfigRole findConfigRole(String configRoleUID) throws ConfigRoleNotFoundException {
        log.debug("/******* find configrole by UID[" + configRoleUID + "] **********/");
        ConfigRole configRole = configRoleRepository.findByUid(configRoleUID);
        if (configRole == null) {
            String message = "configrole with UID[" + configRoleUID + "] does not exist.";
            log.info(message);
            throw new ConfigRoleNotFoundException(message);
        }
        return configRole;
    }

}
