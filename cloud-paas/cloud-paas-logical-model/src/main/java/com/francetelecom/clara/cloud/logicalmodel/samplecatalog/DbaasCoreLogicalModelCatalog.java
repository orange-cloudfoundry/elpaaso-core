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

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * Sample logical model for the orange dbaas core.<br>
 */
public class DbaasCoreLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	String APP_CODE = "dbaas.core";

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		// WAR "dbaas-portal"
		ProcessingNode cfProcessing = createCFJavaProcessing(this, "dbaas-core", APP_CODE,ArtefactType.war);
		existingLDToUpdate.addExecutionNode(cfProcessing);

		// Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("dbaas-core", APP_CODE, true, false, 1, 20, ArtefactType.war);
		
		existingLDToUpdate.addLogicalService(web);
		cfProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		//add config service
		String configSetContent="";
		try {
			LogicalConfigService config=new LogicalConfigService("dbaas-core-config", existingLDToUpdate, configSetContent);
			cfProcessing.addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);			
		} catch (InvalidConfigServiceException e) {
			throw new TechnicalException(e);
		}


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
		return "Dbaas core is the Orange DBaas provisionning engine.";
    }

    @Override
    public String getAppCode() {
		return "dbaas-core";
    }

    @Override
    public String getAppLabel() {
		return "dbaas-core";
    }

    @Override
    public String getAppReleaseDescription() {
		return "dbaas core";
    }

    @Override
    public String getAppReleaseVersion() {
        return "1.8.11";
    }
}
