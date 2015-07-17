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
package com.francetelecom.clara.cloud.logicalmodel;

import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provides access to a SOAP web service exposed by a another application.
 *
 * Represents the Web Service Consumer (WSC) service in the catalog.
 * 
 * @author APOG7416
 * 
 */
@SuppressWarnings("serial")
@XmlRootElement
@Entity
@Table(name = "LogicalSoapConsumer")
@GuiClassMapping(serviceCatalogName = "WSC", serviceCatalogNameKey = "wsc", status = GuiClassMapping.StatusType.BETA, isExternal = true)
public class LogicalSoapConsumer extends LogicalService {

    /**
     * Describes the wsDomain of the target webservice
     */
    public static enum SoapServiceDomainEnum {

        /**
         * This describes that the referenced WSP is registered to the corporate broker (WSOI).
         *
         * As of Oct 2011, this implies that an application/service name/version uniquely identifies
         * the remote party (i.e. no yet support for multiple instances of a given webservice)
         */
        EXTERNAL_BROKERED,

        /**
         * This describes that the referenced WSP is not registered through a broker, and hence
         * its endpoint (URL and authentication) are provided at environment instanciation time.
         */
        EXTERNAL_DIRECT_ACCESS,

        /**
         * This describes that the referenced WSP is registered within the PaaS as an application release.
         * This implies that the environment instance of the WSP is provided at environment instanciation time.
         */
        PAAS,

    }

    /**
     * Describes the wsDomain/scope into which how the WSP is referenced
     */
    @GuiMapping(status = GuiMapping.StatusType.READ_ONLY)
    @NotNull
    private SoapServiceDomainEnum wsDomain = SoapServiceDomainEnum.EXTERNAL_BROKERED;

    /**
     * Identifies the WS provider name. In orange FR, this usually maps to the name of the application (e.g. "15T")
     *
     * Note: applies to EXTERNAL_BROKERED case.
     */
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
    @NotNull
    String serviceProviderName;


    /**
     * The name of the target service within the WS provider (e.g. "ManageLocationCustomerDataManagement")
     *
     * Note: applies to EXTERNAL_BROKERED case.
     */
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
    @NotNull
    String serviceName;


    /**
     * The major version of the target service within the WS provider (e.g. "2")
     *
     * Note: applies to EXTERNAL_BROKERED case.
     */
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
    @Min(value = 1)
    int serviceMajorVersion = 1;


    /**
     * The minor version of the target service within the WS provider (e.g. "1")
     *
     * Note: applies to EXTERNAL_BROKERED case.
     */
    @GuiMapping
    @Min(value = 0)
    int serviceMinorVersion = 0;


    /**
     * Prefered prefix JNDI keys to be looked up by the application. This allows the application
     * to distinguish among multiple WSC subscriptions (e.g. "reflet").
     * The application is then responsible for looking up the following JNDI keys constructed for the prefix, e.g.
     * "reflet/url", "reflet/user", and "reflet/password"
     *
     * TODO: see if there is additional restrictions on the String to be used as JNDI keys. None specified in JNDI interfaces
     * @see javax.naming.Name
     * @see javax.naming.Context#lookup(String)
     */
    @NotNull
    @Size(min = 1)
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
    private String jndiPrefix;


    //TODO: add SLOs (max expected response time, max QPS rate)

	/**
	 * default empty constructor
	 */
	public LogicalSoapConsumer() {

	}

	/**
	 * @param serviceName
	 * @param serviceMajorVersion
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
	 */
	public LogicalSoapConsumer(String label, LogicalDeployment logicalDeployment, String serviceName, String serviceProviderName, int serviceMajorVersion) {
		super(label, logicalDeployment);
		this.serviceName = serviceName;
        this.serviceMajorVersion = serviceMajorVersion;
        this.serviceProviderName = serviceProviderName;
	}

    /**
	 * @param serviceName
	 * @param majorVersion
     * @param minorVersion
	 */
	public LogicalSoapConsumer(String serviceName, String serviceProviderName, int majorVersion, int minorVersion) {
		super();
		this.serviceName = serviceName;
        this.serviceProviderName = serviceProviderName;
	}

	/**
	 * @return the serviceName
	 */
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

    public SoapServiceDomainEnum getWsDomain() {
        return wsDomain;
    }

    public void setWsDomain(SoapServiceDomainEnum wsDomain) {
        this.wsDomain = wsDomain;
    }

    public String getServiceProviderName() {
        return serviceProviderName;
    }

    public void setServiceProviderName(String serviceProviderName) {
        this.serviceProviderName = serviceProviderName;
    }

    public int getServiceMinorVersion() {
        return serviceMinorVersion;
    }

    public void setServiceMinorVersion(int serviceMinorVersion) {
        this.serviceMinorVersion = serviceMinorVersion;
    }

    public String getJndiPrefix() {
        return jndiPrefix;
    }

    public void setJndiPrefix(String jndiPrefix) {
        this.jndiPrefix = jndiPrefix;
    }
}
