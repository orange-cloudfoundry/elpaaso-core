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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.togglz.core.Feature;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.repository.StateRepository;

/**
 * Feature RepositoryState based on Jndi to store feature state and users<br>
 * Features are stored as String values under following names:
 * <pre>
 *  FEATURE1=true/false
 *  FEATURE1.users=user1, user2, ...
 * </pre>
 *
 */
public class PaasJndiStateRepository implements StateRepository {

	Logger logger = LoggerFactory.getLogger(PaasJndiStateRepository.class);
	
	private InitialContext initialContext = null;
	
	@Override
	public FeatureState getFeatureState(Feature feature) {
		try {
			InitialContext jndiContext = getInitialContext();
			String value = (String)jndiContext.lookup(feature.name());
			if(!(value.equalsIgnoreCase("true") || value.equalsIgnoreCase("false"))) {
				return null;
			}
			boolean enabled = Boolean.valueOf(value);
			List<String> users = getFeatureUsers(feature);
			FeatureState featureState = new FeatureState(feature, enabled, users );
			return featureState;
		} catch (NameNotFoundException e) {
			logger.warn("state of feature "+feature.name()+ " is not already defined in jndi");
		} catch (NamingException e) {
			if(ExceptionUtils.indexOfType(e, NameNotFoundException.class) != -1) {
				logger.warn("state of feature "+feature.name()+ " is not already defined in jndi");
			} else {
				logger.error("unable to read state of feature "+feature.name(),e);
			}
		} catch(ClassCastException e) {
			logger.error("unable to read state of feature "+feature.name(),e);
		}
		// By default return null (no feature state found or invalid)
		return null;
	}

	protected List<String> getFeatureUsers(Feature feature) {
		List<String> users = new ArrayList<String>();
		try {
			InitialContext jndiContext = getInitialContext();
			String value = (String)jndiContext.lookup(feature.name()+".users");
			if(value != null) {
				value = value.trim();
				if( !value.equals("")) {
					users = Arrays.asList(value.split("( )*,( )*"));
				}
			}
		} catch (NamingException e) {
			logger.debug("unable to fetch user lists for feature "+feature.name());
		}
		return users;
	}

	@Override
	public void setFeatureState(FeatureState featureState) {
		String featureName = featureState.getFeature().name();
		try {
			InitialContext jndiContext = getInitialContext();
			jndiContext.rebind(featureName, Boolean.toString(featureState.isEnabled()));
			String users = StringUtils.join(featureState.getUsers(), ", ");
			//for(String user:featureState.getUsers()) users += user+", ";
			jndiContext.rebind(featureName+".users", users);
		} catch (NamingException e) {
			logger.error("unable to save state of feature "+featureName+" in jndi",e);
		}
	}

	/**
	 * @return
	 * @throws NamingException
	 */
	protected InitialContext getInitialContext() throws NamingException {
		if( initialContext  == null) {
			initialContext = createInitialContext();
		}
		return initialContext;
	}

	/**
	 * wrap new InitialContext for testing purpose
	 * @return
	 * @throws NamingException
	 */
	protected InitialContext createInitialContext() throws NamingException {
		return new InitialContext();
	}

}
