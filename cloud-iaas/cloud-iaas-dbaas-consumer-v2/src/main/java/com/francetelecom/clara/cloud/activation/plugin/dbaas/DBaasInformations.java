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
package com.francetelecom.clara.cloud.activation.plugin.dbaas;

import java.util.Date;

public class DBaasInformations {
	private String dbUUId;
	private String user;
	private Date creationDate;
	private String dbStatus;

	public DBaasInformations(String dbUUId, String user, Date creationDate, String dbStatus) {
		super();
		this.dbUUId = dbUUId;
		this.user = user;
		this.creationDate = creationDate;
		this.dbStatus = dbStatus;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	/**
	 * @return the dbUUId
	 */
	public String getDbUUId() {
		return dbUUId;
	}

	/**
	 * @param dbUUId
	 *            the dbUUId to set
	 */
	public void setDbUUId(String dbUUId) {
		this.dbUUId = dbUUId;
	}

	public String getDbStatus() {
		return dbStatus;
	}
}
