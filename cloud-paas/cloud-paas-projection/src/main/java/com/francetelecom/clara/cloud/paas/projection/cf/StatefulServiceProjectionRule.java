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
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.ServiceNameBuilder;

import java.util.Set;

public class StatefulServiceProjectionRule implements ServiceProjectionRule {

    public static final String REDIS_SERVICE_NAME = "p-redis";
    public static final String REDIS_PLAN_DEFAULT = "shared-vm";

    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
        Set<LogicalWebGUIService> logicalWebGUIServices = ld.listLogicalServices(LogicalWebGUIService.class);
        for (LogicalWebGUIService logicalWebGUIService : logicalWebGUIServices) {
            if (logicalWebGUIService.isStateful()) {
                toSessionReplicationService(logicalWebGUIService, projectionContext.getSpace(), td);
            }
        }
    }

    protected ManagedService toSessionReplicationService(LogicalWebGUIService logicalWebGUIService, Space space, TechnicalDeployment td) {
        final ManagedService sessionReplicationService = new ManagedService(REDIS_SERVICE_NAME, REDIS_PLAN_DEFAULT, new ServiceNameBuilder(logicalWebGUIService.getLabel()).build() + "-session-replication", space, td);
        sessionReplicationService.setLogicalModelId(logicalWebGUIService.getName());
        return sessionReplicationService;
    }
}
