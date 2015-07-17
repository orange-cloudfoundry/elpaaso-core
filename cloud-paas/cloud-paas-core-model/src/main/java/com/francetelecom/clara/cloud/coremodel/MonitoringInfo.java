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

import java.util.Date;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import com.francetelecom.clara.cloud.commons.UUIDUtils;
import com.francetelecom.clara.cloud.model.ModelItem;

/**
 * Used to map an entity object to a monitoring resource
 */
@XmlRootElement
@Entity
@Table(name = "MONITORING_INFO")
@NamedQueries({
	@NamedQuery(name = "MonitoringInfo.findByExternalResourceId", query = "SELECT i FROM MonitoringInfo i where i.externalResourceId = :externalResourceId"),
	@NamedQuery(name = "MonitoringInfo.findByModelItem", query = "SELECT i FROM MonitoringInfo i where i.entityName = :modelItemName"),
	@NamedQuery(name = "MonitoringInfo.findByModelItemNotOk", query = "SELECT i FROM MonitoringInfo i where i.entityName = :modelItemName AND i.status <> 'A_OK'"),
	@NamedQuery(name = "MonitoringInfo.findByEnvironment", query = "SELECT i FROM MonitoringInfo i where i.id = :environmentId"),
	@NamedQuery(name = "MonitoringInfo.findByEnvironmentNotOk", query = "SELECT i FROM MonitoringInfo i where i.id = :environmentId AND i.status <> 'A_OK'")
})
public class MonitoringInfo extends CoreItem {
	
	private static final long serialVersionUID = 3002192464317037804L;

	@NotNull
	@ManyToOne
	private Environment environment;

	//@ManyToOne
	private String entityName;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private MonitoringInfoTypeEnum type;
	
	@NotNull
	private Date lastUpdate;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	private MonitoringStatusEnum status;
	
	/**
	 * Use this field to store the resource ID of your monitoring system
	 */
	private String externalResourceId;
	
	private Float value;

	/**
	 * Only here for persistance, you must use {@link #MonitoringInfo(Environment, ModelItem, MonitoringInfoTypeEnum)}
	 */
	public MonitoringInfo() {
		super(UUIDUtils.generateUUID());
		this.environment = null;
		//this.entity = null;
		this.type = MonitoringInfoTypeEnum.VM;
		this.status = MonitoringStatusEnum.B_UNKNOWN;
		this.value = null;
		this.lastUpdate = new Date();
		this.externalResourceId = null;
	}

	public MonitoringInfo(Environment environment, String entityName, MonitoringInfoTypeEnum type) {
		super(UUIDUtils.generateUUID());
		this.environment = environment;
		this.entityName = entityName;
		this.type = type;
		this.status = MonitoringStatusEnum.B_UNKNOWN;
		this.value = null;
		this.lastUpdate = new Date();
		this.externalResourceId = null;
	}

	public Environment getEnvironment() {
		return environment;
	}

	public String getEntityName() {
		return entityName;
	}

	public MonitoringInfoTypeEnum getType() {
		return type;
	}

	public Date getLastUpdate() {
		return lastUpdate;
	}

	public MonitoringStatusEnum getStatus() {
		return status;
	}

	public Float getValue() {
		return value;
	}

	public String getExternalResourceId() {
		return externalResourceId;
	}

	public void setExternalResourceId(String externalResourceId) {
		this.externalResourceId = externalResourceId;
	}
}
