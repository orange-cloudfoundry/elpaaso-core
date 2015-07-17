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

import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.technicalservice.exception.NotFoundException;

/**
 * TDI service facade interface.
 */
public interface ManageTechnicalDeploymentInstance extends ManageModelItem {

	/**
	 * Creates a tdi from a tdt and a td
	 * 
	 * @param td
	 *            associated technical deployment
	 */
	TechnicalDeploymentInstance createTechnicalDeploymentInstance(TechnicalDeploymentTemplate tdt, TechnicalDeployment td);

	
	/**
	 * Retrieve a tdi from its id.
	 * 
	 * @param tdiId
	 *            id of tdi to be consulted
	 * @throws NotFoundException
	 *             if tdi does not exist
	 */
	TechnicalDeploymentInstance findTechnicalDeploymentInstance(int tdiId) throws NotFoundException;

	/**
	 * Update a tdi.
	 * 
	 * @param tdi
	 *            tdi to be updated
	 * @throws NotFoundException
	 *             if tdi to be updated does not exist
	 */
	void updateTechnicalDeploymentInstance(TechnicalDeploymentInstance tdi) throws NotFoundException;
}
