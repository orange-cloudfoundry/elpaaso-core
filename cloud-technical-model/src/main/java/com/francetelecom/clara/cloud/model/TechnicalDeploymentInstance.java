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

import com.francetelecom.clara.cloud.commons.UUIDUtils;
import org.springframework.util.Assert;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@Entity
@Table(name = "TECHNICAL_DEPLOYMENT_INSTANCE")
public class TechnicalDeploymentInstance extends ModelItem {

	private static final long serialVersionUID = 7873627229474910137L;

	/**
	 * Reference key, typically : IaaS vApp id. This is not to be confused with
	 * the vApp template Id which is present into
	 * 
	 */
	private String referenceKey;

	@XmlIDREF
	@XmlElement(name = "technicalDeploymentRef")
	@OneToOne (cascade = { CascadeType.ALL })
	// @NotNull //temporary disabled until Hibernate bug about schema is fixed:
	private TechnicalDeployment technicalDeployment;

	/**
	 * The original TDT shared by multiple TDIs. Currently used by activation to
	 * track the completion of the disk image creation. May be used in the
	 * future to delegate state when the image creation is pending when this
	 * level of detail need to be exposed in getDeploymentState();
	 */
	@XmlIDREF
	@XmlElement(name = "technicalDeploymentTemplateRef")
	@OneToOne
	// @NotNull //temporary disabled until Hibernate bug about schema is fixed:
	private TechnicalDeploymentTemplate technicalDeploymentTemplate;

	/**
	 * constructor for JPA / Jaxb
	 */
	protected TechnicalDeploymentInstance() {
	}

	/**
	 * Construct a TechnicalDeploymentInstance
	 * 
	 * @param tdt
	 *            The TechnicalDeploymentTemplate that contains the original
	 *            TechnicalDeployment
	 * @param td
	 *            A copy of the TechnicalDeployment of the tdt describing the
	 *            instance
	 */
	public TechnicalDeploymentInstance(TechnicalDeploymentTemplate tdt, TechnicalDeployment td) {
		super(UUIDUtils.generateUUID("t"));
		// a technical deployment instance must refer to a technical deployment.
		Assert.notNull(td, "Cannot technical deployment instance. No technical deployment has been supplied.");
		this.technicalDeployment = td;
		// a technical deployment instance must refer to a technical deployment template.
		Assert.notNull(tdt, "Cannot technical deployment instance. No technical deployment template has been supplied.");
		this.technicalDeploymentTemplate = tdt;
	}

	/**
	 * Return the reference key (vApp reference/id into the IAAS)
	 * 
	 * @return The reference key
	 */
	public String getReferenceKey() {
		return referenceKey;
	}

	/**
	 * Set the reference key (vApp reference/id into the IAAS).
	 */
	public void setReferenceKey(String referenceKey) {
		this.referenceKey = referenceKey;
	}

	public TechnicalDeployment getTechnicalDeployment() {
		return technicalDeployment;
	}

}
