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
package com.francetelecom.clara.cloud.deployment.technical.service;

import com.francetelecom.clara.cloud.commons.NotFoundException;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplateRepository;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;


@RunWith(MockitoJUnitRunner.class)
public class ManageTechnicalDeploymentTemplateImplTest {
	
	@Mock
	TechnicalDeploymentTemplateRepository technicalDeploymentTemplateRepository;
	
	ManageTechnicalDeploymentTemplate manageTechnicalDeploymentTemplate;
	
	@Before
	public void setup() {
		manageTechnicalDeploymentTemplate = new ManageTechnicalDeploymentTemplateImpl(technicalDeploymentTemplateRepository);
	}
	
	
	@Test
	public void should_find_existing_technical_deployment_template_by_profile_and_releaseId() throws NotFoundException {
		TechnicalDeploymentTemplate expected = new TechnicalDeploymentTemplate(new TechnicalDeployment("name"), DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		Mockito.when(technicalDeploymentTemplateRepository.findByDeploymentProfileAndReleaseId(DeploymentProfileEnum.DEVELOPMENT, "releaseId")).thenReturn(expected);
		
		TechnicalDeploymentTemplate actual = manageTechnicalDeploymentTemplate.findTechnicalDeploymentTemplate(DeploymentProfileEnum.DEVELOPMENT, "releaseId");
		
		Assertions.assertThat(actual).isEqualTo(expected);
	}
	
	@Test(expected=NotFoundException.class)
	public void fail_to_find_non_existing_technical_deployment_template_by_profile_and_releaseId() throws NotFoundException {
		Mockito.when(technicalDeploymentTemplateRepository.findByDeploymentProfileAndReleaseId(DeploymentProfileEnum.DEVELOPMENT, "releaseId")).thenReturn(null);
		
		manageTechnicalDeploymentTemplate.findTechnicalDeploymentTemplate(DeploymentProfileEnum.DEVELOPMENT, "releaseId");
		
	}
	

}
