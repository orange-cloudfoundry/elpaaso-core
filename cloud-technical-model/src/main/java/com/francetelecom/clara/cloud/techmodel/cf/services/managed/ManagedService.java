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
package com.francetelecom.clara.cloud.techmodel.cf.services.managed;

import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import com.francetelecom.clara.cloud.techmodel.cf.services.DefaultServiceNameSpecification;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

@XmlRootElement
@Entity
public class ManagedService extends XaasSubscription {

	private String serviceInstance;
	
	private String service;
	
	private String plan;
	
	@XmlElement(name = "space")
	@OneToOne
	private Space space;

	protected ManagedService() {
	}

	public ManagedService(String service, String plan, String serviceInstance, Space space, TechnicalDeployment td) {
		super(td);
		setPlan(plan);
		setService(service);
		setServiceInstance(serviceInstance);
		setSpace(space);
	}

	public void activate() {
		setDeploymentState(DeploymentStateEnum.CREATED);
	}
	
	public void delete() {
		setDeploymentState(DeploymentStateEnum.REMOVED);
	}

	private void setService(String service) {
		Assert.hasText(plan,"unable to set service type <"+service+">. invalid type.");
		this.service = service;
	}
	
	private void setPlan(String plan) {
		Assert.hasText(plan,"unable to set service plan <"+plan+">. invalid plan.");
		this.plan = plan;
	}

	private void setServiceInstance(String serviceName) {
		new DefaultServiceNameSpecification().assertIsSatisfiedBy(serviceName);
		this.serviceInstance = serviceName;
	}

	private void setSpace(Space space) {
		Assert.notNull(space,"unable to scope service <"+serviceInstance+"> to space <"+space+">. invalid space.");
		this.space = space;
	}

	public String getService() {
		return service;
	}

	public String getServiceInstance() {
		return serviceInstance;
	} 
	
	public String getPlan() {
		return plan;
	}

	public SpaceName getSpace() {
		return space.getSpaceName();
	}
	
	@Override
	public Set<DependantModelItem> listDepedencies() {
		Set<DependantModelItem> dependantModelItems = new HashSet<>();
		dependantModelItems.add(space);
		return dependantModelItems;
	}

}

