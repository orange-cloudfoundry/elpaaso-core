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
import javax.jms.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import com.francetelecom.clara.cloud.paas.activation.v1.async.exception.UnexpectedException;
import com.francetelecom.clara.cloud.paas.activation.v1.async.message.BasicMessage;
import com.francetelecom.clara.cloud.paas.activation.v1.async.message.ErrorMessageBuilder;
import com.francetelecom.clara.cloud.paas.activation.v1.async.message.SimpleBasicMessage;

public class UnexpectedErrorMessageListener implements MessageListener {

	/**
	 * helper that simplifies synchronous JMS access code to 'error' queue
	 */
	private JmsTemplate jmsErrorTemplate;

	private static final Logger log = LoggerFactory.getLogger(UnexpectedErrorMessageListener.class);

	@Override
	public void onMessage(Message message) {

		final BasicMessage msg = new SimpleBasicMessage((ObjectMessage) message);

		try {
			log.error("unexpected error occured during task polling process; msg details : " + msg.getObject());
		} catch (JMSException e) {
			log.error("JMSException occured during task polling process; details : " + e);
		}

		jmsErrorTemplate.send(new MessageCreator() {

			@Override
			public Message createMessage(Session session) throws JMSException {
                String cause = msg.getStringProperty("JMS_JORAM_ERRORCAUSE_1");
                if (cause != null) {
                    return new ErrorMessageBuilder(session, new UnexpectedException(cause + " with content: " + msg.getObject()), msg.getCommunicationId()).build();
                } else {
                    return new ErrorMessageBuilder(session, new UnexpectedException("Unexpected error with content: " + msg.getObject()), msg.getCommunicationId()).build();
                }
			}
		});

	}

	public void setJmsErrorTemplate(JmsTemplate jmsErrorTemplate) {
		this.jmsErrorTemplate = jmsErrorTemplate;
	}
}
