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
package com.francetelecom.clara.cloud.coremodel;

import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Helper class used to interact with the logical model in order to override
 * configuration properties using some {@link ConfigRole}
 */
public class ConfigRoleHelper {

	public static Properties getMergedConfigServicesProperties(ProcessingNode len, List<ConfigRole> overridenConfigs) throws InvalidConfigServiceException {

		Properties properties = len.getMergedConfigServicesProperties();

		List<LogicalConfigService> logicalConfigServices = len.listLogicalServices(LogicalConfigService.class);
		List<String> configSets = new ArrayList<>(logicalConfigServices.size());
		for (LogicalConfigService logicalConfigService : logicalConfigServices) {
			configSets.add(logicalConfigService.getLabel());
		}
		
		if (overridenConfigs != null) {
			for (ConfigRole configRole : overridenConfigs) {
				for (ConfigValue configValue : configRole.listValues()) {
					if (configSets.contains(configValue.getConfigSet())) {
						// Add JNDI prefix
                        String keyPrefix = logicalConfigServices.get(configSets.indexOf(configValue.getConfigSet())).getKeyPrefix();
                        String configKey = keyPrefix == null ? configValue.getKey() : keyPrefix + configValue.getKey();
                        if (properties.getProperty(configKey) != null) {
							properties.setProperty(configKey, configValue.getValue());
						} else {
							throw new InvalidConfigServiceException("Trying to override unexisting property: no such key '" + configValue.getKey()
							        + "' in config set '" + configValue.getConfigSet() + "'.");
						}
					}
				}
			}
		}
		return properties;

	}
	
}
