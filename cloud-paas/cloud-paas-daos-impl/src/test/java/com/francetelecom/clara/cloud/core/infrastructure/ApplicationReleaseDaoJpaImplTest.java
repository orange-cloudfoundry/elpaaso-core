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

import com.francetelecom.clara.cloud.core.domain.ApplicationReleaseRepository;
import com.francetelecom.clara.cloud.coremodel.ApplicationRepository;
import com.francetelecom.clara.cloud.coremodel.PaasUserRepository;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.logicalmodel.LogicalQueueReceiveService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/services/application-context.xml" })
public class ApplicationReleaseDaoJpaImplTest {

	private PaasUser manager;

	@Autowired
	private ApplicationReleaseRepository applicationReleaseRepository;

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private PaasUserRepository paasUserRepository;

	private Application application;

	/*
	 * @Autowired DataSource dataSource;
	 */
	@Before
	@Transactional
	public void setup() throws Exception {
		Assert.assertNotNull(applicationReleaseRepository);
		application = new Application("aLabel", "aCode");
		applicationRepository.save(application);
		applicationRepository.flush();
		// given bob paas user
		manager = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
		paasUserRepository.save(manager);
		paasUserRepository.flush();

	}

	@Test
	@Transactional
	public void shouldFailToFindApplicationReleaseByUnknownUID() {
		// when I want to find a release using an unknown uid
		ApplicationRelease release = applicationReleaseRepository.findByUID("dummy");
		// then I should get this release
		Assert.assertNull("release should not exist", release);
	}

	@Test
	@Transactional
	public void shouldFindApplicationReleaseByExistingUID() {
		// given a persisted application with label myLabel and code myCode
		Application application = new Application("alabel", "aCode");
		applicationRepository.save(application);
		// given a persisted release with version aVersion of application
		ApplicationRelease release = new ApplicationRelease(application, "aVersion");
		applicationReleaseRepository.persist(release);
		applicationRepository.flush();
		// when I want to find this release by its uid
		ApplicationRelease entity = applicationReleaseRepository.findByUID(release.getUID());
		// then I should get this release
		Assert.assertNotNull("cannot find release by its uid", entity);
	}

	@Test
	@Transactional
	public void testPersist() throws MalformedURLException {
		ApplicationRelease toBePersited = new ApplicationRelease(application, "aVersion");
		toBePersited.setVersionControlUrl(new URL("file://url.txt"));
		// test run
		applicationReleaseRepository.persist(toBePersited);
		// assertions
		Assert.assertNotNull("entity does not exist", applicationReleaseRepository.find(toBePersited.getId()));
		applicationReleaseRepository.flush();
	}

	@Test
	@Transactional
	public void testRemove() throws MalformedURLException {
		// test setup
		ApplicationRelease applicationRelease = new ApplicationRelease(application, "G1R0C0");
		applicationRelease.setVersionControlUrl(new URL("file://url.txt"));

		applicationReleaseRepository.persist(applicationRelease);
		applicationReleaseRepository.flush();
		Assert.assertNotNull("entity does not exist", applicationReleaseRepository.find(applicationRelease.getId()));
		// test run
		applicationReleaseRepository.remove(applicationRelease);
		// assertions
		Assert.assertNull("entity should not exist", applicationReleaseRepository.find(applicationRelease.getId()));
	}

	@Test
	@Transactional
	public void testFind() throws MalformedURLException {
		// test setup
		ApplicationRelease toBePersited = new ApplicationRelease(application, "G1R0C0");
		toBePersited.setVersionControlUrl(new URL("file://url.txt"));

		applicationReleaseRepository.persist(toBePersited);
		// test run
		ApplicationRelease entity = applicationReleaseRepository.find(toBePersited.getId());
		// assertions
		Assert.assertNotNull("entity does not exist", entity);
		Assert.assertEquals("G1R0C0", entity.getReleaseVersion());
		applicationReleaseRepository.flush();
	}

