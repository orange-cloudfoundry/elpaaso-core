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

import java.util.ArrayList;
import java.util.List;

import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import com.francetelecom.clara.cloud.core.domain.ApplicationReleaseRepository;
import com.francetelecom.clara.cloud.coremodel.ConfigRoleRepository;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.core.domain.PaasUserRepository;
import com.francetelecom.clara.cloud.core.service.SecurityUtils;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.dao.TechnicalDeploymentCloner;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.paas.projection.ProjectionService;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.technicalservice.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.NotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.PaasUserNotFoundException;

/**
 * Utils to manages environment. We need this class to solve transaction lifecycle
 */
public class ManageEnvironmentImplUtils {

	private EnvironmentRepository environmentRepository;

	private PaasUserRepository paasUserRepository;

	private ProjectionService projectionService;

	private ManageTechnicalDeploymentTemplate manageTechnicalDeploymentTemplate;
	
	private ConfigRoleRepository configRoleRepository;

	private ApplicationReleaseRepository applicationReleaseRepository;

	private TechnicalDeploymentCloner tdCloner;

	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackFor = BusinessException.class)
	public String createTDI(String releaseUID, DeploymentProfileEnum profile, String ownerSsoId, String label, List<String> configRolesUIDs) throws PaasUserNotFoundException,
			ApplicationReleaseNotFoundException, UnsupportedProjectionException {

		// Fetch an AppRelease from DB which is up-to-date and may be browsed
		// for fetching lazy associations
		ApplicationRelease applicationRelease = applicationReleaseRepository.findByUID(releaseUID);
		if (applicationRelease == null) {
			throw new ApplicationReleaseNotFoundException("ApplicationRelease#" + releaseUID);
		}

		// Fetch a PaasUser from DB which is up-to-date and may be browsed for
		// fetching lazy associations
		PaasUser owner = paasUserRepository.findBySsoId(new SSOId(ownerSsoId));
		if (owner == null) {
			throw new PaasUserNotFoundException("PaasUser#" + ownerSsoId);
		}

		assertHasWritePermissionFor(applicationRelease);
		
		// Force projection and locking of AppRelease
		if (applicationRelease.isEditing()) {
			// TODO: add consistency validation step and reject the request
			applicationRelease.validate();
		}

		if (applicationRelease.isValidated()) {
			generateDeploymentTemplates(applicationRelease,profileToUseForProjection());
			applicationRelease.lock();
			
		}
		
		TechnicalDeploymentTemplate tdt = null;

		try {
			tdt = manageTechnicalDeploymentTemplate.findTechnicalDeploymentTemplate(profile, applicationRelease.getUID());
		} catch (NotFoundException e) {
			//should not occur since tdt has just been created
			throw new TechnicalException(e);
		}
		
		// Instantiate the TDI and point it to the TDT.
		// FIXME: envName -> replace by
		TechnicalDeployment tdClone = tdCloner.deepCopy(tdt.getTechnicalDeployment());
		TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, tdClone);

        overrideTemplateWhenConfigRolesSpecified(configRolesUIDs, applicationRelease, tdi);
		
		// FIXME: ensure max size of 150 chars on environment name
		Environment environment = new Environment(profile, label, applicationRelease, owner,tdi);
        ValidatorUtil.validate(environment);
		environmentRepository.persist(environment);

		return environment.getUID();
	}

    protected void overrideTemplateWhenConfigRolesSpecified(List<String> configRolesUIDs, ApplicationRelease applicationRelease, TechnicalDeploymentInstance tdi) {
        List<ConfigRole> configRoles = null;
        if (configRolesUIDs != null) {
            configRoles = configRoleRepository.findByUidIn(configRolesUIDs);
        }
        if (configRoles != null && ! configRoles.isEmpty()) {
            projectionService.updateDeploymentTemplateInstance(tdi, applicationRelease, configRoles);
        }
    }


    private void assertHasWritePermissionFor(ApplicationRelease release) {
		SecurityUtils.assertHasWritePermissionFor(release);
	}

	/**
	 * For now, we limit projection to DEV and PROD.
	 * 
	 * @return
	 */
	public static List<DeploymentProfileEnum> profileToUseForProjection() {
		List<DeploymentProfileEnum> profiles = new ArrayList<DeploymentProfileEnum>();
		profiles.add(DeploymentProfileEnum.DEVELOPMENT);
		profiles.add(DeploymentProfileEnum.PRODUCTION);
		return profiles;
	}

	private void generateDeploymentTemplates(ApplicationRelease applicationRelease,List<DeploymentProfileEnum> profiles) throws UnsupportedProjectionException {
		//FIXME à déplacer ds projectionService.generateNewDeploymentTemplate 
		assert applicationRelease.isValidated();

		for (DeploymentProfileEnum profile : profiles) {
			// FIXME: propagate the Tenant to the projection, or alternatively
			// the SubTenantCatalog to use.
			TechnicalDeploymentTemplate tdt = projectionService.generateNewDeploymentTemplate(applicationRelease, profile);
			manageTechnicalDeploymentTemplate.createTechnicalDeploymentTemplate(tdt);
		}
		
	}

	public void setEnvironmentRepository(EnvironmentRepository repository) {
		this.environmentRepository = repository;
	}

	public void setPaasUserRepository(PaasUserRepository repository) {
		this.paasUserRepository = repository;
	}

	public void setProjectionService(ProjectionService projectionService) {
		this.projectionService = projectionService;
	}

	public void setConfigRoleRepository(ConfigRoleRepository configRoleRepository) {
		this.configRoleRepository = configRoleRepository;
	}

	public void setApplicationReleaseRepository(ApplicationReleaseRepository repository) {
		this.applicationReleaseRepository = repository;
	}

	public void setTdCloner(TechnicalDeploymentCloner tdCloner) {
		this.tdCloner = tdCloner;
	}


	public void setManageTechnicalDeploymentTemplate(ManageTechnicalDeploymentTemplate manageTechnicalDeploymentTemplate) {
		this.manageTechnicalDeploymentTemplate = manageTechnicalDeploymentTemplate;
	}
}
