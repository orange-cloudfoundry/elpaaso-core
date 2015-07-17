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
package com.francetelecom.clara.cloud.paas.it.services.deployer;

import com.francetelecom.clara.cloud.paas.it.services.helper.PaasServicesEnvITHelper;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Class that can be used to deploy a fresh new ElPaaso platform from actual ElPaaso code
 * 
 * @link http://elpaaso_shp/index.php/El_PaaSo_Installation_Guide#Black_box_test_for_an_automatic_install
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class ElPaasoDeployer {
    protected static Logger logger = LoggerFactory.getLogger(ElPaasoDeployer.class);

    @Autowired(required = true)
    protected PaasServicesEnvITHelper paasServicesEnvITHelper;
    protected static PaasServicesEnvITHelper sPaasServicesEnvITHelper;

    @Before
    public void setUp() {
        PaasServicesEnvITHelper.checkThatAutowiredFieldIsNotNull(paasServicesEnvITHelper);
        sPaasServicesEnvITHelper = paasServicesEnvITHelper;
        paasServicesEnvITHelper.setEnvType(EnvironmentDto.EnvironmentTypeEnum.DEVELOPMENT);
        paasServicesEnvITHelper.setUp();
        //by default skip environment suppression
        //deployer is used alone (no in a suite) to deploy something, not test something
        paasServicesEnvITHelper.setSkipDeleteEnvironmentAtTheEnd(true);
    }

    @AfterClass
    static public void tearDown() {
        sPaasServicesEnvITHelper.tearDown();
    }


    @Test
    public void trigger_elpaaso_deployer() {
        // test method just here to trigger activation
    }
}