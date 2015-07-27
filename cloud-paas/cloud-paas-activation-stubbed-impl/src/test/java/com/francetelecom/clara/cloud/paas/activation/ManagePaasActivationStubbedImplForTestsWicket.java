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

import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.EnvironmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock Implementation of {@link com.francetelecom.clara.cloud.paas.activation.ManagePaasActivation}
 */
public class ManagePaasActivationStubbedImplForTestsWicket implements ManagePaasActivation {

	private static Logger logger = LoggerFactory.getLogger(ManagePaasActivationStubbedImplForTestsWicket.class.getName());

	/**
	 * Private IOC attributes. Get a Task executor for async treatment with IOC
	 * (workwanager on was)
	 */
	private TaskExecutor springTaskCoreExecutor;

	@Autowired
	private EnvironmentRepository environmentRepository;
	
	private Map<Long, TaskStatusActivation> taskStatus = new HashMap<Long, TaskStatusActivation>();

    @Autowired
    private com.francetelecom.clara.cloud.paas.activation.ActivationStubbedSideEffectsUtils activationStubbedSideEffectsUtils;

    /**
     * Utility method to factor out implementations of activate(tdi) and activate(td)
     */
    public TaskStatusActivation activate(final int tdiId) {
        logger.info("Starting TechnicalDeploymentInstance#" + tdiId + " activation");

        // Prepare TaskStatus
        final TaskStatusActivation statusActivate = new TaskStatusActivation(100);
        taskStatus.put(statusActivate.getTaskId(), statusActivate);
        statusActivate.setTitle("Activating virtual appliance TechnicalDeploymentInstance#" + tdiId);
        statusActivate.setStartTime(System.currentTimeMillis());
        statusActivate.setTaskStatus(TaskStatusEnum.STARTED);

        final TaskStatusActivation statusIaas = new TaskStatusActivation(101);
        statusIaas.setTitle("Creating virtual appliance TechnicalDeploymentInstance#" + tdiId);
        statusIaas.setTaskStatus(TaskStatusEnum.TRANSIENT);
        statusIaas.setMaxPercent(80);
        statusActivate.addSubtask(statusIaas);

        final TaskStatusActivation statusConfiguration = new TaskStatusActivation(102);
        statusConfiguration.setTitle("Post configuring virtual appliance TechnicalDeploymentInstance#" + tdiId);
        statusConfiguration.setTaskStatus(TaskStatusEnum.TRANSIENT);
        statusConfiguration.setMaxPercent(10);
        statusActivate.addSubtask(statusConfiguration);

        final TaskStatusActivation statusPowerOn = new TaskStatusActivation(103);
        statusPowerOn.setTitle("Powering ON virtual appliance TechnicalDeploymentInstance#" + tdiId);
        statusPowerOn.setTaskStatus(TaskStatusEnum.TRANSIENT);
        statusPowerOn.setMaxPercent(10);
        statusActivate.addSubtask(statusPowerOn);


        try {
            // Create the vApp

            statusActivate.setTechnicalDeploymentInstanceId(tdiId);
            statusIaas.setPercent(50);
            statusIaas.setPercent(100);
            statusIaas.setTaskStatus(TaskStatusEnum.FINISHED_OK);

            // Do post configuration
            statusConfiguration.setTaskStatus(TaskStatusEnum.STARTED);
            statusConfiguration.setPercent(20);
            statusConfiguration.setPercent(100);
            statusConfiguration.setTaskStatus(TaskStatusEnum.FINISHED_OK);

            // Power on
            statusPowerOn.setTaskStatus(TaskStatusEnum.STARTED);
            statusPowerOn.setPercent(30);
            statusPowerOn.setPercent(100);
            statusPowerOn.setTaskStatus(TaskStatusEnum.FINISHED_OK);

        } catch (Throwable e) {
            statusActivate.setEndTime(System.currentTimeMillis());
            statusActivate.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
            statusActivate.setErrorMessage(e.getMessage());
        }

        // Commit transaction if ok

        logger.info("Task is done");
        return giveCurrentTaskStatus(statusActivate);


    }

