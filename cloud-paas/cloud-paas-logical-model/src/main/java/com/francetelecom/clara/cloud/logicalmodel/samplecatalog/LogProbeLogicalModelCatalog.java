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
 * LogProbeLogicalModelCatalog
 *
 * Sample app which supports black box tests for the Log service in the catalog.
 * The Log Probe application generates logs (handled by logging instance like splunk).
 *
 * Last update  : $LastChangedDate: 2012-11-27 11:52:11 +0100 (mar., 27 nov. 2012) $
 * Last author  : $Author: dwvd1206 $
 *
 * @version : $Revision: 28332 $
 */
public class LogProbeLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    @Override
    public String getAppDescription() {
        return "Sample app which supports black box tests for the Log service in the catalog.\n"
        +"This application generates logs (handled by logging instance like splunk).\n\n"
        +"UI : use jsp/echoTest.jsp and jsp/status.jsp relative Urls.";
    }

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		// EAR
        String applicationCode = "logProbe";
        ProcessingNode jeeProcessing = createJeeProcessing(this, "logProbeJEE", applicationCode);
        existingLDToUpdate.addExecutionNode(jeeProcessing);

		// Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("logProbeWebUi", applicationCode, true, false, 1, 20);
		existingLDToUpdate.addLogicalService(web);
        jeeProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		return existingLDToUpdate;

    }

	/**
	 * @param baseUrl Correspond to the context root of the EAR to test. Can be use to
	 *          filter multiples EAR tests. Ignore it if you just have one EAR.
	 * @return list of urls and corresponding keywords used to check application
	 *         deployment
	 */
	@Override
	public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		urls.put("/echoTest.do", "Echo");
		return urls;
	}

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public String getAppCode() {
        return "MyLogProbeSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "MyLogProbeSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyLogProbeSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
