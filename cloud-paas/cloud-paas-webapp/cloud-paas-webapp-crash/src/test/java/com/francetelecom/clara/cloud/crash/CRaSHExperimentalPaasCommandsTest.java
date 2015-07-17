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
package com.francetelecom.clara.cloud.crash;

import com.francetelecom.clara.cloud.commons.BusinessException;
import org.junit.Test;

import java.io.IOException;

/**
 * test of the paas *EXPERIMENTAL* crash commands
 *  based on services mocks layer
 */
public class CRaSHExperimentalPaasCommandsTest extends AbstractSpringTestCase {
    @Override
    protected String getContextXmlFilename() {
        return "crash-service-mocks-context.xml";
    }

    @Test
    public void should_provide_ops_info_official_command() throws IOException {
        // GIVEN
        String command = "ops info";
        // WHEN/THEN
        // verboseAssertCommandOkAndOutputContains(command, true, "build user : don gorgio");
        verboseAssertCommandOkFromReference(command, "ops_info_mocks.ref");
    }


    @Test
    public void should_provide_ops_envs_official_command() throws IOException, BusinessException {
        // GIVEN
        populateEnv();
        String command = "ops envs";

        // WHEN/THEN
        // verboseAssertCommandOkAndOutputContains(command, true, "build user : don gorgio");
        // verboseAssertCommandOkFromReference(command, "ops_envs_mocks.ref");
        verboseAssertCommandOkAndOutputContains(command, true, "1 environments");
    }
}
