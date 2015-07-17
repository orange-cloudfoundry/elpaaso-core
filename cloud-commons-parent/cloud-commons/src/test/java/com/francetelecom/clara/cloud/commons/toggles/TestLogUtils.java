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
package com.francetelecom.clara.cloud.commons.toggles;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;

public class TestLogUtils {

	/**
	 * add a mock appender on the logging system so that loga can be verified
	 * @return the mockAppender instance
	 */
	public static Appender<ILoggingEvent> addMockAppenderLog() {
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)  LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
		final Appender<ILoggingEvent> mockAppender = mock(Appender.class);
		when(mockAppender.getName()).thenReturn("MOCK");
		root.addAppender(mockAppender);
		return mockAppender;
	}

	/**
	 * matcher to verify a log message
	 * @param level expected log level
	 * @param messageContent expected message that should be contained in the log message 
	 * @return
	 */
	public static ILoggingEvent logEventMatches(final Level level, final String messageContent) {
		@SuppressWarnings({ "unchecked", "rawtypes" })
		Object matcher = argThat(new ArgumentMatcher() {
			@Override
			public boolean matches(final Object argument) {
				LoggingEvent loggingEvent = (LoggingEvent)argument;
				return (loggingEvent.getLevel().equals(level) && loggingEvent.getFormattedMessage().contains(messageContent));
			}
		});
		return (ILoggingEvent)matcher;
		
	}

}
