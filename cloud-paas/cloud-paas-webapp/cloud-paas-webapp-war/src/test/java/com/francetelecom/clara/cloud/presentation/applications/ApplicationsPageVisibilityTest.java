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

import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationsPageVisibilityTest extends ApplicationsPagesBaseTest {
	private PaasWicketTester myTester;
	private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

	@Before
	public void init() throws Exception {
		// init wicket tester
		myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
		String cuid = "testuser";
		((PaasTestSession) myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
	}

	@Test
	public void public_application_should_be_listed_as_public() throws Exception {
		// GIVEN
		// Prepare mocks for app provider
		prepareMocksForPublicApp();

		// WHEN : go to the applications list
		myTester.startPage(ApplicationsPage.class);

		// THEN : the application has visibility attribute set to public
		myTester.assertContains(ApplicationVisibilityPanel.APP_VISIBILITY_PUBLIC);
	}

	@Test
	public void private_application_should_be_listed_as_private() throws Exception {
		// GIVEN
		// Prepare mocks for app provider
		prepareMocksForPrivateApp();

		// WHEN : go to the applications list
		myTester.startPage(ApplicationsPage.class);

		// click on "all applications" checkbox to see the app
		AjaxCheckBox chkbx = (AjaxCheckBox) myTester.getComponentFromLastRenderedPage("allAppsCheckbox");
		myTester.getRequest().getPostParameters().setParameterValue(chkbx.getInputName(), "true");
		myTester.executeAjaxEvent("allAppsCheckbox", "click");

		// THEN : the application has visibility attribute set to private
		myTester.assertContains(ApplicationVisibilityPanel.APP_VISIBILITY_PRIVATE);
	}

	@Test
	public void public_application_should_be_detailled_as_public() throws Exception {
		// GIVEN
		// Prepare mocks for app provider
		prepareMocksForPublicApp();

		// WHEN : go to the application details
		myTester.startPage(SelectedAppPage.class);

		// THEN : the application has visibility attribute set to public
		myTester.assertModelValue("appForm:isPublic", Boolean.TRUE);
	}

	@Test
	public void private_application_should_be_detailled_as_private() throws Exception {
		// GIVEN
		// Prepare mocks for app provider
		prepareMocksForPrivateApp();

		// WHEN : go to the application details
		myTester.startPage(SelectedAppPage.class);

		// THEN : the application has visibility attribute set to private
		myTester.assertModelValue("appForm:isPublic", Boolean.FALSE);
	}
	
	@Test
	public void private_application_should_not_be_editable() throws Exception {
		// GIVEN
		// Prepare mocks for app provider
		prepareMocksForPrivateApp();

		// WHEN : go to the application details
		myTester.startPage(SelectedAppPage.class);

		// THEN : the application has visibility attribute set to private
		myTester.assertInvisible("appInfoPanel:appForm:buttonContainer:appModifyLink");
	}

}
