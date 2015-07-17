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
package com.francetelecom.clara.cloud.paas.activation;

import com.francetelecom.clara.cloud.commons.tasks.PollTaskStateInterface;

/**
 * Api to Paas Activation Layer
 */
public interface ManagePaasActivation extends PollTaskStateInterface<TaskStatusActivation> {

    /**
     * Asynchronous method to create and power on a technical deployment (virtual appliance).
     * There is no prerequisite that the {@link #generatePlatformServerTemplates(com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate)}  method be called prior to
     * activate() method. A lazy call is made instead
     *
     * @param tdiId
     * Identifier of the technical deployment instance that will be updated in DB as part of the activation process (it
     * is therefore expected to be initially persisted in db prior to this method call.)
     * This tdi points to a TDT which may or not have its server template been eagerly generated into {@link #generatePlatformServerTemplates(com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate)}
     * The {@link com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate#getDeploymentState()} indicates this
     * status.
     * @return A task status
     */
    public TaskStatusActivation activate(int tdiId);

	/**
	 * Asynchronous method to start a technical deployment instance (virtual appliance)
	 * @param tdiId Identifier of the technical deployment instance to delete
	 * @return A task status
	 */
	public TaskStatusActivation start(int tdiId);
	
	/**
	 * Asynchronous method to stop a technical deployment instance (virtual appliance)
	 * @param tdiId Identifier of the technical deployment instance to delete
	 * @return A task status
	 */
	public TaskStatusActivation stop(int tdiId);
	
	/**
	 * Asynchronous method to delete a technical deployment instance (virtual appliance)
	 * @param tdiId Identifier of the technical deployment instance to delete
	 * @return A task status
	 */
	public TaskStatusActivation delete(int tdiId);
}
