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
import com.francetelecom.clara.cloud.logicalmodel.LogicalOnlineStorageService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * Catalog of sample logical models for the PetClinic application with the
 * following variations: storage capacity, number of concurrent sessions
 */
public class PetcliniccLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    private static String APP_CODE = "petclinic";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {
        if (existingLDToUpdate == null) {
            existingLDToUpdate = new LogicalDeployment();
        }

		LogicalWebGUIService petClinicWebUi = createLogicalWebGuiService("PetClinicWebUi", APP_CODE, true, false, 1, 20);

		LogicalRelationalService petclinicRds = createLogicalRelationalService(this, "PetClinicRds", "postgres-MyDataSource",
				LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 1000, APP_CODE);

        LogicalOnlineStorageService petclinicOnlineStorage = createLogicalOnlineStorage("PetClinicOnlineStorage", "petpictures", 10);

        ProcessingNode jeeProcessing = createJeeProcessing(this, "PetClinic_JEE_processing", APP_CODE);

        existingLDToUpdate.addLogicalService(petClinicWebUi);
        existingLDToUpdate.addLogicalService(petclinicRds);
        existingLDToUpdate.addLogicalService(petclinicOnlineStorage);
        existingLDToUpdate.addExecutionNode(jeeProcessing);

        jeeProcessing.addLogicalServiceUsage(petClinicWebUi, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing.addLogicalServiceUsage(petclinicRds, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing.addLogicalServiceUsage(petclinicOnlineStorage, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;
    }

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		urls.put("/owners/1001", "Owner Information");
		urls.put("/test", "");
		return urls;
    }

    @Override
    public String getAppDescription() {
        return "MyPetclinicSample description";
    }

    @Override
    public String getAppCode() {
        return "MyPetclinicSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "MyPetclinicSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyPetclinicSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
