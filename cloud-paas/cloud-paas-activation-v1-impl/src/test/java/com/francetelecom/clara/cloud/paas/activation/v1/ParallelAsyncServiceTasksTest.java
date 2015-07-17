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
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.IdentityService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ParallelAsyncServiceTasksTest {

	@Autowired
	ProcessEngine processEngine;

	@Autowired
	RuntimeService runtimeService;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	IdentityService identityService;

	@Test
	public void test() throws InterruptedException {

		// TODO test en genuine BPMN 2.0 process

		// deploy the process
		repositoryService.createDeployment().addClasspathResource("com/francetelecom/clara/cloud/paas/activation/v1/parallel-async-servicetasks-process.bpmn20.xml").deploy();

		// process instance varablews settings
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("aVar", "a value");
		variables.put("anotherVar", "another value");

		// start a new process instance
		ProcessInstance pi = runtimeService.startProcessInstanceByKey("parallel-async-servicetasks-process", variables);

		// fetch active executions
		List<Execution> executions = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).list();

		assertEquals("there should be 3 pending executions", 3, executions.size());

		// one is for the fork

		// fetch active execution
		Execution execution2 = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("aServiceWaitState").singleResult();
		// assert that a 'aServiceWaitState' execution exists
		assertNotNull(execution2);

		// fetch active execution
		Execution execution3 = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("anotherServiceWaitState").singleResult();
		// assert that a 'anotherServiceWaitState' execution exists
		assertNotNull(execution3);

		// reactivate 'aServiceWaitState' execution
		runtimeService.signal(execution2.getId());

		// reactivate 'aServiceWaitState' execution
		runtimeService.signal(execution3.getId());

		// fetch active execution
		Execution lastexecution = runtimeService.createExecutionQuery().processInstanceId(pi.getId()).activityId("waitState").singleResult();
		// assert that a 'waitState' execution exists
		assertNotNull(lastexecution);

		// reactivate 'waitState' execution
		runtimeService.signal(lastexecution.getId());
		
		// assert that process is ended
		assertEquals(0, runtimeService.createExecutionQuery().processInstanceId(pi.getId()).list().size());

	}
}
