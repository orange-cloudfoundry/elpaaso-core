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

package com.francetelecom.clara.cloud.services.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * An EnvironmentDeployment contains all useful information for deploying one or more artifacts on one or more application servers on the Paas
 * @author BEAL6226
 *
 */
public class EnvironmentDeploymentDto {

	/**
	 * Name of the environment, should match environment.name
	 */
    protected String environmentName;

    /**
	 * Name of the application, should match application.name
	 */
    protected String applicationName;
    
    /**
	 * Version of the application release, should match applicationRelease.releaseVersion
	 */
    protected String releaseVersion;
    
    /**
     * user's credentials
     */
    protected String username;
    protected String password;
    
    /**
     * List of all deployments to be deployed on the paas
     */
    protected List<DeploymentDto> deployments;


    /**
     * Create an environment deployment.
     * @param environmentName
     * @param applicationName
     * @param releaseVersion
     */
    public EnvironmentDeploymentDto(String applicationName, String releaseVersion, String environmentName) {
		this.applicationName = applicationName;
		this.releaseVersion = releaseVersion;
		this.environmentName = environmentName;
		this.deployments = new ArrayList<DeploymentDto>();
	}

	/**
     * Gets the value of the environmentName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEnvironmentName() {
        return environmentName;
    }

    /**
     * Sets the value of the environmentName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnvironmentName(String value) {
        this.environmentName = value;
    }

    /**
     * Gets the value of the applicationName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * Sets the value of the applicationName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setApplicationName(String value) {
        this.applicationName = value;
    }

    /**
     * Gets the value of the releaseVersion property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReleaseVersion() {
        return releaseVersion;
    }

    /**
     * Sets the value of the releaseVersion property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReleaseVersion(String value) {
        this.releaseVersion = value;
    }

    /**
     * Gets the value of the username property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the value of the username property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUsername(String value) {
        this.username = value;
    }

    /**
     * Gets the value of the password property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the value of the password property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassword(String value) {
        this.password = value;
    }

	/**
	 * List all deployments (read only)
	 * @return
	 */
    public List<DeploymentDto> listDeployments() {
		return Collections.unmodifiableList(deployments);
	}


}
