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
package com.francetelecom.clara.cloud.techmodel.cf.services.userprovided;

import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import com.francetelecom.clara.cloud.techmodel.cf.UserProvidedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.ServiceNameSpecification;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

/**
 *
 */
@XmlRootElement
@Entity
@Table(name = "CF_UPS_V2")
public abstract class AbstractUserProvidedService extends XaasSubscription implements UserProvidedService {

	private String serviceName;

	@XmlElement(name = "space")
	@OneToOne
	private Space space;

	protected AbstractUserProvidedService() {
	}

	public AbstractUserProvidedService(TechnicalDeployment td, String serviceName, Space space) {
		super(td);
		setServiceName(serviceName);
		setSpace(space);
	}

	public String getServiceName() {
		return serviceName;
	}

	@Override
	public SpaceName getSpace() {
		return space.getSpaceName();
	}

	public void setSpace(Space space) {
		Assert.notNull(space, "unable to scope service <" + serviceName + "> to space <" + space + ">. invalid space.");
		this.space = space;
	}

	private void setServiceName(String serviceName) {
		serviceNameSpecification().assertIsSatisfiedBy(serviceName);
		this.serviceName = serviceName;
	}

	@Override
	public void delete() {
		setDeploymentState(DeploymentStateEnum.REMOVED);
	}

	@Override
	public void activate() {
		setDeploymentState(DeploymentStateEnum.CREATED);
	}
	
	@Override
	public Set<DependantModelItem> listDepedencies() {
		Set<DependantModelItem> dependantModelItems = new HashSet<>();
		dependantModelItems.add(space);
		return dependantModelItems;
	}

	public abstract ServiceNameSpecification serviceNameSpecification();

}
