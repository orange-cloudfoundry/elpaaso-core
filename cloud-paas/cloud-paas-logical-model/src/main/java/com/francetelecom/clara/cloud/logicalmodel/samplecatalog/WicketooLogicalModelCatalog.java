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
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;


/**
 * A sample application demonstrating "eligibility" of the Orange DDSI sample "wicketoo" application.
 */
public class WicketooLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    public static final String DATABASE_CONFIG = "DatabaseConfig";

	String databaseConfigSetContent =
            "# property used for database selection (mysql / oracle / postgresql / hsqldb). See jpaDaoContext.xml"
			+ "\ndatabase.name=mysql";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {
        if (existingLDToUpdate == null) {
            existingLDToUpdate = new LogicalDeployment();
        }

		LogicalWebGUIService wicketooUserWebUi = createLogicalWebGuiService("WicketooUserWebUi", "wicketoo", true, false, 30, 20);

		LogicalRelationalService relationalDB = createLogicalRelationalService(this, "WicketooRds", "mysql-MyDataSource",
				LogicalRelationalServiceSqlDialectEnum.MYSQL_DEFAULT, 1000, "wicketoo");

        // not mandatory : comment while storage is not available
        // LogicalOnlineStorageService onlineStorageService = createLogicalOnlineStorage("WicketooOnlineStorage", "WicketooOnlineStorage", 10);

        LogicalConfigService databaseConfig = createLogicalConfigService(DATABASE_CONFIG, "", databaseConfigSetContent);

        ProcessingNode jeeProcessing1 = createJeeProcessing(this, "WicketooJEE", "wicketoo");

        existingLDToUpdate.addLogicalService(wicketooUserWebUi);

        existingLDToUpdate.addLogicalService(relationalDB);
        // existingLDToUpdate.addLogicalService(onlineStorageService);
        existingLDToUpdate.addLogicalService(databaseConfig);

        existingLDToUpdate.addExecutionNode(jeeProcessing1);

        jeeProcessing1.addLogicalServiceUsage(wicketooUserWebUi, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(relationalDB, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        // jeeProcessing1.addLogicalServiceUsage(onlineStorageService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(databaseConfig, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;
    }

    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		urls.put("/web/divers/choice.jsp", "wicketoo");
		return urls;
    }

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public String getAppDescription() {
        return "WicketooSample description";
    }

    @Override
    public String getAppCode() {
        return "WicketooSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "WicketooSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "WicketooSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
