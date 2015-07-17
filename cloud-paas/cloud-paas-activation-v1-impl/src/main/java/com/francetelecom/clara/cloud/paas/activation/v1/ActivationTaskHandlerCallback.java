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

import com.francetelecom.clara.cloud.EnvironmentDescriptionHandler;
import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.EnvironmentStatus;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.ModelItem;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.EnvironmentDescriptionHandlerImpl;
import com.francetelecom.clara.cloud.paas.activation.v1.async.TaskHandlerCallback;
import com.francetelecom.clara.cloud.technicalservice.exception.NotFoundException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Handle ActivationTask, e.g. forward task (from activiti) to the correct
 * plugin and method.
 */
public class ActivationTaskHandlerCallback implements TaskHandlerCallback<ActivationTask> {

	private static Logger logger = LoggerFactory.getLogger(ActivationTaskHandlerCallback.class.getName());

	private ProcessEngine processEngine;

	private ActivationPluginStrategy pluginStrategy;

	private ManageModelItem manageModelItem;

	private EnvironmentRepository environmentRepository;

	/**
	 * Key in logback context for connected environment label when available
	 */
	private static final String LOG_KEY_ENV_NAME = "env_name";

	/**
	 * Key in logback context for connected environment UID when available
	 */
	private static final String LOG_KEY_ENV_UID = "env_uid";

	/**
	 * Key in logback context for connected activiti process instance ID when
	 * available
	 */
	private static final String LOG_KEY_PROCESSID = "pid";

	private static final String TASK_SUCCESS = "successTask";

	private static final String TASK_FAILURE = "failureTask";

	/**
	 * Default timeout in seconds for a task. This timeout can be overriden by
	 * plugins by setting a suggestedTimeout in TaskStatus.
	 */
	private static final long TASK_DEFAULT_TIMEOUT = 60 * 60 * 4;

	//FIXME
	private static Map<String, Set<Integer>> progressMap = new HashMap<String, Set<Integer>>();

