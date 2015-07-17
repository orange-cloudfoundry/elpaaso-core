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
package com.francetelecom.clara.cloud.paas.activation.v1;

import java.io.Serializable;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.model.ModelItem;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;

/**
 * Represent a task to execute in activation (created from activiti ServiceTask)
 */
public class ActivationTask implements Serializable {

	private static final long serialVersionUID = -5480362458387499317L;

	private ActivationStepEnum activationStep;

	private String processInstanceId;
	
	private String activitiTaskId;
	
	private int tdiId;
	
	private int entityId;
	
	private Class<? extends ModelItem> entityClass;

	private String errMessage;
	
	private int taskIndex;
	
	private int taskCount;
	
	@SuppressWarnings("unused")
	private ActivationTask() {
		super();
	}
	
	public ActivationTask(String processInstanceId, String activationStep, String activitiTaskId, int tdiId, int entityId, String entityClassName) {
		this(processInstanceId, activationStep, activitiTaskId, tdiId, entityId, entityClassName, null);
	}
	
	public ActivationTask(String processInstanceId, String activationStep, String activitiTaskId, int tdiId, int entityId, String entityClassName, String errMessage) {
		this(processInstanceId, activationStep, activitiTaskId, tdiId, entityId, entityClassName, errMessage, -1, -1);
	}
	
	public ActivationTask(String processInstanceId, String activationStep, String activitiTaskId, int tdiId, int entityId, String entityClassName, String errMessage, int taskIndex, int taskCount) {
		super();
		this.errMessage = errMessage;
		this.activitiTaskId = activitiTaskId;
		this.tdiId = tdiId;
		this.entityId = entityId;
		this.taskIndex = taskIndex;
		this.taskCount = taskCount;
		this.processInstanceId = processInstanceId;
		
		try {
			this.entityClass = (Class<? extends ModelItem>) Class.forName(entityClassName);
			if (!(ModelItem.class.isAssignableFrom(this.entityClass))) {
				throw new TechnicalException("Class is not a ModelItem: "+entityClassName);
			}
		} catch (ClassNotFoundException e) {
			throw new TechnicalException("Class not found for entity: "+entityClassName);
		}
		
        if (ActivationStepEnum.INIT.getName().equals(activationStep)) {
            this.activationStep = ActivationStepEnum.INIT;
        }
        else if (ActivationStepEnum.ACTIVATE.getName().equals(activationStep)) {
            this.activationStep = ActivationStepEnum.ACTIVATE;
        }
		else if (ActivationStepEnum.FIRSTSTART.getName().equals(activationStep)) {
			this.activationStep = ActivationStepEnum.FIRSTSTART;
		}
		else if (ActivationStepEnum.START.getName().equals(activationStep)) {
			this.activationStep = ActivationStepEnum.START;
		}
		else if (ActivationStepEnum.STOP.getName().equals(activationStep)) {
			this.activationStep = ActivationStepEnum.STOP;
		}
		else if (ActivationStepEnum.DELETE.getName().equals(activationStep)) {
			this.activationStep = ActivationStepEnum.DELETE;
		}
		else {
			throw new TechnicalException("activationStep '"+activationStep+"' unknown");
		}
	}

	public ActivationStepEnum getActivationStep() {
		return activationStep;
	}

	public String getActivitiTaskId() {
		return activitiTaskId;
	}

	public int getTdiId() {
		return tdiId;
	}

	public int getEntityId() {
		return entityId;
	}

	public Class<? extends ModelItem> getEntityClass() {
		return entityClass;
	}

	public String getErrMessage() {
		return errMessage;
	}

	public void setErrMessage(String errMessage) {
		this.errMessage = errMessage;
	}

	public int getTaskIndex() {
		return taskIndex;
	}

	public void setTaskIndex(int taskIndex) {
		this.taskIndex = taskIndex;
	}

	public int getTaskCount() {
		return taskCount;
	}

	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
}
