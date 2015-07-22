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

import java.net.URL;
import java.util.List;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.services.dto.ApplicationDTO;
import com.francetelecom.clara.cloud.services.dto.ConfigOverrideDTO;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.ConfigRoleNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.InvalidConfigOverrideException;
import com.francetelecom.clara.cloud.coremodel.exception.PaasUserNotFoundException;
import org.springframework.data.domain.Sort;

/**
 * Application management service facade interface.
 */
public interface ManageApplication {

	/**
	 * Find all applications.
	 * 
	 * @return List of Application
	 */
	List<Application> findApplications();

	/**
	 * Find active and editable or public applications
	 * 
	 * @return Applications
	 */
	List<Application> findAccessibleApplications();
	
	/**
	 * Find active and editable applications
	 * 
	 * @return Applications
	 */
	List<Application> findMyApplications();

	/**
	 * Create an application.
	 * 
	 * @param code
	 *            code (must have text, must be unique)
	 * @param label
	 *            label (must have text, must be unique)
	 * @param description
	 *            description
	 * @param applicationRegistryUrl
	 *            registry url
	 * @param members TODO
	 * @return application universal id
	 * @throws DuplicateApplicationException
	 *             if application exists
	 * @throws PaasUserNotFoundException 
	 */
	public String createPublicApplication(String code, String label, String description, URL applicationRegistryUrl, SSOId... members)
			throws DuplicateApplicationException, PaasUserNotFoundException;

	/**
	 * Create an private application.
	 * 
	 * @param code
	 *            code (must have text, must be unique)
	 * @param label
	 *            label (must have text, must be unique)
	 * @param description
	 *            description
	 * @param applicationRegistryUrl
	 *            registry url
	 * @param userSsoIds
	 *            application member ssoId list (required when application is
	 *            private)
	 * @return application universal id
	 * @throws DuplicateApplicationException
	 *             if application exists
	 * @throws PaasUserNotFoundException
	 */
	public String createPrivateApplication(String code, String label, String description, URL applicationRegistryUrl, SSOId... userSsoIds)
			throws DuplicateApplicationException, PaasUserNotFoundException;

	/**
	 * Delete an application.
	 * 
	 * @param applicationUID
	 *            application uid
	 * @throws ApplicationNotFoundException
	 *             if application does not exist
	 */
	void deleteApplication(String applicationUID) throws ApplicationNotFoundException;

	/**
	 * Indicates whether application can be deleted or not, e.g. are there one
	 * or more application release that is not removed?
	 * 
	 * @param applicationUID
	 *            UID of the application
	 * @return true if application can be deleted
	 * @throws ApplicationNotFoundException
	 *             if application does not exist
	 */
	boolean canBeDeleted(String applicationUID) throws ApplicationNotFoundException;

	/**
	 * Find active application (not removed) by its name.
	 * 
	 * @param applicationUID
	 *            UID of application to be found
	 * @throws ApplicationNotFoundException
	 *             if application does not exist
	 */
	Application findApplicationByUID(String applicationUID) throws ApplicationNotFoundException;

	/**
	 * Find application by its label.
	 * 
	 * @param label
	 *            label of application to be found
	 * @return application dto
	 * @throws ApplicationNotFoundException
	 *             if application does not exist
	 */
	ApplicationDTO findApplicationByLabel(String label) throws ApplicationNotFoundException;

	/**
	 * Update an application.
	 * 
	 * @param application
	 *            application to be updated
	 * @return updated Application
	 * 
	 * @throws ApplicationNotFoundException
	 *             when application to be updated does not exist
	 * @throws DuplicateApplicationException
	 *             when new application label or code exists
	 * @throws PaasUserNotFoundException
	 *             when application members do not exist
	 */
	Application updateApplication(Application application) throws ApplicationNotFoundException, DuplicateApplicationException,
			PaasUserNotFoundException;

	/**
	 * Count applications
	 * 
	 * @return number of applications
	 */
	long countApplications();
	
	/**
	 * Count active and editable applications
	 * 
	 * @return number of applications
	 */
	long countMyApplications();


	/**
	 * Return true if application label is unique.
	 * 
	 * @param label
	 *            application label
	 * 
	 * @return true if application label is unique.
	 */
	boolean isApplicationLabelUnique(String label) throws BusinessException;

	/**
	 * Purge old removed applications. NB/ application with release (event with
	 * REMOVED status) are never purged
	 * 
	 * @return number of hard deleted applications
	 */
	void purgeOldRemovedApplications();

	void purgeApplication(String uid) throws ApplicationNotFoundException;

    /**
     * Creates a new config role to permanently save enviroment specific configuration that override the default
     * config values specified in the architecture
     * @return the config role UID
     * @throws ApplicationNotFoundException
     * @throws InvalidConfigOverrideException if the specified dto was invalid
     */
	String createConfigRole(String applicationUID, String configRoleLabel, List<ConfigOverrideDTO> overrideConfigs) throws ApplicationNotFoundException, InvalidConfigOverrideException;

    ConfigRole findConfigRole(String configRoleUID) throws ConfigRoleNotFoundException;
}
