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
package com.francetelecom.clara.cloud.techmodel.bindings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.xml.bind.annotation.XmlRootElement;

import org.springframework.util.Assert;

import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.DefaultServiceNameSpecification;
import com.francetelecom.clara.cloud.techmodel.cf.services.ServiceNameSpecification;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.AbstractUserProvidedService;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;

@XmlRootElement
@Entity
public class DbaasUserProvidedService extends AbstractUserProvidedService {

	@ManyToOne
	private DBaasSubscriptionV2 dBaasSubscription;

	private UriScheme uriScheme;

	/**
	 * this is the uri scheme supported by spring-cloud-connectors to trigger
	 * datasource configuration code
	 * https://github.com/spring-cloud/spring-cloud
	 * -connectors/blob/master/spring
	 * -cloud-cloudfoundry-connector/src/main/java/
	 * org/springframework/cloud/cloudfoundry
	 * /CloudFoundryServiceInfoCreator.java#L27
	 * https://github.com/spring-cloud/spring
	 * -cloud-connectors/blob/master/spring
	 * -cloud-cloudfoundry-connector/src/main
	 * /java/org/springframework/cloud/cloudfoundry
	 * /PostgresqlServiceInfoCreator.java#L13
	 * */
	public enum UriScheme {
		postgres, mysql;
	}

	/**
	 * jpa requires private constructor
	 */
	protected DbaasUserProvidedService() {
	};

	public DbaasUserProvidedService(TechnicalDeployment td, String serviceName, UriScheme uriScheme, DBaasSubscriptionV2 dBaasSubscription, Space space) {
		super(td, serviceName, space);
		setUriScheme(uriScheme);
		setdBaasSubscription(dBaasSubscription);
	}

	public String getServiceUrl() {
		StringBuilder sb = new StringBuilder(getUriScheme().toString()).append("://").append(dBaasSubscription.getUserName()).append(":")
				.append(dBaasSubscription.getUserPassword()).append("@").append(dBaasSubscription.getHostname()).append(":").append(dBaasSubscription.getPort()).append("/")
				.append(dBaasSubscription.getDbname());
		return sb.toString();
	}

	public void setdBaasSubscription(DBaasSubscriptionV2 dBaasSubscription) {
		Assert.notNull(dBaasSubscription, "Fail to create dbaas cloud foundry relational db service. dBaasSubscription <" + dBaasSubscription + "> should not be null.");
		this.dBaasSubscription = dBaasSubscription;
	}

	public void setUriScheme(UriScheme uriScheme) {
		Assert.notNull(uriScheme, "Fail to create dbaas cloud foundry relational db service. uri scheme should be set.");
		this.uriScheme = uriScheme;
	}

	// Public to support tests
	public UriScheme getUriScheme() {
		return uriScheme;
	}

	@Override
	public Set<DependantModelItem> listDepedencies() {
		Set<DependantModelItem> listDepedencies = new HashSet<DependantModelItem>();
		listDepedencies.add(dBaasSubscription);
		listDepedencies.addAll(super.listDepedencies());
		return listDepedencies;
	}

	
	@Override
	public Map<String, Object> getCredentials() {
		Map<String, Object> credentials = new HashMap<>();
		credentials.put("uri", getServiceUrl());
		
		credentials.put("host",this.dBaasSubscription.getHostname());
		credentials.put("port",this.dBaasSubscription.getPort());
		credentials.put("username",this.dBaasSubscription.getUserName());
		credentials.put("password",this.dBaasSubscription.getUserPassword());
		credentials.put("schema",this.dBaasSubscription.getDbname());		
		
		return credentials;
	}

	@Override
	public ServiceNameSpecification serviceNameSpecification() {
		return new DefaultServiceNameSpecification();
	}

	@Override
	public String getLogUrl() {
		return null;
	}

}
