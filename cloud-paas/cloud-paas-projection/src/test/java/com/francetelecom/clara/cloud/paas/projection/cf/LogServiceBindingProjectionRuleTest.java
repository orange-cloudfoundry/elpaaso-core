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
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.CFWicketCxfJpaLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Set;

public class LogServiceBindingProjectionRuleTest {

    @Test
    public void log_service_plan_should_be_splunk() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);

        final LogServiceBindingProjectionRule projectionRule = new LogServiceBindingProjectionRule();

        //when
        final ManagedService managedService = projectionRule.toLogService("my-app-name", space, td);

        //then service plan should be splunk
        Assertions.assertThat(managedService.getPlan()).isEqualTo("splunk");
    }

    @Test
    public void log_service_type_should_be_o_logs() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);

        final LogServiceBindingProjectionRule projectionRule = new LogServiceBindingProjectionRule();

        //when
        final ManagedService managedService = projectionRule.toLogService("my-app-name", space, td);

        //then service type should be o-logs
        Assertions.assertThat(managedService.getService()).isEqualTo("o-logs");
    }

    @Test
    public void log_service_name_should_start_with_app_name() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);

        final LogServiceBindingProjectionRule projectionRule = new LogServiceBindingProjectionRule();

        //when
        final ManagedService managedService = projectionRule.toLogService("my-app-name", space, td);

        //then service name should start with app name
        Assertions.assertThat(managedService.getServiceInstance()).startsWith("my-app-name");
    }

    @Test
    public void log_service_name_should_end_with_log() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);

        final LogServiceBindingProjectionRule projectionRule = new LogServiceBindingProjectionRule();

        //when
        final ManagedService managedService = projectionRule.toLogService("my-app-name", space, td);

        //then service name should start with app name
        Assertions.assertThat(managedService.getServiceInstance()).endsWith("-log");
    }


    @Test
    public void log_service_projection_rule_should_generate_a_log_managed_service_per_app() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final LogServiceBindingProjectionRule projectionRule = new LogServiceBindingProjectionRule();
        LogicalDeployment logicalDeployment = new LogicalDeployment();
        CFWicketCxfJpaLogicalModelCatalog logicalModelCatalog = new CFWicketCxfJpaLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        //simulate app generation
        new App(td, space, new MavenReference(), "server");
        new App(td, space, new MavenReference(), "client");


        //when
        projectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then 2 log managed services should have been generated in td
        final Set<ManagedService> actual = td.listXaasSubscriptionTemplates(ManagedService.class);
        Assertions.assertThat(actual).hasSize(2);
        actual.stream().forEach(service -> Assertions.assertThat(service.getService()).isEqualTo("o-logs"));

    }

    @Test
    public void should_bind_log_service_to_app() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final LogServiceBindingProjectionRule projectionRule = new LogServiceBindingProjectionRule();
        LogicalDeployment logicalDeployment = new LogicalDeployment();
        CFWicketCxfJpaLogicalModelCatalog logicalModelCatalog = new CFWicketCxfJpaLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        //simulate app generation
        App app = new App(td, space, new MavenReference(), "my-app-name");

        //when
        projectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then a log managed service should be bound to the app
        Assertions.assertThat(app.getServiceNames()).hasSize(1);
        Assertions.assertThat(app.getServiceNames()).containsOnly("my-app-name-log");

    }
}