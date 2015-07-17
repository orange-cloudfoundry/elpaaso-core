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
package com.francetelecom.clara.cloud.coremodel;

import org.junit.Assert;
import org.junit.Test;

public class PaasUserTest {

	@Test(expected=IllegalArgumentException.class)
	public void ssoid_is_mandatory() {
		new PaasUser("bob", "Dylan", null, "bob@orange.com");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void first_name_is_mandatory() {
		new PaasUser(null, "Dylan", new SSOId("bob123"), "bob@orange.com");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void first_name_cannot_be_empty() {
		new PaasUser("", "Dylan", new SSOId("bob123"), "bob@orange.com");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void last_name_is_mandatory() {
		new PaasUser("bob", null, new SSOId("bob123"), "bob@orange.com");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void last_name_cannot_be_empty() {
		new PaasUser("bob", "", new SSOId("bob123"), "bob@orange.com");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void mail_is_mandatory() {
		new PaasUser("bob", "Dylan", new SSOId("bob123"), null);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void mail_cannot_be_empty() {
		new PaasUser("bob", "Dylan", new SSOId("bob123"), "");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void paas_user_role_cannot_be_null() {
		//given bob is a paas user
		PaasUser bob = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
		//when is set bob role as null
		bob.setPaasUserRole(null);
		//then it should fail
	}
	
	@Test
	public void default_paas_user_role_is_role_user() {
		//when I create bob
		PaasUser bob = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
		//then bob default role is PaasRoleEnum.ROLE_USER
		Assert.assertEquals(PaasRoleEnum.ROLE_USER, bob.getPaasUserRole());
	}

	@Test
	public void full_name_is_firstname_space_lastname() throws Exception {
		//when I create bob dylan
		PaasUser bob = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
		bob.setLastName("dylan");
		//then fullname should be bob dylan
		Assert.assertEquals("bob dylan", bob.getFullName());
	}
}
