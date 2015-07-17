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
package com.francetelecom.clara.cloud.paas.projection;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.google.common.collect.Lists;

/**
 * This new dispatcher enables the paas to produce different architecture,
 * depending on the middlewareprofileversion of the release, and the environment
 * type.
 * 
 * Technically, it makes a lookup in a SpringBeans hashtable of all possible
 * projection, per middlewareProfileVersion and env type.
 * 
 * @author APOG7416
 * 
 */
public class MultiProfileProjectionServiceDispatcherImpl implements ProjectionService {

	private static Logger logger = LoggerFactory.getLogger(MultiProfileProjectionServiceDispatcherImpl.class);

	protected EnumMap<MiddlewareProfile, ProjectionService> projectionsPerMiddlewareProfileVersion;
	
	@Override
	public TechnicalDeploymentTemplate generateNewDeploymentTemplate(ApplicationRelease applicationRelease, DeploymentProfileEnum profile) throws UnsupportedProjectionException {
		String middleWareProfileVersion = applicationRelease.getMiddlewareProfileVersion();
		MiddlewareProfile middlewareProfile = MiddlewareProfile.fromVersion(middleWareProfileVersion);

		ProjectionService projection = this.projectionsPerMiddlewareProfileVersion.get(middlewareProfile);

		if (projection == null) {
			logger.warn("Requested unsupported profile [" + middleWareProfileVersion + "] Supported ones are:"
					+ projectionsPerMiddlewareProfileVersion.keySet().toString());
			throw new TechnicalException("Unable to generate TDT: unsupported middleware profile version:" + middleWareProfileVersion);
		}
		
		TechnicalDeploymentTemplate technicalDeployment;
		
		technicalDeployment = projection.generateNewDeploymentTemplate(applicationRelease, profile);

		return technicalDeployment;
	}

	@Override
	public List<MiddlewareProfile> findAllMiddlewareProfil() {
		if (projectionsPerMiddlewareProfileVersion != null) {
			return Lists.newArrayList(projectionsPerMiddlewareProfileVersion.keySet());
		}
		return Collections.emptyList();
	}

	@Required
	public void setProjectionsPerMiddlewareProfileVersion(EnumMap<MiddlewareProfile, ProjectionService> projectorPerProfilesEnumMap) {
		this.projectionsPerMiddlewareProfileVersion = projectorPerProfilesEnumMap;
	}

	@Override
	public void updateDeploymentTemplateInstance(TechnicalDeploymentInstance tdi, ApplicationRelease applicationRelease, List<ConfigRole> configRoles) {
		MiddlewareProfile profile = MiddlewareProfile.fromVersion(applicationRelease.getMiddlewareProfileVersion());
		ProjectionService projection = this.projectionsPerMiddlewareProfileVersion.get(profile);
		if (projection == null) {
			logger.warn("Requested unsupported profile [" + profile + "] Supported ones are:"
					+ projectionsPerMiddlewareProfileVersion.keySet().toString());
			throw new TechnicalException("Unable to generate TDT: unsupported middleware profile version:" + profile);
		}
		projection.updateDeploymentTemplateInstance(tdi, applicationRelease, configRoles);
	}
}
