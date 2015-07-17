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
package com.francetelecom.clara.cloud.activation.plugin.cf;

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.OrganizationActivationService;
import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Organization;
import com.francetelecom.clara.cloud.techmodel.cf.OrganizationRepository;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class OrganizationActivationPluginTest {

    @Mock
    OrganizationActivationService organizationActivationService;
    @Mock
    ManageModelItem manageModelItem;
    @Mock
    OrganizationRepository organizationRepository;

    @Test
    public void fail_to_activate_org_if_organization_does_not_exist() throws Exception {
        final OrganizationActivationPlugin organizationActivationPlugin = new OrganizationActivationPlugin(organizationActivationService, manageModelItem, organizationRepository);

        final TaskStatus status = organizationActivationPlugin.activate(99, Organization.class, new ActivationTestContext());

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);

    }

    @Test
    public void should_return_success_task_on_activation_success() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Organization organization = new Organization(td);
        //given space exists
        Mockito.when(organizationRepository.findOne(1)).thenReturn(organization);
        Mockito.when(organizationActivationService.activate(Mockito.anyString())).thenReturn("test");
        Mockito.when(organizationActivationService.getCurrentOrganizationName()).thenReturn("aTestOrg");

        final OrganizationActivationPlugin organizationActivationPlugin = new OrganizationActivationPlugin(organizationActivationService, manageModelItem, organizationRepository);
        final TaskStatus status = organizationActivationPlugin.activate(1, Organization.class, new ActivationTestContext());

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
        Assertions.assertThat(status.getTitle()).isEqualTo("Organization <aTestOrg> has been activated.");
    }

}