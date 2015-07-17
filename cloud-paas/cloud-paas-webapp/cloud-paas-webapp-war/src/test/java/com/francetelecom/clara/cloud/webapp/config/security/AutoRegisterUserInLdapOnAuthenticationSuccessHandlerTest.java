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
package com.francetelecom.clara.cloud.webapp.config.security;

import com.francetelecom.clara.cloud.presentation.tools.LdapAccessChecker;
import com.francetelecom.clara.cloud.presentation.utils.AuthenticationUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by WOOJ7232 on 26/06/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class AutoRegisterUserInLdapOnAuthenticationSuccessHandlerTest {

    @Mock
    LdapAccessChecker ldapAccessChecker;


    @Mock
    HttpServletRequest request;

    @Mock
    HttpServletResponse response;

    @InjectMocks
    AutoRegisterUserInLdapOnAuthenticationSuccessHandler autoRegisterUserInLdapOnAuthenticationSuccessHandler;

    @Before
    public void setup() {

    }


    @Test
    public void should_not_register_users_in_groups_when_disabled() {

        // Prepare mocks
        String login = "testuser";

        Mockito.when(ldapAccessChecker.isAutoRegisterUser()).thenReturn(Boolean.FALSE);

        // Then
        AuthenticationUtil.connectWithoutRoleAs(login);
        //Authentication authentication = new UsernamePasswordAuthenticationToken(login, pwd);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            autoRegisterUserInLdapOnAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //myTester.assertErrorMessages("Sorry, you don't have permission to access the application.");
        Mockito.verify(ldapAccessChecker, Mockito.times(0)).addUserToNexusUserGroup(login);
        Mockito.verify(ldapAccessChecker, Mockito.times(0)).addUserToPaasUserGroup(login);
        Mockito.verify(ldapAccessChecker, Mockito.times(0)).addUserToSplunkUserGroup(login);

    }

    @Test
    public void should_not_register_user_in_groups_when_all_ready_registred() {

        // Prepare mocks
        String login = "testuser";

        Mockito.when(ldapAccessChecker.isAutoRegisterUser()).thenReturn(Boolean.TRUE);

        // When
        // Then
        AuthenticationUtil.connect(login, "ROLE_USER");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            autoRegisterUserInLdapOnAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Then
        Mockito.verify(ldapAccessChecker, Mockito.times(0)).addUserToNexusUserGroup(login);
        Mockito.verify(ldapAccessChecker, Mockito.times(0)).addUserToPaasUserGroup(login);
        Mockito.verify(ldapAccessChecker, Mockito.times(0)).addUserToSplunkUserGroup(login);
    }


    @Test
    public void should_register_users_in_groups_when_enabled() {

        // Prepare mocks
        String login = "testuser";

        Mockito.when(ldapAccessChecker.isAutoRegisterUser()).thenReturn(Boolean.TRUE);

        // When
        // Then
        AuthenticationUtil.connectWithoutRoleAs(login);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        try {
            autoRegisterUserInLdapOnAuthenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        } catch (ServletException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Then
        Mockito.verify(ldapAccessChecker).addUserToNexusUserGroup(login);
        Mockito.verify(ldapAccessChecker).addUserToPaasUserGroup(login);
        Mockito.verify(ldapAccessChecker).addUserToSplunkUserGroup(login);
    }
}
