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
package com.francetelecom.clara.cloud.commons.toggles;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;


public class PaasFeatureManagerProviderConfigurerTest {

	PaasFeatureManagerProviderConfigurer configurer = new PaasFeatureManagerProviderConfigurer();

	@Before
	public void setup() {
		// Ensure static state is reset
		PaasFeatureManagerProvider.clear();
	}

	@After
	public void teardown() {
	}

	@Test
	public void init_should_staticaly_initialize_paas_feature_provider() {
		PaasFeatureManagerProvider.clear();
		// Given
		PaasTogglesConfiguration config = new PaasTogglesConfiguration();
		config.setStateRepository(new InMemoryStateRepository());
		config.setUserProvider(new NoOpUserProvider());
		configurer.setConfig(config );
		configurer.setProviderPriority(10);
		// When
		configurer.initPaasFeatureManagerProvider();
		// Then
		PaasFeatureManagerProvider provider = new PaasFeatureManagerProvider();
		assertNotNull(provider.getFeatureManager());
		
	}
	
}
