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
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.CFWicketCxfJpaLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Set;

/**
 * Created by sbortolussi on 03/06/2015.
 */
public class ToDbaasServiceProjectionRuleTest {


    @Test
    public void dbaas_service_instance_should_equal_to_logical_mysql_service_name() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        LogicalRelationalService logicalRelationalService = new LogicalRelationalService();
        logicalRelationalService.setServiceName("mysql-database");
        final ToDbaasServiceProjectionRule rule = new ToDbaasServiceProjectionRule();

        //when
        final ManagedService managedService = rule.toDbaasService(logicalRelationalService, space, td);

        //then service instance name should equal to logical relational service name
        Assertions.assertThat(managedService.getServiceInstance()).isEqualTo("mysql-database");
    }

    @Test
    public void dbaas_service_plan_should_be_MYSQL_1G_when_a_mysql_db_is_requested() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        LogicalRelationalService logicalRelationalService = new LogicalRelationalService();
        logicalRelationalService.setServiceName("mysql-database");
        logicalRelationalService.setSqlVersion(LogicalRelationalServiceSqlDialectEnum.MYSQL_DEFAULT);
        final ToDbaasServiceProjectionRule rule = new ToDbaasServiceProjectionRule();

        //when
        final ManagedService managedService = rule.toDbaasService(logicalRelationalService, space, td);

        //then service plan should be MYSQL_1G
        Assertions.assertThat(managedService.getPlan()).isEqualTo("MYSQL_1G");
    }

    @Test
    public void dbaas_service_plan_should_be_POSTGRESQL_1G_when_a_postgres_db_is_requested() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        LogicalRelationalService logicalRelationalService = new LogicalRelationalService();
        logicalRelationalService.setServiceName("postgres-database");
        logicalRelationalService.setSqlVersion(LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT);
        final ToDbaasServiceProjectionRule rule = new ToDbaasServiceProjectionRule();

        //when
        final ManagedService managedService = rule.toDbaasService(logicalRelationalService, space, td);

        //then service plan should be POSTGRESQL_1G
        Assertions.assertThat(managedService.getPlan()).isEqualTo("POSTGRESQL_1G");
    }

    @Test
    public void dbaas_service_type_should_be_o_dbaas() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        LogicalRelationalService logicalRelationalService = new LogicalRelationalService();
        logicalRelationalService.setServiceName("mysql-database");
        final ToDbaasServiceProjectionRule rule = new ToDbaasServiceProjectionRule();

        //when
        final ManagedService managedService = rule.toDbaasService(logicalRelationalService, space, td);

        //then service type should be o-dbaas
        Assertions.assertThat(managedService.getService()).isEqualTo("o-dbaas");
    }

    @Test
    public void to_dbaas_service_projection_rule_should_generate_a_dbaas_managed_service_per_mysql_logical_service() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space(td);
        final ToDbaasServiceProjectionRule rule = new ToDbaasServiceProjectionRule();
        LogicalDeployment logicalDeployment = new LogicalDeployment();
        CFWicketCxfJpaLogicalModelCatalog logicalModelCatalog = new CFWicketCxfJpaLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        //when
        rule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then a dbaas managed service should have been generated in td
        final Set<ManagedService> actual = td.listXaasSubscriptionTemplates(ManagedService.class);
        Assertions.assertThat(actual).hasSize(1);
        final ManagedService managedService = actual.iterator().next();
        Assertions.assertThat(managedService.getService()).isEqualTo("o-dbaas");

    }

    @Test
    public void should_map_app_to_dbaas_services() throws Exception {
        // given
        final ToDbaasServiceProjectionRule rule = new ToDbaasServiceProjectionRule();

        LogicalDeployment logicalDeployment = new LogicalDeployment();
        CFWicketCxfJpaLogicalModelCatalog logicalModelCatalog = new CFWicketCxfJpaLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        // given td
        TechnicalDeployment td = new TechnicalDeployment("name");
        Space space = new Space(td);
        rule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //simulate app generation
        App app = new App(td, space, new MavenReference(), "app");
        app.setLogicalModelId(logicalDeployment.findProcessingNode("Cf-wicket-jpaSample").getName());

        AssociationProjectionRule associationProjectionRule = new DefaultServiceBindingProjectionRule();

        associationProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then a dbaas managed service should be bound to the app
        Assertions.assertThat(app.getServiceNames()).hasSize(1);
        Assertions.assertThat(app.getServiceNames()).containsOnly("postgres-db");


    }

}