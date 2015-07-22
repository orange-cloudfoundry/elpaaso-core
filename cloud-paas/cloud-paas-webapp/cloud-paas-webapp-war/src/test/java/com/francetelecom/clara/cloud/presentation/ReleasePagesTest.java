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

import com.francetelecom.clara.cloud.application.impl.ManageLogicalDeploymentImpl;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationReleaseImpl;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.presentation.applications.ApplicationsPage;
import com.francetelecom.clara.cloud.presentation.applications.SelectedAppPage;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.presentation.releases.ReleasesPage;
import com.francetelecom.clara.cloud.presentation.releases.SelectedReleasePage;
import com.francetelecom.clara.cloud.presentation.tools.DeleteConfirmationUtils;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

/**
 * ReleasePagesTest
 *
 * test application releases
 *
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class ReleasePagesTest {
    //~ final static vars
    private static final Logger logger = LoggerFactory.getLogger(ReleasePagesTest.class);

    @Mock
    private ManageApplication manageApplication;
    @Mock
    private ManageApplicationReleaseImpl manageApplicationRelease;
    @Mock
    private ManagePaasUser managePaasUser;
    @Mock
    private SplunkBean splunkBean;
    @Mock
    private HypericBean hypericBean;
    @Mock
    private ManageLogicalDeploymentImpl manageLogicalDeployment;
    @Mock
    private DelegatingDesignerServices delegatingDesignerServices;
    @Mock
    private LogicalServicesHelper logicalServicesHelper;
    @Mock
    private ManageEnvironment manageEnvironment;
    
    @Mock
	private AuthenticationManager authenticationManager;
    
	@Mock
	private ContactUsBean contactUsBean;

    private PaasWicketTester myTester;

    private String releaseUid = "myReleaseUid";

    private String releaseVersion = "G00R01";
    private String appCode = "MyAppCode";
    private String appLabel = "MyAppLabel";
    private String releaseDescription ="my initial release of my first application in the cloud" ;

    private String cuid = "testuser";
    private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

	private List<MiddlewareProfile> mockedProfilesList;

    @Before
    public void init() throws Exception {

        myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
        ((PaasTestSession)myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, getRole()));

        // prepare mocks
        prepareMocks();

        createReleaseMock();
        myTester.startPage(HomePage.class);


    }

    @Test
    public void testCreateReleaseFromAppPageWithoutClone() {

        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        // select testDesigner application
        myTester.clickLink("appsTablePanel:refreshApplication:applicationsDataTable:body:rows:1:cells:1:cell:appDetailsLink");

        // test that we are on testDesigner app page
        myTester.assertRenderedPage(SelectedAppPage.class);
        myTester.assertContains("application : "+appLabel);

        createReleaseFromUI(true, false, false);

        // No error message in the feedbackPanel pipeline
        myTester.assertNoErrorMessage();
        // One info message in the feedbackPanel pipeline
        myTester.assertInfoMessages(myTester.getLastRenderedPage().getString("portal.release.creation.successful",
                new Model<String[]>(new String[]{appLabel, releaseVersion})));
        // info message is displayed the feedbackpanel
        myTester.assertContains(myTester.getLastRenderedPage().getString("portal.release.creation.successful",
                new Model<String[]>(new String[]{appLabel, releaseVersion})));

    }

    @Test
    public void testCreateReleaseFromAppPageWithoutCloneOverridingProfile() {

        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        // select testDesigner application
        myTester.clickLink("appsTablePanel:refreshApplication:applicationsDataTable:body:rows:1:cells:1:cell:appDetailsLink");

        // test that we are on testDesigner app page
        myTester.assertContains("application : "+appLabel);

        createReleaseFromUI(true, false, true);

        // No error message in the feedbackPanel pipeline
        myTester.assertNoErrorMessage();
        // One info message in the feedbackPanel pipeline
        myTester.assertInfoMessages(myTester.getLastRenderedPage().getString("portal.release.creation.successful", new Model<String[]>(new String[]{appLabel, releaseVersion})));
        // info message is displayed the feedbackpanel
        myTester.assertContains(myTester.getLastRenderedPage().getString("portal.release.creation.successful", new Model<String[]>(new String[]{appLabel, releaseVersion})));

    }

    @Test
    public void testCreateReleaseFromReleasesListPageWithoutClone() {

        // go to application page
        myTester.startPage(ReleasesPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ReleasesPage.class);

        // test release list contains testDesigner first release
        myTester.assertContains(appLabel+" - "+releaseVersion);

        createReleaseFromUI(false, true, false);

        // No error message in the feedbackPanel pipeline
        myTester.assertNoErrorMessage();
        // One info message in the feedbackPanel pipeline
        myTester.assertInfoMessages(myTester.getLastRenderedPage().getString("portal.release.creation.successful", new Model<String[]>(new String[]{appLabel, releaseVersion})));
        // info message is displayed the feedbackpanel
        myTester.assertContains(myTester.getLastRenderedPage().getString("portal.release.creation.successful", new Model<String[]>(new String[]{appLabel, releaseVersion})));

    }

    @Test
    public void testDisplaySelectedReleaseFromSelectedApplicationPage() {

        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        // click on edit button of the first application in the designer table
        myTester.clickLink("appsTablePanel:refreshApplication:applicationsDataTable:body:rows:1:cells:1:cell:appDetailsLink");

        // test that we are on selected application page
        myTester.assertRenderedPage(SelectedAppPage.class);

        // test that selected app page breadcrumbs contains the selected application name
        myTester.assertContains("application : "+appLabel);

        // click on edit button of the first release in the releases table
        myTester.clickLink("releasesTablePanel:refreshRealese:releasesDataTable:body:rows:1:cells:1:cell:releaseLink");

        // check we are on SelectedReleasesPage
        myTester.assertRenderedPage(SelectedReleasePage.class);

        //check page contains selected release app name and release version
        myTester.assertContains("release : " + releaseVersion);

        // test that selected release textfield in selected release page are disable
        TagTester tagTester = myTester.getTagByWicketId("releaseVersion");
        String readOnlyAttribute = tagTester.getAttribute("disabled");
        assertThat(readOnlyAttribute).isNotNull();
        assertThat(tagTester.getAttribute("value")).isEqualTo(releaseVersion);

        TagTester descriptionTester = myTester.getTagByWicketId("description");
        assertThat(descriptionTester).isNotNull();
        assertThat(descriptionTester.getAttribute("disabled")).isNotNull();
        assertThat(descriptionTester.getValue()).isEqualTo(releaseDescription);

        TagTester middlewareProfileVersionTester = myTester.getTagByWicketId("middlewareProfileVersion");
        assertThat(middlewareProfileVersionTester)
                .as("no middlewareProfileVersion found")
                .isNotNull();
        assertThat(middlewareProfileVersionTester.getAttribute("disabled"))
                .as("middlewareProfileVersion should be read only")
                .isNotNull();
        myTester.dumpPage();
        assertThat(middlewareProfileVersionTester.getAttribute("value")).isEqualTo(MiddlewareProfile.getDefault().getVersion());

        // test that modify release button is visible
        myTester.assertVisible("releaseInfoPanel:releaseForm:buttonContainer:releaseModifyLink");

    }
    
    @Test
    public void shouldDisplayErrorMessageWhenBusinessException() throws BusinessException {

        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        // click on edit button of the first application in the designer table
        myTester.clickLink("appsTablePanel:refreshApplication:applicationsDataTable:body:rows:1:cells:1:cell:appDetailsLink");

        // test that we are on selected application page
        myTester.assertRenderedPage(SelectedAppPage.class);

        // test that selected app page breadcrumbs contains the selected application name
        myTester.assertContains("application : "+appLabel);

        // click on edit button of the first release in the releases table
        myTester.clickLink("releasesTablePanel:refreshRealese:releasesDataTable:body:rows:1:cells:1:cell:releaseLink");

        // check we are on SelectedReleasesPage
        myTester.assertRenderedPage(SelectedReleasePage.class);

        //click on new environment button
        myTester.executeAjaxEvent("newEnvLink", "onclick");
             
        //GIVEN there is no error message
        myTester.assertNoErrorMessage(); 
        
        //GIVEN environment to be created does not exist
        when(manageEnvironment.isEnvironmentLabelUniqueForRelease(anyString(), anyString(), anyString())).thenReturn(true);
        //GIVEN projection fail
        when(manageEnvironment.createEnvironment(anyString(), any(EnvironmentTypeEnum.class), anyString(), anyString())).thenThrow(new BusinessException("dummy message"));
        
        //WHEN I request for environment creation
        // set environment parameters
        FormTester createEnvFormTester = myTester.newFormTester("createEnvContainer:createEnvForm:envForm");
        createEnvFormTester.setValue("label", "aLabel");
        createEnvFormTester.select("type", 0);
         // click on create environment button
        myTester.executeAjaxEvent("createEnvContainer:createEnvForm:envForm:addEnvButton", "onclick");
                  
        //THEN I should get an error message containing dummy message
        myTester.assertErrorMessages("error : dummy message"); 
        
    }

    @Test
    public void testClickOnEditReleaseFromReleaseTable() {

        // go to releases page
        myTester.startPage(ReleasesPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ReleasesPage.class);

        // click on the first application link in the designer table
        myTester.clickLink("releasesTablePanel:refreshRealese:releasesDataTable:body:rows:1:cells:4:cell:release-edit");

        // test that we are on selected release page
        myTester.assertRenderedPage(SelectedReleasePage.class);

        // test that selected app page breadcrumbs contains the selected application name
        myTester.assertContains("release : " + releaseVersion);

        // test that selected application textfield in selected app page are disable
        myTester.assertEnabled("releaseInfoPanel:releaseForm:releaseVersion");

        // test that update application button is visible
        myTester.assertVisible("releaseInfoPanel:releaseForm:buttonContainer:releaseUpdateLink");

    }

    @Test
    public void testUpdateReleaseOnSelectedReleasesPage() throws Exception {
        // prepare specific mock
        ApplicationRelease updatedRelease = createReleaseMock();
        updatedRelease.setReleaseVersion(releaseVersion+"_modify");
        updatedRelease.setDescription(releaseDescription+"_modify");
        when(manageApplicationRelease.updateApplicationRelease(any(ApplicationRelease.class))).thenReturn(updatedRelease);


        // go to releases page
        myTester.startPage(ReleasesPage.class);

        // test that we are on releases page
        myTester.assertRenderedPage(ReleasesPage.class);

        // click on the first application link in the designer table
        myTester.clickLink("releasesTablePanel:refreshRealese:releasesDataTable:body:rows:1:cells:4:cell:release-edit");

        // test that we are on selected release page
        myTester.assertRenderedPage(SelectedReleasePage.class);

        // test that selected release page breadcrumbs contains the selected release version
        myTester.assertContains("release : "+releaseVersion);

        // test that selected release version textfield in selected release page are enable
        myTester.assertEnabled("releaseInfoPanel:releaseForm:releaseVersion");

        // test that modify release button is visible
        myTester.assertVisible("releaseInfoPanel:releaseForm:buttonContainer:releaseUpdateLink");

        // modify values of each release parameters
        FormTester releaseformTester = myTester.newFormTester("releaseInfoPanel:releaseForm");
        releaseformTester.setValue("releaseVersion", releaseVersion+"_modify");
        releaseformTester.setValue("description", releaseDescription+"_modify");

        // click on update release button
        myTester.executeAjaxEvent("releaseInfoPanel:releaseForm:buttonContainer:releaseUpdateLink", "onclick");


        // test no error message
        myTester.assertNoErrorMessage();

        // test release modifications display
        myTester.assertContains(releaseVersion+"_modify");
        myTester.assertContains(releaseDescription+"_modify");

        // test that modify release button is visible
        myTester.assertVisible("releaseInfoPanel:releaseForm:buttonContainer:releaseModifyLink");

        // test that selected release textfield in selected release page are disable

        Component relVersionTxt = myTester.getComponentFromLastRenderedPage("releaseInfoPanel:releaseForm:releaseVersion", true);
        assertThat(relVersionTxt).as("releaseVersion should be a TextField").isInstanceOf(TextField.class);

        TextField<String> relVersionTxtTextfield = (TextField<String>) relVersionTxt;
        assertThat(relVersionTxtTextfield.isEnabled());
        assertThat(relVersionTxtTextfield.getValue()).isEqualTo(releaseVersion+"_modify");

        Component relDescriptionTxt = myTester.getComponentFromLastRenderedPage("releaseInfoPanel:releaseForm:description", true);
        assertThat(relDescriptionTxt)
                .as("releaseDescription should be a TextArea")
                .isInstanceOf(TextArea.class);

        TextArea<String> relDescriptionTxtTextArea= (TextArea<String>) relDescriptionTxt;
        assertThat(relDescriptionTxtTextArea.isEnabled());
        assertThat(relDescriptionTxtTextArea.getValue()).isEqualTo(releaseDescription+"_modify");

        Component relMiddlewareProfileVersionTxt
             = myTester.getComponentFromLastRenderedPage("releaseInfoPanel:releaseForm:middlewareProfileVersion", true);
        assertThat(relMiddlewareProfileVersionTxt)
                .as("relMiddlewareProfileVersionTxt should be a TextField")
                .isInstanceOf(TextField.class);

        TextField<String> relMiddlewareProfileVersionTxtTextField = (TextField<String>) relMiddlewareProfileVersionTxt;
        assertThat(relMiddlewareProfileVersionTxtTextField.getValue()).isEqualTo(MiddlewareProfile.getDefault().getVersion());
    }

    @Test
    public void testDeleteRelease() {

        // go to application page
        myTester.startPage(ReleasesPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ReleasesPage.class);

        // force to click on OK when asking for application deletion
        DeleteConfirmationUtils.forceOK = true;

        // click on the first application link in the designer table
        myTester.clickLink("releasesTablePanel:refreshRealese:releasesDataTable:body:rows:1:cells:4:cell:release-delete");

        // test that we are still on applications page
        myTester.assertRenderedPage(ReleasesPage.class);

        // test no error message
        myTester.assertNoErrorMessage();

    }

    private void createReleaseFromUI(boolean appSelected, boolean mustShowSelectApp, boolean overrideProfile) {
        // display application and its first release form panel
        myTester.clickLink("newReleaseLink");

        String releaseFormPath = "createReleaseContainer:createReleaseForm:releaseForm";

        final Component releaseCreatePanel = myTester.getComponentFromLastRenderedPage(releaseFormPath);
        assertThat(releaseCreatePanel)
                .as("release creation form should be present")
                .isNotNull();

        if (mustShowSelectApp) {
            myTester.assertContains("\"application\"");
        }
        // test release fields
        myTester.assertContains("\"releaseVersion\"");
        myTester.assertContains("\"description\"");
        // myTester.assertContains("\"versionControlUrl\"");
        myTester.assertContains("\"forkCheckbox\"");
        myTester.assertContains("\"overrideProfileCheckbox\"");
        	
        // check if button with label create application is in the page
        myTester.assertContains("create release");

        FormTester formTester = myTester.newFormTester(releaseFormPath);

        // first to be done because already input form values are RAZ'ed by wicket here
        if (overrideProfile) {
            logger.info("override profile");

			// checkbox form set value
			myTester.assertComponent("overrideProfileCheckbox", CheckBox.class);
			formTester.setValue("releaseFieldsetPanel:overrideProfileCheckbox", true);
			// ajax call overrideProfileCheckboxPath, onclick
			myTester.executeAjaxEvent("overrideProfileCheckbox", "onclick");

			// select profile
			Component listboxComponent = myTester.getComponentFromLastRenderedPage("middlewareProfileSelect");
			assertNotNull(listboxComponent);
			DropDownChoice<MiddlewareProfile> listbox = (DropDownChoice<MiddlewareProfile>) listboxComponent;
			int profilIndex = listbox.getChoices().indexOf(getProfile());
			assertThat(profilIndex).as("Profile not found "+getProfile().getVersion()).isGreaterThanOrEqualTo(0);
			formTester.select("releaseFieldsetPanel:overrideProfilePanel:middlewareProfileContainer:middlewareProfileSelect", profilIndex);
        }
        // DEBUG // myTester.debugComponentTrees("application");

        if (!appSelected) {
            formTester.select("releaseFieldsetPanel:selectApplication:application", 0);
        }

        // change release version
        myTester.assertComponent("releaseVersion", TextField.class);
        // TextField tf = (TextField)myTester.getComponentFromLastRenderedPage(releaseVersionPath);
        // tf.getModel().setObject(releaseVersion);
        formTester.setValue("releaseFieldsetPanel:releaseVersion", releaseVersion);

        // change release description
        myTester.assertComponent("releaseFieldsetPanel:description", TextArea.class);
        // TextArea ta = (TextArea)myTester.getComponentFromLastRenderedPage(releaseDescriptionPath);
        // ta.getModel().setObject(releaseDescription);
        formTester.setValue("releaseFieldsetPanel:description", releaseDescription);

        myTester.executeAjaxEvent("addReleaseButton", "onclick");

        myTester.assertNoErrorMessage();
        // test we are on designer step one page
        myTester.assertRenderedPage(DesignerPage.class);
    }


    private Application createApplicationMock() {
        return new Application(appLabel, appCode);
    }

    private ApplicationRelease createReleaseMock() {

        ApplicationRelease release = new ApplicationRelease(createApplicationMock(),releaseVersion);
        release.setDescription(releaseDescription);

        return release;

    }

    /**
     * Create an applicationContextMock to inject in Spring for Wicket
     * @return applicationContextMock
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


    private void prepareMocks() throws Exception {

        // Prepare mocks for app provider
        List<Application> appList = new ArrayList<Application>();
        appList.add(createApplicationMock());
        when(manageApplication.findMyApplications()).thenReturn(appList);
        when(manageApplication.countApplications()).thenReturn(Long.valueOf("1"));
        when(manageApplication.countMyApplications()).thenReturn(Long.valueOf("1"));
        when(manageApplication.findApplicationByUID(anyString())).thenReturn(createApplicationMock());
        when(manageApplication.findApplications()).thenReturn(appList);
        when(manageApplication.findMyApplications()).thenReturn(appList);

        mockedProfilesList = new ArrayList<MiddlewareProfile>();
        mockedProfilesList.addAll(Arrays.asList(MiddlewareProfile.values()));

        // Prepare mocks for release provider
        List<ApplicationRelease> releaseList = new ArrayList<ApplicationRelease>();
        releaseList.add(createReleaseMock());
        when(manageApplicationRelease.findApplicationReleasesByAppUID(anyString(), anyInt(), anyInt())).thenReturn(releaseList);
        when(manageApplicationRelease.findApplicationReleaseByUID(anyString())).thenReturn(createReleaseMock());
        when(manageApplicationRelease.findMyApplicationReleases(anyInt(), anyInt())).thenReturn(releaseList);
        when(manageApplicationRelease.findApplicationReleases(anyInt(), anyInt())).thenReturn(releaseList);
        when(manageApplicationRelease.countApplicationReleases()).thenReturn(Long.valueOf("1"));
        when(manageApplicationRelease.countApplicationReleasesByAppUID(anyString())).thenReturn(Long.valueOf("1"));
        when(manageApplicationRelease.countMyApplicationReleases()).thenReturn(Long.valueOf("1"));
        when(manageApplicationRelease.findAllMiddlewareProfil()).thenReturn(mockedProfilesList);


        // Prepare mocks to create release
        when(manageApplicationRelease.isReleaseVersionUniqueForApplication((anyString()), anyString())).thenReturn(true);
        when(manageApplicationRelease.createApplicationRelease(anyString(), anyString(), anyString())).thenReturn(releaseUid);
        when(manageApplicationRelease.findApplicationReleaseByUID(anyString())).thenReturn(createReleaseMock());
        doCallRealMethod().when(manageApplicationRelease).updateApplicationRelease(eq(createReleaseMock()));
        doCallRealMethod().when(manageLogicalDeployment).cloneLogicalDeployment(eq("myReleaseToCloneUid"), eq(releaseUid));

        // Prepare mocks to display designer page
        when(manageLogicalDeployment.findLogicalDeployment(anyInt())).thenReturn(new LogicalDeployment());
        when(delegatingDesignerServices.createPanelFor(anyString(), any(LogicalModelItem.class), any(DesignerHelperPage.class), anyBoolean(), anyBoolean(), anyBoolean())).thenReturn(new EmptyPanel("logicalServicePanel"));

        // Prepare mocks for updated app
        when(manageApplicationRelease.updateApplicationRelease(any(ApplicationRelease.class))).thenReturn(createReleaseMock());

        // mocks to be able to delete release
        when(manageApplicationRelease.canBeDeleted(anyString())).thenReturn(true);
        doCallRealMethod().when(manageApplicationRelease).deleteApplicationRelease(eq(releaseUid));

    }

	public PaasWicketTester getMyTester() {
		return myTester;
	}

	public PaasRoleEnum getRole() {
		return role;
	}
	
	public MiddlewareProfile getProfile(){
		return MiddlewareProfile.getDefault();
	}

}
