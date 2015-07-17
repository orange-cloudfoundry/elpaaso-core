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
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * Describes the El Paaso portal application in mocked mode so it can
 * be deployed on Paas.
 * @see <a href="http://elpaaso_shp/index.php/PaasDogFooding">PaasDogFooding wiki</a>
 */
public class ElPaaSoMockLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    private static String APP_CODE = "elpaaso-mocks";

    @Override
    public String getAppDescription() {
        return "Models the El PaaSo internal implementation in mock mode. This is used by the el paaso engineering team to automate the paas deployment. We call it: eat-your-own-dog-food, some prefer drink-your-own-champagne :-)";
    }

    @Override
    public String getAppCode() {
        return "MyElPaasoMockSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "MyElPaasoMockSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyElPaasoMockSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}

		// EAR
		ProcessingNode jeeProcessing = createJeeProcessing(this, "ElPaaSo_Mock", APP_CODE);
		existingLDToUpdate.addExecutionNode(jeeProcessing);

		// Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("paas-portal", APP_CODE, true, false, 1, 20);
        existingLDToUpdate.addLogicalService(web);
		jeeProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;

    }

    @Override
    public boolean isInstantiable() {
        return false;
    }

    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		return urls;
    }
}
