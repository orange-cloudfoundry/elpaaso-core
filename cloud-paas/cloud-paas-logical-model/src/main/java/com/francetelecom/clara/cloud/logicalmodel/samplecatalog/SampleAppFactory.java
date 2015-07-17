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
package com.francetelecom.clara.cloud.logicalmodel.samplecatalog;

import java.net.URL;
import java.util.Map;

import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;

/**
 * The interface for instanciating a sample application from the "logical model catalog". This aims at being used by
 * the UI for providing default sample apps, and by some paas-service-int tests.
 */
public interface SampleAppFactory {

    /**
     * Returns the label of the application that should be created
     * @return a name to use or null to use a default name (For instance, clients may choose AppXXX where XXX is an an automatically generated sequence number)
     */
    String getAppLabel();

    /**
     * @return a app code (e.G. basicat in FR) to use or null to use a default code (For instance, clients may choose XXX as the first 3 app letters)
     */
    String getAppCode();

    /**
     * @return a description to use or null to use a default description ("Sample app:" + ({@link #getAppLabel})
     */
    String getAppDescription();

    /**
     * @return an Url to use or null to use a default Url (empty "")
     */
    String getApplicationVersionControl();

    /**
     * @return an Url to use or null to use a default Url (default to {@link #getApplicationVersionControl()})
     */
    String getApplicationReleaseVersionControl();

    /**
     * @return a description to use or null to use a default description (defauts to {@link #getAppDescription()})
     */
    String getAppReleaseDescription();

    /**
     * @return an version to use or null to use a default version (XXX is an an automatically generated sequence number)
     */
    String getAppReleaseVersion();

    /**
     * Creates the sample logical model corresponding to this application
     *
     * <ul>
     * <li>Note: the maxRequestSession is not in this contract and would be in a subclass for all catalog expecting SLOs
     * <li>Note: the logicalDeploymentNmae is not provided as it is supposed to be provided in ApplicationRelease
     * </ul>
     * @param logicalDeployment the logical deployment to update or null to create a new one.
     * @return The logical deployment that was filled with logical services.
     */
    LogicalDeployment populateLogicalDeployment(LogicalDeployment logicalDeployment);

    /**
     * Defines the list of Urls and Keywords that the sample app expose and that tests should assert against.
     * @param baseUrl
     * @return
     */
    Map<String, String> getAppUrlsAndKeywords(URL baseUrl);

	/**
	 * Used to know whether the sample application is instantiable. If not, it
	 * won't be visible in the populate service of the portal, for example.
	 * 
	 * @return true if the application can be instantiated
	 */
	boolean isInstantiable();

	/**
	 * Used to know whether the sample application is supported. If not, this
	 * application should not be tested by the wicket tests, as the
	 * corresponding web panels are not available.
	 * 
	 * @return true if the application is supported by the web GUI
	 */
	boolean isSupported();
	
	/**
	 * @return Middleware profile version to use for this test or null to use default stable one
	 */
	String getAppReleaseMiddlewareProfile();

}
