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

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.orange.clara.cloud.providersoap.security.v1.Credentials;


public class AuthenticationServiceTest {

	@Test(expected=IllegalArgumentException.class)
	public void fail_to_create_AuthenticationService_if_no_authentication_manager() {
		new CredentialsAuthenticationService(null);
	}
	
	@Test(expected=AuthenticationFailedException.class)
	public void fail_to_authenticate_if_no_credentials_provided() {
		CredentialsAuthenticationService authenticationService = new CredentialsAuthenticationService(Mockito.mock(AuthenticationManager.class));
		authenticationService.authenticate(null);
	}
		
	@Test
	public void should_set_authentication_token_in_thread_context_after_authentication() {
		AuthenticationManager authenticationManager  = Mockito.mock(AuthenticationManager.class);
		Authentication authentication = Mockito.mock(Authentication.class);
		Mockito.when(authenticationManager.authenticate(Mockito.isA(Authentication.class))).thenReturn(authentication);
		CredentialsAuthenticationService authenticationService = new CredentialsAuthenticationService(authenticationManager);
		Credentials credentials = new Credentials();
		authenticationService.authenticate(credentials);
		Assert.assertEquals(authentication,SecurityContextHolder.getContext().getAuthentication());
	}

}
