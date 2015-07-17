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
package com.francetelecom.clara.cloud.presentation.designer;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

import java.net.URL;

import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.presentation.utils.AuthenticationUtil;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithGUI;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.NavigationUtils;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;
import com.francetelecom.clara.cloud.presentation.validators.ConfigDuplicateKeysValidator;
import com.francetelecom.clara.cloud.presentation.validators.ConfigMaxNumberKeysValidator;
import com.francetelecom.clara.cloud.technicalservice.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.PaasUserNotFoundException;

/**
 * ConfigConstraintsTest
 *
 * Last updated : $LastChangedDate: 2012-06-11 17:23:44 +0200 (lun., 11 juin 2012) $
 * Last author  : $Author: dwvd1206 $
 * @version     : $Revision: 17582 $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config/designer-context.xml")
public class ConfigConstraintsTest {

    @Autowired
    private ManageApplication manageApplication;
    @Autowired
    private ManageApplicationRelease manageApplicationRelease;
    @Autowired
    private ManagePaasUser managePaasUser;
    @Autowired
    private ManageLogicalDeployment manageLogicalDeployment;
    @Autowired
    private MvnRepoDao mvnDao;
    @Autowired
    private LogicalServicesHelper logicalServicesHelper;
    @Autowired
    @Qualifier("allServicesLogicalModelCatalog")
    private SampleAppFactory allServicesLogicalSample;
    @Autowired
    private DelegatingDesignerServices delegatingDesignerServices;
    @Autowired
    private SplunkBean splunkBean;
    @Autowired
    private HypericBean hypericBean;
    @Autowired
  	private AuthenticationManager authenticationManager;
    @Autowired
  	private ContactUsBean contactUsBean;

    private LogicalDeployment logicalDeployment;

    private PaasWicketTester myTester;

    private String releaseUid;
    private String appUid;

    private String cuid = "testuser";
    private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

    @Before
    public void init() {
    	
    	//Admin authenticated
    	AuthenticationUtil.connectAsAdmin();
    	
        myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
        ((PaasTestSession)myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
        managePaasUser.checkBeforeCreatePaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));

        myTester.startPage(HomePage.class);


        createApplicationAndFirstRelease(allServicesLogicalSample);
        createAllServicesLD();

        //Mock all WS Call
        when(mvnDao.resolveUrl(isA(MavenReference.class))).thenAnswer(new Answer<MavenReference>() {
            @Override
            public MavenReference answer(InvocationOnMock invocation) throws Throwable {
                MavenReference input = (MavenReference) invocation.getArguments()[0];

                if (input == null) {
                    input = new MavenReference();
                }

                MavenReference output = null;
                // Surprisingly, invocation is sometimes null...
                if (input != null) {
                    output = new MavenReference(input);
                    // compute a representative accessurl
                    output.setAccessUrl(new URL("http://myrepo:80/" + input.getArtifactName()));
                }
                return output;

            }
        });

        // go on designer step two page to access internal services
        NavigationUtils.goOnDesignerPage(myTester, releaseUid);
        NavigationUtils.goOnNextStep(myTester);

    }

    private String escapedRegex(String expectedString) {
        String escapedExpectedString = expectedString.replaceAll("\\(","\\\\(");
        escapedExpectedString = escapedExpectedString.replaceAll("\\)","\\\\)");
        escapedExpectedString = escapedExpectedString.replaceAll("\\[","\\\\[");
        escapedExpectedString = escapedExpectedString.replaceAll("\\]","\\\\]");
        return escapedExpectedString;
    }

    @Test
    public void testMaxKeys() {
        int maxEntries = ProcessingNode.MAX_CONFIG_SET_ENTRIES_PER_EXEC_NODE;
        String tooManyKeysContent = "";
        for (int i = 0; i < maxEntries + 1; i++) {
            tooManyKeysContent += "Key" + i + "=value" + i + "\n";
        }
        CreateObjectsWithGUI.createConfig(myTester, "config", "invalid", tooManyKeysContent);

        String expectedError = myTester.getLastRenderedPage()
                .getString(
                        ConfigMaxNumberKeysValidator.ERROR_MESSAGE_KEY,
                        new Model<String[]>(new String[]{String.valueOf(maxEntries+1), String.valueOf(maxEntries)}));

        // Then: an error message is displayed
        myTester.assertErrorMessages(new String[]{expectedError});

        // escape regExp cars to call assertContains
        myTester.assertContains(escapedRegex(expectedError));
    }



    @Test
    public void testDuplicateKeysSingleService() {
        myTester.assertNoErrorMessage();
        String duplicateKeyContent = "Key1=value1\nKey1=value2";
        CreateObjectsWithGUI.createConfig(myTester, "config", "invalid", duplicateKeyContent);

        String expectedError = myTester.getLastRenderedPage()
                .getString(
                        ConfigDuplicateKeysValidator.ERROR_MESSAGE_KEY,
                        new Model<String[]>(new String[]{"[Key1]"}));
        // Then: an error message is displayed
        myTester.assertErrorMessages(new String[]{expectedError});
        myTester.assertContains(escapedRegex(expectedError));
    }

    @Test
    public void testDuplicateKeysOverall() {

        // Given: an application with its release and an architecture with a JEE Processing node
        myTester.assertNoErrorMessage();
        CreateObjectsWithGUI.createJEEProcessing(myTester, "execNode", "com.francetelecom.clara.prototype.springoojpa", "springoojpa-ear", "6.1.0", "", false, 128);
        myTester.dumpPage();
        CreateObjectsWithGUI.createConfig(myTester, "config1", "invalid", "Key=value1");
        CreateObjectsWithGUI.createConfig(myTester, "config2", "invalid", "Key=value2");
        myTester.dumpPage();
        CreateObjectsWithGUI.createAssociationAtCell(myTester, 7, 4);
        CreateObjectsWithGUI.createAssociationAtCell(myTester, 8, 4);
        myTester.dumpPage();
        // go on designer step three page to check architecture overall consistency
        NavigationUtils.goOnNextStep(myTester);

        // Then: an error message is displayed
        myTester.assertErrorMessages(new String[]{"Duplicate config keys for JEE Processing execNode: [invalidKey]"});
        myTester.assertContains("Duplicate config keys for JEE Processing execNode: \\[invalidKey\\]");
    }

    private void createApplicationAndFirstRelease(SampleAppFactory sample) {

        try {
            releaseUid = manageApplicationRelease.createApplicationRelease(createPublicApplication(sample), cuid, sample.getAppReleaseVersion());
        } catch (PaasUserNotFoundException e) {
            e.printStackTrace();
        } catch (ApplicationNotFoundException e) {
            e.printStackTrace();
        } catch (DuplicateApplicationReleaseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private String createPublicApplication(SampleAppFactory sample) {
        try {
            appUid = manageApplication.createPublicApplication(sample.getAppCode(), sample.getAppLabel(), sample.getAppDescription(), null, new SSOId(cuid));
        } catch (DuplicateApplicationException | PaasUserNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return appUid;
    }

    private void createAllServicesLD() {
        try {
            ApplicationRelease release = manageApplicationRelease.findApplicationReleaseByUID(releaseUid);
            LogicalDeployment ld = manageLogicalDeployment.findLogicalDeployment(release.getLogicalDeployment().getId());
            logicalDeployment = allServicesLogicalSample.populateLogicalDeployment(ld);
            manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
        } catch (ObjectNotFoundException e) {
            e.printStackTrace();
        } catch (InvalidMavenReferenceException e) {
            e.printStackTrace();
        }
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
        applicationContextMock.putBean(manageLogicalDeployment);
        applicationContextMock.putBean(mvnDao);
        applicationContextMock.putBean(logicalServicesHelper);
        applicationContextMock.putBean(delegatingDesignerServices);
        applicationContextMock.putBean(splunkBean);
        applicationContextMock.putBean(hypericBean);
        applicationContextMock.putBean(contactUsBean);
        applicationContextMock.putBean("authenticationManager",authenticationManager);
        return applicationContextMock;
    }

    @After
    public void clean() {

        try {

            manageApplicationRelease.deleteApplicationRelease(releaseUid);
            manageApplication.deleteApplication(appUid);

        } catch (BusinessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        myTester.destroy();
    }


}
