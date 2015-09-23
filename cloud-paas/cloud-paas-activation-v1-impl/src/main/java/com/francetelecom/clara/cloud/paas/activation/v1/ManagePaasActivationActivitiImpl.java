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

import com.francetelecom.clara.cloud.commons.NotFoundException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstanceRepository;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.paas.activation.ManagePaasActivation;
import com.francetelecom.clara.cloud.paas.activation.TaskStatusActivation;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.connection.SynchedLocalTransactionFailedException;

import java.util.HashMap;
import java.util.Map;

public class ManagePaasActivationActivitiImpl implements ManagePaasActivation {

	private static Logger logger = LoggerFactory.getLogger(ManagePaasActivationActivitiImpl.class.getName());

	private Map<Long, TaskStatusActivation> taskStatusMap = new HashMap<Long, TaskStatusActivation>();

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private TechnicalDeploymentInstanceRepository technicalDeploymentInstanceRepository;

	@Autowired
	private ManagePaasActivationActivitiUtilsImpl managePaasActivationActivitiUtilsImpl;

	@Autowired
	protected ProcessEngine processEngine;

	@Override
	public TaskStatusActivation activate(final int tdiId) {

		logger.info("Starting TechnicalDeploymentInstance#" + tdiId + " activation");

		final TaskStatusActivitiProcess status;
		synchronized (taskStatusMap) {
			status = new TaskStatusActivitiProcess(System.currentTimeMillis());
			taskStatusMap.put(status.getTaskId(), status);
		}
		status.setTechnicalDeploymentInstanceId(tdiId);
		status.setTitle("Starting activate appliance TDI : TechnicalDeploymentInstance#" + tdiId);
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		status.setFinalState(DeploymentStateEnum.STARTED);
		status.setPercent(0);

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        this.taskExecutor.execute(new ActivitiRunnableThread(ActivationStepEnum.ACTIVATE, tdiId, mdcContext, status));
		
		return giveCurrentTaskStatus(status);
	}

	@Override
	public TaskStatusActivation start(final int tdiId) {

		final TaskStatusActivitiProcess status;
		synchronized (taskStatusMap) {
			status = new TaskStatusActivitiProcess(System.currentTimeMillis());
			taskStatusMap.put(status.getTaskId(), status);
		}
		status.setTechnicalDeploymentInstanceId(tdiId);
		status.setTitle("Starting TechnicalDeploymentInstance#" + tdiId);
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		status.setFinalState(DeploymentStateEnum.STARTED);
		status.setPercent(0);

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        this.taskExecutor.execute(new ActivitiRunnableThread(ActivationStepEnum.START, tdiId, mdcContext, status));

		return giveCurrentTaskStatus(status);
	}

	@Override
	public TaskStatusActivation stop(final int tdiId) {

		final TaskStatusActivitiProcess status;
		synchronized (taskStatusMap) {
			status = new TaskStatusActivitiProcess(System.currentTimeMillis());
			taskStatusMap.put(status.getTaskId(), status);
		}
		status.setTechnicalDeploymentInstanceId(tdiId);
		status.setTitle("Stopping TechnicalDeploymentInstance#" + tdiId);
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		status.setFinalState(DeploymentStateEnum.STOPPED);
		status.setPercent(0);

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        this.taskExecutor.execute(new ActivitiRunnableThread(ActivationStepEnum.STOP, tdiId, mdcContext, status));

		return giveCurrentTaskStatus(status);
	}

	@Override
	public TaskStatusActivation delete(final int tdiId) {

		final TaskStatusActivitiProcess status;
		synchronized (taskStatusMap) {
			status = new TaskStatusActivitiProcess(System.currentTimeMillis());
			taskStatusMap.put(status.getTaskId(), status);
		}
		status.setTechnicalDeploymentInstanceId(tdiId);
		status.setTitle("Deleting TechnicalDeploymentInstance#" + tdiId);
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		status.setFinalState(DeploymentStateEnum.REMOVED);
		status.setPercent(0);

        final Map<String, String> mdcContext = MDC.getCopyOfContextMap();

        this.taskExecutor.execute(new ActivitiRunnableThread(ActivationStepEnum.DELETE, tdiId, mdcContext, status));

		return giveCurrentTaskStatus(status);
	}

