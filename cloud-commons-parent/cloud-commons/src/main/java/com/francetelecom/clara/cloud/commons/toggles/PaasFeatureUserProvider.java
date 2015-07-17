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
import org.togglz.core.user.FeatureUser;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

public class PaasFeatureUserProvider implements UserProvider {

	private static Logger logger = LoggerFactory.getLogger(PaasFeatureUserProvider.class);
	
	private UserProvider mainUserProvider;
	
	/**
	 * Returns the current user that can be provided by the main user provider<br>
	 * If this current user is null or if an exception is thrown by the main user provider, a default non admin user is returned
	 */
	@Override
	public FeatureUser getCurrentUser() {
		FeatureUser user = null;
		try {
			user =  mainUserProvider.getCurrentUser();
		} catch(Exception e) {
			// Simply log error
			logger.error("Error while identifying current user",e);
		}
		if(user == null) {
			logger.warn("No current user; a default non admin user will be returned");
			user = new SimpleFeatureUser("unknown",false);
		}
		logger.debug("current user is "+user.getName()+" isFeatureAdmin: "+user.isFeatureAdmin());
		return user;
	}

	public UserProvider getMainUserProvider() {
		return mainUserProvider;
	}

	public void setMainUserProvider(UserProvider mainUserProvider) {
		this.mainUserProvider = mainUserProvider;
	}

}
