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
/**
 * 
 */
package com.francetelecom.clara.cloud.logicalmodel;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;

/**
 * LogicalSoapService Exposes a SOAP web service for other consumer application
 * to invoke.
 * 
 * Last updated : $LastChangedDate$ Last author : $Author$
 * 
 * @version : $Revision$
 */
@XmlRootElement
@Entity
@Table(name = "LogicalSoapService")
@GuiClassMapping(serviceCatalogName = "WSP", serviceCatalogNameKey = "wsp", status = GuiClassMapping.StatusType.BETA, isExternal = true)
public class LogicalSoapService extends LogicalService {

	/**
	 * The name of the provided service (e.g. "Echo")
	 */
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@NotNull
	String serviceName;

	/**
	 * The major version of the provided service (e.g. "2")
	 */
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@Min(value = 1)
	@NotNull
	private int serviceMajorVersion = 1;

	/**
	 * The minor version of the provided service (e.g. "1")
	 */
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@Min(value = 0)
	@NotNull
	private int serviceMinorVersion = 0;

	/**
	 * The provided service attached files a jar packaged including : - wsdl
	 * file(s) - xsd file(s)
	 */
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	// @NotNull
	@Valid
	private MavenReference serviceAttachments;

	/**
	 * The provided service attachment type
	 */
	@Enumerated(EnumType.STRING)
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@NotNull
	private LogicalAttachmentTypeEnum serviceAttachmentType = LogicalAttachmentTypeEnum.NONE;

	/**
	 * The provided service relative path url (cxf servlet context root)
	 * <P>
	 * A context root must start with a forward slash (/)
	 */
	@Embedded
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@Valid
	private ContextRoot contextRoot;

	@Embedded
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@Valid
	private Path servicePath;

	@GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	@NotNull
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "accessZone", column = @Column(name = "INBOUND_AP_ACCESS_ZONE")),
			@AttributeOverride(name = "authenticationType", column = @Column(name = "INBOUND_AP_AUTH_TYPE")),
			@AttributeOverride(name = "protocol", column = @Column(name = "INBOUND_AP_PROTOCOL")) })
	@Valid
	private LogicalInboundAuthenticationPolicy inboundAuthenticationPolicy = new LogicalInboundAuthenticationPolicy();

	@GuiMapping(status = GuiMapping.StatusType.SKIPPED)
	@NotNull
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "authenticationType", column = @Column(name = "OUTBOUND_AP_AUTH_TYPE")),
			@AttributeOverride(name = "protocol", column = @Column(name = "OUTBOUND_AP_PROTOCOL")) })
	@Valid
	private LogicalOutboundAuthenticationPolicy outboundAuthenticationPolicy = new LogicalOutboundAuthenticationPolicy();

	@Enumerated(EnumType.STRING)
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@NotNull
	private LogicalIdentityPropagationEnum identityPropagation = LogicalIdentityPropagationEnum.NONE;

	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@NotNull
	private String description = "";

	/**
	 * Prefered prefix JNDI keys to be looked up by the application. This allows
	 * the application to distinguish among multiple WSP subscriptions (e.g.
	 * "myService"). The application is then responsible for looking up the
	 * following JNDI keys constructed for the prefix, e.g. "myService/url",
	 * "myService/user", and "myService/password"
	 * 
	 * TODO: see if there is additional restrictions on the String to be used as
	 * JNDI keys. None specified in JNDI interfaces
	 * 
	 * @see javax.naming.Name
	 * @see javax.naming.Context#lookup(String)
	 */
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	private String jndiPrefix = "";

	/**
	 * The name of the main root file ine the maven reference archive if there
	 * is confusion (e.g. "Echo.wsdl")
	 */
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	private String rootFileName;

	/**
	 * 
	 */
	public LogicalSoapService() {
		super();
	}

	/**
	 * 
	 * @param label
	 * @param logicalDeployment
	 * @param svcName
	 * @param majorVersion
	 * @param minorVersion
	 * @param contextRoot
	 * @param servicePath
	 * @param serviceAttachments
	 * @param description
	 */
	public LogicalSoapService(String label, LogicalDeployment logicalDeployment, String svcName, int majorVersion, int minorVersion,
			String contextRoot, Path servicePath, MavenReference serviceAttachments, String description) {
		super(label, logicalDeployment);
		this.serviceName = svcName;
		this.serviceMajorVersion = majorVersion;
		this.serviceMinorVersion = minorVersion;
		this.contextRoot = new ContextRoot(contextRoot);
		setServicePath(servicePath);
		this.serviceAttachments = serviceAttachments;
		this.description = description;
	}

	public void setServicePath(Path servicePath) {
		if (servicePath == null)
			throw new TechnicalException("cannot set service path value. <"+servicePath+"> is not valid.");
		this.servicePath = servicePath;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public int getServiceMajorVersion() {
		return serviceMajorVersion;
	}

	public void setServiceMajorVersion(int serviceMajorVersion) {
		this.serviceMajorVersion = serviceMajorVersion;
	}

	public int getServiceMinorVersion() {
		return serviceMinorVersion;
	}

	public void setServiceMinorVersion(int serviceMinorVersion) {
		this.serviceMinorVersion = serviceMinorVersion;
	}

	public LogicalAttachmentTypeEnum getServiceAttachmentType() {
		return serviceAttachmentType;
	}

	public void setServiceAttachmentType(LogicalAttachmentTypeEnum serviceAttachmentType) {
		this.serviceAttachmentType = serviceAttachmentType;
	}

	public ContextRoot getContextRoot() {
		return contextRoot;
	}

	public void setContextRoot(ContextRoot contextRoot) {
		this.contextRoot = contextRoot;
	}

	public LogicalInboundAuthenticationPolicy getInboundAuthenticationPolicy() {
		return inboundAuthenticationPolicy;
	}

	public void setInboundAuthenticationPolicy(LogicalInboundAuthenticationPolicy inboundAuthenticationPolicy) {
		this.inboundAuthenticationPolicy = inboundAuthenticationPolicy;
	}

	public LogicalOutboundAuthenticationPolicy getOutboundAuthenticationPolicy() {
		return outboundAuthenticationPolicy;
	}

	public void setOutboundAuthenticationPolicy(LogicalOutboundAuthenticationPolicy outboundAuthenticationPolicy) {
		this.outboundAuthenticationPolicy = outboundAuthenticationPolicy;
	}

	public LogicalIdentityPropagationEnum getIdentityPropagation() {
		return identityPropagation;
	}

	public void setIdentityPropagation(LogicalIdentityPropagationEnum identityPropagation) {
		this.identityPropagation = identityPropagation;
	}

	public MavenReference getServiceAttachments() {
		return serviceAttachments;
	}

	public void setServiceAttachments(MavenReference serviceAttachments) {
		this.serviceAttachments = serviceAttachments;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getJndiPrefix() {
		return jndiPrefix;
	}

	public void setJndiPrefix(String jndiPrefix) {
		this.jndiPrefix = jndiPrefix;
	}

	public String getRootFileName() {
		return rootFileName;
	}

	public void setRootFileName(String rootFileName) {
		this.rootFileName = rootFileName;
	}

	public Path getServicePath() {
		return servicePath;
	}
}
