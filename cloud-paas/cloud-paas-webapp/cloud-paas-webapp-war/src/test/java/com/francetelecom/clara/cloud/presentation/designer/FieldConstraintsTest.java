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
import java.util.List;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
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

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
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
import com.francetelecom.clara.cloud.presentation.utils.GetObjectsUtils;
import com.francetelecom.clara.cloud.presentation.utils.NavigationUtils;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.PaasUserNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 01/02/12
 * Time: 16:12
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config/designer-context.xml")
public class FieldConstraintsTest {

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

    	//admin is authenticated
    	AuthenticationUtil.connectAsAdmin();
    	
        myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
        ((PaasTestSession)myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
        managePaasUser.checkBeforeCreatePaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
        myTester.startPage(HomePage.class);


        createApplicationAndFirstRelease(allServicesLogicalSample);

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

    }


    @Test
    public void should_be_able_to_create_logical_services() {
        // go on designer
        NavigationUtils.goOnDesignerPage(myTester, releaseUid);

        CreateObjectsWithGUI.createWebGUIService(myTester, "myWebGui", "/root", true, true, 1, 1);
    }



    @Test
    public void testLabelUnicityForJEEProcessing() {

        // populate logical deployment of created release
        createAllServicesLD();
        // go on designer step two panel to be able to create jee procesing (external service)
        NavigationUtils.goOnDesignerPage(myTester, releaseUid);
        NavigationUtils.goOnNextStep(myTester);

        myTester.assertContains("jee processing");

        // When: I try to define a JEE processing service with the same label as the existing "frontEnd"
        CreateObjectsWithGUI.createJEEProcessing(myTester, "AllServicesJeeProcessing1", "com", "ear", "0.1", "", false, 128);
        // Then: I see an error message indicating a service with the same name is already defined
        myTester.assertErrorMessages(new String[]{"service AllServicesJeeProcessing1 already exists. please modify service label."});

    }

    @Test
    public void testLabelUnicityForService() {

        // populate logical deployment of created release
        createAllServicesLD();
        // go on designer
        NavigationUtils.goOnDesignerPage(myTester, releaseUid);

        CreateObjectsWithGUI.createWebGUIService(myTester, "AllServicesWebUi", "/root", true, true, 1, 1);
        // Then: I see an error message indicating a service with the same name is already defined
        myTester.assertErrorMessages(new String[]{"service AllServicesWebUi already exists. please modify service label."});

    }

    @Test
    public void testPreviewFields() {

        // go on designer step one page to access internal services
        NavigationUtils.goOnDesignerPage(myTester, releaseUid);

        List<LogicalModelItem> externalItems = logicalServicesHelper.getExternalServices();

        for (LogicalModelItem item : externalItems) {
            // When: I ask add to add an external service
            final Class<? extends LogicalModelItem> serviceClass = item.getClass();
            if (!serviceClass.getAnnotation(GuiClassMapping.class).status().equals(GuiClassMapping.StatusType.PREVIEW)) {

                CreateObjectsWithGUI.selectService(myTester, serviceClass);

                // Then: Preview fields have a default value
                // I'm prevented to change the default value for these fields
                Form form = GetObjectsUtils.getParamsForm(myTester);
                    form.visitFormComponents(new IVisitor<FormComponent, Void>() {
                        @Override
                        public void component(FormComponent formComponent, IVisit<Void> visit) {
                        if (!(formComponent instanceof Button)) {
                            String name = ((FormComponent)formComponent).getInputName();
                            // We don't test complex fields, such as maven reference parameters
                            if (!name.contains(".") && !name.equals("fullvalidationContent:fullvalidation")) {
                                try {
                                    GuiMapping annotation = GetObjectsUtils.getAnyField(serviceClass, name).getAnnotation(GuiMapping.class);
                                    if (annotation.status().equals(GuiMapping.StatusType.READ_ONLY)) {
                                        TagTester tagTester = myTester.getTagByWicketId(((FormComponent) formComponent).getId());
                                        if (tagTester != null) {
                                            Assert.assertNotNull(tagTester.getAttribute("disabled"));
                                        }
//                                        Assert.assertFalse("field " + name + " should be disabled", ((FormComponent) formComponent).isEnabled());
                                        Assert.assertTrue("field " + name + " should be have a default value", ((FormComponent) formComponent).getDefaultModelObject() != null);
                                    }
                                } catch (NoSuchFieldException e) {
                                    Assert.fail("field " + name + " not found for service " + serviceClass);
                                }
                            }
                        }
                        visit.dontGoDeeper();
                    }
                });
            }
        }

        NavigationUtils.goOnNextStep(myTester);

        List<LogicalModelItem> internalItems = logicalServicesHelper.getInternalServices();

        for (LogicalModelItem item : internalItems) {
            // When: I ask add to add an external service
            final Class<? extends LogicalModelItem> serviceClass = item.getClass();
            if (!serviceClass.getAnnotation(GuiClassMapping.class).status().equals(GuiClassMapping.StatusType.PREVIEW)) {

                CreateObjectsWithGUI.selectService(myTester, serviceClass);

                // Then: Preview fields have a default value
                // I'm prevented to change the default value for these fields
                Form form = GetObjectsUtils.getParamsForm(myTester);
                form.visitFormComponents(new IVisitor<FormComponent, Void>() {
                    @Override
                    public void component(FormComponent formComponent, IVisit<Void> visit) {
                        if (!(formComponent instanceof Button)) {
                            String name = ((FormComponent)formComponent).getInputName();
                            // We don't test complex fields, such as maven reference parameters
                            if (!name.contains(".") && !name.equals("fullvalidationContent:fullvalidation")) {
                                try {
                                    GuiMapping annotation = GetObjectsUtils.getAnyField(serviceClass, name).getAnnotation(GuiMapping.class);
                                    if (annotation.status().equals(GuiMapping.StatusType.READ_ONLY)) {
                                        TagTester tagTester = myTester.getTagByWicketId(((FormComponent) formComponent).getId());

                                        if (tagTester != null) {
                                            Assert.assertNotNull(tagTester.getAttribute("disabled"));
                                        }
                                        Assert.assertTrue("field " + name + " should be have a default value", ((FormComponent) formComponent).getDefaultModelObject() != null);
                                    }
                                } catch (NoSuchFieldException e) {
                                    Assert.fail("field " + name + " not found for service " + serviceClass);
                                }
                            }
                        }
                        visit.dontGoDeeper();
                    }
                });
            }
        }
    }

    @Test
    public void testMissingFieldsPreventSaving() {

        // go on designer step one page to access internal services
        NavigationUtils.goOnDesignerPage(myTester, releaseUid);

        // When: I ask to add an external service "webGui" without filling required fields "name" and "contextRoot"
        CreateObjectsWithGUI.createWebGUIService(myTester, "", "", true, true, 1, 1);
        // Then: I'm prevented to add the service  Z
        // missing fields are indicated with an error message
        myTester.assertVisible(NavigationUtils.designerParamFormPath);
        Assert.assertTrue(GetObjectsUtils.getArchitecturePanel(myTester).getLogicalDeployment().listLogicalServices().isEmpty());
        // TODO : Wait for update about i18n for JSR303WicketValidation before reactivate
//        myTester.assertErrorMessages(new String[]{"Field 'label' is required.", "contextRoot may be not null"});
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