	@Override
	public TaskStatus handleRequest(ActivationTask request) {
		TaskStatusActivitiTask status = new TaskStatusActivitiTask(System.currentTimeMillis());
		status.setActivationTask(request);
		if (TASK_SUCCESS.equals(request.getActivitiTaskId())) {
			// Success :-D => update the environment status
			EnvironmentStatus newStatus;
			switch (request.getActivationStep()) {
			case ACTIVATE:
			case FIRSTSTART:
			case START:
				newStatus = EnvironmentStatus.RUNNING;
				break;
			case STOP:
				newStatus = EnvironmentStatus.STOPPED;
				break;
			case DELETE:
				newStatus = EnvironmentStatus.REMOVED;
				break;
			default:
				newStatus = EnvironmentStatus.UNKNOWN;
				logger.error("Unknown activation step : " + request.getActivationStep().getName());
			}
			if (environmentRepository.updateEnvironmentStateByTDI(request.getTdiId(), newStatus, "", 100)) {
				status.setTitle("Successfull activation : " + request.getActivationStep().getName() + " tdiId=" + request.getTdiId());
				status.setEndTime(System.currentTimeMillis());
				status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
			} else {
				status.setTitle("Activation FAILED (process is OK but env update failed) : " + request.getActivationStep().getName() + " tdiId="
						+ request.getTdiId());
				status.setEndTime(System.currentTimeMillis());
				status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
				status.setErrorMessage("Cannot update environment state of tdiId=" + request.getEntityId());
			}
		} else if (TASK_FAILURE.equals(request.getActivitiTaskId())) {
			String msg = generateErrorMessageFromRequest(request);
			logger.error(msg);
			// Error :-( => update the environment status
			environmentRepository.updateEnvironmentStateByTDI(request.getTdiId(), EnvironmentStatus.FAILED, msg, -1);
			// Traitement de l'erreur: on ne fait rien
			status.setTitle("Activation FAILED : " + request.getActivationStep().getName() + " tdiId=" + request.getTdiId());
			status.setEndTime(System.currentTimeMillis());
			status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
			status.setErrorMessage(msg);
		} else {
			// Store executed task id so we can set a progress percentage
			Set<Integer> startedTasks = progressMap.get(request.getProcessInstanceId());
			if (startedTasks == null) {
				startedTasks = new HashSet<Integer>(request.getTaskCount());
				progressMap.put(request.getProcessInstanceId(), startedTasks);
			}
			startedTasks.add(request.getTaskIndex());

			// Check plugin method duration, it must be fast
			long start = System.currentTimeMillis();

			TaskStatus taskStatus = null;
			ActivationPlugin plugin = pluginStrategy.getPlugin(request.getEntityClass(), request.getActivationStep());
			if (plugin != null) {
				try {
					Environment env = environmentRepository.findByTDIId(request.getTdiId());
					if (env != null) {
						// Can be null for mocks
						MDC.put(LOG_KEY_ENV_NAME, env.getLabel());
						MDC.put(LOG_KEY_ENV_UID, env.getUID());
					}
					MDC.put(LOG_KEY_PROCESSID, request.getProcessInstanceId());
					logger.info("[Activation start] " + plugin.getClass().getSimpleName() + "::" + request.getActivationStep() + "("
							+ request.getEntityClass().getSimpleName() + "#" + request.getEntityId() + ") => " + request.getActivitiTaskId());
					switch (request.getActivationStep()) {
					case INIT:
                        taskStatus = plugin.init(request.getEntityId(), request.getEntityClass());
                        break;
					case ACTIVATE:
                        EnvironmentDescriptionHandler environmentDescriptionHandler = new EnvironmentDescriptionHandlerImpl();
                        taskStatus = plugin.activate(request.getEntityId(), request.getEntityClass(),
                                environmentRepository.getActivationContext(request.getTdiId(), environmentDescriptionHandler));
                        break;
					case FIRSTSTART:
                        taskStatus = plugin.firststart(request.getEntityId(), request.getEntityClass());
                        break;
					case START:
                        taskStatus = plugin.start(request.getEntityId(), request.getEntityClass());
                        break;
					case STOP:
                        taskStatus = plugin.stop(request.getEntityId(), request.getEntityClass());
                        break;
					case DELETE:
                        taskStatus = plugin.delete(request.getEntityId(), request.getEntityClass());
                        break;
					default:
						logger.error("Unknown activation step : " + request.getActivationStep().getName());
					}
					status.setStatus(taskStatus);
				} catch (Throwable e) {
					String msg = "Error in " + plugin.getClass().getSimpleName() + "::" + request.getActivationStep() + "("
							+ request.getEntityClass().getSimpleName() + "#" + request.getEntityId() + ") => " + request.getActivitiTaskId();
					logger.error(msg, e);
					status.setEndTime(System.currentTimeMillis());
					status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					status.setErrorMessage(msg + " : " + e.getMessage());
				} finally {
					MDC.remove(LOG_KEY_ENV_NAME);
					MDC.remove(LOG_KEY_ENV_UID);
					MDC.remove(LOG_KEY_PROCESSID);
				}
				if (System.currentTimeMillis() - start > (1000 * 10)) {
					// Plugins method must be asynchrone
					logger.warn("*** Slow plugin (more than 10s) : type=" + request.getEntityClass().getName() + " step=" + request.getActivationStep()
							+ " task=" + request.getActivitiTaskId());
				}
			} else {
				// Nothing to do (no plugin for this entity at this step)
				status.setEndTime(System.currentTimeMillis());
				status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
			}

			// Update title and percentage of the status
			int percent = -1;
			startedTasks = progressMap.get(request.getProcessInstanceId());
			if (startedTasks != null && request.getTaskCount() > 0) {
				percent = (startedTasks.size() - 1) * 100 / request.getTaskCount();
			}
			environmentRepository.updateEnvironmentStateByTDI(request.getTdiId(), null, status.getTitle(), percent);
		}
		return status;
	}

	/**
	 * Generate a error message from the current request
	 * 
	 * @param request
	 * @return
	 */
	private String generateErrorMessageFromRequest(ActivationTask request) {
		StringBuilder sbError = new StringBuilder();
		sbError.append("Error in ").append(request.getActivationStep()).append("(").append(request.getEntityClass().getSimpleName()).append("#")
				.append(request.getEntityId()).append(")");
		String errorMessage = request.getErrMessage();
		if (errorMessage != null) {
			sbError.append(" : ").append(request.getErrMessage());
		}
		return sbError.toString();
	}

