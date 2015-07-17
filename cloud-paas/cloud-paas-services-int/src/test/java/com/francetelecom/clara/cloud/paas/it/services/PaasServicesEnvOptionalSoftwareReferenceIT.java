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
package com.francetelecom.clara.cloud.paas.it.services;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalNodeServiceAssociation;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.paas.it.services.helper.PaasServicesEnvITHelper;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

@ContextConfiguration(classes= PaasServicesEnvOptionalSoftwareReferenceITContext.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PaasServicesEnvOptionalSoftwareReferenceIT {

    protected static Logger logger = LoggerFactory.getLogger(PaasServicesEnvOptionalSoftwareReferenceIT.class);

    @Autowired(required = true)
    protected PaasServicesEnvITHelper paasServicesEnvITHelper;
    protected static PaasServicesEnvITHelper sPaasServicesEnvITHelper;

    @Autowired(required = true)
    protected ManageEnvironment manageEnvironment;
    @Autowired(required = true)
    protected ManageLogicalDeployment manageLogicalDeployment;

    protected String environmentUID;
    protected int logicalDeploymentID;


    @Before
    public void setup() {
        paasServicesEnvITHelper.setDefaultConfigurationItName();
        PaasServicesEnvITHelper.checkThatAutowiredFieldIsNotNull(paasServicesEnvITHelper);
        sPaasServicesEnvITHelper = paasServicesEnvITHelper;
        paasServicesEnvITHelper.setMaxSessions(10);
        paasServicesEnvITHelper.setMaxRequests(10);
        paasServicesEnvITHelper.setEnvType(EnvironmentDto.EnvironmentTypeEnum.DEVELOPMENT);
        environmentUID = paasServicesEnvITHelper.setUp();
        logicalDeploymentID = paasServicesEnvITHelper.getLogicalDeploymentID();
    }

    @AfterClass
    static public void tearDown() {
        sPaasServicesEnvITHelper.tearDown();
    }


    @Test
    public void application_should_be_accessible() {
        paasServicesEnvITHelper.application_should_be_accessible(true);
    }



}
