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
package com.francetelecom.clara.cloud.presentation;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.commons.AuthorizationException;
import com.francetelecom.clara.cloud.coremodel.exception.InvalidApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.InvalidReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionViewFactoryTest {
	
	@Mock
	WebPageFactory webPageFactory;
	
	@Before
	public void setup(){
		Mockito.when(webPageFactory.getAuthorizationExceptionPage()).thenReturn(Mockito.mock(AuthorizationExceptionPage.class));		
		Mockito.when(webPageFactory.getInvalidApplicationExceptionPage()).thenReturn(Mockito.mock(InvalidApplicationExceptionPage.class));
		Mockito.when(webPageFactory.getInvalidReleaseExceptionPage()).thenReturn(Mockito.mock(InvalidReleaseExceptionPage.class));
		Mockito.when(webPageFactory.getObjectNotFoundExceptionPage()).thenReturn(Mockito.mock(ObjectNotFoundExceptionPage.class));
		Mockito.when(webPageFactory.getUnknownExceptionPage()).thenReturn(Mockito.mock(UnknownExceptionPage.class));
	}
	
	@Test
	public void should_get_authorization_exception_page_on_authorization_exception() {
		ExceptionViewFactory factory = new ExceptionViewFactory(webPageFactory);
		Assert.assertTrue(factory.newView(new AuthorizationException()) instanceof AuthorizationExceptionPage);
	}
	
	@Test
	public void should_get_invalid_application_exception_page_on_invalid_application_exception() {
		ExceptionViewFactory factory = new ExceptionViewFactory(webPageFactory);
		Assert.assertTrue(factory.newView(new InvalidApplicationException()) instanceof InvalidApplicationExceptionPage);
	}
	
	@Test
	public void should_get_invalid_release_exception_page_on_invalid_release_exception() {
		ExceptionViewFactory factory = new ExceptionViewFactory(webPageFactory);
		Assert.assertTrue(factory.newView(new InvalidReleaseException()) instanceof InvalidReleaseExceptionPage);
	}
	
	
	@Test
	public void should_get_object_not_found_exception_page_on_object_not_found_exception() {
		ExceptionViewFactory factory = new ExceptionViewFactory(webPageFactory);
		Assert.assertTrue(factory.newView(new ObjectNotFoundException()) instanceof ObjectNotFoundExceptionPage);
	}
	
	
	@Test
	public void should_get_unknown_exception_page_on_runtime_exception() {
		ExceptionViewFactory factory = new ExceptionViewFactory(webPageFactory);
		Assert.assertTrue(factory.newView(new RuntimeException()) instanceof UnknownExceptionPage);
	}

}
