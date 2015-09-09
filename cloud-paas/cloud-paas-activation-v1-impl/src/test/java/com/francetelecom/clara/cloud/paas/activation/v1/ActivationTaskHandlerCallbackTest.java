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

import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.coremodel.EnvironmentRepository;
import com.francetelecom.clara.cloud.model.ModelItemRepository;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ExecutionQuery;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * ActivationTaskHandlerCallbackTest
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivationTaskHandlerCallbackTest {
    @Mock
    RuntimeService activitiRuntimeService;
    @SuppressWarnings("unused")
	@Mock
    private ProcessEngine processEngineMock;
    @SuppressWarnings("unused")
	@Mock
    private ActivationPluginStrategy pluginStrategyMock;
    @SuppressWarnings("unused")
	@Mock
    private ModelItemRepository modelItemRepository;
    @SuppressWarnings("unused")
	@Mock
    private EnvironmentRepository environmentRepositoryMock;
    @Mock
    private Execution executionActiviti;

    @Spy @InjectMocks
    private ActivationTaskHandlerCallback taskHandlerCallback = new ActivationTaskHandlerCallback();
    
    @Mock
	private ExecutionQuery executionQuery;


    @Before
    public void init() {
        doReturn(executionQuery).when(taskHandlerCallback).createActivitiRuntimeQuery();

    }

    @Test
    public void testOnTaskComplete_ReportDetailsOnError() throws Exception {
        // GIVEN
        // activation task
        final String processInstanceId = "taskPid";
        String activationStep = ActivationStepEnum.STOP.getName();
        final String activitiTaskId = "activity1234";
        int tdiId = 111;
        int entityId = 1111;
        String entityClassName = "com.francetelecom.clara.cloud.techmodel.cf.Space";
        String activationTaskErrorMessage = "ActivationTask Error message.";
        String activitiTaskErrorMessage = "ActivitiTask Error message.";
        int taskIndex = 0;
        int taskCount = 1;
        ActivationTask activationTask
                = new ActivationTask(processInstanceId, activationStep, activitiTaskId, tdiId, entityId,
                                     entityClassName, activationTaskErrorMessage, taskIndex, taskCount);

        // current activiti status task
        TaskStatusActivitiTask currentTask = new TaskStatusActivitiTask();
        currentTask.setActivationTask(activationTask);
        currentTask.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
        currentTask.setErrorMessage(activitiTaskErrorMessage);

        Space space = new Space();

        String executionActivitiId = "124";
        when(executionActiviti.getId())
                .thenReturn(executionActivitiId);
        doReturn(executionActiviti)
            .when(taskHandlerCallback).findExecutionByEndTask(any(ActivationTask.class));

        doReturn(activitiRuntimeService).when(processEngineMock).getRuntimeService();

        doReturn(space).when(modelItemRepository).find(anyInt(),any());

        String calculatedErrorMessage = "stop failed on Space#1111 (activity1234) : ActivitiTask Error message.";

        doNothing()
            .when(taskHandlerCallback).signalFailedTaskToActivitiEngine(executionActivitiId,
                "1", calculatedErrorMessage, entityId,
                Space.class, activationStep);

        String communicationId = "1234";
        // WHEN
        taskHandlerCallback.onTaskComplete(currentTask, communicationId);
        // THEN
        verify(taskHandlerCallback).signalFailedTaskToActivitiEngine(executionActivitiId,
                "1", calculatedErrorMessage, entityId,
                Space.class, activationStep);
    }
    
    @Test
	public void finding_execution_should_use_process_and_task_id() throws Exception {
		//Given
		ActivationTask activationTask = mock(ActivationTask.class);
		String processInstanceId  = "processId-0";
		String activitiTaskId  = "taskId-0";
		when(activationTask.getProcessInstanceId()).thenReturn(processInstanceId);
		when(activationTask.getActivitiTaskId()).thenReturn(activitiTaskId);

		String expectedActivityId = activitiTaskId+"-end";
		when(executionQuery.activityId(expectedActivityId)).thenReturn(executionQuery);
		when(executionQuery.processInstanceId(processInstanceId)).thenReturn(executionQuery);
		when(executionQuery.singleResult()).thenReturn(executionActiviti);
		
		//When
    	Execution execution = taskHandlerCallback.findExecutionByEndTask(activationTask);
    	
		//Then
    	verify(executionQuery).singleResult();
    	verify(executionQuery).activityId(expectedActivityId);
    	verify(executionQuery).processInstanceId(processInstanceId);
    	assertThat(execution).isEqualTo(executionActiviti);
    	    	
	}
}
