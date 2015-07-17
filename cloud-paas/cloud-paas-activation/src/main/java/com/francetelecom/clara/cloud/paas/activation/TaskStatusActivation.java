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
package com.francetelecom.clara.cloud.paas.activation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;

public class TaskStatusActivation extends TaskStatus {

	private static Logger logger = LoggerFactory.getLogger(TaskStatusActivation.class.getName());

	private static final long serialVersionUID = -2288271413746127333L;

	private int technicalDeploymentInstanceId = -1;

	public TaskStatusActivation() {
		super();
	}

	public TaskStatusActivation(long taskId) {
		super(taskId);
	}
	
	public TaskStatusActivation(TaskStatusActivation status) {
		// Copy
		super(status);
		this.technicalDeploymentInstanceId = status.technicalDeploymentInstanceId;
	}

	public int getTechnicalDeploymentInstanceId() {
		return technicalDeploymentInstanceId;
	}

	public void setTechnicalDeploymentInstanceId(int technicalDeploymentInstanceId) {
		this.technicalDeploymentInstanceId = technicalDeploymentInstanceId;
	}
	
	public static void displayTaskStatus(TaskStatusActivation status) {
		logger.debug("TDI Id : " + status.getTechnicalDeploymentInstanceId());
		displayTaskStatus(status, 0);
	}
}
