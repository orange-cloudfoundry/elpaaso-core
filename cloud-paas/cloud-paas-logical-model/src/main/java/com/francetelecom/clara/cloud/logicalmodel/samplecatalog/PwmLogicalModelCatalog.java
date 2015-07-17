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
import com.francetelecom.clara.cloud.logicalmodel.LogicalOnlineStorageService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * Sample logical model for pwm password management tool. It is used to managed a LDAP directory through a webapp.<br>
 * <a href="https://code.google.com/p/pwm/">PWM website</a>
 */
public class PwmLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	String APP_CODE = "password.management";

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		// WAR "pwm"
		ProcessingNode cfProcessing = createCFJavaProcessing(this, "PasswordManagement", APP_CODE,ArtefactType.war);
		existingLDToUpdate.addExecutionNode(cfProcessing);

		// Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("PasswordManagementWebUi", APP_CODE, false, false, 1, 20, ArtefactType.war);
		
		existingLDToUpdate.addLogicalService(web);
		cfProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		// Add Config Service. sets the aspectjweaver via JAVA_OPTS env varible
		String configSetContent="JAVA_OPTS=-javaagent:/home/vcap/app/WEB-INF/lib/aspectjweaver-1.8.5.jar";
		try {
			LogicalConfigService config=new LogicalConfigService("pwm-portal-config", existingLDToUpdate, configSetContent);
			cfProcessing.addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);			
		} catch (InvalidConfigServiceException e) {
			throw new TechnicalException(e);
		}

		
		
		//add riakcs s3 bucket to persist configuration file
		LogicalOnlineStorageService storageService = createLogicalOnlineStorage("s3-bucket-pwm-config", "s3-bucket-pwm-config", 100000000);
        storageService.setLabel("s3-bucket-pwm-config");
        existingLDToUpdate.addLogicalService(storageService);
        cfProcessing.addLogicalServiceUsage(storageService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

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
		return "PWM is an open source password self service application for LDAP directories.";
    }

    @Override
    public String getAppCode() {
		return "PasswordManagementSampleCODE";
    }

    @Override
    public String getAppLabel() {
		return "PasswordManagement";
    }

    @Override
    public String getAppReleaseDescription() {
		return "PasswordManagement release";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
