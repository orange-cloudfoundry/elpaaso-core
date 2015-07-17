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
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.Set;

/**
 * used to scope app, route, services
 *
 */
@XmlRootElement
@Entity
public class Organization extends XaasSubscription {
    public static final String UNDEFINED_ORGANIZATION_NAME = "undefined";

	private String organizationName;

	protected Organization() {
	}

	public Organization(TechnicalDeployment td) {
		super(td);
		// set a default space name
		setOrganizationName(UNDEFINED_ORGANIZATION_NAME);
	}

	public void activate(String orgName) {
		setDeploymentState(DeploymentStateEnum.CREATED);
		setOrganizationName(orgName);
	}

	public String getOrganizationName() {
		return organizationName;
	}

	@Override
	public Set<DependantModelItem> listDepedencies() {
		return Collections.emptySet();
	}

	private void setOrganizationName(String orgName) {
		Assert.notNull(name, "Fail to set space name. space name should not be empty.");
		this.organizationName = orgName;
	}

}
