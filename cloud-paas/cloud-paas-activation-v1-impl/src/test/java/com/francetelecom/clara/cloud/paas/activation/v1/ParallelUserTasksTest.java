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

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ExecutionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ParallelUserTasksTest {

	@Autowired
	ProcessEngine processEngine;

	@Autowired
	RuntimeService runtimeService;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	IdentityService identityService;

	@Autowired
	TaskService taskService;

	@Test
	public void test() throws InterruptedException {

		// TODO test en genuine BPMN 2.0 process

		// deploy the process
		repositoryService
				.createDeployment()
				.addClasspathResource(
						"com/francetelecom/clara/cloud/paas/activation/v1/parallel-usertasks-process.bpmn20.xml")
				.deploy();

		// start a new process instance
		ProcessInstance pi = runtimeService
				.startProcessInstanceByKey("parallel-usertasks-process");

		// fetch active user tasks
		TaskQuery query = taskService.createTaskQuery()
				.processInstanceId(pi.getId()).orderByTaskName().asc();

		// assert that user tasks list equals 2
		List<Task> tasks = query.list();
		assertEquals(2, tasks.size());

		// assert that one user task is 'Receive Payment'
		Task task1 = tasks.get(0);
		assertEquals("no task 'Receive Payment' found", "Receive Payment",
				task1.getName());

		// assert that another user task is 'Ship Order'
		Task task2 = tasks.get(1);
		assertEquals("no task 'Ship Order' found", "Ship Order",
				task2.getName());

		// complete 'Receive Payment' user task
		taskService.complete(task1.getId());

		// complete 'Ship Order' user task
		taskService.complete(task2.getId());

		// assert that user tasks list equals 1
		tasks = query.list();
		assertEquals(1, tasks.size());

		// assert that one user task is 'Archive Order'
		Task task3 = tasks.get(0);
		assertEquals("no task 'Archive Order' found", "Archive Order",
				task3.getName());

		// complete 'Archive Order' user task
		taskService.complete(task3.getId());

		// assert that process is ended
		assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).list().size());

	}
}