	@Override
	public TaskStatus onTaskPolled(TaskStatus taskStatus) {
		TaskStatusActivitiTask status = (TaskStatusActivitiTask) taskStatus;
		TaskStatusActivitiTask newStatus = new TaskStatusActivitiTask(status);
		try {
			if (status != null && status.getActivationTask() != null) {
				try {
					Environment env = environmentRepository.findByTDIId(status.getActivationTask().getTdiId());
					MDC.put(LOG_KEY_ENV_NAME, env.getLabel());
					MDC.put(LOG_KEY_ENV_UID, env.getUID());
				} catch (Exception e) {
					MDC.put(LOG_KEY_ENV_NAME, "unknown");
					MDC.put(LOG_KEY_ENV_UID, "unknown");
				}
				MDC.put(LOG_KEY_PROCESSID, status.getActivationTask().getProcessInstanceId());
			}

			if (System.currentTimeMillis() > (status.getStartTime() + (status.getSuggestedTimeout() <= 0L ? TASK_DEFAULT_TIMEOUT * 1000 : status
					.getSuggestedTimeout() * 1000))) {
				logger.error("Timeout after " + status.getSuggestedTimeout() + " seconds!");
				newStatus.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
				newStatus.setErrorMessage("Timeout after " + status.getSuggestedTimeout() + " seconds!");
			} else {
				if (status.getStatus() != null) {
					ActivationTask request = status.getActivationTask();
					ActivationPlugin plugin = pluginStrategy.getPlugin(request.getEntityClass(), request.getActivationStep());
					if (plugin != null) {
						TaskStatus pluginStatus = plugin.giveCurrentTaskStatus(status.getStatus());
						if (pluginStatus != null) {
							newStatus.setStatus(pluginStatus);
							int percent = -1;
							Set<Integer> startedTasks = progressMap.get(request.getProcessInstanceId());
							if (startedTasks != null && request.getTaskCount() > 0) {
								percent = (startedTasks.size() - 1) * 100 / request.getTaskCount();
							}
							logger.debug("Waiting for " + status.getActivationTask().getEntityClass().getSimpleName() + "#"
									+ status.getActivationTask().getEntityId() + " => status=" + status.getTaskStatus() + " progress=" + percent + "%");
						} else {
							String msg = "Plugin " + plugin.getClass().getSimpleName() + " returned null status for "
									+ status.getStatus().getClass().getSimpleName() + "#" + status.getStatus().getTaskId();
							logger.error(msg);
							newStatus.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
							newStatus.setErrorMessage(msg);
						}
					} else {
						String msg = "No more plugin for " + status.getActivationTask().getEntityClass().getSimpleName() + "#"
								+ status.getActivationTask().getEntityId() + " at step " + request.getActivationStep();
						logger.error(msg);
						newStatus.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
						newStatus.setErrorMessage(msg);
					}
				}
			}
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			newStatus.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
			newStatus.setErrorMessage("onTaskPolled(" + status.getStatus().getClass().getSimpleName() + "#" + status.getStatus().getTaskId() + ") => "
					+ e.getMessage());
		} finally {
			MDC.remove(LOG_KEY_PROCESSID);
		}
		return newStatus;
	}

