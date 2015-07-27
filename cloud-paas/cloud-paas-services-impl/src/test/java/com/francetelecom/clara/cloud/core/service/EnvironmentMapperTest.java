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
package com.francetelecom.clara.cloud.core.service;

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.environment.impl.EnvironmentMapper;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class EnvironmentMapperTest {

	@After
	public void tearwown() {
		TestHelper.logout();
	}
	
	@Test
	public void should_map_environment_to_environment_dto() throws Exception {
		
		//FIXME requires to be logged to perform test, find a better way to avoid logging to perform test
		TestHelper.loginAsAdmin();
		
		// given environment env_elpaasso_1_0 for release 1.0
		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		Application elpaaso = new Application("elpaaso", "elpaaso");
		PaasUser owner = new PaasUser("joe", "dalton", new SSOId("jdalton"), "jdalton@orane.com");
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
		Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		EnvironmentDto dto = new EnvironmentDto(environment.getUID(), environment.getInternalName(), "env_elpaasso_1_0", elpaaso.getLabel(), elpaaso_1_0.getUID(), "1.0",
				"jdalton", "joe", environment.getCreationDate(), EnvironmentTypeEnum.DEVELOPMENT, EnvironmentStatusEnum.CREATING, null, -1, null, environment
								.getTechnicalDeploymentInstance().getTechnicalDeployment().getName());
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDto(environment)).isEqualTo(dto);
	}
	
	@Test
	public void should_map_environment_to_editable_environment_dto() throws Exception {
		
		//FIXME requires to be logged to perform test, find a better way to avoid logging to perform test
		TestHelper.loginAsAdmin();
		
		// given environment env_elpaasso_1_0 for release 1.0
		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		Application elpaaso = new Application("elpaaso", "elpaaso");
		PaasUser owner = new PaasUser("joe", "dalton", new SSOId("jdalton"), "jdalton@orane.com");
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
		Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		//dto should be editable
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDto(environment).isEditable()).isEqualTo(true);
	}
	
	@Test
	public void should_map_environment_to_non_editable_environment_dto() throws Exception {
		//FIXME requires to be logged to perform test, find a better way to avoid logging to perform test
		TestHelper.loginAsUser();
		
		// given environment env_elpaasso_1_0 for release 1.0
		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		Application elpaaso = new Application("elpaaso", "elpaaso");
		PaasUser owner = new PaasUser("joe", "dalton", new SSOId("jdalton"), "jdalton@orane.com");
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
		Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		//dto should not be editable
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDto(environment).isEditable()).isEqualTo(false);
	}
	
	@Test
	public void should_map_environment_list_to_environment_dto_list() throws Exception {
		
		//FIXME requires to be logged to perform test, find a better way to avoid logging to perform test
		TestHelper.loginAsAdmin();
		
		// given environment env_elpaasso_1_0 for release 1.0
		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		Application elpaaso = new Application("elpaaso", "elpaaso");
		PaasUser owner = new PaasUser("joe", "dalton", new SSOId("jdalton"), "jdalton@orane.com");
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
		Environment environment_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_1_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		Environment environment_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_2_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		Environment environment_3 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_3_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		List<Environment> environments = Arrays.asList(environment_1,environment_2,environment_3);
		
		EnvironmentDto dto_1 = new EnvironmentDto(environment_1.getUID(), environment_1.getInternalName(), "env_1_elpaasso_1_0", elpaaso.getLabel(), elpaaso_1_0.getUID(), "1.0",
				"jdalton", "joe", environment_1.getCreationDate(), EnvironmentTypeEnum.DEVELOPMENT, EnvironmentStatusEnum.CREATING, null, -1, null, environment_1
								.getTechnicalDeploymentInstance().getTechnicalDeployment().getName());
		
		EnvironmentDto dto_2 = new EnvironmentDto(environment_2.getUID(), environment_2.getInternalName(), "env_2_elpaasso_1_0", elpaaso.getLabel(), elpaaso_1_0.getUID(), "1.0",
				"jdalton", "joe", environment_2.getCreationDate(), EnvironmentTypeEnum.DEVELOPMENT, EnvironmentStatusEnum.CREATING, null, -1, null, environment_2
								.getTechnicalDeploymentInstance().getTechnicalDeployment().getName());
		
		EnvironmentDto dto_3 = new EnvironmentDto(environment_3.getUID(), environment_3.getInternalName(), "env_3_elpaasso_1_0", elpaaso.getLabel(), elpaaso_1_0.getUID(), "1.0",
				"jdalton", "joe", environment_3.getCreationDate(), EnvironmentTypeEnum.DEVELOPMENT, EnvironmentStatusEnum.CREATING, null, -1, null, environment_3
								.getTechnicalDeploymentInstance().getTechnicalDeployment().getName());

		
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).size()).isEqualTo(3);
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).contains(dto_1)).isTrue();
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).contains(dto_2)).isTrue();
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).contains(dto_3)).isTrue();

	}
	
	@Test
	public void should_map_environment_list_to_editable_environment_dto_list() throws Exception {
		
		//FIXME requires to be logged to perform test, find a better way to avoid logging to perform test
		TestHelper.loginAsAdmin();
		
		// given environment env_elpaasso_1_0 for release 1.0
		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		Application elpaaso = new Application("elpaaso", "elpaaso");
		PaasUser owner = new PaasUser("joe", "dalton", new SSOId("jdalton"), "jdalton@orane.com");
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
		Environment environment_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_1_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		Environment environment_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_2_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		Environment environment_3 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_3_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		
		List<Environment> environments = Arrays.asList(environment_1,environment_2,environment_3);
		
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).size()).isEqualTo(3);
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).get(0).isEditable()).isTrue();
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).get(1).isEditable()).isTrue();
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).get(2).isEditable()).isTrue();

	}
	
	@Test
	public void should_map_environment_list_to_non_editable_environment_dto_list() throws Exception {
		
		//FIXME requires to be logged to perform test, find a better way to avoid logging to perform test
		TestHelper.loginAsUser();
		
		// given environment env_elpaasso_1_0 for release 1.0
		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		Application elpaaso = new Application("elpaaso", "elpaaso");
		PaasUser owner = new PaasUser("joe", "dalton", new SSOId("jdalton"), "jdalton@orane.com");
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
		Environment environment_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_1_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		Environment environment_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_2_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		Environment environment_3 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_3_elpaasso_1_0", elpaaso_1_0, owner, tdi);
		
		List<Environment> environments = Arrays.asList(environment_1,environment_2,environment_3);
		
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).size()).isEqualTo(3);
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).get(0).isEditable()).isFalse();
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).get(1).isEditable()).isFalse();
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(environments).get(2).isEditable()).isFalse();

	}
	
	
	@Test
	public void should_map_null_environment_list_to_environment_dto_list() throws Exception {
		Assertions.assertThat(new EnvironmentMapper().toEnvironmentDtoList(null)).isEmpty();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void fail_to_map_null_environment_to_environment_dto() throws Exception {
		new EnvironmentMapper().toEnvironmentDto(null);
	}

}
