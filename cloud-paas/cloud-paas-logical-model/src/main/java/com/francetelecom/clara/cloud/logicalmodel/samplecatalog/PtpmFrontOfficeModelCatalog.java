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

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalQueueReceiveService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalQueueSendService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * A sample point to point messaging application. This part is the front-office part sending
 * ping messages, and receiving pong messages. In addition, it exposes a SpringRPC control WS
 * to trigger sending of messages and verify expected messages were received.
 */
public class PtpmFrontOfficeModelCatalog extends BaseReferenceLogicalModelsCatalog {

    public static final String PTPM_BACK_OFFICE_APP_NAME = "PtpmBackOffice";
    public static final String PTPM_BACK_OFFICE_APP_VERSION = "G1R2C0";
    public static final String PTPM_BACK_OFFICE_APP_BASICAT = "bck";
    public static final String PTPM_BACK_OFFICE_SERVICE_NAME = "ping";
    public static final String PTPM_BACK_OFFICE_SERVICE_VERSION = "G1R1C0";

    public static final String PTPM_FRONT_OFFICE_APP_NAME = "PtpmFrontOffice";
    public static final String PTPM_FRONT_OFFICE_APP_VERSION = "G1R1C0";
    public static final String PTPM_FRONT_OFFICE_APP_BASICAT = "frt";
    public static final String PTPM_FRONT_OFFICE_SERVICE_NAME = "pong";
    public static final String PTPM_FRONT_OFFICE_SERVICE_VERSION = "G1R1C0";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		// EAR
		ProcessingNode jeeProcessing = createJeeProcessing(this, "PtpmFrontOfficeJEE", "ptpm.frontoffice");
        existingLDToUpdate.addExecutionNode(jeeProcessing);

        // Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("PtpmFrontOfficeWebUi", "ptpm.frontoffice", true, false, 1, 20);
		existingLDToUpdate.addLogicalService(web);
        jeeProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        LogicalQueueSendService qss = createLogicalQueueSend("FrontOfficeQueueSendPing",
                                                             PTPM_BACK_OFFICE_SERVICE_NAME,
                                                             PTPM_BACK_OFFICE_SERVICE_VERSION,
                                                             PTPM_BACK_OFFICE_APP_BASICAT,
                                                             PTPM_BACK_OFFICE_APP_NAME,
                                                             PTPM_BACK_OFFICE_APP_VERSION,
                                                             "ping.out", 5, 50, 1);
        existingLDToUpdate.addLogicalService(qss);
		jeeProcessing.addLogicalServiceUsage(qss,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        LogicalQueueReceiveService qrs = createLogicalQueueReceive("FrontOfficeQueueReceivePong",
                                                                    PTPM_FRONT_OFFICE_SERVICE_NAME,
                                                                    PTPM_FRONT_OFFICE_SERVICE_VERSION,
                                                                    "pong.in", 10, 10, 1);
        existingLDToUpdate.addLogicalService(qrs);
		jeeProcessing.addLogicalServiceUsage(qrs,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		return existingLDToUpdate;

    }

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		return urls;
    }

    @Override
    public String getAppDescription() {
        return "PtpmFrontOfficeSample description";
    }

    @Override
    public String getAppCode() {
        return "PtpmFrontOfficeSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "PtpmFrontOfficeSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "PtpmFrontOfficeSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
