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
package com.francetelecom.clara.cloud.coremodel;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;


/**
 * @author YSBU7453
 * 
 */
public interface EnvironmentRepository extends JpaRepository<Environment, Integer> {

	/**
	 * 
	 * @return all Active {@link Environment}
	 */
	@Query("SELECT e FROM Environment e where e.status <> 'REMOVED' ORDER BY e.applicationRelease.application.label, e.applicationRelease.releaseVersion, e.label ASC")
	public List<Environment> findAllActive();

	/**
	 * 
	 * @return all Active {@link Environment} of {@link Application} a user is member of
	 * @param member
	 */
	@Query("SELECT e FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND m.value = ?1")
	List<Environment> findAllActiveByApplicationMember(String member);

	/**
	 * @return all Active {@link Environment} a {@link PaasUser} is owner of
	 */
	@Query("SELECT e FROM Environment e where e.paasUser = ?1")
	List<Environment> findAllByOwner(PaasUser owner);

	/**
	 * @return all Active {@link Environment} of a {@link ApplicationRelease}
	 */
	@Query("SELECT e FROM Environment e where e.applicationRelease.uid = ?1 and e.status <> 'REMOVED'")
	List<Environment> findAllActiveByApplicationReleaseUid(String releaseUid);

	/**
	 * @return number of Active {@link Environment}
	 */
	@Query("SELECT COUNT(e.id) FROM Environment e WHERE e.status <> 'REMOVED'")
	long countActive();

	/**
	 * @return number of {@link Environment} owned by a user
	 */
	@Query("SELECT count(e) FROM Environment e where e.status <> 'REMOVED' AND e.paasUser = ?1")
	long countActiveByOwner(PaasUser owner);

	/**
	 * 
	 * @return number of Active {@link Environment} of {@link Application} a user is member of
	 */
	@Query("SELECT count(e) FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND m.value = ?1")
	long countActiveByApplicationMember(String member);

	/**
	 * @return number of Active {@link Environment} of a {@link ApplicationRelease}
	 */
	@Query("SELECT count(e) FROM Environment e JOIN e.applicationRelease ar WHERE ar.uid = ?1 AND e.status <> 'REMOVED'")
	long countActiveByApplicationReleaseUid(String releaseUID);

	/**
	 * @return {@link Environment} of given {@link ApplicationRelease} with given label
	 */
	@Query("SELECT e FROM Environment e where e.applicationRelease.uid = ?1 and e.label = ?2 and e.status <> 'REMOVED'")
	Environment findByApplicationReleaseUIDAndLabel(String releaseUID, String label);

	/**
	 * @return {@link Environment} with given uid
	 */
	Environment findByUid(String uid);

	/**
	 * @return {@link Environment} for given tdi id
	 */
	Environment findByTechnicalDeploymentInstanceId(int tdiId);

	/**
	 * find environment with a creationDate older thant now - nbDay and with the
	 * "REMOVED" status
	 * 
	 * @param nbDay
	 *            nbDay to consider environment as older one
	 * @return list of older enviromnment with a REMOVED state
	 */
	@Query("SELECT e FROM Environment e WHERE e.status = 'REMOVED' AND e.deletionDate IS NOT NULL AND e.deletionDate < ?1 ORDER BY e.applicationRelease.application.label, e.applicationRelease.releaseVersion, e.label ASC")
	List<Environment> findRemovedOlderThanNDays(Date nbDay);

	/**
	 * @return all active environments of public application or of private
	 *         applications a user is a member of)
	 */
	@Query("SELECT e FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND (a.isPublic = true OR m.value = ?1)")
	List<Environment> findAllPublicOrPrivateByMember(String currentUser);

	/**
	 * @return all active environments of a public release or of a private
	 *         release a user is a member of)
	 */
	@Query("SELECT e FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.applicationRelease.uid = ?1 AND e.status <> 'REMOVED' AND (a.isPublic = true OR m.value = ?2)")
	List<Environment> findAllPublicOrPrivateByMemberAndByApplicationRelease(String releaseUID, String currentUser);

	/**
	 * 
	 * @return count of all active environments of public application or of
	 *         private applications a user is a member of)
	 */
	@Query("SELECT count(e) FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND (a.isPublic = true OR m.value = ?1)")
	long countPublicOrPrivateByMember(String member);

	/**
	 * 
	 * @return count of all active environments of a public release or of a
	 *         private release a user is a member of)
	 */
	@Query("SELECT count(e) FROM Environment e JOIN e.applicationRelease ar JOIN ar.application a JOIN a.members m where e.status <> 'REMOVED' AND ar.uid = ?1 AND (a.isPublic = true OR m.value = ?2)")
	long countAllPublicOrPrivateByMemberAndByApplicationRelease(String releaseUID, String member);

}
