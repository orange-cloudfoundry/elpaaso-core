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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * test of the crash custom paas commands directory & hot redeploying of groovy script
 */
public class CRaSHHotReloadCommandsTest extends AbstractSpringTestCase {
    protected static Logger LOG = LoggerFactory.getLogger(CRaSHHotReloadCommandsTest.class);
    public static final String TEST_RESOURCES_PATH = "src/test/resources/";

    @Override
    protected String getContextXmlFilename() {
        return "crash-manage-bean-mocked-context.xml";
    }

    @Test
    public void should_hot_reload_scripts_file_with_context() throws IOException {
        // GIVEN
        String ssampleFilename = "ssample.groovy";
        File ssampleAvailableScript = new File(TEST_RESOURCES_PATH + "crash/commands/" + ssampleFilename);
        try {
            if (ssampleAvailableScript.exists()) {
                ssampleAvailableScript.delete();
            }
            assertCommandOkAndOutputContains("help", false, "ssample");
            File ssampleRefScript = new File(TEST_RESOURCES_PATH + "crash/ref/commands/" + ssampleFilename);
            Validate.isTrue(ssampleRefScript.exists(),
                    "command reference should exists : " + ssampleRefScript.getAbsolutePath());

            // WHEN
            FileUtils.copyFile(ssampleRefScript, ssampleAvailableScript);
            Validate.isTrue(ssampleAvailableScript.exists(),
                    "command should have been hot deployed here : " + ssampleAvailableScript.getAbsolutePath());
            int nbSec = 2; // cred test overrides vfs refresh period to 1sec
            sleepNSeconds(nbSec);

            // THEN
            assertCommandOkAndOutputContains("help", true, "ssample");
            verboseAssertCommandOkAndOutputContains("ssample gogo", true, " build user : ");
        } finally {
            ssampleAvailableScript.delete();
        }
    }

    private void sleepNSeconds(int nbSec) {
        try {
            Thread.sleep(nbSec*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
