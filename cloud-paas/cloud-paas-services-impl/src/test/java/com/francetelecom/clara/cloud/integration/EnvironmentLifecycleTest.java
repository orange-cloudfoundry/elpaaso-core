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
import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.application.ManageTechnicalDeployment;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDaoTestUtils;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import org.hibernate.SessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.UnsupportedEncodingException;
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
// TODO : test projection
public class EnvironmentLifecycleTest {

	private static final PaasUser JOHN_LENNON = new PaasUser("john", "Lennon", new SSOId("john123"), "john@orange.com");
	private static final PaasUser BOB_DYLAN = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");

	
	public static Logger logger = LoggerFactory.getLogger(EnvironmentLifecycleTest.class.getName());

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
	@Qualifier(value = "petClinicLogicalModelCatalog")
	SampleAppFactory logicalModelCatalog;

	PaasUser paasUser_persisted1;
	PaasUser paasUser_persisted2;
	Application application_persisted1;
	ApplicationRelease applicationRelease_persisted1;

	/**
	 * Maven Dao is mocked
	 */
	@Autowired
	@Qualifier("mvnDao")
	protected MvnRepoDao mvnRepoDaoMock;

	@Before
	public void setup() throws BusinessException, MalformedURLException {
		
		// given admin is authenticated
		TestHelper.loginAsAdmin();

		// Configure MvnRepoDao Mock
		MvnRepoDaoTestUtils.mockResolveUrl(mvnRepoDaoMock);

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

		logger.debug("/*************** createApplicationRelease *************************/");
		// persist application release
		String relUid = manageApplicationRelease.createApplicationRelease(applicationUID, paasUser_persisted1.getSsoId().getValue(), "1");

		logger.debug("/*************** findApplicationRelease *************************/");
		// fetch applicationRelease from DB
		applicationRelease_persisted1 = manageApplicationRelease.findApplicationReleaseByUID(relUid);

		// Refetch to eagerly fetch all fields.
		LogicalDeployment logicalDeployment = manageLogicalDeployment.findLogicalDeployment(applicationRelease_persisted1.getLogicalDeployment().getId());

		// petClinicLogicalModelCatalog.createLogicalModel("petclinic", 1,
		// logicalDeployment);
		logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
		logicalDeployment = manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
		logicalDeployment = manageLogicalDeployment.checkOverallConsistencyAndUpdateLogicalDeployment(logicalDeployment);
	}

	@After
	public void cleanSecurityContext() {
		TestHelper.logout();
	}

	@Test
	public void testEnvironmentLifecycle() throws MalformedURLException, UnsupportedEncodingException, BusinessException {

		// re init of stats
		sessionFactory.getStatistics().clear();

		String environmentUid = manageEnvironment.createEnvironment(applicationRelease_persisted1.getUID(), EnvironmentTypeEnum.PRODUCTION,
				paasUser_persisted1.getSsoId().getValue(), "Guillaume's env 1");

		// Test post conditions:
		// environment is persistent
		EnvironmentDto lookedUpEnv = manageEnvironment.findEnvironmentByUID(environmentUid);
		assertNotNull(lookedUpEnv);

		// AppRelease is locked
		ApplicationRelease refreshedAppRelease = manageApplicationRelease.findApplicationReleaseByUID(applicationRelease_persisted1.getUID());
		assertTrue(refreshedAppRelease.isLocked());

		// The environment is in inprogress until activation completes
		EnvironmentStatusEnum status = lookedUpEnv.getStatus();
		assertEquals(EnvironmentStatusEnum.CREATING, status);

		sessionFactory.getStatistics().logSummary();

	}
}
