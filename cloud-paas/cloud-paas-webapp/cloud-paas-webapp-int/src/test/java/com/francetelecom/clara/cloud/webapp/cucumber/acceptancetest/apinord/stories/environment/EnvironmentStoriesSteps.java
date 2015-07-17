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
package com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.apinord.stories.environment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import com.orange.clara.cloud.consumersoap.administration.model.CreateApplicationCommand;
import com.orange.clara.cloud.consumersoap.administration.model.CreateReleaseCommand;
import com.orange.clara.cloud.consumersoap.administration.service.PaasAdministrationService;
import com.orange.clara.cloud.consumersoap.environment.model.CreateEnvironmentCommand;
import com.orange.clara.cloud.consumersoap.environment.model.DeleteEnvironmentCommand;
import com.orange.clara.cloud.consumersoap.environment.model.EnvironmentModel;
import com.orange.clara.cloud.consumersoap.environment.model.EnvironmentStatus;
import com.orange.clara.cloud.consumersoap.environment.model.EnvironmentStatusEnum;
import com.orange.clara.cloud.consumersoap.environment.model.EnvironmentTypeEnum;
import com.orange.clara.cloud.consumersoap.environment.model.StartEnvironmentCommand;
import com.orange.clara.cloud.consumersoap.environment.model.StopEnvironmentCommand;
import com.orange.clara.cloud.consumersoap.environment.service.EnvironmentNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.environment.service.InvalidEnvironmentErrorFault;
import com.orange.clara.cloud.consumersoap.environment.service.PaasEnvironmentService;
import com.orange.clara.cloud.consumersoap.environment.service.PaasUserNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.environment.service.ReleaseNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.incubator.model.CreateUserCommand;
import com.orange.clara.cloud.consumersoap.incubator.model.SetLogicalDeploymentFromCatalogCommand;
import com.orange.clara.cloud.consumersoap.incubator.service.PaasIncubatorService;
import com.orange.clara.cloud.consumersoap.security.Credentials;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.StepDefAnnotation;

@ContextConfiguration(classes = EnvironmentStoriesStepsContext.class)
@DirtiesContext
@StepDefAnnotation
public class EnvironmentStoriesSteps {

	@Autowired
	// a passEnvironment web service proxy
	private PaasEnvironmentService paasEnvironmentServiceProxy;

	@Autowired
	// a paasAdministration web service proxy
	private PaasAdministrationService paasAdministrationServiceProxy;

	@Autowired
	// a paasIncubator web service proxy
	private PaasIncubatorService paasIncubatorServiceProxy;

	// can be passed through steps
	private Throwable throwable;

	// can be passed through steps
	private String releaseUID;

	// can be passed through steps
	private String environmentUID;

	// can be passed through steps
	private List<EnvironmentModel> environments;

	Logger logger = LoggerFactory.getLogger(EnvironmentStoriesSteps.class);

	private Map<String, Credentials> credentials;
	
	
	@Before
	public void setup() throws JAXBException {
		this.throwable = null;
		this.releaseUID = null;
		this.environmentUID = null;
		this.environments = null;
		this.credentials = getCredentials();
	}

	@After
	public void teardown() throws EnvironmentNotFoundErrorFault {
		// removes undeleted environments on scenario teardown
		if (environmentUID != null) {
			DeleteEnvironmentCommand command = new DeleteEnvironmentCommand();
			command.setUid(environmentUID);
			paasEnvironmentServiceProxy.deleteEnvironment(command,getTestUserCredentials());
		}
	}

