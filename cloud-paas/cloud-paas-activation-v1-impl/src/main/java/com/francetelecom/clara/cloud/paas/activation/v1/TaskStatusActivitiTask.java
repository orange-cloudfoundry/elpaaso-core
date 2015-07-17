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

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;

/**
 * TaskStatus used by ActivationTaskHandlerCallback
 */
public class TaskStatusActivitiTask extends TaskStatus {
	
	private static final long serialVersionUID = 7474496403761240203L;

	private static Logger logger = LoggerFactory.getLogger(TaskStatusActivitiTask.class.getName());

	private ActivationTask activationTask = null;
	
	private String executionId = null;
	
	private TaskStatus status = null;

	public TaskStatusActivitiTask() {
		super();
	}

	public TaskStatusActivitiTask(long taskId) {
		super(taskId);
	}
	
	public TaskStatusActivitiTask(TaskStatusActivitiTask status) {
		// Copy
		super(status);
		this.activationTask = status.getActivationTask();
		this.executionId = status.executionId;
		if (status.getStatus() != null) {
			setStatus(new TaskStatus(status.getStatus()));
		}
		else {
			this.status = null;
		}
	}
	
	public static void displayTaskStatus(TaskStatusActivitiTask status) {
		if (status.getActivationTask() != null) {
			logger.debug("ActivationTask: " 
					+ status.getActivationTask().getActivationStep() + ", " 
					+ status.getActivationTask().getActivitiTaskId() + ", " 
					+ status.getActivationTask().getEntityId() + ", " 
					+ status.getActivationTask().getEntityClass().getName() + ")");
		}
		displayTaskStatus(status, 0);
	}

	public String getExecutionId() {
		return executionId;
	}

	public void setExecutionId(String executionId) {
		this.executionId = executionId;
	}

	public ActivationTask getActivationTask() {
		return activationTask;
	}

	public void setActivationTask(ActivationTask activationTask) {
		this.activationTask = activationTask;
	}

	public TaskStatus getStatus() {
		return status;
	}

	/**
	 * Set task status and copy status info to 'this' status
	 * @param status Status of the task being executed
	 */
	public void setStatus(TaskStatus status) {
		this.status = status;
		setTitle(status.getTitle());
		setSubtitle(status.getSubtitle());
		setStartTime(status.getStartTime());
		setEndTime(status.getEndTime());
		setSuggestedTimeout(status.getSuggestedTimeout());
		setPercent(status.getPercent());
		setMaxPercent(status.getMaxPercent());
		setErrorMessage(status.getErrorMessage());
		setTaskStatus(status.getTaskStatus());
	}
}
