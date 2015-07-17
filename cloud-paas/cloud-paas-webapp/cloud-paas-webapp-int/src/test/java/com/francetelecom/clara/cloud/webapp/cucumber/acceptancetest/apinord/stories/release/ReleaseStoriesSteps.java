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
package com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.apinord.stories.release;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import com.orange.clara.cloud.consumersoap.administration.model.CreateApplicationCommand;
import com.orange.clara.cloud.consumersoap.administration.model.CreateReleaseCommand;
import com.orange.clara.cloud.consumersoap.administration.model.MiddlewareProfile;
import com.orange.clara.cloud.consumersoap.administration.model.ReleaseModel;
import com.orange.clara.cloud.consumersoap.administration.service.ApplicationNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.administration.service.DuplicateApplicationErrorFault;
import com.orange.clara.cloud.consumersoap.administration.service.DuplicateReleaseErrorFault;
import com.orange.clara.cloud.consumersoap.administration.service.PaasAdministrationService;
import com.orange.clara.cloud.consumersoap.administration.service.PaasUserNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.incubator.model.CreateUserCommand;
import com.orange.clara.cloud.consumersoap.incubator.service.PaasIncubatorService;
import com.orange.clara.cloud.consumersoap.security.Credentials;

import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

//@StepDefAnnotation
@ContextConfiguration(classes = ReleaseStoriesStepsContext.class)
@DirtiesContext
public class ReleaseStoriesSteps {

	@Autowired
	// a paasAdministration web service proxy
	private PaasAdministrationService paasAdministrationServiceProxy;

	// can be passed through steps
	private String applicationUID;

	// can be passed through steps
	private String releaseUID;
	private String releaseUIDSearchedFor;
	private ReleaseModel releaseModel;
	List<ReleaseModel> releases;
	private List<MiddlewareProfile> lastFetchedMiddlewareProfiles;

	// can be passed through steps
	private Throwable throwable;

	@Autowired
	// a paasIncubator web service proxy
	private PaasIncubatorService paasIncubatorServiceProxy;

	private Map<String, Credentials> credentials;
	
	@Before
	public void setup() {
		this.throwable = null;
		this.applicationUID = null;
		this.releaseUID = null;
		this.releaseUIDSearchedFor = null;
		this.releases = new ArrayList<ReleaseModel>();
		this.releaseModel = null;
		this.lastFetchedMiddlewareProfiles = new ArrayList<MiddlewareProfile>();
		this.credentials = getCredentials();
	}

	@Given("^testuser is a paas user$")
	public void givenTestUserIsAPaasUser() throws JAXBException {
		CreateUserCommand command = new CreateUserCommand();
		command.setUsername("testuser");
		command.setSsoId("testuser");
		command.setMail("testuser@orange.com");
		paasIncubatorServiceProxy.createUser(command,credentials.get("testuser"));
	}

	@Given("^guest is a paas user with no role$")
	public void givenGuestIsAPaasUserWithNoRole() throws JAXBException {
		CreateUserCommand command = new CreateUserCommand();
		command.setUsername("guest");
		command.setSsoId("guest");
		command.setMail("guest@orange.com");
		paasIncubatorServiceProxy.createUser(command,credentials.get("testuser"));
	}

	@Given("^dummy is a not a paas user$")
	public void givenDummyIsANotAPaasUser() throws JAXBException {
	}

	@Given("^an application exists$")
	public void givenAnApplicationExists() throws DuplicateApplicationErrorFault, PaasUserNotFoundErrorFault, JAXBException {
		//create application with admin role
		givenTestUserIsAPaasUser();
		CreateApplicationCommand command = new CreateApplicationCommand();
		command.setCode(UUID.randomUUID().toString());
		command.setLabel(UUID.randomUUID().toString());
		command.setIsPublic(true);
		applicationUID = paasAdministrationServiceProxy.createApplication(command, credentials.get("testuser"));
	}
	
	@Given("^a release exists$")
	public void givenAReleaseExists() throws DuplicateReleaseErrorFault, PaasUserNotFoundErrorFault, ApplicationNotFoundErrorFault {
		CreateReleaseCommand command = new CreateReleaseCommand();
		command.setApplicationUID(applicationUID);
		command.setVersion("g1r0c0");
		releaseUID = paasAdministrationServiceProxy.createRelease(command, credentials.get("testuser"));

	}

	@Given("^an application exists with description application for bad profile (.+)$")
	public void an_application_exists_with_description_application_for_bad_profile(String profile) throws Throwable {
		// Write code here that turns the phrase above into concrete actions
		throw new PendingException();
	}

	@When("^(.+) creates a release with version (.+) with description (.+) with versionControlURL (.+) of that application$")
	public void whenTestUserCreatesAReleaseWithVersionWithOwnerOfThatApplication(String paasUser, String version, String description, String versionControlURL) {
		try {
			CreateReleaseCommand command = new CreateReleaseCommand();
			command.setApplicationUID(applicationUID);
			command.setVersion(version);
			command.setDescription(description);
			command.setVersionControlUrl(versionControlURL);
			releaseUID = paasAdministrationServiceProxy.createRelease(command,credentials.get(paasUser));
		} catch (Exception e) {
			throwable = e;
		}
	}
	
	@When("^(.+) creates a release with version (.+) with middleware profile (.+) of that application$")
	public void whenTestUserCreatesAReleaseWithVersionWithOwnerAndProfileOfThatApplication(String paasUser, String version, String profile) {
		try {
			CreateReleaseCommand command = new CreateReleaseCommand();
			command.setApplicationUID(applicationUID);
			command.setVersion(version);
			command.setProfileVersion(profile);
			command.setDescription("test release for profile "+profile);
			releaseUID = paasAdministrationServiceProxy.createRelease(command,credentials.get(paasUser));
		} catch (Exception e) {
			throwable = e;
		}
	}

