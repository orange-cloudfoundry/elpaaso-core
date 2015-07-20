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

import java.util.Arrays;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.coremodel.ConfigRoleRepository;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.ConfigValue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/coremodel/application-context.xml" })
public class ConfigRoleRepositoryTest {

	@Autowired
	ConfigRoleRepository configRoleRepository;
	
	@Test
	@Transactional
	public void should_find_existing_config_role_by_application_uid() {
		ConfigRole configRole = new ConfigRole("myapp");
		configRole.setLastModificationComment("Modified by Guillaume.");
		configRole.setValues(Arrays.asList(new ConfigValue("myconfigset", "mykey", "myvalue", "override mykey with its new value")));
		
		configRoleRepository.save(configRole);
		
		Assertions.assertThat(configRoleRepository.findByApplicationUID("myapp")).hasSize(1);
		
		ConfigRole persisted = configRoleRepository.findByApplicationUID("myapp").get(0);
		Assertions.assertThat(persisted.getLastModificationComment()).isEqualTo("Modified by Guillaume.");
		Assertions.assertThat(persisted.listValues()).hasSize(1);
		
		ConfigValue persistedValue = persisted.listValues().get(0);
		Assertions.assertThat(persistedValue.getComment()).isEqualTo("override mykey with its new value");
		Assertions.assertThat(persistedValue.getKey()).isEqualTo("mykey");
		Assertions.assertThat(persistedValue.getValue()).isEqualTo("myvalue");
		Assertions.assertThat(persistedValue.getConfigSet()).isEqualTo("myconfigset");
	}

	@Test
	@Transactional
	public void should_find_existing_config_role_by_its_uid() {
		ConfigRole configRole = new ConfigRole("myapp");
		configRole.setLastModificationComment("Modified by Guillaume.");
		configRole.setValues(Arrays.asList(new ConfigValue("myconfigset", "mykey", "myvalue", "override mykey with its new value")));
		
		configRoleRepository.save(configRole);
		
		Assertions.assertThat(configRoleRepository.findByUid(configRole.getUID())).isNotNull();
		
		ConfigRole persisted = configRoleRepository.findByUid(configRole.getUID());
		Assertions.assertThat(persisted.getLastModificationComment()).isEqualTo("Modified by Guillaume.");
		Assertions.assertThat(persisted.listValues()).hasSize(1);
		
		ConfigValue persistedValue = persisted.listValues().get(0);
		Assertions.assertThat(persistedValue.getComment()).isEqualTo("override mykey with its new value");
		Assertions.assertThat(persistedValue.getKey()).isEqualTo("mykey");
		Assertions.assertThat(persistedValue.getValue()).isEqualTo("myvalue");
		Assertions.assertThat(persistedValue.getConfigSet()).isEqualTo("myconfigset");
	}
	
	@Test
	@Transactional
	public void should_find_existing_config_roles_by_their_uids() {
		ConfigRole configRole = new ConfigRole("myapp");
		configRole.setLastModificationComment("Modified by Guillaume.");
		configRole.setValues(Arrays.asList(new ConfigValue("myconfigset", "mykey", "myvalue", "override mykey with its new value")));
		
		ConfigRole configRole2 = new ConfigRole("myapp");
		configRole2.setLastModificationComment("Modified by Guillaume.");
		configRole2.setValues(Arrays.asList(new ConfigValue("myconfigset2", "mykey2", "myvalue2", "override mykey2 with its new value")));
		
		configRoleRepository.save(Arrays.asList(configRole, configRole2));
		
		Assertions.assertThat(configRoleRepository.findByUidIn(Arrays.asList(configRole.getUID(), configRole2.getUID()))).isNotNull();
		Assertions.assertThat(configRoleRepository.findByUidIn(Arrays.asList(configRole.getUID(), configRole2.getUID()))).isNotEmpty();
		Assertions.assertThat(configRoleRepository.findByUidIn(Arrays.asList(configRole.getUID(), configRole2.getUID())).size()).isEqualTo(2);
	}

}
