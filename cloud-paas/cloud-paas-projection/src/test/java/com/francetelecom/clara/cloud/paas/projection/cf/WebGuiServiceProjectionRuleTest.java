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

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.BaseReferenceLogicalModelsCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.JeeProbeLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class WebGuiServiceProjectionRuleTest {

    WebGuiServiceProjectionRule webGuiServiceProjectionStrategy;
    RouteStrategyImpl routeStrategy;

    SampleAppProperties sampleAppProperties;
    LogicalDeployment logicalDeployment;

    @Before
    public void setUp() throws MalformedURLException {
        ApplicationRelease applicationRelease = new ApplicationRelease(new Application("mon Appli", "code"), "G1R0C0");
        logicalDeployment = applicationRelease.getLogicalDeployment();

        webGuiServiceProjectionStrategy = new WebGuiServiceProjectionRule();
        routeStrategy = new RouteStrategyImpl("cfapps.redacted-domain.org", "paasinstance");
        webGuiServiceProjectionStrategy.setRouteStrategy(routeStrategy);
    }

    @Test
    public void route_context_root_should_equal_to_logical_webgui_service_context_root() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        LogicalRabbitService logicalRabbitService = new LogicalRabbitService();
        logicalRabbitService.setServiceName("myRabbitService");
        final RabbitServiceProjectionRule rabbitServiceProjectionRule = new RabbitServiceProjectionRule();

        //when
        final ManagedService managedService = rabbitServiceProjectionRule.toRabbitService(logicalRabbitService, space, td);

        //then service instance name should equal to logical rabbitmq service name
        Assertions.assertThat(managedService.getServiceInstance()).isEqualTo("myRabbitService");
    }

    @Test
    public void generates_two_uris_when_two_webguis_bound_to_a_jeeprocessing() throws UnsupportedProjectionException {
        // Given
        BaseReferenceLogicalModelsCatalog logicalModelCatalog = new JeeProbeLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        JeeProcessing jeeProcessing = logicalDeployment.listProcessingNodes(JeeProcessing.class).get(0);
        LogicalWebGUIService webGui = new LogicalWebGUIService();
        webGui.setLabel("JEE-Back.OfficeUI"); // necessary otherwise NPEs
        webGui.setContextRoot(new ContextRoot("/"));
        logicalDeployment.addLogicalService(webGui);
        jeeProcessing.addLogicalServiceUsage(webGui, LogicalServiceAccessTypeEnum.PRODUCE);

        // when
        TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        webGuiServiceProjectionStrategy.apply(logicalDeployment, td, new DummyProjectionContext(space));

        // then
        assertThat(td.listXaasSubscriptionTemplates(Route.class)).hasSize(2);
    }

    @Test
    public void generates_a_route_for_each_webgui_with_matching_context_root_and_logical_model_id() {
        // Given
        BaseReferenceLogicalModelsCatalog logicalModelCatalog = new JeeProbeLogicalModelCatalog();
        sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        Set<LogicalWebGUIService> webGUIServices = logicalDeployment.listLogicalServices(LogicalWebGUIService.class);
        assertThat(webGUIServices.size()).isEqualTo(1);

        // Lets assign explicit webGui members
        Iterator<LogicalWebGUIService> webGUIServiceIterator = webGUIServices.iterator();
        LogicalWebGUIService webGui1 = webGUIServiceIterator.next();
        webGui1.setContextRoot(new ContextRoot("/context-root1"));

        // when
        TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final Route route = webGuiServiceProjectionStrategy.toRoute(webGui1, new DummyProjectionContext(space), space, td);

        // then
        assertThat(route.getContextRoot()).isEqualTo("/context-root1");
        assertThat(route.getLogicalModelId()).isEqualTo(webGui1.getName());
    }

}
