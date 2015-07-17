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
import com.francetelecom.clara.cloud.logicalmodel.LogicalRabbitService;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;

import java.util.Set;

public class RabbitServiceProjectionRule implements ServiceProjectionRule {

    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
        // generate services
        Set<LogicalRabbitService> logicalRabbitServices = ld.listLogicalServices(LogicalRabbitService.class);
        for (LogicalRabbitService logicalRabbitService : logicalRabbitServices) {
            // generate cloud foundry service
            toRabbitService(logicalRabbitService, projectionContext.getSpace(), td);
        }

    }

    protected ManagedService toRabbitService(LogicalRabbitService logicalRabbitService, Space space, TechnicalDeployment td) {
        ManagedService rabbitService = new ManagedService("p-rabbitmq", "standard", logicalRabbitService.getServiceName(), space, td);
        rabbitService.setLogicalModelId(logicalRabbitService.getName());
        return rabbitService;
    }

}
