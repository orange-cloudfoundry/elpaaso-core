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
package com.francetelecom.clara.cloud.paas.it.services;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.HibernateStatsHelper;
import com.francetelecom.clara.cloud.commons.HibernateStatsReferenceType;
import com.francetelecom.clara.cloud.commons.P6SpyAppender;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.paas.it.services.helper.AuthenticationHelper;
import com.francetelecom.clara.cloud.scalability.ManageScalability;
import com.francetelecom.clara.cloud.scalability.helper.StatisticsHelper;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.LogManager;

/**
 * PaasServicesScalabilityIT Scalability tests through the paas-services layer.
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@IfProfileValue(name = "test-groups", values = { "legacy-test" })
public class PaasServicesScalabilityIT {

	protected static Logger logger = LoggerFactory.getLogger(PaasServicesScalabilityIT.class.getName());

	@Autowired
	protected ManageScalability manageScalability;

	@Autowired
	protected ManageEnvironment manageEnvironment;

	@Autowired(required = true)
	SessionFactory sessionFactory;

    P6SpyAppender p6spyAppender = null;

	public PaasServicesScalabilityIT() {
		// Configure logs
		String propertiesFileName = "logging.properties";
		ClassLoader classLoader = getClass().getClassLoader();
		URL resource = classLoader.getResource(propertiesFileName);
		System.out.println("Forcing java.util logging configuration from " + resource);
		InputStream inputStream = classLoader.getResourceAsStream(propertiesFileName);
		try {
			LogManager.getLogManager().readConfiguration(inputStream);
		} catch (Exception e) {
			System.out.println("Unable to configure logging for " + propertiesFileName);
			e.printStackTrace();
		}
	}

	@Before
	public void setUp() throws MalformedURLException, BusinessException {
		AuthenticationHelper.loginAsAdmin();
		// Enable and clear statistics
		sessionFactory.getStatistics().setStatisticsEnabled(true);
		sessionFactory.getStatistics().clear();

        p6spyAppender = P6SpyAppender.getCurrentInstance();
    	if (p6spyAppender != null) {
    		p6spyAppender.reset();
    	}
    	
	}
	
    @After
    public void tearDown() {
    	p6spyAppender = P6SpyAppender.getCurrentInstance();
    	if (p6spyAppender != null) {
    		p6spyAppender.checkStats(true);
    	}
    	AuthenticationHelper.logout();
    }

	/**
	 * Creates data to test scalability:
	 * http://10.177.111.51/projects/el_paso_project/cards/1244
	 * <ul>
	 * <li>5 FUTS: with 5 people (mgr, arch, dev1, dev2, qa)
	 * <li>25 users + 7 ops
	 * <li>5 apps
	 * <li>5×5 releases with logical models of the following size
	 * <ul>
	 * <li>3 exec nodes
	 * <li>3 DBs
	 * <li>2 online stores
	 * <li>3 web gui
	 * </ul>
	 * <li>5×5x5 envs
	 * </ul>
	 */
	@Test
	public void scalabilityOnPopulate() throws BusinessException, MalformedURLException {
		scalabilityOnPopulate("NNNDDDSSGGG", "portalTest", 5, 5, 5);
	}

	/**
	 * Populate database with 2 applications x 2 releases x 2 environments
	 * @throws BusinessException
	 * @throws MalformedURLException
	 */
	@Test
	public void scalabilityOnPopulate_fewData() throws BusinessException, MalformedURLException {
		scalabilityOnPopulate("NNNDDDSSGGGB", "portalTest2", 2, 2, 2);
	}
	
	private void scalabilityOnPopulate(String pattern, String teamName, int nbApp, int nbReleasePerApp, int nbEnvPerRelease) throws BusinessException, MalformedURLException {
		// Set reference values
		Map<HibernateStatsReferenceType, Long> refs = new HashMap<HibernateStatsReferenceType, Long>(14);
		refs.put(HibernateStatsReferenceType.DURATION, Long.valueOf(3600000));
		refs.put(HibernateStatsReferenceType.QUERY_COUNT, Long.valueOf(1152));
		refs.put(HibernateStatsReferenceType.QUERY_MAX_TIME_MS, Long.valueOf(1700));

		refs.put(HibernateStatsReferenceType.ENTITY_FETCH_COUNT, Long.valueOf(8016));
		refs.put(HibernateStatsReferenceType.ENTITY_LOAD_COUNT, Long.valueOf(43968));
		refs.put(HibernateStatsReferenceType.ENTITY_INSERT_COUNT, Long.valueOf(44455));
		refs.put(HibernateStatsReferenceType.ENTITY_DELETE_COUNT, Long.valueOf(0));
		refs.put(HibernateStatsReferenceType.ENTITY_UPDATE_COUNT, Long.valueOf(3480));

		refs.put(HibernateStatsReferenceType.COLLECTION_FETCH_COUNT, Long.valueOf(16360));
		refs.put(HibernateStatsReferenceType.COLLECTION_LOAD_COUNT, Long.valueOf(16982));
		refs.put(HibernateStatsReferenceType.COLLECTION_RECREATE_COUNT, Long.valueOf(29000));
		refs.put(HibernateStatsReferenceType.COLLECTION_REMOVE_COUNT, Long.valueOf(0));
		refs.put(HibernateStatsReferenceType.COLLECTION_UPDATE_COUNT, Long.valueOf(75));

		// Creation of all paas users, app, app release and env
		long startTime = System.currentTimeMillis();
		manageScalability.populate(pattern, teamName, nbApp, nbReleasePerApp, nbEnvPerRelease);

        long duration = System.currentTimeMillis() - startTime;
        Statistics stats = sessionFactory.getStatistics();
        logger.info("Test duration : " + duration);
        StatisticsHelper.logStats(stats);

		// Check stats
        // Durations are not ignored as checking durations tends to make this test fragile due to IaaS and DBaaS response time dependencies
        HibernateStatsHelper.checkStatsIgnoringDuration(refs, stats);
	}
	
	@Test
	public void scalabilityEnvDTO() throws BusinessException, MalformedURLException, InterruptedException {
		// Set reference values
		Map<HibernateStatsReferenceType, Long> refs = new HashMap<HibernateStatsReferenceType, Long>(14);
		refs.put(HibernateStatsReferenceType.DURATION, Long.valueOf(1000));   //2389
		refs.put(HibernateStatsReferenceType.QUERY_COUNT, Long.valueOf(5));
		refs.put(HibernateStatsReferenceType.QUERY_MAX_TIME_MS, Long.valueOf(100));   //1414

		refs.put(HibernateStatsReferenceType.ENTITY_FETCH_COUNT, Long.valueOf(27));
		refs.put(HibernateStatsReferenceType.ENTITY_LOAD_COUNT, Long.valueOf(138));
		refs.put(HibernateStatsReferenceType.ENTITY_INSERT_COUNT, Long.valueOf(0));
		refs.put(HibernateStatsReferenceType.ENTITY_DELETE_COUNT, Long.valueOf(0));
		refs.put(HibernateStatsReferenceType.ENTITY_UPDATE_COUNT, Long.valueOf(0));

		refs.put(HibernateStatsReferenceType.COLLECTION_FETCH_COUNT, Long.valueOf(4));
		refs.put(HibernateStatsReferenceType.COLLECTION_LOAD_COUNT, Long.valueOf(7));
		refs.put(HibernateStatsReferenceType.COLLECTION_RECREATE_COUNT, Long.valueOf(0));
		refs.put(HibernateStatsReferenceType.COLLECTION_REMOVE_COUNT, Long.valueOf(0));
		refs.put(HibernateStatsReferenceType.COLLECTION_UPDATE_COUNT, Long.valueOf(0));

		// Populate 1 env
		Collection<ApplicationRelease> releases = manageScalability.populate("NNNDDDSSGGG", "portalTest", 1, 1, 1);
		ApplicationRelease release = releases.iterator().next();
		String envUid = manageEnvironment.findEnvironmentsByAppRelease(release.getUID()).get(0).getUid();
		EnvironmentDto dto = null;

        long timeout = System.currentTimeMillis() + getEnvironmentCreationTimeout() * 1000;
        dto = manageEnvironment.findEnvironmentByUID(envUid);
		do {
            if(System.currentTimeMillis() > timeout) {
                Assert.fail("Timeout: environment not "+dto.getStatus()+" after "+getEnvironmentCreationTimeout()+" secondes");
            }
            if (!dto.getStatus().toString().endsWith("ING") && dto.getStatus() != EnvironmentStatusEnum.RUNNING) {
                // In a final step, will not change until an action is requested
                Assert.assertEquals("Activation process failed : " + dto.getStatusMessage(), EnvironmentStatusEnum.RUNNING, dto.getStatus());
            }

			Thread.sleep(1000);
			dto = manageEnvironment.findEnvironmentByUID(envUid);

        } while (dto != null && dto.getStatus().equals(EnvironmentStatusEnum.CREATING));



		// Do not want to test populate so clear stats
		sessionFactory.getStatistics().clear();
		StatisticsHelper.logStats(sessionFactory.getStatistics());

		// Get DTO
		long startTime = System.currentTimeMillis();
		dto = manageEnvironment.findEnvironmentDetails(envUid);

        long duration = System.currentTimeMillis() - startTime;
        Statistics stats = sessionFactory.getStatistics();
        logger.info("Test duration : " + duration);
        StatisticsHelper.logStats(stats);

		// Check stats
        // Durations are not ignored as checking durations tends to make this test fragile due to IaaS and DBaaS response time dependencies
        HibernateStatsHelper.checkStatsIgnoringDuration(refs, stats);
	}

    /**
     * @return environment creation timeout in secondes: 
     * 2 minutes should be enough for scalability tests that do not create XaaS resources (activation process is empty) 
     */
    public int getEnvironmentCreationTimeout() {
        return 120;
    }
}
