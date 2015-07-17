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
package com.francetelecom.clara.cloud.commons.jasypt;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jasypt.encryption.pbe.config.EnvironmentStringPBEConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentAndJndiStringPBEConfig extends EnvironmentStringPBEConfig {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentAndJndiStringPBEConfig.class);
	
	public EnvironmentAndJndiStringPBEConfig(String envName, String jndiName){
		String envValue = System.getenv(envName);
		if(envValue != null && envValue.length() > 0){
			setPasswordEnvName(envName);
		}else{
			try {
				InitialContext ctx = createInitialContext();
				String jndiValue = (String)ctx.lookup(jndiName);
				setPassword(jndiValue);
			} catch (NamingException e) {
				LOGGER.error("Error resolving Env or Jndi jasypt secret named "+jndiName,e);
			} 
		}
	}
	
	protected InitialContext createInitialContext() throws NamingException{
		return new InitialContext();
	}
}
