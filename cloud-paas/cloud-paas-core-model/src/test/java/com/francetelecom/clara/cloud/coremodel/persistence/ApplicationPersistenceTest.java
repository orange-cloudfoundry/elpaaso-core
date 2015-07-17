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
package com.francetelecom.clara.cloud.coremodel.persistence;

import static org.fest.assertions.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import com.francetelecom.clara.cloud.commons.PersistenceTestUtil;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.ConfigValue;
import com.francetelecom.clara.cloud.model.AbstractPersistenceTest;

@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
public class ApplicationPersistenceTest extends AbstractPersistenceTest {

    @Autowired
    PersistenceTestUtil persistenceTestUtil;

	@Test
	public void should_be_valid_and_persistable() {
		// Given
		Application application = new Application("application label","Application code");

		// When
        persistenceTestUtil.persistObject(application);
        Application retrievedApplication = persistenceTestUtil.reloadEntity(Application.class, application.getId(), true);

		// Then
		assertThat(retrievedApplication.getCode()).isEqualTo(application.getCode());
		assertThat(retrievedApplication.getLabel()).isEqualTo(application.getLabel());
		assertThat(retrievedApplication.isPublic()).isEqualTo(application.isPublic());
	}
	

	@Test
	public void application_with_config_role_should_be_valid() {
		// Given
		Application application = new Application("application label","Application code");
		ConfigRole configRole = new ConfigRole("myapp");
		configRole.setLastModificationComment("Modified by Guillaume.");
        configRole.setValues(Arrays.asList(new ConfigValue("myconfigset", "mykey", "myvalue", "update mykey to its new value")));
		application.addConfigRole(configRole);

		// When
        persistenceTestUtil.persistObject(application);
        Application retrievedApplication = persistenceTestUtil.reloadEntity(Application.class, application.getId(), true);

		// Then
		assertThat(retrievedApplication.getCode()).isEqualTo(application.getCode());
		assertThat(retrievedApplication.getLabel()).isEqualTo(application.getLabel());
		assertThat(retrievedApplication.isPublic()).isEqualTo(application.isPublic());
		
	}
	
	@Test
	public void application_with_valid_large_config_role_should_properly_persist() {
		// Given
		Application application = new Application("application label","Application code");
		ConfigRole configRole = new ConfigRole("myapp");
		configRole.setLastModificationComment("Modified by Guillaume.");

        String long300CharsComment= StringUtils.leftPad("comment", 300, 'X');
        configRole.setValues(Arrays.asList(new ConfigValue("myconfigset", "mykey", "myvalue", long300CharsComment)));
		application.addConfigRole(configRole);

		// When
        persistenceTestUtil.persistObject(application);
		Application retrievedApplication = persistenceTestUtil.reloadEntity(Application.class, application.getId(), true);

		// Then
        List<ConfigRole> reloadedConfigRoles = retrievedApplication.listConfigRoles();
        assertThat(reloadedConfigRoles.size()).isEqualTo(1);
        ConfigRole reloadedConfigRole = reloadedConfigRoles.get(0);
        List<ConfigValue> reloadedConfigValues = reloadedConfigRole.listValues();
        assertThat(reloadedConfigValues.size()).isEqualTo(1);
        ConfigValue reloadedConfigValue = reloadedConfigValues.get(0);
        assertThat(reloadedConfigValue.getConfigSet()).isEqualTo("myconfigset");
        assertThat(reloadedConfigValue.getKey()).isEqualTo("mykey");
        assertThat(reloadedConfigValue.getValue()).isEqualTo("myvalue");
        assertThat(reloadedConfigValue.getComment()).isEqualTo(long300CharsComment);
    }

}
