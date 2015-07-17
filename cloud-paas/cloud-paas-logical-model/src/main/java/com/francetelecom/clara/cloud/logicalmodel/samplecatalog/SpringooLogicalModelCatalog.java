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
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * A utility class which creates a reference logical model illustrating springoo with empty constructor.
 * This is mainly used for testing transition from strongly typed constructors to empty constructors.
 */
public class SpringooLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    String APP_CODE = "springoo";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

        if (existingLDToUpdate == null) {
            existingLDToUpdate = new LogicalDeployment();
        }

		LogicalWebGUIService webGui = createLogicalWebGuiService("SpringooWebUi", APP_CODE, true, false, 1, 20);
        LogicalRelationalService relationalDB = createLogicalRelationalService(this, "SpringooRds", "jdbc/MyDataSource", LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 1000, APP_CODE);
        ProcessingNode jeeProcessing = createJeeProcessing(this, "Springoo_Jee_processing", APP_CODE);

        existingLDToUpdate.addLogicalService(webGui);
        existingLDToUpdate.addLogicalService(relationalDB);
        existingLDToUpdate.addExecutionNode(jeeProcessing);

        jeeProcessing.addLogicalServiceUsage(webGui, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing.addLogicalServiceUsage(relationalDB, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

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
		urls.put("/web/divers/choice.jsp", "springoo");
		return urls;
	}

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public String getAppDescription() {
        return "SpringooSample description";
    }

    @Override
    public String getAppCode() {
        return "SpringooSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "SpringooSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "SpringooSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
