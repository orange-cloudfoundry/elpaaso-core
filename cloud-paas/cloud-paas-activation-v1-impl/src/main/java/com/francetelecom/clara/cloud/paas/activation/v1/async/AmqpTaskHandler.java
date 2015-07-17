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

import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.paas.activation.v1.async.exception.MaxRetryCountExceededException;
import com.francetelecom.clara.cloud.paas.activation.v1.async.policy.RetryPolicy;
import com.francetelecom.clara.cloud.paas.activation.v1.async.policy.SimpleRetryPolicy;

public class AmqpTaskHandler<R, C extends TaskHandlerCallback<R>> implements TaskHandler<R, C> {

	/**
	 * helper that simplifies synchronous AMQP access code to 'request' queue
	 */
	private AmqpTemplate amqpRequestTemplate;

	/**
	 * helper that simplifies synchronous AMQP access code to 'reply' queue
	 */
	private AmqpTemplate amqpReplyTemplate;

	/**
	 * helper that simplifies synchronous AMQP access code to 'error' queue
	 */
	private AmqpTemplate amqpErrorTemplate;

	/**
	 * injected callback implementation
	 */
	private TaskHandlerCallback<R> taskHandlerCallback;

	/**
	 * retry policy
	 */
	private RetryPolicy retryPolicy = new SimpleRetryPolicy();

	private static final Logger log = LoggerFactory.getLogger(AmqpTaskHandler.class);

	@Override
	public TaskStatus handleRequest(R request, final String communicationId) {
		log.trace("(0) handleRequest {}", request.toString());
		// callback delegation
		final TaskStatus taskStatus = taskHandlerCallback.handleRequest(request);

		if (taskStatus.isComplete()) {
			log.trace("(1) task is complete. no need to poll. Sends task Status to reply queue");
			MessageProperties props;
			props = MessagePropertiesBuilder.newInstance().setContentType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT).setMessageId(UUID.randomUUID().toString())
					.setCorrelationId(communicationId.getBytes()).build();
			Message message = MessageBuilder.withBody(SerializationUtils.serialize(taskStatus)).andProperties(props).build();
			amqpReplyTemplate.send(message);
			log.trace("(2) message sent to reply queue");
		} else {
			log.trace("(1) handleRequest status : {} send in amqp queue a 'retry' request used to poll backend service for", taskStatus.toString());
			MessageProperties props;
			props = MessagePropertiesBuilder.newInstance().setContentType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT).setMessageId(UUID.randomUUID().toString())
					.setCorrelationId(communicationId.getBytes()).setHeader("retryCount", 1).build();
			Message message = MessageBuilder.withBody(SerializationUtils.serialize(taskStatus)).andProperties(props).build();
			amqpRequestTemplate.send(message);
			log.trace("(2) handleRequest send amqp message ended");

		}

		return taskStatus;
	}

	@Override
	public void onTaskPolled(TaskStatus taskStatus, final RetryContext retryContext, final String communicationId) {
		log.trace("onTaskPolled taskStatus={}", taskStatus.toString());
		// callback delegation
		final TaskStatus t = taskHandlerCallback.onTaskPolled(taskStatus);
		log.trace("[after] onTaskPolled taskStatus={}", t.toString());
		if (t.isComplete()) {
			log.trace("task is complete. no need to keep polling. Sends task Status to reply queue");
			MessageProperties props;
			props = MessagePropertiesBuilder.newInstance().setContentType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT).setMessageId(UUID.randomUUID().toString())
					.setCorrelationId(communicationId.getBytes()).build();
			Message message = MessageBuilder.withBody(SerializationUtils.serialize(t)).andProperties(props).build();
			amqpReplyTemplate.send(message);
		}

		else {
			if (!retryPolicy.canRetry(retryContext.getRetryCount())) {
				// max retry count exceeded. no need to keep polling. Sends
				// Exception to error queue
				log.warn("Max retry count reached: " + retryPolicy.getMaxAttempts());
				MessageProperties props = MessagePropertiesBuilder.newInstance().setContentType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT)
						.setMessageId(UUID.randomUUID().toString()).setCorrelationId(communicationId.getBytes()).build();
				Message message = MessageBuilder
						.withBody(SerializationUtils.serialize(new MaxRetryCountExceededException("Max retry count reached : " + retryPolicy.getMaxAttempts())))
						.andProperties(props).build();
				amqpErrorTemplate.send(message);
			} else {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					log.warn("Thread sleeping before sending message back to Request queue has been interrupted.");
				}
				log.trace("task is not complete. Must keep polling. Sends task Status to request queue");
				MessageProperties props = MessagePropertiesBuilder.newInstance().setContentType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT)
						.setMessageId(UUID.randomUUID().toString()).setCorrelationId(communicationId.getBytes()).setHeader("retryCount", retryContext.getRetryCount() + 1).build();
				Message message = MessageBuilder.withBody(SerializationUtils.serialize(t)).andProperties(props).build();
				amqpRequestTemplate.send(message);

			}
		}
	}

	@Override
	public void onTaskComplete(TaskStatus taskStatus, String communicationId) {
		log.trace("onTaskComplete {}", taskStatus.toString());
		// callback delegation
		taskHandlerCallback.onTaskComplete(taskStatus, communicationId);
	}

	@Override
	public void onTaskFailure(Throwable throwable, String communicationId) {
		log.trace("onTaskFailure {}", throwable.getMessage());
		// callback delegation
		taskHandlerCallback.onFailure(throwable, communicationId);
	}

	public void setTaskHandlerCallback(TaskHandlerCallback<R> taskHandlerCallback) {
		this.taskHandlerCallback = taskHandlerCallback;
	}

	public void setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	public RetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

	public void setAmqpRequestTemplate(AmqpTemplate rabbitRequestTemplate) {
		this.amqpRequestTemplate = rabbitRequestTemplate;
	}

	public void setAmqpReplyTemplate(AmqpTemplate rabbitReplyTemplate) {
		this.amqpReplyTemplate = rabbitReplyTemplate;
	}

	public void setAmqpErrorTemplate(AmqpTemplate rabbitErrorTemplate) {
		this.amqpErrorTemplate = rabbitErrorTemplate;
	}

}
