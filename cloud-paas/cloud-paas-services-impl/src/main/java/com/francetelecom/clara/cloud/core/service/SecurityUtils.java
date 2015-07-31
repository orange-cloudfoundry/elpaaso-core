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
import org.springframework.beans.factory.annotation.Autowired;
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

	@Autowired
	SecurityContextUtil securityContextUtil;

	public SecurityUtils() {
	}

	public SSOId currentUser() {
		return new SecurityContextUtilImpl().currentUser();
	}

	public boolean currentUserIsAdmin() {
		return new SecurityContextUtilImpl().currentUserIsAdmin();
	}

	public boolean hasWritePermissionFor(Application application) {
		return currentUserIsAdmin() || application.hasForMember(currentUser());
	}
	
	public void assertHasWritePermissionFor(Application application) {
		if (!hasWritePermissionFor(application))
			throw new AuthorizationException();
	}
	
	public void assertHasWritePermissionFor(ApplicationRelease release) {
		assertHasWritePermissionFor(release.getApplication());
	}
	
	public void assertHasWritePermissionFor(Environment environment) {
		assertHasWritePermissionFor(environment.getApplicationRelease());
	}

	public void assertHasReadPermissionFor(Environment environment) {
		assertHasReadPermissionFor(environment.getApplicationRelease());	
	}

	public void assertHasReadPermissionFor(ApplicationRelease applicationRelease) {
		assertHasReadPermissionFor(applicationRelease.getApplication());
	}

	private void assertHasReadPermissionFor(Application application) {
		if (!hasReadPermissionFor(application))
			throw new AuthorizationException();
	}

	private boolean hasReadPermissionFor(Application application) {
		return currentUserIsAdmin() || application.hasForMember(currentUser()) || application.isPublic();
	}

	public boolean hasWritePermissionFor(Environment environment) {
		return hasWritePermissionFor(environment.getApplicationRelease());
	}

	private boolean hasWritePermissionFor(ApplicationRelease applicationRelease) {
		return hasWritePermissionFor(applicationRelease.getApplication());
	}
}
