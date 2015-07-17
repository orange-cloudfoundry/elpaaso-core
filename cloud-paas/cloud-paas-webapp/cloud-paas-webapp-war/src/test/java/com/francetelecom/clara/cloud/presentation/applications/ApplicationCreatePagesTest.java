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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.models.FirstApplicationReleaseInfos;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationCreatePagesTest extends ApplicationsPagesBaseTest {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationCreatePagesTest.class);

    private String cuid = "testuser";
    private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;
    private PaasWicketTester myTester;

    @Before
    public void init() throws Exception {
        // init wicket tester
        myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
        ((PaasTestSession)myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));

        // prepare mocks
        prepareMocksForAppCreation();

        // Go to home page
        myTester.startPage(HomePage.class);
    }

    /**
     * Creates the given application and release using the user interface.
     */
    private void createApplicationFromUI(Application application, ApplicationRelease release, MiddlewareProfile selectedProfile) {
        // display application and its first release form panel
        myTester.clickLink("buttonContainer:newAppLink", true);

        String appCreateFormPath = "createAppContainer:appCreateForm:appForm";
        String releaseFieldsetPanel =  appCreateFormPath + ":releaseFieldsetPanel";

        final Component appCreateForm = myTester.getComponentFromLastRenderedPage(appCreateFormPath);
        assertThat(appCreateForm )
                .as("application creation form should be present")
                .isNotNull();

        // test application fields
        myTester.assertContains("\"appLabel\"");
        myTester.assertContains("\"releaseVersion\"");
        myTester.assertContains("\"description\"");
        myTester.assertContains("\"appVisibilityRadioGroup-public\"");
        myTester.assertContains("\"appVisibilityRadioGroup-private\"");
        // myTester.assertContains("\"versionControlUrl\"");
        if (WicketSession.get().getRoles().hasRole("ROLE_AMAZON")) {
            myTester.assertContains("\"overrideProfileCheckbox\"");
        }
        myTester.assertContains("\"forkCheckbox\"");

        // check if button with label create application is in the page
        myTester.assertContains("create application");

        FormTester formTester = myTester.newFormTester(appCreateFormPath);
        
        String appPublicPath = appCreateFormPath + ":appPublic";
        myTester.assertComponent(appPublicPath, RadioGroup.class);
        
        // App is private by default in IHM
        myTester.assertModelValue(appPublicPath, Boolean.FALSE);

        // first to be done because already input form values are RAZ'ed by wicket here
        if (selectedProfile != null) {
            logger.info("override profile");
            String overrideProfileCheckboxPath = releaseFieldsetPanel + ":overrideProfileCheckbox";

            // checkbox form set value
            myTester.assertComponent(overrideProfileCheckboxPath, CheckBox.class);
            formTester.setValue("releaseFieldsetPanel:overrideProfileCheckbox", true);
            // ajax call overrideProfileCheckboxPath, onclick
            myTester.executeAjaxEvent(overrideProfileCheckboxPath, "onclick");

            Component listboxComponent = myTester.getComponentFromLastRenderedPage("middlewareProfileSelect");
            assertNotNull(listboxComponent);
            DropDownChoice<MiddlewareProfile> listbox = (DropDownChoice<MiddlewareProfile>) listboxComponent;
            int profilIndex = listbox.getChoices().indexOf(selectedProfile);
            assertThat(profilIndex).as("Profile not found "+selectedProfile.getVersion()).isGreaterThanOrEqualTo(0);
            formTester.select("releaseFieldsetPanel:overrideProfilePanel:middlewareProfileContainer:middlewareProfileSelect", profilIndex);
        }

        // Select in form what is required for visibility
        if (application.isPublic()) {
            formTester.select("appPublic", 0);
        } else {
            formTester.select("appPublic", 1);
        }
        String members = "";
        for (SSOId member : application.listMembers()) {
            members += member.getValue();
            members += " ";
        }
        myTester.assertComponent("members", TextField.class);
        formTester.setValue("members", members);

        String appLabelPath = appCreateFormPath + ":appLabel";
        myTester.assertComponent(appLabelPath, TextField.class);
        formTester.setValue("appLabel", application.getLabel());

        String appCodePath = appCreateFormPath + ":appCode";
        myTester.assertComponent(appCodePath , TextField.class);
        formTester.setValue("appCode", application.getCode());

        String appDescriptionPath = appCreateFormPath + ":appDescription";
        myTester.assertComponent(appDescriptionPath, TextArea.class);
        formTester.setValue("appDescription", application.getDescription());

        String releaseVersionPath = releaseFieldsetPanel + ":releaseVersion";
        myTester.assertComponent(releaseVersionPath, TextField.class);
        formTester.setValue("releaseFieldsetPanel:releaseVersion", release.getReleaseVersion());

        String releaseDescriptionPath = releaseFieldsetPanel + ":description";
        myTester.assertComponent(releaseDescriptionPath, TextArea.class);
        formTester.setValue("releaseFieldsetPanel:description", release.getDescription());

        // DEBUG PURPOSE
        // myTester.debugComponentTrees("appCreateForm");
        // myTester.dumpPage();

        myTester.executeAjaxEvent("createAppContainer:appCreateForm:appForm:addAppButton", "onclick");

        // Assert that the model was correctly filled
        FirstApplicationReleaseInfos model = (FirstApplicationReleaseInfos) formTester.getForm().getModelObject();
        Assert.assertEquals(application.getLabel(), model.getAppLabel());
        Assert.assertEquals(application.getCode(), model.getAppCode());
        Assert.assertEquals(application.getDescription(), model.getAppDescription());
        Assert.assertEquals(application.isPublic(), model.getAppPublic().booleanValue());
        Assert.assertNotNull(model.getMembers());
        Assert.assertEquals(release.getReleaseVersion(), model.getReleaseVersion());
        Assert.assertEquals(release.getDescription(), model.getDescription());

        myTester.assertNoErrorMessage();
        // test we are on designer step one page
        myTester.assertRenderedPage(DesignerPage.class);
    }

    @Test
    public void create_an_application_without_clone_should_success() throws Exception {
        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        createApplicationFromUI(publicApp, mockedRelease, null);

        // No error message in the feedbackPanel pipeline
        myTester.assertNoErrorMessage();
        // One info message in the feedbackPanel pipeline
		myTester.assertInfoMessages(myTester.getLastRenderedPage().getString("portal.application.creation.successful",
		        new Model<String[]>(new String[] { publicApp.getLabel(), mockedRelease.getReleaseVersion() })));
		// info message is displayed the feedbackpanel
		myTester.assertContains(myTester.getLastRenderedPage().getString("portal.application.creation.successful",
		        new Model<String[]>(new String[] { publicApp.getLabel(), mockedRelease.getReleaseVersion() })));
    }


    @Test
    public void create_an_application_without_clone_overriding_profile_should_success() throws Exception {
        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        //Override with default profile
        createApplicationFromUI(publicApp, mockedRelease, MiddlewareProfile.getDefault());

        // No error message in the feedbackPanel pipeline
        myTester.assertNoErrorMessage();
        // One info message in the feedbackPanel pipeline
		myTester.assertInfoMessages(myTester.getLastRenderedPage().getString("portal.application.creation.successful",
		        new Model<String[]>(new String[] { publicApp.getLabel(), mockedRelease.getReleaseVersion() })));
		// info message is displayed the feedbackpanel
		myTester.assertContains(myTester.getLastRenderedPage().getString("portal.application.creation.successful",
		        new Model<String[]>(new String[] { publicApp.getLabel(), mockedRelease.getReleaseVersion() })));
    }

    @Test
    public void create_a_public_application_should_success() throws Exception {
        myTester.startPage(ApplicationsPage.class);
        myTester.assertRenderedPage(ApplicationsPage.class);
        createApplicationFromUI(publicApp, mockedRelease, null);
        myTester.assertNoErrorMessage();
        verify(manageApplication).createPublicApplication(publicApp.getCode(), publicApp.getLabel(), publicApp.getDescription(), null, publicApp.listMembers().toArray(new SSOId[publicApp.listMembers().size()]));
    }

    @Test
    public void create_a_private_application_should_success() throws Exception {
        myTester.startPage(ApplicationsPage.class);
        myTester.assertRenderedPage(ApplicationsPage.class);
        createApplicationFromUI(privateApp, mockedRelease, null);
        myTester.assertNoErrorMessage();
        verify(manageApplication).createPrivateApplication(privateApp.getCode(), privateApp.getLabel(), privateApp.getDescription(), null, privateApp.listMembers().toArray(new SSOId[privateApp.listMembers().size()]));
    }
}
