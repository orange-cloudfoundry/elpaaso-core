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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.CFServiceActivationService;
import com.francetelecom.clara.cloud.activation.plugin.cf.domain.ServiceActivationStatus;
import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.ManagedServiceRepository;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ManagedServiceActivationPluginTest {

    @Mock
    CFServiceActivationService cfServiceActivationService;
    @Mock
    ManageModelItem manageModelItem;
    @Mock
    ManagedServiceRepository managedServiceRepository;

    @Test
    public void fail_to_activate_managed_service_if_managed_service_does_not_exist() throws Exception {
        final ManagedServiceActivationPlugin managedServiceActivationPlugin = new ManagedServiceActivationPlugin(cfServiceActivationService, manageModelItem, managedServiceRepository);

        final TaskStatus status = managedServiceActivationPlugin.activate(99, ManagedService.class, new ActivationTestContext());

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
        Mockito.verify(cfServiceActivationService, Mockito.never()).delete(Mockito.any(ManagedService.class));
    }

    @Test
    public void fail_to_delete_managed_service_if_managed_service_does_not_exist() throws Exception {
        final ManagedServiceActivationPlugin managedServiceActivationPlugin = new ManagedServiceActivationPlugin(cfServiceActivationService, manageModelItem, managedServiceRepository);

        final TaskStatus status = managedServiceActivationPlugin.delete(99, ManagedService.class);

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
        Mockito.verify(cfServiceActivationService, Mockito.never()).delete(Mockito.any(ManagedService.class));
    }

    @Test
    public void should_return_deletion_status() throws Exception {
        //given service has been activated
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        ManagedService service = new ManagedService("rabbit", "rabbit", "rabbit", space, td);
        //given service exists
        Mockito.when(managedServiceRepository.findOne(1)).thenReturn(service);
        Mockito.when(cfServiceActivationService.delete(service)).thenReturn(ServiceActivationStatus.ofService("rabbit", space.getSpaceName().getValue()).isPending("in progress"));
        //and has been activated
        service.activate();

        final ManagedServiceActivationPlugin managedServiceActivationPlugin = new ManagedServiceActivationPlugin(cfServiceActivationService, manageModelItem, managedServiceRepository);
        final TaskStatus status = managedServiceActivationPlugin.delete(1, ManagedService.class);

        Assertions.assertThat(status.isStarted()).isEqualTo(true);
        Assertions.assertThat(status.getTitle()).isEqualTo("in progress");
    }

    @Test
    public void should_return_activation_status() throws Exception {
        //given service has been activated
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        ManagedService service = new ManagedService("rabbit", "rabbit", "rabbit", space, td);
        //given service exists
        Mockito.when(managedServiceRepository.findOne(1)).thenReturn(service);
        Mockito.when(cfServiceActivationService.activate(service)).thenReturn(ServiceActivationStatus.ofService("rabbit", space.getSpaceName().getValue()).isPending("in progress"));

        final ManagedServiceActivationPlugin managedServiceActivationPlugin = new ManagedServiceActivationPlugin(cfServiceActivationService, manageModelItem, managedServiceRepository);
        final TaskStatus status = managedServiceActivationPlugin.activate(1, ManagedService.class, new ActivationTestContext());

        Assertions.assertThat(status.isStarted()).isEqualTo(true);
        Assertions.assertThat(status.getTitle()).isEqualTo("in progress");
    }
}