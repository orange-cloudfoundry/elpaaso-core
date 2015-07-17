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

import org.hibernate.SessionFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;

/**
 * paas user lifecycle lifecycle test
 * 
 * @author Clara
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/integration/LifecycleTests-context.xml")
@DirtiesContext
public class PaasUserLifecycleTest {

	private static final PaasUser BOB_DYLAN = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");


	public static Logger logger = LoggerFactory.getLogger(PaasUserLifecycleTest.class.getName());

	@Autowired(required = true)
	ManagePaasUser managePaasUser;

	@Autowired(required = true)
	SessionFactory sessionFactory;

	@Test
	public void testPassUserLifecycle() throws BusinessException {
		// test setup
		logger.debug("/*************** createPaasUser *************************/");
		// Initialization of Hibernate statistics
		sessionFactory.getStatistics().clear();
		// persist paas user
		managePaasUser.checkBeforeCreatePaasUser(BOB_DYLAN);
		// display of Hibernate statistics
		sessionFactory.getStatistics().logSummary();

		logger.debug("/*************** findPaasUser *************************/");
		// Initialization of Hibernate statistics
		sessionFactory.getStatistics().clear();
		// fetch paas user
		managePaasUser.findPaasUser(BOB_DYLAN.getSsoId().getValue());
		// display of Hibernate statistics
		sessionFactory.getStatistics().logSummary();

		logger.debug("/*************** updatePaasUser *************************/");
		// Initialization of Hibernate statistics
		sessionFactory.getStatistics().clear();
		// update paas user
		BOB_DYLAN.setFirstName("new_user_name_1");
		managePaasUser.updatePaasUser(BOB_DYLAN);
		// display of Hibernate statistics
		sessionFactory.getStatistics().logSummary();

		logger.debug("/*************** deletePaasUser *************************/");
		// Initialization of Hibernate statistics
		sessionFactory.getStatistics().clear();
		// delete paas user
		managePaasUser.deletePaasUser(BOB_DYLAN.getId());
		// display of Hibernate statistics
		sessionFactory.getStatistics().logSummary();

	}

}
