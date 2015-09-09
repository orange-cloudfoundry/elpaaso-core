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
package com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure;

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.AppActivationService;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class AppActivationServiceDefaultImplTest {

    @Mock
    CfAdapter cfAdapter;

    @After
    public void teardown() {
        Mockito.reset(cfAdapter);
    }

    @Test
    public void should_not_start_app_if_app_is_already_started() {
        AppActivationService appActivationService = new AppActivationServiceDefaultImpl(cfAdapter);

        // given application joyns
        Space space = new Space();
        space.activate(new SpaceName("joynspace"));

        App joyn = new App(space, Mockito.mock(MavenReference.class), "joyn");
        // given application joyn is already started
        Mockito.when(cfAdapter.isAppStarted("joyn", "joynspace")).thenReturn(true);

        TaskStatus status = appActivationService.start(joyn);

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
        Mockito.verify(cfAdapter, Mockito.never()).startApp(joyn, "joynspace");
    }

    @Test
    public void should_start_app_if_app_is_not_started() {
        AppActivationService appActivationService = new AppActivationServiceDefaultImpl(cfAdapter);

        // given application joyn
        Space space = new Space();
        space.activate(new SpaceName("joynspace"));
        App joyn = new App(space, Mockito.mock(MavenReference.class), "joyn");
        // given application joyn is already started
        Mockito.when(cfAdapter.isAppStarted("joyn", "joynspace")).thenReturn(false);

        TaskStatus status = appActivationService.start(joyn);

        Assertions.assertThat(status.isComplete()).isEqualTo(false);
        Mockito.verify(cfAdapter).startApp(joyn, "joynspace");
    }

    @Test
    public void should_not_stop_app_if_app_is_already_stopped() {
        AppActivationService appActivationService = new AppActivationServiceDefaultImpl(cfAdapter);

        // given application joyn
        App joyn = new App(new Space(), Mockito.mock(MavenReference.class), "joyn");
        // given application joyn is already stopped
        Mockito.when(cfAdapter.isAppStopped("joyn", "joynspace")).thenReturn(true);

        appActivationService.stop(joyn);

        Mockito.verify(cfAdapter, Mockito.never()).stopApp(joyn, "joynspace");
    }

    @Test
    public void should_stop_app_if_app_is_not_stopped() {
        AppActivationService appActivationService = new AppActivationServiceDefaultImpl(cfAdapter);

        // given application joyn
        Space space = new Space();
        space.activate(new SpaceName("joynspace"));
        App joyn = new App(space, Mockito.mock(MavenReference.class), "joyn");
        // given application joyn is already stopped
        Mockito.when(cfAdapter.isAppStopped("joyn", "joynspace")).thenReturn(false);

        appActivationService.stop(joyn);

        Mockito.verify(cfAdapter).stopApp(joyn, "joynspace");
    }

    @Test
    public void should_not_delete_app_if_app_has_not_been_activated_yet() {
        AppActivationService appActivationService = new AppActivationServiceDefaultImpl(cfAdapter);

        // given application joyn that has not been activated yet
        App joyn = new App(new Space(), Mockito.mock(MavenReference.class), "joyn");

        appActivationService.delete(joyn);

        Mockito.verify(cfAdapter, Mockito.never()).deleteApp(Mockito.any(App.class), Mockito.anyString());
    }

    @Test
    public void should_not_delete_app_if_app_is_already_deleted() {
        AppActivationService appActivationService = new AppActivationServiceDefaultImpl(cfAdapter);

        // given application joyn
        App joyn = new App(new Space(), Mockito.mock(MavenReference.class), "joyn");
        // given application joyn is already deleted
        Mockito.when(cfAdapter.appExists("joyn", "joynspace")).thenReturn(false);

        appActivationService.delete(joyn);

        Mockito.verify(cfAdapter, Mockito.never()).deleteApp(joyn, "joynspace");
    }

    @Test
    public void should_delete_app_if_app_is_not_deleted() {
        AppActivationService appActivationService = new AppActivationServiceDefaultImpl(cfAdapter);

        // given application joyn
        Space space = new Space();
        space.activate(new SpaceName("joynspace"));

        App joyn = new App(space, Mockito.mock(MavenReference.class), "joyn");
        joyn.activate(UUID.randomUUID());

        // given application joyn exists
        Mockito.when(cfAdapter.appExists("joyn", "joynspace")).thenReturn(true);

        appActivationService.delete(joyn);

        Mockito.verify(cfAdapter).deleteApp(joyn, "joynspace");
    }

    @Test
    public void app_should_be_ACTIVATED_after_activation() {

        // given application joyns
        final Space space = new Space();
        App joyn = new App(space, Mockito.mock(MavenReference.class), "joyn");
        Mockito.when(cfAdapter.createApp(joyn, space.getSpaceName().getValue())).thenReturn(UUID.randomUUID());
        AppActivationService appActivationService = new AppActivationServiceDefaultImpl(cfAdapter);

        appActivationService.activate(joyn);

    }

}
