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
package com.francetelecom.clara.cloud.core.domain;

import com.francetelecom.clara.cloud.EnvironmentDescriptionHandler;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.dao.GenericDaoJpa;

import java.util.List;


/**
 * @author YSBU7453
 * 
 */
public interface EnvironmentRepository extends GenericDaoJpa<Environment, Integer> {

	/**
	 * 
	 * @return all Active {@link Environment}
	 */
	public List<Environment> findAllActive();

	/**
	 * 
	 * @return all Active {@link Environment}
	 */
	public List<Environment> findAllActive(int first, int count);


	/**
	 * @return all Active {@link Environment}
	 */
	public List<Environment> findAllActive(int first, int count, String sortProperty, String sortType);

	/**
	 * 
	 * @return all Active {@link Environment} of {@link Application} a user is member of
	 */
	public List<Environment> findAllActiveByApplicationMember(SSOId member, int first, int count, String sortProperty, String sortType);

	/**
	 * @return all Active {@link Environment} a {@link PaasUser} is owner of
	 */
	public List<Environment> findAllByOwner(PaasUser owner);

	/**
	 * @return all Active {@link Environment} of a {@link ApplicationRelease}
	 */
	public List<Environment> findAllActiveByApplicationReleaseUid(String releaseUid, int first, int count, String sortProperty, String sortType);

	/**
	 * @return all Active {@link Environment} of a {@link ApplicationRelease} a user is member of
	 */
	public List<Environment> findAllByApplicationReleaseUidAndApplicationMember(String releaseUID, SSOId member, int first, int count,
			String sortProperty, String sortType);

	/**
	 * @return number of {@link Environment}
	 */
	public Long count();

	/** 
	 * @return number of Active {@link Environment}
	 */
	public Long countActive();

	/**
	 * @return number of {@link Environment} owned by a user
	 */
	public Long countActiveByOwner(PaasUser owner);

	/**
	 * 
	 * @return number of Active {@link Environment} of {@link Application} a user is member of
	 */
	public Long countActiveByApplicationMember(SSOId member);

	/**
	 * @return number of Active {@link Environment} of a {@link ApplicationRelease}
	 */
	public long countActiveByApplicationReleaseUid(String releaseUID);

	/**
	 * Update the Environment state associated with the TDI
	 * 
	 * @param tdiId
	 *            Identifier of the TechnicalDeploymentInstance
	 * @param status
	 *            The new state of the environment
	 * @param message
	 *            For error state you can add a message to explain the error
	 * @param percent
	 *            Percentage of progress, -1 if unknown
	 * @return true if the Environment associated with the TDI has been found
	 *         and state has been updated
	 */
	boolean updateEnvironmentStateByTDI(int tdiId, EnvironmentStatus status, String message, int percent);

	/**
	 * @return {@link Environment} of given {@link ApplicationRelease} with given label
	 */
	public Environment findByApplicationReleaseUIDAndLabel(String releaseUID, String label);

	/**
	 * @return {@link Environment} with given uid
	 */
	public Environment findByUID(String uid);

	/**
	 * @return {@link Environment} for given tdi id
	 */
	public Environment findByTDIId(int tdiId);

	/**
	 * Returns a string that identify environment: it's dedicated to Xaas to tag
	 * resources
	 * 
	 * 
	 * @param tdiId
	 *            ID of the TDI
	 * @param environmentDescriptionHandler
	 * @return a string that identify environment
	 */
    public ActivationContext getActivationContext(int tdiId, EnvironmentDescriptionHandler environmentDescriptionHandler);

	/**
	 * find environment with a creationDate older thant now - nbDay and with the
	 * "REMOVED" status
	 * 
	 * @param nbDay
	 *            nbDay to consider environment as older one
	 * @return list of older enviromnment with a REMOVED state
	 */
	public List<Environment> findRemovedOlderThanNDays(int nbDay);

	/**
	 * physicaly remove given environments nb : only REMOVED environment (status
	 * check) will be purged
	 * 
	 * @param environmentsToPurge
	 *            list of environments to purge
	 * @return purged environments count
	 */
	int purgeEnvironments(List<Environment> environmentsToPurge);

	int purgeEnvironment(Environment environment);

	boolean forceRemovedStatusForAndEnvironment(String uid, EnvironmentStatus newStatus);

	/**
	 * @return all active environments of public application or of private
	 *         applications a user is a member of)
	 */
	public List<Environment> findAllPublicOrPrivateByMember(SSOId currentUser, int first, int count, String sortProperty, String sortType);

	/**
	 * @return all active environments of a public release or of a private
	 *         release a user is a member of)
	 */
	public List<Environment> findAllPublicOrPrivateByMemberAndByApplicationRelease(String releaseUID, SSOId currentUser, int first, int count,
			String sortProperty, String sortType);

	/**
	 * 
	 * @return count of all active environments of public application or of
	 *         private applications a user is a member of)
	 */
	public Long countPublicOrPrivateByMember(SSOId member);

	/**
	 * 
	 * @return count of all active environments of a public release or of a
	 *         private release a user is a member of)
	 */
	public Long countAllPublicOrPrivateByMemberAndByApplicationRelease(String releaseUID, SSOId member);

}
