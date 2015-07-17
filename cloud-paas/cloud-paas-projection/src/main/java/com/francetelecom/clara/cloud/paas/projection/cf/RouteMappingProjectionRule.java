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
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Route;

import java.util.Set;

public class RouteMappingProjectionRule implements AssociationProjectionRule {

    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
        // list logical web guiservices
        Set<LogicalWebGUIService> logicalWebGUIServices = ld.listLogicalServices(LogicalWebGUIService.class);
        for (LogicalWebGUIService logicalWebGUIService : logicalWebGUIServices) {
            //list processing nodes associated to the logical web gui service
            Set<ProcessingNode> processingNodes = logicalWebGUIService.listDependentProcessingNodes();
            for (ProcessingNode processingNode : processingNodes) {
                //get associated app
                Set<App> apps = td.listXaasSubscriptionTemplates(App.class, processingNode.getName());
                for (App app : apps) {
                    // get associated routes
                    Set<Route> routes = td.listXaasSubscriptionTemplates(Route.class, logicalWebGUIService.getName());
                    for (Route route : routes) {
                        app.mapRoute(route);
                    }
                }

            }
        }

    }

}
