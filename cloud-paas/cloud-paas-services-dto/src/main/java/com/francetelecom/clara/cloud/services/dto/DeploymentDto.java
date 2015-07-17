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

/**
 * A deployment contains all information for deploying one artifact on one or more application servers on the Paas
 * @author BEAL6226
 *
 */
public class DeploymentDto {

	/**
	 * Name of the execution node, should match logicalExecutionNode.name
	 */
    protected String executionNode;
    
    /**
     * the artifact to be deployed on the application server instances projected from logical execution node
     */
    protected DeployableMavenArtifactDto artifact;
    
    /**
     * The associated EnvironmentDeployment, containing application and application release information
     */
    private EnvironmentDeploymentDto environmentDeployment;

    
    public DeploymentDto(String executionNode, DeployableMavenArtifactDto artifact, EnvironmentDeploymentDto environmentDeployment) {
		super();
		this.executionNode = executionNode;
		this.artifact = artifact;
		this.environmentDeployment = environmentDeployment;
		environmentDeployment.deployments.add(this);
	}

	/**
     * Gets the value of the executionNode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getExecutionNode() {
        return executionNode;
    }

    /**
     * Sets the value of the executionNode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setExecutionNode(String value) {
        this.executionNode = value;
    }

    /**
     * Gets the value of the artifact property.
     * 
     * @return
     *     possible object is
     *     {@link DeployableMavenArtifactDto }
     *     
     */
    public DeployableMavenArtifactDto getArtifact() {
        return artifact;
    }

    /**
     * Sets the value of the artifact property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeployableMavenArtifactDto }
     *     
     */
    public void setArtifact(DeployableMavenArtifactDto value) {
        this.artifact = value;
    }

    /**
     * get associated environment
     * @return
     */
	public EnvironmentDeploymentDto getEnvironmentDeployment() {
		return environmentDeployment;
	}

}
