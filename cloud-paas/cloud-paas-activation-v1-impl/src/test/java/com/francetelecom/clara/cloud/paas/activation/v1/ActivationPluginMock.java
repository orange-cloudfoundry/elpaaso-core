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

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.coremodel.ActivationContext;
import com.francetelecom.clara.cloud.model.ModelItem;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;

import java.util.HashMap;
import java.util.Map;

public class ActivationPluginMock extends ActivationPlugin<ModelItem> {

    private static Logger logger = LoggerFactory.getLogger(ActivationPluginMock.class.getName());

	@Autowired
	private TaskExecutor taskExecutor;

	private ActivationPluginMockUtils utils;

	private Map<Long, TaskStatus> taskStatus = new HashMap<Long, TaskStatus>();

	@Override
	public TaskStatus giveCurrentTaskStatus(TaskStatus status) {
		TaskStatus newStatus = null;
		TaskStatus currentStatus = taskStatus.get(status.getTaskId());
		if (currentStatus != null) {
			newStatus = new TaskStatus(currentStatus);
		}
        String statusTitle = (status != null ? status.getTitle(): "null");
        String newStatusStr = (newStatus != null ? newStatus.getTaskStatus().toString() : "(null)");
		logger.debug("giveCurrentTaskStatus '"+statusTitle+"' => "+newStatusStr);
		return newStatus;
	}

	@Override
    public TaskStatus init(final int entityId, final Class<ModelItem> entityClass) {
        logger.debug("init entity "+entityId);

		final TaskStatus status;
		synchronized (taskStatus) {
			status = new TaskStatus(System.currentTimeMillis());
			taskStatus.put(status.getTaskId(), status);
			status.setTitle("init "+entityId);
			status.setStartTime(System.currentTimeMillis());
			status.setTaskStatus(TaskStatusEnum.STARTED);
		}

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

		this.taskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					// Copy current log context informations to the new thread
					if (mdcContext != null) MDC.setContextMap(mdcContext);

					Thread.currentThread().setName("myThread#"+entityId);
					utils.init(entityId,entityClass);
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
				} catch (Throwable e) {
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					status.setErrorMessage(e.getMessage());
					logger.error(e.getMessage(), e);
				} finally {
					// Clear all context informations so the thread can be re-used
					MDC.clear();
				}
			}
		});

		return status;
	}

	@Override
    public TaskStatus activate(final int entityId, final Class<ModelItem> entityClass, ActivationContext context) {
        logger.debug("activating entity "+entityId);

		final TaskStatus status;
		synchronized (taskStatus) {
			status = new TaskStatus(System.currentTimeMillis());
			taskStatus.put(status.getTaskId(), status);
			status.setTitle("activate "+entityId);
			status.setStartTime(System.currentTimeMillis());
			status.setTaskStatus(TaskStatusEnum.STARTED);
		}

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

		this.taskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					// Copy current log context informations to the new thread
					if (mdcContext != null) MDC.setContextMap(mdcContext);

					utils.activate(entityId,entityClass);
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
				} catch (Throwable e) {
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					status.setErrorMessage(e.getMessage());
					logger.error(e.getMessage(), e);
				} finally {
					// Clear all context informations so the thread can be re-used
					MDC.clear();
				}
			}
		});

		return status;
	}

	@Override
    public TaskStatus firststart(final int entityId, final Class<ModelItem> entityClass) {
        logger.debug("starting entity "+entityId);

		final TaskStatus status;
		synchronized (taskStatus) {
			status = new TaskStatus(System.currentTimeMillis());
			taskStatus.put(status.getTaskId(), status);
			status.setTitle("firststart "+entityId);
			status.setStartTime(System.currentTimeMillis());
			status.setTaskStatus(TaskStatusEnum.STARTED);
		}

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

		this.taskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					// Copy current log context informations to the new thread
					if (mdcContext != null) MDC.setContextMap(mdcContext);

					utils.firststart(entityId,entityClass);
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
				} catch (Throwable e) {
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					status.setErrorMessage(e.getMessage());
					logger.error(e.getMessage(), e);
				} finally {
					// Clear all context informations so the thread can be re-used
					MDC.clear();
				}
			}
		});

		return status;
	}

	@Override
    public TaskStatus start(final int entityId, final Class<ModelItem> entityClass) {
        logger.debug("starting entity "+entityId);

		final TaskStatus status;
		synchronized (taskStatus) {
			status = new TaskStatus(System.currentTimeMillis());
			taskStatus.put(status.getTaskId(), status);
			status.setTitle("start "+entityId);
			status.setStartTime(System.currentTimeMillis());
			status.setTaskStatus(TaskStatusEnum.STARTED);
		}

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

		this.taskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					// Copy current log context informations to the new thread
					if (mdcContext != null) MDC.setContextMap(mdcContext);

					utils.start(entityId,entityClass);
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
				} catch (Throwable e) {
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					status.setErrorMessage(e.getMessage());
					logger.error(e.getMessage(), e);
				} finally {
					// Clear all context informations so the thread can be re-used
					MDC.clear();
				}
			}
		});

		return status;
	}

	@Override
    public TaskStatus stop(final int entityId, final Class<ModelItem> entityClass) {
        logger.debug("stopping entity "+entityId);

		final TaskStatus status;
		synchronized (taskStatus) {
			status = new TaskStatus(System.currentTimeMillis());
			taskStatus.put(status.getTaskId(), status);
			status.setTitle("stop "+entityId);
			status.setStartTime(System.currentTimeMillis());
			status.setTaskStatus(TaskStatusEnum.STARTED);
		}

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

		this.taskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					// Copy current log context informations to the new thread
					if (mdcContext != null) MDC.setContextMap(mdcContext);

					utils.stop(entityId,entityClass);
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
				} catch (Throwable e) {
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					status.setErrorMessage(e.getMessage());
					logger.error(e.getMessage(), e);
				} finally {
					// Clear all context informations so the thread can be re-used
					MDC.clear();
				}
			}
		});

		return status;
	}

	@Override
    public TaskStatus delete(final int entityId, final Class<ModelItem> entityClass) {
        logger.debug("deleting entity "+entityId);

		final TaskStatus status;
		synchronized (taskStatus) {
			status = new TaskStatus(System.currentTimeMillis());
			taskStatus.put(status.getTaskId(), status);
			status.setTitle("delete "+entityId);
			status.setStartTime(System.currentTimeMillis());
			status.setTaskStatus(TaskStatusEnum.STARTED);
		}

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

		this.taskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					// Copy current log context informations to the new thread
					if (mdcContext != null) MDC.setContextMap(mdcContext);

					utils.delete(entityId,entityClass);
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
				} catch (Throwable e) {
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					status.setErrorMessage(e.getMessage());
					logger.error(e.getMessage(), e);
				} finally {
					// Clear all context informations so the thread can be re-used
					MDC.clear();
				}
			}
		});

		return status;
	}

	@Override
	public boolean accept(Class<?> entityClass, ActivationStepEnum step) {
		return true;
	}

	public void setUtils(ActivationPluginMockUtils utils) {
		this.utils = utils;
	}
}
