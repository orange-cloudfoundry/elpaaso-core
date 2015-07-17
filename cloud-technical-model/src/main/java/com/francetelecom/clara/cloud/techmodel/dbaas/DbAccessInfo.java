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

import org.springframework.util.Assert;

@Embeddable
public class DbAccessInfo {

	private String hostname;
	private Integer port;
	private String dbName;

	// JPA required
	protected DbAccessInfo() {
	}

	public DbAccessInfo(String hostname, int port, String dbname) {
		this.setHostname(hostname);
		this.setPort(port);
		this.setDbName(dbname);
	}

	public String getHostname() {
		return hostname;
	}

	public int getPort() {
		if (port == null) {
			return 0;
		} else {
			return port;
		}
	}

	public String getDbName() {
		return dbName;
	}

	private void setHostname(String hostname) {
		Assert.hasText(hostname, "Fail to create database access info. hostname value <" + hostname + "> should not be empty.");
		this.hostname = hostname;
	}
	
	private void setPort(int port) {
		Assert.isTrue(port >= 0, "Fail to create database access info. port number <" + port + "> is invalid.");
		this.port = port;
	}
	
	private void setDbName(String dbname) {
		Assert.hasText(dbname, "Fail to create database access info. dbName value <" + dbname + "> should not be empty.");
		this.dbName = dbname;
	}
		
}