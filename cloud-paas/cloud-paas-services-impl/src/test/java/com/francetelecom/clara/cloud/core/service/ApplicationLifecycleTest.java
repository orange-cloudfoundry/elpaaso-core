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
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.services.dto.ConfigOverrideDTO;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;

/**
 * Test Business implementation for Application component
 * 
 * @author Clara
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/integration/LifecycleTests-context.xml")
@DirtiesContext
public class ApplicationLifecycleTest {

	public static Logger logger = LoggerFactory.getLogger(ApplicationLifecycleTest.class.getName());

	@Autowired(required = true)
	ManagePaasUser managePaasUser;

	@Autowired(required = true)
	ManageApplication manageApplication;

	@Autowired(required = true)
	SessionFactory sessionFactory;

	PaasUser dylan;
	PaasUser lennon;
	PaasUser alice;

	@Before
	public void setup() {
		// given admin is authenticated
		TestHelper.loginAsAdmin();
	}

	@After
	public void cleanSecurityContext() {
		TestHelper.logout();
	}

	@Test
	public void testApplicationLifecycle() throws MalformedURLException, BusinessException {
		// test setup
		logger.debug("/*************** createPaasUser *************************/");
		// persist paas user
		managePaasUser.checkBeforeCreatePaasUser(new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com"));
		// fetch paas user from DB
		dylan = managePaasUser.findPaasUser("bob123");

		logger.debug("/*************** createPaasUser *************************/");
		// persist paas user
		managePaasUser.checkBeforeCreatePaasUser(new PaasUser("john", "Lennon", new SSOId("john123"), "john@orange.com"));
		// fetch paas user from DB
		lennon = managePaasUser.findPaasUser("john123");

		logger.debug("/*************** createPaasUser *************************/");
		// persist paas user
		managePaasUser.checkBeforeCreatePaasUser(new PaasUser("alice", "InWonderland", new SSOId("alice123"), "alice@orange.com"));
		// fetch paas user from DB
		alice = managePaasUser.findPaasUser("alice123");

		// test setup
		// Application application_transient = new Application("aLabel",
		// "aCode");
		// application_transient.setDescription("aDescription");
		logger.debug("/*************** createApplication *************************/");
		// Initialization of Hibernate statistics
		sessionFactory.getStatistics().clear();
		// persist application
		String applicationUID = manageApplication.createPublicApplication("aCode", "aLabel", null, null, dylan.getSsoId(), lennon.getSsoId(), alice.getSsoId());
		// display of Hibernate statistics
		sessionFactory.getStatistics().logSummary();

		logger.debug("/*************** findApplication *************************/");
		// Initialization of Hibernate statistics
		sessionFactory.getStatistics().clear();
		// fetch application from DB
		Application application_persisted = manageApplication.findApplicationByUID(applicationUID);
		// display of Hibernate statistics
		sessionFactory.getStatistics().logSummary();

		logger.debug("/*************** updateApplication *************************/");
		// Initialization of Hibernate statistics
		sessionFactory.getStatistics().clear();
		// update application
		application_persisted.setDescription("aDescriptionUpdated");
		application_persisted.setCode("aCodeUpdated");
		application_persisted.setApplicationRegistryUrl(new URL("file:/url1_updated"));
		Set<SSOId> members = new HashSet<>();
		members.add(dylan.getSsoId());
		members.add(lennon.getSsoId());
		application_persisted.setAsPrivate();
		application_persisted.setMembers(members);

		application_persisted = manageApplication.updateApplication(application_persisted);

		// display of Hibernate statistics
		sessionFactory.getStatistics().logSummary();

		// update assertion
		Application application_updated = manageApplication.findApplicationByUID(applicationUID);
		Assert.assertEquals("aDescriptionUpdated", application_updated.getDescription());
		Assert.assertEquals("aCodeUpdated", application_updated.getCode());
		Assert.assertEquals("file:/url1_updated", application_updated.getApplicationRegistryUrl().toString());
		Assert.assertEquals(2, application_updated.listMembers().size());
		Assert.assertTrue(application_updated.listMembers().contains(dylan.getSsoId()));
		Assert.assertTrue(application_updated.listMembers().contains(lennon.getSsoId()));

		logger.debug("/*************** updateApplication another time *************************/");
		// Initialization of Hibernate statistics
		sessionFactory.getStatistics().clear();
		// update application
		application_persisted.setDescription("aDescriptionUpdatedTwice");
		application_persisted.setCode("aCodeUpdatedTwice");
		application_persisted.setApplicationRegistryUrl(new URL("file:/url1_updated"));
		Set<SSOId> newMembers = new HashSet<>();
		newMembers.add(alice.getSsoId());
		application_persisted.setAsPrivate();
		application_persisted.setMembers(newMembers);

		application_persisted = manageApplication.updateApplication(application_persisted);
		// display of Hibernate statistics
		sessionFactory.getStatistics().logSummary();

		// update assertion
		application_updated = manageApplication.findApplicationByUID(applicationUID);
		Assert.assertEquals("aDescriptionUpdatedTwice", application_updated.getDescription());
		Assert.assertEquals("aCodeUpdatedTwice", application_updated.getCode());
		Assert.assertEquals("file:/url1_updated", application_updated.getApplicationRegistryUrl().toString());
		Assert.assertEquals(1, application_updated.listMembers().size());
		Assert.assertTrue(application_updated.listMembers().contains(alice.getSsoId()));

        logger.debug("/*************** create config role*************************/");

        List<ConfigOverrideDTO> overrideConfigs = new ArrayList<>();
        ConfigOverrideDTO configOverrideDTO = new ConfigOverrideDTO("configSet", "key", "value", "comment");
        overrideConfigs.add(configOverrideDTO);

        //when
        String configRoleUID = manageApplication.createConfigRole(applicationUID, "role label", overrideConfigs);
        ConfigRole configRolePersistet = manageApplication.findConfigRole(configRoleUID);
        assertEquals("role label", configRolePersistet.getLastModificationComment());
        assertEquals(applicationUID, configRolePersistet.getApplicationUID());
        List<ConfigValue> configValues = configRolePersistet.listValues();
        assertEquals(1, configValues.size());
        ConfigValue configValue = configValues.get(0);
        assertEquals("key", configValue.getKey());
        assertEquals("value", configValue.getValue());
        assertEquals("comment", configValue.getComment());
        assertEquals("configSet", configValue.getConfigSet());


        logger.debug("/*************** deleteApplication *************************/");
		// Initialization of Hibernate statistics
		sessionFactory.getStatistics().clear();
		// delete application
		manageApplication.deleteApplication(applicationUID);
		// display of Hibernate statistics
		sessionFactory.getStatistics().logSummary();
	}

}
