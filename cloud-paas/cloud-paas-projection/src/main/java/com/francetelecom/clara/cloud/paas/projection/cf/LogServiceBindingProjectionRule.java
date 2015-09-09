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
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.ServiceNameBuilder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LogServiceBindingProjectionRule implements AssociationProjectionRule {

    public static final String SERVICE_NAME_SUFFIX = "-log";
    public static final String SERVICE_TYPE = "o-logs";
    public static final String SERVICE_PLAN = "splunk";

    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
        final Set<App> apps = td.listXaasSubscriptionTemplates(App.class);
        for (App app : apps) {
            final ManagedService managedService = toLogService(app.getAppName(), projectionContext.getSpace());
            app.bindService(managedService);
            td.add(managedService);
        }
    }

    protected ManagedService toLogService(String appName, Space space) {
        ManagedService logService = new ManagedService(SERVICE_TYPE, SERVICE_PLAN, getServiceName(appName), space);
        return logService;
    }

    protected String getServiceName(String appName) {
        return new StringBuilder().append(new ServiceNameBuilder(appName).build()).append(SERVICE_NAME_SUFFIX).toString();
    }
}
