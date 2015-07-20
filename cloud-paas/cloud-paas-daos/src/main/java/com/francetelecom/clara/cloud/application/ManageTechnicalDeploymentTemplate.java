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
package com.francetelecom.clara.cloud.application;


import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.commons.NotFoundException;

/**
 * TDI service facade interface.
 */
public interface ManageTechnicalDeploymentTemplate {

	/**
	 * Retrieve a tdt from its id.
	 * 
	 * @param tdtId
	 *            id of tdtId to be consulted
	 * @throws NotFoundException
	 *             if tdtId does not exist
	 */
	TechnicalDeploymentTemplate findTechnicalDeploymentTemplate(int tdtId) throws NotFoundException;
	
	/**
	 * Retrieve a tdt from deployment profile and release uid.
	 * 
	 * @throws NotFoundException
	 *             if tdtId does not exist
	 */
	TechnicalDeploymentTemplate findTechnicalDeploymentTemplate(DeploymentProfileEnum profile, String releaseId) throws NotFoundException;
	
	/**
	 * Creation of TDT (persists it)
	 * @param tdt
	 */
	void createTechnicalDeploymentTemplate(TechnicalDeploymentTemplate tdt);
	

}
