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
package com.francetelecom.clara.cloud.paas.it.services.helper;


import java.util.Arrays;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;

public class AuthenticationHelper {

	public static final SSOId USER_WITH_ADMIN_ROLE_SSOID = new SSOId("bobIsAdmin");
	public static final SSOId USER_WITH_USER_ROLE_SSOID = new SSOId("aliceIsUser");

	private static final Logger logger = LoggerFactory.getLogger(AuthenticationHelper.class);

	public static PaasUser loginAsAdmin() {

		logger.info("login As Admin");

		SecurityContextHolder.getContext().setAuthentication(new Authentication() {
			@Override
			public String getName() {
				return "bobIsAdmin";
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			}

			@Override
			public boolean isAuthenticated() {
				return true;
			}

			@Override
			public Object getPrincipal() {
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getCredentials() {
				return null;
			}

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return Arrays.asList(new SimpleGrantedAuthority(PaasRoleEnum.ROLE_ADMIN.toString()));
			}
		});

		return createPaasAdminUser();

	}

	private static PaasUser createPaasAdminUser() {
		String firstname = AuthenticationHelper.USER_WITH_ADMIN_ROLE_SSOID.getValue();
		String lastname = firstname;
		String email = firstname + "." + lastname + "@orange.com";
		PaasUser paasUser = new PaasUser(firstname, lastname, AuthenticationHelper.USER_WITH_ADMIN_ROLE_SSOID, email);
		paasUser.setPaasUserRole(PaasRoleEnum.ROLE_ADMIN);
		return paasUser;
	}


	public static void loginAsUser() {
		logger.info("login As User");
		SecurityContextHolder.getContext().setAuthentication(new Authentication() {
			@Override
			public String getName() {
				return "alice123";
			}

			@Override
			public Collection<? extends GrantedAuthority> getAuthorities() {
				return Arrays.asList(new SimpleGrantedAuthority(PaasRoleEnum.ROLE_USER.toString()));
			}

			@Override
			public Object getCredentials() {
				return null;
			}

			@Override
			public Object getDetails() {
				return null;
			}

			@Override
			public Object getPrincipal() {
				return null;
			}

			@Override
			public boolean isAuthenticated() {
				return true;
			}

			@Override
			public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
			}
		});
	}

	public static void logout() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		logger.info("{} is logging out !!!", username);

		SecurityContextHolder.getContext().setAuthentication(null);
	}
}
