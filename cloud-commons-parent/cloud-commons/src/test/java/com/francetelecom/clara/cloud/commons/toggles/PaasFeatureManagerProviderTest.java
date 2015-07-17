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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;

public class PaasFeatureManagerProviderTest {

	PaasFeatureManagerProvider provider = new PaasFeatureManagerProvider();
	
	@Before
	public void setup() {
		// Ensure static state is reset
		PaasFeatureManagerProvider.clear();
	}
	
	@After
	public void teardown() {
	}
	
	@Test
	public void static_bind_creates_a_feature_manager() {
		// Given
		PaasTogglesConfiguration config = new PaasTogglesConfiguration();
		config.setStateRepository(new InMemoryStateRepository());
		config.setUserProvider(new NoOpUserProvider());
		// When
		PaasFeatureManagerProvider.bind(config);
		// Then
		assertNotNull(provider.getFeatureManager());
	}
	
	@Test
	public void static_set_priority_configures_priority_for_all_instances() {
		// When
		PaasFeatureManagerProvider.setPriority(200);
		// Then
		assertEquals(200, provider.priority());
	}
	
	@Test
	public void clear_resets_static_state() {
		// When
		PaasFeatureManagerProvider.clear();
		// Then
		assertEquals(300, provider.priority());
		assertNull(provider.getFeatureManager());
	}
	

	
}
