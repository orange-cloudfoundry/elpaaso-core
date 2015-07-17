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
package com.francetelecom.clara.cloud.test.database;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.EngineWsEnum;

/**
 * dbaas database bean
 * the bean provides create() and delete() operations to create and delete the database on dbaas
 * create() and delete() operations are delegated to a DbaasService bean
 */
public class DbaasDatabase {

	/**
	 * dbaas service to be used to create/delete the database
	 */
	DbaasService dbaasService;

	/**
	 * database properties
	 */
	String name;
	String uuId;
	String user;
	String password;
	String host;
	String port;
	EngineWsEnum engine;
	String description;
	
	/**
	 * flag used to tag a database that has been deleted
	 */
	boolean isDeleted = false;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUUId() {
		return uuId;
	}

	public void setUUId(String uuId) {
		this.uuId = uuId;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	protected void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	protected void setPort(String port) {
		this.port = port;
	}

	public EngineWsEnum getEngine() {
		return engine;
	}

	public void setEngine(EngineWsEnum engine) {
		this.engine = engine;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isDeleted() {
		return isDeleted;
	}

	protected void setDeleted(boolean isDeleted) {
		this.isDeleted = isDeleted;
	}

	public DbaasService getDbaasService() {
		return dbaasService;
	}

	public void setDbaasService(DbaasService dbaasService) {
		this.dbaasService = dbaasService;
	}

	/**
	 * Create the database on dbaas
	 */
	@PostConstruct
	public void create() {
		dbaasService.createDatabase(this);
	}

	/**
	 * Delete the database on dbaas
	 */
	@PreDestroy
	public void delete() {
		dbaasService.deleteDatabase(this);
	}

	/**
	 * work-out jdbc url from engine, host, port and name
	 * @return jdbc url
	 */
	public String getUrl() {
		if(name == null || host == null || port == null || engine == null) {
			throw new TechnicalException("database name, host, port or engine is null");
		}
		String driverType = null;
		switch (engine) {
		case MYSQL:
			driverType = "mysql";
			break;
		case POSTGRESQL:
			driverType = "postgresql";
			break;
		case DB_2:
			driverType = "db2";
			break;
		case ORACLE:
			driverType = "oracle";
			break;
		default:
			throw new TechnicalException("Unable to determine driver type for engine " + engine);
		}
		return "jdbc:"+driverType+"://"+host+":"+port+"/"+name;
	}
}
