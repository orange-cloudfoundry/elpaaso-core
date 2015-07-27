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
import com.francetelecom.clara.cloud.commons.AuthorizationException;
import com.francetelecom.clara.cloud.commons.MissingDefaultUserException;
import com.francetelecom.clara.cloud.commons.NotFoundException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.core.service.exception.*;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.services.dto.ApplicationDTO;
import com.francetelecom.clara.cloud.services.dto.ConfigOverrideDTO;
import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Test Business implementation for Application component
 * 
 * @author Clara
 */
@RunWith(MockitoJUnitRunner.class)
public class ManageApplicationImplTest {

	private static final PaasUser JOE_DALTON = new PaasUser("Joe", "Dalton", new SSOId("jdalton"), "joe.dalton@alcatraz.com");

	@InjectMocks
	ManageApplicationImpl manageApplication;

	@Mock
	ApplicationRepository applicationRepository;
	@Mock
	ApplicationReleaseRepository applicationReleaseRepository;
	@Mock
	PaasUserRepository paasUserRepository;
	@Mock
    ConfigRoleRepository configRoleRepository;

	@After
	public void cleanSecurityContext() {
		TestHelper.logout();
	}

	@Test
	public void shouldCount2Applications() {
		// Given 2 applications exist
		Mockito.when(applicationRepository.count(any(Specification.class))).thenReturn(new Long(2));
		// when I count all applications
		Long count = manageApplication.countApplications();
		// then I should get 2 applications
		Assert.assertEquals(new Long(2), count);
	}

	@Test
	public void admin_user_should_count_private_applications_he_is_a_member_of() {
		// given bob is authenticated
		TestHelper.loginAsAdmin();
		// Given bob is a member of private application joyn
		Mockito.when(applicationRepository.count(any(Specifications.class))).thenReturn(new Long(1));

		// when bob count applications
		long count = manageApplication.countMyApplications();

		// then bob should count 1 application
		Assert.assertEquals(1, count);

	}

	@Test
	public void non_admin_user_should_count_private_applications_she_is_a_member_of() {
		// given alice is authenticated
		TestHelper.loginAsUser();
		// Given alice is a member of private application joyn
		Mockito.when(applicationRepository.count(any(Specifications.class))).thenReturn(new Long(1));

		// when Alice count applications
		long count = manageApplication.countMyApplications();

		// then Alice should count 1 application
		Assert.assertEquals(1, count);

	}

