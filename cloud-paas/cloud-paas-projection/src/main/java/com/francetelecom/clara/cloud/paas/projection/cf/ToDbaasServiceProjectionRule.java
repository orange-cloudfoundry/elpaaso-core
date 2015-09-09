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
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ToDbaasServiceProjectionRule implements ServiceProjectionRule {

    private static final Logger logger = LoggerFactory.getLogger(ToDbaasServiceProjectionRule.class);

    public ToDbaasServiceProjectionRule() {
    }


    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
        // generate services
        Set<LogicalRelationalService> logicalRelationalServices = ld.listLogicalServices(LogicalRelationalService.class);
        for (LogicalRelationalService logicalRelationalService : logicalRelationalServices) {
            // generate cloud foundry service
            td.add(toDbaasService(logicalRelationalService, projectionContext.getSpace()));
        }

    }

    protected ManagedService toDbaasService(LogicalRelationalService logicalRelationalService, Space space) {
        ManagedService dbaasService = new ManagedService("o-dbaas", getPlan(logicalRelationalService), logicalRelationalService.getServiceName(), space);
        dbaasService.setLogicalModelId(logicalRelationalService.getName());
        return dbaasService;
    }

    protected String getPlan(LogicalRelationalService logicalRelationalService) {
        switch (logicalRelationalService.getSqlVersion()) {
            case POSTGRESQL_DEFAULT:
                return "POSTGRESQL_1G";
            default:
                return "MYSQL_1G";
        }
    }

}
