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
import com.francetelecom.clara.cloud.logicalmodel.CFJavaProcessing;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.CFWicketCxfJpaLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.OptionalSoftwareReferenceLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SimpleProbeLogicalModelCatalog;
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
public class CFJavaProcessingProjectionRuleTest {

    public static final String BUILDPACK_URL = "a_buildpack_url";
    CFJavaProcessingProjectionRule cfJavaProcessingProjectionStrategy;

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
        cfJavaProcessingProjectionStrategy = new CFJavaProcessingProjectionRule(BUILDPACK_URL);
        ApplicationRelease applicationRelease = new ApplicationRelease(new Application("mon Appli", "code"), "G1R0C0");
        logicalDeployment = applicationRelease.getLogicalDeployment();

        projectionPlan.setWasPerVm(1);
        projectionPlan.setVmNumber(1);

        when(projectionPlanStrategy.getApplicationServerProjectionPlan(any(CFJavaProcessing.class), any(DeploymentProfileEnum.class))).thenReturn(projectionPlan);
        cfJavaProcessingProjectionStrategy.setProjectionPlanStrategy(projectionPlanStrategy);
        when(mvnRepoDao.resolveUrl(any(MavenReference.class))).thenReturn(mavenReference);
        when(mavenReference.getAccessUrl()).thenReturn(new URL("http://nexus.com"));
        cfJavaProcessingProjectionStrategy.setMvnDao(mvnRepoDao);

    }

    @Test
    public void app_name_should_equals_processing_node_label() throws UnsupportedProjectionException {
        // given
        CFWicketCxfJpaLogicalModelCatalog logicalModelCatalog = new CFWicketCxfJpaLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        CFJavaProcessing cfJavaProcessing = logicalDeployment.listProcessingNodes(CFJavaProcessing.class).get(0);

        // when
        TechnicalDeployment td = new TechnicalDeployment("name");
        final Space space = new Space(td);
        App app = cfJavaProcessingProjectionStrategy.toApp(space, td, new DummyProjectionContext(space), cfJavaProcessing);

        // then
        assertThat(app).isNotNull();
        Assertions.assertThat(app.getAppName()).isEqualTo(cfJavaProcessing.getLabel());
    }

    @Test
    public void app_disksize_should_equals_to_processing_node_disk_size() throws UnsupportedProjectionException {
        // given
        CFWicketCxfJpaLogicalModelCatalog logicalModelCatalog = new CFWicketCxfJpaLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        CFJavaProcessing cfJavaProcessing = logicalDeployment.listProcessingNodes(CFJavaProcessing.class).get(0);

        // when
        TechnicalDeployment td = new TechnicalDeployment("name");
        final Space space = new Space(td);
        App app = cfJavaProcessingProjectionStrategy.toApp(space, td, new DummyProjectionContext(space), cfJavaProcessing);

        // then
        assertThat(app.getDiskSizeMb()).isEqualTo(cfJavaProcessing.getMinDiskMbHint());
    }

    @Test
    public void it_rounds_up_the_ram_to_supported_plans() throws UnsupportedProjectionException {
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(0)).isEqualTo(128);
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(1)).isEqualTo(128);
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(-1)).isEqualTo(128);
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(127)).isEqualTo(128);
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(128)).isEqualTo(128);
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(129)).isEqualTo(256);
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(257)).isEqualTo(512);
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(513)).isEqualTo(1024);
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(1024)).isEqualTo(1024);
        assertThat(cfJavaProcessingProjectionStrategy.getMemory(2048)).isEqualTo(2048);
    }

    @Test(expected = UnsupportedProjectionException.class)
    public void it_rejects_unsupported_ram_amounts() throws UnsupportedProjectionException {
        cfJavaProcessingProjectionStrategy.getMemory(2049);
    }

    @Test
    public void should_generate_an_war_app_as_optional_artifact() throws UnsupportedProjectionException {
        // Given
        OptionalSoftwareReferenceLogicalModelCatalog logicalModelCatalog = new OptionalSoftwareReferenceLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        CFJavaProcessing cfJavaProcessing = logicalDeployment.listProcessingNodes(CFJavaProcessing.class).get(0);
        cfJavaProcessing.setOptionalSoftwareReference(true);

        projectionPlan.setMemoryMbPerWas(512);
        projectionPlan.setVmNumber(2);
        projectionPlan.setWasPerVm(3);

        // when
        TechnicalDeployment td = new TechnicalDeployment("name");
        final Space space = new Space(td);
        App app = cfJavaProcessingProjectionStrategy.toApp(space, td, new DummyProjectionContext(space), cfJavaProcessing);
        // then
        assertThat(app.getAppBinaries()).isEqualTo(cfJavaProcessing.getSoftwareReference());
        assertThat(app.getAppBinaries().getExtension()).isEqualToIgnoringCase("war");

    }

    @Test
    public void should_generate_an_jar_app_as_optional_artifact() throws UnsupportedProjectionException {
        // Given
        SimpleProbeLogicalModelCatalog logicalModelCatalog = new SimpleProbeLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        CFJavaProcessing cfJavaProcessing = logicalDeployment.listProcessingNodes(CFJavaProcessing.class).get(0);
        cfJavaProcessing.setOptionalSoftwareReference(true);

        projectionPlan.setMemoryMbPerWas(512);
        projectionPlan.setVmNumber(2);
        projectionPlan.setWasPerVm(3);

        // when
        TechnicalDeployment td = new TechnicalDeployment("name");
        final Space space = new Space(td);
        App app = cfJavaProcessingProjectionStrategy.toApp(space, td, new DummyProjectionContext(space), cfJavaProcessing);
        // then
        assertThat(app.getAppBinaries()).isEqualTo(cfJavaProcessing.getSoftwareReference());
        assertThat(app.getAppBinaries().getExtension()).isEqualToIgnoringCase("jar");
    }

    @Test
    public void generates_cfapp_respecting_memory_and_cpu_sizing_from_received_projection_plan() throws UnsupportedProjectionException {
        // Given
        CFWicketCxfJpaLogicalModelCatalog logicalModelCatalog = new CFWicketCxfJpaLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        CFJavaProcessing cfJavaProcessing = logicalDeployment.listProcessingNodes(CFJavaProcessing.class).get(0);

        projectionPlan.setMemoryMbPerWas(512);
        projectionPlan.setVmNumber(2);
        projectionPlan.setWasPerVm(3);

        // when
        TechnicalDeployment td = new TechnicalDeployment("name");
        final Space space = new Space(td);
        App app = cfJavaProcessingProjectionStrategy.toApp(space, td, new DummyProjectionContext(space), cfJavaProcessing);

        // then
        assertThat(app.getRamMb()).isEqualTo(512);
        assertThat(app.getInstanceCount()).isEqualTo(2 * 3);
    }

}
