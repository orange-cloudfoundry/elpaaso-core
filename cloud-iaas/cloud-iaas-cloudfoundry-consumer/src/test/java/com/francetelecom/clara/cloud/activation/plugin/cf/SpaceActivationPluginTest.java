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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.SpaceActivationService;
import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceRepository;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SpaceActivationPluginTest {

    @Mock
    SpaceActivationService spaceActivationService;
    @Mock
    ManageModelItem manageModelItem;
    @Mock
    SpaceRepository spaceRepository;

    @Test
    public void fail_to_activate_space_if_space_does_not_exist() throws Exception {
        final SpaceActivationPlugin spaceActivationPlugin = new SpaceActivationPlugin(spaceActivationService, manageModelItem, spaceRepository);

        final TaskStatus status = spaceActivationPlugin.activate(99, Space.class, new ActivationTestContext());

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
        Mockito.verify(spaceActivationService, Mockito.never()).delete(Mockito.any(SpaceName.class));

    }

    @Test
    public void fail_to_delete_space_if_space_does_not_exist() throws Exception {
        final SpaceActivationPlugin spaceActivationPlugin = new SpaceActivationPlugin(spaceActivationService, manageModelItem, spaceRepository);

        final TaskStatus status = spaceActivationPlugin.delete(99, Space.class);

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
        Mockito.verify(spaceActivationService, Mockito.never()).delete(Mockito.any(SpaceName.class));
    }

    @Test
    public void should_not_delete_space_if_space_has_not_been_activated_yet() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        Mockito.when(spaceRepository.findOne(1)).thenReturn(space);

        final SpaceActivationPlugin spaceActivationPlugin = new SpaceActivationPlugin(spaceActivationService, manageModelItem, spaceRepository);
        final TaskStatus status = spaceActivationPlugin.delete(1, Space.class);

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
        Mockito.verify(spaceActivationService, Mockito.never()).delete(Mockito.any(SpaceName.class));
    }

    @Test
    public void should_return_success_task_on_activation_success() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        //given space exists
        Mockito.when(spaceRepository.findOne(1)).thenReturn(space);
        Mockito.when(spaceActivationService.activate(Mockito.anyString())).thenReturn(new SpaceName("test"));

        final SpaceActivationPlugin spaceActivationPlugin = new SpaceActivationPlugin(spaceActivationService, manageModelItem, spaceRepository);
        final TaskStatus status = spaceActivationPlugin.activate(1, Space.class, new ActivationTestContext());

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
        Assertions.assertThat(status.getTitle()).isEqualTo("Space <test> has been activated.");
    }

    @Test
    public void should_return_success_task_on_deletion_success() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        //given space exists
        Mockito.when(spaceRepository.findOne(1)).thenReturn(space);
        //and has been activated
        space.activate(new SpaceName("test"));

        final SpaceActivationPlugin spaceActivationPlugin = new SpaceActivationPlugin(spaceActivationService, manageModelItem, spaceRepository);
        final TaskStatus status = spaceActivationPlugin.delete(1, Space.class);

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
        Assertions.assertThat(status.getTitle()).isEqualTo("Space <test> has been deleted.");
    }
}