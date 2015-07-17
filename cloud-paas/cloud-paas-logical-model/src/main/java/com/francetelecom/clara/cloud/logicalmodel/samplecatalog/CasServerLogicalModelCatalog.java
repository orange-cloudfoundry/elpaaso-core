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

import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * JEE Probe LogicalModel Sample : a single execution node with only one web gui service
 */
public class CasServerLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    String APP_CODE = "cas";
    
    private static final String CAS_CONFIG="ldap.url=ldap://ladp.elpaaso.org:10389";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

        if (existingLDToUpdate == null) {
            existingLDToUpdate = new LogicalDeployment();
        }

		LogicalWebGUIService webGui = createLogicalWebGuiService("cas-gui", APP_CODE, true, false, 1, 20);
		ProcessingNode jeeProcessing = createJeeProcessing(this, "CasExecNode", APP_CODE);
        LogicalConfigService casConfig = createLogicalConfigService("cas-config","",CAS_CONFIG);

        existingLDToUpdate.addLogicalService(webGui);
        existingLDToUpdate.addExecutionNode(jeeProcessing);
        existingLDToUpdate.addLogicalService(casConfig);

        jeeProcessing.addLogicalServiceUsage(webGui, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;
    }

    /**
	 * @param baseUrl  Correspond to the context root of the EAR to test. Can be use to
	 *          filter multiples EAR tests. Ignore it if you just have one EAR.
	 * @return list of urls and corresponding keywords used to check application
	 *         deployment
	 */
	@Override
	public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		urls.put("/", "Cas Server");
		return urls;
	}

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public String getAppDescription() {
        return "JASIG CAS server";
    }

    @Override
    public String getAppCode() {
        return "CAS";
    }

    @Override
    public String getAppLabel() {
        return "CasServerSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "CAS Server release";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }

}
