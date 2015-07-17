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
package com.francetelecom.clara.cloud.scalability.impl;

import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDaoTestUtils;
import com.francetelecom.clara.cloud.scalability.ManageScalability;


/**
 * ManageScalabilityImplTest
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ManageScalabilityImplTest {
    /**
     * Logger
     */
    private static final transient org.slf4j.Logger logger
       = LoggerFactory.getLogger(ManageScalabilityImplTest.class);

    @Autowired(required = true)
	ManageScalability manageScalability;

	/**
	 * Maven Dao is mocked
	 */
	@Autowired
	@Qualifier("mvnDao")
	protected MvnRepoDao mvnRepoDaoMock;
	
	@Before 
	public void setup() {
		// given admin is authenticated
		TestHelper.loginAsAdmin();
		
		// Configure MvnRepoDao Mock
		MvnRepoDaoTestUtils.mockResolveUrl(mvnRepoDaoMock);
	}

	@After
	public void cleanSecurityContext() throws BusinessException {
		manageScalability.razData();
		TestHelper.logout();
	}
	
	@Test
    public void should_populate_simple_app_release_env_and_delete_it() throws BusinessException {
		String prefix = "simpleTst";
		logger.debug("");
		Collection<PaasUser> testUsers = manageScalability.createPaasUsers(prefix, 1);
		Assert.assertNotNull("no user collection created ?", testUsers);
		PaasUser firstUser = testUsers.iterator().next();
		Assert.assertNotNull("no user created ?", firstUser);
		Assert.assertEquals(new SSOId(prefix + "0"), firstUser.getSsoId());
		boolean createAnEnvironment = true;
		Application newApp = manageScalability.populateSimpleTestPhase(firstUser, createAnEnvironment);
		Assert.assertNotNull("no app created ?", newApp);
		Assert.assertNotNull("no app label created ?", newApp.getLabel());
		logger.info("app successfully created");
	}

    @Test
    public void testPopulatePortalPhase() throws BusinessException {
      int nbApp=5;
      int nbReleasePerApp=5;
      int nbEnvPerRelease=0;
      Collection<ApplicationRelease> releases
        = manageScalability.populatePortalPhase(nbApp, nbReleasePerApp, nbEnvPerRelease);
      int awaitingReleasesCount = nbApp*nbReleasePerApp;
      Assert.assertNotNull("no app created ?", releases);
      Assert.assertEquals("get wrong appReleases count",
                          Integer.valueOf(awaitingReleasesCount),
                          Integer.valueOf(releases.size()));
    }

    @Test
    public void testHeavyPopulatePortalPhase() throws BusinessException  {
      String prefix = "heavyPortalTst";
      int nbUsers=150;
      Collection<PaasUser> testUsers = manageScalability.createPaasUsers(prefix, nbUsers);
      Assert.assertNotNull("no user collection created ?", testUsers);
      Assert.assertEquals("get wrong testUsers count",
                          Integer.valueOf(nbUsers),
                          Integer.valueOf(testUsers.size()));
      int nbApp=50;
      int nbReleasePerApp=10;
      int nbEnvPerRelease=0;
      Collection<ApplicationRelease> releases
        = manageScalability.populatePortalPhase(nbApp, nbReleasePerApp, nbEnvPerRelease);
      int awaitingReleasesCount = nbApp*nbReleasePerApp;
      Assert.assertNotNull("no app created ?", releases);
      Assert.assertEquals("get wrong appReleases count",
                          Integer.valueOf(awaitingReleasesCount),
                          Integer.valueOf(releases.size()));
    }
}
