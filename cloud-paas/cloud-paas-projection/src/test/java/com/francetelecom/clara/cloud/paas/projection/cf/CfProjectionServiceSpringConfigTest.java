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
package com.francetelecom.clara.cloud.paas.projection.cf;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.JeeProbeLogicalModelCatalog;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Organization;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Cloudfoundry specific projection tests
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CfProjectionServiceSpringConfigTest {

	@Autowired
	@Qualifier("baseProjectionService-2.1.0-cf")
	CfProjectionServiceImpl cfProjectionServiceImpl;

	@Autowired
	@Qualifier("jeeProbeLogicalModelCatalog")
	JeeProbeLogicalModelCatalog logicalModelCatalog;

	@Autowired
	protected MvnRepoDao mvnRepoDaoMock;

	ApplicationRelease applicationRelease;
	LogicalDeployment logicalDeployment;

	@Before
	public void setUp() {
		applicationRelease = new ApplicationRelease(new Application("mon Appli", "code"), "G1R0C0");
		logicalDeployment = applicationRelease.getLogicalDeployment();

		when(mvnRepoDaoMock.resolveUrl(any(MavenReference.class))).thenAnswer(new Answer<MavenReference>() {
			@Override
			public MavenReference answer(InvocationOnMock invocation) throws Throwable {
				return (MavenReference) invocation.getArguments()[0];
			}
		});
	}

	@Test
	public void spring_config_properly_resolves_dev_strategies() throws UnsupportedProjectionException {
		assertBasicProjectionDoesNotBreakOnSpringBeansResolution(DeploymentProfileEnum.DEVELOPMENT, 1);

	}

	@Test
	public void spring_config_properly_resolves_prod_strategies() throws UnsupportedProjectionException {
		assertBasicProjectionDoesNotBreakOnSpringBeansResolution(DeploymentProfileEnum.PRODUCTION, 2);

	}

	private void assertBasicProjectionDoesNotBreakOnSpringBeansResolution(DeploymentProfileEnum deploymentProfile, int expectedInstanceCounts)
			throws UnsupportedProjectionException {
		// given
		logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
		resolvUrlOnSampleCatalog(logicalDeployment);

		// when
		TechnicalDeploymentTemplate tdt = cfProjectionServiceImpl.generateNewDeploymentTemplate(applicationRelease, deploymentProfile);

		// then
		TechnicalDeployment td = tdt.getTechnicalDeployment();

		Set<Space> spaces = td.listXaasSubscriptionTemplates(Space.class);
		assertThat(spaces).hasSize(1);

		Set<Organization> organizations = td.listXaasSubscriptionTemplates(Organization.class);
		assertThat(organizations).hasSize(1);

		Set<App> apps = td.listXaasSubscriptionTemplates(App.class);
		assertThat(apps).hasSize(1);
		App app = apps.iterator().next();
		assertThat(app.getInstanceCount()).isEqualTo(expectedInstanceCounts);
	}

	private void resolvUrlOnSampleCatalog(LogicalDeployment logicalDeploymentToUpdate) {

		// Simulate maven reference of all execution nodes of the logical
		// deployment
		List<ProcessingNode> nodes = logicalDeploymentToUpdate.listProcessingNodes();
		for (ProcessingNode node : nodes) {

			MavenReference mvnRef1 = node.getSoftwareReference();
			try {
				mvnRef1.setAccessUrl(new URL("http://my.nexus.com"));
			} catch (MalformedURLException e) {
				fail(e.toString());
			}
		}
	}

}
