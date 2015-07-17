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
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.togglz.core.context.FeatureContext;
import org.togglz.core.manager.FeatureManager;
import org.togglz.testing.TestFeatureManager;
import org.togglz.testing.TestFeatureManagerProvider;
import org.togglz.testing.fallback.FallbackTestFeatureManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:META-INF/spring/toggles-context.xml")
public class PaasFeatureManagerProviderSpringTest {

	@Autowired
	public PaasTogglesConfiguration paasTogglesConfiguration;
	
	@Before
	public void setup() {
		// Ensure that PaasFeatureManagerProvider will be queried first
		PaasFeatureManagerProvider.setPriority(0);
		// Ensure FeatureContext cache is empty
		FeatureContext.clearCache();
	}
	
	@After
	public void teardown() {
	}
	
	@Test
	public void a_default_feature_manager_should_be_available() {
		FeatureManager featureManager = FeatureContext.getFeatureManager();
		assertNotNull(featureManager);
	}
	
	@Test
	public void default_feature_manager_is_not_a_test_feature_manager() {
		// Given a configured TestFeatureManagerProvider
		FeatureManager testFeatureManager = new TestFeatureManager(PaasFeatures.class);
		TestFeatureManagerProvider.setFeatureManager(testFeatureManager);
		
		// When
		FeatureManager featureManager = FeatureContext.getFeatureManager();
		
		// Then
		assertTrue("The feature manager should not be provided by the TestFeatureManagerProvider",featureManager != testFeatureManager);
		assertTrue("The feature manager should not be provided by the FallbackTestFeatureManagerProvider",!(featureManager instanceof FallbackTestFeatureManager));
	}

}
