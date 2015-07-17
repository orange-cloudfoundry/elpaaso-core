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

import com.francetelecom.clara.cloud.logicalmodel.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class which creates a reference logical model illustrating springoo with empty constructor.
 * This is mainly used for testing transition from strongly typed constructors to empty constructors.
 */
public class CFWicketCxfJpaLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    String APP_CODE = "cf-wicket-jpa";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

        if (existingLDToUpdate == null) {
            existingLDToUpdate = new LogicalDeployment();
        }

        LogicalWebGUIService webGui = createLogicalWebGuiService("webUi", APP_CODE, true, false, 1, 20, ArtefactType.war);
        LogicalRelationalService relationalDB = createLogicalRelationalService(this, "db", "postgres-db", LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 1000, APP_CODE);
        CFJavaProcessing cfJavaProcessing = createCFJavaProcessing(this, getAppLabel(), APP_CODE, ArtefactType.war);

        existingLDToUpdate.addLogicalService(webGui);
        existingLDToUpdate.addLogicalService(relationalDB);
        existingLDToUpdate.addExecutionNode(cfJavaProcessing);

        cfJavaProcessing.addLogicalServiceUsage(webGui, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        cfJavaProcessing.addLogicalServiceUsage(relationalDB, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

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
		urls.put("/app/homepage", "cf-wicket-jpa");
		return urls;
	}

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public String getAppDescription() {
        return "Cf-wicket-jpaSample description";
    }

    @Override
    public String getAppCode() {
        return "Cf-wicket-jpaSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "Cf-wicket-jpaSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "Cf-wicket-jpaSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
