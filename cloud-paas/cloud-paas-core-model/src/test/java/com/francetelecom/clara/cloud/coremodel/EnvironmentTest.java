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
package com.francetelecom.clara.cloud.coremodel;

import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import org.apache.commons.lang3.RandomStringUtils;
import org.fest.assertions.Assertions;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Created with IntelliJ IDEA. User: shjn2064 Date: 06/09/12 Time: 11:36 To
 * change this template use File | Settings | File Templates.
 */
public class EnvironmentTest {

	@Test
	public void testSetAndTruncateStatusMessage() {
		Environment environment = new Environment();
		String statusMessage = new String("A message");

		environment.setAndTruncateStatusMessage(statusMessage);

		assertThat(environment.getStatusMessage()).isEqualTo(statusMessage);
	}

	@Test
	public void testSetAndTruncateStatusMessageWithA5000carsMessage() {
		Environment environment = new Environment();
		String a5000carsMessage = RandomStringUtils.randomAlphabetic(5000);

		environment.setAndTruncateStatusMessage(a5000carsMessage);

		assertThat(environment.getStatusMessage()).isEqualTo(a5000carsMessage);
	}

	@Test
	public void testSetAndTruncateStatusMessageWithMoreThan5000carsMessage() {
		Environment environment = new Environment();
		String a5001carsMessage = RandomStringUtils.randomAlphabetic(5001);
		environment.setAndTruncateStatusMessage(a5001carsMessage);
		a5001carsMessage.length();

		assertThat(environment.getStatusMessage()).isEqualTo(a5001carsMessage.substring(0, 4994) + Environment.STATUS_MESSAGE_SUFFIX);
	}

	@Test
	public void unique_label_is_built_from_uid_and_label() {
		// Given
		Environment environment = new Environment();
		environment.setLabel("my environment");
		environment.setUID("environment-uid");

		// When
		String envUniqueLabel = environment.getUniqueLabel();

		// Then
		assertThat(envUniqueLabel).isEqualTo("env environment-uid: my environment");
	}

	@Test
	public void unique_label_is_truncated_to_100_characters() {
		// Given
		Environment environment = new Environment();
		environment.setLabel("this is a very long environment label with description whose length is more than 100 caracters bla bla bla blas");
		environment.setUID("environment-uid");

		// When
		String envUniqueLabel = environment.getUniqueLabel();

		// Then
		assertThat(envUniqueLabel.length()).isEqualTo(100);
		assertThat(envUniqueLabel).startsWith("env environment-uid: "); // unique
																		// label
																		// should
																		// still
																		// starts
																		// with
																		// env
																		// uid
	}

	@Test
	public void unique_label_should_ends_with_ellipsis_if_truncated() {

		// Given
		Environment environment = new Environment();
		environment.setLabel("this is a very long environment label with description whose length is more than 100 caracters bla bla bla blas");
		environment.setUID("environment-uid");

		// When
		String envUniqueLabel = environment.getUniqueLabel();

		// Then
		assertThat(envUniqueLabel).isEqualTo("env environment-uid: this is a very long environment label with description whose length is more ...");
	}

	@Test(expected = IllegalArgumentException.class)
	public void environment_owner_is_mandatory() {
		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		ApplicationRelease applicationRelease = new ApplicationRelease(new Application("label", "code"),"version");
		new Environment(DeploymentProfileEnum.DEVELOPMENT, "envlabel", applicationRelease, null, new TechnicalDeploymentInstance(tdt, td));
	}

	@Test
	public void should_get_log_string() throws Exception {
		//given environment env_elpaasso_1_0 for release 1.0
		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		Application elpaaso = new Application("elpaaso", "elpaaso");
		ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
		TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
		Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "env_elpaasso_1_0", elpaaso_1_0, new PaasUser(),
				tdi);

		//when I get environment as a log string
		assertThat(environment.toLogString()).isEqualTo("createEnvironment query finished: id=0, releaseUID=" + elpaaso_1_0.getUID() + ", type=DEVELOPMENT, label=env_elpaasso_1_0, EnvTdiName="
				+ tdi.getName());
	}

	@Test
	public void activation_context_should_contain_env_label() throws Exception {

		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		ApplicationRelease applicationRelease = new ApplicationRelease(new Application("label", "code"), "version");
		final Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "envlabel", applicationRelease, new PaasUser("herve", "Vilard", new SSOId("Vilard"), "vilard@orange.com"), new TechnicalDeploymentInstance(tdt, td));

		final ActivationContext activationContext = environment.getActivationContext();

		Assertions.assertThat(activationContext.getEnvLabel()).isEqualTo("envlabel");
	}


	@Test
	public void activation_context_should_contain_env_uid() throws Exception {

		TechnicalDeployment td = new TechnicalDeployment("td");
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		ApplicationRelease applicationRelease = new ApplicationRelease(new Application("label", "code"), "version");
		final Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "envlabel", applicationRelease, new PaasUser("herve", "Vilard", new SSOId("Vilard"), "vilard@orange.com"), new TechnicalDeploymentInstance(tdt, td));

		final ActivationContext activationContext = environment.getActivationContext();

		Assertions.assertThat(activationContext.getEnvUID()).isEqualTo(environment.getUID());
	}
}