	/**
	 * Notify activiti server that the task is ended: call success or error
	 * receiveTask depending on taskStatus.
	 */
	@Override
	public void onTaskComplete(TaskStatus taskStatus, String communicationId) {
		TaskStatusActivitiTask status = (TaskStatusActivitiTask) taskStatus;
		try {
			ActivationTask activationTask = status.getActivationTask();
			if (status != null && activationTask != null) {
				try {
					Environment env = environmentRepository.findByTDIId(activationTask.getTdiId());
					MDC.put(LOG_KEY_ENV_NAME, env.getLabel());
					MDC.put(LOG_KEY_ENV_UID, env.getUID());
				} catch (Exception e) {
					MDC.put(LOG_KEY_ENV_NAME, "unknown");
					MDC.put(LOG_KEY_ENV_UID, "unknown");
				}
				MDC.put(LOG_KEY_PROCESSID, activationTask.getProcessInstanceId());
			}

			logger.info("[Activation end] " + taskStatus.getTitle() + " \t" + activationTask.getEntityClass().getSimpleName() + "\t"
					+ activationTask.getActivationStep() + "\t" + status.getTaskStatus() + "\t" + (status.getEndTime() - status.getStartTime()));

			// Clean progress map
			Set<Integer> startedTasks = progressMap.get(activationTask.getProcessInstanceId());
			if (startedTasks != null && startedTasks.size() == activationTask.getTaskCount() || TASK_FAILURE.equals(activationTask.getActivitiTaskId())) {
				progressMap.remove(activationTask.getProcessInstanceId());
			}

			if (!TASK_SUCCESS.equals(activationTask.getActivitiTaskId()) && !TASK_FAILURE.equals(activationTask.getActivitiTaskId())) {
				try {
					// Protect this critic section so that execution will not be
					// stopped by an other task failure
					synchronized (processEngine) {

						Execution execution = findExecutionByEndTask(activationTask);

						if (execution == null) {
							logger.debug("Waiting 5s to be sure that activiti has flushed its session for " + activationTask.getActivitiTaskId() + " ("
									+ activationTask.getEntityClass().getSimpleName() + "#" + activationTask.getEntityId() + ")");
							Thread.sleep(5000);
							execution = findExecutionByEndTask(activationTask);

						}
						if (execution != null) {
							// Execution can be null if process failed in
							// another task
							status.setExecutionId(execution.getId());
							if (status.getTaskStatus() == TaskStatusEnum.FINISHED_OK) {
								onTaskCompleteHandleFinishedOk(status);
							} else {
								onTaskCompleteHandleFinishedNotOk(status);
							}
						} else {
							logger.warn("* Signal *NOT* sent to task " + activationTask.getActivitiTaskId() + " because there is no execution running");
							for (Execution exec : createActivitiRuntimeQuery().list()) {
								logger.debug("   - pi=" + exec.getProcessInstanceId() + " exec=" + exec.getId());
								for (String id : processEngine.getRuntimeService().getActiveActivityIds(exec.getProcessInstanceId())) {
									logger.debug("      - active activiti: " + id);
								}
							}
						}
					}
				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
					// to get more details onto javax validation, add the
					// following trace level :
					// <logger
					// name="org.hibernate.cfg.beanvalidation.BeanValidationEventListener"
					// level="TRACE"/>
					// Signal activiti that the task has failed
					if (status.getExecutionId() != null) {
						processEngine.getRuntimeService().setVariableLocal(status.getExecutionId(), "errCode", "1");
						processEngine.getRuntimeService().setVariable(status.getExecutionId(), "errMessage",
								e.getMessage() != null ? e.getMessage().substring(0, Math.min(4000, e.getMessage().length())) : "null");
						processEngine.getRuntimeService().setVariable(status.getExecutionId(), "entityId", activationTask.getEntityId());
						processEngine.getRuntimeService().setVariable(status.getExecutionId(), "entityClass", activationTask.getEntityClass().getName());
						processEngine.getRuntimeService().setVariable(status.getExecutionId(), "activationStep", activationTask.getActivationStep().getName());
						processEngine.getRuntimeService().signal(status.getExecutionId());
					} else {
						// task execution id is null => task has not been
						// executed (activiti error)
					}

				}
			}
		} finally {
			MDC.remove(LOG_KEY_PROCESSID);
			MDC.remove(LOG_KEY_ENV_NAME);
			MDC.remove(LOG_KEY_ENV_UID);
		}
	}

	protected Execution findExecutionByEndTask(ActivationTask task) {
		ExecutionQuery query = createActivitiRuntimeQuery();
		return query.processInstanceId(task.getProcessInstanceId()).activityId(task.getActivitiTaskId() + "-end").singleResult();
	}

	private void onTaskCompleteHandleFinishedOk(TaskStatusActivitiTask status) throws NotFoundException {
		// Update entity state if there was a treatment
		ActivationPlugin plugin = pluginStrategy.getPlugin(status.getActivationTask().getEntityClass(), status.getActivationTask().getActivationStep());
		if (plugin != null) {
			switch (status.getActivationTask().getActivationStep()) {
			case INIT:
				manageModelItem.setDeploymentState(status.getActivationTask().getEntityId(), status.getActivationTask().getEntityClass(),
						DeploymentStateEnum.CHECKED);
				break;
			case ACTIVATE:
				manageModelItem.setDeploymentState(status.getActivationTask().getEntityId(), status.getActivationTask().getEntityClass(),
						DeploymentStateEnum.CREATED);
				break;
			case FIRSTSTART:
			case START:
				manageModelItem.setDeploymentState(status.getActivationTask().getEntityId(), status.getActivationTask().getEntityClass(),
						DeploymentStateEnum.STARTED);
				break;
			case STOP:
				manageModelItem.setDeploymentState(status.getActivationTask().getEntityId(), status.getActivationTask().getEntityClass(),
						DeploymentStateEnum.STOPPED);
				break;
			case DELETE:
				manageModelItem.setDeploymentState(status.getActivationTask().getEntityId(), status.getActivationTask().getEntityClass(),
						DeploymentStateEnum.REMOVED);
				break;
			default:
				throw new TechnicalException("Unkwown ActivationStep: " + status.getActivationTask().getActivationStep().getName());
			}
		}
		// Signal activiti that the task is complete successfully
		processEngine.getRuntimeService().setVariableLocal(status.getExecutionId(), "errCode", "0");
		processEngine.getRuntimeService().signal(status.getExecutionId());
		logger.debug("+ Signal OK sent to task " + status.getActivationTask().getActivitiTaskId() + " finished status=" + status.getTaskStatus() + " ("
				+ status.getActivationTask().getEntityClass().getSimpleName() + "#" + status.getActivationTask().getEntityId() + ")");
	}

