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
 * Sample logical model for activiti web explorere.<br>
 */
public class ActivitiExplorerLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	String APP_CODE = "activiti.explorer";

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		// WAR "dbaas-portal"
		ProcessingNode cfProcessing = createCFJavaProcessing(this, "activiti-explorer", APP_CODE,ArtefactType.war);
		existingLDToUpdate.addExecutionNode(cfProcessing);

		// Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("activiti-explorer-webui", APP_CODE, true, false, 1, 20, ArtefactType.war);
		
		existingLDToUpdate.addLogicalService(web);
		cfProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

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
		return "Activiti Explorer.";
    }

    @Override
    public String getAppCode() {
		return "activiti-explorer";
    }

    @Override
    public String getAppLabel() {
		return "activiti-explorer";
    }

    @Override
    public String getAppReleaseDescription() {
		return "activiti explorer";
    }

    @Override
    public String getAppReleaseVersion() {
        return "5.17.0";
    }
}
