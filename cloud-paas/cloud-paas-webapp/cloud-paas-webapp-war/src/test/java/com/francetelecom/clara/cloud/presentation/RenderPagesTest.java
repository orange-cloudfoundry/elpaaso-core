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

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.presentation.applications.ApplicationsPage;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.environments.EnvironmentsPage;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.presentation.releases.ReleasesPage;
import com.francetelecom.clara.cloud.presentation.utils.*;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 12/01/12
 * Time: 16:30
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config/designer-context.xml")
public class RenderPagesTest {

    @Autowired
    private ManageApplication manageApplication;
    @Autowired
    private ManageApplicationRelease manageApplicationRelease;
    @Autowired
    private ManagePaasUser managePaasUser;
    @Autowired
    private SplunkBean splunkBean;
    @Autowired
    private HypericBean hypericBean;
    @Autowired
    private ManageLogicalDeployment manageLogicalDeployment;
    @Autowired
    private DelegatingDesignerServices delegatingDesignerServices;
    @Autowired
    private LogicalServicesHelper logicalServicesHelper;
    @Autowired
    private ManageEnvironment manageEnvironment;

    private PaasWicketTester myTester;
    
    @Autowired
	private AuthenticationManager authenticationManager;
    
    @Autowired
	private ContactUsBean contactUsBean;

    private String cuid = "testuser";
    private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

    @Before
    public void init() {
        myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
        ((PaasTestSession)myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
        AuthenticationUtil.connect(cuid, role.toString());
    }

	@Test
	public void homePageRendersSuccessfully() {
        PageRendersTest.testPageRenders(myTester, HomePage.class);
	}

    @Test
    public void applicationPageRendersSuccessfully() {
        PageRendersTest.testPageRenders(myTester, ApplicationsPage.class);
    }

    @Test
    public void releasesPageRendersSuccessfully() {
        PageRendersTest.testPageRenders(myTester, ReleasesPage.class);
    }

    @Test
    public void environmentsPageRendersSuccessfully() {
        PageRendersTest.testPageRenders(myTester, EnvironmentsPage.class);
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
        applicationContextMock.putBean(manageLogicalDeployment);
        applicationContextMock.putBean(manageEnvironment);
        applicationContextMock.putBean(delegatingDesignerServices);
        applicationContextMock.putBean(logicalServicesHelper);
        applicationContextMock.putBean(contactUsBean);
        applicationContextMock.putBean("authenticationManager",authenticationManager);

        return applicationContextMock;
    }

}
