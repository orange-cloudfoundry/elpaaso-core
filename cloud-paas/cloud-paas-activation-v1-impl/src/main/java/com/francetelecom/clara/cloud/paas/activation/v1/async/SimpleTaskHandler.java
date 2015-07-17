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
import com.francetelecom.clara.cloud.paas.activation.v1.async.exception.MaxRetryCountExceededException;
import com.francetelecom.clara.cloud.paas.activation.v1.async.message.ErrorMessageBuilder;
import com.francetelecom.clara.cloud.paas.activation.v1.async.message.ReplyMessageBuilder;
import com.francetelecom.clara.cloud.paas.activation.v1.async.message.RetryableMessageBuilder;
import com.francetelecom.clara.cloud.paas.activation.v1.async.policy.RetryPolicy;
import com.francetelecom.clara.cloud.paas.activation.v1.async.policy.SimpleRetryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

public class SimpleTaskHandler<R, C extends TaskHandlerCallback<R>> implements TaskHandler<R, C> {

	/**
	 * helper that simplifies synchronous JMS access code to 'request' queue
	 */
	private JmsTemplate jmsRequestTemplate;

	/**
	 * helper that simplifies synchronous JMS access code to 'reply' queue
	 */
	private JmsTemplate jmsReplyTemplate;

	/**
	 * helper that simplifies synchronous JMS access code to 'error' queue
	 */
	private JmsTemplate jmsErrorTemplate;

	/**
	 * injected callback implementation
	 */
	private TaskHandlerCallback<R> taskHandlerCallback;

	/**
	 * retry policy
	 */
	private RetryPolicy retryPolicy = new SimpleRetryPolicy();

	private static final Logger log = LoggerFactory.getLogger(SimpleTaskHandler.class);

	@Override
	public TaskStatus handleRequest(R request, final String communicationId) {
		log.trace("(0) handleRequest {}", request.toString());
		// callback delegation
		final TaskStatus taskStatus = taskHandlerCallback.handleRequest(request);

		if (taskStatus.isComplete()) {
			log.trace("task is complete. no need to poll. Sends task Status to reply queue");
			jmsReplyTemplate.send(new MessageCreator() {

				@Override
				public Message createMessage(Session session) throws JMSException {
					log.trace("taskPolled(replyTemplate) transacted={},acknowledgeMode={}", session.getTransacted(), session.getAcknowledgeMode());
					return new ReplyMessageBuilder(session, taskStatus, communicationId).build();
				}
			});
		} else {
			log.trace("(1) handleRequest status : {} send in JMS queue a 'retry' request used to poll backend service for", taskStatus.toString());
			// reply
			jmsRequestTemplate.send(new MessageCreator() {

				@Override
				public Message createMessage(Session session) throws JMSException {
					log.trace("handleRequest(requestTemplate) transacted={},acknowledgeMode={}", session.getTransacted(),
							session.getAcknowledgeMode());
					return new RetryableMessageBuilder(session, taskStatus, communicationId).build();
				}
			});
			log.trace("(2) handleRequest send JMS ended");

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
			jmsReplyTemplate.send(new MessageCreator() {

				@Override
				public Message createMessage(Session session) throws JMSException {
					log.trace("taskPolled(replyTemplate) transacted={},acknowledgeMode={}", session.getTransacted(), session.getAcknowledgeMode());
					return new ReplyMessageBuilder(session, t, communicationId).build();
				}
			});

		}

		else {

			if (!retryPolicy.canRetry(retryContext.getRetryCount())) {
				// max retry count exceeded. no need to keep polling. Sends
				// Exception to error queue
				log.warn("Max retry count reached: " + retryPolicy.getMaxAttempts());
				jmsErrorTemplate.send(new MessageCreator() {

					@Override
					public Message createMessage(Session session) throws JMSException {
						log.trace("taskPolled(errorTemplate) transacted={},acknowledgeMode={}", session.getTransacted(), session.getAcknowledgeMode());
						return new ErrorMessageBuilder(session, new MaxRetryCountExceededException("Max retry count reached : "
								+ retryPolicy.getMaxAttempts()), communicationId).build();
					}
				});

			} else {

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					log.warn("Thread sleeping before sending message back to Request queue has been interrupted.");
				}

				log.trace("task is not complete. Must keep polling. Sends task Status to request queue");
				jmsRequestTemplate.send(new MessageCreator() {

					@Override
					public Message createMessage(Session session) throws JMSException {
						log.trace("taskPolled(requestTemplate) transacted={},acknowledgeMode={}", session.getTransacted(),
								session.getAcknowledgeMode());
						return new RetryableMessageBuilder(session, t, communicationId).withRetryContext(retryContext).build();
					}
				});

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

	public void setJmsRequestTemplate(JmsTemplate jmsRequestTemplate) {
		this.jmsRequestTemplate = jmsRequestTemplate;
	}

	public void setJmsReplyTemplate(JmsTemplate jmsReplyTemplate) {
		this.jmsReplyTemplate = jmsReplyTemplate;
	}

	public void setTaskHandlerCallback(TaskHandlerCallback<R> taskHandlerCallback) {
		this.taskHandlerCallback = taskHandlerCallback;
	}

	public void setJmsErrorTemplate(JmsTemplate jmsErrorTemplate) {
		this.jmsErrorTemplate = jmsErrorTemplate;
	}

	public void setRetryPolicy(RetryPolicy retryPolicy) {
		this.retryPolicy = retryPolicy;
	}

	public RetryPolicy getRetryPolicy() {
		return retryPolicy;
	}

}
