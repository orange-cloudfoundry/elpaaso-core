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
package com.francetelecom.clara.cloud.presentation.releases;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;


import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;

import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile.MiddlewareProfileStatus;
import com.francetelecom.clara.cloud.presentation.releases.ReleaseOverrideProfilePanel.ProfileChoiceRenderer;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;

@RunWith(MockitoJUnitRunner.class)
public class ReleaseOverrideProfilePanelTest {

	@Mock
	private ManagePaasUser managePaasUser;
	@Mock
	private AuthenticationManager authenticationManager;
	@Mock
	private ManageApplicationRelease manageApplicationRelease;
	private WicketTester tester;

	@Before
	public void init() {
		ApplicationContextMock context = new ApplicationContextMock();
		context.putBean(managePaasUser);
		context.putBean("authenticationManager",authenticationManager);
		context.putBean(manageApplicationRelease);
		PaasTestApplication application = new PaasTestApplication(context);
		tester = new WicketTester(application);
	}

	@Test
	public void component_should_select_default_profile() throws Exception {
		ReleaseOverrideProfilePanel panel = tester.startComponentInPage(ReleaseOverrideProfilePanel.class);
		
		assertEquals(MiddlewareProfile.getDefault(), panel.getCurrentMiddlewareProfile());
	}
	
	@Test
	public void component_should_show_a_filtered_profiles_list() throws Exception {
		//Mock services with all existing profiles
		when(manageApplicationRelease.findAllMiddlewareProfil()).thenReturn(Arrays.asList(MiddlewareProfile.values()));
		//Fake user (ROLE_USER)
		PaasUser createPaasUserMock = CreateObjectsWithJava.createPaasUserMock("testuser", PaasRoleEnum.ROLE_USER);
		
		((PaasTestSession)tester.getSession()).setPaasUser(createPaasUserMock);
		
		//Construct profile versions expectations
		Set<MiddlewareProfile> expectedProfiles = MiddlewareProfile.filter(createPaasUserMock,Arrays.asList(MiddlewareProfile.values()));

		//When
		tester.startComponentInPage(ReleaseOverrideProfilePanel.class);
		
		//Grab listbox from last page
		DropDownChoice<MiddlewareProfile> choiceComponent = (DropDownChoice<MiddlewareProfile>) tester.getComponentFromLastRenderedPage("middlewareProfileContainer:middlewareProfileSelect");

		//Check that profiles are queried from elpaaso core services
		verify(manageApplicationRelease).findAllMiddlewareProfil();
		//Check that all expected profiles are displayed
		assertThat(choiceComponent.getChoices()).containsOnly(expectedProfiles.toArray());
	}
	
	@Test
	public void component_should_show_a_sorted_profiles_list() throws Exception {
		when(manageApplicationRelease.findAllMiddlewareProfil()).thenReturn(Arrays.asList(MiddlewareProfile.values()));
		//Fake user (ROLE_USER)
		PaasUser createPaasUserMock = CreateObjectsWithJava.createPaasUserMock("testuser", PaasRoleEnum.ROLE_ADMIN);
		((PaasTestSession)tester.getSession()).setPaasUser(createPaasUserMock);
		
		//When
		tester.startComponentInPage(ReleaseOverrideProfilePanel.class);
		
		//Grab listbox from last page
		DropDownChoice<MiddlewareProfile> choiceComponent = (DropDownChoice<MiddlewareProfile>) tester.getComponentFromLastRenderedPage("middlewareProfileContainer:middlewareProfileSelect");

		List<MiddlewareProfile> sortedList = new ArrayList<MiddlewareProfile>(choiceComponent.getChoices());
		Collections.sort(sortedList);
		
		assertThat(choiceComponent.getChoices()).isEqualTo(sortedList);
	}
	
	@Test
	public void profile_renderer_render_status_for_each_profile() throws Exception {
		IChoiceRenderer<MiddlewareProfileStatus> statusRenderer = mock(IChoiceRenderer.class);
		when(statusRenderer.getDisplayValue(any(MiddlewareProfileStatus.class))).thenReturn("mockedstatus");
		
		ProfileChoiceRenderer renderer = spy(new ReleaseOverrideProfilePanel.ProfileChoiceRenderer(null, statusRenderer));
		doReturn("default, ").when(renderer).getDefaultLabel();
		
		for (MiddlewareProfile profile : MiddlewareProfile.values()) {
			assertThat(renderer.getDisplayValue(profile).contains("mockedstatus"));
		}
	}
	
	@Test
	public void profile_renderer_render_default_label_only_on_default_profile() throws Exception {
		IChoiceRenderer<MiddlewareProfileStatus> statusRenderer = mock(IChoiceRenderer.class);
		when(statusRenderer.getDisplayValue(any(MiddlewareProfileStatus.class))).thenReturn("mockedstatus");
		
		ProfileChoiceRenderer renderer = spy(new ReleaseOverrideProfilePanel.ProfileChoiceRenderer(null, statusRenderer));
		doReturn("default, ").when(renderer).getDefaultLabel();
		
		for (MiddlewareProfile profile : MiddlewareProfile.values()) {
			if(MiddlewareProfile.getDefault() == profile){
				assertThat(renderer.getDisplayValue(profile)).contains("default, ");
			}else{

				assertThat(renderer.getDisplayValue(profile)).doesNotContain("default, ");
			}
		}
	}
	
}
