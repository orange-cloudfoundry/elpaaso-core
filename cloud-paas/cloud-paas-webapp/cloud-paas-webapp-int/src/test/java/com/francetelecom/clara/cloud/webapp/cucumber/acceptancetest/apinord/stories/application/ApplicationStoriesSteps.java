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
package com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.apinord.stories.application;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import com.orange.clara.cloud.consumersoap.administration.model.ApplicationModel;
import com.orange.clara.cloud.consumersoap.administration.model.CreateApplicationCommand;
import com.orange.clara.cloud.consumersoap.administration.service.ApplicationNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.administration.service.DuplicateApplicationErrorFault;
import com.orange.clara.cloud.consumersoap.administration.service.PaasAdministrationService;
import com.orange.clara.cloud.consumersoap.administration.service.PaasUserNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.incubator.model.CreateUserCommand;
import com.orange.clara.cloud.consumersoap.incubator.service.PaasIncubatorService;
import com.orange.clara.cloud.consumersoap.security.Credentials;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.StepDefAnnotation;

@ContextConfiguration(classes = ApplicationStoriesStepsContext.class)
@DirtiesContext
@StepDefAnnotation
public class ApplicationStoriesSteps {

	@Autowired
	// a paasAdministration web service proxy
	private PaasAdministrationService paasAdministrationServiceProxy;

	@Autowired
	// a paasIncubator web service proxy
	private PaasIncubatorService paasIncubatorServiceProxy;

	// can be passed through steps
	private Throwable throwable;

	// can be passed through steps
	private String applicationUID;

	// can be passed through steps
	private List<ApplicationModel> applications;

	private Map<String, Credentials> credentials;

	@Before
	public void setup() {
		this.throwable = null;
		this.applicationUID = null;
		this.credentials = getCredentials();
	}

	@Given("^testuser is a paas admin user$")
	public void givenTestUSerIsAPaasUser() throws JAXBException {
		CreateUserCommand command = new CreateUserCommand();
		command.setUsername("testuser");
		command.setSsoId("testuser");
		command.setMail("testuser@orange.com");
		paasIncubatorServiceProxy.createUser(command, credentials.get("testuser"));
	}

	@Given("^guest is a paas user$")
	public void givenGuestIsAPaasUser() throws JAXBException {
		CreateUserCommand command = new CreateUserCommand();
		command.setUsername("guest");
		command.setSsoId("guest");
		command.setMail("guest@orange.com");
		paasIncubatorServiceProxy.createUser(command, credentials.get("guest"));
	}

	@Given("^anonymous exists in Ldap with no paas role$")
	public void anonymous_exists_in_Ldap_with_no_paas_role() throws Throwable {
	}

	@Given("^dummy is a not a paas user$")
	public void givenDummyIsANotAPaasUser() throws JAXBException {
	}

	@When("^(.+) creates an application with code (.+) and label (.+)$")
	public void whenPaasUserCreatesAnApplicationWithLabelAndcode(String paasUser, String code, String label) {
		try {
			CreateApplicationCommand command = new CreateApplicationCommand();
			command.setCode(code);
			command.setLabel(label);
			command.setIsPublic(true);
			applicationUID = paasAdministrationServiceProxy.createApplication(command, credentials.get(paasUser));
		} catch (DuplicateApplicationErrorFault e) {
			throwable = e;
		} catch (javax.xml.ws.soap.SOAPFaultException ex) {
			throwable = ex;
		} catch (PaasUserNotFoundErrorFault e) {
			throwable = e;
		} catch (Exception e) {
			throwable = e;
		}
	}

	@When("^(.+) creates an application with null code$")
	public void whenPaasUserUserCreatesAnApplicationWithNullCode(String paasUser) {
		try {
			CreateApplicationCommand command = new CreateApplicationCommand();
			command.setCode(null);
			command.setLabel("aLabel");
			command.setIsPublic(true);
			paasAdministrationServiceProxy.createApplication(command, credentials.get(paasUser));
		} catch (DuplicateApplicationErrorFault e) {
			throwable = e;
		} catch (javax.xml.ws.soap.SOAPFaultException ex) {
			throwable = ex;
		} catch (PaasUserNotFoundErrorFault e) {
			throwable = e;
		}
	}

	@When("^(.+) creates an application with empty code$")
	public void whenPaasUserCreatesAnApplicationWithEmptyCode(String paasUser) {
		try {
			CreateApplicationCommand command = new CreateApplicationCommand();
			command.setCode("");
			command.setLabel("aLabel");
			command.setIsPublic(true);
			paasAdministrationServiceProxy.createApplication(command, credentials.get(paasUser));
		} catch (DuplicateApplicationErrorFault e) {
			throwable = e;
		} catch (javax.xml.ws.soap.SOAPFaultException ex) {
			throwable = ex;
		} catch (PaasUserNotFoundErrorFault e) {
			throwable = e;
		}
	}

	@When("^(.+) creates an application with null label$")
	public void whenPaasUserCreatesAnApplicationWithNullLabel(String paasUser) {
		try {
			CreateApplicationCommand command = new CreateApplicationCommand();
			command.setCode("aCode");
			command.setLabel(null);
			command.setIsPublic(true);
			paasAdministrationServiceProxy.createApplication(command, credentials.get(paasUser));
		} catch (DuplicateApplicationErrorFault e) {
			throwable = e;
		} catch (javax.xml.ws.soap.SOAPFaultException ex) {
			throwable = ex;
		} catch (PaasUserNotFoundErrorFault e) {
			throwable = e;
		}
	}

