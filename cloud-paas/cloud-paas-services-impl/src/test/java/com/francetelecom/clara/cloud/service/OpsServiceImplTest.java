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
package com.francetelecom.clara.cloud.service;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;

/**
 * OpsServiceImplTest
 */
@RunWith(MockitoJUnitRunner.class)
public class OpsServiceImplTest {
	@Spy
	private OpsServiceImpl opsService = new OpsServiceImpl();
	@Mock
	private ManageEnvironment manageEnvironment;
	@Mock
	private ManageApplicationRelease manageAppRelease;
	@Mock
	private ManageApplication manageApp;

	private List<Environment> olderEnvironments;

	@Before
	public void setUp() throws Exception {
		opsService.setManageApplication(manageApp);
		opsService.setManageRelease(manageAppRelease);
		opsService.setManageEnvironment(manageEnvironment);

		Environment envA = mock(Environment.class);
		Environment envB = mock(Environment.class);
		doReturn("uidA").when(envA).getUID();
		doReturn("uidB").when(envB).getUID();
		olderEnvironments = Arrays.asList(envA, envB);
		doReturn(olderEnvironments).when(manageEnvironment).findOldRemovedEnvironments();
	}

	@Test
	public void purge_should_catch_any_exception() throws ObjectNotFoundException {
		doThrow(new RuntimeException("Oo something was wrong here")).when(manageEnvironment).purgeRemovedEnvironment(anyString());
		// WHEN
		opsService.purgeDatabase();
		// THEN
		verify(manageEnvironment).findOldRemovedEnvironments();
		verify(manageEnvironment, times(olderEnvironments.size())).purgeRemovedEnvironment(anyString());
		// we stop on the first exception
		// but without rethrowing the exception (to avoid crashing the
		// scheduler)
	}

	@Test
	public void should_purge_env_release_and_application() throws ObjectNotFoundException {
		// WHEN
		opsService.purgeDatabase();
		// THEN
		verify(manageEnvironment).findOldRemovedEnvironments();
		verify(manageEnvironment, times(olderEnvironments.size())).purgeRemovedEnvironment(anyString());
		verify(manageAppRelease).purgeOldRemovedReleases();
		verify(manageApp).purgeOldRemovedApplications();
	}
}
