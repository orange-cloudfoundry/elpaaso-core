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
package com.francetelecom.clara.cloud.coremodel;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.Assert;

import com.francetelecom.clara.cloud.commons.UUIDUtils;

/**
 * User of the PAAS
 * 
 */

@XmlRootElement
@Entity
@Table(name = "CORE_PAAS_USER")
public class PaasUser extends CoreItem {

	/**
	 * pass user sso id. Mandatory.
	 * 
	 * In the case of usage of the Gassi authentication framework, this maps to
	 * 
	 * @{link com.francetelecom.clara.security.User#getId()}. The assumption is
	 *        that login/password was validated by the SSO framework.
	 */
	@Embedded
	@NotNull
	private SSOId ssoId;

	@NotNull
	private String lastName;

	@NotNull
	private String firstName;

	/**
	 * pass user mail. Mandatory. Considered for use for traceability for IOSW
	 * account i.e.
	 * <ul>
	 * <li>when an environment is used and consummes XaaS subcription such as
	 * WSOI/WS broker,
	 * <li>and the environment triggers issues with the XaaS (e.g. Ws broker
	 * usage is higher than registered usage, or SSL certificate expires)
	 * <li>then the email address of the PaasUsers (who created the environment,
	 * started/stopped it...) may be used as a contact point
	 * </ul>
	 */
	@NotNull
	private String mail;

	private String subTenant;

	/**
	 * pass user phone. Optional.
	 */
	private String phone;

	@NotNull
	private PaasRoleEnum paasUserRole;

	/**
	 * required by jpa.
	 */
	public PaasUser() {
	}

	/**
	 * public constructor.
	 * 
	 * @param firstname
	 *            paas user first name. (ex: Bob)
	 * @param lastname
	 *            paas user last name. (ex: Dylan)
	 * @param ssoId
	 *            pass user sso id.
	 * @param mail
	 *            pass user mail.
	 * @Deprecated
	 */
	public PaasUser(String firstname, String lastname, SSOId ssoId, String mail) {
		super(UUIDUtils.generateUUID("u"));
		setSsoId(ssoId);
		setPaasUserRole(PaasRoleEnum.ROLE_USER);
		setFirstName(firstname);
		setLastName(lastname);
		setMail(mail);
	}

	/**
	 * @return full name (firstname lastname).
	 *         <P>
	 *         ex : bob dylan
	 */
	public String getFullName() {
		return firstName + " " + lastName;
	}

	public void setFirstName(String firstName) {
		Assert.hasText(firstName, "Cannot set paas user first name. Paas user first name cannot be empty.");
		this.firstName = firstName;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		Assert.hasText(lastName, "Cannot set paas user last name. Paas user last name cannot be empty.");
		this.lastName = lastName;
	}

	public String getLastName() {
		return lastName;
	}

	public SSOId getSsoId() {
		return ssoId;
	}

	public void setSsoId(SSOId ssoId) {
		Assert.notNull(ssoId, "Invalid ssoId value <" + ssoId + ">. Paas user ssoId cannot be empty.");
		this.ssoId = ssoId;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		Assert.hasText(mail, "Invalid mail value <" + mail + ">. Paas user mail cannot be empty.");
		this.mail = mail;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public PaasRoleEnum getPaasUserRole() {
		return paasUserRole;
	}

	public void setPaasUserRole(PaasRoleEnum paasUserRole) {
		Assert.notNull(paasUserRole, "Invalid user role value <" + paasUserRole + ">. Paas user role cannot be empty.");
		this.paasUserRole = paasUserRole;
	}

	public String getSubTenant() {
		return subTenant;
	}

	public void setSubTenant(String subTenant) {
		this.subTenant = subTenant;
	}
}
