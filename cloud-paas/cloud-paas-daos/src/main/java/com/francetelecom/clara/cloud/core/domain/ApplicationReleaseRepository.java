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

import java.util.Collection;
import java.util.List;

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.dao.GenericDaoJpa;

/**
 * DAO interface for ApplicationRelease Entity management
 */
public interface ApplicationReleaseRepository extends GenericDaoJpa<ApplicationRelease, Integer> {

	/**
	 * Retrieve an (not removed) application release from its uid.
	 * 
	 * @param uid
	 *            application release uid
	 * @return an application release or null if no uid is matching.
	 */
	ApplicationRelease findByUID(String uid);

	/**
	 * Retrieve an application release from its uid.
	 * 
	 * @param uid
	 *            application release uid
	 * @param evenRemovedOne
	 *            search into removed application released too or not
	 * @return an application release or null if no uid is matching.
	 */
	ApplicationRelease findByUID(String uid, boolean evenRemovedOne);


	/**
	 * Counts the application releases for a specific application
	 * 
	 * @param applicationUID
	 *            the application UID related to the releases we want to count
	 * @return the number of releases of the application
	 */
	public long countApplicationReleasesByApplicationUID(String applicationUID);

	/**
	 * find all application releases
	 * 
	 * @param first
	 *            index of the first application release to retrieve (included)
	 * @param count
	 *            count number of application releases to retrieve
	 * 
	 * @return a Collection of ApplicationRelease
	 */

	public List<ApplicationRelease> findAll(int firstIndex, int count);

	/**
	 * Counts all application releases
	 * 
	 * @return the total number of application releases
	 */
	public long countApplicationReleases();

	/**
	 * find all application releases of active and private applications a user
	 * is a member of
	 * 
	 * @param ssoId
	 *            ssoid of application member
	 * @param first
	 *            index of the first application release to retrieve (included)
	 * @param count
	 *            count number of application releases to retrieve
	 * 
	 * @return a List of ApplicationRelease
	 */
	public Collection<ApplicationRelease> findAllByApplicationMember(SSOId ssoId, int first, int count);

	/**
	 * count all application releases of active and private applications a user
	 * is a member of
	 * 
	 * @param ssoId
	 *            ssoid of application member
	 * 
	 * @return a count of ApplicationRelease
	 */
	public long countByApplicationMember(SSOId currentUser);

	/**
	 * find all application releases of active and ( public application or
	 * private applications a user is a member of)
	 * 
	 * @param ssoId
	 *            ssoid of application member
	 * @param first
	 *            index of the first application release to retrieve (included)
	 * @param count
	 *            count number of application releases to retrieve
	 * 
	 * @return a List of ApplicationRelease
	 */
	public List<ApplicationRelease> findAllPublicOrPrivateByMember(SSOId ssoId, int first, int count);

	/**
	 * count application releases of active and ( public application or
	 * private applications a user is a member of)
	 * 
	 * @param ssoId
	 *            ssoid of application member
	 * 
	 * @return a count of ApplicationRelease
	 */
	public long countPublicOrPrivateByMember(SSOId currentUser);
	
	/**
	 * count application releases of the given application if this application is active and public or
	 * private and the given user is a member of
	 * 
	 * @param ssoId
	 *            ssoid of application member
	 * @param applicationUID
	 *            UID of the application 
	 * 
	 * @return a count of ApplicationRelease
	 */
	long countPublicOrPrivateByMemberAndByAppUID(SSOId currentUser, String applicationUID);

	Collection<ApplicationRelease> findApplicationReleasesByAppUID(String appUid);

	Collection<ApplicationRelease> findApplicationReleasesByAppUID(String appUid, int first, int count);

	/**
	 * Retrieve all application where there is QRS at least one
	 * 
	 * @return list of application name
	 */
	List<String> findApplicationHavingQrs();

	/**
	 * Retrieve all application versions from a specific application
	 * 
	 * @param appUID
	 *            application UID
	 * @return list of release
	 */
	List<String> findApplicationVersion(String appUID);

	/**
	 * Find application release by application and version
	 * 
	 * @param applicationUID
	 *            application uid
	 * @param releaseVersion
	 *            release version
	 * @return application release
	 */
	ApplicationRelease findByApplicationUIDAndReleaseVersion(String applicationUID, String releaseVersion);

	/**
	 * Retrieve all service QueueReceiveService from a specific application
	 * release
	 * 
	 * @param applicationName
	 *            name
	 * @param releaseVersion
	 *            version
	 * @return list of service name of Queue Receive Service
	 */
	List<String> findQRSServiceName(String applicationName, String releaseVersion);

	/**
	 * Retrieve all service versions from a specific service QRS
	 * 
	 * @param applicationName
	 *            name
	 * @param releaseVersion
	 *            version
	 * @param serviceName
	 *            service name
	 * @return list of service versions of Queue Receive Service
	 */
	List<String> findQRSServiceVersion(String applicationName, String releaseVersion, String serviceName);

	/**
	 * find application releases with the "REMOVED" status and without
	 * associated environment
	 * 
	 * @return list of applications releases that match
	 */
	List<ApplicationRelease> findRemovedReleasesWithoutEnvironment();
	
	/**
	 * find all application releases of the given application if this application is active and public or
	 * private and the given user is a member of
	 * 
	 * @param ssoId
	 *            ssoid of application member
	 * @param applicationUID
	 *            UID of the application 
	 * @param first
	 *            index of the first application release to retrieve (included)
	 * @param count
	 *            count number of application releases to retrieve
	 * 
	 * @return a List of ApplicationRelease
	 */
	List<ApplicationRelease> findPublicOrPrivateByMemberAndByAppUID(SSOId currentUser, String applicationUID, int firstIndex, int count);

}
