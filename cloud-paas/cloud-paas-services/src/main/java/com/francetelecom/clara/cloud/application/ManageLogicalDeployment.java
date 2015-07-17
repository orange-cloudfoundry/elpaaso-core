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
package com.francetelecom.clara.cloud.application;

import java.net.URL;
import java.util.List;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.technicalservice.exception.InvalidReleaseException;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;

/**
 * Logical deployment management service facade interface.
 * 
 * Last updated : $LastChangedDate: 2012-06-01 11:41:38 +0200 (ven., 01 juin
 * 2012) $ Last author : $Author$
 * 
 * @author Clara
 * @version : $Revision$
 */
public interface ManageLogicalDeployment {

	/**
	 * Utility method to check the consistency of the specified
	 * LogicalSoapService This method use iosw consumer to create a temporary
	 * fake project and a service to get iosw validation feedbacks
	 * 
	 * @param logSoapService
	 * @param fullValidation
	 * @throws BusinessException
	 *             when there is a condition which prevents this
	 *             LogicalSoapService from being valid to request a new
	 *             environment creation.
	 */
	void checkLogicalSoapServiceConsistency(LogicalSoapService logSoapService, boolean fullValidation) throws BusinessException;

	/**
	 * Utility method to check the consistency of the specified
	 * LogicalDeployment. This methods triggers maven references resolution but
	 * does not update the model
	 * 
	 * @throws BusinessException
	 *             when there is a condition which prevents this
	 *             LogicalDeployment from being valid to request a new
	 *             environment creation.
	 */
	void checkOverallConsistency(LogicalDeployment logicalDeployment) throws BusinessException;

	/**
	 * Check that the logical model is consistent and update logical model if
	 * needed. This methods triggers maven references resolution; access urls
	 * are updated in the model if all checks success
	 * 
	 * @param logicalDeployment
	 * @throws BusinessException
	 *             , the exact exception is LogicalModelNotConsistentException
	 *             which wraps all inconsistency error exceptions raised during
	 *             check
	 */

	LogicalDeployment checkOverallConsistencyAndUpdateLogicalDeployment(LogicalDeployment logicalDeployment) throws BusinessException;

	/**
	 * Update a logical deployment.
	 * 
	 * @param logicalDeployment
	 *            a logical deployment to update
	 * 
	 * @return a logical deployment
	 * 
	 * @throws ObjectNotFoundException
	 *             if logicalDeployment does not exist FIXME: should break if
	 *             locked ?
	 * 
	 */
	LogicalDeployment updateLogicalDeployment(LogicalDeployment logicalDeployment) throws ObjectNotFoundException, InvalidMavenReferenceException;

	/**
	 * Retrieve a logical deployment that is eager fetched
	 * 
	 * TODO: precise the depth of objects fetched
	 * 
	 * @param logicalDeploymentId
	 *            id of logical deployment to be consulted as returned by
	 *            {@link com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment#getId()}
	 * @return a logical deployment detached object.
	 * 
	 * @throws ObjectNotFoundException
	 *             if logical deployment does not exist
	 */
	LogicalDeployment findLogicalDeployment(int logicalDeploymentId) throws ObjectNotFoundException;

	/**
	 * 
	 * @return maven url for a node
	 * @deprecated the
	 *             {@link #updateLogicalDeployment(com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment)}
	 *             method now performs the MavenReference url resolution for all
	 *             items in the logical model
	 */
	@Deprecated
	public void resolveMavenURL(ProcessingNode node);

	/**
	 * List of application name that are able to receive messages (i.e. that
	 * subscribed to QRS service.
	 * 
	 * @param domain
	 *            Momaas's Domain Domain where the user search the application
	 * 
	 * @return List of application name
	 * @throws ObjectNotFoundException
	 *             if domain not exist
	 */
	public List<String> getQrsApplications(String domain) throws ObjectNotFoundException;

	/**
	 * List of application version that are able to receive messages (i.e. that
	 * subscribed to QRS service.
	 * 
	 * @param domain
	 *            Momaas's Domain Domain where the user search the application
	 * @param appName
	 *            Application Name
	 * @return List of application version
	 * @throws ObjectNotFoundException
	 *             if domain or Application not exist
	 */
	public List<String> getQrsApplicationVersions(String domain, String appName) throws ObjectNotFoundException;

	/**
	 * List of service name that are able to receive messages (i.e. that
	 * subscribed to QRS service.
	 * 
	 * @param domain
	 *            the Momaas's Domain Domain where the user search the
	 *            application
	 * @return List of service name
	 * 
	 * @throws ObjectNotFoundException
	 *             if domain or Application or ApplicationVersion not exist
	 * 
	 */
	public List<String> getQrsServices(String domain, String appName, String appVersion) throws ObjectNotFoundException;

	/**
	 * List of service version that are able to receive messages (i.e. that
	 * subscribed to QRS service.
	 * 
	 * @param domain
	 *            Momaas's Domain Domain where the user search the application
	 * @return List of service version
	 * @throws ObjectNotFoundException
	 *             if domain or Application or ApplicationVersion or service not
	 *             exist
	 * 
	 */
	public List<String> getQrsServicesVersions(String domain, String appName, String appVersion, String serviceName) throws ObjectNotFoundException;

	/**
	 * Clone a Logical deployment from a source application release into a
	 * target application release
	 * 
	 * @param applicationReleaseSourceInternalName
	 *            The internal name of the source application release to use the
	 *            logical deployment of. As returned by
	 *            {@link com.francetelecom.clara.cloud.coremodel.ApplicationRelease#getUID()}
	 * @param applicationReleaseTargetInternalName
	 *            The internal name of the target application release to which
	 *            the the logical deployment content should be replaced. As
	 *            returned by
	 *            {@link com.francetelecom.clara.cloud.coremodel.ApplicationRelease#getUID()}
	 * @throws InvalidReleaseException
	 *             if the source or target application release names are invalid
	 * @throws InvalidReleaseException
	 *             if the target application release in a locked state
	 * @throws InvalidReleaseException
	 *             if the target application has a already a clone while the
	 *             source is from a distinct app (ok to fork within same app but
	 *             not across apps)
	 */
	public void cloneLogicalDeployment(String applicationReleaseSourceInternalName, String applicationReleaseTargetInternalName) throws InvalidReleaseException;

	/**
	 * check if a MavenReference can be resolved
	 * 
	 * @param mavenReference
	 * @return artifact access URL
	 * @throws InvalidMavenReferenceException
	 *             if maven reference is invalid; typically if the access url
	 *             can not be resolved
	 */
	public URL checkMavenReference(MavenReference mavenReference) throws InvalidMavenReferenceException;
}
