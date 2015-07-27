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
package com.francetelecom.clara.cloud.presentation.tools;

import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException.ErrorType;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.core.service.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelNotConsistentException;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;
import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AuthenticationManager;

@RunWith(MockitoJUnitRunner.class)
public class BusinessExceptionHandlerTest {
    @Mock
    private ManagePaasUser managePaasUser;
    
    @Mock
	private AuthenticationManager authenticationManager;

	BusinessExceptionHandler sut;
    private PaasWicketTester tester;
	Component testComponent;

    private String cuid = "testuser";
    private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

    @Before
	public void setup() {
		// Create an application without spring init
		PaasTestApplication app = new PaasTestApplication() {
			@Override
			public void init() {
			}
        };

        tester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
        ((PaasTestSession)tester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));

		// Dummy Component used for test
		testComponent = new Component("test") {

                        @Override
                        protected void onRender() {
                        }
		};
		
		// Start component
		tester.startComponentInPage(testComponent);
		
		// Configure our SUT 
		sut = new BusinessExceptionHandler(testComponent);
	}
	
	@Test
	public void testApplicationNotFoundException() {
		// Test data
		ApplicationNotFoundException e = new ApplicationNotFoundException("specific error");
		
		// Exercise SUT
		sut.error(e);
		
		// Assertions
		String[] expectedErrorMessages = new String[1];
		expectedErrorMessages[0] = testComponent.getString("portal.application.notfound");
				
		tester.assertErrorMessages(expectedErrorMessages);
	}
	
	@Test
	public void testInvalidMavenReferenceException() {
		// Test data
		MavenReference mr = new MavenReference("group","artifact","version","type","classifier");
		InvalidMavenReferenceException e = new InvalidMavenReferenceException(mr,ErrorType.ARTIFACT_NOT_FOUND);
		
		// Exercise SUT
		sut.error(e);
		
		// Assertions
		String[] expectedErrorMessages = new String[1];
		expectedErrorMessages[0] = testComponent.getString("portal.logicaldeployment.artifactnotfound", toMessageParameters(mr.toGavString()));
				
		tester.assertErrorMessages(expectedErrorMessages);
	}
	
	@Test
	public void testLogicalModelConsistencyException() {
		// Test Data : a LogicalModelException wrapping 2 InvalidMavenReferenceException + 1 InvalidConfigServiceException
		MavenReference mr1 = new MavenReference("group","artifact-1","version","type","classifier");
		InvalidMavenReferenceException e1 = new InvalidMavenReferenceException(mr1,ErrorType.ARTIFACT_NOT_FOUND);

		MavenReference mr2 = new MavenReference("group","artifact-2","version","type","classifier");
		InvalidMavenReferenceException e2 = new InvalidMavenReferenceException(mr2,ErrorType.ARTIFACT_NOT_FOUND);
		
		InvalidConfigServiceException e3 = new InvalidConfigServiceException();
		e3.setType(InvalidConfigServiceException.ErrorType.DUPLICATE_KEYS);
		e3.getDuplicateKeys().add("key1");
		e3.setImpactedElementName("jee1");

		LogicalModelNotConsistentException e = new LogicalModelNotConsistentException();
		e.addError(e1);
		e.addError(e2);
		e.addError(e3);
		
		// Exercise SUT
		sut.error(e);
		
		// Assertions
		String[] expectedErrorMessages = new String[3];
		expectedErrorMessages[0] = testComponent.getString("portal.logicaldeployment.artifactnotfound", toMessageParameters(mr1.toGavString()));
		expectedErrorMessages[1] = testComponent.getString("portal.logicaldeployment.artifactnotfound", toMessageParameters(mr2.toGavString()));
		expectedErrorMessages[2] = testComponent.getString("portal.designer.config.summary.error.duplicateKeys.jee", toMessageParameters(e3.getImpactedElementName(),e3.getDuplicateKeys().toString()));
		
		tester.assertErrorMessages(expectedErrorMessages);
		
		// This could be used to have a test more tolerant:
		// Assert.assertEquals(3, tester.getMessages(FeedbackMessage.ERROR).size());

	}

	private IModel<String[]> toMessageParameters(String... params) {
		return new Model<String[]>(params);
	}

    @After
    public void cleanFeedbacks() {
        tester.cleanupFeedbackMessages();
    }

    /**
     * Create an applicationContextMock to inject in Spring for Wicket
     * @return
     */
    private ApplicationContextMock getApplicationContextMock() {
        ApplicationContextMock applicationContextMock = new ApplicationContextMock();

        applicationContextMock.putBean(managePaasUser);
        applicationContextMock.putBean("authenticationManager",authenticationManager);

        return applicationContextMock;
    }
}
