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

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.fest.assertions.Assertions.assertThat;

import static com.francetelecom.clara.cloud.commons.toggles.TestLogUtils.*;

import java.util.Arrays;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.repository.FeatureState;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;


public class PaasJndiStateRepositoryTest {

	private static enum MyFeature implements Feature {
        FEATURE1, FEATURE2, FEATURE3, FEATURE4;
        @Override
        public boolean isActive() {
            return FeatureContext.getFeatureManager().isActive(this);
        }
    }
	
	PaasJndiStateRepository repository = spy(new PaasJndiStateRepository());
	InitialContext context = mock(InitialContext.class);
	
	@Before
	public void setup() throws Exception {
		// Stub jndi initial context creation
		 doReturn(context).when(repository).getInitialContext();
	}
	
	@Test
	public void getFeatureState_should_fetch_state_from_jndi_when_feature_is_defined_in_jndi() throws Exception{
		 // Given
		 when(context.lookup("FEATURE1")).thenReturn("false");
		 when(context.lookup("FEATURE2")).thenReturn("true");
		 
		 // When
		 FeatureState feature1 = repository.getFeatureState(MyFeature.FEATURE1);
		 FeatureState feature2 = repository.getFeatureState(MyFeature.FEATURE2);
		 
		 // Then
		 assertThat(feature1.isEnabled()).isFalse();
		 assertThat(feature2.isEnabled()).isTrue();
	}
	
	@Test
	public void getFeatureState_should_fetch_users_from_jndi_when_defined_in_jndi() throws Exception{
		 // Given
		 when(context.lookup("FEATURE1")).thenReturn("true");
		 when(context.lookup("FEATURE1.users")).thenReturn("user1, user2, user3");
		 
		 // When
		 FeatureState feature1 = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 assertThat(feature1.getUsers()).containsOnly("user1","user2","user3");
	}
	
	@Test
	public void getFeatureState_should_return_empty_users_list_when_users_list_is_empty() throws Exception{
		 // Given
		 when(context.lookup("FEATURE1")).thenReturn("true");
		 when(context.lookup("FEATURE1.users")).thenReturn("");
		 
		 // When
		 FeatureState feature1 = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 assertThat(feature1.getUsers()).isEmpty();
	}
	
	@Test
	public void getFeatureState_should_ignore_spaces_around_comma_in_user_list() throws Exception{
		 // Given
		 when(context.lookup("FEATURE1")).thenReturn("true");
		 when(context.lookup("FEATURE1.users")).thenReturn("user1 name , user2 name , user3 name ");
		 
		 // When
		 FeatureState feature1 = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 assertThat(feature1.getUsers()).containsOnly("user1 name","user2 name","user3 name");
	}
	
	@Test
	public void getFeatureState_should_ignore_last_comma_in_user_list() throws Exception{
		 // Given
		 when(context.lookup("FEATURE1")).thenReturn("true");
		 when(context.lookup("FEATURE1.users")).thenReturn("user1, user2, user3,");
		 
		 // When
		 FeatureState feature1 = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 assertThat(feature1.getUsers()).containsOnly("user1","user2","user3");
	}
	
	@Test
	public void getFeatureState_should_return_null_when_feature_is_not_defined_in_jndi() throws Exception{
		 // Given
		 doThrow(new NameNotFoundException("jndi test error")).when(context).lookup(anyString());
		 
		 // When
		 FeatureState feature = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 assertThat(feature).isNull();
	}
	
	@Test
	public void getFeatureState_should_log_a_warn_when_feature_is_not_defined_in_jndi() throws Exception{
		 final Appender<ILoggingEvent> mockAppender = addMockAppenderLog();

		 // Given
		 doThrow(new NameNotFoundException("jndi test error")).when(context).lookup(anyString());
		 
		 // When
		 FeatureState feature = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 verify(mockAppender).doAppend(logEventMatches(Level.WARN, "FEATURE1"));
	}
	
	@Test
	public void getFeatureState_should_return_null_when_value_is_not_a_string() throws Exception{
		 // Given
		 when(context.lookup("FEATURE1")).thenReturn(new Object());
		 
		 // When
		 FeatureState feature = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 assertThat(feature).isNull();
	}
	
	@Test
	public void getFeatureState_should_return_null_when_value_is_not_true_or_false() throws Exception{
		 // Given
		 when(context.lookup("FEATURE1")).thenReturn("invalid value");
		 
		 // When
		 FeatureState feature = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 assertThat(feature).isNull();
	}
	
	@Test
	public void getFeatureState_should_return_null_when_name_not_found_exception_is_wrapped() throws Exception{
		 final Appender<ILoggingEvent> mockAppender = TestLogUtils.addMockAppenderLog();
		 
		 // Given
		 doReturn(context).when(repository).getInitialContext();
		 // stub expected behavior of lookup method
		 NamingException exception = new NamingException("jndi exception");
		 exception.initCause(new NameNotFoundException("invalid name"));
		 doThrow(exception).when(context).lookup(anyString());
		 
		 // When
		 FeatureState feature = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 assertThat(feature).isNull();
	}
	
