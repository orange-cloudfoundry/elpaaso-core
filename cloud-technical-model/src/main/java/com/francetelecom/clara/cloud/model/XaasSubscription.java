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
package com.francetelecom.clara.cloud.model;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Base class for representing a xAAS subscription (per environnent
 * instantiation of xAAS Subscription template)
 * 
 * Depending on the deployment state, a XaasSubscription instance may represent
 * a template for making a XaaS request (when deploymentState=transient and the
 * instance), or represent the result of a XaaS request (when
 * deploymentState=created)
 * 
 * @author apog7416
 */
@XmlRootElement
@Entity
@Table(name = "XAAS_SUBSCRIPTION_INSTANCE")
public abstract class XaasSubscription extends DependantModelItem {

	private static final long serialVersionUID = 4826141896100516201L;

	/**
	 * A human readeable description which is passed to XaaS that support it.
	 * This is for instance useful within the DBaaS to be able to correlate to
	 * the environment, applicationRelease and possibly Paas instance.
	 */
	private String description = "";

	/** Parent TechnicalDeployment */
	@XmlIDREF
	@XmlElement(name = "technicalDeployment")
	@ManyToOne
	@NotNull
	protected TechnicalDeployment technicalDeployment;

	/**
	 * JPA required constructor
	 */
	protected XaasSubscription() {
	}

	public XaasSubscription(TechnicalDeployment td) {
		super(td.getName() + "-subscription-" + td.listXaasSubscriptionTemplates().size());
		this.technicalDeployment = td;
		td.xaasSubscriptions.add(this);
	}

	/**
	 * read only getter
	 * 
	 * @return
	 */
	public TechnicalDeployment getTechnicalDeployment() {
		return technicalDeployment;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
