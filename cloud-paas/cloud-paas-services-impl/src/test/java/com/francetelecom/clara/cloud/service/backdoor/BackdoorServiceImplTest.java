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
package com.francetelecom.clara.cloud.service.backdoor;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.technicalservice.exception.EnvironmentNotFoundException;

public class BackdoorServiceImplTest {

	private static final PaasUser BOB_DYLAN = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");

	
	BackdoorServiceImpl backdoorService;
	EnvironmentRepository environmentRepository;

	@Before
	public void setup() {
		environmentRepository = Mockito.mock(EnvironmentRepository.class);
		backdoorService = new BackdoorServiceImpl();
		backdoorService.setEnvironmentRepository(environmentRepository);
	}

	@Test
	public void shouldFindEnvironmentByReleaseUIDAndEnvironmentLabel() throws EnvironmentNotFoundException {
		// given paas user with ssoId aSsoId exists
		// given environment with label aLabel of release aVersion exists
		Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "aLabel", new ApplicationRelease(new Application("aLabel", "aCode"),
				"aVersion"), BOB_DYLAN,Mockito.mock(TechnicalDeploymentInstance.class));
		Mockito.when(environmentRepository.findByApplicationReleaseUIDAndLabel(environment.getApplicationRelease().getUID(), "aLabel")).thenReturn(
				environment);
		// when I find environment with label aLabel of release aVersion
		Environment result = backdoorService.findEnvironmentByApplicationReleaseAndLabel(environment.getApplicationRelease().getUID(), "aLabel");
		// then I should get environment with label aLabel of release aVersion
		Assert.assertEquals(environment, result);
	}

	@Test(expected = EnvironmentNotFoundException.class)
	public void shouldFailToFindUnknownEnvironmentByReleaseUIDAndEnvironmentLabel() throws EnvironmentNotFoundException {
		// given environment with label unknown of release unknown does not
		// exist
		Mockito.when(environmentRepository.findByApplicationReleaseUIDAndLabel("unknown", "unknown")).thenReturn(null);
		// when I find environment with label aLabel of release aVersion
		Environment result = backdoorService.findEnvironmentByApplicationReleaseAndLabel("unknown", "unknown");
		// then It should fail
	}

	@Test
	public void shouldFindEnvironmentByUID() throws EnvironmentNotFoundException {
		// given paas user with ssoId aSsoId exists
		// given environment with label aLabel of release aVersion exists
		Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "aLabel", new ApplicationRelease(new Application("aLabel", "aCode"),
				"aVersion"), BOB_DYLAN,Mockito.mock(TechnicalDeploymentInstance.class));
		Mockito.when(environmentRepository.findByUID(environment.getUID())).thenReturn(environment);
		// when I find environment with label aLabel of release aVersion
		Environment result = backdoorService.findEnvironmentByUID(environment.getUID());
		// then I should get environment with label aLabel of release aVersion
		Assert.assertEquals(environment, result);
	}

	@Test(expected = EnvironmentNotFoundException.class)
	public void shouldFailToFindEnvironmentByUnknownUID() throws EnvironmentNotFoundException {
		// given environment with uid unknown does not exist
		Mockito.when(environmentRepository.findByUID("unknown")).thenReturn(null);
		// when I find environment with uid unknown
		Environment result = backdoorService.findEnvironmentByUID("unknown");
		// then It should fail
	}
}
