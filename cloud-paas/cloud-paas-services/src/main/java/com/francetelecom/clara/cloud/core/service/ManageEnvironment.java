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

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.EnvironmentNotFoundException;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.EnvironmentStatus;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentOpsDetailsDto;

import java.util.List;

/**
 * Application interface to manage environments. Uses DTOs as parameters and returned values.
 */
public interface ManageEnvironment {

	/**
	 * Requests the creation of an environment for a given application release (including disk image creation and environment instanciation).
	 * 
	 * <h2>Preconditions:</h2>
	 * <ol>
	 * <li>The logical model is finalized in the ApplicationRelease which is ready to be locked if not already locked</li>
	 * </ol>
	 * 
	 * <h2>Post conditions:</h2>
	 * <ol>
	 * <li>This method returns immediately with a reference to the Environment. This reference may be used to perform queries through
	 * {@link #findEnvironmentByUID(String)} to follow is state.</li>
	 * <li>The ApplicationRelease is populated with TDTs for all environment types.</li>
	 * <li>The ApplicationRelease is locked and may not be changed.</li>
	 * </ol>
	 * 
	 * @param releaseUID
	 *            The application release uid that is deployed on the requested environment.
	 * @param type
	 *            The type of environment to instanciate.
	 * @param label
	 *            A human-readeable name of the environment (e.g. "Guillaume's dev env 1") used in GUIs. This is not an idenfier.
	 * @param ownerSsoId
	 *            The primary owner identifier of the environment. This in turn selects the associated Tenant used for the default projection rules, and the
	 *            software product catalog.
	 * @return The environment UID
	 * @throws BusinessException
	 */
	String createEnvironment(String releaseUID, EnvironmentTypeEnum type, String ownerSsoId, String label) throws BusinessException;
	String createEnvironment(String releaseUID, EnvironmentTypeEnum type, String ownerSsoId, String label, List<String> configRoleUIDs) throws BusinessException;

	/**
	 * Find a specific environment with its identifier and return basic informations about it
	 * 
	 * @param uid
	 *            Environment uid returned by {@link #createEnvironment(String, DeploymentProfileEnum, String, String)}
	 * @return Basic informations about environment
	 * @throws EnvironmentNotFoundException TODO
	 */
	EnvironmentDto findEnvironmentByUID(String uid) throws EnvironmentNotFoundException;

	/**
	 * Find a specific environment with its identifier and return detailed operators informations about it
	 * 
	 * @param uid
	 *            Environment uid returned by {@link #createEnvironment(String, DeploymentProfileEnum, String, String)}
	 * @return Operators informations about environment
	 * @throws EnvironmentNotFoundException TODO
	 */
	EnvironmentOpsDetailsDto findEnvironmentOpsDetailsByUID(String uid) throws EnvironmentNotFoundException;

	/**
	 * Find a specific environment with its identifier and return details informations about it
	 * 
	 * @param environmentUID
	 *            Environment internal name returned by {@link #createEnvironment(String, DeploymentProfileEnum, String, String)}
	 * @return Basic details informations about environment
	 * @throws EnvironmentNotFoundException TODO
	 */
	EnvironmentDetailsDto findEnvironmentDetails(String environmentUID) throws EnvironmentNotFoundException;

	/**
	 * Find all registered environments for a given application release
	 * 
	 * @param releaseUID
	 *            The application release uid.
	 * @throws ApplicationReleaseNotFoundException
	 *             application release is not found
	 */
	List<EnvironmentDto> findEnvironmentsByAppRelease(String releaseUID) throws ApplicationReleaseNotFoundException;

	/**
	 * Find environments, starting at providing index, order by specific field
	 * 
	 * @param first
	 *            index of the first environment
	 * @return List of EnvironmentDto
	 */
	List<EnvironmentDto> findEnvironments();

	/**
	 * Find environments of application the connected user is a member of, starting at a specified index, order by specific field
	 * 
	 * @param firstIndex
	 *            index of the first application release to retrieve (included)
	 * @return a List of ApplicationRelease
	 */
	public List<EnvironmentDto> findMyEnvironments();
	
	/**
	 * Counts all environments
	 * 
	 * @return total number of environments
	 */
	public Long countEnvironments();
	
	/**
	 * Counts environments of private application the connected user is a member of
	 * 
	 * @return total number of environments
	 */
	public Long countMyEnvironments();

	/**
	 * Start environment. Note: currently {@link #createEnvironment(String, DeploymentProfileEnum, String, String)} performs the start.
	 * 
	 * @param environmentUID
	 *            Environment internal name returned by {@link #createEnvironment(String, DeploymentProfileEnum, String, String)}
	 * @throws EnvironmentNotFoundException TODO
	 */
	void startEnvironment(String environmentUID) throws EnvironmentNotFoundException;

	/**
	 * Stop environment.
	 * 
	 * @param environmentUID
	 *            Environment internal name returned by {@link #createEnvironment(String, DeploymentProfileEnum, String, String)}
	 * @throws EnvironmentNotFoundException TODO
	 */
	void stopEnvironment(String environmentUID) throws EnvironmentNotFoundException;

    /**
     * Delete environment.
     *
     * @param environmentUID
     *            Environment internal name returned by {@link #createEnvironment(String, DeploymentProfileEnum, String, String)}
     * @throws EnvironmentNotFoundException TODO
     */
    void deleteEnvironment(String environmentUID) throws EnvironmentNotFoundException;



    /**
     * force given status for an environment :
     *  - used by scalabilit RaZ
     *  (maybe will be used by OPS to recover an anvironment)
     *
     * @param envUid
     *            Environment internal name returned by createEnvironment(...)
     * @param newStatus
     *            New status
     * @throws EnvironmentNotFoundException TODO
     */
    void forceStatusForAndEnvironment(String envUid, EnvironmentStatus newStatus)  throws EnvironmentNotFoundException;


    List<Environment> findOldRemovedEnvironments();

    void purgeRemovedEnvironment(String envUid) throws EnvironmentNotFoundException;

    /**
	 * Counts the active (i.e. not REMOVED) environments for an application release
	 * 
	 * @param releaseUID
	 *            the application release we want related environments
	 * @return number of environments for the application release
	 */
	public Long countEnvironmentsByApplicationRelease(String releaseUID) throws ApplicationReleaseNotFoundException;

	/**
	 * test if application label is unique before trying to persist application
	 * 
	 * @param ssoid
	 *            the user identifier
	 * @param searchEnvironmentLabel
	 *            the search label in active environment list of the selected release
	 * @param releaseUID
	 *            the releaseUID to be able to get environments attached to this release
	 * 
	 * @return true if there is no application with the same label.
	 */
	boolean isEnvironmentLabelUniqueForRelease(String ssoid, String searchEnvironmentLabel, String releaseUID) throws BusinessException;

	/**
	 * Update environment, mainly used to add a comment on env
	 * 
	 * @param environmentDetailsDto
	 * @return
	 */
	Environment update(EnvironmentDetailsDto environmentDetailsDto);

}