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
package com.francetelecom.clara.cloud.techmodel.dbaas;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

@Embeddable
public class DbOwner {

	/**
	 * The dbOwnerName of the user that will manage the database. This user has admin
	 * privileges and can create additional users with restricted privileges.
	 * Lower case and digit characters and length between 4 and 16.
	 */
	@NotNull
	private String dbOwnerName;

	/**
	 * The dbOwnerPwd of the user that will manage the database. Upper and lower
	 * case letters A-Z and the digits 0-9 with at least one uppercase, one
	 * lowercase and one digit character. Length must be between 8 to 12
	 * characters.
	 *
	 */
	@Type(type = "encryptedString")
	@NotNull
	private String dbOwnerPwd;

	protected DbOwner() {
	}

	public DbOwner(String name, String password) {
		super();
		setName(name);
		setPassword(password);
	}

	public String getName() {
		return dbOwnerName;
	}

	private void setName(String name) {
		Assert.hasText(name, "Fail to create database owner. owner name value <" + name + "> should not be empty.");
		this.dbOwnerName = name;
	}

	public String getPassword() {
		return dbOwnerPwd;
	}

	private void setPassword(String password) {
		Assert.hasText(password, "Fail to create database owner. owner password value <" + password + "> should not be empty.");
		this.dbOwnerPwd = password;
	}

}
