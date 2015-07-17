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
package com.francetelecom.clara.cloud.paas.activation.v1.async.listener.amqp;

import java.util.UUID;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;

import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.paas.activation.v1.async.exception.UnexpectedException;

public class UnexpectedErrorMessageListener implements MessageListener {

	/**
	 * helper that simplifies synchronous amqp access code to 'error' queue
	 */
	private AmqpTemplate amqpErrorTemplate;

	private static final Logger log = LoggerFactory.getLogger(UnexpectedErrorMessageListener.class);

	@Override
	public void onMessage(Message message) {

		log.debug("message received : " + message);
		TaskStatus payload = (TaskStatus) SerializationUtils.deserialize(message.getBody());
		String communicationId = new String(message.getMessageProperties().getCorrelationId());

		try {
			log.error("unexpected error occured during task polling process; msg details : " + payload);
		} catch (Exception e) {
			log.error("Exception occured during task polling process; details : " + e);
		}

		MessageProperties props = MessagePropertiesBuilder.newInstance().setContentType(MessageProperties.CONTENT_TYPE_SERIALIZED_OBJECT)
				.setMessageId(UUID.randomUUID().toString()).setCorrelationId(communicationId.getBytes()).build();
		Message error = MessageBuilder
				.withBody(SerializationUtils.serialize(new UnexpectedException("Unexpected error with content: " + payload)))
				.andProperties(props).build();
		amqpErrorTemplate.send(error);
		

	}

	public void setAmqpErrorTemplate(AmqpTemplate amqpErrorTemplate) {
		this.amqpErrorTemplate = amqpErrorTemplate;
	}

}
