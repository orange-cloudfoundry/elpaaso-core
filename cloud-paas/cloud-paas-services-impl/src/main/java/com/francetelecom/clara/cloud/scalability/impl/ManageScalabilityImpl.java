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
package com.francetelecom.clara.cloud.scalability.impl;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.paas.activation.v1.ActivationPluginStrategyImpl;
import com.francetelecom.clara.cloud.scalability.ManageScalability;
import com.francetelecom.clara.cloud.scalability.helper.ScalabilityHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

/**
 * ManageScalabilityImpl
 * Sample usage : see ManageScalabilityImplTest
 */
public class ManageScalabilityImpl implements ManageScalability {
    /**
     * This is just used to know if environment will be really created
     * e.g. if VM vill be instanciated
     */
    @Autowired(required=false)
    ActivationPluginStrategyImpl pluginStrategy;

	/**
	 * paas managers
	 */
    @Autowired(required = true)
	private ManagePaasUser managePaasUser;
    @Autowired(required = true)
    private ManageApplication manageApplication;
    @Autowired(required = true)
    private ManageApplicationRelease manageApplicationRelease;
    @Autowired(required = true)
    private ManageLogicalDeployment manageLogicalDeployment;
    @Autowired(required = true)
    private ManageEnvironment manageEnvironment;
    @Autowired(required = true)
    private SampleAppProperties sampleAppProperties;


    private ScalabilityHelper helper;

    //~setters (spring requirement)
    public void setManagePaasUser(ManagePaasUser managePaasUser) {
        this.managePaasUser = managePaasUser;
    }

    public void setManageApplication(ManageApplication manageApplication) {
        this.manageApplication = manageApplication;
    }

    public void setManageApplicationRelease(ManageApplicationRelease manageApplicationRelease) {
        this.manageApplicationRelease = manageApplicationRelease;
    }

    public void setManageLogicalDeployment(ManageLogicalDeployment manageLogicalDeployment) {
        this.manageLogicalDeployment = manageLogicalDeployment;
    }

    public void setManageEnvironment(ManageEnvironment manageEnvironment) {
        this.manageEnvironment = manageEnvironment;
    }

    public void setSampleAppProperties(SampleAppProperties sampleAppProperties) {
        this.sampleAppProperties = sampleAppProperties;
    }

    private boolean isFakeWorld() {
        return (pluginStrategy == null
		     || pluginStrategy.getPlugins().size() == 0
			 || (pluginStrategy.getPlugins().size() == 1
                 && pluginStrategy.getPlugins().get(0).getClass().getName().contains("ActivationPluginMock")));

    }

    private ScalabilityHelper getHelper() {
        if (this.helper == null) {
            boolean isFakeWorld = isFakeWorld();
            this.helper = new ScalabilityHelper(managePaasUser,
                                                manageApplication,
                                                manageApplicationRelease,
                                                manageLogicalDeployment,
                                                manageEnvironment,
                                                sampleAppProperties,
                                                isFakeWorld);
        }
        return this.helper;
    }

    @Override
    public void razData() throws BusinessException {
        getHelper().razData(false);
    }

    @Override
    public void razDataWithActivation() throws BusinessException {
        getHelper().razData(true);
    }

    @Override
    public Collection<PaasUser> createPaasUsers(String namePrefix, int nbToCreate) {
        return getHelper().createPaasUsers(namePrefix, nbToCreate);
    }

    @Override
    public Collection<PaasUser> createTeam(String namePrefix) {
        return getHelper().createTeam(namePrefix);
    }

    @Override
    public Application populateSimpleTestPhase(PaasUser author, boolean createEnv) throws BusinessException {
        return getHelper().populateSimpleTestPhase(author, createEnv);
    }

    @Override
    public Collection<ApplicationRelease> populate(String pattern, String teamName, int nbApp, int nbReleasePerApp, int nbEnvPerRelease) throws BusinessException {
        return getHelper().populate(pattern, teamName, nbApp, nbReleasePerApp, nbEnvPerRelease);
    }

    public Collection<ApplicationRelease> populatePortalPhase(int nbApp, int nbReleasePerApp, int nbEnvPerRelease) throws BusinessException {
        return populate("GDSNN", "portalTest", nbApp, nbReleasePerApp, nbEnvPerRelease);
    }
}
