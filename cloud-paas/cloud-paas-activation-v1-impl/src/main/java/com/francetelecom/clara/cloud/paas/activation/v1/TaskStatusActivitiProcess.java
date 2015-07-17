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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.paas.activation.TaskStatusActivation;

/**
 * TaskStatus used by ManagePaasActivationActivitiImpl
 */
public class TaskStatusActivitiProcess extends TaskStatusActivation {
	
	private static final long serialVersionUID = 1378872733311480046L;

	private static Logger logger = LoggerFactory.getLogger(TaskStatusActivitiProcess.class.getName());

	private String processInstanceId = null;
	
	private DeploymentStateEnum finalState = DeploymentStateEnum.UNKNOWN;

	public TaskStatusActivitiProcess() {
		super();
	}

	public TaskStatusActivitiProcess(long taskId) {
		super(taskId);
	}
	
	public TaskStatusActivitiProcess(TaskStatusActivitiProcess status) {
		// Copy
		super(status);
		this.processInstanceId = status.processInstanceId;
		this.finalState = status.finalState;
	}
	
	public static void displayTaskStatus(TaskStatusActivitiProcess status) {
		logger.debug("Process Id : " + status.getProcessInstanceId());
		displayTaskStatus(status, 0);
	}

	public String getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(String processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public DeploymentStateEnum getFinalState() {
		return finalState;
	}

	public void setFinalState(DeploymentStateEnum finalState) {
		this.finalState = finalState;
	}
}
