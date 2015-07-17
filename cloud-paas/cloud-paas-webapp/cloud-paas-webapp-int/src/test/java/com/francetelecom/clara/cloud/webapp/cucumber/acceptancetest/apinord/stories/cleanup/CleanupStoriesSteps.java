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
package com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.apinord.stories.cleanup;

import java.util.ArrayList;
import java.util.List;

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

import com.orange.clara.cloud.consumersoap.administration.model.ApplicationModel;
import com.orange.clara.cloud.consumersoap.administration.model.ReleaseModel;
import com.orange.clara.cloud.consumersoap.administration.service.ApplicationNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.administration.service.PaasAdministrationService;
import com.orange.clara.cloud.consumersoap.environment.model.DeleteEnvironmentCommand;
import com.orange.clara.cloud.consumersoap.environment.model.EnvironmentModel;
import com.orange.clara.cloud.consumersoap.environment.model.EnvironmentStatus;
import com.orange.clara.cloud.consumersoap.environment.model.EnvironmentStatusEnum;
import com.orange.clara.cloud.consumersoap.environment.service.EnvironmentNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.environment.service.PaasEnvironmentService;
import com.orange.clara.cloud.consumersoap.environment.service.ReleaseNotFoundErrorFault;
import com.orange.clara.cloud.consumersoap.incubator.model.CreateUserCommand;
import com.orange.clara.cloud.consumersoap.incubator.service.PaasIncubatorService;
import com.orange.clara.cloud.consumersoap.security.Credentials;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;

//@StepDefAnnotation
@ContextConfiguration(classes = CleanupStoriesStepsContext.class)
@DirtiesContext
public class CleanupStoriesSteps {
	private static final Logger logger = LoggerFactory.getLogger(CleanupStoriesSteps.class);

	@Autowired
	// a paasAdministration web service proxy
	private PaasAdministrationService paasAdministrationServiceProxy;

	@Autowired
	// a paasIncubator web service proxy
	private PaasIncubatorService paasIncubatorServiceProxy;
	@Autowired
	// a passEnvironment web service proxy
	private PaasEnvironmentService paasEnvironmentServiceProxy;

	@Given("^adminuser is a paas user$")
	public void givenTestUSerIsAPaasUser() throws JAXBException {
		CreateUserCommand command = new CreateUserCommand();
		command.setUsername("testuser");
		command.setSsoId("testuser");
		command.setMail("testuser@orange.com");
		paasIncubatorServiceProxy.createUser(command, getPaasAdminCredential());
	}

	@When("^(.+) deletes all environments,releases and applications$")
	public void whenPaasAdminDeletesAllEnvironmentsReleasesAndApplications(String paasUser) throws Exception {
		List<ApplicationModel> applications = getAllApplications();

		for (ApplicationModel anApplication : applications) {
			List<ReleaseModel> releases = getAllReleaseForApplication(anApplication);
			for (ReleaseModel aRelease : releases) {
				List<EnvironmentModel> environments = getAllEnvironmentForRelease(aRelease);
				for (EnvironmentModel anEnvironment : environments) {
					if (shouldDeleteEnvironment(anEnvironment)) {
						deleteEnvironment(anEnvironment);
					}
				}
				environments = getAllEnvironmentForRelease(aRelease);
				if (environments.size() == 0) {
					deleteRelease(aRelease);
				}
			}
			releases = getAllReleaseForApplication(anApplication);
			if (releases.size() == 0) {
				deleteApplication(anApplication);
			}
		}

	}

	@Then("^application count is (\\d+) and release count is (\\d+) and environment count is (\\d+)$")
	public void thenAllCountShouldMatchExpectation(Integer applicationCount, Integer releaseCount, Integer environmentCount) {

		List<ApplicationModel> applications = getAllApplications();
		int currentReleaseCount = 0;
		int currentEnvironmentCount = 0;

		for (ApplicationModel anApplication : applications) {
			List<ReleaseModel> allReleaseForApplication = getAllReleaseForApplication(anApplication);
			for (ReleaseModel aRelease : allReleaseForApplication) {
				currentEnvironmentCount += getAllEnvironmentForRelease(aRelease).size();
			}
			currentReleaseCount += allReleaseForApplication.size();
		}
		Assert.assertEquals("Too much environments left.", environmentCount.longValue(), currentEnvironmentCount);
		Assert.assertEquals("Too much releases left.", releaseCount.longValue(), currentReleaseCount);
		Assert.assertEquals("Too much applications left.", applicationCount.longValue(), applications.size());
	}

