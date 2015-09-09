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
package com.francetelecom.clara.cloud.techmodel.cf;

import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashSet;
import java.util.Set;

/**
 * used to scope app, route, services
 *
 */
@XmlRootElement
@Entity
public class Space extends XaasSubscription {

	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "value", column = @Column(name = "space_name")) })
	private SpaceName spaceName;

	@XmlElement(name = "organization")
	@OneToOne(cascade = CascadeType.ALL)
	private Organization organization;

	public Space() {
		super();
		// set a default space name
		setSpaceName(new SpaceName("undefined"));
		setOrganization(new Organization());
	}

	public Space(Organization organization) {
		super();
		// set a default space name
		setSpaceName(new SpaceName("undefined"));
		setOrganization(organization);
	}


	public void activate(SpaceName spaceName) {
		setDeploymentState(DeploymentStateEnum.CREATED);
		setSpaceName(spaceName);
	}
	
	public void delete() {
		setDeploymentState(DeploymentStateEnum.REMOVED);
	}

	public SpaceName getSpaceName() {
		return spaceName;
	}

	@Override
	public Set<DependantModelItem> listDepedencies() {
		Set<DependantModelItem> dependantModelItems = new HashSet<>();
		dependantModelItems.add(organization);
		return dependantModelItems;
	}

	private void setSpaceName(SpaceName name) {
		Assert.notNull(name, "Fail to set space name. space name should not be empty.");
		this.spaceName = name;
	}

	private void setOrganization(Organization organization) {
		Assert.notNull(organization, "Fail to set organization, it should not be empty.");
		this.organization = organization;
	}


}
