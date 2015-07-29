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

import com.francetelecom.clara.cloud.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/coremodel/application-context.xml" })
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class EnvironmentRepositoryTest {

	@Autowired
	private EnvironmentRepository environmentRepository;

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private ApplicationReleaseRepository applicationReleaseRepository;

	@Autowired
	private TechnicalDeploymentTemplateRepository technicalDeploymentTemplateRepository;

	@Autowired
	private PaasUserRepository paasUserRepository;

	private PaasUser manager;

	private ApplicationRelease release;

	private TechnicalDeploymentInstance technicalDeploymentInstance;

	/*
	 * @Autowired DataSource dataSource;
	 */
	@Before
	@Transactional
	public void setup() throws Exception {
		Assert.assertNotNull(environmentRepository);
		// given paas user with ssoId aSsoId exists
		manager = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
		paasUserRepository.save(manager);
		paasUserRepository.flush();
		// given application with label aLabel and code aCode exists
		Application application = new Application("aLabel", "aCode");
		applicationRepository.save(application);
		applicationRepository.flush();
		// given release with version aVersion exists
		release = new ApplicationRelease(application, "aVersion");
		applicationReleaseRepository.save(release);
		applicationReleaseRepository.flush();

		// given td exists
		TechnicalDeployment technicalDeployment = new TechnicalDeployment("foo");
		// given tdt exists
		TechnicalDeploymentTemplate technicalDeploymentTemplate = new TechnicalDeploymentTemplate(technicalDeployment, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		technicalDeploymentTemplateRepository.save(technicalDeploymentTemplate);
		// given tdi exists
		technicalDeploymentInstance = new TechnicalDeploymentInstance(technicalDeploymentTemplate, technicalDeployment);

	}

	@Test
	@Transactional
	public void testPersist() {
		Environment env = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager, technicalDeploymentInstance);
		environmentRepository.save(env);
		assertFindEnvById(env.getId(), true, env);
		environmentRepository.flush();
	}

	private void assertFindEnvById(int envId, boolean expectsEnvToBePresent, Environment expectedDetachedEnv) {
		Environment lookedUpEnv = environmentRepository.findOne(envId);
		assertFindEnv(expectsEnvToBePresent, expectedDetachedEnv, lookedUpEnv);
	}

	private void assertFindEnvByUID(String envUID, Environment expectedDetachedEnv) {
		Environment lookedUpEnv = environmentRepository.findByUid(envUID);
		assertFindEnv((expectedDetachedEnv != null), expectedDetachedEnv, lookedUpEnv);

	}

	@Test
	@Transactional
	public void shouldFailToFindUnknownEnvironmentByUID() {
		// when i want to find an environment using an unknown uid
		Environment environment = environmentRepository.findByUid("dummy");
		// then I should not get this environment
		Assert.assertNull("environment should not exist", environment);
	}

	@Test
	 @Transactional
	 public void shouldFindExistingEnvironmentByUID() {
		// given a persisted environment
		Environment persisted = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager,
				technicalDeploymentInstance);
		environmentRepository.save(persisted);
		// when I want to find this environment by its uid
		Environment entity = environmentRepository.findByUid(persisted.getUID());
		// then I should get this environment
		Assert.assertNotNull("cannot find environment by its uid", entity);
	}

	@Test
	@Transactional
	public void shouldFindByTechnicalInstanceDeploymentId() {
		// given a persisted environment
		Environment persisted = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager,
				technicalDeploymentInstance);
		environmentRepository.save(persisted);
		// when I want to find this environment by its uid
		Environment entity = environmentRepository.findByTechnicalDeploymentInstanceId(technicalDeploymentInstance.getId());
		// then I should get this environment
		Assert.assertNotNull("cannot find environment by its uid", entity);
	}

	private void assertFindEnv(boolean expectsEnvToBePresent, Environment expectedDetachedEnv, Environment lookedUpEnv) {
		if (expectsEnvToBePresent) {
			assert lookedUpEnv != null; // assumption
			Assert.assertNotNull("entity does not exist", lookedUpEnv);
			Assert.assertEquals(expectedDetachedEnv.getUID(), lookedUpEnv.getUID());
		} else {
			Assert.assertNull("entity should not exist", lookedUpEnv);
		}
	}

	@Test
	@Transactional
	public void should_remove_environment() {
		Environment env = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager, technicalDeploymentInstance);
		environmentRepository.save(env);
		assertFindEnvById(env.getId(), true, env);
		environmentRepository.delete(env);
		environmentRepository.flush();
		assertFindEnvById(env.getId(), false, null);
	}

	@Test
	@Transactional
	public void should_find_environment_by_id() {
		Environment env = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager, technicalDeploymentInstance);
		environmentRepository.save(env);
		assertFindEnvById(env.getId(), true, env);
	}

	@Test
	@Transactional
	public void should_find_environment_with_configuration_by_id() {
		Environment env = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager, technicalDeploymentInstance);
		
		ConfigRole cfg = new ConfigRole("app");
		cfg.setValues(Arrays.asList(new ConfigValue("configset", "key1", "value1", "my key 1"), new ConfigValue("configset", "key2", "value2", "my key 2")));
		
		environmentRepository.save(env);
		assertFindEnvById(env.getId(), true, env);
	}

	@Test
	@Transactional
	public void should_find_environment_by_uid() {
		Environment env = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager, technicalDeploymentInstance);
		environmentRepository.save(env);
		assertFindEnvByUID(env.getUID(), env);
	}

	@Test
	@Transactional
	public void should_find_all_environments() {
		Environment env1 = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager, technicalDeploymentInstance);
		environmentRepository.save(env1);
		Environment env2 = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager, technicalDeploymentInstance);
		environmentRepository.save(env2);

		environmentRepository.flush();

		// test run
		List<Environment> entities = environmentRepository.findAll();
		// assertions
		Assert.assertNotNull("entities should not be null", entities);
		Assert.assertEquals("there should be 2 entities", 2, entities.size());
	}

	@Test
	@Transactional
	public void testFindAllActiveWithCount() {
		Environment env1 = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager, technicalDeploymentInstance);
		environmentRepository.save(env1);
		Environment env2 = new Environment(DeploymentProfileEnum.PRODUCTION, "my first environment", release, manager, technicalDeploymentInstance);
		env2.setStatus(EnvironmentStatus.REMOVED);
		environmentRepository.save(env2);
		environmentRepository.flush();

		List<Environment> entities = environmentRepository.findAllActive();
		Assert.assertEquals("there should be 1 entity", 1, entities.size());
		Assert.assertFalse("entities should not contain removed environment", entities.contains(env2));
	}

	@Test
	@Transactional
	public void should_find_active_environment_by_releaseUID_and_environment_lLabel() {
		// given default environment
		Environment environment = new Environment(DeploymentProfileEnum.PRODUCTION, "aLabel", release, manager, technicalDeploymentInstance);
		environmentRepository.save(environment);
		// given removed environment
		Environment removed = new Environment(DeploymentProfileEnum.PRODUCTION, "aLabel", release, manager, technicalDeploymentInstance);
		removed.setStatus(EnvironmentStatus.REMOVED);
		environmentRepository.save(removed);
		// when I find environment by application release uid and environment
		// label aLabel
		Environment result = environmentRepository.findByApplicationReleaseUIDAndLabel(environment.getApplicationRelease().getUID(), "aLabel");
		// then I should get default environment
		Assert.assertEquals(environment, result);
	}

	@Test
	@Transactional
	public void testCount() {
		Environment environment1 = new Environment(DeploymentProfileEnum.PRODUCTION, "aLabel", release, manager, technicalDeploymentInstance);
		environmentRepository.save(environment1);
		Environment environment2 = new Environment(DeploymentProfileEnum.PRODUCTION, "aLabel", release, manager, technicalDeploymentInstance);
		environmentRepository.save(environment2);
		environmentRepository.flush();
		long count = environmentRepository.count();
		Assert.assertEquals("there should be 2 entities count", 2, count);
		List<Environment> entities = environmentRepository.findAll();
		Assert.assertEquals("there should be 2 entities", 2, entities.size());
	}

	@Test
	@Transactional
	public void shouldCount1EnvironmentForApplicationReleaseUID() throws MalformedURLException {
		// given a REMOVED environment with profile DEVELOPMENT with label
		// ALabel for
		// release for user aSsoId

		Environment environment1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "aLabel", release, manager, technicalDeploymentInstance);
		environment1.setStatus(EnvironmentStatus.REMOVED);
		environmentRepository.save(environment1);
		// given a environment with profile DEVELOPMENT with label anotherLabel
		// for release for user aSsoId
		Environment environment2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", release, manager, technicalDeploymentInstance);
		environmentRepository.save(environment2);
		// when I count environment of release with label aLabel
		long count = environmentRepository.countActiveByApplicationReleaseUid(release.getUID());
		// then I should get 1
		Assert.assertEquals(1, count);
	}

	@Test
	@Transactional
	public void should_find_active_environments_for_given_member() throws MalformedURLException {
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
		applicationReleaseRepository.save(joyn_1_0);
		applicationReleaseRepository.save(joyn_2_0);
		applicationReleaseRepository.save(joyn_3_0);

		Environment joyn_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_1);
		Environment joyn_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_2);

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
		applicationReleaseRepository.save(myOrange_1_0);
		applicationReleaseRepository.save(myOrange_2_0);

		Environment myOrange_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", myOrange_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(myOrange_1_0_env_1);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.save(elpaaso_1_0);

		Environment elpaaso_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_1);
		Environment elpaaso_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_2);

		applicationRepository.flush();
		applicationReleaseRepository.flush();
		environmentRepository.flush();

		// assertions
		// when I find all active environments
		List<Environment> jdaltonEnvs = environmentRepository.findAllActiveByApplicationMember("jdalton");
		List<Environment> aliceEnvs = environmentRepository.findAllActiveByApplicationMember("alice123");
		List<Environment> bobEnvs = environmentRepository.findAllActiveByApplicationMember("bob123");

		Assert.assertEquals("jdalton should see no environment", 0, jdaltonEnvs.size());
		Assert.assertEquals("alice should see 2 environments", 2, aliceEnvs.size());
		Assert.assertTrue("alice should see joyn_1_0_env_1", aliceEnvs.contains(joyn_1_0_env_1));
		Assert.assertTrue("alice should see joyn_1_0_env_2",aliceEnvs.contains(joyn_1_0_env_2));
		Assert.assertEquals("bob should see 3  environments", 3, bobEnvs.size());
		Assert.assertTrue("bob should see joyn_1_0_env_1", bobEnvs.contains(joyn_1_0_env_1));
		Assert.assertTrue("bob should see joyn_1_0_env_2",bobEnvs.contains(joyn_1_0_env_2));
		Assert.assertTrue("bob should see myOrange_1_0_env_1", bobEnvs.contains(myOrange_1_0_env_1));

	}
	
	
	@Test
	@Transactional
	public void should_find_public_environments_or_private_environment_for_given_member() throws MalformedURLException {
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
		applicationReleaseRepository.save(joyn_1_0);
		applicationReleaseRepository.save(joyn_2_0);
		applicationReleaseRepository.save(joyn_3_0);

		Environment joyn_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_1);
		Environment joyn_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_2);

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
		applicationReleaseRepository.save(myOrange_1_0);
		applicationReleaseRepository.save(myOrange_2_0);

		Environment myOrange_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", myOrange_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(myOrange_1_0_env_1);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		HashSet<SSOId> elpaasoMembers = new HashSet<>();
		elpaasoMembers.add(new SSOId("jdalton"));
		elpaaso.setMembers(elpaasoMembers);
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.save(elpaaso_1_0);

		Environment elpaaso_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_1);
		Environment elpaaso_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_2);

		applicationRepository.flush();
		applicationReleaseRepository.flush();
		environmentRepository.flush();

		// assertions
		// when I find all active environments
		List<Environment> jdaltonEnvs = environmentRepository.findAllPublicOrPrivateByMember("jdalton");
		List<Environment> aliceEnvs = environmentRepository.findAllPublicOrPrivateByMember("alice123");
		List<Environment> bobEnvs = environmentRepository.findAllPublicOrPrivateByMember("bob123");

		Assert.assertEquals("jdalton should see 2 environments", 2, jdaltonEnvs.size());
		Assert.assertTrue("jdalton should see elpaaso_1_0_env_1",aliceEnvs.contains(elpaaso_1_0_env_1));
		Assert.assertTrue("jdalton should see elpaaso_1_0_env_2",aliceEnvs.contains(elpaaso_1_0_env_2));
		
		Assert.assertEquals("alice should see 3 environments", 4, aliceEnvs.size());
		Assert.assertTrue("alice should see joyn_1_0_env_1", aliceEnvs.contains(joyn_1_0_env_1));
		Assert.assertTrue("alice should see joyn_1_0_env_2",aliceEnvs.contains(joyn_1_0_env_2));
		Assert.assertTrue("alice should see elpaaso_1_0_env_1",aliceEnvs.contains(elpaaso_1_0_env_1));
		Assert.assertTrue("alice should see elpaaso_1_0_env_2",aliceEnvs.contains(elpaaso_1_0_env_2));

		Assert.assertEquals("bob should see 5  environments", 5, bobEnvs.size());
		Assert.assertTrue("bob should see joyn_1_0_env_1", bobEnvs.contains(joyn_1_0_env_1));
		Assert.assertTrue("bob should see joyn_1_0_env_2",bobEnvs.contains(joyn_1_0_env_2));
		Assert.assertTrue("bob should see myOrange_1_0_env_1", bobEnvs.contains(myOrange_1_0_env_1));
		Assert.assertTrue("bob should see elpaaso_1_0_env_1",aliceEnvs.contains(elpaaso_1_0_env_1));
		Assert.assertTrue("bob should see elpaaso_1_0_env_2",aliceEnvs.contains(elpaaso_1_0_env_2));

	}
	
	@Test
	@Transactional
	public void should_count_active_environments_for_given_member() throws MalformedURLException {
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
		applicationReleaseRepository.save(joyn_1_0);
		applicationReleaseRepository.save(joyn_2_0);
		applicationReleaseRepository.save(joyn_3_0);

		Environment joyn_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_1);
		Environment joyn_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_2);

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
		applicationReleaseRepository.save(myOrange_1_0);
		applicationReleaseRepository.save(myOrange_2_0);

		Environment myOrange_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", myOrange_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(myOrange_1_0_env_1);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.save(elpaaso_1_0);

		Environment elpaaso_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_1);
		Environment elpaaso_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_2);

		applicationRepository.flush();
		applicationReleaseRepository.flush();
		environmentRepository.flush();

		// assertions
		// when I find all active environments
		Long jdaltonEnvs = environmentRepository.countActiveByApplicationMember("jdalton");
		Long aliceEnvs = environmentRepository.countActiveByApplicationMember("alice123");
		Long bobEnvs = environmentRepository.countActiveByApplicationMember("bob123");

		Assert.assertEquals("jdalton should see no environment", new Long(0), jdaltonEnvs);
		Assert.assertEquals("alice should see 2 environments",  new Long(2), aliceEnvs);
		Assert.assertEquals("bob should see 3  environments",  new Long(3), bobEnvs);
	}
	
	@Test
	@Transactional
	public void should_count_public_environments_or_environment_for_given_member() throws MalformedURLException {
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
		applicationReleaseRepository.save(joyn_1_0);
		applicationReleaseRepository.save(joyn_2_0);
		applicationReleaseRepository.save(joyn_3_0);

		Environment joyn_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_1);
		Environment joyn_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_2);

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
		applicationReleaseRepository.save(myOrange_1_0);
		applicationReleaseRepository.save(myOrange_2_0);

		Environment myOrange_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", myOrange_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(myOrange_1_0_env_1);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.save(elpaaso_1_0);

		Environment elpaaso_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_1);
		Environment elpaaso_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_2);

		applicationRepository.flush();
		applicationReleaseRepository.flush();
		environmentRepository.flush();

		// assertions
		// when I find all active environments
		Long jdaltonEnvs = environmentRepository.countActiveByApplicationMember("jdalton");
		Long aliceEnvs = environmentRepository.countActiveByApplicationMember("alice123");
		Long bobEnvs = environmentRepository.countActiveByApplicationMember("bob123");

		Assert.assertEquals("jdalton should see no environment", new Long(0), jdaltonEnvs);
		Assert.assertEquals("alice should see 2 environments",  new Long(2), aliceEnvs);
		Assert.assertEquals("bob should see 3  environments",  new Long(3), bobEnvs);
	}

	@Test
	@Transactional
	public void should_find_active_public_environments_or_environment_by_member_for_given_release() throws MalformedURLException {
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
		applicationReleaseRepository.save(joyn_1_0);
		applicationReleaseRepository.save(joyn_2_0);
		applicationReleaseRepository.save(joyn_3_0);

		Environment joyn_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_1);
		Environment joyn_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_2);

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
		applicationReleaseRepository.save(myOrange_1_0);
		applicationReleaseRepository.save(myOrange_2_0);

		Environment myOrange_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", myOrange_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(myOrange_1_0_env_1);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		HashSet<SSOId> elpaasoMembers = new HashSet<>();
		elpaasoMembers.add(new SSOId("jdalton"));
		elpaaso.setMembers(elpaasoMembers);
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.save(elpaaso_1_0);

		Environment elpaaso_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_1);
		Environment elpaaso_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_2);

		applicationRepository.flush();
		applicationReleaseRepository.flush();
		environmentRepository.flush();

		// assertions
		// when I find all active environments
		List<Environment> jdalton_joyn_1_0_envs = environmentRepository.findAllPublicOrPrivateByMemberAndByApplicationRelease(joyn_1_0.getUID(),"jdalton");
		List<Environment> alice_joyn_1_0_envs = environmentRepository.findAllPublicOrPrivateByMemberAndByApplicationRelease(joyn_1_0.getUID(),"alice123");
		List<Environment> bob_joyn_1_0_envs = environmentRepository.findAllPublicOrPrivateByMemberAndByApplicationRelease(joyn_1_0.getUID(),"bob123");

		Assert.assertEquals("jdalton should see no environment of joyn_1_0", 0, jdalton_joyn_1_0_envs.size());
		Assert.assertEquals("alice should see 2 environments  of joyn_1_0", 2, alice_joyn_1_0_envs.size());
		Assert.assertTrue("alice should see joyn_1_0_env_1", alice_joyn_1_0_envs.contains(joyn_1_0_env_1));
		Assert.assertTrue("alice should see joyn_1_0_env_2",alice_joyn_1_0_envs.contains(joyn_1_0_env_2));
		Assert.assertEquals("bob should see 2  environments  of joyn_1_0", 2, bob_joyn_1_0_envs.size());
		Assert.assertTrue("bob should see joyn_1_0_env_1", bob_joyn_1_0_envs.contains(joyn_1_0_env_1));
		Assert.assertTrue("bob should see joyn_1_0_env_2",bob_joyn_1_0_envs.contains(joyn_1_0_env_2));
		
		List<Environment> jdalton_elpaaso_1_0_envs = environmentRepository.findAllPublicOrPrivateByMemberAndByApplicationRelease(elpaaso_1_0.getUID(),"jdalton");
		List<Environment> alice_elpaaso_1_0_envs = environmentRepository.findAllPublicOrPrivateByMemberAndByApplicationRelease(elpaaso_1_0.getUID(),"alice123");
		List<Environment> bob_elpaaso_1_0_envs = environmentRepository.findAllPublicOrPrivateByMemberAndByApplicationRelease(elpaaso_1_0.getUID(),"bob123");

		
		Assert.assertEquals("jdalton should see 2 environments of elpaaso_1_0", 2, jdalton_elpaaso_1_0_envs.size());
		Assert.assertTrue("jdalton should see elpaaso_1_0_env_1", jdalton_elpaaso_1_0_envs.contains(elpaaso_1_0_env_1));
		Assert.assertTrue("jdalton should see elpaaso_1_0_env_2",jdalton_elpaaso_1_0_envs.contains(elpaaso_1_0_env_2));
		Assert.assertEquals("alice should see 2 environments  of elpaaso_1_0", 2, alice_elpaaso_1_0_envs.size());
		Assert.assertTrue("alice should see elpaaso_1_0_env_1", alice_elpaaso_1_0_envs.contains(elpaaso_1_0_env_1));
		Assert.assertTrue("alice should see elpaaso_1_0_env_2",alice_elpaaso_1_0_envs.contains(elpaaso_1_0_env_2));
		Assert.assertEquals("bob should see 2  environments  of elpaaso_1_0", 2, bob_elpaaso_1_0_envs.size());
		Assert.assertTrue("bob should see elpaaso_1_0_env_1", bob_elpaaso_1_0_envs.contains(elpaaso_1_0_env_1));
		Assert.assertTrue("bob should see elpaaso_1_0_env_2",bob_elpaaso_1_0_envs.contains(elpaaso_1_0_env_2));

	}
	
	@Test
	@Transactional
	public void should_count_active_public_environments_or_environment_by_member_for_given_release() throws MalformedURLException {
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
		applicationReleaseRepository.save(joyn_1_0);
		applicationReleaseRepository.save(joyn_2_0);
		applicationReleaseRepository.save(joyn_3_0);

		Environment joyn_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_1);
		Environment joyn_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_2);

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
		applicationReleaseRepository.save(myOrange_1_0);
		applicationReleaseRepository.save(myOrange_2_0);

		Environment myOrange_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", myOrange_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(myOrange_1_0_env_1);

		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		HashSet<SSOId> elpaasoMembers = new HashSet<>();
		elpaasoMembers.add(new SSOId("jdalton"));
		elpaaso.setMembers(elpaasoMembers);
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.save(elpaaso_1_0);

		Environment elpaaso_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_1);
		Environment elpaaso_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_2);

		applicationRepository.flush();
		applicationReleaseRepository.flush();
		environmentRepository.flush();

		// assertions
		// when I count all active environments
		long jdalton_joyn_1_0_envs = environmentRepository.countAllPublicOrPrivateByMemberAndByApplicationRelease(joyn_1_0.getUID(),"jdalton");
		long alice_joyn_1_0_envs = environmentRepository.countAllPublicOrPrivateByMemberAndByApplicationRelease(joyn_1_0.getUID(),"alice123");
		long bob_joyn_1_0_envs = environmentRepository.countAllPublicOrPrivateByMemberAndByApplicationRelease(joyn_1_0.getUID(),"bob123");

		Assert.assertEquals("jdalton should see no environment of joyn_1_0", 0, jdalton_joyn_1_0_envs);
		Assert.assertEquals("alice should see 2 environments  of joyn_1_0", 2, alice_joyn_1_0_envs);
		Assert.assertEquals("bob should see 2  environments  of joyn_1_0", 2, bob_joyn_1_0_envs);
		
		long jdalton_elpaaso_1_0_envs = environmentRepository.countAllPublicOrPrivateByMemberAndByApplicationRelease(elpaaso_1_0.getUID(),"jdalton");
		long alice_elpaaso_1_0_envs = environmentRepository.countAllPublicOrPrivateByMemberAndByApplicationRelease(elpaaso_1_0.getUID(),"alice123");
		long bob_elpaaso_1_0_envs = environmentRepository.countAllPublicOrPrivateByMemberAndByApplicationRelease(elpaaso_1_0.getUID(),"bob123");

		
		Assert.assertEquals("jdalton should see 2 environments of elpaaso_1_0", 2, jdalton_elpaaso_1_0_envs);
		Assert.assertEquals("alice should see 2 environments  of elpaaso_1_0", 2, alice_elpaaso_1_0_envs);
		Assert.assertEquals("bob should see 2  environments  of elpaaso_1_0", 2, bob_elpaaso_1_0_envs);
	}

	@Test
	@Transactional
	public void should_count_public_environments_or_private_environment_for_given_member() throws MalformedURLException {
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
		applicationReleaseRepository.save(joyn_1_0);
		applicationReleaseRepository.save(joyn_2_0);
		applicationReleaseRepository.save(joyn_3_0);
	
		Environment joyn_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_1);
		Environment joyn_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", joyn_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(joyn_1_0_env_2);
	
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
		applicationReleaseRepository.save(myOrange_1_0);
		applicationReleaseRepository.save(myOrange_2_0);
	
		Environment myOrange_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", myOrange_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(myOrange_1_0_env_1);
	
		// given elpaaso public application
		Application elpaaso = new Application("elpaaso", "elpaaso");
		HashSet<SSOId> elpaasoMembers = new HashSet<>();
		elpaasoMembers.add(new SSOId("jdalton"));
		elpaaso.setMembers(elpaasoMembers);
		applicationRepository.save(elpaaso);
		// given releases of application elpaaso
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		applicationReleaseRepository.save(elpaaso_1_0);
	
		Environment elpaaso_1_0_env_1 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_1);
		Environment elpaaso_1_0_env_2 = new Environment(DeploymentProfileEnum.DEVELOPMENT, "anotherLabel", elpaaso_1_0, manager,
				technicalDeploymentInstance);
		environmentRepository.save(elpaaso_1_0_env_2);
	
		applicationRepository.flush();
		applicationReleaseRepository.flush();
		environmentRepository.flush();
	
		// assertions
		// when I count all active environments
		long jdaltonEnvs = environmentRepository.countPublicOrPrivateByMember("jdalton");
		long aliceEnvs = environmentRepository.countPublicOrPrivateByMember("alice123");
		long bobEnvs = environmentRepository.countPublicOrPrivateByMember("bob123");
	
		Assert.assertEquals("jdalton should see 2 environments", 2, jdaltonEnvs);
		Assert.assertEquals("alice should see 3 environments", 4, aliceEnvs);
		Assert.assertEquals("bob should see 5  environments", 5, bobEnvs);
	
	}
}
