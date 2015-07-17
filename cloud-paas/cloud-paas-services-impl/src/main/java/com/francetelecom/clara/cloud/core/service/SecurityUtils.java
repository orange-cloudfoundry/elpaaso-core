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
package com.francetelecom.clara.cloud.core.service;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.francetelecom.clara.cloud.commons.AuthorizationException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.SSOId;

public class SecurityUtils {
	private static Logger LOGGER = LoggerFactory.getLogger(SecurityUtils.class);


	public static SSOId currentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			throw new TechnicalException("User is not authenticated. No authentication token found.");
		}
		return new SSOId(authentication.getName());
	}

	public static boolean currentUserIsAdmin() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			// FIXME raise a specific exception
			throw new TechnicalException("User is not authenticated. No authentication token found.");
		}
		Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
		for (GrantedAuthority grantedAuthority : authorities) {
			if (grantedAuthority.getAuthority().equals(PaasRoleEnum.ROLE_ADMIN.toString())) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasWritePermissionFor(Application application) {
		return SecurityUtils.currentUserIsAdmin() || application.hasForMember(SecurityUtils.currentUser());
	}
	
	public static void assertHasWritePermissionFor(Application application) {
		if (!hasWritePermissionFor(application))
			throw new AuthorizationException();
	}
	
	public static void assertHasWritePermissionFor(ApplicationRelease release) {
		assertHasWritePermissionFor(release.getApplication());
	}
	
	public static void assertHasWritePermissionFor(Environment environment) {
		assertHasWritePermissionFor(environment.getApplicationRelease());
	}

	public static void assertHasReadPermissionFor(Environment environment) {
		assertHasReadPermissionFor(environment.getApplicationRelease());	
	}

	public static void assertHasReadPermissionFor(ApplicationRelease applicationRelease) {
		assertHasReadPermissionFor(applicationRelease.getApplication());
	}

	private static void assertHasReadPermissionFor(Application application) {
		if (!hasReadPermissionFor(application))
			throw new AuthorizationException();
	}

	private static boolean hasReadPermissionFor(Application application) {
		return SecurityUtils.currentUserIsAdmin() || application.hasForMember(SecurityUtils.currentUser()) || application.isPublic();
	}

	public static boolean hasWritePermissionFor(Environment environment) {
		return hasWritePermissionFor(environment.getApplicationRelease());
	}

	private static boolean hasWritePermissionFor(ApplicationRelease applicationRelease) {
		return hasWritePermissionFor(applicationRelease.getApplication());
	}
}
