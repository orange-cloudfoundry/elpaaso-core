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
package com.francetelecom.clara.cloud.presentation.utils;

import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class AuthenticationUtil {
    final static String DEFAULT_PASSWORD = "password";

    public static void connectAsAdmin() {
        connectAs("bob123", PaasRoleEnum.ROLE_ADMIN.toString());
    }

    public static Authentication connectAsUser() {
        connect("bob123", PaasRoleEnum.ROLE_USER.toString());
        return null;
    }


    public static void connectAs(String username, String... roles) {
        connect(username, roles);
    }

    public static void connectWithoutRoleAs(String username) {
        connect(username, null);
    }

    public static void connect(final String username, final String... roles) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken;
        if (roles != null) {
            List<GrantedAuthority> grantedAuthorities = AuthorityUtils.createAuthorityList(roles);
            usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, DEFAULT_PASSWORD, grantedAuthorities);
        } else {
            usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, DEFAULT_PASSWORD);
        }
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
    }
}
