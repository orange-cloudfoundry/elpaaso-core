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

import com.francetelecom.clara.cloud.core.service.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.presentation.ObjectNotFoundExceptionPage;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationPagesErrorCasesTest extends ApplicationsPagesBaseTest {
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
    public void display_application_details_of_an_unknown_application_should_produce_an_error_report() throws ApplicationNotFoundException {
        // GIVEN
        doThrow(new ApplicationNotFoundException("application not found"))
                .when(manageApplication).findApplicationByUID(anyString());

        // WHEN  : go to the application details
        myTester.startPage(SelectedAppPage.class);

        // THEN
        myTester.assertRenderedPage(ObjectNotFoundExceptionPage.class);
        myTester.assertContains("application not found");
    }
}
