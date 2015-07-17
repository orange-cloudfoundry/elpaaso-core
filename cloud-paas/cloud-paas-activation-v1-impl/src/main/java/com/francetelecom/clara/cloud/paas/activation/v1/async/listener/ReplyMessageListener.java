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
package com.francetelecom.clara.cloud.paas.activation.v1.async.listener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.paas.activation.v1.async.TaskHandler;
import com.francetelecom.clara.cloud.paas.activation.v1.async.TaskHandlerCallback;
import com.francetelecom.clara.cloud.paas.activation.v1.async.message.RetryableMessage;

public class ReplyMessageListener implements MessageListener {

	private TaskHandler<TaskStatus, TaskHandlerCallback<TaskStatus>> taskHandler;

	private static final Logger log = LoggerFactory.getLogger(ReplyMessageListener.class);

	@Override
	public void onMessage(Message message) {

		try {
			RetryableMessage msg = new RetryableMessage((ObjectMessage) message);
			TaskStatus payload = (TaskStatus) msg.getObject();
			String communicationId = msg.getCommunicationId();
			taskHandler.onTaskComplete(payload, communicationId);
		} catch (JMSException ex) {
			log.error("JMSException occured during task polling process; details : " + ex);
			throw new RuntimeException(ex);
		}

	}

	public void setTaskHandler(TaskHandler<TaskStatus, TaskHandlerCallback<TaskStatus>> taskHandler) {
		this.taskHandler = taskHandler;
	}

}
