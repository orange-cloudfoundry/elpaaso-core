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
package com.francetelecom.clara.cloud.integration;

import java.net.MalformedURLException;
import java.net.URL;

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

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.InvalidApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;

/**
 * Test Business implementation for Application component
 * 
 * @author Clara
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/integration/LifecycleTests-context.xml")
@DirtiesContext
public class ApplicationReleaseLifecycleTest {

	private static final PaasUser JOHN_LENNON = new PaasUser("john", "Lennon", new SSOId("john123"), "john@orange.com");
	private static final PaasUser BOB_DYLAN = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
	
	public static Logger logger = LoggerFactory.getLogger(ApplicationReleaseLifecycleTest.class.getName());

	@Autowired(required = true)
	ManageApplication manageApplication;

	@Autowired(required = true)
	ManagePaasUser managePaasUser;

	@Autowired(required = true)
	ManageApplicationRelease manageApplicationRelease;

	@Autowired(required = true)
	SessionFactory sessionFactory;

	PaasUser paasUser_persisted1;
	PaasUser paasUser_persisted2;
	Application application_persisted1;

	@Before
	public void setup() throws MalformedURLException, InvalidApplicationException, ObjectNotFoundException, DuplicateApplicationException {

		// given admin is authenticated
		TestHelper.loginAsAdmin();

		// test setup
		logger.debug("/*************** createPaasUser *************************/");
		// persist paas user
		managePaasUser.checkBeforeCreatePaasUser(BOB_DYLAN);
		// fetch paas user from DB
		paasUser_persisted1 = managePaasUser.findPaasUser(BOB_DYLAN.getSsoId().getValue());

		logger.debug("/*************** createPaasUser *************************/");
		// persist paas user
		managePaasUser.checkBeforeCreatePaasUser(JOHN_LENNON);
		// fetch paas user from DB
		paasUser_persisted2 = managePaasUser.findPaasUser(JOHN_LENNON.getSsoId().getValue());

		// persist application
		String applicationUID = manageApplication.createPublicApplication("aCode", "aLabel", null, null, BOB_DYLAN.getSsoId());
		// fetch application from DB
		application_persisted1 = manageApplication.findApplicationByUID(applicationUID);

	}

	@After
	public void cleanSecurityContext() {
		TestHelper.logout();
	}

	@Test
	public void testApplicationReleaseLifecycle() throws MalformedURLException, BusinessException {

		logger.debug("/*************** createApplicationRelease *************************/");
		// hibernate stats init
		sessionFactory.getStatistics().clear();
		// persist application release
		String relUid = manageApplicationRelease.createApplicationRelease(application_persisted1.getUID(), paasUser_persisted1.getSsoId().getValue(), "1");
		// display hibernate stats
		sessionFactory.getStatistics().logSummary();

		logger.debug("/*************** findApplicationRelease *************************/");
		// hibernate stats init
		sessionFactory.getStatistics().clear();
		// fetch applicationRelease from DB
		ApplicationRelease applicationRelease_persisted = manageApplicationRelease.findApplicationReleaseByUID(relUid);
		// display hibernate stats
		sessionFactory.getStatistics().logSummary();

		// hibernate stats init
		sessionFactory.getStatistics().clear();
		// update application release
		applicationRelease_persisted.setDescription("description1_updated");
		applicationRelease_persisted.setVersionControlUrl(new URL("file:url1_updated"));
		manageApplicationRelease.updateApplicationRelease(applicationRelease_persisted);
		// display hibernate stats
		sessionFactory.getStatistics().logSummary();

		ApplicationRelease applicationRelease_updated = manageApplicationRelease.findApplicationReleaseByUID(relUid);
		Assert.assertNotNull(applicationRelease_updated);
		Assert.assertEquals("description1_updated", applicationRelease_updated.getDescription());
		Assert.assertEquals("file:url1_updated", applicationRelease_updated.getVersionControlUrl().toString());

		logger.debug("/*************** grantRole *************************/");
		// hibernate stats init
		sessionFactory.getStatistics().clear();
		// affichage logs ds output
		sessionFactory.getStatistics().logSummary();

		logger.debug("/*************** deleteApplicationRelease *************************/");
		// hibernate stats init
		sessionFactory.getStatistics().clear();
		manageApplicationRelease.deleteApplicationRelease(applicationRelease_persisted.getUID());
		// affichage logs ds output
		sessionFactory.getStatistics().logSummary();

		// FIXME: assert there is no leak on the delete. In particular
	}
}
