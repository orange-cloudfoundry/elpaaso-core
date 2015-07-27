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
package com.francetelecom.clara.cloud.presentation.applications;

import com.francetelecom.clara.cloud.core.service.ManageApplicationImpl;
import com.francetelecom.clara.cloud.core.service.ManageApplicationReleaseImpl;
import com.francetelecom.clara.cloud.core.service.ManagePaasUserImpl;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeploymentImpl;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.mockito.Mock;
import org.springframework.security.authentication.AuthenticationManager;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/**
 * ApplicationsPagesBaseTest
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public abstract class ApplicationsPagesBaseTest {
    @Mock
    protected ManageApplicationImpl manageApplication;
    @Mock
    protected ManageApplicationReleaseImpl manageApplicationRelease;
    @Mock
    protected ManagePaasUserImpl managePaasUser;
    @Mock
    protected SplunkBean splunkBean;
    @Mock
    protected HypericBean hypericBean;
    @Mock
    protected ManageLogicalDeploymentImpl manageLogicalDeployment;
    @Mock
    protected DelegatingDesignerServices delegatingDesignerServices;
    @Mock
    protected LogicalServicesHelper logicalServicesHelper;
    @Mock
    protected AuthenticationManager authenticationManager;
    @Mock
    protected ContactUsBean contactUsBean;

    private String appUid = "myAppUid";
    private String releaseVersion = "G00R01";
    private String appCode = "MyAppCode";
    private String appLabel = "MyAppLabel";
    private String appDescription = "MyAppDescription";
    private String releaseDescription = "my initial release of my first application in the cloud";
    
    protected Application publicApp = createPublicApplicationMock();
    protected Application privateApp = createPrivateApplicationMock();
    protected Application updatedApp;
    protected ApplicationRelease mockedRelease = createReleaseMock();
    protected List<MiddlewareProfile> mockedProfilesList;

    /**
     * Create an applicationContextMock to inject in Spring for Wicket
     * @return mocked application context
     */
    protected ApplicationContextMock getApplicationContextMock() {
        ApplicationContextMock applicationContextMock = new ApplicationContextMock();
        try {
         applicationContextMock.putBean(manageApplication);
        } catch (NullPointerException npe) {
            npe.printStackTrace();
            throw new RuntimeException("applicationContextMock NullPointerException : maybe you miss mock initialization like '@RunWith(MockitoJUnitRunner.class)' ?");
        }
        applicationContextMock.putBean(manageApplicationRelease);
        applicationContextMock.putBean(managePaasUser);
        applicationContextMock.putBean(splunkBean);
        applicationContextMock.putBean(hypericBean);
        applicationContextMock.putBean(manageLogicalDeployment);
        applicationContextMock.putBean(delegatingDesignerServices);
        applicationContextMock.putBean(logicalServicesHelper);
        applicationContextMock.putBean("authenticationManager", authenticationManager);
        applicationContextMock.putBean(contactUsBean);
        return applicationContextMock;
    }

    private Application createPublicApplicationMock() {
        Application app = new Application(appLabel, appCode);
        app.setDescription(appDescription);
        app.setMembers(new HashSet<>(Arrays.asList(new SSOId("lois"), new SSOId("clark"))));
        app.setAsPublic();
        return app;
    }

    private Application createPrivateApplicationMock() {
        Application app = new Application(appLabel, appCode);
        app.setDescription(appDescription);
        app.setMembers(new HashSet<>(Arrays.asList(new SSOId("lois"), new SSOId("clark"))));
        app.setAsPrivate();
        app.setEditable(false);
        return app;
    }

    private ApplicationRelease createReleaseMock() {
        ApplicationRelease release = new ApplicationRelease(createPublicApplicationMock(),releaseVersion);
        release.setDescription(releaseDescription);
        return release;
    }
    
    protected void prepareMocksForPublicApp() throws Exception {
        // Prepare mocks for app provider
        when(manageApplication.findApplications()).thenReturn(Arrays.asList(createPublicApplicationMock()));
        when(manageApplication.findMyApplications()).thenReturn(Arrays.asList(createPublicApplicationMock()));
        when(manageApplication.countApplications()).thenReturn(Long.valueOf("1"));
        when(manageApplication.countMyApplications()).thenReturn(Long.valueOf("1"));
        when(manageApplication.findApplicationByUID(anyString())).thenReturn(createPublicApplicationMock());
    }
    
    protected void prepareMocksForPrivateApp() throws Exception {
        // Prepare mocks for app provider : private app is not listed by default
        when(manageApplication.findApplications()).thenReturn(Arrays.asList(createPrivateApplicationMock()));
        when(manageApplication.findMyApplications()).thenReturn(Arrays.asList(createPrivateApplicationMock()));
        when(manageApplication.countApplications()).thenReturn(Long.valueOf("1"));
        when(manageApplication.countMyApplications()).thenReturn(Long.valueOf("1"));
        when(manageApplication.findApplicationByUID(anyString())).thenReturn(createPrivateApplicationMock());
    }

    protected void prepareMocksForAppDeletion() throws Exception {
        // mocks to be able to delete application
        when(manageApplication.canBeDeleted(anyString())).thenReturn(true);
        doCallRealMethod().when(manageApplication).deleteApplication(eq(appUid));
    }
    
    protected void prepareMocksForAppUpdate() throws Exception {
        // Prepare mocks for updated app
        updatedApp = new Application(publicApp.getLabel() + "_modify", publicApp.getCode() + "_modify");
        updatedApp.setDescription(publicApp.getDescription() + "_modify");
        updatedApp.setAsPrivate();
        updatedApp.setMembers(new HashSet<>(Arrays.asList(new SSOId("marge"), new SSOId("homer"), new SSOId("bart"), new SSOId("lisa"))));
        when(manageApplication.updateApplication(any(Application.class))).thenReturn(updatedApp);
    }

    protected void prepareMocksForAppCreation() throws Exception {
        // Prepare mocks to create app and its first release
        String releaseUid = "myReleaseUid";

        mockedProfilesList = Arrays.asList(MiddlewareProfile.values());

        when(manageApplication.createPublicApplication(anyString(), anyString(), anyString(), any(URL.class), (SSOId[]) anyVararg())).thenReturn(appUid);
        when(manageApplicationRelease.createApplicationRelease(anyString(), anyString(), anyString())).thenReturn(releaseUid);
        when(manageApplicationRelease.findApplicationReleaseByUID(anyString())).thenReturn(createReleaseMock());
        when(manageApplication.isApplicationLabelUnique(anyString())).thenReturn(true);
        when(delegatingDesignerServices.createPanelFor(anyString(), any(LogicalModelItem.class), any(DesignerHelperPage.class), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(new EmptyPanel("logicalServicePanel"));
        doCallRealMethod().when(manageApplicationRelease).updateApplicationRelease(eq(createReleaseMock()));
        doCallRealMethod().when(manageLogicalDeployment).cloneLogicalDeployment(eq("myReleaseToCloneUid"), eq(releaseUid));
        when(manageLogicalDeployment.findLogicalDeployment(anyInt())).thenReturn(new LogicalDeployment());
        when(manageApplicationRelease.findAllMiddlewareProfil()).thenReturn(mockedProfilesList);
    }
}
