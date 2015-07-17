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
import org.junit.Before;
import org.junit.Test;
import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.repository.mem.InMemoryStateRepository;
import org.togglz.core.user.NoOpUserProvider;
import org.togglz.core.user.UserProvider;
import com.francetelecom.clara.cloud.commons.toggles.PaasFeatures;

public class PaasFeaturesTest {

	@Before
	public void setup() {
		// Ensure PaasFeatureManagerProvider is reset and will be queried first
		PaasFeatureManagerProvider.clear();
		PaasFeatureManagerProvider.setPriority(0);
		// Configure PaasFeaureManagerProvider
		TogglzConfig config = DummyTogglzConfigForTest(PaasFeatures.class);
		PaasFeatureManagerProvider.bind(config);
	}
	
	@Test
	public void iaas_capacity_is_disabled_by_default() {
		assertEquals(false, PaasFeatures.IAAS_CAPACITY.isActive());
	}
	
	/**
	 * Create a simple dummy Togglz config for test
	 * @param featureClass
	 * @return
	 */
	private TogglzConfig DummyTogglzConfigForTest(final Class<? extends Feature> featureClass) {
		return new TogglzConfig() {
			
			@Override
			public UserProvider getUserProvider() {
				return new NoOpUserProvider();
			}
			
			@Override
			public StateRepository getStateRepository() {
				return new InMemoryStateRepository();
			}
			
			@Override
			public Class<? extends Feature> getFeatureClass() {
				return featureClass;
			}
		};
	}

}
