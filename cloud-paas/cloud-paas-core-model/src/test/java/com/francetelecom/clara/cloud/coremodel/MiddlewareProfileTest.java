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

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class MiddlewareProfileTest {

	@Test
	public void from_version_should_find_every_declared_values() throws Exception {
		for (MiddlewareProfile existingProfil : MiddlewareProfile.values()) {
			assertEquals(existingProfil, MiddlewareProfile.fromVersion(existingProfil.getVersion()));
		}
	}
	
	@Test
	public void from_version_should_return_null_for_not_known_version() throws Exception {
		assertNull(MiddlewareProfile.fromVersion("0.0.1"));
	}
	
	@Test
	public void filter_on_admin_users_should_return_deprecated_supported_and_experimental_profiles() throws Exception {
		Set<MiddlewareProfile> visibleProfiles =  MiddlewareProfile.filter(user(PaasRoleEnum.ROLE_ADMIN), Arrays.asList(MiddlewareProfile.values()));

		// Forbidden for
		// Currently no internal profile as 1.0.0-dbaas has been removed.
		// assertThat(visibleProfiles).excludes(MiddlewareProfile.V100_DBAAS);
		
  	//Allow for
		assertThat(visibleProfiles).containsOnly(MiddlewareProfile.V200_CF, MiddlewareProfile.V210_CF);

	}
	
	@Test
	public void filter_on_common_users_should_return_supported_profiles() throws Exception {
		Set<MiddlewareProfile> visibleProfiles =  MiddlewareProfile.filter(user(PaasRoleEnum.ROLE_USER), Arrays.asList(MiddlewareProfile.values()));

		//Forbidden for
		// assertThat(visibleProfiles).excludes(MiddlewareProfile.V200_CF);
		
		//Allow for
		assertThat(visibleProfiles).containsOnly(MiddlewareProfile.V200_CF);
	}
	
	@Test
	public void filter_on_deprecated_roles_should_return_supported_profiles() throws Exception {
		assertThatUserOnlySee(user(PaasRoleEnum.DEVELOPER), MiddlewareProfile.V200_CF);
		assertThatUserOnlySee(user(PaasRoleEnum.ARCHITECT), MiddlewareProfile.V200_CF);
		assertThatUserOnlySee(user(PaasRoleEnum.USER), MiddlewareProfile.V200_CF);
		assertThatUserOnlySee(user(PaasRoleEnum.RELEASE_MANAGER), MiddlewareProfile.V200_CF);
	}

	private PaasUser user(PaasRoleEnum role) {
		String username = role.getName().toLowerCase();
		PaasUser user = new PaasUser(username,username,new SSOId("ssoid"), username+"@orange.com");
		user.setPaasUserRole(role);
		return user;
	}

	private void assertThatUserOnlySee(PaasUser user, MiddlewareProfile...seenProfiles) {
		Collection<MiddlewareProfile> profiles = Arrays.asList(MiddlewareProfile.values());
		Set<MiddlewareProfile> visibleProfiles = MiddlewareProfile.filter(user, profiles);
		assertThat(visibleProfiles).containsOnly(seenProfiles);
	}
}
