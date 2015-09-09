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
import com.francetelecom.clara.cloud.logicalmodel.LogicalOnlineStorageService;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.StoragesLogicalModelCatalog;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.fest.assertions.Assertions;
import org.junit.Test;

import java.util.Set;

public class StoreServiceProjectionRuleTest {

    @Test
    public void riacks_service_instance_should_equal_to_logical_logical_online_service_name() throws Exception {
        //given
        final Space space = new Space();
        LogicalOnlineStorageService logicalOnlineStorageService = new LogicalOnlineStorageService();
        logicalOnlineStorageService.setServiceName("myStorageService");
        final StoreServiceProjectionRule storeServiceProjectionRule = new StoreServiceProjectionRule();

        //when
        final ManagedService managedService = storeServiceProjectionRule.toRiakcsService(logicalOnlineStorageService, space);

        //then service instance name should equal to logical storage service name
        Assertions.assertThat(managedService.getServiceInstance()).isEqualTo("myStorageService");
    }

    @Test
    public void riacks_service_plan_should_be_developer() throws Exception {
        //given
        final Space space = new Space();
        LogicalOnlineStorageService logicalOnlineStorageService = new LogicalOnlineStorageService();
        logicalOnlineStorageService.setServiceName("myStorageService");
        final StoreServiceProjectionRule storeServiceProjectionRule = new StoreServiceProjectionRule();

        //when
        final ManagedService managedService = storeServiceProjectionRule.toRiakcsService(logicalOnlineStorageService, space);

        //then service plan should be developer
        Assertions.assertThat(managedService.getPlan()).isEqualTo("developer");
    }

    @Test
    public void riacks_service_type_should_be_p_riakcs() throws Exception {
        //given
        final Space space = new Space();
        LogicalOnlineStorageService logicalOnlineStorageService = new LogicalOnlineStorageService();
        logicalOnlineStorageService.setServiceName("myStorageService");
        final StoreServiceProjectionRule storeServiceProjectionRule = new StoreServiceProjectionRule();

        //when
        final ManagedService managedService = storeServiceProjectionRule.toRiakcsService(logicalOnlineStorageService, space);

        //then service type should be p-riakcs
        Assertions.assertThat(managedService.getService()).isEqualTo("p-riakcs");
    }

    @Test
    public void store_service_projection_rule_should_generate_a_riakcs_managed_service_per_logical_online_storage_service() throws Exception {
        //given
        final TechnicalDeployment td = new TechnicalDeployment("");
        final Space space = new Space();
        final StoreServiceProjectionRule storeServiceProjectionRule = new StoreServiceProjectionRule();
        LogicalDeployment logicalDeployment = new LogicalDeployment();
        StoragesLogicalModelCatalog logicalModelCatalog = new StoragesLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);

        //when
        storeServiceProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then a riakcs managed service should have been generated in td
        final Set<ManagedService> actual = td.listXaasSubscriptionTemplates(ManagedService.class);
        Assertions.assertThat(actual).hasSize(1);
        final ManagedService managedService = actual.iterator().next();
        Assertions.assertThat(managedService.getService()).isEqualTo("p-riakcs");

    }

}