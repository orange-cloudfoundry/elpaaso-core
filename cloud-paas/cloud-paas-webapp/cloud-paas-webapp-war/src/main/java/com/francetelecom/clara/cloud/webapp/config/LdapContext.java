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

import com.francetelecom.clara.cloud.presentation.tools.LdapAccessChecker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool.factory.MutablePoolingContextSource;
import org.springframework.ldap.pool.validation.DefaultDirContextValidator;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.ldap.userdetails.LdapUserDetailsService;

/**
 * Created by WOOJ7232 on 26/05/2015.
 */
@Configuration
public class LdapContext {


	@Value("${ldap.url}")
	private String ldapUrl;
	@Value("${ldap.manager_dn}")
	private String managerUserDn;
	@Value("${ldap.manager_password}")
	private String managerPasword;

	@Value("${ldap.paas_group_dn}")
	private String paasGroupDn;

	@Value("${ldap.splunk_group_dn}")
	private String splunkGroupDn;

	@Value("${ldap.nexus_group_dn}")
	private String nexusGroupDn;
	@Value("${ldap.user_base_dn}")
	private String userDn;
	@Value("${ldap.auto_add_user_groups.enabled}")
	private boolean autoRegisterUser;


	@Bean
	public DefaultDirContextValidator dirContextValidator() {
		//	<bean id="dirContextValidator" class="org.springframework.ldap.pool.validation.DefaultDirContextValidator"/>
		return new DefaultDirContextValidator();
	}

	@Bean
	public MutablePoolingContextSource securityContextSource() {
//	<bean id="securityContextSource"
//	class="org.springframework.ldap.pool.factory.MutablePoolingContextSource">
//	<property name="contextSource" ref="sdContextSourceTarget" />
//	<property name="dirContextValidator" ref="dirContextValidator" />
//	<property name="testOnBorrow" value="false" />
//	<property name="testWhileIdle" value="true" />
//	<property name="minIdle" value="3" />
//	<property name="maxIdle" value="8" />
//	<property name="maxActive" value="100" />
//	<property name="maxTotal" value="100" />
//	<property name="maxWait" value="100" />
//	<property name="timeBetweenEvictionRunsMillis" value="60000" />
//	<property name="minEvictableIdleTimeMillis" value="180000" />
//	</bean>
		MutablePoolingContextSource mutablePoolingContextSource = new MutablePoolingContextSource();
		mutablePoolingContextSource.setContextSource(sdContextSourceTarget());
		mutablePoolingContextSource.setDirContextValidator(dirContextValidator());
		mutablePoolingContextSource.setTestOnBorrow(false);
		mutablePoolingContextSource.setTestWhileIdle(true);
		mutablePoolingContextSource.setMinIdle(3);
		mutablePoolingContextSource.setMaxIdle(8);
		mutablePoolingContextSource.setMaxActive(100);
		mutablePoolingContextSource.setMaxTotal(100);
		mutablePoolingContextSource.setMaxWait(100);
		mutablePoolingContextSource.setTimeBetweenEvictionRunsMillis(60000);
		mutablePoolingContextSource.setMinEvictableIdleTimeMillis(180000);

		return mutablePoolingContextSource;
	}

	//	<bean id="sdContextSourceTarget" class="org.springframework.ldap.core.support.LdapContextSource">
//	<property name="url" value="${ldap.url}" />
//	<property name="userDn" value="${ldap.manager_dn}" />
//	<property name="password" value="${ldap.manager_password}" />
//	<property name="pooled" value="false" />
//	</bean>
	@Bean
	public LdapContextSource sdContextSourceTarget() {
		LdapContextSource ldapContextSource = new LdapContextSource();

		ldapContextSource.setUrl(ldapUrl);
		ldapContextSource.setUserDn(managerUserDn);
		ldapContextSource.setPassword(managerPasword);
		ldapContextSource.setPooled(false);
		ldapContextSource.afterPropertiesSet();
		return ldapContextSource;
	}

	//	<bean id="ldapTemplate" class="org.springframework.ldap.core.LdapTemplate">
//	<constructor-arg ref="securityContextSource" />
//	</bean>
	@Bean
	public LdapTemplate ldapTemplate() {
		return new LdapTemplate(securityContextSource());
	}

	@Bean(initMethod = "check")
	public LdapAccessChecker ldapAccessChecker() {
		//	<bean id="ldapAccessChecker" class="com.francetelecom.clara.cloud.presentation.tools.LdapAccessChecker" init-method="check">
		//	<property name="ldapTemplate" ref="ldapTemplate" />
		//	<property name="paasGroupDn" value="${ldap.paas_group_dn}"/>
		//	<property name="splunkGroupDn" value="${ldap.splunk_group_dn}"/>
		//	<property name="nexusGroupDn" value="${ldap.nexus_group_dn}"/>
		//	<property name="userDn" value="${ldap.user_base_dn}"/>
		//	<property name="autoRegisterUser" value="${ldap.auto_add_user_groups.enabled}"/>
		//	</bean>
 		LdapAccessChecker ldapAccessChecker = new LdapAccessChecker();
		ldapAccessChecker.setLdapTemplate(ldapTemplate());
		ldapAccessChecker.setPaasGroupDn(paasGroupDn);
		ldapAccessChecker.setSplunkGroupDn(splunkGroupDn);
		ldapAccessChecker.setNexusGroupDn(nexusGroupDn);
		ldapAccessChecker.setUserDn(userDn);
		ldapAccessChecker.setAutoRegisterUser(autoRegisterUser);
		return ldapAccessChecker;
	}


	public String getPaasGroupDn() {
		return paasGroupDn;
	}

	public void setPaasGroupDn(String paasGroupDn) {
		this.paasGroupDn = paasGroupDn;
	}

	public String getUserDn() {
		return userDn;
	}

	public void setUserDn(String userDn) {
		this.userDn = userDn;
	}


}
