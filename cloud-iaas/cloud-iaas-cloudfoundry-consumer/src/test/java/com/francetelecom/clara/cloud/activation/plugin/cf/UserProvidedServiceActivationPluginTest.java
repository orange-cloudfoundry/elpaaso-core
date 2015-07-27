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
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.model.ModelItemRepository;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.UserProvidedServiceRepository;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.AbstractUserProvidedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.SimpleUserProvidedService;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserProvidedServiceActivationPluginTest {

    @Mock
    CFServiceActivationService cfServiceActivationService;
    @Mock
    ModelItemRepository modelItemRepository;
    @Mock
    UserProvidedServiceRepository userProvidedServiceRepository;

    @Test
    public void fail_to_activate_ups_if_ups_does_not_exist() throws Exception {
        //given service does not exist

        final UserProvidedServiceActivationPlugin userProvidedServiceActivationPlugin = new UserProvidedServiceActivationPlugin(cfServiceActivationService, modelItemRepository, userProvidedServiceRepository);
        final TaskStatus status = userProvidedServiceActivationPlugin.activate(99, AbstractUserProvidedService.class, new ActivationTestContext());

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
        Mockito.verify(cfServiceActivationService, Mockito.never()).delete(Mockito.any(AbstractUserProvidedService.class));
    }

    @Test
    public void fail_to_delete_ups_if_ups_does_not_exist() throws Exception {
        //given service does not exist

        final UserProvidedServiceActivationPlugin userProvidedServiceActivationPlugin = new UserProvidedServiceActivationPlugin(cfServiceActivationService, modelItemRepository, userProvidedServiceRepository);
        final TaskStatus status = userProvidedServiceActivationPlugin.delete(99, AbstractUserProvidedService.class);

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
        Mockito.verify(cfServiceActivationService, Mockito.never()).delete(Mockito.any(AbstractUserProvidedService.class));
    }

    @Test
    public void should_return_activation_status() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        SimpleUserProvidedService service = new SimpleUserProvidedService("postgres-joyndb", "postgres://user:password@hostname:1234/joyndb", td, space);
        //given service exists
        Mockito.when(userProvidedServiceRepository.findOne(1)).thenReturn(service);
        Mockito.when(cfServiceActivationService.activate(service)).thenReturn(ServiceActivationStatus.ofService("postgres-joyndb", space.getSpaceName().getValue()).hasSucceeded());


        final UserProvidedServiceActivationPlugin userProvidedServiceActivationPlugin = new UserProvidedServiceActivationPlugin(cfServiceActivationService, modelItemRepository, userProvidedServiceRepository);
        final TaskStatus status = userProvidedServiceActivationPlugin.activate(1, AbstractUserProvidedService.class, new ActivationTestContext());

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
    }

    @Test
    public void should_return_deletion_status() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        SimpleUserProvidedService service = new SimpleUserProvidedService("postgres-joyndb", "postgres://user:password@hostname:1234/joyndb", td, space);
        //given service exists
        Mockito.when(userProvidedServiceRepository.findOne(1)).thenReturn(service);
        Mockito.when(cfServiceActivationService.delete(service)).thenReturn(ServiceActivationStatus.ofService("postgres-joyndb", space.getSpaceName().getValue()).hasSucceeded());

        //and has been activated
        service.activate();

        final UserProvidedServiceActivationPlugin userProvidedServiceActivationPlugin = new UserProvidedServiceActivationPlugin(cfServiceActivationService, modelItemRepository, userProvidedServiceRepository);
        final TaskStatus status = userProvidedServiceActivationPlugin.delete(1, AbstractUserProvidedService.class);

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
    }

}