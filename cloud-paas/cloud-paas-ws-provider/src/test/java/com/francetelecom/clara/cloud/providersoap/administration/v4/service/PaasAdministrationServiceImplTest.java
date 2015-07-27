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
package com.francetelecom.clara.cloud.providersoap.administration.v4.service;

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.exception.*;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.providersoap.mapping.SoapMapper;
import com.orange.clara.cloud.providersoap.administration.v4.model.CreateApplicationCommand;
import com.orange.clara.cloud.providersoap.administration.v4.model.CreateReleaseCommand;
import com.orange.clara.cloud.providersoap.administration.v4.service.ApplicationNotFoundErrorFault;
import com.orange.clara.cloud.providersoap.administration.v4.service.DuplicateApplicationErrorFault;
import com.orange.clara.cloud.providersoap.administration.v4.service.DuplicateReleaseErrorFault;
import com.orange.clara.cloud.providersoap.administration.v4.service.PaasUserNotFoundErrorFault;
import com.orange.clara.cloud.providersoap.security.v1.Credentials;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class PaasAdministrationServiceImplTest {
	
	@InjectMocks
	PaasAdministrationServiceImpl paasAdministrationService = new PaasAdministrationServiceImpl();
	
	@Mock
	ManageApplication manageApplication;
	
	@Mock
	ManageApplicationRelease manageApplicationRelease;
	
	@Mock
	AuthenticationManager authenticationManager;
	
	@Mock
	SoapMapper mapper;

	private Credentials credentials;
	
	@Before
	public void setup() {
		paasAdministrationService.setMapper(new SoapMapper());
		credentials = new Credentials();
		credentials.setSsoid("ssoid");
		credentials.setPassword("pwd");
	}
	
	@Test
	public void createApplication_should_create_private_application_when_requested() throws Exception {
		CreateApplicationCommand command = new CreateApplicationCommand();
		command.setCode("code");
		command.setDescription("description");
		command.setIsPublic(false);
		command.setLabel("label");
		command.setRegistryUrl(null);
		
		paasAdministrationService.createApplication(command, credentials);

		verify(manageApplication).createPrivateApplication(anyString(), anyString(), anyString(), any(URL.class), (SSOId[]) anyVararg());
	}

	@Test
	public void createApplication_should_create_public_application_when_requested() throws Exception {
		CreateApplicationCommand command = getBasicCommand();
		command.setIsPublic(true);
		
		paasAdministrationService.createApplication(command,credentials);

		verify(manageApplication).createPublicApplication(anyString(), anyString(), anyString(), any(URL.class), (SSOId[]) anyVararg());
	}
	
	@Test
	public void createApplication_should_set_default_member_when_specified() throws Exception {
		CreateApplicationCommand command = new CreateApplicationCommand();
		command.setCode("code");
		command.setDescription("description");
		command.setIsPublic(true);
		command.setLabel("label");
		command.setRegistryUrl(null);
		
		paasAdministrationService.createApplication(command, credentials);

		verify(manageApplication).createPublicApplication(anyString(), anyString(), anyString(), any(URL.class), (SSOId[]) anyVararg());
	}

	@Test
	public void createApplication_should_create_public_application_when_visibility_is_not_specified() throws Exception {
		CreateApplicationCommand command = getBasicCommand();
		command.setIsPublic(null);
		
		paasAdministrationService.createApplication(command, credentials);
		
		verify(manageApplication).createPublicApplication(anyString(), anyString(), anyString(), any(URL.class), (SSOId[]) anyVararg());
	}

	private CreateApplicationCommand getBasicCommand() {
		CreateApplicationCommand command = new CreateApplicationCommand();
		command.setCode("code");
		command.setDescription("description");
		command.setIsPublic(true);
		command.setLabel("label");
		command.setRegistryUrl("http://10.114.6.1/myapp.url");
		return command;
	}
	
	@Test(expected=PaasUserNotFoundErrorFault.class)
	public void fail_to_create_private_application_with_unknown_member() throws Exception {
		//given a private application
		CreateApplicationCommand command = getBasicCommand();
		command.setIsPublic(false);
		
		when(manageApplication.createPrivateApplication(anyString(), anyString(), anyString(), any(URL.class),any(SSOId.class))).thenThrow(new PaasUserNotFoundException("Paas user not found thrown by tests"));
		
		paasAdministrationService.createApplication(command, credentials);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createApplication_should_throw_IllegalArgumentException_on_MalformedURLException() throws Exception {
		CreateApplicationCommand command = getBasicCommand();
		command.setRegistryUrl("invalid_url");
		
		paasAdministrationService.createApplication(command, credentials);
	}

	@Test(expected=DuplicateApplicationErrorFault.class)
	public void createApplication_should_throw_DuplicateApplicationErrorFault_on_DuplicateApplicationException() throws Exception, PaasUserNotFoundException, DuplicateApplicationException, InvalidApplicationException, DuplicateApplicationErrorFault {
		CreateApplicationCommand command = getBasicCommand();
		when(manageApplication.createPublicApplication(anyString(), anyString(), anyString(), any(URL.class), (SSOId[]) anyVararg())).thenThrow(new DuplicateApplicationException("DuplicateApplicationException thown by tests"));
		
		paasAdministrationService.createApplication(command, credentials);
	}
	
	@Test
	public void createApplication_should_translate_string_to_url_for_registry_url() throws Exception{
		CreateApplicationCommand command = getBasicCommand();
				
		paasAdministrationService.createApplication(command, credentials);
		
		URL expectedUrl = new URL("http://10.114.6.1/myapp.url");
		verify(manageApplication).createPublicApplication(anyString(), anyString(), anyString(), eq(expectedUrl), eq(new SSOId(credentials.getSsoid())));
	}
	
	@Test
	public void createRelease_should_support_optional_params() throws Exception {
		CreateReleaseCommand command = getCreateReleaseCommand();
		command.setDescription(null);
		command.setProfileVersion(null);
		command.setVersionControlUrl(null);
		
		paasAdministrationService.createRelease(command, credentials);
		
		verify(manageApplicationRelease).createApplicationRelease("id", "ssoid", "1.0", null, null, null);
	}

	private CreateReleaseCommand getCreateReleaseCommand() {
		CreateReleaseCommand command = new CreateReleaseCommand();
		command.setApplicationUID("id");
		command.setDescription("description");
		command.setProfileVersion(MiddlewareProfile.V210_CF.getVersion());
		command.setVersion("1.0");
		command.setVersionControlUrl("http://test.com");
		return command;
	}
	
	@Test
	public void createRelease_should_try_to_create_a_release_specifying_profile_to_core_service() throws Exception {
		CreateReleaseCommand command = getCreateReleaseCommand();
		
		paasAdministrationService.createRelease(command,credentials);
		
		verify(manageApplicationRelease).createApplicationRelease("id", "ssoid", "1.0", "description", new URL("http://test.com"), MiddlewareProfile.V210_CF.getVersion());
	}
	
	@Test(expected=PaasUserNotFoundErrorFault.class)
	public void createRelease_should_wrap_user_not_found_exceptions_into_fault() throws Exception {
		CreateReleaseCommand command = getCreateReleaseCommand();
		
		doThrow(new PaasUserNotFoundException()).when(manageApplicationRelease).createApplicationRelease(anyString(), anyString(), anyString(), anyString(), any(URL.class), anyString());
		
		paasAdministrationService.createRelease(command,credentials);
	}
	
	@Test(expected=ApplicationNotFoundErrorFault.class)
	public void createRelease_should_application_not_found_exceptions_into_fault() throws Exception {
		CreateReleaseCommand command = getCreateReleaseCommand();

		doThrow(new ApplicationNotFoundException()).when(manageApplicationRelease).createApplicationRelease(anyString(), anyString(), anyString(), anyString(), any(URL.class), anyString());
		
		paasAdministrationService.createRelease(command,credentials);
	}
	
	@Test(expected=DuplicateReleaseErrorFault.class)
	public void createRelease_should_wrap_duplicate_application_release_into_fault() throws Exception {
		CreateReleaseCommand command = getCreateReleaseCommand();

		doThrow(new DuplicateApplicationReleaseException()).when(manageApplicationRelease).createApplicationRelease(anyString(), anyString(), anyString(), anyString(), any(URL.class), anyString());
		
		paasAdministrationService.createRelease(command,credentials);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void createRelease_should_wrap_malformed_url_into_illegal_argument() throws Exception {
		CreateReleaseCommand command = getCreateReleaseCommand();
		command.setVersionControlUrl("qsdfjalzefaz024az2+ù*$^*ef0azef");

		paasAdministrationService.createRelease(command,credentials);
	}
	
	@Test
	public void getAllMiddlewarePRofiles_should_list_profiles_from_core_add_map_each_items_to_soap() throws Exception {
		when(manageApplicationRelease.findAllMiddlewareProfil()).thenReturn(Arrays.asList(MiddlewareProfile.values()));
		
		List<com.orange.clara.cloud.providersoap.administration.v4.model.MiddlewareProfile> allMiddlewareProfiles = paasAdministrationService.getAllMiddlewareProfiles(credentials);
		
		assertThat(allMiddlewareProfiles).hasSize(MiddlewareProfile.values().length);
	}
	
}
