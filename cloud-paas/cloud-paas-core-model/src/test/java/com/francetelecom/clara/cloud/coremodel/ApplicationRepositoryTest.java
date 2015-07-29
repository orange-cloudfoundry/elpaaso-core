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
package com.francetelecom.clara.cloud.coremodel;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static com.francetelecom.clara.cloud.coremodel.ApplicationSpecifications.*;
import static org.fest.assertions.Assertions.assertThat;
import static org.springframework.data.jpa.domain.Specifications.where;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/coremodel/application-context.xml" })
@TransactionConfiguration(defaultRollback = true)
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class ApplicationRepositoryTest {

	@Autowired
	private ApplicationRepository applicationRepository;


	@Test
	@Transactional
	public void shouldPersistApplication() {
		// test setup
		Application toBePersited = new Application("application-test-1", "code1");
		// test run
		applicationRepository.save(toBePersited);
		// assertions
		Assert.assertNotNull("entity does not exist", applicationRepository.findOne(toBePersited.getId()));
		applicationRepository.flush();
	}

	@Test
	@Transactional
	public void shouldPersistPrivateApplication() {
		// test setup
		Application application = new Application("application-test-1", "code1");
		HashSet<SSOId> members = new HashSet<>();
		members.add(new SSOId("bob123"));
		members.add(new SSOId("alice123"));
		application.setAsPrivate();
		application.setMembers(members);
		// test run
		applicationRepository.save(application);
		// assertions
		Assert.assertNotNull("entity does not exist", applicationRepository.findOne(application.getId()));
		applicationRepository.flush();
	}

	@Test
	@Transactional
	public void shouldRemoveApplication() {
		// test setup
		Application toBePersited = new Application("application-test-1", "code1");
		applicationRepository.save(toBePersited);
		Assert.assertNotNull("entity does not exist", applicationRepository.findOne(toBePersited.getId()));
		// test run
		applicationRepository.delete(toBePersited);
		// assertions
		Assert.assertNull("entity should not exist", applicationRepository.findOne(toBePersited.getId()));
		applicationRepository.flush();
	}

	@Test
	@Transactional
	public void shouldFindApplicationByLabel() {
		// given an removed application with label aLabel and code aCode
		Application application1 = new Application("aLabel", "aCode");
		application1.markAsRemoved();
		applicationRepository.save(application1);
		// given an application with label aLabel and code aCode
		Application application2 = new Application("aLabel", "aCode");
		applicationRepository.save(application2);
		// when I find application with label aLabel
		Application result = applicationRepository.findOne(where(isActive()).and(hasLabel("aLabel")));
		// then I should get application with label aLabel and code aCode
		Assert.assertEquals(application2, result);
	}

	@Test
	@Transactional
	public void shouldFailToFindApplicationByUnknownLabel() {
		// when I find application with label unknown
		Application result = applicationRepository.findOne(hasCode("unknown"));
		// then I should get no application
		Assert.assertNull(result);
	}

	@Test
	@Transactional
	public void shouldFindApplicationByCode() {
		// given an removed application with label aLabel and code aCode
		Application application1 = new Application("aLabel", "aCode");
		application1.markAsRemoved();
		applicationRepository.save(application1);
		// given an existing application with label aLabel and code aCode
		Application application2 = new Application("aLabel", "aCode");
		applicationRepository.save(application2);
		// when I find application with code aCode
		Application result = applicationRepository.findOne(where(isActive()).and(hasCode("aCode")));
		// then I should get application with label aLabel and code aCode
		Assert.assertEquals(application2, result);
	}

	@Test
	@Transactional
	public void shouldFailToFindApplicationByUnknownCode() {
		// when I find application with label unknown
		Application result = applicationRepository.findOne(hasCode("unknown"));
		// then I should get no application
		Assert.assertNull(result);
	}

	@Test
	@Transactional
	public void shouldFindApplicationByUID() {
		// given an existing application with label aLabel and code aCode
		Application application = new Application("alabel", "aCode");
		applicationRepository.save(application);
		applicationRepository.flush();
		// when I find this application by its uid
		Application result = applicationRepository.findByUid(application.getUID());
		// then I should get application with label aLabel and code aCode
		Assert.assertEquals(application, result);
	}

	@Test
	@Transactional
	public void shouldFailToFindApplicationByUnknownUID() {
		// when I find application with uid unknown
		Application result = applicationRepository.findByUid("unknown");
		// then I should get no application
		Assert.assertNull(result);
	}

	@Test
	@Transactional
	public void should_only_find_active_applications() {
		// given an existing application with label aLabel and code aCode
		Application application1 = new Application("aLabel", "aCode");
		// given a removed application with label anotherLabel and code
		// anotherCode
		Application application2 = new Application("anotherLabel", "anotherCode");
		application2.markAsRemoved();
		// given an existing application with label anotherLabel and code
		// anotherCode
		Application application3 = new Application("anotherLabel", "anotherCode");
		applicationRepository.save(application1);
		applicationRepository.save(application2);
		applicationRepository.save(application3);
		// when I find all active applications
		Collection<Application> result = applicationRepository.findAll(isActive());
		// then I should get 2 applications
		Assert.assertEquals("there should be 2 entities", 2, result.size());
		applicationRepository.flush();
	}

	@Test
	@Transactional
	public void should_only_count_active_applications() {
		// given an existing application with label aLabel and code aCode
		Application application1 = new Application("aLabel", "aCode");
		// given a removed application with label anotherLabel and code
		// anotherCode
		Application application2 = new Application("anotherLabel", "anotherCode");
		application2.markAsRemoved();
		// given an existing application with label anotherLabel and code
		// anotherCode
		Application application3 = new Application("anotherLabel", "anotherCode");
		applicationRepository.save(application1);
		applicationRepository.save(application2);
		applicationRepository.save(application3);
		// when I find all active applications
		long result = applicationRepository.count(isActive());
		// then I should get 2 applications
		Assert.assertEquals("there should be 2 entities", 2, result);
		applicationRepository.flush();
	}

	@Test
	@Transactional
	public void should_count_active_private_application_for_given_member() {
		// given joyn private application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given myOrange private application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// assertions
		// when I count all active applications
		Assert.assertEquals("there should be 0 entities", 0, applicationRepository.count(where(isActive()).and(hasForMember(new SSOId("jdalton")))));
		Assert.assertEquals("there should be 1 entities", 1, applicationRepository.count(where(isActive()).and(hasForMember(new SSOId("alice123")))));
		Assert.assertEquals("there should be 2 entities", 2, applicationRepository.count(where(isActive()).and(hasForMember(new SSOId("bob123")))));

	}

	@Test
	@Transactional
	public void should_find_accessible_applications_for_given_member() {
		// given joyn private application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given myOrange private application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// assertions
		// when I find all active applications
		Assert.assertEquals("there should be 1 entities", 1, applicationRepository.count(where(isActive()).and(isPublicOrHasForMember(new SSOId("jdalton")))));
		Assert.assertEquals("there should be 2 entities", 2, applicationRepository.count(where(isActive()).and(isPublicOrHasForMember(new SSOId("alice123")))));
		Assert.assertEquals("there should be 3 entities", 3, applicationRepository.count(where(isActive()).and(isPublicOrHasForMember(new SSOId("bob123")))));
	}

	@Test
	@Transactional
	public void should_find_ordered_active_applications_for_given_member() throws Exception {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// assertions
		// when I find all active applications
		Assert.assertEquals("there should be 0 entities", 0, applicationRepository.count(where(isActive()).and(hasForMember(new SSOId("jdalton")))));
		Assert.assertEquals("there should be 1 entities", 1, applicationRepository.count(where(isActive()).and(hasForMember(new SSOId("alice123")))));
		Assert.assertEquals("there should be 2 entities", 2, applicationRepository.count(where(isActive()).and(hasForMember(new SSOId("bob123")))));
	}

	@Test
	public void application_with_config_role_should_be_valid() {
		// Given
		Application application = new Application("application label","Application code");
		ConfigRole configRole = new ConfigRole("myapp");
		configRole.setLastModificationComment("Modified by Guillaume.");
		configRole.setValues(Arrays.asList(new ConfigValue("myconfigset", "mykey", "myvalue", "update mykey to its new value")));
		application.addConfigRole(configRole);

		// When
		applicationRepository.save(application);
		Application retrievedApplication = applicationRepository.findOne(application.getId());

		// Then
		assertThat(retrievedApplication.getCode()).isEqualTo(application.getCode());
		assertThat(retrievedApplication.getLabel()).isEqualTo(application.getLabel());
		assertThat(retrievedApplication.isPublic()).isEqualTo(application.isPublic());

	}

	@Test
	@Transactional
	public void application_with_valid_large_config_role_should_properly_persist() {
		// Given
		Application application = new Application("application label","Application code");
		ConfigRole configRole = new ConfigRole("myapp");
		configRole.setLastModificationComment("Modified by Guillaume.");

		String long300CharsComment= StringUtils.leftPad("comment", 300, 'X');
		configRole.setValues(Arrays.asList(new ConfigValue("myconfigset", "mykey", "myvalue", long300CharsComment)));
		application.addConfigRole(configRole);

		// When
		applicationRepository.save(application);
		Application retrievedApplication = applicationRepository.findOne(application.getId());

		// Then
		List<ConfigRole> reloadedConfigRoles = retrievedApplication.listConfigRoles();
		assertThat(reloadedConfigRoles.size()).isEqualTo(1);
		ConfigRole reloadedConfigRole = reloadedConfigRoles.get(0);
		List<ConfigValue> reloadedConfigValues = reloadedConfigRole.listValues();
		assertThat(reloadedConfigValues.size()).isEqualTo(1);
		ConfigValue reloadedConfigValue = reloadedConfigValues.get(0);
		assertThat(reloadedConfigValue.getConfigSet()).isEqualTo("myconfigset");
		assertThat(reloadedConfigValue.getKey()).isEqualTo("mykey");
		assertThat(reloadedConfigValue.getValue()).isEqualTo("myvalue");
		assertThat(reloadedConfigValue.getComment()).isEqualTo(long300CharsComment);
	}
}
