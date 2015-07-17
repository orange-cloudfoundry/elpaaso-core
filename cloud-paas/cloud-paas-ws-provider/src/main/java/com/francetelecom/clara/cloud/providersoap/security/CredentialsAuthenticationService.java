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

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.orange.clara.cloud.providersoap.security.v1.Credentials;


/**
 * Processes a {@link Credentials} authentication.
 *
 */
public class CredentialsAuthenticationService implements AuthenticationService<Credentials>  {
	
	private AuthenticationManager authenticationManager;

	public CredentialsAuthenticationService(AuthenticationManager authenticationManager) {
		this.setAuthenticationManager(authenticationManager);
	}

	@Override
	public void authenticate(Credentials credentials) throws AuthenticationFailedException {
		if (credentials == null)
			throw new AuthenticationFailedException("No user credentials provided");
		Authentication authentication = new UsernamePasswordAuthenticationToken(credentials.getSsoid(), credentials.getPassword());
		authentication = authenticationManager.authenticate(authentication);
		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	
	private void setAuthenticationManager(AuthenticationManager authenticationManager) {
		if (authenticationManager == null)
			throw new IllegalArgumentException("Cannot create AuthenticationService. No authentication manager is provided.");
		this.authenticationManager = authenticationManager;
	}

}
