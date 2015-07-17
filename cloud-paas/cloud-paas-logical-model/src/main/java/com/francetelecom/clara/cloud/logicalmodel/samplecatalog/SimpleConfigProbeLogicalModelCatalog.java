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
package com.francetelecom.clara.cloud.logicalmodel.samplecatalog;

import com.francetelecom.clara.cloud.logicalmodel.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A sample application to illustrate the use of the config service with the merging of multiple ConfigSets on a single JEEProcessing, and warning for overlapping ConfigSet keys on a given JEE processing
 */
public class SimpleConfigProbeLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    public static final String JNDI_PREFIX_FOR_CONFIG = "";
    private static String APP_CODE = "simple-config";

    /**
     * Used in unit tests
     */
    public static final String FRONT_END_CONFIG = "FrontEndConfig";
    public static final String BACK_END_CONFIG = "BackEndConfig";
    public static final String WHOLE_APP_CONFIG = "WholeAppConfig";


    String frontEndConfigSetContent = "# Defines the email contact that appears in the UI for asking help. Should be overriden per environment\n" +
            "support.email.contact=delis-dev-support@orange.com\n" +
            "# Ability to turn off the UI to rejects HTTP requests without the SSO gassi header. Use only on dev environments as a convenient for local testing\n" +
            "reject_unauthenticated_gassi_logins=true";
    String backendConfigSetContent = "#Configureable period in seconds after which the audit DB tables are purged. Typically increase in QA environments to perform white box testing on the content of these tables.\n" +
            "purge_database_audit_tables_period=20";
    String wholeAppConfigSetContent = "# for the launch of the Orange Open commercial offer, turn this option on, for the app enable corresponding features\n" +
            "enable.palier.for.open.launch=false\n" +
            "# By default, DB schema upgrades are handled through our liqui-base setup see http://www.liquibase.org/ and not automatically\n" +
            "hibernate_auto_db_schema_upgrades=false\n";
    String fancyConfigSetContent =
            "#   _______________          |*\\_/*|________\n" +
                    "#  |  ___________  |        ||_/-\\_|______  |\n" +
                    "#  | |           | |        | |           | |\n" +
                    "#  | |   0   0   | |        | |   0   0   | |\n" +
                    "#  | |     -     | |        | |     -     | |\n" +
                    "#  | |   \\___/   | |        | |   \\___/   | |\n" +
                    "#  | |___     ___| |        | |___________| |\n" +
                    "#  |_____|\\_/|_____|        |_______________|\n" +
                    "#    _|__|/ \\|_|_.............._|________|_\n" +
                    "#   / ********** \\            / ********** \\\n" +
                    "# /  ************  \\        /  ************  \\\n" +
                    "#--------------------      --------------------\n" +
                    "fancy.computer = true\n" +
                    "\n" +
                    "#                        _\n" +
                    "#                       | \\\n" +
                    "#                       | |\n" +
                    "#                       | |\n" +
                    "#  |\\                   | |\n" +
                    "# /, ~\\                / /\n" +
                    "#X     `-.....-------./ /\n" +
                    "# ~-. ~  ~              |\n" +
                    "#    \\             /    |\n" +
                    "#     \\  /_     ___\\   /\n" +
                    "#     | /\\ ~~~~~   \\ |\n" +
                    "#     | | \\        || |\n" +
                    "#     | |\\ \\       || )\n" +
                    "#    (_/ (_/      ((_/\n" +
                    "fancy.cat = true\n" +
                    "\n" +
                    "#             ,       ,\n" +
                    "#            /(       )`\n" +
                    "#            \\ \\__   / |\n" +
                    "#            /- _ `-/  '\n" +
                    "#           (/\\/ \\ \\   /\\\n" +
                    "#           / /   | `    \\\n" +
                    "#           O O   )      |\n" +
                    "#           `-^--'`<     '\n" +
                    "#          (_.)  _ )    /\n" +
                    "#           `.___/`    /\n" +
                    "#             `-----' /\n" +
                    "#<----.     __ / __   \\\n" +
                    "#<----|====O)))==) \\) /=============\n" +
                    "#<----'    `--' `.__,' \\\n" +
                    "#             |         |\n" +
                    "#              \\       /\n" +
                    "#          ____( (_   / \\______\n" +
                    "#        ,'  ,----'   |        \\\n" +
                    "#        `--{__________)       \\/\n" +
                    "multi_Props.name=enabled\n" +
                    "enable.daemon = true";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

        if (existingLDToUpdate == null) {
            existingLDToUpdate = new LogicalDeployment();
        }

        /** FRONTEND **/
        ProcessingNode frontEndJeeProcessing = addFrontEndProcessingNode(existingLDToUpdate);

        /** BACKEND **/
        ProcessingNode backendJeeProcessing = addBackEndProcessingNode(existingLDToUpdate);

        /** COMMON TO BACKEND AND FRONTEND **/
        LogicalConfigService wholeAppConfig = createLogicalConfigService(WHOLE_APP_CONFIG, JNDI_PREFIX_FOR_CONFIG, wholeAppConfigSetContent);

        existingLDToUpdate.addLogicalService(wholeAppConfig);

        frontEndJeeProcessing.addLogicalServiceUsage(wholeAppConfig, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        backendJeeProcessing.addLogicalServiceUsage(wholeAppConfig, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        /** WITH FANCY COMMENTS **/
        LogicalConfigService fancyConfig = createLogicalConfigService("FancyConfig", JNDI_PREFIX_FOR_CONFIG, fancyConfigSetContent);

        existingLDToUpdate.addLogicalService(fancyConfig);

        backendJeeProcessing.addLogicalServiceUsage(fancyConfig, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;
    }

    private ProcessingNode addBackEndProcessingNode(LogicalDeployment logicalDeployment) {
        LogicalWebGUIService backOfficeUI = createLogicalWebGuiService("BackOfficeUI", APP_CODE, false, false, 10, 20, ArtefactType.jar);
        ProcessingNode backendJeeProcessing = createCFJavaProcessing(this, "BackEnd", APP_CODE, ArtefactType.jar);

        LogicalConfigService backEndConfig = createLogicalConfigService(BACK_END_CONFIG, JNDI_PREFIX_FOR_CONFIG, backendConfigSetContent);


        logicalDeployment.addLogicalService(backOfficeUI);
        logicalDeployment.addLogicalService(backEndConfig);
        logicalDeployment.addExecutionNode(backendJeeProcessing);

        backendJeeProcessing.addLogicalServiceUsage(backOfficeUI, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        backendJeeProcessing.addLogicalServiceUsage(backEndConfig, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        return backendJeeProcessing;
    }

    private ProcessingNode addFrontEndProcessingNode(LogicalDeployment logicalDeployment) {
        LogicalWebGUIService frontOfficeUI = createLogicalWebGuiService("FrontEndGui", APP_CODE, true, false, 1, 20, ArtefactType.jar);
        ProcessingNode frontEndJeeProcessing = createCFJavaProcessing(this, "FrontEnd", APP_CODE, ArtefactType.jar);

        LogicalConfigService frontEndConfig = createLogicalConfigService(FRONT_END_CONFIG, JNDI_PREFIX_FOR_CONFIG, frontEndConfigSetContent);

        logicalDeployment.addLogicalService(frontOfficeUI);
        logicalDeployment.addLogicalService(frontEndConfig);
        logicalDeployment.addExecutionNode(frontEndJeeProcessing);

        frontEndJeeProcessing.addLogicalServiceUsage(frontOfficeUI, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        frontEndJeeProcessing.addLogicalServiceUsage(frontEndConfig, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        return frontEndJeeProcessing;
    }

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
        HashMap<String, String> urls = new HashMap<String, String>();
        urls.put("/", "Paas Probe");
        urls.put("/env/", "vcap");
        urls.put("/metrics/", "mem");
        urls.put("/env/java.runtime.name", "Runtime");
        return urls;
    }

    @Override
    public String getAppCode() {
        return "MyConfigSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "MyConfigSample";
    }

    @Override
    public String getAppDescription() {
        return "MyConfigSample description";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyConfigSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
