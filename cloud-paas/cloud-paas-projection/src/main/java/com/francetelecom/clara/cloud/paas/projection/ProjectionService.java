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

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;

import java.util.List;

/**
 * The Projection translates a request for an new environment (including its logical model) into a technical deployment template. The projection hides the
 * decisions on how to handle customer facing catalog in technical terms
 * 
 */
public interface ProjectionService {

	/**
	 * Requests generation of a TDD from a given application release specification
	 * 
	 * @param applicationRelease
	 *            The appRelease which holds a valid logical deployment
	 * @param profile
	 *            the type of enviromnent to generate the template of.
	 * @return
	 * @throws UnsupportedProjectionException
	 *             if the requested ApplicationRelease is invalid and not environment can be created from it.
	 */
	public TechnicalDeploymentTemplate generateNewDeploymentTemplate(ApplicationRelease applicationRelease, DeploymentProfileEnum profile)
			throws UnsupportedProjectionException;

	/**
	 * Return all available middleware profile
	 * 
	 * @return all available middleware profile
	 */
	List<MiddlewareProfile> findAllMiddlewareProfil();

	/**
	 * Update technical deployment template instance to handle configuration properties,
	 * including the ones which are overridden using {@link ConfigRole}.
     * @param configRoles a non-null, non-empty list of config roles to apply, otherwise an IllegalArgumentException is thrown
	 */
	public void updateDeploymentTemplateInstance(TechnicalDeploymentInstance tdi, ApplicationRelease applicationRelease, List<ConfigRole> configRoles);
}