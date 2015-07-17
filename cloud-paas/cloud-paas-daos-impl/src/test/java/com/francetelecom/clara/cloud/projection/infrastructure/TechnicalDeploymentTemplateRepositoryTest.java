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
package com.francetelecom.clara.cloud.projection.infrastructure;

import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplateRepository;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/services/application-context.xml" })
public class TechnicalDeploymentTemplateRepositoryTest {

	@Autowired
	TechnicalDeploymentTemplateRepository technicalDeploymentTemplateRepository;
	
	@Test
	@Transactional
	public void should_find_existing_technical_deployment_template_by_profile_and_releaseId() {
		TechnicalDeploymentTemplate devTemplate = new TechnicalDeploymentTemplate(new TechnicalDeployment("dev"), DeploymentProfileEnum.DEVELOPMENT, "a releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		technicalDeploymentTemplateRepository.save(devTemplate);
		TechnicalDeploymentTemplate testTemplate = new TechnicalDeploymentTemplate(new TechnicalDeployment("test"), DeploymentProfileEnum.TEST, "a releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		technicalDeploymentTemplateRepository.save(testTemplate);
		
		Assertions.assertThat(technicalDeploymentTemplateRepository.findByDeploymentProfileAndReleaseId(DeploymentProfileEnum.DEVELOPMENT, "a releaseId")).isEqualTo(devTemplate);
		Assertions.assertThat(technicalDeploymentTemplateRepository.findByDeploymentProfileAndReleaseId(DeploymentProfileEnum.TEST, "a releaseId")).isEqualTo(testTemplate);

	}
	
	@Test
	@Transactional
	public void fail_to_find_not_existing_technical_deployment_template_by_profile_and_releaseId() {
		TechnicalDeploymentTemplate devTemplate = new TechnicalDeploymentTemplate(new TechnicalDeployment("dev"), DeploymentProfileEnum.DEVELOPMENT, "a releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		technicalDeploymentTemplateRepository.save(devTemplate);
		TechnicalDeploymentTemplate testTemplate = new TechnicalDeploymentTemplate(new TechnicalDeployment("test"), DeploymentProfileEnum.TEST, "a releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		technicalDeploymentTemplateRepository.save(testTemplate);
		
		Assertions.assertThat(technicalDeploymentTemplateRepository.findByDeploymentProfileAndReleaseId(DeploymentProfileEnum.DEVELOPMENT, "")).isNull();

	}
	
	@Test
	@Transactional
	public void should_find_existing_technical_deployment_template_by_releaseId() {
		TechnicalDeploymentTemplate devTemplate = new TechnicalDeploymentTemplate(new TechnicalDeployment("dev"), DeploymentProfileEnum.DEVELOPMENT, "a releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		technicalDeploymentTemplateRepository.save(devTemplate);
		TechnicalDeploymentTemplate testTemplate = new TechnicalDeploymentTemplate(new TechnicalDeployment("test"), DeploymentProfileEnum.TEST, "a releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		technicalDeploymentTemplateRepository.save(testTemplate);
		TechnicalDeploymentTemplate prodTemplate = new TechnicalDeploymentTemplate(new TechnicalDeployment("prod"), DeploymentProfileEnum.TEST, "another releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		technicalDeploymentTemplateRepository.save(prodTemplate);
		
		Assertions.assertThat(technicalDeploymentTemplateRepository.findAllByReleaseId("a releaseId").size()).isEqualTo(2);
		Assertions.assertThat(technicalDeploymentTemplateRepository.findAllByReleaseId("a releaseId")).contains(devTemplate);
		Assertions.assertThat(technicalDeploymentTemplateRepository.findAllByReleaseId("a releaseId")).contains(testTemplate);

		Assertions.assertThat(technicalDeploymentTemplateRepository.findAllByReleaseId("another releaseId").size()).isEqualTo(1);
		Assertions.assertThat(technicalDeploymentTemplateRepository.findAllByReleaseId("another releaseId")).contains(prodTemplate);

	}


}
