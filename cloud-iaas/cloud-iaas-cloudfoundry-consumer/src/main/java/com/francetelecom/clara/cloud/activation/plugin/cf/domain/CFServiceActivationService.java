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
package com.francetelecom.clara.cloud.activation.plugin.cf.domain;

import com.francetelecom.clara.cloud.techmodel.cf.UserProvidedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;

/**
 * handle cloud foundry service activation
 *
 */
public interface CFServiceActivationService {

	/**
	 * Activate a user provided service.
	 *
	 * @param service
	 *            the user provided service
	 *
	 * @return the status of requested operation
	 */
	public ServiceActivationStatus activate(UserProvidedService service);

	/**
	 * Activate a managed service.
     *
	 * @param service
	 *            the managed service
	 *
	 * @return the status of requested operation
	 */
	public ServiceActivationStatus activate(ManagedService service);

	/**
	 * Delete a service. if bindings exist, removes those bindings.
     * Will not attempt to delete service if service is missing.
     *
	 * @param service
	 *            the user provided service
	 *
	 * @return the status of requested operation
	 */
	public ServiceActivationStatus delete(UserProvidedService service);
	
	/**
	 * Delete a service. if bindings exist, removes those bindings.
     * Will not attempt to delete service if service is missing.
     *
	 * @param service
	 *            the managed service
	 *
	 * @return the status of requested operation
	 */
	public ServiceActivationStatus delete(ManagedService service);

	/**
	 * @return service last operation status
	 */
	ServiceActivationStatus getServiceActivationStatus(ServiceActivationStatus status);
}
