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

import com.francetelecom.clara.cloud.logicalmodel.CFJavaProcessing;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalOnlineStorageService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;


/**
 * A sample application testing the Online storage service (blobstore) service using the JClouds Blob Store API.
 *
 */
public class StoragesLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {


    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}

        
        //JAR
		CFJavaProcessing javaProcessing=createCFJavaProcessing(this, "StorageJEE", "storage-probe", ArtefactType.jar);
		javaProcessing.setLabel("StorageJEE");
        existingLDToUpdate.addExecutionNode(javaProcessing);		
        

		// Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("storage-probe-gui", "storage-probe", true, false, 1, 20,ArtefactType.jar);
        web.setLabel("storeConsumerProbe");
        existingLDToUpdate.addLogicalService(web);
		javaProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        // Storage
		//FIXME : jndiprefix to replace with cf service-name
		LogicalOnlineStorageService storageService = createLogicalOnlineStorage("jclouds", "s3-myriak", 100000000);
        storageService.setLabel("jclouds");
        existingLDToUpdate.addLogicalService(storageService);
        javaProcessing.addLogicalServiceUsage(storageService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;

    }

    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		return urls;
    }

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public String getAppDescription() {
        return "StoragesSample description";
    }

    @Override
    public String getAppCode() {
        return "StoragesSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "StoragesSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "StoragesSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
