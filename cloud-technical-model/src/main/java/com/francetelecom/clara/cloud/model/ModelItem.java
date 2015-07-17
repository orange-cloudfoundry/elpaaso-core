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
package com.francetelecom.clara.cloud.model;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Classe de base d'un modele de deploiement
 * 
 * @author APOG7416
 * 
 */
@XmlRootElement
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ModelItem extends Entite {

	/**
	 * serialUID
	 */
	private static final long serialVersionUID = 8117270323003982550L;

	@XmlAttribute
	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	private int id;

	@XmlAttribute
	@NotNull
	DeploymentStateEnum deploymentState = DeploymentStateEnum.TRANSIENT;

	/**
	 * Represents the real-life ID of this model item such as Inventory items.
	 * For instance, a IaaS-specific ID of a VM could be applied to the
	 * PlatformServer.
	 */
	private String technicalId;

	/**
	 * Provides traceability to the corresponding Entite.name in the logical
	 * model. This is used monitoring/supervision/logs to allow to correlate
	 * back and aggregate information from the technical model to the logical
	 * model abstraction.
	 * 
	 * This is traceability link does not always make sense and is sometimes
	 * null.
	 * 
	 * @see Entite#name
	 */
	// FIXME rename to logicalModelUUID
	private String logicalModelId;

	/**
	 * Empty constructor
	 */
	protected ModelItem() {

	}

	public ModelItem(String name) {
		this.name = name;
	}

	/**
	 * id is read-only
	 * 
	 * @return
	 */
	public int getId() {
		return id;
	}

	/**
	 * setName as final prevent overriding name attribute
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * getName as final prevent overriding name attribute
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public String getTechnicalId() {
		return technicalId;
	}

	/**
	 * 
	 * @param technicalId
	 */
	public void setTechnicalId(String technicalId) {
		this.technicalId = technicalId;
	}

	/**
	 * 
	 * @return
	 */
	public DeploymentStateEnum getDeploymentState() {
		return deploymentState;
	}

	/**
	 * 
	 * @param deploymentState
	 */
	public void setDeploymentState(DeploymentStateEnum deploymentState) {
		this.deploymentState = deploymentState;
	}

	public boolean isActivated() {
		return DeploymentStateEnum.STARTED.equals(deploymentState) || DeploymentStateEnum.STOPPED.equals(deploymentState)
				|| DeploymentStateEnum.CREATED.equals(deploymentState);
	}
	
	public boolean isTransient() {
		return DeploymentStateEnum.TRANSIENT.equals(deploymentState);
	}
	
	public boolean isRemoved() {
		return DeploymentStateEnum.REMOVED.equals(deploymentState);
	}


	public boolean isUnkwown() {
		return DeploymentStateEnum.UNKNOWN.equals(deploymentState);
	}

	/**
	 * Provides optional traceability from a technical element to an item in the
	 * logical model item. Override this method to precise to which element it
	 * is pointing to in the logical model, and if necessary semantic of this
	 * traceability relationship.
	 * 
	 * @return null or a Id that identify an element in the logical model (an
	 *         ExecutionNode or a LogicalService).
	 */
	public String getLogicalModelId() {
		return logicalModelId;
	}

	public void setLogicalModelId(String logicalModelId) {
		this.logicalModelId = logicalModelId;
	}

	public void failed() {
		setDeploymentState(DeploymentStateEnum.UNKNOWN);
	}
}
