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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static com.francetelecom.clara.cloud.commons.toggles.TestLogUtils.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;


@RunWith(MockitoJUnitRunner.class)
public class PaasFeatureUserProviderTest {

	@InjectMocks
	PaasFeatureUserProvider userProvider = new PaasFeatureUserProvider();
	
	@Mock
	UserProvider mainUserProvider;
	
	@Test
	public void getUser_should_return_a_default_non_admin_user_when_main_provider_returns_null_user() {
		// Given
		doReturn(null).when(mainUserProvider).getCurrentUser();
		
		// When
		FeatureUser user = userProvider.getCurrentUser();
		
		// Then
		assertEquals("unknown", user.getName());
		assertEquals(false, user.isFeatureAdmin());
	}
	
	@Test
	public void getUser_should_return_a_default_non_admin_user_when_main_provider_throws_exception() {
		// Given
		doThrow(new RuntimeException("unable to get current user")).when(mainUserProvider).getCurrentUser();
		
		// When
		FeatureUser user = userProvider.getCurrentUser();
		
		// Then
		assertEquals("unknown", user.getName());
		assertEquals(false, user.isFeatureAdmin());
	}
	
	@Test
	public void getUser_should_log_an_error_when_main_provider_throws_exception() {
		Appender<ILoggingEvent> logAppender = addMockAppenderLog();
		
		// Given
		doThrow(new RuntimeException("unable to get current user")).when(mainUserProvider).getCurrentUser();
		
		// When
		FeatureUser user = userProvider.getCurrentUser();

		// Then
		verify(logAppender).doAppend(logEventMatches(Level.ERROR, ""));
	}
	
	@Test
	public void getUser_should_return_user_returned_by_main_provider_when_not_null() {
		// Given
		FeatureUser currentUser = new SimpleFeatureUser("a user", true);
		doReturn(currentUser).when(mainUserProvider).getCurrentUser();
		
		// When
		FeatureUser user = userProvider.getCurrentUser();
		
		// Then
		assertEquals(currentUser, user);
	}
	

	

}
