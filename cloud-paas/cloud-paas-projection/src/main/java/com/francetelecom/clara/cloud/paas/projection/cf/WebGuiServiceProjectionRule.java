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
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class WebGuiServiceProjectionRule implements ServiceProjectionRule {

    RouteStrategy routeStrategy;

    public void setRouteStrategy(RouteStrategy routeStrategy) {
        this.routeStrategy = routeStrategy;
    }

    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
        Set<LogicalWebGUIService> logicalWebGUIServices = ld.listLogicalServices(LogicalWebGUIService.class);
        for (LogicalWebGUIService webGUIService : logicalWebGUIServices) {
            toRoute(webGUIService, projectionContext, projectionContext.getSpace(), td);
        }
    }

    protected Route toRoute(LogicalWebGUIService webGUIService, ProjectionContext projectionContext, Space space, TechnicalDeployment td) {
        String uriTemplate = routeStrategy.buildRouteTemplate(projectionContext.getApplicationName(), projectionContext.getReleaseVersion(), webGUIService);
        Route route = new Route(new RouteUri(uriTemplate), webGUIService.getContextRoot().getValue(), space, td);
        route.setLogicalModelId(webGUIService.getName());
        return route;
    }
}