	@Given("^testuser is a paas user$")
	public void givenTestUSerIsAPaasUser() throws JAXBException {
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

	@Given("^a release with a (.+) like logical deployment exists$")
	public void givenAReleaseWithLogicalDeploymentExists(String samplePrefixName) throws Exception {
		// create application
        String sampleLogicalModelName = samplePrefixName + "LogicalModelCatalog";
        CreateApplicationCommand createApplicationCommand = new CreateApplicationCommand();
		createApplicationCommand.setCode(UUID.randomUUID().toString());
		createApplicationCommand.setLabel(UUID.randomUUID().toString());
		createApplicationCommand.setIsPublic(true);
		String applicationUID = paasAdministrationServiceProxy.createApplication(createApplicationCommand, credentials.get("testuser"));
		// create release
		CreateReleaseCommand createReleaseCommand = new CreateReleaseCommand();
		createReleaseCommand.setApplicationUID(applicationUID);
		createReleaseCommand.setVersion("aVersion");
		releaseUID = paasAdministrationServiceProxy.createRelease(createReleaseCommand, credentials.get("testuser"));
		SetLogicalDeploymentFromCatalogCommand command = new SetLogicalDeploymentFromCatalogCommand();
		command.setLogicalModelNameReference(sampleLogicalModelName);
		command.setReleaseUID(releaseUID);
		paasIncubatorServiceProxy.setLogicalDeploymentFromCatalog(command,credentials.get("testuser"));
	}

	@Given("^(.+) has created an environment of type (.+) with label (.+) for that release")
	public void givenPaasUserHasCreatedAnEnvironment(String paasUser, EnvironmentTypeEnum type, String label)
			throws ReleaseNotFoundErrorFault, InvalidEnvironmentErrorFault, PaasUserNotFoundErrorFault {
		CreateEnvironmentCommand environment = new CreateEnvironmentCommand();
		environment.setType(type);
		environment.setReleaseUID(releaseUID);
		environment.setLabel(label);
		paasEnvironmentServiceProxy.createEnvironment(environment,credentials.get(paasUser));
	}

	@When("^(.+) creates an environment of type (.+) with label (.+) for that release$")
	public void whenPaasUserCreatesAnEnvironment(String paasUser, EnvironmentTypeEnum type, String label) {
		CreateEnvironmentCommand environment = new CreateEnvironmentCommand();
		environment.setType(type);
		environment.setReleaseUID(releaseUID);
		environment.setLabel(label);
		try {
			environmentUID = paasEnvironmentServiceProxy.createEnvironment(environment,credentials.get(paasUser));
		} catch (Exception e) {
			throwable = e;
			logger.error(e.getMessage(), e);
		}

	}

	@When("(.+) creates an environment for an unknown release")
	public void whenPaasUserCreatesAnEnvironmentForUnkwownRelease(String paasUser) {
		CreateEnvironmentCommand environment = new CreateEnvironmentCommand();
		environment.setType(EnvironmentTypeEnum.DEVELOPMENT);
		environment.setReleaseUID("dummy");
		environment.setLabel("a label");
		try {
			environmentUID = paasEnvironmentServiceProxy.createEnvironment(environment,credentials.get(paasUser));
		} catch (Exception e) {
			throwable = e;
			logger.error(e.getMessage(), e);
		}
	}

	@When("(.+) starts that environment")
	public void whenTestUserStartsThatEnvironment(String paasUser) throws EnvironmentNotFoundErrorFault {
		StartEnvironmentCommand command = new StartEnvironmentCommand();
		command.setUid(environmentUID);
		paasEnvironmentServiceProxy.startEnvironment(command,credentials.get(paasUser));
	}

	@When("^(.+) stops that environment$")
	public void whenTestUserStopsThatEnvironment(String paasUser) throws EnvironmentNotFoundErrorFault {
		StopEnvironmentCommand command = new StopEnvironmentCommand();
		command.setUid(environmentUID);
		paasEnvironmentServiceProxy.stopEnvironment(command,credentials.get(paasUser));
	}

	@When("^(.+) deletes that environment$")
	public void whenTestUserDeletesThatEnvironment(String paasUser) throws EnvironmentNotFoundErrorFault {
		DeleteEnvironmentCommand command = new DeleteEnvironmentCommand();
		command.setUid(environmentUID);
		paasEnvironmentServiceProxy.deleteEnvironment(command,credentials.get(paasUser));
	}

	@When("^(.+) gets details of all environments for that release$")
	public void whenPaasUserGetsDetailsOfAllEnvironmentsForThatRelease(String paasUser) throws ReleaseNotFoundErrorFault {
		environments = paasEnvironmentServiceProxy.findAndGetEnvironmentsByRelease(releaseUID,credentials.get(paasUser));
	}

	@Then("^that environment should be running within a delay of (\\d+) minutes$")
	public void thenTheEnvironmentShouldBeRunningWithinADelayOf(Integer timeoutInSec) throws Exception {
		Assert.assertTrue(throwable == null);
		// environment should be running before timeout
		// we need to retry because environment creation is asynchronous
		getRetryTemplate(timeoutInSec).execute(new RetryCallback<EnvironmentStatus, Exception>() {

			@Override
			public EnvironmentStatus doWithRetry(RetryContext context) throws Exception {
				EnvironmentStatus status = getEnvironmentStatus(environmentUID);
				logger.debug("environment status is " + status.getType());
				logger.debug("environment status progress is " + status.getProgress());
				logger.debug("environment status message is " + status.getMessage());
				if (!isRunning(status) && (!isFailed(status))) {
					throw new AssertionError("environment is not in required state; expected status was " + EnvironmentStatusEnum.RUNNING
							+ "; actual status is " + status.getType() + " - status message is " + status.getMessage());
				}
				return status;
			}
		});

	}

	@Then("^she should not be able to authenticate$")
	public void thenSheShouldNotBeAbleToAuthenticate() {
		Assert.assertEquals("Authentication failed. Bad credentials", throwable.getMessage());
	}
	
	@Then("^dummy should fail to create that environment$")
	public void thenDummyShouldFailToCreateThatEnvironment() {
		Assert.assertTrue(throwable instanceof PaasUserNotFoundErrorFault);
	}

	@Then("^she should fail to create that environment$")
	public void thenTestUserShouldFailToCreateThatEnvironment() {
		Assert.assertTrue(throwable instanceof ReleaseNotFoundErrorFault);
	}

	@Then("^that environment should be stopped within a delay of (\\d+) minutes")
	public void thenTheEnvironmentShouldBeStoppedWithinADelayOf(Integer timeoutInSec) throws Exception {
		// environment should be stopped before timeout
		// we need to retry because environment stop is asynchronous
		getRetryTemplate(timeoutInSec).execute(new RetryCallback<EnvironmentStatus, Exception>() {

			@Override
			public EnvironmentStatus doWithRetry(RetryContext context) throws Exception {
				EnvironmentStatus status = getEnvironmentStatus(environmentUID);
				logger.debug("environment status is " + status.getType());
				logger.debug("environment status progress is " + status.getProgress());
				logger.debug("environment status message is " + status.getMessage());

				if (!isStopped(status) && (!isFailed(status))) {
					throw new AssertionError("environment is not in required state; expected status was " + EnvironmentStatusEnum.STOPPED
							+ "; actual status is " + status.getType() + " - status message is " + status.getMessage());
				}
				return status;
			}
		});
	}

	@Then("^that environment should be removed within a delay of (\\d+) minutes$")
	public void thenTheEnvironmentShouldBeRemovedWithinADelayOf(Integer timeoutInSec) throws Exception {
		// environment should be removed before timeout
		// we need to retry because environment removal is asynchronous
		getRetryTemplate(timeoutInSec).execute(new RetryCallback<EnvironmentStatus, Exception>() {

			@Override
			public EnvironmentStatus doWithRetry(RetryContext context) throws Exception {
				EnvironmentStatus status = getEnvironmentStatus(environmentUID);
				logger.debug("environment status is " + status.getType());
				logger.debug("environment status progress is " + status.getProgress());
				logger.debug("environment status message is " + status.getMessage());
				if (!isRemoved(status) && (!isFailed(status))) {
					throw new AssertionError("environment is not in required state; expected status was " + EnvironmentStatusEnum.REMOVED
							+ "; actual status is " + status.getType() + " - status message is " + status.getMessage());
				}
				return status;
			}
		});
	}

	@Then("^she should get environment details containing a label (.+), a type (.+) and a status (.+)$")
	public void thenSheShouldGetEnvironmentDetailsContaining(String label, EnvironmentTypeEnum type, EnvironmentStatusEnum status) {
		boolean exists = false;
		for (EnvironmentModel environment : environments) {
			if (environment.getLabel().equals(label) && environment.getType().equals(type) && status.equals(environment.getStatus().getType())) {
				exists = true;
			}
		}
		Assert.assertTrue("findAndGetEnvironmentsByRelease operation should return an environment with label " + label + " of type " + type
				+ " and a status " + status, exists);
	}

	@Then("^she should not be authorized to perform this action$")
	public void thenSheShouldNotBeAuthorizedToPerformThisAction() {
		Assert.assertNotNull("authorization should have failed",throwable);
		Assert.assertEquals("User is not authorized to perform this action", throwable.getMessage());
	}

	/**
	 * get a spring customized retry template
	 * 
	 * @param timeoutInSec
	 * @return a customized retry template
	 */
	private static RetryTemplate getRetryTemplate(Integer timeoutInSec) {
		// we set the retry time out
		TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
		retryPolicy.setTimeout(timeoutInSec * 1000 * 60);

		// we set the back off period to 60s
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(60000);

		// our retry service
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		return retryTemplate;
	}

	private EnvironmentStatus getEnvironmentStatus(String environmentUID) throws EnvironmentNotFoundErrorFault {
		logger.debug("checking environment status...");
		return paasEnvironmentServiceProxy.getEnvironmentStatus(environmentUID,credentials.get("testuser"));
	}

	private boolean isRunning(EnvironmentStatus status) {
		return EnvironmentStatusEnum.RUNNING.equals(status.getType());
	}

	private boolean isStopped(EnvironmentStatus status) {
		return EnvironmentStatusEnum.STOPPED.equals(status.getType());
	}

	private boolean isRemoved(EnvironmentStatus status) {
		return EnvironmentStatusEnum.REMOVED.equals(status.getType());
	}

	private boolean isFailed(EnvironmentStatus status) {
		return EnvironmentStatusEnum.FAILED.equals(status.getType());
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
