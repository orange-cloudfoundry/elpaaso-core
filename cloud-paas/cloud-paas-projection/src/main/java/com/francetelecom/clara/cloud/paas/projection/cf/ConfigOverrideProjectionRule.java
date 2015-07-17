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

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.ConfigRoleHelper;
import com.francetelecom.clara.cloud.coremodel.ConfigValue;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.EnvVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ConfigOverrideProjectionRule {

    private static Logger logger = LoggerFactory.getLogger(ConfigOverrideProjectionRule.class.getName());


    public void updateDeploymentTemplateInstance(TechnicalDeploymentInstance tdi, ApplicationRelease applicationRelease, List<ConfigRole> configRoles) {
        //Q: how to extract this into a dedicated class ? Too early ?
        //Wait for other overides ? Sizing (nb instances) ?

        Set<ProcessingNode> impactedProcessingServices = identifyImpactedProcessingNodes(applicationRelease, configRoles);

        for (ProcessingNode impactedProcessingService : impactedProcessingServices) {
            Properties mergedConfigServicesProperties;
            try {
                mergedConfigServicesProperties = ConfigRoleHelper.getMergedConfigServicesProperties(impactedProcessingService, configRoles);
            } catch (InvalidConfigServiceException e) {
                String msg = "expected LogicalDeployment to be checked prior to projection being called. Caught: " + e;
                logger.error(msg, e);
                throw new IllegalArgumentException(msg);
            }

            Set<App> impactedApps = tdi.getTechnicalDeployment().listXaasSubscriptionTemplates(App.class, impactedProcessingService.getName());

            for (App impactedApp : impactedApps) { //Usually a single app, but we never know
                for (Map.Entry<Object, Object> potentiallyOverridenEnvEntries : mergedConfigServicesProperties.entrySet()) {
                    String key = (String) potentiallyOverridenEnvEntries.getKey();
                    String value = (String) potentiallyOverridenEnvEntries.getValue();
                    impactedApp.setEnvVariable(key, value);
                }

                //Q: do we need to capture this so that we can also reuse the code to modify an existing implemented app ?
            }
        }


    }

    protected Set<ProcessingNode> identifyImpactedProcessingNodes(ApplicationRelease applicationRelease, List<ConfigRole> configRoles) {
        Set<ProcessingNode> impactedProcessingServices = new HashSet<ProcessingNode>();

        LogicalDeployment architecture = applicationRelease.getLogicalDeployment();
        for (ConfigRole configRole : configRoles) {
            List<ConfigValue> configValues = configRole.listValues();
            for (ConfigValue configValue : configValues) {
                String configServiceName = configValue.getConfigSet();
                Set<LogicalConfigService> logicalConfigServices = architecture.listLogicalServices(LogicalConfigService.class, configServiceName);

                LogicalConfigService configService = logicalConfigServices.iterator().next();
                impactedProcessingServices.addAll(configService.listDependentProcessingNodes());
            }
        }
        return impactedProcessingServices;
    }
}