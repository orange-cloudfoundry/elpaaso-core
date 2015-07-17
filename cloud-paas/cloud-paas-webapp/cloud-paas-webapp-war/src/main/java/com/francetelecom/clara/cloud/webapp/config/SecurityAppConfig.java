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
package com.francetelecom.clara.cloud.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.servlet.configuration.EnableWebMvcSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

/**
 * Created by WOOJ7232 on 26/05/2015.
 */
@Configuration
@EnableWebMvcSecurity
@EnableWebSecurity(debug = true)
//@Order(SecurityProperties.ACCESS_OVERRIDE_ORDER)
@Import(LdapContext.class)
public class SecurityAppConfig extends WebSecurityConfigurerAdapter {
    private static Logger LOGGER = LoggerFactory.getLogger(SecurityAppConfig.class);
    final String MANAGEMENT_PREFIX = "/ops";

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//		<security:http create-session="never" auto-config="true" >
//		<security:intercept-url pattern="/portal/login*" access="IS_AUTHENTICATED_ANONYMOUSLY" />
//		<security:intercept-url pattern="/portal/**" access="ROLE_ADMIN, ROLE_USER"/>
//		<security:intercept-url pattern="/togglz/**" access="ROLE_ADMIN"/>
//		<security:form-login login-page="/portal/login" default-target-url="/portal/home" always-use-default-target="true" />
//		<security:access-denied-handler error-page="/index.html"/>
//		<security:logout invalidate-session="true" logout-success-url="/portal/login" />
//		</security:http>

        final boolean alwaysUseDefaultSuccess = true;
        //@formatter:off
        http.authorizeRequests()
//                .antMatchers("**/favicon.ico").permitAll()
//                .antMatchers("**/styles/**").permitAll()
                .antMatchers("/portal/login/**").permitAll()
                .antMatchers("/portal/**").authenticated()
                .antMatchers(MANAGEMENT_PREFIX+"/health",MANAGEMENT_PREFIX+"/info").permitAll()
                .antMatchers(MANAGEMENT_PREFIX+"/beans",MANAGEMENT_PREFIX+"/trace",MANAGEMENT_PREFIX+"/dump").hasRole("ADMIN")
                .antMatchers(MANAGEMENT_PREFIX+"/env**",MANAGEMENT_PREFIX+"/env/**",MANAGEMENT_PREFIX+"/metrics**",MANAGEMENT_PREFIX+"/metrics/**").hasRole("ADMIN")
                .antMatchers(MANAGEMENT_PREFIX+"/mappings",MANAGEMENT_PREFIX+"/autoconfig",MANAGEMENT_PREFIX+"/configprops").hasRole("ADMIN")
                .antMatchers(MANAGEMENT_PREFIX+"/*").hasRole("ADMIN")
                .antMatchers("/togglz/**").hasRole("ADMIN")
                .antMatchers("/**").authenticated()
            .and()
                .formLogin()
                	.loginPage("/login.html").permitAll()
                    .defaultSuccessUrl("/portal/home",alwaysUseDefaultSuccess).permitAll()
            .and().logout().invalidateHttpSession(true).permitAll()
            .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
                .exceptionHandling().accessDeniedPage("/index.html")
            .and()
                .csrf().disable()
        ;

        //@formatter:on
    }

//    @Autowired
//    LdapContext ldapContext;
//
//    @Override
//    public void configure(AuthenticationManagerBuilder auth) throws Exception {
//        //		<security:authentication-manager alias="authenticationManager" >
//        //		<security:ldap-authentication-provider user-dn-pattern="uid={0},${ldap.user_base_dn}" group-search-base="${ldap.paas_group_dn}" />
//        //		</security:authentication-manager>
//        LOGGER.info("SecurityAppConfig.configure - authenticationManagerBuilder: {} {} {}", new Object[]{auth, auth.isConfigured(), auth.getDefaultUserDetailsService()});
//
//        auth.ldapAuthentication().groupSearchBase(ldapContext.getPaasGroupDn()).userDnPatterns("uid={0}," + ldapContext.getUserDn()).contextSource(ldapContext.sdContextSourceTarget());
//    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
            .ignoring()
            .antMatchers("/styles/**", "/images/**", "/javascripts/**");
    }

    @Configuration
    protected static class AuthenticationConfiguration extends
            GlobalAuthenticationConfigurerAdapter {

        @Autowired
        LdapContext ldapContext;


        @Override
        public void init(AuthenticationManagerBuilder auth) throws Exception {
            auth.ldapAuthentication().groupSearchBase(ldapContext.getPaasGroupDn()).userDnPatterns("uid={0}," + ldapContext.getUserDn()).contextSource(ldapContext.sdContextSourceTarget());
        }
    }


}

