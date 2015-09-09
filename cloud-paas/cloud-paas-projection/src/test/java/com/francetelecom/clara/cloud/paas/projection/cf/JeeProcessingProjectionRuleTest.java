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
import com.francetelecom.clara.cloud.logicalmodel.JeeProcessing;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.JeeProbeLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.paas.constraint.ProjectionPlan;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.net.MalformedURLException;
import java.net.URL;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 * Cloudfoundry specific projection unit tests
 */
@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class JeeProcessingProjectionRuleTest {

    JeeProcessingProjectionRule jeeProcessingStrategy = new JeeProcessingProjectionRule("a_buildpack_url");

    SampleAppProperties sampleAppProperties;
    @Mock
    MvnRepoDao mvnRepoDao;
    @Mock
    MavenReference mavenReference;

    @Mock
    ProjectionPlanStrategy projectionPlanStrategy;

    ProjectionPlan projectionPlan = new ProjectionPlan();

    LogicalDeployment logicalDeployment;

    @Before
    public void setUp() throws MalformedURLException {
        ApplicationRelease applicationRelease = new ApplicationRelease(new Application("mon Appli", "code"), "G1R0C0");
        logicalDeployment = applicationRelease.getLogicalDeployment();

        projectionPlan.setWasPerVm(1);
        projectionPlan.setVmNumber(1);
        when(projectionPlanStrategy.getApplicationServerProjectionPlan(any(JeeProcessing.class), any(DeploymentProfileEnum.class))).thenReturn(projectionPlan);

        jeeProcessingStrategy.setProjectionPlanStrategy(projectionPlanStrategy);
        when(mvnRepoDao.resolveUrl(any(MavenReference.class))).thenReturn(mavenReference);
        when(mavenReference.getAccessUrl()).thenReturn(new URL("http://nexus.com"));
        jeeProcessingStrategy.setMvnDao(mvnRepoDao);
    }

    @Test
    public void generates_a_cf_app() throws UnsupportedProjectionException {
        // given
        JeeProbeLogicalModelCatalog logicalModelCatalog = new JeeProbeLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        JeeProcessing jeeProcessing = logicalDeployment.listProcessingNodes(JeeProcessing.class).get(0);

        // when
        TechnicalDeployment td = new TechnicalDeployment("name");
        final Space space = new Space();
        App app = jeeProcessingStrategy.toApp(space, new DummyProjectionContext(space), jeeProcessing);

        // then
        assertThat(app).isNotNull();
        Assertions.assertThat(app.getAppName()).isEqualTo("JEEProbeExecNode");
        assertThat(app.getLogicalModelId()).isEqualTo(jeeProcessing.getName());
    }

    @Test
    public void it_rounds_up_the_ram_to_supported_plans() throws UnsupportedProjectionException {
        assertThat(jeeProcessingStrategy.getMemory(0)).isEqualTo(128);
        assertThat(jeeProcessingStrategy.getMemory(1)).isEqualTo(128);
        assertThat(jeeProcessingStrategy.getMemory(-1)).isEqualTo(128);
        assertThat(jeeProcessingStrategy.getMemory(127)).isEqualTo(128);
        assertThat(jeeProcessingStrategy.getMemory(128)).isEqualTo(128);
        assertThat(jeeProcessingStrategy.getMemory(129)).isEqualTo(256);
        assertThat(jeeProcessingStrategy.getMemory(257)).isEqualTo(512);
        assertThat(jeeProcessingStrategy.getMemory(513)).isEqualTo(1024);
        assertThat(jeeProcessingStrategy.getMemory(1024)).isEqualTo(1024);
        assertThat(jeeProcessingStrategy.getMemory(2048)).isEqualTo(2048);
    }

    @Test(expected = UnsupportedProjectionException.class)
    public void it_rejects_unsupported_ram_amounts() throws UnsupportedProjectionException {
        jeeProcessingStrategy.getMemory(2049);
    }

    @Test
    public void should_generate_an_ear_app_as_optional_artifact() throws UnsupportedProjectionException {
        // Given
        JeeProbeLogicalModelCatalog logicalModelCatalog = new JeeProbeLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        JeeProcessing jeeProcessing = logicalDeployment.listProcessingNodes(JeeProcessing.class).get(0);
        jeeProcessing.setOptionalSoftwareReference(true);

        projectionPlan.setMemoryMbPerWas(512);
        projectionPlan.setVmNumber(2);
        projectionPlan.setWasPerVm(3);

        // when
        final Space space = new Space();
        App app = jeeProcessingStrategy.toApp(space, new DummyProjectionContext(space), jeeProcessing);

        assertThat(app.getAppBinaries()).isEqualTo(jeeProcessing.getSoftwareReference());
        assertThat(app.getAppBinaries().getExtension()).isEqualToIgnoringCase("ear");
    }

    @Test
    public void generates_cfapp_respecting_memory_and_cpu_sizing_from_received_projection_plan() throws UnsupportedProjectionException {
        // Given
        JeeProbeLogicalModelCatalog logicalModelCatalog = new JeeProbeLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        JeeProcessing jeeProcessing = logicalDeployment.listProcessingNodes(JeeProcessing.class).get(0);

        projectionPlan.setMemoryMbPerWas(512);
        projectionPlan.setVmNumber(2);
        projectionPlan.setWasPerVm(3);

        // when
        final Space space = new Space();
        App app = jeeProcessingStrategy.toApp(space, new DummyProjectionContext(space), jeeProcessing);

        // then
        assertThat(app.getAppBinaries()).isEqualTo(jeeProcessing.getSoftwareReference());
        assertThat(app.getRamMb()).isEqualTo(512);
        assertThat(app.getInstanceCount()).isEqualTo(2 * 3);
    }

}
