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
package com.francetelecom.clara.cloud.providersoap.mapping;

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ApplicationReleaseStateEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import com.orange.clara.cloud.providersoap.administration.v4.model.ApplicationModel;
import com.orange.clara.cloud.providersoap.administration.v4.model.MiddlewareProfile;
import com.orange.clara.cloud.providersoap.administration.v4.model.ReleaseModel;
import com.orange.clara.cloud.providersoap.administration.v4.model.StateType;
import com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentModel;
import com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentStatus;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import static org.junit.Assert.assertEquals;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class SoapMapperTest {

	@Autowired
	public SoapMapper mapper;

	@Test
	public void shouldMapEnvironmentTypeEnumTESTToEnvironmentTypeEnumTEST() {
		com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentTypeEnum src = com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentTypeEnum.TEST;
		EnvironmentTypeEnum target = mapper.map(src, EnvironmentTypeEnum.class);
		Assert.assertEquals(EnvironmentTypeEnum.TEST, target);
	}

	@Test
	public void shouldMapEnvironmentStatusEnumCREATINGToEnvironmentStatusEnumCREATING() {
		com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum src = com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum.CREATING;
		com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentStatusEnum target = mapper.map(src,
				com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentStatusEnum.class);
		Assert.assertEquals(com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentStatusEnum.CREATING, target);
	}

	@Test
	public void shouldMapEnvironmentDtoToEnvironmentModel() {
		EnvironmentDto src = new EnvironmentDto("name", "internal-name", "label", "applicationLabel", "releaseUID", "releaseVersion", "ownerId", "ownerName",
				new Date(), EnvironmentTypeEnum.DEVELOPMENT, EnvironmentStatusEnum.CREATING, "statusMessage", 50, "", "tdiTdName");
		EnvironmentModel target = mapper.map(src, EnvironmentModel.class);
		Assert.assertEquals("label", target.getLabel());
		Assert.assertEquals("ownerId", target.getOwner());
		Assert.assertEquals("releaseUID", target.getReleaseUID());
		Assert.assertEquals(com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentTypeEnum.DEVELOPMENT, target.getType());
		Assert.assertEquals(com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentStatusEnum.CREATING, target.getStatus().getType());
		Assert.assertEquals("statusMessage", target.getStatus().getMessage());
		Assert.assertEquals("50", target.getStatus().getProgress());
	}

	@Test
	public void shouldMapEnvironmentDtoToEnvironmentStatus() {
		EnvironmentDto src = new EnvironmentDto("name", "internal-name", "label", "applicationLabel", "releaseUID", "releaseVersion", "ownerId", "ownerName",
				new Date(), EnvironmentTypeEnum.DEVELOPMENT, EnvironmentStatusEnum.CREATING, "statusMessage", 50, "", "tdiTdName");
		EnvironmentStatus target = mapper.map(src, EnvironmentStatus.class);
		Assert.assertEquals(com.orange.clara.cloud.providersoap.environment.v3.model.EnvironmentStatusEnum.CREATING, target.getType());
		Assert.assertEquals("statusMessage", target.getMessage());
		Assert.assertEquals("50", target.getProgress());
	}

	@Test
	public void shouldMapApplicationFromCoreModelToSOAPModel() throws MalformedURLException {
		Application source = new Application("aLabel", "aCode");
		source.setDescription("aDescription");
		source.setApplicationRegistryUrl(new URL("http://www.yahoo.com"));
		ApplicationModel target = mapper.map(source, ApplicationModel.class);
		Assert.assertEquals("aCode", target.getCode());
		Assert.assertEquals("aLabel", target.getLabel());
		Assert.assertEquals("aDescription", target.getDescription());
		Assert.assertEquals("http://www.yahoo.com", target.getRegistryUrl());
	}

	@Test
	public void shouldMapApplicationReleaseFromCoreModelToSOAPModel() throws MalformedURLException {
		// Given application release from code model
		Application application = new Application("aLabel", "aCode");
		ApplicationRelease source = new ApplicationRelease(application, "aVersion");
		source.setDescription("aDescription");
		source.setVersionControlUrl(new URL("http://www.yahoo.com"));
		// when application release from code model is mapped to SOAP model
		ReleaseModel target = mapper.map(source, ReleaseModel.class);
		// we should get the same release uid
		Assert.assertEquals(source.getUID(), target.getUid());
		// we should get the same application uid
		Assert.assertEquals(application.getUID(), target.getApplicationUID());
		// we should get the same version
		Assertions.assertThat(target.getVersion()).isEqualTo("aVersion");
		// we should get the same state
		Assertions.assertThat(target.getState()).isEqualTo(StateType.EDITING);
		// we should get the same description
		Assertions.assertThat(target.getDescription()).isEqualTo("aDescription");
		// we should get the same versionControl Url
		Assertions.assertThat(target.getVersionControlUrl()).isEqualTo("http://www.yahoo.com");
	}

	@Test
	public void shouldMapApplicationReleaseStatusFromCoreModelToSOAPModel() throws MalformedURLException {
		Assertions.assertThat(mapper.map(ApplicationReleaseStateEnum.DISCARDED, StateType.class)).isEqualTo(StateType.DISCARDED);
		Assertions.assertThat(mapper.map(ApplicationReleaseStateEnum.REMOVED, StateType.class)).isEqualTo(StateType.REMOVED);
		Assertions.assertThat(mapper.map(ApplicationReleaseStateEnum.EDITING, StateType.class)).isEqualTo(StateType.EDITING);
		Assertions.assertThat(mapper.map(ApplicationReleaseStateEnum.VALIDATED, StateType.class)).isEqualTo(StateType.VALIDATED);
		Assertions.assertThat(mapper.map(ApplicationReleaseStateEnum.LOCKED, StateType.class)).isEqualTo(StateType.LOCKED);
	}

	@Test
	public void should_map_middlewareprofiles_from_core_model_profiles() {
		com.francetelecom.clara.cloud.coremodel.MiddlewareProfile coreProfile = com.francetelecom.clara.cloud.coremodel.MiddlewareProfile.V210_CF;

		MiddlewareProfile target = mapper.map(coreProfile, MiddlewareProfile.class);

		assertEquals(coreProfile.getVersion(), target.getVersion());
		assertEquals(coreProfile.getStatus().name(), target.getStatus());
	}
}
