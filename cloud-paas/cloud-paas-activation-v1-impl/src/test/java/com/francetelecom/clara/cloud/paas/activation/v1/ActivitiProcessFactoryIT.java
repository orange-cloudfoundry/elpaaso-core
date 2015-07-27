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
package com.francetelecom.clara.cloud.paas.activation.v1;

import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.model.*;
import com.francetelecom.clara.cloud.model.validators.ModelItemGenericValidationUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBException;

/**
 * ActivitiProcessFactoryIT
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ActivitiProcessFactoryIT {
    /**
     * logger
     */
    private static Logger logger = LoggerFactory
            .getLogger(ActivitiProcessFactoryIT.class);

    @Autowired
    private TechnicalDeploymentTestFactory technicalDeploymentTestFactory;

    @Autowired
    private ActivitiProcessFactory activitiProcessFactory;


    private TechnicalDeploymentInstance getTdiSample() {
        TechnicalDeployment td
                = technicalDeploymentTestFactory.createWicketJpaTD("testCreateActivateProcess", "foo.groupid:foo.artifactid:foo.version");
        // Create TDT from TD
        TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);

        logger.info("model validation (before TDT creation)");
        ModelItemGenericValidationUtils.validateModel(tdt, logger);

        return new TechnicalDeploymentInstance(tdt,td);
    }

    @Test
    public void testCreateActivateProcess_vcdTasksInside() throws JAXBException {
        // GIVEN
        TechnicalDeploymentInstance tdi = getTdiSample();

        // WHEN
        org.activiti.bpmn.model.Process  resultTProcess = activitiProcessFactory.createActivateProcess(tdi);

        //TODO reactivate to assert process generation
        // THEN

    }
}
