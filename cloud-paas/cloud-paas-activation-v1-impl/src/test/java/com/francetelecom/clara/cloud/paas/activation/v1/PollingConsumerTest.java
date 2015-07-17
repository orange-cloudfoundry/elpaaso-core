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
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.commons.tasks.PollTaskStateInterface;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class PollingConsumerTest {

	@Autowired
	RuntimeService runtimeService;

	@Autowired
	RepositoryService repositoryService;

	@Autowired
	ManagementService managementService;

	// myBackend is a mock
	@Autowired
	PollTaskStateInterface<TaskStatus> myBackend;

	@Before
	public void setup() {
		// deploy the process
		repositoryService
				.createDeployment()
				.addClasspathResource(
						"com/francetelecom/clara/cloud/paas/activation/v1/polling-consumer.bpmn20.xml")
				.deploy();
	}

	@After
	public void teardown() {
		// reset mock
		reset(myBackend);
	}

	@Test
	@Transactional
	public void processExecutionWaitsBeforePollingBackendTest()
			throws InterruptedException {

		/* GIVEN */
		// Given polled task is not completed yet
		TaskStatus isNotCompleteTaskStatus = new TaskStatus(0);
		isNotCompleteTaskStatus.setTaskStatus(TaskStatusEnum.STARTED);
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("taskStatus", isNotCompleteTaskStatus);
		// and max polling attempts is 2
		variables.put("maxAttempts", 2);
		// and delay between 2 polling attempts is PT5M
		variables.put("backOffPeriod", "PT5M");

		/* WHEN */
		// when a new polling process instance is started
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(
				"polling-consumer", variables);

		/* THEN */
		// then polling process instance should be pending and should wait for
		// a timer event to be thrown
		List<Job> jobs = managementService.createJobQuery().list();
		assertNotNull("job list should not be null!!!!", jobs);
		assertEquals("there should be 1 pending job !!!!", 1, jobs.size());

	}

	@Test
	@Transactional
	public void taskIsCompleteWhenFirstPollTest() {

		/* GIVEN */
		// Given polled task is completed
		TaskStatus isCompleteTaskStatus = new TaskStatus(0);
		isCompleteTaskStatus.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		given(myBackend.giveCurrentTaskStatus(any(TaskStatus.class)))
				.willReturn(isCompleteTaskStatus);
		// and a process execution is waiting for a timer event to be thrown
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("taskStatus", new TaskStatus(0));
		// and max polling attempts is 2
		variables.put("maxAttempts", 2);
		// and delay between 2 polling attempts is PT5M
		variables.put("backOffPeriod", "PT5M");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(
				"polling-consumer", variables);

		/* WHEN */
		// when polling process execution catches timer event
		List<Job> jobs = managementService.createJobQuery().list();
		managementService.executeJob(jobs.get(0).getId());

		/* THEN */
		// then myBackend.giveCurrentTaskStatus(...) must have been called once
		verify(myBackend, times(1))
				.giveCurrentTaskStatus(any(TaskStatus.class));
		// and no job must be pending
		jobs = managementService.createJobQuery().list();
		assertEquals("there should be 0 pending job !!!!", 0, jobs.size());

	}

	@Test
	@Transactional
	public void taskIsCompleteWhenSecondPollTest() {

		/* GIVEN */
		// Given polled task is no completed after first poll and is completed
		// after second poll
		TaskStatus isCompleteTaskStatus = new TaskStatus(0);
		isCompleteTaskStatus.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		TaskStatus isNotCompleteTaskStatus = new TaskStatus(0);
		isNotCompleteTaskStatus.setTaskStatus(TaskStatusEnum.STARTED);
		given(myBackend.giveCurrentTaskStatus(any(TaskStatus.class)))
				.willReturn(isNotCompleteTaskStatus).willReturn(
						isCompleteTaskStatus);
		// and a process execution is waiting for a timer event to be thrown
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("taskStatus", new TaskStatus(0));
		// and max polling attempts is 2
		variables.put("maxAttempts", 2);
		// and delay between 2 polling attempts is PT5M
		variables.put("backOffPeriod", "PT5M");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(
				"polling-consumer", variables);

		/* WHEN */
		// when polling process execution catches timer event
		List<Job> jobs = managementService.createJobQuery().list();
		managementService.executeJob(jobs.get(0).getId());
		// and polling process execution catches timer event
		jobs = managementService.createJobQuery().list();
		managementService.executeJob(jobs.get(0).getId());

		/* THEN */
		// assert that myBackend.giveCurrentTaskStatus(...) has been called
		// twice
		verify(myBackend, times(2))
				.giveCurrentTaskStatus(any(TaskStatus.class));
		// assert that no job is pending
		jobs = managementService.createJobQuery().list();
		assertEquals("there should be 0 pending job !!!!", 0, jobs.size());

	}

	@Test
	@Transactional
	public void taskIsNeverCompleteTest() {

		/* GIVEN */
		// Given polled task is never completed
		TaskStatus isNeverCompleteTaskStatus = new TaskStatus(0);
		isNeverCompleteTaskStatus.setTaskStatus(TaskStatusEnum.STARTED);
		given(myBackend.giveCurrentTaskStatus(any(TaskStatus.class)))
				.willReturn(isNeverCompleteTaskStatus);
		// and a process execution is waiting for a timer event to be thrown
		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("taskStatus", new TaskStatus(0));
		variables.put("maxAttempts", 2);
		// and delay between 2 polling attempts is PT5M
		variables.put("backOffPeriod", "PT5M");
		ProcessInstance pi = runtimeService.startProcessInstanceByKey(
				"polling-consumer", variables);

		/* WHEN */
		// when polling process execution catches timer event
		List<Job> jobs = managementService.createJobQuery().list();
		managementService.executeJob(jobs.get(0).getId());
		// and polling process execution catches timer event
		jobs = managementService.createJobQuery().list();
		managementService.executeJob(jobs.get(0).getId());

		/* THEN */
		// assert that myBackend.giveCurrentTaskStatus(...) has been called
		// twice
		verify(myBackend, times(2))
				.giveCurrentTaskStatus(any(TaskStatus.class));
		// assert that no job is pending
		jobs = managementService.createJobQuery().list();
		assertEquals("there should be 0 pending job !!!!", 0, jobs.size());

	}
}
