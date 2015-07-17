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

import com.francetelecom.clara.cloud.logicalmodel.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Properties;

import static org.fest.assertions.Assertions.assertThat;

public class ConfigRoleHelperTest {

	@Test
	public void should_override_config_value() throws InvalidConfigServiceException {
		LogicalDeployment deployment = new LogicalDeployment();
		ProcessingNode node = new JeeProcessing("node1", deployment);

		LogicalConfigService logicalConfigService = new LogicalConfigService("frontEnd", deployment, "#config1 \nkey=value\n");
		node.addLogicalServiceUsage(logicalConfigService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        logicalConfigService.setKeyPrefix("config");

        ConfigRole configRole = new ConfigRole();
		configRole.setValues(Arrays.asList(new ConfigValue("frontEnd", "key", "overridenValue", "comment")));
		
		Properties props = ConfigRoleHelper.getMergedConfigServicesProperties(node, Arrays.asList(configRole));
		
		assertThat(props.getProperty("configkey")).isNotNull();
		assertThat(props.getProperty("configkey")).isEqualTo("overridenValue");
	}
	
	@Test
	public void should_override_one_of_multi_config_service_value() throws InvalidConfigServiceException {
		LogicalDeployment deployment = new LogicalDeployment();
		ProcessingNode node = new JeeProcessing("node1", deployment);

		LogicalConfigService logicalConfigService = new LogicalConfigService("frontEnd", deployment, "#config1 \nkey=value\n");
		node.addLogicalServiceUsage(logicalConfigService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        logicalConfigService.setKeyPrefix("config");

		LogicalConfigService logicalConfigService2 = new LogicalConfigService("backEnd", deployment, "#config1 \nkey=value\n");
		node.addLogicalServiceUsage(logicalConfigService2, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        logicalConfigService2.setKeyPrefix("config2");

        ConfigRole configRole = new ConfigRole();
		configRole.setValues(Arrays.asList(new ConfigValue("frontEnd", "key", "overridenValue", "comment")));
		
		Properties props = ConfigRoleHelper.getMergedConfigServicesProperties(node, Arrays.asList(configRole));
		
		assertThat(props.getProperty("configkey")).isNotNull();
		assertThat(props.getProperty("configkey")).isEqualTo("overridenValue");
		assertThat(props.getProperty("config2key")).isNotNull();
		assertThat(props.getProperty("config2key")).isEqualTo("value");
	}
	
	@Test
	public void should_not_override_config_value() throws InvalidConfigServiceException {
		LogicalDeployment deployment = new LogicalDeployment();
		ProcessingNode node = new JeeProcessing("node1", deployment);

		LogicalConfigService logicalConfigService = new LogicalConfigService("frontEnd", deployment, "#config1 \nkey=value\n");
		node.addLogicalServiceUsage(logicalConfigService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        logicalConfigService.setKeyPrefix("config");

        ConfigRole configRole = new ConfigRole();
		
		Properties props = ConfigRoleHelper.getMergedConfigServicesProperties(node, Arrays.asList(configRole));
		
		assertThat(props.getProperty("configkey")).isNotNull();
		assertThat(props.getProperty("configkey")).isEqualTo("value");
	}

	@Test(expected = InvalidConfigServiceException.class)
	public void should_fail_with_unknown_key() throws InvalidConfigServiceException {
		LogicalDeployment deployment = new LogicalDeployment();
		ProcessingNode node = new JeeProcessing("node1", deployment);

		LogicalConfigService logicalConfigService = new LogicalConfigService("frontEnd", deployment, "#config1 \nkey=value\n");
		node.addLogicalServiceUsage(logicalConfigService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        logicalConfigService.setKeyPrefix("config");

		ConfigRole configRole = new ConfigRole();
		configRole.setValues(Arrays.asList(new ConfigValue("frontEnd", "erroneouskey", "overridenValue", "comment")));

		ConfigRoleHelper.getMergedConfigServicesProperties(node, Arrays.asList(configRole));
	}
}
