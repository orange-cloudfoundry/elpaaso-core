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
package com.francetelecom.clara.cloud.stepdefs;

import com.francetelecom.clara.cloud.application.impl.DoubleAuthentication;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils.ConfigEntry;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDaoTestUtils;
import com.francetelecom.clara.cloud.services.dto.ConfigOverrideDTO;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.net.URL;
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;

@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/stepdefs/cucumber-context.xml")
@DirtiesContext
public class ConfigureEnvStepDefs {

	@Autowired(required = true)
	ManageApplication manageApplication;

	@Autowired(required = true)
	ManageApplicationRelease manageApplicationRelease;

	@Autowired(required = true)
	ManageEnvironment manageEnvironment;

	@Autowired(required = true)
	ManageLogicalDeployment manageLogicalDeployment;

	@Autowired(required = true)
	ManagePaasUser managePaasUser;

	@Autowired
	@Qualifier("mvnDao")
	protected MvnRepoDao mvnRepoDaoMock;
	
	LogicalConfigServiceUtils logicalConfigServiceUtils = new LogicalConfigServiceUtils();
	
	@Autowired
	ProjectionServiceStub projectionServiceStub;

	private String applicationUID;
	private String releaseUID;
	private String environmentUID;

	@Before("@config")
	public void init() throws Exception {
		// Configure MvnRepoDao Mock
		MvnRepoDaoTestUtils.mockResolveUrl(mvnRepoDaoMock);

		// -- Create a user
		// FIXME: workaround for art #147272: use cuid as firstname
		String cuid = "qbwb2700";
		PaasUser paasUser = new PaasUser(cuid, "Guillaume", new SSOId(cuid), "any@email.com");
		paasUser.setPaasUserRole(PaasRoleEnum.ROLE_USER);
		managePaasUser.checkBeforeCreatePaasUser(paasUser);
		managePaasUser.findPaasUser(cuid);

		// -- Authentication
		SecurityContextHolder.getContext().setAuthentication(new DoubleAuthentication(cuid, paasUser.getPaasUserRole()));

		// -- Create an application
		applicationUID = manageApplication.createPublicApplication(
				"appCode",
				"appName",
				"random desc",
				new URL("http://any-url.org"),
				new SSOId(cuid));

		// -- Create a release
		releaseUID = manageApplicationRelease.createApplicationRelease(applicationUID, cuid, "1.0");
	}
	
	@Given("^the following contract in the architecture:$")
	public void given_the_following_contract_in_the_architecture(List<ConfigProperty> configProperties) throws Throwable {
		int logicalDeploymentId = manageApplicationRelease.findApplicationReleaseByUID(releaseUID).getLogicalDeployment().getId();
		LogicalDeployment logicalDeployment = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentId);

		ProcessingNode node = new JeeProcessing();
		node.setLabel("processingNode");
		node.setSoftwareReference(new MavenReference("groupId", "artifactId", "version", "ear"));
		logicalDeployment.addExecutionNode(node);
		
		Map<String, List<ConfigEntry>> configSets = formatConfigToText(configProperties);
		
		for (Map.Entry<String, List<ConfigEntry>> configSet : configSets.entrySet()) {
			LogicalConfigService configService = new LogicalConfigService();
			configService.setLabel(configSet.getKey());
			configService.setConfigSetContent(logicalConfigServiceUtils.dumpConfigContentToString(new StructuredLogicalConfigServiceContent("", configSet.getValue())));
			logicalDeployment.addLogicalService(configService);

			node.addLogicalServiceUsage(configService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		}
		logicalDeployment = manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
		logicalDeployment = manageLogicalDeployment.checkOverallConsistencyAndUpdateLogicalDeployment(logicalDeployment);
	}

	private Map<String, List<ConfigEntry>> formatConfigToText(List<ConfigProperty> configProperties) {
		Map<String, List<ConfigEntry>> configSets = new HashMap<>();
		for (ConfigProperty configProperty : configProperties) {
			List<ConfigEntry> configEntries = configSets.get(configProperty.config_set);
			if (configEntries == null) {
				configEntries = new ArrayList<>();
				configSets.put(configProperty.config_set, configEntries);
			}
			configEntries.add(new ConfigEntry(configProperty.key, configProperty.value, configProperty.comment));
		}
		return configSets;
	}

	@When("^instantiating environment \"(.*)\", I request the following override:$")
	public void when_instantiating_environment_paas_env_I_request_the_following_override(String envName, List<ConfigOverrideDTO> configProperties)
	        throws Throwable {
		String configRoleUID = manageApplication.createConfigRole(applicationUID, "implicit role for environment", configProperties);
		environmentUID = manageEnvironment.createEnvironment(releaseUID, EnvironmentTypeEnum.DEVELOPMENT, "qbwb2700", envName, Arrays.asList(configRoleUID));
	}

	@When("^instantiating a new environment, I request to override the (.*) with key named (.*) and an overriden value (.*) and an associated comment (.*)$")
	public void when_I_request_to_override_the_given_property(String config_set, String key, String newValue, String comment) throws Throwable {
		ConfigOverrideDTO configProperty = new ConfigOverrideDTO(config_set, key, newValue, comment);
		String configRoleUID = manageApplication.createConfigRole(applicationUID, "implicit role for environment", Arrays.asList(configProperty));
		environmentUID = manageEnvironment.createEnvironment(releaseUID, EnvironmentTypeEnum.DEVELOPMENT, "qbwb2700", "my-new-env", Arrays.asList(configRoleUID));
	}

	@Then("^my config override request is (.*) with the potential (.*) message$")
	public void then_my_config_override_request_is_accepted_or_not_with_the_potential_message(RequestStatusEnum status, String message) throws Throwable {
		switch (status) {
		case ACCEPTED:
			assertThat(projectionServiceStub.exceptionMessage).isNull();
			break;
		case REJECTED:
			assertThat(projectionServiceStub.exceptionMessage).isNotNull();
			assertThat(projectionServiceStub.exceptionMessage).contains(message);
			break;
		}
	}
	
	@Then("^the following environment config is available for review, and bound to the application:$")
	public void then_the_environment_config_is_available_for_review_and_bound_to_the_app(List<ResultProperty> resultProperties) throws Throwable {
		// TODO assert environment configuration review
		
		// assert application configuration: only key and value are checked (see ConfigProperty equals method)
		assertThat(projectionServiceStub.environmentProperties).containsOnly(resultProperties.toArray());
	}

	public static class ConfigProperty {
		private String config_set;
		private String key;
		private String value;
		private String comment;

		public ConfigProperty() {
		}

		public ConfigProperty(String config_set, String key, String value, String comment) {
			super();
			this.config_set = config_set;
			this.key = key;
			this.value = value;
			this.comment = comment;
		}

		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this, Arrays.asList("config_set", "comment"));
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj, Arrays.asList("config_set", "comment"));
		}

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this);
        }
	}

	public static class ResultProperty extends ConfigProperty {

		private boolean is_overriden;
		private String jee_processing;

		public ResultProperty() {
		}

		public ResultProperty(String config_set, String key, String value, String comment, String jee_processing, boolean is_overriden) {
			super(config_set, key, value, comment);
			this.jee_processing = jee_processing;
			this.is_overriden = is_overriden;
		}
		
		@Override
		public int hashCode() {
			return HashCodeBuilder.reflectionHashCode(this, Arrays.asList("config_set", "comment", "jee_processing", "is_overriden"));
		}

		@Override
		public boolean equals(Object obj) {
			return EqualsBuilder.reflectionEquals(this, obj, Arrays.asList("config_set", "comment", "jee_processing", "is_overriden"));
		}

    }
}

