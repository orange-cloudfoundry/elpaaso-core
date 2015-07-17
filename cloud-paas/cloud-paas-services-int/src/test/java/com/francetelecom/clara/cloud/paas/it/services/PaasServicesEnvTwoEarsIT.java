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

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.paas.it.services.helper.PaasServicesEnvITHelper;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@IfProfileValue(name = "test-groups", values = { "legacy-test" })
public class PaasServicesEnvTwoEarsIT {
    protected static Logger logger = LoggerFactory.getLogger(PaasServicesEnvTwoEarsIT.class);

    @Autowired(required = true)
    private PaasServicesEnvITHelper paasServicesEnvITHelper;
    private static PaasServicesEnvITHelper sPaasServicesEnvITHelper;

    @Before
    public void setUp() {
        PaasServicesEnvITHelper.checkThatAutowiredFieldIsNotNull(paasServicesEnvITHelper);
        sPaasServicesEnvITHelper = paasServicesEnvITHelper;
        paasServicesEnvITHelper.setMaxSessions(10);
        paasServicesEnvITHelper.setMaxRequests(200);
        paasServicesEnvITHelper.setEnvType(EnvironmentDto.EnvironmentTypeEnum.DEVELOPMENT);
        paasServicesEnvITHelper.setUp();
    }
    @AfterClass
    static public void tearDown() {
        PaasServicesEnvITHelper.shutdown(sPaasServicesEnvITHelper);
    }

    @Test
    public void application_should_be_accessible() {
        paasServicesEnvITHelper.application_should_be_accessible(true);
    }

    @Test
    public void application_should_be_accessible_after_environment_restart() {
        paasServicesEnvITHelper.application_should_be_accessible_after_environment_restart();
    }
}