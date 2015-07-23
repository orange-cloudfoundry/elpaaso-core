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

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.PaasUserNotFoundException;

import java.net.URL;
import java.util.List;

/**
 *  Application release management service facade interface.
 */
public interface ManageApplicationRelease {

	/**
	 * Find an application release from its uid.
	 * 
	 * @param uid
	 *            application release uid
	 * @throws ApplicationReleaseNotFoundException
	 *             when application release does not exist
	 */
	public ApplicationRelease findApplicationReleaseByUID(String uid) throws ApplicationReleaseNotFoundException;

	/**
	 * Create an application release.
	 * 
	 * @param applicationUID
	 *            application uid
	 * @param ssoId
	 *            paas user ssoId
	 * @param version
	 *            release version
	 * @return application release uid
	 * @throws PaasUserNotFoundException
	 *             when pass user does not exist
	 * @throws ApplicationNotFoundException
	 *             when application does not exist
	 * @throws DuplicateApplicationReleaseException
	 *             when application release already exists
	 */
	public String createApplicationRelease(String applicationUID, String ssoId, String version) throws PaasUserNotFoundException, ApplicationNotFoundException,
			DuplicateApplicationReleaseException;

	/**
	 * Create an application release.
	 * 
	 * @param applicationUID
	 *            application uid
	 * @param ssoId
	 *            paas user ssoId
	 * @param version
	 *            release version
	 * @param description 
	 * 			Description of the release
	 * @param versionControleUrl
	 * @param middlewareProfil
	 *            The middleware profil to use for the release. See
	 *            {@link #findAllMiddlewareProfil()} for a list of available
	 *            profil. Default is taken if set to null. No verification except
	 *            nullity is done, a not existing middleware profile version
	 *            could be provided. It will not fail at release creation.
	 * @return application release uid
	 * @throws PaasUserNotFoundException
	 *             when pass user does not exist
	 * @throws ApplicationNotFoundException
	 *             when application does not exist
	 * @throws DuplicateApplicationReleaseException
	 *             when application release already exists
	 */
	public String createApplicationRelease(String applicationUID, String ssoId, String version, String description, URL versionControleUrl, String middlewareProfil) throws PaasUserNotFoundException,
			ApplicationNotFoundException, DuplicateApplicationReleaseException;

	/**
	 * Update an application release.
	 * 
	 * @param applicationRelease
	 *            application release to be updated
	 * @throws ApplicationReleaseNotFoundException
	 *             when application release to be updated does not exist
	 * @return ApplicationRelease
	 */
	public ApplicationRelease updateApplicationRelease(ApplicationRelease applicationRelease) throws ApplicationReleaseNotFoundException;

	/**
	 * Delete an application release.
	 * 
	 * @param applicationReleaseUID
	 *            application release uid to be deleted
	 * @throws ApplicationReleaseNotFoundException
	 *             when application release to be deleted does not exist
	 */
	public void deleteApplicationRelease(String applicationReleaseUID) throws ApplicationReleaseNotFoundException;

    /**
     * Purge old removed releases.
     * NB/ release with environment (event with REMOVED status) are never purged
     */
    void purgeOldRemovedReleases();

    /**
     * remove an application release
     * @param uid application release uid
     * @throws ApplicationReleaseNotFoundException
     */
    void deleteAndPurgeApplicationRelease(String uid) throws ApplicationReleaseNotFoundException;

	/**
	 * Indicates whether application release can be deleted or not, e.g. are
	 * there one or more environment that is not removed?
	 * 
	 * @param applicationReleaseUID
	 *            application release uid
	 * @return true if application release can be deleted
	 */
	public boolean canBeDeleted(String applicationReleaseUID) throws ApplicationReleaseNotFoundException;

	
	/**
	 * Find all releases of private application the connected user is a member of.
	 * 
	 * @return list of application release
	 */
	public List<ApplicationRelease> findMyApplicationReleases();

	/**
	 * Find application releases from a specified application
	 * 
	 * @param applicationUid
	 *            application uid
	 * @return list of ApplicationRelease
	 * @throws ApplicationNotFoundException
	 *             if application does not exist
	 */
	public List<ApplicationRelease> findApplicationReleasesByAppUID(String applicationUid) throws ApplicationNotFoundException;

	/**
	 * Find all releases of private and public application the connected user is a member of, starting at a specified index
	 * 
	 * @param firstIndex
	 *            index of the first application release to retrieve (included)
	 * @param count
	 *            number of application releases to retrieve
	 * @return a List of ApplicationRelease
	 */
	public List<ApplicationRelease> findApplicationReleases(int firstIndex, int count);

	/**
	 * @return releases count of public and private application the connected user is a member of
	 */
	public long countApplicationReleases();

	/**
	 * @return releases count of private application the connected user is a member of
	 */
	public long countMyApplicationReleases();

	/**
	 * Counts the application releases for a specific application
	 * 
	 * @param applicationUID
	 *            the application UID related to the releases we want to count
	 * @return the number of releases of the application
	 * @throws ApplicationNotFoundException
	 *             when application does not exist
	 */
	public long countApplicationReleasesByAppUID(String applicationUID) throws ApplicationNotFoundException;

	/**
	 * Find application release by application uid and version.
	 * 
	 * @param applicationUID
	 *            application uid
	 * @param releaseVersion
	 *            release version
	 * @return application release
	 * @throws ApplicationReleaseNotFoundException
	 *             when application release does not exist
	 */
	public ApplicationRelease findApplicationReleaseByApplicationAndReleaseVersion(String applicationUID, String releaseVersion)
			throws ApplicationReleaseNotFoundException;

	/**
	 * Is release version unique for given application ?
	 * 
	 * @param applicationUID
	 *            application uid
	 * @param version
	 *            release version
	 * 
	 * @return true if version is unique for application.
	 */
	public boolean isReleaseVersionUniqueForApplication(String applicationUID, String version);

	/**
	 * Return all available middleware profil
	 * 
	 * @return all available middleware profil
	 */
	public List<MiddlewareProfile> findAllMiddlewareProfil();

}
