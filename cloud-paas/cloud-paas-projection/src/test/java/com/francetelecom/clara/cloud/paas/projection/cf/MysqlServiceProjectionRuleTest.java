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
import com.francetelecom.clara.cloud.logicalmodel.LogicalMysqlService;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.CFWicketJpaWithMysqlServiceLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Set;

public class MysqlServiceProjectionRuleTest {

    @Test
    public void mysql_service_instance_should_equal_to_logical_mysql_service_name() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        LogicalMysqlService logicalMysqlService = new LogicalMysqlService();
        logicalMysqlService.setServiceName("mysql-database");
        final MysqlServiceProjectionRule mysqlServiceProjectionRule = new MysqlServiceProjectionRule();

        //when
        final ManagedService managedService = mysqlServiceProjectionRule.toMysqlService(logicalMysqlService, space, td);

        //then service instance name should equal to logical mysql service name
        Assertions.assertThat(managedService.getServiceInstance()).isEqualTo("mysql-database");
    }

    @Test
    public void mysql_service_plan_should_be_standard() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        LogicalMysqlService logicalMysqlService = new LogicalMysqlService();
        logicalMysqlService.setServiceName("myMysqlService");
        final MysqlServiceProjectionRule mysqlServiceProjectionRule = new MysqlServiceProjectionRule();

        //when
        final ManagedService managedService = mysqlServiceProjectionRule.toMysqlService(logicalMysqlService, space, td);

        //then service plan should be default
        Assertions.assertThat(managedService.getPlan()).isEqualTo("100mb");
    }

    @Test
    public void mysql_service_type_should_be_p_mysql() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        LogicalMysqlService logicalMysqlService = new LogicalMysqlService();
        logicalMysqlService.setServiceName("myMysqlService");
        final MysqlServiceProjectionRule mysqlServiceProjectionRule = new MysqlServiceProjectionRule();

        //when
        final ManagedService managedService = mysqlServiceProjectionRule.toMysqlService(logicalMysqlService, space, td);

        //then service type should be redis
        Assertions.assertThat(managedService.getService()).isEqualTo("p-mysql");
    }

    @Test
    public void mysql_service_projection_rule_should_generate_a_mysql_managed_service_per_mysql_logical_service() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final MysqlServiceProjectionRule mysqlServiceProjectionRule = new MysqlServiceProjectionRule();
        LogicalDeployment logicalDeployment = new LogicalDeployment();
        CFWicketJpaWithMysqlServiceLogicalModelCatalog logicalModelCatalog = new CFWicketJpaWithMysqlServiceLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        //when
        mysqlServiceProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then a mysql managed service should have been generated in td
        final Set<ManagedService> actual = td.listXaasSubscriptionTemplates(ManagedService.class);
        Assertions.assertThat(actual).hasSize(1);
        final ManagedService managedService = actual.iterator().next();
        Assertions.assertThat(managedService.getService()).isEqualTo("p-mysql");

    }

    @Test
    public void should_map_app_to_Mysql_services() throws Exception {
        // given
        ServiceProjectionRule mysqlServiceProjectionRule = new MysqlServiceProjectionRule();

        LogicalDeployment logicalDeployment = new LogicalDeployment();
        CFWicketJpaWithMysqlServiceLogicalModelCatalog logicalModelCatalog = new CFWicketJpaWithMysqlServiceLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        // given td
        TechnicalDeployment td = new TechnicalDeployment("name");
        Space space = new Space(td);
        mysqlServiceProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //simulate app generation
        App app = new App(td, space, new MavenReference(), "app");
        app.setLogicalModelId(logicalDeployment.findProcessingNode("Cf-wicket-jpaSample").getName());

        AssociationProjectionRule associationProjectionRule = new DefaultServiceBindingProjectionRule();

        associationProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then a mysql service should be bound to the app
        Assertions.assertThat(app.getServiceNames()).hasSize(1);
        Assertions.assertThat(app.getServiceNames()).containsOnly("mysql-db");


    }
}