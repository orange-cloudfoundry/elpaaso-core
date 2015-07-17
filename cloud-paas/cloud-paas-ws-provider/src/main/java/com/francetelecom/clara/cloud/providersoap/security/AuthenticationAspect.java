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
package com.francetelecom.clara.cloud.providersoap.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;

import com.orange.clara.cloud.providersoap.security.v1.Credentials;


public class AuthenticationAspect {

	private AuthenticationService<Credentials> authenticationService;

	private static final Logger LOG = LoggerFactory.getLogger(AuthenticationAspect.class);

	public AuthenticationAspect(AuthenticationService<Credentials> authenticationService) {
		super();
		setAuthenticationService(authenticationService);
	}

	public void authenticate(Credentials credentials) {
		if (credentials == null)
			throw new AuthenticationFailedException("No user credentials provided");
		LOG.debug("aspect called for credentials with ssoid<" + credentials.getSsoid() + ">");
		try {
			authenticationService.authenticate(credentials);
		} catch (AuthenticationException e) {
			LOG.warn("authentication error for ssoid<" + credentials.getSsoid() + "> : " + e);
			throw new AuthenticationFailedException(e);
		}
	}

	private void setAuthenticationService(AuthenticationService<Credentials> authenticationService) {
		if (authenticationService == null)
			throw new IllegalArgumentException("Cannot create AuthenticationAspect. No authentication service is provided.");
		this.authenticationService = authenticationService;
	}

}