	@When("^(.+) creates a release with version (.+) of an unknown application$")
	public void whenPaasUserCreatesAReleaseWithVersionWithOwnerOfUnknownApplication(String paasUser, String version) {
		try {
			CreateReleaseCommand command = new CreateReleaseCommand();
			command.setApplicationUID("unknown");
			command.setVersion(version);
			paasAdministrationServiceProxy.createRelease(command,credentials.get(paasUser));
		} catch (Exception e) {
			throwable = e;
		}
	}

	@When("^(.+) searches for a release with version (.+)$")
	public void whenPaasUserSearchesForAReleaseWithVersion(String paasUser, String version) {
		try {
			releaseUIDSearchedFor = paasAdministrationServiceProxy.findApplicationReleaseByApplicationUIDAndVersion(applicationUID, version,credentials.get(paasUser));
		} catch (Exception e) {
			throwable = e;
		}
	}

	@When("^(.+) searches for a release previously created$")
	public void whenPaasUserSearchesForAReleasePreviouslyCreated(String paasUser) {
		try {
			releaseModel = paasAdministrationServiceProxy.getApplicationRelease(releaseUID,credentials.get(paasUser));
		} catch (Exception e) {
			throwable = e;
		}
	}

	@When("^(.+) finds all releases for that application$")
	public void whenPaasUserFindsAllReleasesForThatApplication(String paasUser) {
		try {
			releases = paasAdministrationServiceProxy.getApplicationReleasesByApplicationUID(applicationUID,credentials.get(paasUser));
		} catch (ApplicationNotFoundErrorFault e) {
			throwable = e;
		}
	}
	
	@When("^(.+) list all middleware profiles$")
	public void whenTestuserListMiddlewareProfiles(String paasUser) {
		try {
			lastFetchedMiddlewareProfiles = paasAdministrationServiceProxy.getAllMiddlewareProfiles(credentials.get(paasUser));
		} catch (Exception e) {
			throwable = e;
		}
	}

	@Then("(.+) should get a release with version (.+) with description (.+) with versionControlURL (.+)$")
	public void thenHeShouldGetAReleaseWithVersionG1r0c0WithOwnerTestuser(String paasUser, String version, String description, String versionControlURL) {
		for (ReleaseModel release : releases) {
			if (release.getVersion().equals(version) && release.getDescription().equals(description)
					&& release.getVersionControlUrl().equals(versionControlURL))
				return;
		}
		Assert.fail("there should be a release with version " + version + " with description " + description + " with versionControlURL " + versionControlURL
				+ "");

	}
	
	@Then("^(.+) should not see any profile with status (.+)$")
	public void thenShouldNotSeeProfileWithStatus(String paasUser, String status) {
		for (MiddlewareProfile release : lastFetchedMiddlewareProfiles) {
			if(release.getStatus().equals(status)){
				fail("should not see profile "+release.getVersion()+" because it is in status "+status);
			}
		}
	}
	
	@Then("^(.+) should see profile (.+) with status (.+)$")
	public void thenShouldSeeProfile(String paasUser, String version, String status) {
		for (MiddlewareProfile release : lastFetchedMiddlewareProfiles) {
			if(release.getVersion().equals(version)){
				if(release.getStatus().equals(status)){
					return;
				}else{
					fail("status of middleware profile "+version+" seems bad (expected "+status+" but was "+release.getStatus());
				}
			}
		}
		fail("a profile with version "+version+" should be found");
	}

	@Then("^the release creation should succeed$")
	public void thenTheReleaseCreationShouldSucceed() {
		// assert no exception has been thrown
		Assert.assertTrue("an exception has been thrown while creating the release : " + throwable, throwable == null);
		// assert createRelease operation returns release uid
		org.springframework.util.Assert.hasText(releaseUID, "createRelease operation should return release uid");

	}

	@Then("^the release creation should fail$")
	public void thenTheReleaseCreationShouldFail() {
		Assert.assertTrue("an exception should have been thrown while creating the release", throwable != null);
	}

	@Then("^the system should raise a application not found error$")
	public void thenTheSystemShouldRaiseAApplicationNotFoundError() {
		Assert.assertTrue("wrong error type has been raised while creating the release. Expected: " + ApplicationNotFoundErrorFault.class + ", but was: "
				+ throwable.getClass(), throwable instanceof ApplicationNotFoundErrorFault);
	}

	@Then("^the research should succeed$")
	public void thenTheReseachShouldSucceed() {
		Assert.assertEquals(releaseUID, releaseUIDSearchedFor);
	}

	@Then("^the research of the Release should succeed$")
	public void thenTheResearchOfTheReleaseShouldSucceed() {
		Assert.assertNotNull(releaseModel);
		Assert.assertEquals("EDITING", releaseModel.getState().toString());
	}
	
	@Then("^she should not be authorized to perform this action$")
	public void thenSheShouldNotBeAuthorizedToPerformThisAction() {
		Assert.assertNotNull("authorization should have failed",throwable);
		Assert.assertEquals("User is not authorized to perform this action", throwable.getMessage());
	}

	@Then("^she should not be able to authenticate$")
	public void thenSheShouldNotBeAbleToAuthenticate() {
		Assert.assertEquals("Authentication failed. Bad credentials", throwable.getMessage());
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
	
	private Map<String, Credentials> getCredentials() {
		Map<String, Credentials> credentials = new HashMap<String, Credentials>();
		credentials.put("testuser", getTestUserCredentials());
		credentials.put("guest", getGuestUserCredentials());
		credentials.put("dummy", getDummyUserCredentials());
		return credentials;
	}
	
}
