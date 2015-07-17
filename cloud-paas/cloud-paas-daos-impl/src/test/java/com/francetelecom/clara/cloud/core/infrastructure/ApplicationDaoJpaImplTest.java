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
package com.francetelecom.clara.cloud.core.infrastructure;

import java.util.Collection;
import java.util.HashSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.core.domain.ApplicationRepository;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.SSOId;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/services/application-context.xml" })
public class ApplicationDaoJpaImplTest {

	@Autowired
	private ApplicationRepository applicationRepository;

	@Before
	@Transactional
	public void setup() throws Exception {
		Assert.assertNotNull(applicationRepository);
	}

	@Test
	@Transactional
	public void shouldPersistApplication() {
		// test setup
		Application toBePersited = new Application("application-test-1", "code1");
		// test run
		applicationRepository.persist(toBePersited);
		// assertions
		Assert.assertNotNull("entity does not exist", applicationRepository.find(toBePersited.getId()));
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
		applicationRepository.persist(application);
		// assertions
		Assert.assertNotNull("entity does not exist", applicationRepository.find(application.getId()));
		applicationRepository.flush();
	}

	@Test
	@Transactional
	public void shouldRemoveApplication() {
		// test setup
		Application toBePersited = new Application("application-test-1", "code1");
		applicationRepository.persist(toBePersited);
		Assert.assertNotNull("entity does not exist", applicationRepository.find(toBePersited.getId()));
		// test run
		applicationRepository.remove(toBePersited);
		// assertions
		Assert.assertNull("entity should not exist", applicationRepository.find(toBePersited.getId()));
		applicationRepository.flush();
	}

	@Test
	@Transactional
	public void shouldFindApplicationByLabel() {
		// given an removed application with label aLabel and code aCode
		Application application1 = new Application("aLabel", "aCode");
		application1.markAsRemoved();
		applicationRepository.persist(application1);
		// given an application with label aLabel and code aCode
		Application application2 = new Application("aLabel", "aCode");
		applicationRepository.persist(application2);
		// when I find application with label aLabel
		Application result = applicationRepository.findByLabel("aLabel");
		// then I should get application with label aLabel and code aCode
		Assert.assertEquals(application2, result);
	}

	@Test
	@Transactional
	public void shouldFailToFindApplicationByUnknownLabel() {
		// when I find application with label unknown
		Application result = applicationRepository.findByLabel("unknown");
		// then I should get no application
		Assert.assertNull(result);
	}

	@Test
	@Transactional
	public void shouldFindApplicationByCode() {
		// given an removed application with label aLabel and code aCode
		Application application1 = new Application("aLabel", "aCode");
		application1.markAsRemoved();
		applicationRepository.persist(application1);
		// given an existing application with label aLabel and code aCode
		Application application2 = new Application("aLabel", "aCode");
		applicationRepository.persist(application2);
		// when I find application with code aCode
		Application result = applicationRepository.findByCode("aCode");
		// then I should get application with label aLabel and code aCode
		Assert.assertEquals(application2, result);
	}

	@Test
	@Transactional
	public void shouldFailToFindApplicationByUnknownCode() {
		// when I find application with label unknown
		Application result = applicationRepository.findByCode("unknown");
		// then I should get no application
		Assert.assertNull(result);
	}

	@Test
	@Transactional
	public void shouldFindApplicationByUID() {
		// given an existing application with label aLabel and code aCode
		Application application = new Application("alabel", "aCode");
		applicationRepository.persist(application);
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
		applicationRepository.persist(application1);
		applicationRepository.persist(application2);
		applicationRepository.persist(application3);
		// when I find all active applications
		Collection<Application> result = applicationRepository.findAll();
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
		applicationRepository.persist(application1);
		applicationRepository.persist(application2);
		applicationRepository.persist(application3);
		// when I find all active applications
		long result = applicationRepository.count();
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
		applicationRepository.persist(joyn);
		// given myOrange private application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.persist(myOrange);
		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.persist(elpaaso);
		// assertions
		// when I count all active applications
		Assert.assertEquals("there should be 0 entities", 0, applicationRepository.countByMember(new SSOId("jdalton")));
		Assert.assertEquals("there should be 1 entities", 1, applicationRepository.countByMember(new SSOId("alice123")));
		Assert.assertEquals("there should be 2 entities", 2, applicationRepository.countByMember(new SSOId("bob123")));

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
		applicationRepository.persist(joyn);
		// given myOrange private application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.persist(myOrange);
		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.persist(elpaaso);
		// assertions
		// when I find all active applications
		Assert.assertEquals("there should be 1 entities", 1, applicationRepository.findAllPublicOrPrivateByMember(new SSOId("jdalton")).size());
		Assert.assertEquals("there should be 2 entities", 2, applicationRepository.findAllPublicOrPrivateByMember(new SSOId("alice123")).size());
		Assert.assertEquals("there should be 3 entities", 3, applicationRepository.findAllPublicOrPrivateByMember(new SSOId("bob123")).size());
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
		applicationRepository.persist(joyn);
		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.persist(myOrange);
		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.persist(elpaaso);
		// assertions
		// when I find all active applications
		Assert.assertEquals("there should be 0 entities", 0, applicationRepository.findAllByMember(new SSOId("jdalton"), 0, 10, "code", "ASC").size());
		Assert.assertEquals("there should be 1 entities", 1, applicationRepository.findAllByMember(new SSOId("alice123"), 0, 10, "code", "ASC").size());
		Assert.assertEquals("there should be 2 entities", 2, applicationRepository.findAllByMember(new SSOId("bob123"), 0, 10, "code", "ASC").size());
	}
}
