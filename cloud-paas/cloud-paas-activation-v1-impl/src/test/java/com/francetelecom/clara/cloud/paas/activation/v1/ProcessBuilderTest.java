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

import java.io.File;
import java.io.InputStream;
import java.util.List;

import org.activiti.bpmn.BpmnAutoLayout;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.EndEvent;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.bpmn.model.StartEvent;
import org.activiti.bpmn.model.UserTask;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ProcessBuilderTest {

	@Autowired
	ProcessEngine processEngine;

	@Autowired
	RuntimeService runtimeService;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	TaskService taskService;

	@Test
	public void testDynamicDeploy() throws Exception {
		// 1. Build up the model from scratch
		BpmnModel model = new BpmnModel();
		Process process = new Process();
		model.addProcess(process);
		process.setId("my-process");

		process.addFlowElement(createStartEvent());
		process.addFlowElement(createUserTask("task1", "First task", "fred"));
		process.addFlowElement(createUserTask("task2", "Second task", "john"));
		process.addFlowElement(createEndEvent());

		process.addFlowElement(createSequenceFlow("start", "task1"));
		process.addFlowElement(createSequenceFlow("task1", "task2"));
		process.addFlowElement(createSequenceFlow("task2", "end"));

		// 2. Generate graphical information
		new BpmnAutoLayout(model).execute();

		// 3. Deploy the process to the engine
		Deployment deployment = repositoryService.createDeployment().addBpmnModel("dynamic-model.bpmn", model).name("Dynamic process deployment").deploy();

		// 4. Start a process instance
		ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

		// 5. Check if task is available
		List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstance.getId()).list();

		Assert.assertEquals(1, tasks.size());
		Assert.assertEquals("First task", tasks.get(0).getName());
		Assert.assertEquals("fred", tasks.get(0).getAssignee());

		// 6. Save process diagram to a file
		InputStream processDiagram = repositoryService.getProcessDiagram(processInstance.getProcessDefinitionId());
		FileUtils.copyInputStreamToFile(processDiagram, new File("target/diagram.png"));

		// 7. Save resulting BPMN xml to a file
		InputStream processBpmn = repositoryService.getResourceAsStream(deployment.getId(), "dynamic-model.bpmn");
		FileUtils.copyInputStreamToFile(processBpmn, new File("target/process.bpmn20.xml"));
	}

	protected UserTask createUserTask(String id, String name, String assignee) {
		UserTask userTask = new UserTask();
		userTask.setName(name);
		userTask.setId(id);
		userTask.setAssignee(assignee);
		return userTask;
	}

	protected SequenceFlow createSequenceFlow(String from, String to) {
		SequenceFlow flow = new SequenceFlow();
		flow.setSourceRef(from);
		flow.setTargetRef(to);
		return flow;
	}

	protected StartEvent createStartEvent() {
		StartEvent startEvent = new StartEvent();
		startEvent.setId("start");
		return startEvent;
	}

	protected EndEvent createEndEvent() {
		EndEvent endEvent = new EndEvent();
		endEvent.setId("end");
		return endEvent;
	}

}
