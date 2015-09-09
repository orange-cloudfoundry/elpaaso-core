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
import com.francetelecom.clara.cloud.logicalmodel.LogicalRabbitService;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.InternalRabbitLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Set;

public class RabbitServiceProjectionRuleTest {

    @Test
    public void rabbit_service_instance_should_equal_to_logical_rabbitmq_service_name() throws Exception {
        //given
        final Space space = new Space();
        LogicalRabbitService logicalRabbitService = new LogicalRabbitService();
        logicalRabbitService.setServiceName("myRabbitService");
        final RabbitServiceProjectionRule rabbitServiceProjectionRule = new RabbitServiceProjectionRule();

        //when
        final ManagedService managedService = rabbitServiceProjectionRule.toRabbitService(logicalRabbitService, space);

        //then service instance name should equal to logical rabbitmq service name
        Assertions.assertThat(managedService.getServiceInstance()).isEqualTo("myRabbitService");
    }

    @Test
    public void rabbit_service_plan_should_be_standard() throws Exception {
        //given
        final Space space = new Space();
        LogicalRabbitService logicalRabbitService = new LogicalRabbitService();
        logicalRabbitService.setServiceName("myRabbitService");
        final RabbitServiceProjectionRule rabbitServiceProjectionRule = new RabbitServiceProjectionRule();

        //when
        final ManagedService managedService = rabbitServiceProjectionRule.toRabbitService(logicalRabbitService, space);

        //then service plan should be default
        Assertions.assertThat(managedService.getPlan()).isEqualTo("standard");
    }

    @Test
    public void rabbit_service_type_should_be_p_rabbitmq() throws Exception {
        //given
        final Space space = new Space();
        LogicalRabbitService logicalRabbitService = new LogicalRabbitService();
        logicalRabbitService.setServiceName("myRabbitService");
        final RabbitServiceProjectionRule rabbitServiceProjectionRule = new RabbitServiceProjectionRule();

        //when
        final ManagedService managedService = rabbitServiceProjectionRule.toRabbitService(logicalRabbitService, space);

        //then service type should be redis
        Assertions.assertThat(managedService.getService()).isEqualTo("p-rabbitmq");
    }

    @Test
    public void rabbitmq_service_projection_rule_should_generate_a_rabbit_managed_service_per_rabbitmq_logical_service() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space();
        final RabbitServiceProjectionRule rabbitServiceProjectionRule = new RabbitServiceProjectionRule();
        LogicalDeployment logicalDeployment = new LogicalDeployment();
        InternalRabbitLogicalModelCatalog logicalModelCatalog = new InternalRabbitLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        //when
        rabbitServiceProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then a rabbitmq managed service should have been generated in td
        final Set<ManagedService> actual = td.listXaasSubscriptionTemplates(ManagedService.class);
        Assertions.assertThat(actual).hasSize(2);
        final ManagedService managedService = actual.iterator().next();
        Assertions.assertThat(managedService.getService()).isEqualTo("p-rabbitmq");

    }

    @Test
    public void should_map_app_to_rabbitMQ_services() throws Exception {
        // given
        ServiceProjectionRule rabbitServiceProjectionRule = new RabbitServiceProjectionRule();

        LogicalDeployment logicalDeployment = new LogicalDeployment();
        InternalRabbitLogicalModelCatalog logicalModelCatalog = new InternalRabbitLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        // given td
        TechnicalDeployment td = new TechnicalDeployment("name");
        Space space = new Space();
        rabbitServiceProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));
        //"InternalRabbitJEE-Client" InternalRabbitJEE-Server

        //simulate app generation
        App client = new App(space, new MavenReference(), "client");
        client.setLogicalModelId(logicalDeployment.findProcessingNode("InternalRabbitJEE-Client").getName());
        td.add(client);
        App server = new App(space, new MavenReference(), "server");
        td.add(server);
        server.setLogicalModelId(logicalDeployment.findProcessingNode("InternalRabbitJEE-Server").getName());

        AssociationProjectionRule associationProjectionRule = new DefaultServiceBindingProjectionRule();

        associationProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then 2 rabbit services should be bound to the app
        Assertions.assertThat(client.getServiceNames()).hasSize(2);
        Assertions.assertThat(client.getServiceNames()).containsOnly("request", "response");

        //then 2 rabbit services should be bound to the app
        Assertions.assertThat(server.getServiceNames()).hasSize(2);
        Assertions.assertThat(server.getServiceNames()).containsOnly("request", "response");


    }
}