	@Test
	public void getFeatureState_should_log_a_warn_when_name_not_found_exception_is_wrapped() throws Exception{
		 final Appender<ILoggingEvent> mockAppender = addMockAppenderLog();
		 
		 // Given
		 doReturn(context).when(repository).getInitialContext();
		 // stub expected behavior of lookup method
		 NamingException exception = new NamingException("jndi exception");
		 exception.initCause(new NameNotFoundException("invalid name"));
		 doThrow(exception).when(context).lookup(anyString());
		 
		 // When
		 FeatureState feature = repository.getFeatureState(MyFeature.FEATURE1);
		 
		 // Then
		 verify(mockAppender).doAppend(logEventMatches(Level.WARN, "FEATURE1"));
	}

	@Test
	public void setFeatureState_should_add_feature_state_in_jndi_when_not_defined() throws Exception{
		 
		 // When
		 repository.setFeatureState(new FeatureState(MyFeature.FEATURE1, true));
		 
		 // Then
		 verify(context).rebind("FEATURE1", "true");
	}
	
	@Test
	public void setFeatureState_should_update_feature_state_in_jndi() throws Exception{
		 // Given
		 doReturn(context).when(repository).getInitialContext();
		 // stub expected behavior of bind method
		 doThrow(new NameAlreadyBoundException("jndi error")).when(context).bind(anyString(), anyObject());
		 
		 // When
		 repository.setFeatureState(new FeatureState(MyFeature.FEATURE1, true));
		 
		 // Then
		 verify(context).rebind("FEATURE1", "true");
	}
	
	@Test
	public void setFeatureState_should_add_feature_users_in_jndi_when_not_defined() throws Exception{
		 
		 // When
		 repository.setFeatureState(new FeatureState(MyFeature.FEATURE1, true));
		 
		 // Then
		 verify(context).rebind("FEATURE1.users", "");
	}
	
	@Test
	public void setFeatureState_should_update_feature_users_in_jndi() throws Exception{
		 // Given
		 doReturn(context).when(repository).getInitialContext();
		 // stub expected behavior of bind method
		 doThrow(new NameAlreadyBoundException("jndi error")).when(context).bind(anyString(), anyObject());
		 
		 // When
		 repository.setFeatureState(new FeatureState(MyFeature.FEATURE1, true, Arrays.asList("user1","user2","user3")));
		 
		 // Then
		 verify(context).rebind("FEATURE1", "true");
		 verify(context).rebind("FEATURE1.users", "user1, user2, user3");
	}
	
	@Test
	public void setFeatureState_should_silently_ignore_jndi_init_context_errors() throws Exception{
		 // Given
		 doThrow(new NamingException("jndi error")).when(repository).getInitialContext();
		 
		 // When
		 repository.setFeatureState(new FeatureState(MyFeature.FEATURE1, true));
		 
		 // Then
		 verify(repository).getInitialContext();
	}
	
	@Test
	public void setFeatureState_should_silently_ignore_jndi_bind_errors() throws Exception{
		 // Given
		 doThrow(new NamingException("jndi error")).when(context).bind(anyString(), anyObject());
		 doThrow(new NamingException("jndi error")).when(context).rebind(anyString(), anyObject());
		 
		 // When
		 repository.setFeatureState(new FeatureState(MyFeature.FEATURE1, true));
		 
		 // Then
		 verify(context).rebind(anyString(),anyObject());
	}

	@Test
	public void setFeatureState_should_generate_log_error_on_jndi_errors() throws Exception{
		 final Appender<ILoggingEvent> mockAppender = addMockAppenderLog();
		 
		 // Given
		 doThrow(new NamingException("jndi error")).when(context).bind(anyString(), anyObject());
		 doThrow(new NamingException("jndi error")).when(context).rebind(anyString(), anyObject());
		 
		 // When
		 repository.setFeatureState(new FeatureState(MyFeature.FEATURE1, true));
		 
		 // Then
		 verify(mockAppender).doAppend(logEventMatches(Level.ERROR, "FEATURE1"));
	}
	
	@Test
	public void jndi_initial_context_should_be_cached() throws Exception {
		// Given
		doCallRealMethod().when(repository).getInitialContext();		
		doReturn(context).when(repository).createInitialContext();
		
		// When
		InitialContext context1 = repository.getInitialContext();
		InitialContext context2 = repository.getInitialContext();
		
		// Then
		verify(repository).createInitialContext();
		assertThat(context1).isEqualTo(context);
		assertThat(context2).isEqualTo(context);
		
	}

}
