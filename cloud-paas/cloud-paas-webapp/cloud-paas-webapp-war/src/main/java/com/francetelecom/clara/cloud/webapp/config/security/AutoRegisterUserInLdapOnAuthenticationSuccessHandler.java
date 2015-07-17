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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by WOOJ7232 on 25/06/2015.
 */
public class AutoRegisterUserInLdapOnAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(AutoRegisterUserInLdapOnAuthenticationSuccessHandler.class);

    @Autowired
    LdapAccessChecker ldapAccessChecker;

    private boolean isFirstLogin(Authentication authentication) {
        if (authentication==null || authentication.getAuthorities()==null|| authentication.getAuthorities().size() ==0){
            return true;
        }
        return false;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
        LOGGER.info("onAuthenticationSuccess", authentication);
        if (ldapAccessChecker.isAutoRegisterUser() && isFirstLogin(authentication)){

            LOGGER.warn("User " + getAuthenticationName(authentication) + " does not have the rights to access the application : registering him to the right LDAP groups.");
            try {
                assignCurrentUserToLdapGroups(authentication);
                forceReloginAfterLdapGroupUpdate(request, response);
            } catch (Exception exc) {
                LOGGER.error("Error registering user {}  to LDAP groups.", getAuthenticationName(authentication), exc);
            }
        } else {
            super.onAuthenticationSuccess(request, response, authentication);

            if (isFirstLogin(authentication)) {
                LOGGER.error("Authenticated user does not have PaaS usage rights: {}. Manual action required !", getAuthenticationName(authentication));
            }
        }

    }

    private String getAuthenticationName(Authentication authentication) {
        return authentication!=null?authentication.getName():null;
    }

    private void forceReloginAfterLdapGroupUpdate(HttpServletRequest request, HttpServletResponse response) throws IOException {
        getRedirectStrategy().sendRedirect(request, response, "/login");
    }

    private void assignCurrentUserToLdapGroups(Authentication authentication) {
        ldapAccessChecker.addUserToPaasUserGroup(authentication.getName());
        ldapAccessChecker.addUserToSplunkUserGroup(authentication.getName());
        ldapAccessChecker.addUserToNexusUserGroup(authentication.getName());
    }
}
