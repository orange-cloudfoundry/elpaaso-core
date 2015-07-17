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
package com.francetelecom.clara.cloud.paas.projection;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.EnumMap;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Simply test normal and error condition of MultiProfileProjectionServiceDispatcherImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class MultiProfileProjectionServiceDispatcherImplTest {

	@Mock
	private ProjectionService profile;

	@Mock
	private ApplicationRelease applicationRelease;

	private final MultiProfileProjectionServiceDispatcherImpl dispatcher = new MultiProfileProjectionServiceDispatcherImpl();
	private EnumMap<MiddlewareProfile, ProjectionService> profileMap;

	@Before
	public void setUp() {
		profileMap = new EnumMap<MiddlewareProfile, ProjectionService>(MiddlewareProfile.class);
		profileMap.put(MiddlewareProfile.V210_CF, profile);
		dispatcher.setProjectionsPerMiddlewareProfileVersion(profileMap);
	}

	@Test(expected = TechnicalException.class)
	public void testUnsupportedProfileThrowsException() throws UnsupportedProjectionException {
		when(applicationRelease.getMiddlewareProfileVersion()).thenReturn("1.4");
		
		//When
		dispatcher.generateNewDeploymentTemplate(applicationRelease, DeploymentProfileEnum.DEVELOPMENT);
	}

	@Test
	public void testSupportedProfileIndeedSelectsRightProjection() throws UnsupportedProjectionException {
		when(applicationRelease.getMiddlewareProfileVersion()).thenReturn(MiddlewareProfile.V210_CF.getVersion());
		DeploymentProfileEnum development = DeploymentProfileEnum.DEVELOPMENT;
		
		//When
		dispatcher.generateNewDeploymentTemplate(applicationRelease, development);
		
		//Then
		verify(profile).generateNewDeploymentTemplate(applicationRelease, development);
	}

	@Test
	public void find_all_middleware_profile_should_return_all_keys_from_projections_profile_map() throws Exception {
		// When
		List<MiddlewareProfile> middlewareProfiles = dispatcher.findAllMiddlewareProfil();

		// Then
		assertThat(middlewareProfiles).containsExactly(Lists.newArrayList(profileMap.keySet()).toArray());
	}
}
