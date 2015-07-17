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
package com.francetelecom.clara.cloud.services;

import java.net.MalformedURLException;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;

/**
 * ManageLogicalDeploymentImplIT Class which test the
 * checkLogicalSoapServiceConsistency service
 * 
 * Last updated : $LastChangedDate: 2012-06-07 16:53:15 +0200 (jeu., 07 juin
 * 2012) $ Last author : $Author$
 * 
 * @author Clara
 * @version : $Revision$
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ManageLogicalDeploymentImplIT {

	private static final PaasUser BOB_DYLAN = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
	
	private static final Logger logger = LoggerFactory.getLogger(ManageLogicalDeploymentImplIT.class);

	@Autowired
	protected ManagePaasUser managePaasUser;

	@Autowired
	protected ManageApplication manageApplication;

	@Autowired
	protected ManageApplicationRelease manageApplicationRelease;

	@Autowired(required = true)
	ManageLogicalDeployment manageLogicalDeployment;

	@Autowired(required = false)
	SampleAppFactory logicalModelCatalog;

	protected static Application application = null;
	protected static int logicalDeploymentId;

	/**
	 * Test setup consist in creating an Application, then an ApplicationRelease
	 * and finally an Environment
	 * 
	 * @throws com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException
	 * @throws com.francetelecom.clara.cloud.technicalservice.exception.InvalidApplicationException
	 * @throws com.francetelecom.clara.cloud.technicalservice.exception.InvalidReleaseException
	 * @throws java.net.MalformedURLException
	 */
	@Before
	public void setUp() throws BusinessException, MalformedURLException {
		//all ITs are performed with admin roles
		// given admin is authenticated
		TestHelper.loginAsAdmin();
		
		logger.debug("/*************** createPaasUser *************************/");
 		managePaasUser.checkBeforeCreatePaasUser(BOB_DYLAN);

		logger.debug("/*************** createApplication *************************/");
		// persist application
		String applicationUID = manageApplication.createPublicApplication("aCode" + UUID.randomUUID(), "aLabel" + UUID.randomUUID(), null, null, BOB_DYLAN.getSsoId());
		// fetch application from DB
		application = manageApplication.findApplicationByUID(applicationUID);

		logger.debug("/*************** createApplicationRelease *************************/");
		// Creates and persist application release
		String applicationReleaseUID = manageApplicationRelease.createApplicationRelease(application.getUID(), BOB_DYLAN.getSsoId().getValue(), "1");
		// Fetch applicationRelease from DB
		ApplicationRelease applicationRelease = manageApplicationRelease.findApplicationReleaseByUID(applicationReleaseUID);

		logicalDeploymentId = applicationRelease.getLogicalDeployment().getId();

		logger.debug("/*************** createLogicalDeployment *************************/");
		// Refetch to eagerly fetch all fields.
		LogicalDeployment logicalDeployment = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentId);

		logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

		manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
	}

	@After
	public void cleanSecurityContext() {
		TestHelper.logout();
	}

	private LogicalSoapService getLogicalSoapService() throws ObjectNotFoundException {
		LogicalDeployment logicalDeployment = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentId);
		return logicalDeployment.listLogicalServices(LogicalSoapService.class).iterator().next();
	}

	/**
	 * check that the service is validated
	 * 
	 * @throws ObjectNotFoundException
	 */
	@Test
	public void checkLogicalSoapServiceConsistency() throws ObjectNotFoundException {
		LogicalSoapService s = getLogicalSoapService();
		try {
			manageLogicalDeployment.checkLogicalSoapServiceConsistency(s, true);
		} catch (BusinessException e) {
			logger.error("Exception while checking logical soap service consistency {} : {}", s.getName(), e.getMessage());
			Assert.fail(e.getMessage());
		}
	}

	/**
	 * check that invalid maven reference is reported as error
	 * 
	 * @throws BusinessException
	 */
	@Test(expected = InvalidMavenReferenceException.class)
	public void checkLogicalSoapServiceConsistency_invalidMaven() throws BusinessException {
		LogicalSoapService s = getLogicalSoapService();
		s.getServiceAttachments().setGroupId("unknown.reference");
		manageLogicalDeployment.checkLogicalSoapServiceConsistency(s, true);
	}

	/**
	 * check that no wsdl into the attachments (jar maven reference) is reported
	 * as error
	 * 
	 * @throws BusinessException
	 */
	@Test(expected = BusinessException.class)
	public void checkLogicalSoapServiceConsistency_invalidAttachmentsContent() throws BusinessException {
		LogicalSoapService s = getLogicalSoapService();
		String version = s.getServiceAttachments().getVersion();
		logger.info("current version is {}", version);
		s.getServiceAttachments().setArtifactId("cloud-paas-services");
		s.getServiceAttachments().setGroupId("com.francetelecom.clara.cloud");
		s.getServiceAttachments().setType("jar");
		manageLogicalDeployment.checkLogicalSoapServiceConsistency(s, true);
	}
}
