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

import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.CFWicketCxfJpaLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Set;

public class StatefulServiceProjectionRuleTest {

    @Test
    public void log_service_instance_should_end_with_session_replication() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final LogicalWebGUIService logicalWebGUIService = new LogicalWebGUIService();
        logicalWebGUIService.setLabel("joyn-cfjavaprocessing");

        final StatefulServiceProjectionRule statefulServiceProjectionRule = new StatefulServiceProjectionRule();

        //when
        final ManagedService managedService = statefulServiceProjectionRule.toSessionReplicationService(logicalWebGUIService, space, td);

        //then service instance name should end with -session-replication
        Assertions.assertThat(managedService.getServiceInstance()).endsWith("-session-replication");
    }

    @Test
    public void log_service_instance_should_start_with_cf_java_processing_label() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final LogicalWebGUIService logicalWebGUIService = new LogicalWebGUIService();
        logicalWebGUIService.setLabel("joyn-cfjavaprocessing");
        final StatefulServiceProjectionRule statefulServiceProjectionRule = new StatefulServiceProjectionRule();

        //when
        final ManagedService managedService = statefulServiceProjectionRule.toSessionReplicationService(logicalWebGUIService, space, td);

        //then service instance name should start with cf_java_processing_label
        Assertions.assertThat(managedService.getServiceInstance()).startsWith("joyn-cfjavaprocessing");
    }

    @Test
    public void log_service_plan_should_be_default() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final LogicalWebGUIService logicalWebGUIService = new LogicalWebGUIService();
        logicalWebGUIService.setLabel("joyn-cfjavaprocessing");
        final StatefulServiceProjectionRule statefulServiceProjectionRule = new StatefulServiceProjectionRule();

        //when
        final ManagedService managedService = statefulServiceProjectionRule.toSessionReplicationService(logicalWebGUIService, space, td);

        //then service plan should be default
        Assertions.assertThat(managedService.getPlan()).isEqualTo("shared-vm");
    }

    @Test
    public void log_service_type_should_be_redis() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final LogicalWebGUIService logicalWebGUIService = new LogicalWebGUIService();
        logicalWebGUIService.setLabel("joyn-cfjavaprocessing");
        final StatefulServiceProjectionRule statefulServiceProjectionRule = new StatefulServiceProjectionRule();

        //when
        final ManagedService managedService = statefulServiceProjectionRule.toSessionReplicationService(logicalWebGUIService, space, td);

        //then service type should be redis
        Assertions.assertThat(managedService.getService()).isEqualTo("p-redis");
    }

    @Test
    public void log_service_projection_rule_should_generate_a_redis_managed_service_per_cf_java_processing() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final StatefulServiceProjectionRule statefulServiceProjectionRule = new StatefulServiceProjectionRule();
        LogicalDeployment logicalDeployment = new LogicalDeployment();
        CFWicketCxfJpaLogicalModelCatalog logicalModelCatalog = new CFWicketCxfJpaLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        //when
        statefulServiceProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then a redis managed service should have been generated in td
        final Set<ManagedService> actual = td.listXaasSubscriptionTemplates(ManagedService.class);
        Assertions.assertThat(actual).hasSize(1);
        final ManagedService managedService = actual.iterator().next();
        Assertions.assertThat(managedService.getService()).isEqualTo("p-redis");

    }

}