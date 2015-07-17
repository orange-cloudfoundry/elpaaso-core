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

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.dao.GenericDaoJpa;

import java.util.List;

/**
 * DAO interface for Application Entity management
 * 
 * @author Clara
 */

public interface ApplicationRepository extends GenericDaoJpa<Application, Integer> {

	/**
	 * Finds application by UID. UID is unique.
	 *
	 * @param uid
	 *            application UID
	 * @return an application
	 */
	Application findByUid(String uid);

    /**
     * Retrieve an application by UID. UID is unique.
     *
     * @param uid
     *            application uid
     * @param evenRemovedOne
     *            search into removed application too or not
     * @return an application or null if no uid is matching.
     */
    Application findByUid(String uid, boolean evenRemovedOne);

	/**
	 * Finds application by its label. Label is unique.
	 * 
	 * @param label
	 *            label corresponding to
	 *            {@link com.francetelecom.clara.cloud.coremodel.Application#getLabel()}
	 * 
	 * @return Application if found, null otherwise.
	 */
	Application findByLabel(String label);

	/**
	 * Finds application by its code. Code is unique.
	 * 
	 * @param code
	 *            code corresponding to
	 *            {@link com.francetelecom.clara.cloud.coremodel.Application#getCode()}
	 * 
	 * @return Application if found, null otherwise.
	 */
	Application findByCode(String code);


	/**
	 * Retrieve all applications accessible.
	 * 
	 * @return
	 */
	List<Application> findAll();

	/**
	 *  Returns list of {@link Application} that are public or that have given user
	 *  as a member
	 * @param ssoId 
	 * 
	 * @return list of {@link Application} if found, empty list otherwise.
	 */
	public List<Application> findAllPublicOrPrivateByMember(SSOId ssoId);

	/**
	 * Retrieve a certain number of applications, ordered for all users
	 * 
	 * @param first
	 *            first index
	 * @param count
	 *            maximum number of returned Application
	 * @param sortProperty
	 *            field used for the sort
	 * @param sortType
	 *            ASC or DESC
	 * @return Applications
	 */
	List<Application> findAll(int first, int count, String sortProperty, String sortType);

	
	/**
	 * Returns list of {@link Application} that have given user as a member
	 * 
	 * @param first
	 *            first index
	 * @param count
	 *            maximum number of returned Application
	 * @param sortProperty
	 *            field used for the sort
	 * @param sortType
	 *            ASC or DESC
	 * @return list of {@link Application} if found, empty list otherwise.
	 */
	public List<Application> findAllByMember(SSOId ssoId, int first, int count, String sortProperty, String sortType);

	/**
	 * Counts the applications
	 * 
	 * @return total number of applications
	 */
	long count();

	/**
	 * returns number of active {@link Application} given user is a member of
	 * 
	 * @return
	 */
	public long countByMember(SSOId member);

	/**
     * find application with the "REMOVED" status and without associated application release
     * @return list of applications that match
     */
    List<Application> findRemovedApplicationWithoutRelease();

    /**
     * purge all given application  except not removed application.
     * @param applications applications to remove
     * @return
     */
    int purgeApplications(List<Application> applications);

    int purgeApplication(Application application);


    
}