	@Test
	public void shouldCreateApplicationWithNonExistingCodeAndNonExistingLabel() throws Exception {
		// given bob is authenticated
		TestHelper.loginAsAdmin();
		// Given user "Joe Dalton"
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));
		// given no application with label aLabel exists
		Mockito.when(applicationRepository.findOne(any(Specifications.class))).thenReturn(null);
		// given no application with code aCode exists
		Mockito.when(applicationRepository.findOne(any(Specifications.class))).thenReturn(null);
		// when I create an application with label aLabel and code aCode
		manageApplication.createPublicApplication("aCode", "aLabel", null, null, new SSOId("jdalton"));
		// then application should be persisted
		Mockito.verify(applicationRepository).save(Matchers.isA(Application.class));
	}

	@Test(expected = DuplicateApplicationException.class)
	public void shouldFailToCreateApplicationWithExistingLabel() throws Exception {
		// given bob is authenticated
		TestHelper.loginAsAdmin();
		// Given user "Joe Dalton"
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));
		// given application with label aLabel exists
		Mockito.when(applicationRepository.findOne(any(Specifications.class))).thenReturn(new Application("aLabel", "aCode"));
		// when I create an application with label aLabel and code aCode
		manageApplication.createPublicApplication("aCode", "aLabel", null, null, new SSOId("jdalton"));
		// then it should fail
	}

	@Test(expected = DuplicateApplicationException.class)
	public void shouldFailToCreateApplicationWithExistingCode() throws Exception {
		// given bob is authenticated
		TestHelper.loginAsAdmin();
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));
		// given application with code aCode exists
		Mockito.when(applicationRepository.findOne(any(Specifications.class))).thenReturn(new Application("aLabel", "aCode"));
		// when I create an application with label aLabel and code aCode
		manageApplication.createPublicApplication("aCode", "aLabel", null, null, new SSOId("jdalton"));
	}

	@Test(expected = TechnicalException.class)
	public void shouldValidateApplicationWithTooLongDescription() throws Exception {
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));
		String description = TestHelper.generateOutOfLengthForDefaultString();
		manageApplication.createPublicApplication("aCode", "aLabel", description, null, new SSOId("jdalton"));
	}

	@Test(expected = TechnicalException.class)
	public void shouldValidateApplicationWithTooLongLabel() throws Exception {
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));
		String label = TestHelper.generateOutOfLengthForDefaultString();
		manageApplication.createPublicApplication("aCode", label, "description", null, new SSOId("jdalton"));
	}

	@Test(expected = TechnicalException.class)
	public void shouldValidateApplicationWithTooLongCode() throws Exception {
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));
		String code = TestHelper.generateOutOfLengthForDefaultString();
		manageApplication.createPublicApplication(code, "label", "description", null, new SSOId("jdalton"));
	}

	@Test
	public void should_delete_existing_application_with_no_active_releases() throws ApplicationNotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given application with label aLabel and code aCode exists
		Application application = new Application("aLabel", "aCode");
		Application spy = Mockito.spy(application);
		Mockito.when(applicationRepository.findByUid(application.getUID())).thenReturn(spy);
		// given application with label aLabel and code aCode has no active
		// release
		Mockito.when((applicationReleaseRepository).countApplicationReleasesByApplicationUID(spy.getUID())).thenReturn(new Long(0));
		// when I delete application with label aLabel and code aCode
		manageApplication.deleteApplication(spy.getUID());
		// then application should be set as removed
		Mockito.verify(spy).markAsRemoved();
	}

	@Test(expected = ApplicationNotFoundException.class)
	public void ShouldFailToDeleteUnknownApplication() throws ApplicationNotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given no application with uid unknown exists
		Mockito.when(applicationRepository.findByUid("unknown")).thenReturn(null);
		// when I delete application with uid unknown
		manageApplication.deleteApplication("unknown");
		// then It should failed
	}

	@Test(expected = IllegalStateException.class)
	public void fail_to_delete_existing_application_with_active_releases() throws ApplicationNotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given application with label aLabel and code aCode exists
		Application application = new Application("aLabel", "aCode");
		Mockito.when(applicationRepository.findByUid(application.getUID())).thenReturn(application);
		// given application with label aLabel and code aCode has 2 active
		// releases
		Mockito.when((applicationReleaseRepository).countApplicationReleasesByApplicationUID(application.getUID())).thenReturn(new Long(2));
		// when I delete application with label aLabel and code aCode
		manageApplication.deleteApplication(application.getUID());
		// then It should failed
	}

	@Test
	public void shouldFindApplicationByExistingUID() throws ApplicationNotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given application joyn
		Application joyn = new Application("joyn", "joyn");
		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
		// when I find application joyn by its UID
		Application result = manageApplication.findApplicationByUID(joyn.getUID());
		// then I should get application joyn
		Mockito.verify(applicationRepository).findByUid(joyn.getUID());
		Assert.assertEquals(joyn, result);
	}

	@Test
	public void admin_user_should_find_by_its_existing_uid_an_editable_application() throws ApplicationNotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> members = new HashSet<>();
		members.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(members);
		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
		// when I find application joyn by its UID
		Application result = manageApplication.findApplicationByUID(joyn.getUID());
		// then I should get application joyn
		Mockito.verify(applicationRepository).findByUid(joyn.getUID());
		Assert.assertEquals(joyn, result);
		Assert.assertTrue(joyn.isEditable());
	}

	@Test
	public void non_admin_user_should_find_by_its_existing_uid_an_editable_application_if_she_is_a_member_of() throws ApplicationNotFoundException {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// given Alice is a member of private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> members = new HashSet<>();
		members.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(members);
		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);

		// when Alice find application joyn by its UID
		Application result = manageApplication.findApplicationByUID(joyn.getUID());

		// then Alice should get application joyn
		Mockito.verify(applicationRepository).findByUid(joyn.getUID());
		Assert.assertEquals(joyn, result);
		// then application joyn should be editable
		Assert.assertTrue(joyn.isEditable());
	}

	@Test
	public void non_admin_user_should_find_by_its_existing_uid_a_non_editable_application_if_she_is_not_a_member_of()
			throws ApplicationNotFoundException {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// given Alice is a not member of private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> members = new HashSet<>();
		members.add(new SSOId("bob123"));
		joyn.setAsPrivate();
		joyn.setMembers(members);

		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);

		// when Alice find application joyn by its UID
		Application result = manageApplication.findApplicationByUID(joyn.getUID());

		// then Alice should get application joyn
		Mockito.verify(applicationRepository).findByUid(joyn.getUID());
		Assert.assertEquals(joyn, result);
		// then application joyn should not be editable
		Assert.assertFalse(joyn.isEditable());
	}

	@Test(expected = ApplicationNotFoundException.class)
	public void shouldFailToFindApplicationByUnkownUID() throws ApplicationNotFoundException {
		// given no application with UID unknown exists
		Mockito.when(applicationRepository.findByUid("unkown")).thenReturn(null);
		// when I find application with UID unknown
		manageApplication.findApplicationByUID("unkown");
		// then It should fail
	}

	@Test
	public void shouldFindApplicationByExistingLabel() throws ApplicationNotFoundException {
		// given application with label aLabel and code aCode exists
		Application application = new Application("aLabel", "aCode");
		Mockito.when(applicationRepository.findOne(any(Specifications.class))).thenReturn(application);
		// when I find application with label aLabel
		ApplicationDTO result = manageApplication.findApplicationByLabel(application.getLabel());
		// then I should get application with uid
		Assert.assertEquals(application.getUID(), result.getUid());
		// then I should get application with label aLabel
		Assert.assertEquals("aLabel", result.getLabel());
		// then I should get application with code aCode
		Assert.assertEquals("aCode", result.getCode());
	}

	@Test(expected = ApplicationNotFoundException.class)
	public void shouldFailToFindApplicationByUnkownLabel() throws ApplicationNotFoundException {
		// given no application with label unknown exists
		Mockito.when(applicationRepository.findOne(any(Specifications.class))).thenReturn(null);
		// when I find application with label unknown
		manageApplication.findApplicationByLabel("unkown");
		// then It should fail
	}

	@Test
	public void shouldUpdateExistingApplication() throws ApplicationNotFoundException, DuplicateApplicationException, PaasUserNotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given application with label aLabel and code aCode exists
		Application application = new Application("aLabel", "aCode");
		Mockito.when(applicationRepository.findByUid(application.getUID())).thenReturn(application);
		// when I update application
		application.setLabel("anotherLabel");
		application.setCode("anotherCode");
		manageApplication.updateApplication(application);
		// application should be updated
		Mockito.verify(applicationRepository).save(application);
	}

	@Test(expected = PaasUserNotFoundException.class)
	public void fail_to_update_application_with_unknown_members() throws ApplicationNotFoundException, DuplicateApplicationException,
			PaasUserNotFoundException, NotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given application with label aLabel and code aCode exists
		Application application = new Application("aLabel", "aCode");
		when(applicationRepository.findByUid(application.getUID())).thenReturn(application);
		when(paasUserRepository.findBySsoId(new SSOId("hacker"))).thenReturn(null);
		// when I update application
		Set<SSOId> members = new HashSet<>();
		members.add(new SSOId("hacker"));
		application.setAsPrivate();
		application.setMembers(members);
		manageApplication.updateApplication(application);
		// application should be updated
		Mockito.verify(applicationRepository).save(application);
	}

	@Test(expected = ApplicationNotFoundException.class)
	public void shouldFailToUpdateUnkwownApplication() throws ApplicationNotFoundException, DuplicateApplicationException, PaasUserNotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given no application with label aLabel and code aCode exists
		Application application = new Application("aLabel", "aCode");
		Mockito.when(applicationRepository.findByUid(application.getUID())).thenReturn(null);
		// when I update application
		application.setLabel("anotherLabel");
		application.setCode("anotherCode");
		manageApplication.updateApplication(application);
		// then It should fail
	}

	@Test(expected = DuplicateApplicationException.class)
	public void fail_to_update_application_with_existing_code() throws DuplicateApplicationException, ApplicationNotFoundException,
			PaasUserNotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given application with label aLabel and code aCode exists
		Application application = new Application("aLabel", "aCode");
		Mockito.when(applicationRepository.findByUid(application.getUID())).thenReturn(new Application("aLabel", "aCode"));
		// given application with code anotherCode exists
		Mockito.when(applicationRepository.findOne(any(Specifications.class))).thenReturn(new Application("anotherLabel", "anotherCode"));
		// when I update application
		application.setLabel("aLabel");
		application.setCode("anotherCode");
		manageApplication.updateApplication(application);
		// then It should fail
	}

	@Test(expected = DuplicateApplicationException.class)
	public void fail_to_update_application_with_existing_label() throws DuplicateApplicationException, ApplicationNotFoundException,
			PaasUserNotFoundException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// given application with label aLabel and code aCode exists
		Application application = new Application("aLabel", "aCode");
		Mockito.when(applicationRepository.findByUid(application.getUID())).thenReturn(new Application("aLabel", "aCode"));
		// given application with label anotherLabel exists
		Mockito.when(applicationRepository.findOne(any(Specifications.class))).thenReturn(new Application("anotherLabel", "anotherCode"));
		// when I update application
		application.setLabel("anotherLabel");
		application.setCode("aCode");
		manageApplication.updateApplication(application);
		// then It should fail
	}

	@Test(expected = ApplicationNotFoundException.class)
	public void ShouldBeUnableToDeleteUnknownApplication() throws ApplicationNotFoundException {
		// given no application with uid unknown exists
		Mockito.when(applicationRepository.findByUid("unknown")).thenReturn(null);
		// when I ask if application with uid unknown can be deleted
		boolean result = manageApplication.canBeDeleted("unknown");
		// then I should be unable to delete application with uid unknown
		Assert.assertFalse(result);
	}

	@Test
	public void created_public_Application_should_be_public() throws Exception {
		// given bob is authenticated
		TestHelper.loginAsAdmin();

		ManageApplication manageApplication = spy(this.manageApplication);
		// Given user "Joe Dalton"
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));

		// When I create an application without specifying its visibility and
		// creator
		manageApplication.createPublicApplication("code", "label", "desc", null, new SSOId("jdalton"));

		// Then detailed create application service should be called with public
		// visibility and no creator
		verify(manageApplication).createPublicApplication("code", "label", "desc", null, new SSOId("jdalton"));
	}

	@Test
	public void created_private_Application_should_be_private() throws Exception {
		// given bob is authenticated
		TestHelper.loginAsAdmin();

		// Given user "Joe Dalton"
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));

		// when Joe creates a private application

		manageApplication.createPrivateApplication("code", "label", "description", null, new SSOId("jdalton"));

		// Then the persisted application should be private
		ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
		verify(applicationRepository).save(captor.capture());
		Application app = captor.getValue();

		assertEquals("isPublic", false, app.isPublic());
	}

	@Test
	public void created_private_Application_should_contain_specified_members() throws Exception {
		// given bob is authenticated
		TestHelper.loginAsAdmin();

		// Given user "Joe Dalton"
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));

		// when Joe creates a private application
		manageApplication.createPrivateApplication("code", "label", "description", null, new SSOId("jdalton"));

		// Then the persisted application should have "Joe Dalton" as member
		ArgumentCaptor<Application> captor = ArgumentCaptor.forClass(Application.class);
		verify(applicationRepository).save(captor.capture());
		Application app = captor.getValue();

		assertTrue("user that creates the application should be a member", app.listMembers().contains(new SSOId("jdalton")));
	}

	@Test(expected = PaasUserNotFoundException.class)
	public void fail_to_create_a_private_application_with_unknown_member() throws Exception {
		// Given user "jdalton" ssoid does not exists
		Mockito.doReturn(null).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));

		// When I create a private application with "jdalton" as a member
		manageApplication.createPrivateApplication("code", "label", "description", null, new SSOId("jdalton"));

		// Then it should fail
	}

	@Test(expected=MissingDefaultUserException.class)
	public void fail_to_create_a_private_application_with_no_member() throws Exception {
		// When I create a private application without specifying a user
		manageApplication.createPrivateApplication("code", "label", "description", null);
	}

	@Test(expected=MissingDefaultUserException.class)
	public void fail_to_create_a_public_application_with_no_member() throws Exception {
		// When I create a private application without specifying a user
		manageApplication.createPublicApplication("code", "label", "description", null);
	}
	
	@Test
	public void non_admin_user_should_see_public_and_private_applications_she_is_a_member_of_as_accessible() {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// Given Alice is a member of private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);

		// Given a public application elPaaso
		Application elPaaso = new Application("elPaaso", "elPaaso");

		List<Application> result = new ArrayList<Application>();
		result.add(joyn);
		result.add(elPaaso);
		Mockito.when(applicationRepository.findAll(any(Specifications.class))).thenReturn(result);

		// when Alice find all applications
		Collection<Application> applications = manageApplication.findAccessibleApplications();
		// then Alice should get 2 applications
		Assert.assertEquals(2, applications.size());
		// then Alice should get application joyn
		Assert.assertTrue(applications.contains(joyn));
		// then Alice should get application elPaaso
		Assert.assertTrue(applications.contains(elPaaso));
		// then application joyn should be editable
		Assert.assertTrue(joyn.isEditable());
		// then public application elPaaso should not be editable
		Assert.assertFalse(elPaaso.isEditable());
	}

	@Test
	public void non_admin_user_should_only_see_as_her_own_pageable_applications_she_is_a_member_of() {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// Given Alice is a member of private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);

		// Given Alice is a member of private application myOrange
		Application myOrange = new Application("myOrange", "myOrange");
		Set<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(myOrangeMembers);

		List<Application> result = new ArrayList<Application>();
		result.add(joyn);
		result.add(myOrange);
		Mockito.when(
				applicationRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(result);

		// when Alice find all applications
		Collection<Application> applications = manageApplication.findMyApplications();
		// then Alice should get 2 applications
		Assert.assertEquals(2, applications.size());
		// then Alice should get application joyn
		Assert.assertTrue(applications.contains(joyn));
		// then Alice should get application myOrange
		Assert.assertTrue(applications.contains(myOrange));
		// then application joyn should be editable
		Assert.assertTrue(joyn.isEditable());
		// then application myOrange should be editable
		Assert.assertTrue(myOrange.isEditable());
	}

	@Test
	public void admin_user_should_see_as_her_own_pageable_applications_she_is_a_member_of() {
		// given Alice is authenticated
		TestHelper.loginAsAdmin();
		// Given Alice is a member of private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		// Given Alice is a member of private application myOrange
		Application myOrange = new Application("myOrange", "myOrange");
		Set<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);

		List<Application> result = new ArrayList<Application>();
		result.add(joyn);
		result.add(myOrange);
		Mockito.when(
				applicationRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(result);

		// when Alice find all applications
		Collection<Application> applications = manageApplication.findMyApplications();
		// then Alice should get 2 applications
		Assert.assertEquals(2, applications.size());
		// then Alice should get application joyn
		Assert.assertTrue(applications.contains(joyn));
		// then Alice should get application myOrange
		Assert.assertTrue(applications.contains(myOrange));
		// then application joyn should be editable
		Assert.assertTrue(joyn.isEditable());
		// then application myOrange should be editable
		Assert.assertTrue(myOrange.isEditable());
	}

	@Test
	public void admin_user_should_see_all_applications_as_his_own() {
		// given Alice is authenticated
		TestHelper.loginAsAdmin();
		// Given Alice is a member of private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		// Given Alice is a member of private application myOrange
		Application myOrange = new Application("myOrange", "myOrange");
		Set<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);

		List<Application> result = new ArrayList<Application>();
		result.add(joyn);
		result.add(myOrange);
		Mockito.when(applicationRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(result);

		// when Alice find all applications
		Collection<Application> applications = manageApplication.findMyApplications();
		// then Alice should get 2 applications
		Assert.assertEquals(2, applications.size());
		// then Alice should get application joyn
		Assert.assertTrue(applications.contains(joyn));
		// then Alice should get application myOrange
		Assert.assertTrue(applications.contains(myOrange));
		// then application joyn should be editable
		Assert.assertTrue(joyn.isEditable());
		// then application myOrange should be editable
		Assert.assertTrue(myOrange.isEditable());
	}

	@Test
	public void non_admin_user_should_only_see_as_editable_private_appplications_she_is_a_member_of() {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// Given Alice is a member of private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		// Given Alice is a not member of private application myOrange
		Application myOrange = new Application("myOrange", "myOrange");
		Set<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		// Given a public application elPaaso
		Application elPaaso = new Application("elPaaso", "elPaaso");

		List<Application> result = new ArrayList<Application>();
		result.add(joyn);
		result.add(myOrange);
		result.add(elPaaso);
		Mockito.when(applicationRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(result);

		// when Alice find all applications
		Collection<Application> applications = manageApplication.findApplications();
		// then Alice should see all applications
		Assert.assertEquals(3, applications.size());
		// then Alice should get application joyn
		Assert.assertTrue(applications.contains(joyn));
		// then Alice should get application myOrange
		Assert.assertTrue(applications.contains(myOrange));
		// then Alice should get application myOrange
		Assert.assertTrue(applications.contains(elPaaso));
		// then application joyn should be editable
		Assert.assertTrue(joyn.isEditable());
		// then application myOrange should not be editable
		Assert.assertFalse(myOrange.isEditable());
		// then application elPaaso should not be editable either
		Assert.assertFalse(elPaaso.isEditable());
	}

	@Test
	public void admin_user_should_see_all_applications_as_accessible() {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// Given Bob is a member of private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		// Given Bob is a member of public application elPaaso
		Application elPaaso = new Application("elPaaso", "elPaaso");

		List<Application> result = new ArrayList<Application>();
		result.add(joyn);
		result.add(elPaaso);
		Mockito.when(applicationRepository.findAll(any(Specification.class),any(Sort.class))).thenReturn(result);

		// when Bob find all applications
		Collection<Application> applications = manageApplication.findAccessibleApplications();
		// then bob should get 2 applications
		Assert.assertEquals(2, applications.size());
		// then bob should get application joyn
		Assert.assertTrue(applications.contains(joyn));
		// then bob should get application elpaaso
		Assert.assertTrue(applications.contains(elPaaso));
		// then application joyn should be editable
		Assert.assertTrue(joyn.isEditable());
		// then application elPaaso should be editable
		Assert.assertTrue(elPaaso.isEditable());
	}

	@Test
	public void admin_user_should_see_all_pageable_applications() {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// Given Bob is a member of private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		// Given Bob is a member of public application elPaaso
		Application elPaaso = new Application("elPaaso", "elPaaso");

		List<Application> result = new ArrayList<Application>();
		result.add(joyn);
		result.add(elPaaso);
		Mockito.when(applicationRepository.findAll(any(Specification.class), any(Sort.class))).thenReturn(result);

		// when Bob find all applications
		Collection<Application> applications = manageApplication.findApplications();
		// then bob should get 2 applications
		Assert.assertEquals(2, applications.size());
		// then bob should get application joyn
		Assert.assertTrue(applications.contains(joyn));
		// then bob should get application elpaaso
		Assert.assertTrue(applications.contains(elPaaso));
	}

	@Test
	public void non_admin_user_can_edit_a_private_application_she_is_a_member_of() throws ApplicationNotFoundException, PaasUserNotFoundException,
			DuplicateApplicationException {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// Given a private application joyn
		Application joyn = new Application("joyn", "joyn");
		// given Alice is a member of application joyn
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);

		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("alice123"));
		// when Alice update application joyn
		joyn.setLabel("anotherLabel");
		manageApplication.updateApplication(joyn);

		// it should succeed
	}

	@Test(expected = AuthorizationException.class)
	public void non_admin_user_can_not_edit_a_public_application_she_is_not_member_of() throws ApplicationNotFoundException,
			PaasUserNotFoundException, DuplicateApplicationException {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// given a public application elpaaso
		Application joyn = new Application("elpaaso", "elpaaso");
		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);

		// when Alice update application joyn
		joyn.setLabel("anotherLabel");
		manageApplication.updateApplication(joyn);

		// it should succeed
	}

	@Test(expected = AuthorizationException.class)
	public void non_admin_user_fail_to_edit_a_private_application_she_is_not_a_member_of() throws ApplicationNotFoundException,
			PaasUserNotFoundException, DuplicateApplicationException {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// Given a private application joyn
		Application joyn = new Application("joyn", "joyn");
		// given Alice is not a member of application joyn
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("jdalton"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);

		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));


		// when Alice update application joyn
		joyn.setLabel("anotherLabel");
		manageApplication.updateApplication(joyn);

		// it should fail
	}

	@Test(expected = AuthorizationException.class)
	public void non_admin_user_can_not_delete_a_public_application_she_is_not_member_of() throws ApplicationNotFoundException,
			PaasUserNotFoundException, DuplicateApplicationException {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// given a public application elpaaso
		Application joyn = new Application("elpaaso", "elpaaso");
		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);

		// when Alice ask if she can delete public application joyn
		boolean canBeDeleted = manageApplication.canBeDeleted(joyn.getUID());
		// then it should be possible
		Assert.assertFalse(canBeDeleted);

		// when Alice delete public application joyn
		manageApplication.deleteApplication(joyn.getUID());
		// it should succeed
	}

	@Test(expected = AuthorizationException.class)
	public void non_admin_user_fail_to_delete_a_private_application_she_is_not_a_member_of() throws ApplicationNotFoundException,
			PaasUserNotFoundException, DuplicateApplicationException {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// Given a private application joyn
		Application joyn = new Application("joyn", "joyn");
		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
		// given Alice is not a member of application joyn
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);

		// when Alice ask if she can delete public application joyn
		boolean canBeDeleted = manageApplication.canBeDeleted(joyn.getUID());
		// then it should not be possible
		Assert.assertFalse(canBeDeleted);

		// when Alice delete private application joyn
		manageApplication.deleteApplication(joyn.getUID());

		// it should fail
	}

	@Test
	public void non_admin_user_can_delete_a_private_application_she_is_a_member_of() throws ApplicationNotFoundException, PaasUserNotFoundException,
			DuplicateApplicationException {
		// given Alice is authenticated
		TestHelper.loginAsUser();
		// Given a private application joyn
		Application joyn = new Application("joyn", "joyn");
		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
		// given Alice is a member of application joyn
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);

		// when Alice ask if she can delete public application joyn
		boolean canBeDeleted = manageApplication.canBeDeleted(joyn.getUID());
		// then it should be possible
		Assert.assertTrue(canBeDeleted);

		// when Alice delete application joyn
		manageApplication.deleteApplication(joyn.getUID());

		// it should succeed
	}

	@Test
	public void admin_user_can_edit_a_private_application() throws ApplicationNotFoundException, PaasUserNotFoundException,
			DuplicateApplicationException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// Given a private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("jdalton"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
		doReturn(JOE_DALTON).when(paasUserRepository).findBySsoId(new SSOId("jdalton"));


		// when bob update application joyn
		joyn.setLabel("anotherLabel");
		manageApplication.updateApplication(joyn);

		// it should succeed
	}

	@Test
	public void admin_user_can_edit_a_public_application() throws ApplicationNotFoundException, PaasUserNotFoundException,
			DuplicateApplicationException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// Given a public application elpaaso
		Application elpaaso = new Application("elpaaso", "elpaaso");
		Mockito.when(applicationRepository.findByUid(elpaaso.getUID())).thenReturn(elpaaso);

		// when bob update application joyn
		elpaaso.setLabel("anotherLabel");
		manageApplication.updateApplication(elpaaso);

		// it should succeed
	}

	@Test
	public void admin_user_can_delete_a_private_application() throws ApplicationNotFoundException, PaasUserNotFoundException,
			DuplicateApplicationException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// Given a private application joyn
		Application joyn = new Application("joyn", "joyn");
		Set<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);

		// when Bob ask if he can delete public application joyn
		boolean canBeDeleted = manageApplication.canBeDeleted(joyn.getUID());
		// then it should be possible
		Assert.assertTrue(canBeDeleted);

		// when bob delete application joyn
		manageApplication.deleteApplication(joyn.getUID());

		// it should succeed
	}

	@Test
	public void admin_user_can_delete_a_public_application() throws ApplicationNotFoundException, PaasUserNotFoundException,
			DuplicateApplicationException {
		// given Bob is authenticated
		TestHelper.loginAsAdmin();
		// Given a public application elpaaso
		Application elpaaso = new Application("elpaaso", "elpaaso");
		Mockito.when(applicationRepository.findByUid(elpaaso.getUID())).thenReturn(elpaaso);

		// when Bob ask if he can delete public application joyn
		boolean canBeDeleted = manageApplication.canBeDeleted(elpaaso.getUID());
		// then it should be possible
		Assert.assertTrue(canBeDeleted);

		// when bob delete application elpaaso
		manageApplication.deleteApplication(elpaaso.getUID());

		// it should succeed
	}

    @Test
    public void member_user_can_create_config_roles() throws ApplicationNotFoundException, InvalidConfigOverrideException, ConfigRoleNotFoundException {
        TestHelper.loginAsUser(); // given Bob is authenticated

        Application joyn = new Application("joyn", "joyn");
        Set<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(new SSOId(TestHelper.USER_WITH_USER_ROLE_SSOID.getValue()));
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        String applicationUID = joyn.getUID();
        Mockito.when(applicationRepository.findByUid(applicationUID)).thenReturn(joyn);

        //given valid config role
        List<ConfigOverrideDTO> overrideConfigs = new ArrayList<>();
        ConfigOverrideDTO configOverrideDTO = new ConfigOverrideDTO("configSet", "key", "value", "comment");
        overrideConfigs.add(configOverrideDTO);

        //when
        String configRoleUID = manageApplication.createConfigRole(applicationUID, "role label", overrideConfigs);

        // then request is not rejected
        // (asserts on proper saving of the role are in integration tests, see ApplicationLifeCycleTest
    }

    @Test(expected = AuthorizationException.class)
    public void non_member_user_can_not_create_config_roles() throws ApplicationNotFoundException, InvalidConfigOverrideException, ConfigRoleNotFoundException {
        TestHelper.loginAsUser(); // given Bob is authenticated

        Application joyn = new Application("joyn", "joyn");
        joyn.setAsPrivate();
        String applicationUID = joyn.getUID();
        Mockito.when(applicationRepository.findByUid(applicationUID)).thenReturn(joyn);

        //given valid config role
        List<ConfigOverrideDTO> emptyOverrideConfigs = new ArrayList<>();

        //when
        manageApplication.createConfigRole(applicationUID, "role label", emptyOverrideConfigs);
    }

    @Test
    public void syntaxically_invalid_config_roles_are_rejected() throws ApplicationNotFoundException {
        TestHelper.loginAsUser(); // given Bob is authenticated

        Application elpaaso = new Application("elpaaso", "elpaaso");
        Set<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(new SSOId(TestHelper.USER_WITH_USER_ROLE_SSOID.getValue()));
        elpaaso.setAsPrivate();
        elpaaso.setMembers(joynMembers);
        Mockito.when(applicationRepository.findByUid(elpaaso.getUID())).thenReturn(elpaaso);

        Mockito.when(applicationRepository.findByUid(elpaaso.getUID())).thenReturn(elpaaso);

        //given invalid config role: too large comment
        List<ConfigOverrideDTO> overrideConfigs = new ArrayList<>();
        String tooLargeComment = StringUtils.rightPad("value", ConfigOverrideDTO.MAX_CONFIG_VALUE_LENGTH + 2, 'X');
        ConfigOverrideDTO configOverrideDTO = new ConfigOverrideDTO("configSet", "key", tooLargeComment, "comment");
        overrideConfigs.add(configOverrideDTO);

        //when
        try {
            manageApplication.createConfigRole(elpaaso.getUID(), "role label", overrideConfigs);
            fail("expected config role to be rejected with invalid config role");
        } catch (InvalidConfigOverrideException e) {
            Assert.assertTrue(e.getFaultyOverride() == configOverrideDTO);
        }


    }

}