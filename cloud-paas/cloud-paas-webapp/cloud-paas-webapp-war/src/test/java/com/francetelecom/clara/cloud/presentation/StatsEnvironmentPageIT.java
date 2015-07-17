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
package com.francetelecom.clara.cloud.presentation;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.HibernateStatsHelper;
import com.francetelecom.clara.cloud.commons.HibernateStatsReferenceType;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.environment.log.LogService;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.environments.SelectedEnvironmentPage;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.presentation.tools.PopulateDatasService;
import com.francetelecom.clara.cloud.presentation.utils.AuthenticationUtil;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;
import com.francetelecom.clara.cloud.scalability.ManageScalability;
import com.francetelecom.clara.cloud.scalability.ManageStatistics;
import com.francetelecom.clara.cloud.scalability.helper.StatisticsHelper;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;

/**
 * Created with IntelliJ IDEA.
 * User: shjn2064
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config/wicket-tester-env-context.xml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class StatsEnvironmentPageIT {

    protected static Logger logger = LoggerFactory.getLogger(StatsEnvironmentPageIT.class.getName());

    @Autowired
    protected ManageLogicalDeployment manageLogicalDeployment;
    @Autowired
    protected ManageEnvironment manageEnvironment;
    @Autowired
    protected ManageApplicationRelease manageApplicationRelease;
    @Autowired
    protected ManageApplication manageApplication;
    @Autowired
    protected ManagePaasUser managePaasUser;
    @Autowired
    private DelegatingDesignerServices delegatingDesignerServices;
    @Autowired
    private LogicalServicesHelper logicalServicesHelper;
    @Autowired
    private LogService logService;
    @Autowired
    private SplunkBean splunkBean;
    @Autowired
    private HypericBean hypericBean;
   

    @Autowired(required = true)
    ManageStatistics manageStatistics;

    @Autowired
    protected ManageScalability manageScalability;

    @Autowired(required = true)
    SessionFactory sessionFactory;

    protected PaasWicketTester myTester;
    private Application application;
    private ApplicationRelease applicationRelease;
    private EnvironmentDto environmentDto;


    @Autowired
    private PopulateDatasService populateDatasService;
    
    @Autowired
   	private AuthenticationManager authenticationManager;

    private String cuid = "testuser";
    private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

    @Before
    public void init() throws BusinessException, MalformedURLException {
        
    	// given Admin is authenticated
		AuthenticationUtil.connectAsAdmin();

    	// Enable Hibernate Stats and clear them
        sessionFactory.getStatistics().setStatisticsEnabled(true);
        sessionFactory.getStatistics().clear();
      
        // Create new Application with current Spring context
        myTester = new PaasWicketTester( new PaasTestApplication(getApplicationContextMock(), false));
        ((PaasTestSession)myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
        managePaasUser.checkBeforeCreatePaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
        myTester.startPage(HomePage.class);
        // Load lots of data in Database to have more realistics performances values
        manageScalability.populate("NNNDDDSSGGG", "portalTest", 1, 1, 1);

        // Create one elpaaso application, one applicationrelease with an environment using populate function
        populateDatasService.populateSingleApp("elPaaSoTomcatLogicalModelCatalog", "1", "1", false);

        //Get created objects after populate to display env detail page
        application = manageApplication.findApplications().iterator().next();
        applicationRelease = manageApplicationRelease.findMyApplicationReleases().iterator().next();
        environmentDto = manageEnvironment.findEnvironmentsByAppRelease(applicationRelease.getUID()).iterator().next();

    }


    @Test
    public void envPageTest() throws BusinessException, MalformedURLException {

        logger.info("################################### END INIT TEST #############################################");

        // Init some stats
        // Set reference values
        Map<HibernateStatsReferenceType, Long> refs = new HashMap<HibernateStatsReferenceType, Long>(14);
        // FIXME: reference value for duration is set to 10s as there is a known performance bug that needs to be fixed (art #82545)
        // Leaving the value to a lower value (e.g. 3s) tends to fail the test
        // see art #82545 for details
        refs.put(HibernateStatsReferenceType.DURATION, Long.valueOf(10000));
        refs.put(HibernateStatsReferenceType.QUERY_COUNT, Long.valueOf(2));
        refs.put(HibernateStatsReferenceType.QUERY_MAX_TIME_MS, Long.valueOf(250));

		refs.put(HibernateStatsReferenceType.ENTITY_FETCH_COUNT, Long.valueOf(5));
		refs.put(HibernateStatsReferenceType.ENTITY_LOAD_COUNT, Long.valueOf(60));
        refs.put(HibernateStatsReferenceType.ENTITY_INSERT_COUNT, Long.valueOf(0));
        refs.put(HibernateStatsReferenceType.ENTITY_DELETE_COUNT, Long.valueOf(0));
        refs.put(HibernateStatsReferenceType.ENTITY_UPDATE_COUNT, Long.valueOf(0));

		refs.put(HibernateStatsReferenceType.COLLECTION_FETCH_COUNT, Long.valueOf(26));
		refs.put(HibernateStatsReferenceType.COLLECTION_LOAD_COUNT, Long.valueOf(28));
        refs.put(HibernateStatsReferenceType.COLLECTION_RECREATE_COUNT, Long.valueOf(0));
        refs.put(HibernateStatsReferenceType.COLLECTION_REMOVE_COUNT, Long.valueOf(0));
        refs.put(HibernateStatsReferenceType.COLLECTION_UPDATE_COUNT, Long.valueOf(0));



        long startTime = System.currentTimeMillis();
        //Create parameters for selectedEnvPage
        PageParameters pageParameters = new PageParameters();
        pageParameters.add("appUid",application.getUID());
        pageParameters.add("releaseUid",applicationRelease.getUID());
        pageParameters.add("envUid",environmentDto.getUid());

        logger.info("################################### CLEAR STATS BEFORE Call Environment Page #############################################");
        sessionFactory.getStatistics().clear();
        //Start EnvPage
        myTester.startPage(SelectedEnvironmentPage.class, pageParameters);
        myTester.assertRenderedPage(SelectedEnvironmentPage.class);


        logger.info("################################### AFTER Call start env page #############################################");
        StatisticsHelper.logStats(sessionFactory.getStatistics());
        long duration = System.currentTimeMillis() - startTime;
        logger.info("Duration : " + duration + " ms");

        Statistics stats = sessionFactory.getStatistics();
        logger.info("Test duration : " + duration);
        StatisticsHelper.logStats(stats);

        // Check stats
        HibernateStatsHelper.checkStats(refs, duration, stats);

    }

    /**
     * Create an applicationContextMock to inject in Spring for Wicket
     * @return
     */
    private ApplicationContextMock getApplicationContextMock() {
        ApplicationContextMock applicationContextMock = new ApplicationContextMock();

        applicationContextMock.putBean(manageApplication);
        applicationContextMock.putBean(manageApplicationRelease);
        applicationContextMock.putBean(managePaasUser);
        applicationContextMock.putBean(splunkBean);
        applicationContextMock.putBean(hypericBean);
        applicationContextMock.putBean(manageEnvironment);
        applicationContextMock.putBean(manageLogicalDeployment);
        applicationContextMock.putBean(manageScalability);
        applicationContextMock.putBean(manageStatistics);
        applicationContextMock.putBean(sessionFactory);
        applicationContextMock.putBean(delegatingDesignerServices);
        applicationContextMock.putBean(logicalServicesHelper);
        applicationContextMock.putBean("authenticationManager",authenticationManager);
        return applicationContextMock;
    }
}