	@When("^(.+) creates an application with empty label$")
	public void whenPaasUserCreatesAnApplicationWithEmptyLabel(String paasUser) {
		try {
			CreateApplicationCommand command = new CreateApplicationCommand();
			command.setCode("aCode");
			command.setLabel("");
			command.setIsPublic(true);
			paasAdministrationServiceProxy.createApplication(command, credentials.get(paasUser));
		} catch (DuplicateApplicationErrorFault e) {
			throwable = e;
		} catch (javax.xml.ws.soap.SOAPFaultException ex) {
			throwable = ex;
		} catch (PaasUserNotFoundErrorFault e) {
			throwable = e;
		}
	}

	@When("^(.+) searches for application with label (.+)$")
	public void whenPaasUserSearchesForAnApplicationWithLabel(String paasUser, String label) {
		try {
			applicationUID = paasAdministrationServiceProxy.findApplicationByLabel(label, credentials.get(paasUser));
		} catch (ApplicationNotFoundErrorFault e) {
			throwable = e;
		}
	}

	@When("^(.+) searches for all applications$")
	public void whenPaasUserSearchesForAllApplications(String paasUser) {
		// can be passed through steps
		applications = paasAdministrationServiceProxy.getAllApplications(credentials.get(paasUser));
	}

	@Then("^the application creation should succeed$")
	public void thenTheApplicationCreationShouldSucceed() {
		// assert no exception has been thrown
		Assert.assertTrue("createApplication operation has thrown following exception : " + throwable, throwable == null);
		// assert createApplication operation returns application uid
		org.springframework.util.Assert.hasText(applicationUID, "createApplication operation should return application uid");
	}

	@Then("^the application creation should fail$")
	public void thenTheApplicationCreationShouldFail() {
		Assert.assertTrue(throwable instanceof javax.xml.ws.soap.SOAPFaultException);
	}

	@Then("^the application creation should fail as (.*) is unknown$")
	public void thenSheShouldNotBeAbleToAuthenticate(String member) {
		Assert.assertEquals("Cannot create application as member \"" + member + "\" is unknown.", throwable.getMessage());
	}

	@Then("^the application creation should fail because application already exists$")
	public void thenTheApplicationCreationShouldFailBecauseApplicationAlreadyExists() {
		Assert.assertTrue(throwable instanceof DuplicateApplicationErrorFault);
	}

	@Then("^the research of the application should succeed$")
	public void thenTheReseachOfTheApplicationShouldSucceed() {
		// Assert the applicationID or identical
		// assert no exception has been thrown
		Assert.assertTrue("findApplicationByLabel operation has thrown following exception : " + throwable, throwable == null);
		// assert createApplication operation returns application uid
		org.springframework.util.Assert.hasText(applicationUID, "findApplicationByLabel operation should return application uid");
	}

	@Then("^she should get application with code (.+) and label (.+)$")
	public void thenTheReseachOfTheApplicationsShouldSucceed(String code, String label) {
		// assert getAllApplications operation returns applications
		org.springframework.util.Assert.notEmpty(applications, "getAllApplications operation should return applications");
		Assert.assertTrue(contains(applications, code, label));
	}

	@Then("^she should not be authorized to perform this action$")
	public void thenSheShouldNotBeAuthorizedToPerformThisAction() {
		Assert.assertNotNull("authorization should have failed", throwable);
		Assert.assertEquals("User is not authorized to perform this action", throwable.getMessage());
	}

	@Then("^she should not be able to authenticate$")
	public void thenSheShouldNotBeAbleToAuthenticate() {
		Assert.assertEquals("Authentication failed. Bad credentials", throwable.getMessage());
	}

	private boolean contains(List<ApplicationModel> applications, String code, String label) {
		Assert.assertNotNull(code);
		Assert.assertNotNull(label);
		for (int i = 0; i < applications.size(); i++)
			if (code.equals(applications.get(i).getCode()) && label.equals(applications.get(i).getLabel()))
				return true;
		return false;
	}

	private Credentials getTestUserCredentials() {
		Credentials credentials = new Credentials();
		credentials.setSsoid("testuser");
		credentials.setPassword("ce6utEtH");
		return credentials;
	}

	private Credentials getGuestUserCredentials() {
		Credentials credentials = new Credentials();
		credentials.setSsoid("guest");
		credentials.setPassword("guest");
		return credentials;
	}

	private Credentials getDummyUserCredentials() {
		Credentials credentials = new Credentials();
		credentials.setSsoid("dummy");
		credentials.setPassword("dummy");
		return credentials;
	}

	private Credentials getAnonymousUserCredentials() {
		Credentials credentials = new Credentials();
		credentials.setSsoid("anonymous");
		credentials.setPassword("anonymous");
		return credentials;
	}

	private Map<String, Credentials> getCredentials() {
		Map<String, Credentials> credentials = new HashMap<String, Credentials>();
		credentials.put("testuser", getTestUserCredentials());
		credentials.put("guest", getGuestUserCredentials());
		credentials.put("dummy", getDummyUserCredentials());
		credentials.put("anonymous", getAnonymousUserCredentials());
		return credentials;
	}
}
