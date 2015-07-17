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

import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;

/**
 * This class provides a singleton bean instantiated by spring which accesses the LDAP server. This is used to verify
 * that the spring LDAP configuration is correct. This ensures a fail-fast behavior when instantiating a PaaS platform
 * in case of a misconfiguration.<br>
 * <br>
 * The LDAP connection created by this bean is also used to register a new user to the necessary LDAP groups to use the
 * PaaS.
 */
public class LdapAccessChecker {

	private LdapTemplate ldapTemplate;

	private String paasGroupDn;

	private String splunkGroupDn;

	private String nexusGroupDn;

	private String userDn;

	private boolean autoRegisterUser;

	public void check() {
		ldapTemplate.lookup(paasGroupDn);
		ldapTemplate.lookup(splunkGroupDn);
		ldapTemplate.lookup(nexusGroupDn);
		ldapTemplate.lookup(userDn);
	}

	/**
	 * @param ldapTemplate
	 *            the ldapTemplate to set
	 */
	public void setLdapTemplate(LdapTemplate ldapTemplate) {
		this.ldapTemplate = ldapTemplate;
	}

	/**
	 * @param groupDn
	 *            the groupDn to set
	 */
	public void setPaasGroupDn(String paasGroupDn) {
		this.paasGroupDn = paasGroupDn;
	}

	/**
	 * @param splunkGroupDn
	 *            the splunkGroupDn to set
	 */
	public void setSplunkGroupDn(String splunkGroupDn) {
		this.splunkGroupDn = splunkGroupDn;
	}

	/**
	 * @param nexusGroupDn
	 *            the nexusGroupDn to set
	 */
	public void setNexusGroupDn(String nexusGroupDn) {
		this.nexusGroupDn = nexusGroupDn;
	}

	/**
	 * @param userDn
	 *            the userDn to set
	 */
	public void setUserDn(String userDn) {
		this.userDn = userDn;
	}

	/**
	 * @return true if we must auto register the user
	 */
	public boolean isAutoRegisterUser() {
		return autoRegisterUser;
	}

	/**
	 * @param autoRegisterUser
	 *            enable or disable user auto registration
	 */
	public void setAutoRegisterUser(boolean autoRegisterUser) {
		this.autoRegisterUser = autoRegisterUser;
	}

	public void addUserToPaasUserGroup(final String uid) {
		DirContextOperations ctx = ldapTemplate.lookupContext("cn=user," + paasGroupDn);
		ctx.addAttributeValue("uniqueMember", "uid=" + uid + "," + userDn);
		ldapTemplate.modifyAttributes(ctx);
	}

	public void addUserToSplunkUserGroup(final String uid) {
		DirContextOperations ctx = ldapTemplate.lookupContext("cn=user," + splunkGroupDn);
		ctx.addAttributeValue("uniqueMember", "uid=" + uid + "," + userDn);
		ldapTemplate.modifyAttributes(ctx);
	}

	public void addUserToNexusUserGroup(final String uid) {
		DirContextOperations ctx = ldapTemplate.lookupContext("cn=ui-basic," + nexusGroupDn);
		ctx.addAttributeValue("uniqueMember", "uid=" + uid + "," + userDn);
		ldapTemplate.modifyAttributes(ctx);
	}

	public void removeUserFromPaasUserGroup(final String uid) {
		DirContextOperations ctx = ldapTemplate.lookupContext("cn=user," + paasGroupDn);
		ctx.removeAttributeValue("uniqueMember", "uid=" + uid + "," + userDn);
		ldapTemplate.modifyAttributes(ctx);
	}

	public void removeUserFromSplunkUserGroup(final String uid) {
		DirContextOperations ctx = ldapTemplate.lookupContext("cn=user," + splunkGroupDn);
		ctx.removeAttributeValue("uniqueMember", "uid=" + uid + "," + userDn);
		ldapTemplate.modifyAttributes(ctx);
	}

	public void removeUserFromNexusUserGroup(final String uid) {
		DirContextOperations ctx = ldapTemplate.lookupContext("cn=ui-basic," + nexusGroupDn);
		ctx.removeAttributeValue("uniqueMember", "uid=" + uid + "," + userDn);
		ldapTemplate.modifyAttributes(ctx);
	}
}