	public List<ApplicationModel> getAllApplications() {
		List<ApplicationModel> applications = paasAdministrationServiceProxy.getAllApplications(getPaasAdminCredential());
		return applications;
	}

	public List<EnvironmentModel> getAllEnvironmentForRelease(ReleaseModel aRelease) {
		List<EnvironmentModel> environments = new ArrayList<EnvironmentModel>();

		try {
			environments = paasEnvironmentServiceProxy.findAndGetEnvironmentsByRelease(aRelease.getUid(), getPaasAdminCredential());
		} catch (ReleaseNotFoundErrorFault e) {
			logger.warn("Release {} doesn't exist anymore, ignoring it: {}", aRelease.getVersion(), e.getMessage());
		}
		return environments;
	}

	public List<ReleaseModel> getAllReleaseForApplication(ApplicationModel application) {
		List<ReleaseModel> releases = new ArrayList<ReleaseModel>();
		try {
			releases = paasAdministrationServiceProxy.getApplicationReleasesByApplicationUID(application.getUid(), getPaasAdminCredential());
		} catch (ApplicationNotFoundErrorFault e) {
			logger.warn("Application {}-{} doesn't exist anymore, ignoring it: {}",
					new Object[] { application.getLabel(), application.getCode(), e.getMessage() });
		}
		return releases;
	}

	public boolean shouldDeleteEnvironment(EnvironmentModel anEnvironment) {
		EnvironmentStatus envStatus = anEnvironment.getStatus();
		return isFailed(envStatus) || isRunning(envStatus) || isStopped(envStatus);
	}

	public void deleteEnvironment(EnvironmentModel anEnvironment) throws Exception {
		DeleteEnvironmentCommand command = new DeleteEnvironmentCommand();
		command.setUid(anEnvironment.getUid());
		try {
			paasEnvironmentServiceProxy.deleteEnvironment(command, getPaasAdminCredential());
			Integer timeoutInMin = 10;
			environmentShouldBeRemovedWithinADelayOf(timeoutInMin, anEnvironment.getUid());
		} catch (EnvironmentNotFoundErrorFault e) {
			logger.warn("Environment {} doesn't exist anymore, ignoring it", anEnvironment.getLabel(), e.getMessage());
		}
	}

	private void deleteRelease(ReleaseModel aRelease) {
		logger.info("Deleting release {}", aRelease.getVersion());
		try {
			paasAdministrationServiceProxy.deleteApplicationRelease(aRelease.getUid(), getPaasAdminCredential());
		} catch (com.orange.clara.cloud.consumersoap.administration.service.ReleaseNotFoundErrorFault e) {
			logger.error("Release {} not found. Error {}", aRelease.getVersion(), e.getMessage());
		}
	}

	private void deleteApplication(ApplicationModel anApplication) {
		logger.info("Deleting application {}", anApplication.getLabel());
		try {
			paasAdministrationServiceProxy.deleteApplication(anApplication.getUid(), getPaasAdminCredential());
		} catch (ApplicationNotFoundErrorFault e) {
			logger.error("Application {} not found. Error {}", anApplication.getLabel(), e.getMessage());
		}

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

	public void environmentShouldBeRemovedWithinADelayOf(Integer timeoutInSec, final String environmentUID) throws Exception {
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

	private EnvironmentStatus getEnvironmentStatus(String environmentUID) throws EnvironmentNotFoundErrorFault {
		logger.debug("checking environment status...");
		return paasEnvironmentServiceProxy.getEnvironmentStatus(environmentUID, getPaasAdminCredential());
	}

	private Credentials getPaasAdminCredential() {
		Credentials credentials = new Credentials();
		credentials.setSsoid("testuser");
		credentials.setPassword("ce6utEtH");
		return credentials;
	}

	private static RetryTemplate getRetryTemplate(Integer timeoutInMin) {
		// we set the retry time out
		TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
		retryPolicy.setTimeout(timeoutInMin * 1000 * 60);

		// we set the back off period to 60s
		FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
		backOffPolicy.setBackOffPeriod(60000);

		// our retry service
		RetryTemplate retryTemplate = new RetryTemplate();
		retryTemplate.setRetryPolicy(retryPolicy);
		retryTemplate.setBackOffPolicy(backOffPolicy);
		return retryTemplate;
	}

}
