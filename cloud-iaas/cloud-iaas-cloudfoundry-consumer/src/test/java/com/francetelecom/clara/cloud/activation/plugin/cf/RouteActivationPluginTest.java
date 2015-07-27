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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.RouteActivationService;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.model.ModelItemRepository;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.RouteRepository;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RouteActivationPluginTest {

    @Mock
    RouteActivationService routeActivationService;
    @Mock
    ModelItemRepository modelItemRepository;
    @Mock
    RouteRepository routeRepository;

    @Test
    public void fail_to_activate_route_if_route_does_not_exist() throws Exception {

        final RouteActivationPlugin routeActivationPlugin = new RouteActivationPlugin(routeActivationService, modelItemRepository, routeRepository);
        final TaskStatus status = routeActivationPlugin.activate(99, Route.class, new ActivationTestContext());

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
        Mockito.verify(routeActivationService, Mockito.never()).activate(Mockito.any(Route.class));
    }

    @Test
    public void fail_to_delete_route_if_route_does_not_exist() throws Exception {

        final RouteActivationPlugin routeActivationPlugin = new RouteActivationPlugin(routeActivationService, modelItemRepository, routeRepository);
        final TaskStatus status = routeActivationPlugin.delete(99, Route.class);

        Assertions.assertThat(status.hasFailed()).isEqualTo(true);
        Mockito.verify(routeActivationService, Mockito.never()).activate(Mockito.any(Route.class));
    }

    @Test
    public void should_not_delete_route_if_route_has_not_been_activated_yet() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        Route route = new Route(new RouteUri("host1.mysubdomain.cfapps.redacted-domain.org"), "root1", space, td);
        Mockito.when(routeRepository.findOne(1)).thenReturn(route);

        final RouteActivationPlugin routeActivationPlugin = new RouteActivationPlugin(routeActivationService, modelItemRepository, routeRepository);
        final TaskStatus status = routeActivationPlugin.delete(1, Route.class);

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
        Mockito.verify(routeActivationService, Mockito.never()).delete(route);
    }

    @Test
    public void should_return_success_task_on_deletion_success() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        Route route = new Route(new RouteUri("host1.mysubdomain.cfapps.redacted-domain.org"), "root1", space, td);
        Mockito.when(routeRepository.findOne(1)).thenReturn(route);

        final RouteActivationPlugin routeActivationPlugin = new RouteActivationPlugin(routeActivationService, modelItemRepository, routeRepository);
        final TaskStatus status = routeActivationPlugin.delete(1, Route.class);

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
        Assertions.assertThat(status.getTitle()).isEqualTo("Route <host1.mysubdomain.cfapps.redacted-domain.org> has been deleted.");
    }

    @Test
    public void should_return_success_task_on_activation_success() throws Exception {
        TechnicalDeployment td = new TechnicalDeployment("depl");
        Space space = new Space(td);
        Route route = new Route(new RouteUri("host1.mysubdomain.cfapps.redacted-domain.org"), "root1", space, td);
        Mockito.when(routeRepository.findOne(1)).thenReturn(route);
        Mockito.when(routeActivationService.activate(Mockito.any(Route.class))).thenReturn(new RouteUri("host1.mysubdomain.cfapps.redacted-domain.org"));


        final RouteActivationPlugin routeActivationPlugin = new RouteActivationPlugin(routeActivationService, modelItemRepository, routeRepository);
        final TaskStatus status = routeActivationPlugin.activate(1, Route.class, new ActivationTestContext());

        Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
        Assertions.assertThat(status.getTitle()).isEqualTo("Route <host1.mysubdomain.cfapps.redacted-domain.org> has been activated.");
    }
}