	@Override
	public TaskStatusActivation giveCurrentTaskStatus(TaskStatusActivation taskStatus) {

		TaskStatusActivation newStatus = null;

		if (taskStatus.getClass().equals(TaskStatusActivation.class)) {
			TaskStatusActivation currentStatus = taskStatusMap.get(taskStatus.getTaskId());
			newStatus = new TaskStatusActivation(taskStatus.getTaskId());
			newStatus.setTechnicalDeploymentInstanceId(currentStatus.getTechnicalDeploymentInstanceId());
			newStatus.setStartTime(currentStatus.getStartTime());
			newStatus.setEndTime(currentStatus.getEndTime());
			newStatus.setTaskStatus(currentStatus.getTaskStatus());
			newStatus.setTitle(currentStatus.getTitle());
			newStatus.setSubtitle(currentStatus.getSubtitle());
			newStatus.setPercent(currentStatus.getPercent());
			newStatus.setErrorMessage(currentStatus.getErrorMessage());
			newStatus.setMaxPercent(currentStatus.getMaxPercent());
			// We have to call addSubtask method in order to update global state
			// of the task
			for (TaskStatus subTask : currentStatus.listSubtasks()) {
				if (subTask.getClass().equals(TaskStatusActivation.class) || subTask.getClass().equals(TaskStatusTemplatesGeneration.class)) {
					newStatus.addSubtask(subTask);
				} else if (subTask.getClass().equals(TaskStatusActivitiProcess.class)) {
					newStatus.addSubtask(giveCurrentTaskStatus((TaskStatusActivation) subTask));
				} else {
					throw new TechnicalException("Not a supported TaskStatus.");
				}
			}
		} else if (taskStatus.getClass().equals(TaskStatusActivitiProcess.class)) {

			// Copy it
			TaskStatusActivation currentStatus = taskStatusMap.get(taskStatus.getTaskId());
			newStatus = new TaskStatusActivitiProcess((TaskStatusActivitiProcess) currentStatus);
			// If ProcessInstanceId is null, the process activity hasn't started yet
			// Return the copy of task
			String processInstanceId = ((TaskStatusActivitiProcess) newStatus).getProcessInstanceId();
			if (processInstanceId != null) {
				// Then update it
				ProcessInstance pi = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
				if (pi == null || pi.isEnded()) {
					// Process is complete

					// Update TDI state and task status
					try {
						HistoricProcessInstance hpi = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
						if (hpi != null && hpi.getEndActivityId() != null && "endglobal".equals(hpi.getEndActivityId())) {
							final TechnicalDeploymentInstance technicalDeploymentInstance = technicalDeploymentInstanceRepository.findOne(newStatus.getTechnicalDeploymentInstanceId());
							if (technicalDeploymentInstance == null) throw (new NotFoundException("Cannot find TDI id=" + newStatus.getTechnicalDeploymentInstanceId()));
							technicalDeploymentInstance.setDeploymentState(((TaskStatusActivitiProcess) newStatus).getFinalState());
							technicalDeploymentInstanceRepository.save(technicalDeploymentInstance);
							newStatus.setEndTime(hpi.getEndTime().getTime());
							newStatus.setPercent(100);
							newStatus.setTaskStatus(TaskStatusEnum.FINISHED_OK);
						} else {
							// TODO find a way to get error message from activiti
							StringBuffer error = new StringBuffer("Activiti process ended but not in expected task. HistoricProcessInstance (hpi): ");
							if (hpi != null) {
								error.append(" hpi.id=");
								error.append(hpi.getId());
								error.append(" hpi.deleteReason=");
								error.append(hpi.getDeleteReason());
								error.append(" hpi.processDefinitionId=");
								error.append(hpi.getProcessDefinitionId());
								error.append(" hpi.startTime=");
								error.append(hpi.getStartTime());
								error.append(" hpi.endTime=");
								error.append(hpi.getEndTime());
								newStatus.setEndTime(hpi.getEndTime() != null ? hpi.getEndTime().getTime() : System.currentTimeMillis());
							}
							else {
								error.append(" hpi=NULL");
							}
							final TechnicalDeploymentInstance technicalDeploymentInstance = technicalDeploymentInstanceRepository.findOne(newStatus.getTechnicalDeploymentInstanceId());
							if (technicalDeploymentInstance == null) throw (new NotFoundException("Cannot find TDI id=" + newStatus.getTechnicalDeploymentInstanceId()));
							technicalDeploymentInstance.setDeploymentState(DeploymentStateEnum.UNKNOWN);
							technicalDeploymentInstanceRepository.save(technicalDeploymentInstance);

							newStatus.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
							newStatus.setErrorMessage(error.toString());
							logger.debug(error.toString() + " - Following is an HistoricDetailQuery:");
							for (HistoricDetail detail : processEngine.getHistoryService().createHistoricDetailQuery().processInstanceId(processInstanceId).list()) {
								logger.debug("  - " + detail.toString());
							}
							logger.debug("End of HistoricDetailQuery:");
						}
						logger.debug("End of process for TechnicalDeploymentInstance#" + newStatus.getTechnicalDeploymentInstanceId() + " => " + newStatus.getTaskStatus().name());
					} catch (NotFoundException e) {
						newStatus.setErrorMessage("Cannot find TDI id=" + newStatus.getTechnicalDeploymentInstanceId());
						newStatus.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
					}
				} else {
					// Process is in progress
					newStatus.setTaskStatus(TaskStatusEnum.STARTED);
					try {
						for (HistoricActivityInstance ai : processEngine.getHistoryService().createHistoricActivityInstanceQuery()
								.processInstanceId(processInstanceId).orderByHistoricActivityInstanceEndTime().desc().list()) {
							// logger.debug("Activiti id="+ai.getActivityId()+" type="+ai.getActivityType()+" start="+ai.getStartTime()+" end="+ai.getEndTime());
							if (ai.getActivityName() != null) {
								newStatus.setSubtitle(ai.getActivityName());
								break;
							}
						}
						//logger.debug("-----------------------------");
					}
					catch (ActivitiException e) {
						// Ignore since it can be due to end of process
						// and because it doesn't really matter
					}
				}
			}

		}
		return newStatus;
	}

