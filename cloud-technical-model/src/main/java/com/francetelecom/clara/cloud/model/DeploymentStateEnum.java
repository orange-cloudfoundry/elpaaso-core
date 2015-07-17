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
package com.francetelecom.clara.cloud.model;

/**
 * Enum to allow to track changes in deployment of elements in TDI/TDT
 * @author apog7416
 *
 */
public enum DeploymentStateEnum {
    /**
     * The element needs to be deployed as part of the activation process.
     */
	TRANSIENT,
	/**
	 * The element is provided (e.g. in a base OS image) and the next step do not need to deploy them.
	 */
	PROVIDED,
	/**
	 * The element has been checked (init) and is ready to be created.
	 */
	CHECKED,
	/**
	 * The element was deployed by the previous element in the activation chain.
	 */
	CREATED,
	/**
	 * The element was stopped
	 */
	STOPPED,
	/**
	 * The element was started
	 */
	STARTED,
	UNKNOWN,
	REMOVED,

    /**
     * The element is currently being deployed. This state maybe useful to avoid concurrent deployment of
     * the same element
     *
     * FIXME: review synchronization for concurrent activities to avoid relying solely on the DB state but
     * instead/in addition leverage in memory synchronization/notifications
     */
    INPROGRESS,
    
	/**
	 * The element deployment is in progress.
	 */
    CREATING,
    
	/**
	 * The element is about to be started.
	 */
    STARTING,
    
	/**
	 * The element is about to be stopped.
	 */
    STOPPING,
    
	/**
	 * The element is about to be removed.
	 */
    REMOVING,
}
