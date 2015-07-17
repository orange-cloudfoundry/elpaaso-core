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
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDaoTestUtils;
import com.francetelecom.clara.cloud.scalability.ManageScalability;
import com.francetelecom.clara.cloud.scalability.ManageStatistics;
import com.francetelecom.clara.cloud.scalability.helper.PaasStats;

/**
 * ManageStatisticsImplTest Class that test ManageStatisticsImpl Last update :
 * $LastChangedDate$ Last
 * author : $Author$
 * 
 * @version : $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ManageStatisticsImplTest {
	/**
	 * Logger
	 */
	private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ManageStatisticsImplTest.class);

	@Autowired(required = true)
	ManageStatistics manageStatistics;
	@Autowired(required = true)
	ManageScalability manageScalability;
	@Autowired(required = true)
	ManageApplication manageApplication;
	@Autowired(required = true)
	ManageApplicationRelease manageApplicationRelease;

	/**
	 * Maven Dao is mocked
	 * 
	 * @link http://elpaaso_shp/index.php/Development_%
	 *       26_Test_Tips#How_to_mock_MvnRepoDao.resolveUrl.28.29_.3F
	 */
	@Autowired
	protected MvnRepoDao mvnRepoDaoMock;

	@Before
	public void init() {
		// given admin is authenticated
		TestHelper.loginAsAdmin();

		// Mock MvnRepoDao.resolveUrl()
		// @link
		// http://elpaaso_shp/index.php/HowTo_mock_MvnRepoDao
		MvnRepoDaoTestUtils.mockResolveUrl(mvnRepoDaoMock);

		// enable stats
		manageStatistics.setStatsState(true);
	}

	@After
	public void cleanSecurityContext() {
		TestHelper.logout();
	}

	@Test
	public void testStatisticsPopulateSimpleTestPhase() throws BusinessException {
		Assert.assertEquals("expected stats enabled", true, manageStatistics.isStatEnable());
		long snapId = manageStatistics.startSnapshot("testStatisticsPopulateSimpleTestPhase");
		String prefix = "simpleStatsTst";
		Collection<PaasUser> testUsers = manageScalability.createPaasUsers(prefix, 1);
		PaasUser firstUser = testUsers.iterator().next();
		Assert.assertNotNull("no user created ?", firstUser);
		boolean mustCreateOneEnvironment = true;
		Application newApp = manageScalability.populateSimpleTestPhase(firstUser, mustCreateOneEnvironment);
		Collection<Application> readApps = manageApplication.findApplications();
		for (Application a : readApps) {
			manageApplicationRelease.findApplicationReleasesByAppUID(a.getUID());
		}
		PaasStats statsSnap = manageStatistics.endSnapShot(snapId);

		Assert.assertNotNull("snapshot statistics is null ?", statsSnap);
		logger.info(statsSnap.toString());

		Map<String, Long> deltaValues = statsSnap.getDeltaValues();
		Assert.assertNotNull("snapshot statistics delta values is null ?", deltaValues);
		logger.info("delta values :");
		for (Map.Entry<String, Long> deltaVal : deltaValues.entrySet()) {
			logger.info("delta value \"{}\"={}", deltaVal.getKey(), deltaVal.getValue().toString());
		}
		Long tdiInsertCount = deltaValues.get(TechnicalDeploymentInstance.class.getSimpleName() + " Inserted");
		Assert.assertNotNull("snapshot statistics TechDep Inserted delta value is null ?", tdiInsertCount);
		Assert.assertEquals("expected 1 TD inserted ?", 1, tdiInsertCount.longValue());
	}
}
