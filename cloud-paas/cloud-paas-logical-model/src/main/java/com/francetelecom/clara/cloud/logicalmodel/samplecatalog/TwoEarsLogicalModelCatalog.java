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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * A sample app featuring two distinct EAR: 2 Springoo, sharing the same database
 */
public class TwoEarsLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	protected static Logger logger = LoggerFactory.getLogger(com.francetelecom.clara.cloud.logicalmodel.samplecatalog.TwoEarsLogicalModelCatalog.class.getName());

    public final String APP_CODE = "springoo";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}


		// Execution Node 1
		ProcessingNode jeeProcessing1 = createJeeProcessing(this, "TwoEarJEE_1", APP_CODE);
		existingLDToUpdate.addExecutionNode(jeeProcessing1);

		// Execution Node 2
		ProcessingNode jeeProcessing2 = createJeeProcessing(this, "TwoEarJEE_2", APP_CODE);
		existingLDToUpdate.addExecutionNode(jeeProcessing2);

		// =============================================================
		// == Create Common database elements
		// =============================================================
		LogicalRelationalService rds0 = createLogicalRelationalService(this, "TwoEarRds", "postgres-MyDataSource",
                LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 1000, APP_CODE);
        existingLDToUpdate.addLogicalService(rds0);
        jeeProcessing1.addLogicalServiceUsage(rds0, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing2.addLogicalServiceUsage(rds0, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		// =============================================================
		// == Create Elements for TwoEarJEE_1
		// =============================================================

		// Web GUI 1
		LogicalWebGUIService web1 = createLogicalWebGuiService("TwoEarWebUi_1", APP_CODE, true, false, 1, 20);
        existingLDToUpdate.addLogicalService(web1);
		jeeProcessing1.addLogicalServiceUsage(web1, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// =============================================================
		// == Create Elements for TwoEarJEE_2
		// =============================================================

		// Web GUI 2
		LogicalWebGUIService web2 = createLogicalWebGuiService("TwoEarWebUi_2", APP_CODE, false, false, 1, 20);
        existingLDToUpdate.addLogicalService(web2);
		jeeProcessing2.addLogicalServiceUsage(web2, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;
    }

    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		if (baseUrl.toString().contains("springoo")) {
			urls.put("/web/divers/choice.jsp", "springoo");
		}
		return urls;
    }

    @Override
    public boolean isInstantiable() {
		return true;
    }

    @Override
    public String getAppDescription() {
        return "TwoEarsSample description";
    }

    @Override
    public String getAppCode() {
        return "TwoEarsSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "TwoEarsSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "TwoEarsSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
