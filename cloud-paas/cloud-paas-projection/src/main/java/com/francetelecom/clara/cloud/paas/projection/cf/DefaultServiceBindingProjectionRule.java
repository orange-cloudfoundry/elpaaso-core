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
import com.francetelecom.clara.cloud.logicalmodel.LogicalService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.AbstractUserProvidedService;

import java.util.List;
import java.util.Set;

public class DefaultServiceBindingProjectionRule implements AssociationProjectionRule {

    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
        // list logical services
        List<LogicalService> logicalServices = ld.listLogicalServices();
        for (LogicalService logicalService : logicalServices) {
            //list processing nodes associated to the logical service
            Set<ProcessingNode> processingNodes = logicalService.listDependentProcessingNodes();
            for (ProcessingNode processingNode : processingNodes) {
                //get associated app
                Set<App> apps = td.listXaasSubscriptionTemplates(App.class, processingNode.getName());
                for (App app : apps) {
                    // get associated managed services
                    Set<ManagedService> managedServices = td.listXaasSubscriptionTemplates(ManagedService.class, logicalService.getName());
                    for (ManagedService managedService : managedServices) {
                        app.bindService(managedService);
                    }
                    Set<AbstractUserProvidedService> userProvidedServices = td.listXaasSubscriptionTemplates(AbstractUserProvidedService.class, logicalService.getName());
                    for (AbstractUserProvidedService userProvidedService : userProvidedServices) {
                        app.bindService(userProvidedService);
                    }

                }

            }
        }

    }

}
