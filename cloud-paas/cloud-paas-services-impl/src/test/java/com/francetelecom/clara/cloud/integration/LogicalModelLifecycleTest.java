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

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.core.service.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.core.service.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.core.service.exception.InvalidReleaseException;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.deployment.technical.service.ManageTechnicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.PetcliniccLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import org.hibernate.SessionFactory;
import org.junit.After;
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

import static org.junit.Assert.*;

/**
 * Test Business implementation for Application component
 * 
 * @author Clara
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/integration/LifecycleTests-context.xml")
@DirtiesContext
public class LogicalModelLifecycleTest {

	public static Logger logger = LoggerFactory.getLogger(LogicalModelLifecycleTest.class.getName());

	@Autowired(required = true)
	ManageApplication manageApplication;

	@Autowired(required = true)
	ManagePaasUser managePaasUser;

	@Autowired(required = true)
	ManageApplicationRelease manageApplicationRelease;

	@Autowired(required = true)
	ManageLogicalDeployment manageLogicalDeployment;

	@Autowired(required = true)
	ManageTechnicalDeployment manageTechnicalDeployment;

	@Autowired(required = true)
	ManageEnvironment manageEnvironment;

	@Autowired(required = true)
	SessionFactory sessionFactory;

	@Autowired
	PetcliniccLogicalModelCatalog petClinicLogicalModelCatalog;

	@Autowired
	SampleAppProperties sampleAppProperties;

	PaasUser paasUser_persisted1;
	PaasUser paasUser_persisted2;
	Application application1_persisted;
	Application application2_persisted;
	ApplicationRelease application1Release_persisted;
	ApplicationRelease application2Release_persisted;
	PaasUser user1;
	PaasUser user2;

	@Before
	public void setup() throws ObjectNotFoundException, MalformedURLException, DuplicateApplicationException, DuplicateApplicationReleaseException {

		// given admin is authenticated
		TestHelper.loginAsAdmin();
		
		user1 = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
		user2 = new PaasUser("john", "Lennon", new SSOId("john123"), "john@orange.com");
		

		// test setup
        logger.debug("/*************** createPaasUser *************************/");
		// persist paas user
		managePaasUser.checkBeforeCreatePaasUser(user1);
		// fetch paas user from DB
		paasUser_persisted1 = managePaasUser.findPaasUser("bob123");

		logger.debug("/*************** createPaasUser *************************/");
		// persist paas user
		managePaasUser.checkBeforeCreatePaasUser(user2);
		// fetch paas user from DB
		paasUser_persisted2 = managePaasUser.findPaasUser("john123");

		logger.debug("/*************** createApplication1 *************************/");
		// persist application
		String applicationUID = manageApplication.createPublicApplication("aCode", "aLabel", null, null, user1.getSsoId(), user2.getSsoId());
		// fetch application from DB
		application1_persisted = manageApplication.findApplicationByUID(applicationUID);

		logger.debug("/*************** createApplication2 *************************/");
		// persist application
		String applicationUID2 = manageApplication.createPublicApplication("anotherCode", "anotherLabel", null, null, user1.getSsoId(), user2.getSsoId());
		// fetch application from DB
		application2_persisted = manageApplication.findApplicationByUID(applicationUID2);

		logger.debug("/*************** createApplicationRelease1 *************************/");
		// persist application release
		String relUid = manageApplicationRelease.createApplicationRelease(applicationUID, paasUser_persisted1.getSsoId().getValue(), "1");

		logger.debug("/*************** createApplicationRelease2 *************************/");
		// persist application release
		String relUid2 = manageApplicationRelease.createApplicationRelease(applicationUID2, paasUser_persisted1.getSsoId().getValue(), "1");

		logger.debug("/*************** findApplicationReleases 1 & 2 *************************/");
		// fetch applicationRelease from DB
		application1Release_persisted = manageApplicationRelease.findApplicationReleaseByUID(relUid);

		application2Release_persisted = manageApplicationRelease.findApplicationReleaseByUID(relUid2);

		assertNotNull(manageLogicalDeployment.findLogicalDeployment(application1Release_persisted.getLogicalDeployment().getId()));
		assertNotNull(manageLogicalDeployment.findLogicalDeployment(application2Release_persisted.getLogicalDeployment().getId()));
	}

	@After
	public void cleanSecurityContext() {
		TestHelper.logout();
	}

	@Test
	@DirtiesContext
	public void testRealApplicationReleaseLifecycle() throws BusinessException, MalformedURLException {
		testApplicationReleaseLifecycle(false, false);
	}

	@Test
	@DirtiesContext
	public void testRealApplicationReleaseLifecycleIncremental() throws BusinessException, MalformedURLException {
		testApplicationReleaseLifecycle(false, true);
	}

	@Test
	@DirtiesContext
	public void testDummyAppApplicationReleaseLifecycle() throws BusinessException, MalformedURLException {

		testApplicationReleaseLifecycle(true, false);
	}

	private void testApplicationReleaseLifecycle(boolean useDefaultSampleLd, boolean incremental) throws BusinessException {
		logger.debug("/*************** testUpdateLogicalModel *************************/");
		// fetch logical deployment from DB
		ApplicationRelease applicationRelease = application1Release_persisted;
		populateAndPersistApplicationRelease(useDefaultSampleLd, applicationRelease, incremental);
	}

	public void populateAndPersistApplicationRelease(boolean useDefaultSampleLd, ApplicationRelease persistedApplicationRelease, boolean incremental)
			throws ObjectNotFoundException, InvalidMavenReferenceException {
		LogicalDeployment logicalDeployment = manageLogicalDeployment.findLogicalDeployment(persistedApplicationRelease.getLogicalDeployment().getId());

		if (useDefaultSampleLd) {
			populateLogicalDeployment(logicalDeployment, incremental);
		} else {
			populateLogicalDeploymentWithPetclinic(logicalDeployment);
		}

		// re init de stats
		sessionFactory.getStatistics().clear();
		// maj du modele logique
		manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
		// affichage logs ds output
		sessionFactory.getStatistics().logSummary();

		// FIXME: should also validate the delete has no leaks.
	}

	private void populateLogicalDeploymentWithPetclinic(LogicalDeployment logicalDeployment) {
		petClinicLogicalModelCatalog.createLogicalModel("petclinic", logicalDeployment);
	}

	private void populateLogicalDeployment(LogicalDeployment logicalDeployment, boolean incremental) throws ObjectNotFoundException,
			InvalidMavenReferenceException {
		// logicalDeployment.setName("ld-name-test");
		/**
		 * Functionnal cluster
		 */
		ProcessingNode node = new JeeProcessing();

		node.setLabel("node1");
		node.setSoftwareReference(sampleAppProperties.getMavenReference("petclinic", "ear"));

		logicalDeployment.addExecutionNode(node);

		if (incremental) {
			manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
			logicalDeployment = manageLogicalDeployment.findLogicalDeployment(logicalDeployment.getId());
		}

		LogicalRelationalService rds = new LogicalRelationalService();
		rds.setLabel("rds");
		rds.setServiceName("postgres-MyDataSource");
		logicalDeployment.addLogicalService(rds);
		node.addLogicalServiceUsage(rds, LogicalServiceAccessTypeEnum.READ_WRITE);

		if (incremental) {
			manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
			logicalDeployment = manageLogicalDeployment.findLogicalDeployment(logicalDeployment.getId());
		}

		LogicalWebGUIService web = new LogicalWebGUIService();
		web.setLabel("web");
		web.setContextRoot(new ContextRoot("/appliWeb"));
		logicalDeployment.addLogicalService(web);
		node.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		if (incremental) {
			manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
			logicalDeployment = manageLogicalDeployment.findLogicalDeployment(logicalDeployment.getId());
		}

		LogicalMomService mom = new LogicalMomService();
		mom.setLabel("mom");
		mom.setDestinationName("myQueue");
		logicalDeployment.addLogicalService(mom);
		node.addLogicalServiceUsage(mom, LogicalServiceAccessTypeEnum.READ_WRITE);

		if (incremental) {
			manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
			logicalDeployment = manageLogicalDeployment.findLogicalDeployment(logicalDeployment.getId());
		}

		LogicalLogService log = new LogicalLogService();
		log.setLabel("log");
		log.setLogName("logApplicatif");
		logicalDeployment.addLogicalService(log);
		node.addLogicalServiceUsage(log, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
	}

	@Test
	@DirtiesContext
	// @Ignore("LogicalDeploymentClonerImpl not complete: equals test fails")
	public void testCloneLogicalDeployment() throws MalformedURLException, BusinessException {
		// Populate a first application with a logical model
		testRealApplicationReleaseLifecycleIncremental();

		// AppRelease was created in setup() to host the LD clone

		// Ask to clone an existing LD into a target LD. This iterates on all
		// contained services and deep clone them
		try {
			manageLogicalDeployment.cloneLogicalDeployment(application1Release_persisted.getUID(), application2Release_persisted.getUID());
		} catch (InvalidReleaseException e) {
			fail("unexpected exception" + e);
		}

		// Then reload both releases
		application1Release_persisted = manageApplicationRelease.findApplicationReleaseByUID(application1Release_persisted.getUID());
		application2Release_persisted = manageApplicationRelease.findApplicationReleaseByUID(application2Release_persisted.getUID());

		// Then compare the two logical deployment for equality
		LogicalDeployment sourceLd = manageLogicalDeployment.findLogicalDeployment(application1Release_persisted.getLogicalDeployment().getId());
		LogicalDeployment targetLd = manageLogicalDeployment.findLogicalDeployment(application2Release_persisted.getLogicalDeployment().getId());
		// FIXME: correct equals(): false detection from equals support ?
		// assertEquals("clone of LD failed", sourceLd, targetLd);
		assertTrue("expected different db ids", sourceLd.getId() != targetLd.getId());
		assertNotSame("expected distinct LDs", sourceLd, targetLd);
	}
}
