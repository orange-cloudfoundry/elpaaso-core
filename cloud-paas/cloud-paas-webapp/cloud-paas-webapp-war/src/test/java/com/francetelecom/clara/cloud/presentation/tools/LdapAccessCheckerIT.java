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

import com.francetelecom.clara.cloud.webapp.config.LdapContext;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class LdapAccessCheckerIT {

    @Configuration
    @ImportResource("classpath:/spring-config/ldap-checker-context.xml")
    @Import(LdapContext.class)
    static class Context{
    }

    @Autowired
    LdapAccessChecker ldapAccessChecker;

    @Bean
    public static PropertySourcesPlaceholderConfigurer placeHolderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }


    @Test
    public void test() {
        ldapAccessChecker.addUserToPaasUserGroup("testuser");
        ldapAccessChecker.addUserToSplunkUserGroup("testuser");
        ldapAccessChecker.addUserToNexusUserGroup("testuser");
    }

    @After
    public void tearDown() {
        ldapAccessChecker.removeUserFromPaasUserGroup("testuser");
        ldapAccessChecker.removeUserFromSplunkUserGroup("testuser");
        ldapAccessChecker.removeUserFromNexusUserGroup("testuser");
    }
}
