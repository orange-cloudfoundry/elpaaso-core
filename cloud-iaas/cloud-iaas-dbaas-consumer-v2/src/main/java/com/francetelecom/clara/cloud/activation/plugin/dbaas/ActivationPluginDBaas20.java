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
package com.francetelecom.clara.cloud.activation.plugin.dbaas;

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.coremodel.ActivationContext;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.task.TaskExecutor;

import java.util.HashMap;
import java.util.Map;


/**
 * Plugin that create/start/stop/delete databases as a service (using dbaas
 * consumer)
 */
public class ActivationPluginDBaas20 extends ActivationPlugin<DBaasSubscriptionV2> {
    /**
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(ActivationPluginDBaas20.class);

	public static final long TIMEOUT_ACTIVATE = 60 * 20;
	public static final long TIMEOUT_FIRSTSTART = 60 * 10;
	public static final long TIMEOUT_START = 60 * 10;
	public static final long TIMEOUT_STOP = 60 * 10;
	public static final long TIMEOUT_DELETE = 60 * 10;

	private ActivationPluginDBaasUtils utils;

	private TaskExecutor taskExecutor;

	private static class DbaasTaskStatus extends TaskStatus {

		private static final long serialVersionUID = -7862238190572587095L;

		private final String dbaasVersion;

		private final TaskStatus status;

		public DbaasTaskStatus(long taskId, String dbaasVersion) {
			super(taskId);
			this.status = null;
			this.dbaasVersion = dbaasVersion;
		}

		public DbaasTaskStatus(TaskStatus status, String dbaasVersion) {
			super(status);
			this.status = status;
			this.dbaasVersion = dbaasVersion;
		}

		public DbaasTaskStatus(DbaasTaskStatus status) {
			super(status);
			this.status = null;
			this.dbaasVersion = status.dbaasVersion;
		}

	}

	private final Map<Long, TaskStatus> taskStatus = new HashMap<Long, TaskStatus>();

	@Override
	public TaskStatus giveCurrentTaskStatus(TaskStatus status) {
		if (status.isComplete()) {
			return status;
		}
		DbaasTaskStatus myStatus = (DbaasTaskStatus) taskStatus.get(status.getTaskId());
		if (myStatus.status != null) {
			TaskStatus newStatus = utils.getDBaasConsumer(myStatus.dbaasVersion).giveCurrentTaskStatus(myStatus.status);
			myStatus.setTaskStatus(newStatus.getTaskStatus());
			myStatus.setEndTime(newStatus.getEndTime());
			myStatus.setErrorMessage(newStatus.getErrorMessage());
			myStatus.setTitle(newStatus.getTitle());
			myStatus.setSubtitle(newStatus.getSubtitle());
		}
		return new DbaasTaskStatus(myStatus);
	}


	@Override
    public TaskStatus activate(final DBaasSubscriptionV2 dbaas, final ActivationContext context) {
        DbaasTaskStatus status = new DbaasTaskStatus(utils.createDatabase(dbaas.getId(), context.toString()), dbaas.getDbaasVersion());
        status.setTitle("Creating database for dbaasSubscription#" + dbaas.getId() + " (" + dbaas.getSqlDialect().name() + ")");
		status.setSuggestedTimeout(TIMEOUT_ACTIVATE);
		synchronized (taskStatus) {
			this.taskStatus.put(status.getTaskId(), status);
		}
		return status;
	}

	/**
	 * Populate SQL at first start
	 */
	@Override
    public TaskStatus firststart(final DBaasSubscriptionV2 dbaas) {
        final DbaasTaskStatus status;
		synchronized (taskStatus) {
			status = new DbaasTaskStatus(System.currentTimeMillis(), dbaas.getDbaasVersion());
			status.setTitle("Starting database " + dbaas.getDatabaseUUId() + " (" + dbaas.getSqlDialect().name() + ")");
			status.setStartTime(System.currentTimeMillis());
			status.setSuggestedTimeout(TIMEOUT_FIRSTSTART);
			status.setTaskStatus(TaskStatusEnum.STARTED);
			taskStatus.put(status.getTaskId(), status);
		}

		final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

		this.taskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				try {
					// Copy current log context informations to the new thread
					if (mdcContext != null)
						MDC.setContextMap(mdcContext);

					utils.firststart(dbaas.getId());
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
				} catch (Throwable e) {
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					status.setErrorMessage(e.getMessage());
				} finally {
					// Clear all context informations so the thread can be
					// re-used
					MDC.clear();
				}
			}
		});
		return new DbaasTaskStatus(status);
	}

	@Override
    public TaskStatus start(final DBaasSubscriptionV2 dbaas) {
        DbaasTaskStatus status = new DbaasTaskStatus(utils.start(dbaas), dbaas.getDbaasVersion());
		synchronized (taskStatus) {
			this.taskStatus.put(status.getTaskId(), status);
		}
		status.setTitle("Starting database " + dbaas.getDatabaseUUId() + " (" + dbaas.getSqlDialect().name() + ")");
		status.setSuggestedTimeout(TIMEOUT_START);
		return status;
	}


	@Override
    public TaskStatus stop(final DBaasSubscriptionV2 dbaas) {
        DbaasTaskStatus status = new DbaasTaskStatus(utils.stop(dbaas), dbaas.getDbaasVersion());
		synchronized (taskStatus) {
			this.taskStatus.put(status.getTaskId(), status);
		}
		status.setTitle("Stopping database " + dbaas.getDatabaseUUId() + " (" + dbaas.getSqlDialect().name() + ")");
		status.setSuggestedTimeout(TIMEOUT_STOP);
		return status;
	}


	@Override
    public TaskStatus delete(final DBaasSubscriptionV2 dbaas) {
        // Must not fail if DB does not exists (when deleting after an
		// activation error)
		DbaasTaskStatus status;
		String dbaasVersion = dbaas.getDbaasVersion();
		String databaseUUId = dbaas.getDatabaseUUId();
		String sqlVersionName = (dbaas.getSqlDialect() != null ? dbaas.getSqlDialect().name() : "(null!?!)");
		String action = "[DBaaS " + dbaasVersion + "] deleting database " + databaseUUId + " (" + sqlVersionName + ")";
		try {
			status = new DbaasTaskStatus(utils.delete(dbaas), dbaasVersion);
			synchronized (taskStatus) {
				this.taskStatus.put(status.getTaskId(), status);
			}
			status.setTitle(action);
			status.setSuggestedTimeout(TIMEOUT_DELETE);
		} catch (Throwable e) {
			status = new DbaasTaskStatus(System.currentTimeMillis(), dbaasVersion);
			this.taskStatus.put(status.getTaskId(), status);
			status.setEndTime(System.currentTimeMillis());
			String errorMessage = e.getMessage();
			String reportedErrorMessage = "Error while " + action + " : " + errorMessage;
			if (isIgnored(errorMessage)) {
				status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
			} else {
				status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
				status.setErrorMessage(reportedErrorMessage);
				logger.error(reportedErrorMessage, e);
			}
		}
		return status;
	}


	private boolean isIgnored(String errorMessage) {
		if (errorMessage == null) {
			return false;
		}
		for (IgnoredErrors error : IgnoredErrors.values()) {
			if (errorMessage.contains(error.toString())) {
				return true;
			}
		}
		return false;
	}

	public enum IgnoredErrors {
		DATABASE_NOT_FOUND("No database found"), DATABASE_IN_INCIDENT("is in incident"), DATABASE_ALREADY_DELETED("database is already deleted"), CONCURRENT_DELETION(
				"concurrent deletion");

		private String message;

		private IgnoredErrors(String msg) {
			this.message = msg;
		}

		public String toString() {
			return message;
		}
	}

	@Override
	public boolean accept(Class<?> entityClass, ActivationStepEnum step) {
		return entityClass.equals(DBaasSubscriptionV2.class);
	}

	/**
	 * IOC
	 * 
	 * @param utils
	 */
	public void setUtils(ActivationPluginDBaasUtils utils) {
		this.utils = utils;
	}

	/**
	 * IOC
	 * 
	 * @param taskExecutor
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}
}
