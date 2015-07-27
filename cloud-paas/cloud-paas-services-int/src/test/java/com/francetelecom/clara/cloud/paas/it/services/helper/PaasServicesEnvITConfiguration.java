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
package com.francetelecom.clara.cloud.paas.it.services.helper;

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;

/**
 * PaasServicesEnvITConfiguration
 */
public class PaasServicesEnvITConfiguration {

    private ManagePaasUser managePaasUser;

    private ManageApplication manageApplication;

    private ManageApplicationRelease manageApplicationRelease;

    private ManageLogicalDeployment manageLogicalDeployment;

    private ManageEnvironment manageEnvironment;
    
    /**
     * ssh tunnel should be used when testing
     */
    protected boolean useSshTunnel = false;
    /**
     * name your test and so the associated environment label
     */
    private String name;
    /**
     * logical model to test
     */
    private SampleAppFactory logicalModelCatalog;
    /**
     * test user email :
     * - used by Garbage collector to determine test resource
     */
    private String testUserEmail;

    /**
     * Whether to use HTTP proxy giving access to the internet for checking webGui access urls
     */
    private boolean useHttpIgeProxy = false;
    private String httpProxyHost;
    private int httpProxyPort;

    public String getTestUserEmail() {
        return testUserEmail;
    }

    public void setTestUserEmail(String testUserEmail) {
        this.testUserEmail = testUserEmail;
    }

    public ManageApplication getManageApplication() {
        return manageApplication;
    }

    public void setManageApplication(ManageApplication manageApplication) {
        this.manageApplication = manageApplication;
    }

    public ManageApplicationRelease getManageApplicationRelease() {
        return manageApplicationRelease;
    }

    public void setManageApplicationRelease(ManageApplicationRelease manageApplicationRelease) {
        this.manageApplicationRelease = manageApplicationRelease;
    }

    public ManageEnvironment getManageEnvironment() {
        return manageEnvironment;
    }

    public void setManageEnvironment(ManageEnvironment manageEnvironment) {
        this.manageEnvironment = manageEnvironment;
    }

    public ManageLogicalDeployment getManageLogicalDeployment() {
        return manageLogicalDeployment;
    }

    public void setManageLogicalDeployment(ManageLogicalDeployment manageLogicalDeployment) {
        this.manageLogicalDeployment = manageLogicalDeployment;
    }

    public ManagePaasUser getManagePaasUser() {
        return managePaasUser;
    }

    public void setManagePaasUser(ManagePaasUser managePaasUser) {
        this.managePaasUser = managePaasUser;
    }

    public SampleAppFactory getLogicalModelCatalog() {
        return logicalModelCatalog;
    }

    public void setLogicalModelCatalog(SampleAppFactory logicalModelCatalog) {
        this.logicalModelCatalog = logicalModelCatalog;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isUseSshTunnel() {
        return useSshTunnel;
    }

    public void setUseSshTunnel(boolean useSshTunnel) {
        this.useSshTunnel = useSshTunnel;
    }

    public boolean isUseHttpIgeProxy() {
        return useHttpIgeProxy;
    }

    public void setUseHttpIgeProxy(boolean useHttpIgeProxy) {
        this.useHttpIgeProxy = useHttpIgeProxy;
    }

    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    public void setHttpProxyHost(String httpProxyHost) {
        this.httpProxyHost = httpProxyHost;
    }

    public int getHttpProxyPort() {
        return httpProxyPort;
    }

    public void setHttpProxyPort(int httpProxyPort) {
        this.httpProxyPort = httpProxyPort;
    }

}
