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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;

/**
 * TestMultiProfileProjectionServiceDispatcherImpl classe used by VcdProjectionServiceTest.testHostnamesUniqueAcrossProjectionsForVHM and set a custom
 * projection timestamp.
 * 
 * Last update : $LastChangedDate$ Last author : $Author$
 * 
 * @version : $Revision$
 */
public class TestMultiProfileProjectionServiceDispatcherImpl extends MultiProfileProjectionServiceDispatcherImpl {

	private static Logger logger = LoggerFactory.getLogger(TestMultiProfileProjectionServiceDispatcherImpl.class);

	/**
	 * This method help test method to simulate the projection timestamp to use
	 * 
	 * @param currentTimeMillis
	 * @param applicationRelease
	 * @param profile
	 * @return
	 */
	public TechnicalDeploymentTemplate generateNewDeploymentTemplateUsingCustomTimestamp(long currentTimeMillis, ApplicationRelease applicationRelease,
			DeploymentProfileEnum profile) {
		String middleWareProfileVersion = applicationRelease.getMiddlewareProfileVersion();
		logger.debug("looking for middlewareProfileVersion " + middleWareProfileVersion);
		MiddlewareProfile middlewareProfile = MiddlewareProfile.fromVersion(middleWareProfileVersion);

		ProjectionService projection = this.projectionsPerMiddlewareProfileVersion.get(middlewareProfile);

		TechnicalDeploymentTemplate technicalDeployment;
		try {
			technicalDeployment = projection.generateNewDeploymentTemplate(applicationRelease, profile);
			return technicalDeployment;
		} catch (Exception e) {
			throw new TechnicalException(e);
		}
	}
}
