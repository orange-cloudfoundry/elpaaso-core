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
package com.francetelecom.clara.cloud.presentation.tools;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;

import org.apache.wicket.request.Request;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.presentation.login.UnauthorizedAccess;

@RunWith(MockitoJUnitRunner.class)
public class WicketSessionTest {

	@Mock
	private Request requestMock;
	
	@Mock
	private AuthenticationManager authenticationManager;
	
	@Mock
	private ManagePaasUser managePaasUser;
	
	@Mock
	private Logger logger;
	
	private WicketSession session;
	
	@Before
	public void init(){
		getAuthenticationWithRole(PaasRoleEnum.ROLE_ADMIN.toString());



		when(requestMock.getLocale()).thenReturn(Locale.getDefault());
		
		session = new WicketSession(requestMock){
			@Override
			protected void injectDependencies() {
				setAuthenticationManager(authenticationManager);
				setManagePassUser(managePaasUser);
			}
			
			@Override
			protected WicketSession getSession() {
				return session;
			}

			@Override
			protected Logger getLoginLogger() {
				return logger;
			}
		};
	}

	private Authentication getAuthenticationWithRole(String... roles) {

		List<GrantedAuthority> grantedAuthorities=null;
		if (roles !=null) {
			grantedAuthorities = AuthorityUtils.createAuthorityList(roles);
		}
		UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken("logintest", "password", grantedAuthorities);
		return usernamePasswordAuthenticationToken;
	}

	@Ignore
	@Test
	public void test_a_log_is_done_if_authentication_is_ok() throws Exception {
		//GIVEN
		when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(getAuthenticationWithRole(PaasRoleEnum.ROLE_ADMIN.toString()));


		session = new WicketSession(requestMock){
			@Override
			protected void injectDependencies() {
				setAuthenticationManager(authenticationManager);
				setManagePassUser(managePaasUser);
			}

			@Override
			protected WicketSession getSession() {
				return session;
			}

			@Override
			protected Logger getLoginLogger() {
				return logger;
			}
		};

		session.getRoles();
		//WHEN
//		session.authenticate("logintest", "password");
//		session.
		
		//THEN
		
		
		ArgumentCaptor<String> logCaptor = ArgumentCaptor.forClass(String.class);
		verify(logger).info(logCaptor.capture());
		
		assertTrue(logCaptor.getValue().contains("logintest"));
		assertTrue(logCaptor.getValue().contains("logged IN"));
		
	}
	
	/**
	 * This test is added to ensure that if ROLE_ADMIN and ROLE_USER are assigned to a user; the actual role set in paas
	 * is ROLE_ADMIN
	 */
	@Test
	public void paas_role_admin_should_override_paas_role_user() {
		// GIVEN
		Authentication authentication = getAuthenticationWithRole("ROLE_USER","ROLE_ADMIN","ROLE_USER");

		// WHEN
		PaasRoleEnum role = session.workoutPaasRole(authentication);
		
		// THEN
		assertEquals(PaasRoleEnum.ROLE_ADMIN, role);
	}

	@Test(expected = UnauthorizedAccess.class)
	public void user_without_role_should_throw_an_exception() {
		// GIVEN
		Authentication authentication = getAuthenticationWithRole(null);
		// WHEN
			session.workoutPaasRole(authentication);
			fail("User without a role must throw an exception.");

	}

	@Test
	public void user_with_invalid_role_should_throw_an_exception() {
		// GIVEN
		Authentication authentication = getAuthenticationWithRole("ROLE_UNKNOWN");

		// WHEN
		Exception exc = null;
		try {
			session.workoutPaasRole(authentication);
			fail("User with an invalid role must throw an exception.");
		} catch (Exception e) {
			exc = e;
		}

		// THEN
		assertThat(exc).isInstanceOf(UnauthorizedAccess.class);
	}



}
