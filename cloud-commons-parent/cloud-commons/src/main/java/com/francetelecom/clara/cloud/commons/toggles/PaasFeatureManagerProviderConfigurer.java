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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.context.FeatureContext;

/**
 * Bean used to configure {@link PaasFeatureManagerProvider} with spring
 */
public class PaasFeatureManagerProviderConfigurer {

	Logger logger = LoggerFactory.getLogger(PaasFeatureManagerProviderConfigurer.class);
	
	PaasTogglesConfiguration config;
	private int providerPriority = 300;
	
	public PaasTogglesConfiguration getConfig() {
		return config;
	}

	public void setConfig(PaasTogglesConfiguration config) {
		this.config = config;
	}

	@PostConstruct
	public void initPaasFeatureManagerProvider() {
		logger.debug("Initialize PaasFeatureManagerProvider");
		PaasFeatureManagerProvider.bind(config);

		PaasFeatureManagerProvider.setPriority(getProviderPriority());
		logger.info("Testing Feature Toggles Context is properly set-up");
		
		FeatureContext.getFeatureManager();
	}

	public int getProviderPriority() {
		return providerPriority;
	}

	public void setProviderPriority(int providerPriority) {
		this.providerPriority = providerPriority;
	}
}
