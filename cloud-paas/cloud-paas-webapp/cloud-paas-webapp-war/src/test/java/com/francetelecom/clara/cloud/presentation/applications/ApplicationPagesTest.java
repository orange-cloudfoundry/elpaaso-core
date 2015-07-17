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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.wicket.util.tester.FormTester;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.presentation.tools.DeleteConfirmationUtils;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;

/**
 * ApplicationPagesTest
 *
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class ApplicationPagesTest extends ApplicationsPagesBaseTest {

    public static final String APPS_TABLE_PANEL_ACTION_CELL = "appsTablePanel:refreshApplication:applicationsDataTable:body:rows:1:cells:5";
    public static final String APPS_TABLE_PANEL_ENV_NAME_CELL = "appsTablePanel:refreshApplication:applicationsDataTable:body:rows:1:cells:1";
    private PaasWicketTester myTester;
    private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

    @Before
    public void init() throws Exception {
        // init wicket tester
        myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
        String cuid = "testuser";
        ((PaasTestSession)myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
    }

    @Test
    public void select_a_public_application_should_display_application_details() throws Exception {
        prepareMocksForPublicApp();
        selectAnApplicationAndCheckContent(true);
    }

    @Test
    public void select_a_private_application_should_display_application_details() throws Exception {
        prepareMocksForPrivateApp();
        selectAnApplicationAndCheckContent(false);
    }

    private void selectAnApplicationAndCheckContent(boolean isPublic) {
        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);
        
        // click on details button of the first application in the designer table
        myTester.clickLink(APPS_TABLE_PANEL_ENV_NAME_CELL + ":cell:appDetailsLink");

        // test that we are on selected application page
        myTester.assertRenderedPage(SelectedAppPage.class);

        // test that selected app page breadcrumbs contains the selected application name
        myTester.assertContains("application : "+ publicApp.getLabel());

        // test that selected application textfield in selected app page are readonly
        myTester.assertDisabled("appInfoPanel:appForm:label");
        myTester.assertDisabled("appInfoPanel:appForm:code");
        myTester.assertDisabled("appInfoPanel:appForm:description");
        myTester.assertDisabled("appInfoPanel:appForm:isPublic");
        if (isPublic) {
            myTester.assertVisible("appInfoPanel:appForm:buttonContainer:appModifyLink");
        } else {
            myTester.assertDisabled("appInfoPanel:appForm:members");
        	myTester.assertInvisible("appInfoPanel:appForm:buttonContainer:appModifyLink");
        }
    }

    @Test
    public void update_public_application_shoud_render_application_update_gui() throws Exception {
        prepareMocksForPublicApp();
        launchApplicationEditionAndCheckContent(true);
    }

    private void launchApplicationEditionAndCheckContent(boolean isPublic) {

        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        // click on the first application link in the designer table
        myTester.clickLink(APPS_TABLE_PANEL_ACTION_CELL +":cell:app-edit");

        // test that we are on selected application page
        myTester.assertRenderedPage(SelectedAppPage.class);

        // test that selected app page breadcrumbs contains the selected application name
        myTester.assertContains("application : " + publicApp.getLabel());

        // test that selected application textfield in selected app page are disable
        myTester.assertEnabled("appInfoPanel:appForm:label");
        myTester.assertEnabled("appInfoPanel:appForm:code");
        myTester.assertEnabled("appInfoPanel:appForm:description");
        myTester.assertEnabled("appInfoPanel:appForm:isPublic");

        // test that update application button is visible
        myTester.assertVisible("appInfoPanel:appForm:buttonContainer:appUpdateLink");
    }

    @Test
    public void update_application_should_update_application_details() throws Exception {
        prepareMocksForPublicApp();
        prepareMocksForAppUpdate();

        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        // click on the first application link in the designer table
        myTester.clickLink(APPS_TABLE_PANEL_ACTION_CELL +":cell:app-edit");

        // test that we are on selected application page
        myTester.assertRenderedPage(SelectedAppPage.class);

        // test that selected app page breadcrumbs contains the selected application name
        myTester.assertContains("application : " + publicApp.getLabel());

        // test that selected application textfield in selected app page are disable
        myTester.assertEnabled("appInfoPanel:appForm:label");

        // test that update application button is visible
        myTester.assertVisible("appInfoPanel:appForm:buttonContainer:appUpdateLink");

        // modify values of each application parameters
        FormTester appformTester = myTester.newFormTester("appInfoPanel:appForm");

        appformTester.select("isPublic", 1); // set private

        appformTester.setValue("label", publicApp.getLabel() + "_modify");
        appformTester.setValue("code", publicApp.getCode() + "_modify");
        appformTester.setValue("description", publicApp.getDescription() + "_modify");

        appformTester.setValue("members", "homer marge lisa bart");

        // click on update application button
        myTester.executeAjaxEvent("appInfoPanel:appForm:buttonContainer:appUpdateLink", "onclick");
        
        ArgumentCaptor<Application> argument = ArgumentCaptor.forClass(Application.class);
        Mockito.verify(manageApplication).updateApplication(argument.capture());
        assertApplicationEquals(updatedApp, argument.getValue());

        // test no error message
        myTester.assertNoErrorMessage();

        // test application modifications display
        myTester.assertContains(publicApp.getLabel() + "_modify");
        myTester.assertContains(publicApp.getCode() + "_modify");
        myTester.assertContains(publicApp.getDescription() + "_modify");

        // test that modify application button is visible
        myTester.assertVisible("appInfoPanel:appForm:buttonContainer:appModifyLink");
    }

    @Test
    public void update_private_application_should_not_be_possible() throws Exception {
        prepareMocksForPrivateApp();
        prepareMocksForAppUpdate();

        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        // the application is not visible by default
        try {
            myTester.assertVisible(APPS_TABLE_PANEL_ENV_NAME_CELL + ":cell:app-edit");
            fail("The edit link should not be accesible as the private app should not be listed.");
        } catch (AssertionError exc) {
            Assertions.assertThat(exc.getMessage()).contains("path: '" + APPS_TABLE_PANEL_ENV_NAME_CELL + ":cell:app-edit' not found");
        }

        // Edition of the app is now present but locked
        myTester.assertVisible(APPS_TABLE_PANEL_ACTION_CELL +":cell:app-edit-disable");
    }


    private static void assertApplicationEquals(Application expectedApp, Application actualApp) {
        assertEquals(expectedApp.getLabel(), actualApp.getLabel());
        assertEquals(expectedApp.getCode(), actualApp.getCode());
        assertEquals(expectedApp.getDescription(), actualApp.getDescription());
        assertEquals(expectedApp.isPublic(), actualApp.isPublic());
        assertEquals(expectedApp.listMembers(), actualApp.listMembers());
    }

	@Test
    public void delete_an_application_should_work() throws Exception {
        prepareMocksForPublicApp();
        prepareMocksForAppDeletion();

        // go to application page
        myTester.startPage(ApplicationsPage.class);

        // test that we are on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        // force to click on OK when asking for application deletion
        DeleteConfirmationUtils.forceOK = true;

        // click on the first application link in the designer table
        myTester.clickLink(APPS_TABLE_PANEL_ACTION_CELL + ":cell:app-delete", true);

        // test that we are still on applications page
        myTester.assertRenderedPage(ApplicationsPage.class);

        // test no error message
        myTester.assertNoErrorMessage();
    }

	@Test
	public void members_must_be_split_with_spaces_when_building_array() {
		SSOId[] expectedSsoids = new SSOId[] { new SSOId("bart"), new SSOId("homer") };
		assertTrue(Arrays.equals(expectedSsoids, ApplicationsPage.toSSOIdsArray("bart homer")));
		assertTrue(Arrays.equals(expectedSsoids, ApplicationsPage.toSSOIdsArray("bart	homer")));
		assertTrue(Arrays.equals(expectedSsoids, ApplicationsPage.toSSOIdsArray(" bart   homer  ")));
		assertTrue(Arrays.equals(expectedSsoids, ApplicationsPage.toSSOIdsArray("		bart homer	")));

		// The followings should fail because of badly formed SSOId
		try {
			ApplicationsPage.toSSOIdsArray("bart,homer");
			fail();
		} catch (IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("bart,homer"));
		}
		try {
			ApplicationsPage.toSSOIdsArray("bart, homer");
			fail();
		} catch (IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("bart,"));
		}
		try {
			ApplicationsPage.toSSOIdsArray("bart-homer");
			fail();
		} catch (IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("bart-homer"));
		}
		try {
			ApplicationsPage.toSSOIdsArray("bart | homer");
			fail();
		} catch (IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("|"));
		}
	}
	
	@Test
	public void members_must_be_split_with_spaces_when_building_set() {
		Set<SSOId> expectedSsoids = new HashSet<>(Arrays.asList(new SSOId("bart"), new SSOId("homer")));
		assertEquals(expectedSsoids, ApplicationsPage.toSSOIdsSet("bart homer"));
		assertEquals(expectedSsoids, ApplicationsPage.toSSOIdsSet("bart	homer"));
		assertEquals(expectedSsoids, ApplicationsPage.toSSOIdsSet(" bart   homer  "));
		assertEquals(expectedSsoids, ApplicationsPage.toSSOIdsSet("		bart homer	"));
		
		// The followings should fail because of badly formed SSOId
		try {
			ApplicationsPage.toSSOIdsSet("bart,homer");
			fail();
		} catch (IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("bart,homer"));
		}
		try {
			ApplicationsPage.toSSOIdsSet("bart, homer");
			fail();
		} catch (IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("bart,"));
		}
		try {
			ApplicationsPage.toSSOIdsSet("bart-homer");
			fail();
		} catch (IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("bart-homer"));
		}
		try {
			ApplicationsPage.toSSOIdsSet("bart | homer");
			fail();
		} catch (IllegalArgumentException iae) {
			assertTrue(iae.getMessage().contains("|"));
		}
	}
}
