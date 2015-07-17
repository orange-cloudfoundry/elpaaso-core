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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.manager.TogglzConfig;
import org.togglz.core.repository.StateRepository;
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

public class PaasTogglesConfiguration implements TogglzConfig {

    private final static Logger logger = LoggerFactory.getLogger(PaasTogglesConfiguration.class.getName());

    private StateRepository stateRepository;
    private UserProvider userProvider;
    
    public Class<? extends Feature> getFeatureClass() {
        return PaasFeatures.class;
    }

    public StateRepository getStateRepository() {
		return stateRepository;
    }

    public UserProvider getUserProvider() {
    	return this.userProvider;
    }

	public void setStateRepository(StateRepository stateRepository) {
		this.stateRepository = stateRepository;
	}

	public void setUserProvider(UserProvider userProvider) {
		this.userProvider = userProvider;
	}



}
