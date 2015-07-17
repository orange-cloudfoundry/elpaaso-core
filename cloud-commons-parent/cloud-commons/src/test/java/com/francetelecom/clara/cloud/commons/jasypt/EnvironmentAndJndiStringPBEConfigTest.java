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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import javax.naming.InitialContext;

import org.junit.Test;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;


public class EnvironmentAndJndiStringPBEConfigTest {
	
	@Test
	public void should_use_env_variable_if_available() throws Exception {
		assertNotNull(System.getenv("PAAS_ENCRYPTION_PASSWORD"));
		
		EnvironmentAndJndiStringPBEConfig config = new EnvironmentAndJndiStringPBEConfig("PAAS_ENCRYPTION_PASSWORD", "jasypt.secret");
		
		assertEquals(System.getenv("PAAS_ENCRYPTION_PASSWORD"), config.getPassword());
	}

	@Test
	public void should_use_jndi_variable_if_env_variable_not_found_and_jndi_available() throws Exception {
		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		InitialContext context = new InitialContext();
		context.bind("jasypt.secret", "secret tres secret");
		
		EnvironmentAndJndiStringPBEConfig config = new EnvironmentAndJndiStringPBEConfig("nimportenaouak", "jasypt.secret");
		
		assertEquals("secret tres secret", config.getPassword());
	}
	

	@Test
	public void should_do_nothing_if_nothing_found() throws Exception {
		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
		
		new EnvironmentAndJndiStringPBEConfig("nimportenaouak", "jasypt.secret");
	}

}
