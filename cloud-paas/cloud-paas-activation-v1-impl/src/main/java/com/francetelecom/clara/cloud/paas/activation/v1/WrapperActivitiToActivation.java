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

import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.paas.activation.v1.async.TaskHandler;

/**
 * Just a wrapper between activiti and activation module
 */
final public class WrapperActivitiToActivation {
	private static Logger logger = LoggerFactory.getLogger(WrapperActivitiToActivation.class.getName());

	private TaskHandler<ActivationTask, ActivationTaskHandlerCallback> taskHandler;

	/**
	 * Activiti will call this through ServiceTask in processus, it just wrap
	 * parameters into an ActivationTask and call handleRequest() of
	 * ActivationTaskHandlerCallback.
	 * 
	 * @param activationStep
	 * @param activityId
	 * @param entityId
	 * @param entityType
	 */
	public void execute(String processInstanceId, String activationStep, String activityId, int tdiId, int entityId, String entityType, int taskIndex, int taskCount) {
		execute(processInstanceId, activationStep, activityId, tdiId, entityId, entityType, null, taskIndex, taskCount);
	}

	public void execute(String processInstanceId, String activationStep, String activityId, int tdiId, int entityId, String entityType, String errMessage, int taskIndex, int taskCount) {
		logger.debug("wrapper.execute(" + activationStep + ", " + activityId + ", " + entityId + ", " + entityType + ", " + errMessage + ")");
		taskHandler.handleRequest(new ActivationTask(processInstanceId, activationStep, activityId, tdiId, entityId, entityType, errMessage, taskIndex, taskCount), activityId);
	}

	public void success(String processInstanceId, String activationStep, int tdiId) {
		logger.debug("wrapper.success(" + tdiId + ")");
		taskHandler.handleRequest(new ActivationTask(processInstanceId, activationStep, "successTask", tdiId, tdiId, TechnicalDeploymentInstance.class.getName(), ""), "successTask");
	}

	public void failed(String processInstanceId, String activationStep, int tdiId, String msgError) {
		logger.debug("wrapper.failed(" + tdiId + ", "+msgError+")");
		taskHandler.handleRequest(new ActivationTask(processInstanceId, activationStep, "failureTask", tdiId, tdiId, TechnicalDeploymentInstance.class.getName(), msgError), "failureTask");
	}

	public TaskHandler<ActivationTask, ActivationTaskHandlerCallback> getTaskHandler() {
		return taskHandler;
	}

	public void setTaskHandler(TaskHandler<ActivationTask, ActivationTaskHandlerCallback> taskHandler) {
		this.taskHandler = taskHandler;
	}
}