	@Test
	@Transactional
	public void should_find_all_releases() throws MalformedURLException {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		List<ApplicationRelease> releases = applicationReleaseRepository.findAll(0, Integer.MAX_VALUE);
		Assert.assertNotNull("entities should not be null", releases);
		Assert.assertEquals("there should be 6 releases", 6, releases.size());
		Assert.assertTrue("entities should contain joyn_1_0", releases.contains(joyn_1_0));
		Assert.assertTrue("entities should contain joyn_2_0", releases.contains(joyn_2_0));
		Assert.assertTrue("entities should contain joyn_3_0", releases.contains(joyn_3_0));
		Assert.assertTrue("entities should contain myOrange_1_0", releases.contains(myOrange_1_0));
		Assert.assertTrue("entities should contain myOrange_2_0", releases.contains(myOrange_2_0));
		Assert.assertTrue("entities should contain elpaaso_1_0", releases.contains(elpaaso_1_0));
	}

	@Test
	@Transactional
	public void should_only_find_active_paged_releases() throws MalformedURLException {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		joyn_1_0.markAsRemoved();
		joyn_2_0.markAsRemoved();
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);
		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		myOrange_1_0.markAsRemoved();
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		List<ApplicationRelease> releases = applicationReleaseRepository.findAll(0, Integer.MAX_VALUE);
		Assert.assertNotNull("entities should not be null", releases);
		Assert.assertEquals("there should be 3 releases", 3, releases.size());
		Assert.assertFalse("entities should not contain joyn_1_0", releases.contains(joyn_1_0));
		Assert.assertFalse("entities should not contain joyn_2_0", releases.contains(joyn_2_0));
		Assert.assertTrue("entities should contain joyn_3_0", releases.contains(joyn_3_0));
		Assert.assertFalse("entities should not contain myOrange_1_0", releases.contains(myOrange_1_0));
		Assert.assertTrue("entities should contain myOrange_2_0", releases.contains(myOrange_2_0));
		Assert.assertTrue("entities should contain elpaaso_1_0", releases.contains(elpaaso_1_0));
	}

	@Test
	@Transactional
	public void should_only_find_active_releases() throws MalformedURLException {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		joyn_1_0.markAsRemoved();
		joyn_2_0.markAsRemoved();
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);
		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		myOrange_1_0.markAsRemoved();
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		List<ApplicationRelease> releases = applicationReleaseRepository.findAll();
		Assert.assertNotNull("entities should not be null", releases);
		Assert.assertEquals("there should be 3 releases", 3, releases.size());
		Assert.assertFalse("entities should not contain joyn_1_0", releases.contains(joyn_1_0));
		Assert.assertFalse("entities should not contain joyn_2_0", releases.contains(joyn_2_0));
		Assert.assertTrue("entities should contain joyn_3_0", releases.contains(joyn_3_0));
		Assert.assertFalse("entities should not contain myOrange_1_0", releases.contains(myOrange_1_0));
		Assert.assertTrue("entities should contain myOrange_2_0", releases.contains(myOrange_2_0));
		Assert.assertTrue("entities should contain elpaaso_1_0", releases.contains(elpaaso_1_0));
	}

	@Test
	@Transactional
	public void should_count_all_releases() throws MalformedURLException {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		Assert.assertEquals("there should be 6 releases", 6, applicationReleaseRepository.countApplicationReleases());

	}

	@Test
	@Transactional
	public void should_find_public_releases_or_private_releases_for_given_member() throws Exception {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		List<ApplicationRelease> releases = applicationReleaseRepository.findAllPublicOrPrivateByMember(new SSOId("alice123"), 0, Integer.MAX_VALUE);
		Assert.assertNotNull("entities should not be null", releases);
		Assert.assertEquals("there should be 4 releases", 4, releases.size());
		Assert.assertTrue("entities should contain joyn_1_0", releases.contains(joyn_1_0));
		Assert.assertTrue("entities should contain joyn_2_0", releases.contains(joyn_2_0));
		Assert.assertTrue("entities should contain joyn_3_0", releases.contains(joyn_3_0));
		Assert.assertFalse("entities should not contain myOrange_1_0", releases.contains(myOrange_1_0));
		Assert.assertFalse("entities should not contain myOrange_2_0", releases.contains(myOrange_2_0));
		Assert.assertTrue("entities should contain elpaaso_1_0", releases.contains(elpaaso_1_0));
	}

	@Test
	@Transactional
	public void should_find_public_releases_or_private_releases_for_given_member_and_app() throws Exception {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		List<ApplicationRelease> releases = applicationReleaseRepository.findPublicOrPrivateByMemberAndByAppUID(new SSOId("alice123"), joyn.getUID(),
				0, Integer.MAX_VALUE);
		Assert.assertNotNull("entities should not be null", releases);
		Assert.assertEquals("there should be 3 releases", 3, releases.size());
		Assert.assertTrue("entities should contain joyn_1_0", releases.contains(joyn_1_0));
		Assert.assertTrue("entities should contain joyn_2_0", releases.contains(joyn_2_0));
		Assert.assertTrue("entities should contain joyn_3_0", releases.contains(joyn_3_0));

		releases = applicationReleaseRepository
				.findPublicOrPrivateByMemberAndByAppUID(new SSOId("alice123"), myOrange.getUID(), 0, Integer.MAX_VALUE);
		Assert.assertEquals("there should be NO releases", 0, releases.size());
		Assert.assertFalse("entities should not contain myOrange_1_0", releases.contains(myOrange_1_0));
		Assert.assertFalse("entities should not contain myOrange_2_0", releases.contains(myOrange_2_0));

		releases = applicationReleaseRepository.findPublicOrPrivateByMemberAndByAppUID(new SSOId("alice123"), elpaaso.getUID(), 0, Integer.MAX_VALUE);
		Assert.assertEquals("there should be 1 release", 1, releases.size());
		Assert.assertTrue("entities should contain elpaaso_1_0", releases.contains(elpaaso_1_0));
	}

	@Test
	@Transactional
	public void should_count_public_releases_or_private_releases_for_given_member_and_app() throws Exception {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		Assert.assertEquals("there should be 3 releases", 3,
				applicationReleaseRepository.countPublicOrPrivateByMemberAndByAppUID(new SSOId("alice123"), joyn.getUID()));
		Assert.assertEquals("there should be 3 releases", 0,
				applicationReleaseRepository.countPublicOrPrivateByMemberAndByAppUID(new SSOId("alice123"), myOrange.getUID()));
		Assert.assertEquals("there should be 3 releases", 1,
				applicationReleaseRepository.countPublicOrPrivateByMemberAndByAppUID(new SSOId("alice123"), elpaaso.getUID()));
	}

	@Test
	@Transactional
	public void should_not_find_removed_public_releases_or_removed_private_releases_for_given_member() throws Exception {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		joyn_1_0.markAsRemoved();
		myOrange_1_0.markAsRemoved();
		elpaaso_1_0.markAsRemoved();
		applicationReleaseRepository.flush();

		List<ApplicationRelease> releases = applicationReleaseRepository.findAllPublicOrPrivateByMember(new SSOId("alice123"), 0, Integer.MAX_VALUE);

		Assert.assertNotNull("entities should not be null", releases);
		Assert.assertEquals("there should be 2 releases", 2, releases.size());
		Assert.assertFalse("entities should not contain removed joyn_1_0", releases.contains(joyn_1_0));
		Assert.assertTrue("entities should contain joyn_2_0", releases.contains(joyn_2_0));
		Assert.assertTrue("entities should contain joyn_3_0", releases.contains(joyn_3_0));
		Assert.assertFalse("entities should not contain myOrange_1_0", releases.contains(myOrange_1_0));
		Assert.assertFalse("entities should not contain myOrange_2_0", releases.contains(myOrange_2_0));
		Assert.assertFalse("entities should not contain removed elpaaso_1_0", releases.contains(elpaaso_1_0));
	}

	@Test
	@Transactional
	public void should_count_public_releases_or_private_releases_for_given_member() throws Exception {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		Assert.assertEquals("there should be 4 releases", 4, applicationReleaseRepository.countPublicOrPrivateByMember(new SSOId("alice123")));

	}

	@Test
	@Transactional
	public void should_not_count_removed_public_releases_or_removed_private_releases_for_given_member() throws Exception {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		joyn_1_0.markAsRemoved();
		myOrange_1_0.markAsRemoved();
		elpaaso_1_0.markAsRemoved();
		applicationReleaseRepository.flush();

		Assert.assertEquals("there should be 2 releases", 2, applicationReleaseRepository.countPublicOrPrivateByMember(new SSOId("alice123")));

	}

	@Test
	@Transactional
	public void should_find_releases_of_private_applications_a_given_user_is_member_of() throws MalformedURLException {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		List<ApplicationRelease> releases = (List<ApplicationRelease>) applicationReleaseRepository.findAllByApplicationMember(new SSOId("bob123"), 0, Integer.MAX_VALUE);
		Assert.assertNotNull("entities should not be null", releases);
		Assert.assertEquals("there should be 5 releases", 5, releases.size());
		Assert.assertTrue("entities should contain joyn_1_0", releases.contains(joyn_1_0));
		Assert.assertTrue("entities should contain joyn_2_0", releases.contains(joyn_2_0));
		Assert.assertTrue("entities should contain joyn_3_0", releases.contains(joyn_3_0));
		Assert.assertTrue("entities should contain myOrange_1_0", releases.contains(myOrange_1_0));
		Assert.assertTrue("entities should contain myOrange_2_0", releases.contains(myOrange_2_0));
		Assert.assertFalse("entities should not contain elpaaso_1_0", releases.contains(elpaaso_1_0));

	}

	@Test
	@Transactional
	public void should_count_releases_of_private_applications_a_given_user_is_member_of() throws MalformedURLException {
		// given joyn application
		Application joyn = new Application("joyn", "joyn");
		HashSet<SSOId> joynMembers = new HashSet<>();
		joynMembers.add(new SSOId("bob123"));
		joynMembers.add(new SSOId("alice123"));
		joyn.setAsPrivate();
		joyn.setMembers(joynMembers);
		applicationRepository.save(joyn);
		// given releases of application joyn
		ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
		ApplicationRelease joyn_2_0 = new ApplicationRelease(joyn, "2.0");
		ApplicationRelease joyn_3_0 = new ApplicationRelease(joyn, "3.0");
		applicationReleaseRepository.persist(joyn_1_0);
		applicationReleaseRepository.persist(joyn_2_0);
		applicationReleaseRepository.persist(joyn_3_0);

		// given myOrange application
		Application myOrange = new Application("myOrange", "myOrange");
		HashSet<SSOId> myOrangeMembers = new HashSet<>();
		myOrangeMembers.add(new SSOId("bob123"));
		myOrange.setAsPrivate();
		myOrange.setMembers(myOrangeMembers);
		applicationRepository.save(myOrange);
		// given releases of application myOrange
		ApplicationRelease myOrange_1_0 = new ApplicationRelease(myOrange, "1.0");
		ApplicationRelease myOrange_2_0 = new ApplicationRelease(myOrange, "2.0");
		applicationReleaseRepository.persist(myOrange_1_0);
		applicationReleaseRepository.persist(myOrange_2_0);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.persist(elpaaso_1_0);

		applicationReleaseRepository.flush();

		applicationReleaseRepository.countByApplicationMember(new SSOId("bob123"));
		Assert.assertEquals("there should be 5 releases", 5, applicationReleaseRepository.countByApplicationMember(new SSOId("bob123")));

	}

	@Test
	@Transactional
	public void testFindWithApplicationAndCount() throws MalformedURLException {
		ApplicationRelease toBePersisted1 = new ApplicationRelease(application, "G1R0C0");
		toBePersisted1.setVersionControlUrl(new URL("file://url.txt"));

		ApplicationRelease toBePersisted2 = new ApplicationRelease(application, "G1R0C0");
		toBePersisted1.setVersionControlUrl(new URL("file://url.txt"));

		Application application2 = new Application("application-test-2", "code2");
		applicationRepository.save(application2);

		ApplicationRelease toBePersisted3 = new ApplicationRelease(application2, "G1R0C0");
		toBePersisted1.setVersionControlUrl(new URL("file://url.txt"));

		applicationReleaseRepository.persist(toBePersisted1);
		applicationReleaseRepository.persist(toBePersisted2);
		applicationReleaseRepository.persist(toBePersisted3);

		List<ApplicationRelease> entities = (List<ApplicationRelease>) applicationReleaseRepository.findApplicationReleasesByAppUID(
				application.getUID(), 0, 3);
		Assert.assertNotNull("entities should not be null", entities);
		Assert.assertEquals("there should be 2 entities", 2, entities.size());
		Assert.assertFalse("entities should not contain the third release", entities.contains(toBePersisted3));
		applicationReleaseRepository.flush();
	}

	@Test
	@Transactional
	public void shouldCount2ApplicationReleasesForApplicationUID() throws MalformedURLException {
		// given an application with label myLabel and code myCode
		Application application = new Application("aLabel", "aCode");
		applicationRepository.save(application);
		// given a release with version aVersion of application with label
		// aLabel and code aCode
		ApplicationRelease release1 = new ApplicationRelease(application, "aVersion");
		applicationReleaseRepository.persist(release1);
		// given a release with version anotherVersion of application with label
		// aLabel and code aCode
		ApplicationRelease release2 = new ApplicationRelease(application, "anotherVersion");
		applicationReleaseRepository.persist(release2);
		// when I count application releases of application with label aLabel
		// and code aCode
		// then I should get 2
		Assert.assertEquals(2, applicationReleaseRepository.countApplicationReleasesByApplicationUID(application.getUID()));
		applicationReleaseRepository.flush();
	}

	@Test
	@Transactional
	public void testFindApplicationVersion() throws MalformedURLException {
		// test setup
		Application application2 = new Application("application-test-2", "code2");
		applicationRepository.save(application2);
		applicationRepository.flush();

		ApplicationRelease toBePersited1 = new ApplicationRelease(application, "G1R0C0");
		toBePersited1.setVersionControlUrl(new URL("file://url.txt"));
		ApplicationRelease toBePersited2 = new ApplicationRelease(application, "G2R0C0");
		toBePersited2.setVersionControlUrl(new URL("file://url.txt"));
		ApplicationRelease toBePersited3 = new ApplicationRelease(application2, "G1R0C0");
		toBePersited3.setVersionControlUrl(new URL("file://url.txt"));

		applicationReleaseRepository.persist(toBePersited1);
		applicationReleaseRepository.persist(toBePersited2);
		applicationReleaseRepository.persist(toBePersited3);

		// test run
		List<String> versions = applicationReleaseRepository.findApplicationVersion(application.getUID());
		// assertions
		Assert.assertNotNull("entities should not be null", versions);
		Assert.assertEquals("there should be 2 entities", 2, versions.size());
		applicationReleaseRepository.flush();
	}

	@Test
	@Transactional
	public void shouldFindApplicationReleaseByApplicationAndReleaseVersion() {
		// given an application with code aCode and label aLabel
		Application application = new Application("aLabel", "aCode");
		applicationRepository.save(application);
		// given a removed release of application with version aVersion
		ApplicationRelease release1 = new ApplicationRelease(application, "aVersion");
		release1.markAsRemoved();
		applicationReleaseRepository.persist(release1);
		// given a release of application with version aVersion
		ApplicationRelease release2 = new ApplicationRelease(application, "aVersion");
		applicationReleaseRepository.persist(release2);
		// when I find release of that application with version aVersion
		ApplicationRelease result = applicationReleaseRepository.findByApplicationUIDAndReleaseVersion(application.getUID(), "aVersion");
		// then I should get release with version a Version
		Assert.assertEquals(release2, result);
	}

	@Test
	@Transactional
	public void shouldFindNoApplicationReleaseByApplicationAndReleaseVersion() {
		// given an application with code aCode and label aLabel
		Application application = new Application("aLabel", "aCode");
		applicationRepository.save(application);
		// given a removed release of application with version aVersion
		ApplicationRelease release = new ApplicationRelease(application, "aVersion");
		release.markAsRemoved();
		applicationReleaseRepository.persist(release);
		// when I find release of that application with version aVersion
		ApplicationRelease result = applicationReleaseRepository.findByApplicationUIDAndReleaseVersion(application.getUID(), "aVersion");
		// then I should get no release
		Assert.assertNull(result);
	}

	@Test
	@Transactional
	public void testFindQRSServiceName() throws MalformedURLException {
		// test setup
		Application application2 = new Application("application-test-2", "code2");
		applicationRepository.save(application2);
		applicationRepository.flush();

		ApplicationRelease toBePersited1 = new ApplicationRelease(application, "G1R0C0");
		toBePersited1.setVersionControlUrl(new URL("file://url.txt"));
		ApplicationRelease toBePersited2 = new ApplicationRelease(application, "G2R0C0");
		toBePersited2.setVersionControlUrl(new URL("file://url.txt"));

		new LogicalQueueReceiveService("QRS-test-1", toBePersited2.getLogicalDeployment(), "getClient", "G1R0C0", 5, 1000, 1);
		new LogicalQueueReceiveService("QRS-test-2", toBePersited2.getLogicalDeployment(), "getClient", "G2R0C0", 5, 1000, 1);

		ApplicationRelease toBePersited3 = new ApplicationRelease(application2, "G1R0C0");
		toBePersited3.setVersionControlUrl(new URL("file://url.txt"));

		applicationReleaseRepository.persist(toBePersited1);
		applicationReleaseRepository.persist(toBePersited2);
		applicationReleaseRepository.persist(toBePersited3);

		// test run
		List<String> services = applicationReleaseRepository.findQRSServiceName(application.getUID(), "G2R0C0");

		// assertions
		Assert.assertNotNull("services should not be null", services);
		Assert.assertEquals("there should be 1 services", 1, services.size());
		Assert.assertEquals("service name", "getClient", services.get(0));
		applicationReleaseRepository.flush();
	}

	@Test
	@Transactional
	public void testFindQRSServiceVersion() throws MalformedURLException {
		// test setup
		Application application2 = new Application("application-test-2", "code2");
		applicationRepository.save(application2);
		applicationRepository.flush();

		ApplicationRelease toBePersited1 = new ApplicationRelease(application, "G1R0C0");
		toBePersited1.setVersionControlUrl(new URL("file://url.txt"));
		ApplicationRelease toBePersited2 = new ApplicationRelease(application, "G2R0C0");
		toBePersited2.setVersionControlUrl(new URL("file://url.txt"));

		new LogicalQueueReceiveService("QRS-test-1", toBePersited2.getLogicalDeployment(), "getClient", "G1R0C0", 5, 1000, 1);
		new LogicalQueueReceiveService("QRS-test-2", toBePersited2.getLogicalDeployment(), "getClient", "G2R0C0", 5, 1000, 1);

		ApplicationRelease toBePersited3 = new ApplicationRelease(application2, "G1R0C0");
		toBePersited3.setVersionControlUrl(new URL("file://url.txt"));

		applicationReleaseRepository.persist(toBePersited1);
		applicationReleaseRepository.persist(toBePersited2);
		applicationReleaseRepository.persist(toBePersited3);

		// test run
		List<String> serviceVersions = applicationReleaseRepository.findQRSServiceVersion(application.getUID(), "G2R0C0", "getClient");

		// assertions
		Assert.assertNotNull("services should not be null", serviceVersions);
		Assert.assertEquals("there should be 2 versions", 2, serviceVersions.size());
		Assert.assertEquals("service version", "G1R0C0", serviceVersions.get(0));
		Assert.assertEquals("service version", "G2R0C0", serviceVersions.get(1));
		applicationReleaseRepository.flush();
	}

	@Test
	@Transactional
	public void testFindApplicationHavingQrs() throws MalformedURLException {
		// test setup
		Application application2 = new Application("application-test-2", "code2");
		applicationRepository.save(application2);
		applicationRepository.flush();

		ApplicationRelease toBePersited1 = new ApplicationRelease(application, "G1R0C0");
		toBePersited1.setVersionControlUrl(new URL("file://url.txt"));
		ApplicationRelease toBePersited2 = new ApplicationRelease(application, "G2R0C0");
		toBePersited2.setVersionControlUrl(new URL("file://url.txt"));

		new LogicalQueueReceiveService("QRS-test-1", toBePersited2.getLogicalDeployment(), "getClient", "G1R0C0", 5, 1000, 1);
		new LogicalQueueReceiveService("QRS-test-2", toBePersited2.getLogicalDeployment(), "getClient", "G2R0C0", 5, 1000, 1);

		ApplicationRelease toBePersited3 = new ApplicationRelease(application2, "G1R0C0");

		toBePersited3.setVersionControlUrl(new URL("file://url.txt"));

		applicationReleaseRepository.persist(toBePersited1);
		applicationReleaseRepository.persist(toBePersited2);
		applicationReleaseRepository.persist(toBePersited3);

		// test run
		List<String> applicationNames = applicationReleaseRepository.findApplicationHavingQrs();

		// assertions
		Assert.assertNotNull("applications should not be null", applicationNames);
		Assert.assertEquals("there should be 1 application name", 1, applicationNames.size());
		Assert.assertEquals("application name", application.getUID(), applicationNames.get(0));

		applicationReleaseRepository.flush();
	}

}