	private void onTaskCompleteHandleFinishedNotOk(TaskStatusActivitiTask status) throws NotFoundException {
		// Update entity state
		ActivationTask activationTask = status.getActivationTask();
		manageModelItem.setDeploymentState(activationTask.getEntityId(), activationTask.getEntityClass(), DeploymentStateEnum.UNKNOWN);

		String errorCode = "1";
		int entityId = activationTask.getEntityId();
		Class<? extends ModelItem> entityClass = activationTask.getEntityClass();
		String activationStep = activationTask.getActivationStep().getName();
		String activitiTaskId = activationTask.getActivitiTaskId();

		String errorMessage = activationStep + " failed on " + entityClass.getSimpleName() + "#" + entityId + " (" + activitiTaskId + ")";
		if (status.getErrorMessage() != null) {
			errorMessage += " : " + status.getErrorMessage();
		}
		logger.error(errorMessage);
		signalFailedTaskToActivitiEngine(status.getExecutionId(), errorCode, errorMessage, entityId, entityClass, activationStep);

		logger.debug("* Signal KO sent to task " + activitiTaskId + " finished status=" + status.getTaskStatus());
	}

	void signalFailedTaskToActivitiEngine(String executionId, String errorCode, String errorMessage, int entityId, Class<? extends ModelItem> entityClass,
			String activationStep) {
		// Signal activiti that the task has failed
		processEngine.getRuntimeService().setVariableLocal(executionId, "errCode", errorCode);
		processEngine.getRuntimeService().setVariable(executionId, "errMessage", limitTo(errorMessage, 4000));
		processEngine.getRuntimeService().setVariable(executionId, "entityId", entityId);
		processEngine.getRuntimeService().setVariable(executionId, "entityClass", entityClass.getName());
		processEngine.getRuntimeService().setVariable(executionId, "activationStep", activationStep);
		processEngine.getRuntimeService().signal(executionId);
	}

	private String limitTo(String errorMessage, int maxSize) {
		if (errorMessage == null) {
			return null;
		}
		return errorMessage.substring(0, Math.min(maxSize, errorMessage.length()));

	}

	@Override
	public void onFailure(Throwable throwable, String communicationId) {
		logger.error("onFailure called : communicationId=" + communicationId + " msg=" + throwable.getMessage(), throwable);
		for (Execution exec : createActivitiRuntimeQuery().list()) {
			logger.debug("   - pi=" + exec.getProcessInstanceId() + " exec=" + exec.getId() + " isEnded=" + exec.isEnded());
			for (String id : processEngine.getRuntimeService().getActiveActivityIds(exec.getId())) {
				logger.debug("      - active activiti: " + id);
			}
			if (!exec.getProcessInstanceId().equals(exec.getId())) {
				for (String id : processEngine.getRuntimeService().getActiveActivityIds(exec.getId())) {
					if (id.equals(communicationId + "-end")) {
						// Signal activiti that the task has failed
						processEngine.getRuntimeService().setVariableLocal(exec.getId(), "errCode", "1");
						processEngine.getRuntimeService().setVariable(exec.getId(), "errMessage",
								throwable.getMessage() != null ? throwable.getMessage().substring(0, Math.min(4000, throwable.getMessage().length())) : "null");
						processEngine.getRuntimeService().signal(exec.getId());
						break;
					}
				}
			}
		}
	}

	protected ExecutionQuery createActivitiRuntimeQuery() {
		return processEngine.getRuntimeService().createExecutionQuery();
	}

	/**
	 * IOC
	 * 
	 * @param processEngine
	 */
	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	/**
	 * IOC
	 * 
	 * @param pluginStrategy
	 */
	public void setPluginStrategy(ActivationPluginStrategy pluginStrategy) {
		this.pluginStrategy = pluginStrategy;
	}

	/**
	 * IOC
	 * 
	 * @param manageModelItem
	 */
	public void setManageModelItem(ManageModelItem manageModelItem) {
		this.manageModelItem = manageModelItem;
	}

	/**
	 * IOC
	 * 
	 * @param environmentRepository
	 */
	public void setEnvironmentRepository(EnvironmentRepository environmentRepository) {
		this.environmentRepository = environmentRepository;
	}

}
