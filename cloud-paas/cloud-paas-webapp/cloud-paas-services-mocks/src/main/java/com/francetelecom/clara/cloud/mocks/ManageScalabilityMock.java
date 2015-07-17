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
package com.francetelecom.clara.cloud.mocks;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.scalability.ManageScalability;
import com.francetelecom.clara.cloud.scalability.helper.ScalabilityHelper;

/**
 * ManageScalabilityMock
 * mocked version of scalability service
 * Sample usage :
 * Last update  : $LastChangedDate$
 *
 * @author : $Author$
 * @version : $Revision$
 */
@Service("manageScalability")
public class ManageScalabilityMock implements ManageScalability {
	/**
	 * paas managers
	 */
    @Autowired(required = true)
    private ManagePaasUserMock managePaasUserMock;

    @Autowired(required = true)
    private ManageApplicationMock manageApplicationMock;

    @Autowired(required = true)
    private ManageApplicationReleaseMock manageApplicationReleaseMock;

    @Autowired(required = true)
    private ManageLogicalDeploymentMock manageLogicalDeploymentMock;

    @Autowired(required = true)
    private ManageEnvironmentMock manageEnvironmentMock;

    @Autowired(required = true)
    private SampleAppProperties sampleAppProperties;

    private ScalabilityHelper helper;

    //~setters (spring requirement)
    public void setManagePaasUserMock(ManagePaasUserMock managePaasUserMock) {
        this.managePaasUserMock = managePaasUserMock;
    }

    public void setManageApplicationMock(ManageApplicationMock manageApplicationMock) {
        this.manageApplicationMock = manageApplicationMock;
    }

    public void setManageApplicationReleaseMock(ManageApplicationReleaseMock manageApplicationReleaseMock) {
        this.manageApplicationReleaseMock = manageApplicationReleaseMock;
    }

    public void setManageLogicalDeploymentMock(ManageLogicalDeploymentMock manageLogicalDeploymentMock) {
        this.manageLogicalDeploymentMock = manageLogicalDeploymentMock;
    }

    public void setManageEnvironmentMock(ManageEnvironmentMock manageEnvironmentMock) {
        this.manageEnvironmentMock = manageEnvironmentMock;
    }

    public void setSampleAppProperties(SampleAppProperties sampleAppProperties) {
        this.sampleAppProperties = sampleAppProperties;
    }

    private boolean isFakeWorld() {
        return true;
    }

    private ScalabilityHelper getHelper() {
        if (this.helper == null) {
            boolean isFakeWorld = isFakeWorld();
            this.helper = new ScalabilityHelper(managePaasUserMock,
                                                manageApplicationMock,
                                                manageApplicationReleaseMock,
                                                manageLogicalDeploymentMock,
                                                manageEnvironmentMock,
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