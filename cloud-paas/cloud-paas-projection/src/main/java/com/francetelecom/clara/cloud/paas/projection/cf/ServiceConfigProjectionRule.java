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

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.CFJavaProcessing;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.EnvVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;

public class ServiceConfigProjectionRule implements AssociationProjectionRule {

    private static Logger logger = LoggerFactory.getLogger(ServiceConfigProjectionRule.class.getName());

    /*
    @Override
	public void updateConfigServiceEntries(ProcessingNode node, App app, List<ConfigRole> configRoles) {
		Properties mergedConfigServicesProperties = null;
		try {
			mergedConfigServicesProperties = ConfigRoleHelper.getMergedConfigServicesProperties(node, configRoles);
		} catch (InvalidConfigServiceException e) {
			String msg = "expected LogicalDeployment to be checked prior to projection being called. Caught: " + e;
			logger.error(msg, e);
			throw new IllegalArgumentException(msg);
		}

		for (Map.Entry<Object, Object> entry : mergedConfigServicesProperties.entrySet()) {
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			app.addEnvVariable(new EnvVariable(key, value));
		}
	}
	*/

    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
// convert config service to tech model
        Set<LogicalConfigService> configServices = ld.listLogicalServices(LogicalConfigService.class);
        for (LogicalConfigService config : configServices) {
            final Set<ProcessingNode> processingNodes = config.listDependentProcessingNodes();
            for (ProcessingNode processingNode : processingNodes) {
                if (processingNode instanceof CFJavaProcessing) {
                    logger.debug("applying config {} on processing {}", config, processingNode);

                    Properties properties = new Properties();
                    try {
                        properties.load(new StringReader(config.getConfigSetContent()));
                        ArrayList<String> invalidEnvVars = new ArrayList<>();
                        for (Object key : properties.keySet()) {
                            Object value = properties.get(key);
                            try {
                                final Set<App> apps = td.listXaasSubscriptionTemplates(App.class, processingNode.getName());
                                for (App app : apps) {
                                    app.setEnvVariable(key.toString(), value.toString());
                                }
                            } catch (IllegalArgumentException iae) {
                                invalidEnvVars.add(iae.getMessage());
                            }
                        }
                        if (invalidEnvVars.size() > 0) {
                            throw new TechnicalException("Fatal : incorrect config service varenv: " + Arrays.toString(invalidEnvVars.toArray()));
                        }
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        throw new TechnicalException("Fatal : incorrect config service parsing", e);
                    }
                }
            }
        }

    }
}
