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
package com.francetelecom.clara.cloud.paas.activation.v1;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.techmodel.cf.*;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * ActivitiProcessUtilsTest
 * <p/>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class ActivitiProcessUtilsTest {
    private static Logger logger = LoggerFactory.getLogger(ActivitiProcessUtilsTest.class.getName());

    @Test
    public void add_service_should_construct_node_dependencies_tree() {
        //Given
        Map<String, ActivitiProcessUtils.NodeTask> nodes = new HashMap<>();
        Map<String, Set<ActivitiProcessUtils.NodeTask>> cache = new HashMap<>();

        TechnicalDeployment td = new TechnicalDeployment("call_to_addService_checkAwsUseCase");
        MavenReference mavenRef = mock(MavenReference.class);
        List<DependantModelItem> tdItems = new ArrayList<>();

        final Organization organization = new Organization(td);
        tdItems.add(organization);

        final Space space = new Space(td, organization);
        tdItems.add(space);

        final App app = new App(td, space, mavenRef, "appName");
        tdItems.add(app);

        final ManagedService mysql_service = new ManagedService("o-dbaas", "MYSQL_1G", "appName-db", space, td);
        tdItems.add(mysql_service);

        final ManagedService splunk_service = new ManagedService("o-logs", "splunk", "appName-log", space, td);
        tdItems.add(splunk_service);

        final Route route1 = new Route(new RouteUri("uri1"), null, space, td);
        tdItems.add(route1);

        boolean canParrallel = false;
        ActivationStepEnum activateStep = ActivationStepEnum.ACTIVATE;
        ActivationPluginStrategy pluginStrategy = mock(ActivationPluginStrategy.class);
        ActivationPlugin mockPlugin = mock(ActivationPlugin.class);
        when(mockPlugin.toString())
                .thenReturn("mockPlugin");
        when(pluginStrategy.getPlugin(any(Class.class), any(ActivationStepEnum.class)))
                .thenReturn(mockPlugin);

        //When
        for (DependantModelItem item : tdItems) {
            ActivitiProcessUtils.addService(nodes, cache, item, canParrallel, activateStep, pluginStrategy);
        }

        //Then
        assertThat(nodes).hasSize(tdItems.size());
        ActivitiProcessUtils.NodeTask first = ActivitiProcessUtils.findFirst(nodes);
        assertThat(first.item).isEqualTo(organization);

        // for debug purpose
        ActivitiProcessUtils.logSequence(nodes, activateStep);
    }
}