	private class ActivitiRunnableThread implements Runnable {
        private final ActivationStepEnum step;
        private int tdiId;
        private Map<String, String> mdcContext;
        private TaskStatusActivitiProcess status;

        public ActivitiRunnableThread(final ActivationStepEnum step, final int tdiId, Map<String, String> mdcContext, TaskStatusActivitiProcess status) {
            this.step = step;
            this.tdiId = tdiId;
            this.mdcContext = mdcContext;
            this.status = status;
        }

        @Override
        public void run() {
            String action = "process "+step.name()+" on TDI#"+tdiId;
            String curAction = action;
            try {
                // Copy current log context informations to the new thread
                if (mdcContext != null) MDC.setContextMap(mdcContext);

                Thread.currentThread().setName("tdi#"+tdiId);
                curAction = action + " getTDI";
                TechnicalDeploymentInstance tdi = managePaasActivationActivitiUtilsImpl.getTDI(tdiId);
                curAction = action + " createProcess";
				String processId = managePaasActivationActivitiUtilsImpl.createProcess(tdi, step);
				try {
					curAction = action + " runProcess";
                    ProcessInstance processInstance = managePaasActivationActivitiUtilsImpl.runProcess(tdiId, step, processId);
                    status.setProcessInstanceId(processInstance.getId());
                } catch (SynchedLocalTransactionFailedException synchedLocalTransactionFailedException) {
                    // we do not update environment in this case because activiti workflow continue
                    // cf. anomalie #103144  &&  [ anomalie #103832 ] ElPaaso transactionnal aspect : local or global
                    String errorMessage = "[anomalie #103832] Internal error while " + action;
                    String endUserErrorMessage = "INTERNAL ERROR DURING ACTIVATION PROCESS "
                            + synchedLocalTransactionFailedException.getMessage();
                    logger.warn(errorMessage, synchedLocalTransactionFailedException);
                    status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
                    status.setEndTime(System.currentTimeMillis());
                    status.setErrorMessage(endUserErrorMessage);
                } catch (Throwable exc) {
                    managePaasActivationActivitiUtilsImpl.handleError(action + " : exception while activation process", exc, status, tdiId);
                }
            } catch (NotFoundException e) {
                managePaasActivationActivitiUtilsImpl.handleError(action + " : unable to find the technical model", e, status, tdiId);
            } catch (Throwable exc) {
                managePaasActivationActivitiUtilsImpl.handleError(curAction, exc, status, tdiId);
            } finally {
                // Clear all context informations so the thread can be re-used
                MDC.clear();
            }
        }
    }

}
