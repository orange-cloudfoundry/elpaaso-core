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

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.core.service.exception.*;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalMomService;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.presentation.utils.*;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.junit.After;
import org.junit.Assert;
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

import java.net.URL;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * Created with IntelliJ IDEA.
 * User: shjn2064
 * Date: 09/05/12
 * Time: 15:34
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config/designer-context.xml")
public class InternalMomTest {

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
    private ContactUsBean contactUsBean;
    private PaasWicketTester myTester;

    private String releaseUid;
    private String appUid;

    private String cuid = "testuser";
    private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;
    
    @Autowired
	private AuthenticationManager authenticationManager;

    @Before
    public void init() {

    	// given Admin is authenticated
    	AuthenticationUtil.connectAsAdmin();

        myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
        ((PaasTestSession)myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
        managePaasUser.checkBeforeCreatePaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));

        myTester.startPage(HomePage.class);

        myTester.assertRenderedPage(HomePage.class);



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


        // Given: () for all tests an application with its release and an empty architecture
        createApplicationAndFirstRelease(allServicesLogicalSample);

        // go on designer step two page to access internal services
        NavigationUtils.goOnDesignerPage(myTester, releaseUid);
        NavigationUtils.goOnNextStep(myTester);

        //Select internal mom service
        CreateObjectsWithGUI.selectService(myTester, LogicalMomService.class);

        //Check that componenent path exists
        myTester.assertComponent(NavigationUtils.designerParamFormPath+":jmsConnectionFactoryJndiName", TextField.class);
    }

    @Test
    public void testEnableDeadLetterQueue() {

        // Get checkBoxComponenent
        AjaxCheckBox checkBoxComponent = (AjaxCheckBox) myTester.getComponentFromLastRenderedPage(NavigationUtils.designerParamFormPath+":hasDeadLetterQueue");
        
        // Check input enabled
        Assert.assertTrue(checkBoxComponent.getModelObject());
        myTester.assertEnabled(NavigationUtils.designerParamFormPath+":retriesBeforeMovingToDeadLetterQueue");
        myTester.assertEnabled(NavigationUtils.designerParamFormPath + ":deadLetterQueueCapacity");
        myTester.assertEnabled(NavigationUtils.designerParamFormPath+":deadLetterQueueName");
        
    }

    @Test
    public void testDisableDeadLetterQueue() {

        myTester.executeAjaxEvent(NavigationUtils.designerParamFormPath+":hasDeadLetterQueue","onclick");

        // Get checkBoxComponenent
        AjaxCheckBox checkBoxComponent = (AjaxCheckBox) myTester.getComponentFromLastRenderedPage(NavigationUtils.designerParamFormPath+":hasDeadLetterQueue");

        // Check input disabled
        Assert.assertFalse(checkBoxComponent.getModelObject());
        myTester.assertDisabled(NavigationUtils.designerParamFormPath + ":retriesBeforeMovingToDeadLetterQueue");
        myTester.assertDisabled(NavigationUtils.designerParamFormPath + ":deadLetterQueueCapacity");
        myTester.assertDisabled(NavigationUtils.designerParamFormPath+":deadLetterQueueName");

    }

    @Test
    public void testNegativeValueForMaxMessageSize() {

        //Create FormTester
        FormTester formTester = myTester.newFormTester(NavigationUtils.designerParamFormPath);
        //Fill form with default required values
        fillForm(formTester);
        // Set negative Value on msg size field
        formTester.setValue("msgMaxSizeKB", "-1");
        //Submit form
        myTester.executeAjaxEvent(NavigationUtils.designerParamFormPath + ":addUpdateButton", "onclick");
        //Check error
        //TODO : Wait for zenika i18n improvments
//        myTester.assertErrorMessages(new String[]{"msgMaxSizeKB doit être plus grand que 1"});

    }

    @Test
    public void testValidValueForMaxMessageSize() {

        //Create FormTester
        FormTester formTester = myTester.newFormTester(NavigationUtils.designerParamFormPath);
        //Fill form with default required values
        fillForm(formTester);
        // Set negative Value on msg size field
        formTester.setValue("msgMaxSizeKB","10");
        //Submit form
        myTester.executeAjaxEvent(NavigationUtils.designerParamFormPath+":addUpdateButton", "onclick");
        //Check Ok
        myTester.assertNoErrorMessage();

    }

    private void fillForm(FormTester formTester) {
        formTester.setValue("label", "Mom Service");
        formTester.setValue("destinationName", "Destination");
        formTester.setValue("hasDeadLetterQueue","false");
    }

    private void createApplicationAndFirstRelease(SampleAppFactory sample) {

        try {
            releaseUid = manageApplicationRelease.createApplicationRelease(createPublicApplication(sample), cuid, sample.getAppReleaseVersion());
        } catch (PaasUserNotFoundException e) {
            e.printStackTrace();
        } catch (ApplicationNotFoundException e) {
            e.printStackTrace();
        } catch (ObjectNotFoundException e) {
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
