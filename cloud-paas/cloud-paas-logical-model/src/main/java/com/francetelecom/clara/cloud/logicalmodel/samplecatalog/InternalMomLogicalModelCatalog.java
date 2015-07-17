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
import com.francetelecom.clara.cloud.logicalmodel.LogicalMomService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * Sample application for the internal MOM, with two execution nodes exchanging
 * messaging using two queues.
 * <ul>
 * <li>The first one, known as the "client", should send messages on the
 * "Request" queue.</li>
 * <li>The second one, known as the "server", should be notified of the request
 * messages and send replies on the "Response" queue.</li>
 * </ul>
 */
public class InternalMomLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	String APP_CODE_1 = "internalmom.client";
	String APP_CODE_2 = "internalmom.server";

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		// EAR-1 "client"
		ProcessingNode clientJeeProcessing = createJeeProcessing(this, "InternalMomJEE-Client", APP_CODE_1);
		existingLDToUpdate.addExecutionNode(clientJeeProcessing);

		// EAR-2 "server"
		ProcessingNode serverJeeProcessing = createJeeProcessing(this, "InternalMomJEE-Server", APP_CODE_2);
		existingLDToUpdate.addExecutionNode(serverJeeProcessing);

		// Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("InternalMomJEEClientWebUi", APP_CODE_1, true, false, 1, 20);
		existingLDToUpdate.addLogicalService(web);
		clientJeeProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        LogicalMomService momRequestService = createInternalMomService("Request Queue", "Request", 3, 10000, "DeadLetterQueue");
        existingLDToUpdate.addLogicalService(momRequestService);

        LogicalMomService momReplyService = createInternalMomService("Response Queue", "Response", 3, 10000, "DeadLetterQueue");
        existingLDToUpdate.addLogicalService(momReplyService);

		clientJeeProcessing.addLogicalServiceUsage(momRequestService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		serverJeeProcessing.addLogicalServiceUsage(momRequestService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		clientJeeProcessing.addLogicalServiceUsage(momReplyService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		serverJeeProcessing.addLogicalServiceUsage(momReplyService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		// Add a datasource
        LogicalRelationalService momds = createLogicalRelationalService(this, "MomDataSource", "jdbc/momDatasource",
                LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 10, null);
        existingLDToUpdate.addLogicalService(momds);
        serverJeeProcessing.addLogicalServiceUsage(momds, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

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
        return "MyInternalMomSample description";
    }

    @Override
    public String getAppCode() {
        return "MyInternalMomSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "MyInternalMomSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyInternalMomSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
