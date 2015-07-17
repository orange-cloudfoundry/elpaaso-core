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
package com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.gui.stories.environment;

import static org.junit.Assert.assertTrue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.TimeoutRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import com.francetelecom.clara.cloud.webapp.acceptancetest.pages.EnvironmentCreationPage;
import com.francetelecom.clara.cloud.webapp.acceptancetest.pages.EnvironmentDetailsPage;
import com.francetelecom.clara.cloud.webapp.acceptancetest.pages.HomePage;
import com.francetelecom.clara.cloud.webapp.acceptancetest.pages.PopulatePage;
import com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.gui.stories.CommonHtmlUnitDriverStepHelper;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.runtime.java.StepDefAnnotation;

@StepDefAnnotation
@ContextConfiguration(classes = EnvironmentStoriesStepsContext.class)
@DirtiesContext
public class EnvironmentStoriesSteps {

	// the page containing fields we are asserting
	private EnvironmentDetailsPage environmentDetailsPage;

	@Autowired
	private CommonHtmlUnitDriverStepHelper driverStepHelper;

	@Autowired
	// our retry service
	private RetryTemplate retryTemplate;

	Logger logger = LoggerFactory.getLogger(EnvironmentStoriesSteps.class);

	public EnvironmentStoriesSteps() {
	}

	@Before
	public void beforeScenario() {
		logger.debug("Breakpoint");
	}

	@After
	public void afterScenario() {
		// free allocated resources
		// In case environment is not deleted we force its deletion
		if (environmentDetailsPage != null) {
			if (!environmentDetailsPage.isDeleted()) {
				logger.debug("Deleting environment");
				environmentDetailsPage.delete();
				thenTheEnvironmentShouldBeDeletedWithin(10); // minutes
			} else {
				logger.debug("environment is already deleted: no need to force its deletion");
			}

		} else {
			logger.debug("environmentPage is null: unable to delete environment");
		}
		environmentDetailsPage = null;
	}

	@Given("^testuser is a paas user$")
	public void givenTestUserIsAPaasUser() {
		// PENDING
	}

	@Given("^(.+) has created a jeeProbe application with its MyJeeProbeSample - G00R01 release using populate page$")
	public void givenTestUserHasCreatedAJeeProbeApplicationWithItsG00R01ReleaseUsingPopulatePage(String ssoId) {
		HomePage homePage = driverStepHelper.goToLoginPage().loginAsDefaultUser();
		PopulatePage populatePage = homePage.displayPopulatePage(driverStepHelper.getPopulatePageURL().toString());
		populatePage.createJeeProbeAppAndItsRelease();
	}

    @Given("^(.+) has created a SimpleProbe application with its MySimpleProbeSample - G00R01 release using populate page$")
    public void testuser_has_created_a_SimpleProbe_application_with_its_MySimpleProbeSample_G_R_release_using_populate_page(String ssoId){
        HomePage homePage = driverStepHelper.goToLoginPage().loginAsDefaultUser();
        PopulatePage populatePage = homePage.displayPopulatePage(driverStepHelper.getPopulatePageURL().toString());
        populatePage.createSimpleProbeAppAndItsRelease();
    }

	@When("^(.+) creates an environment of type (.+) for release (.+) with label (.+)$")
	public void whenTestUserCreatesAnEnvironmentOfTypeTestForReleaseG1r0c0(String user, String type, String releaseName, String label) {
		// logs in elpaaso portal
		HomePage homePage = driverStepHelper.goToLoginPage().loginAsDefaultUser();
		// goes to required page to create an environment
		EnvironmentCreationPage environmentCreationPage = homePage.displayEnvironmentsOverviewPage().displayEnvironmentCreationPage();
		// creates an environment
		environmentDetailsPage = environmentCreationPage.createEnvironment(releaseName, label, type);
	}

	@Then("^the environment creation should succeed$")
	public void thenTheEnvironmentCreationShouldSucceed() {
		// PENDING
	}

	@Then("^the environment should be operational within (\\d+) minutes$")
	public void thenTheEnvironmentShouldBeOperationalWithin(int timeout) {
		TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
		// timeout in ms
		retryPolicy.setTimeout(timeout * 1000 * 60);

		retryTemplate.setRetryPolicy(retryPolicy);

		// Then environment should be in a operational state
		// we need to retry because environment creation is asynchronous
		try {
			retryTemplate.execute(new RetryCallback<Object, Exception>() {

				@Override
				public Object doWithRetry(RetryContext context) throws Exception {
					logger.debug("asserts that environment is running....");
					environmentDetailsPage.refresh();
					assertTrue("environment status is still CREATING", !environmentDetailsPage.isCreating());
					return null;
				}
			});
			assertTrue("environment is not operational yet : " + environmentDetailsPage.statusMessage(), environmentDetailsPage.isOperational());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@When("^(.+) deletes that environment")
	public void whenTestUserDeletesThatEnvironment(String user) {
		if (environmentDetailsPage != null) {
			logger.debug("Deleting environment");
			environmentDetailsPage.delete();
		} else {
			logger.debug("environmentPage is null: unable to delete environment");
		}
	}

	@Then("^that environment should be deleted within a delay of (.+) minutes$")
	public void thenTheEnvironmentShouldBeDeletedWithin(int timeout) {
		logger.info("The environment should be deleted within {} minutes", timeout);
		TimeoutRetryPolicy retryPolicy = new TimeoutRetryPolicy();
		// timeout in ms
		retryPolicy.setTimeout(timeout * 1000 * 60);

		retryTemplate.setRetryPolicy(retryPolicy);

		// Then environment should be deleted
		// we need to retry because this process is asynchronous
		try {
			retryTemplate.execute(new RetryCallback<Object, Exception>() {

				@Override
				public Object doWithRetry(RetryContext context) throws Exception {
					logger.debug("asserts that environment is deleted....");
					environmentDetailsPage.refresh();
					assertTrue("environment status is still REMOVING", !environmentDetailsPage.isDeleting());
					return null;
				}
			});
			assertTrue("environment is not deleted yet : " + environmentDetailsPage.statusMessage(), environmentDetailsPage.isDeleted());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void setDriverStepHelper(CommonHtmlUnitDriverStepHelper driverStepHelper) {
		this.driverStepHelper = driverStepHelper;
	}

}
