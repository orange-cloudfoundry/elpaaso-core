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
package com.francetelecom.clara.cloud.paas.activation.v1.async;

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.paas.activation.v1.async.exception.MaxRetryCountExceededException;
import com.francetelecom.clara.cloud.paas.activation.v1.async.exception.UnexpectedException;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@IfProfileValue(name = "test-groups", values = {"rabbitmq"})
public class AmqpTaskHandlerIT {

	// TaskHandlerCallback mock -> see spring configuration file
	@Autowired
	TaskHandlerCallback<String> myBackendTaskHandlerCallback;

	@Autowired
	TaskHandler<String, TaskHandlerCallback<String>> myBackendSimpleTaskHandler;

	private static final Logger logger = LoggerFactory.getLogger(AmqpTaskHandlerIT.class);

	@After
	public void teardown() {
		// reset mock
		Mockito.reset(myBackendTaskHandlerCallback);
	}

	@Test
	public void shouldPollOnce() throws Exception {

		/* GIVEN */
		// given myBackend is requested for a task
		TaskStatus started = new TaskStatus();
		started.setTaskStatus(TaskStatusEnum.STARTED);
		Mockito.when(myBackendTaskHandlerCallback.handleRequest(Mockito.anyString())).thenReturn(started);
		// and my Backend will complete this before first poll
		TaskStatus finishedOK = new TaskStatus();
		finishedOK.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		Mockito.when(myBackendTaskHandlerCallback.onTaskPolled(Mockito.any(TaskStatus.class))).thenReturn(finishedOK);

		/* WHEN */
		// when we send a request to myBackendSimpleTaskHandler
		myBackendSimpleTaskHandler.handleRequest("1234", "99");
		// and we wait for a delay of 10000 ms (because of asynchrony)
		CountDownLatch latch = new CountDownLatch(1);
		latch.await(10000, TimeUnit.MILLISECONDS);

		/* THEN */
		// it should poll once
		Mockito.verify(myBackendTaskHandlerCallback, Mockito.times(1)).onTaskPolled(Mockito.any(TaskStatus.class));

		// and return with status complete
		Mockito.verify(myBackendTaskHandlerCallback, Mockito.times(1)).onTaskComplete(Mockito.any(TaskStatus.class), Mockito.eq("99"));

	}

	@Test
	public void shouldPollTwice() throws Exception {

		/* GIVEN */
		// given myBackend is requested for a task
		TaskStatus started = new TaskStatus();
		started.setTaskStatus(TaskStatusEnum.STARTED);
		Mockito.when(myBackendTaskHandlerCallback.handleRequest(Mockito.anyString())).thenReturn(started);
		// and my Backend will complete this task after first poll
		TaskStatus finishedOK = new TaskStatus();
		finishedOK.setTaskStatus(TaskStatusEnum.FINISHED_OK);
		Mockito.when(myBackendTaskHandlerCallback.onTaskPolled(Mockito.any(TaskStatus.class))).thenReturn(started).thenReturn(finishedOK);

		/* WHEN */
		// when we send a request to myBackendSimpleTaskHandler
		myBackendSimpleTaskHandler.handleRequest("1234", "99");
		// and we wait for a delay of 10000 ms (because of asynchrony)
		CountDownLatch latch = new CountDownLatch(1);
		latch.await(10000, TimeUnit.MILLISECONDS);

		/* THEN */
		// it should poll twice
		Mockito.verify(myBackendTaskHandlerCallback, Mockito.times(2)).onTaskPolled(Mockito.any(TaskStatus.class));

		// and return with status complete
		Mockito.verify(myBackendTaskHandlerCallback, Mockito.times(1)).onTaskComplete(Mockito.any(TaskStatus.class), Mockito.eq("99"));

	}

	@Test
	public void shouldExceedMaxRetryCount() throws Exception {

		/* GIVEN */
		// given myBackend is requested for a task
		TaskStatus started = new TaskStatus();
		started.setTaskStatus(TaskStatusEnum.STARTED);
		Mockito.when(myBackendTaskHandlerCallback.handleRequest(Mockito.anyString())).thenReturn(started);
		// and my Backend will never complete this task
		Mockito.when(myBackendTaskHandlerCallback.onTaskPolled(Mockito.any(TaskStatus.class))).thenReturn(started);

		/* WHEN */
		// when we send a request to myBackendSimpleTaskHandler
		myBackendSimpleTaskHandler.handleRequest("1234", "99");

		/* THEN */
		// after a delay of 10000 ms (because of asynchrony)
		CountDownLatch latch = new CountDownLatch(1);
		latch.await(10000, TimeUnit.MILLISECONDS);

		// it should poll twice
		Mockito.verify(myBackendTaskHandlerCallback, Mockito.times(2)).onTaskPolled(Mockito.any(TaskStatus.class));

		// and returns with a failure MaxRetryCountExceededException
		Mockito.verify(myBackendTaskHandlerCallback, Mockito.times(1)).onFailure(Mockito.any(MaxRetryCountExceededException.class), Mockito.eq("99"));

	}

	@Test
	public void shouldRaiseAnUnexpectedException() throws Exception {

		/* GIVEN */
		// given myBackend is requested for a task
		TaskStatus started = new TaskStatus();
		started.setTaskStatus(TaskStatusEnum.STARTED);
		Mockito.when(myBackendTaskHandlerCallback.handleRequest(Mockito.anyString())).thenReturn(started);
		// and task handling will throw an exception
		Mockito.when(myBackendTaskHandlerCallback.onTaskPolled(Mockito.any(TaskStatus.class))).thenThrow(new RuntimeException());

		/* WHEN */
		// when we send a request to myBackendSimpleTaskHandler
		myBackendSimpleTaskHandler.handleRequest("1234", "99");
		// after a delay of 10000 ms (because of asynchrony)
		CountDownLatch latch = new CountDownLatch(1);
		latch.await(10000, TimeUnit.MILLISECONDS);

		/* THEN */
		// it should poll twice
		Mockito.verify(myBackendTaskHandlerCallback, Mockito.times(2)).onTaskPolled(Mockito.any(TaskStatus.class));

		// and returns with a failure UnexpectedException ....
		Mockito.verify(myBackendTaskHandlerCallback, Mockito.times(1)).onFailure(Mockito.any(UnexpectedException.class), Mockito.eq("99"));

	}

}