    @Override
	public TaskStatusActivation start(int tdiId) {
		final TaskStatusActivation status = new TaskStatusActivation(200);
		taskStatus.put(status.getTaskId(), status);
		status.setTitle("Starting virtual appliance TechnicalDeploymentInstance#" + tdiId);
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		this.springTaskCoreExecutor.execute(new Runnable() {

			@Override
			public void run() {
				logger.info("Task is starting");
				status.setPercent(50);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// ignore
				}
				status.setPercent(100);
				status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
				logger.info("Task is done");
			}
		});

		return giveCurrentTaskStatus(status);
	}

	@Override
	public TaskStatusActivation stop(int tdiId) {
		final TaskStatusActivation status = new TaskStatusActivation(200);
		taskStatus.put(status.getTaskId(), status);
		status.setTitle("Stopping virtual appliance TechnicalDeploymentInstance#" + tdiId);
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		this.springTaskCoreExecutor.execute(new Runnable() {

			@Override
			public void run() {
				logger.info("Task is starting");
				status.setPercent(50);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// ignore
				}
				status.setPercent(100);
				status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
				logger.info("Task is done");
			}
		});

		return giveCurrentTaskStatus(status);
	}

	@Override
	public TaskStatusActivation delete(final int tdiId) {
		// Prepare TaskStatus
		final TaskStatusActivation statusDelete = new TaskStatusActivation(200);
		taskStatus.put(statusDelete.getTaskId(), statusDelete);
		statusDelete.setTitle("Deleting virtual appliance TechnicalDeploymentInstance#" + tdiId);
		statusDelete.setStartTime(System.currentTimeMillis());
		statusDelete.setTaskStatus(TaskStatusEnum.STARTED);

		final TaskStatusActivation statusIaas = new TaskStatusActivation(201);
		statusIaas.setTitle("Deleting virtual appliance TechnicalDeploymentInstance#" + tdiId);
		statusIaas.setTaskStatus(TaskStatusEnum.TRANSIENT);
		statusDelete.addSubtask(statusIaas);

		final TaskStatusActivation statusConfiguration = new TaskStatusActivation(202);
		statusConfiguration.setTitle("Deleting post configuration of virtual appliance TechnicalDeploymentInstance#" + tdiId);
		statusConfiguration.setTaskStatus(TaskStatusEnum.TRANSIENT);
		statusDelete.addSubtask(statusConfiguration);

		this.springTaskCoreExecutor.execute(new Runnable() {

			@Override
			public void run() {

				logger.info("Task is starting");

				// Need an entity Manager ?

				// Need a transaction Manager

				// Attach a transactional context + démarcation on forked Thread

				try {
				
					statusIaas.setPercent(50);
					Thread.sleep(2000);
					statusIaas.setPercent(100);
					statusIaas.setTaskStatus(TaskStatusEnum.FINISHED_OK);

					statusConfiguration.setPercent(50);
					Thread.sleep(2000);
					statusConfiguration.setPercent(100);
					statusConfiguration.setTaskStatus(TaskStatusEnum.FINISHED_OK);

					final Environment environment = environmentRepository.findByTechnicalDeploymentInstanceId(tdiId);
					environment.updateStatus(EnvironmentStatus.REMOVED, "", 100);
					environmentRepository.save(environment);

				} catch (Throwable e) {
					statusDelete.setEndTime(System.currentTimeMillis());
					statusDelete.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					statusDelete.setErrorMessage(e.getMessage());
				}

				// Commit transaction if ok

				logger.info("Task is done");
			}
		});

		return giveCurrentTaskStatus(statusDelete);
	}

	/**
	 * IOC
	 * 
	 * @param springTaskCoreExecutor
	 */
	public void setSpringTaskCoreExecutor(TaskExecutor springTaskCoreExecutor) {
		this.springTaskCoreExecutor = springTaskCoreExecutor;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public TaskStatusActivation giveCurrentTaskStatus(TaskStatusActivation status) {
		TaskStatusActivation returnedStatus = null;
		TaskStatusActivation currentStatus = taskStatus.get(status.getTaskId());
		if (currentStatus != null) {
			returnedStatus = new TaskStatusActivation(currentStatus);
		}
		return returnedStatus;
	}

	public void setEnvironmentRepository(EnvironmentRepository environmentRepository) {
		this.environmentRepository = environmentRepository;
	}
}
