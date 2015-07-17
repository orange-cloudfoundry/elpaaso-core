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
import com.francetelecom.clara.cloud.logicalmodel.LogicalRabbitService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;

/**
 * Sample application for the internal RabbitMQ service, with two execution nodes exchanging
 * messaging using two queues.
 * <ul>
 * <li>The first one, known as the "client", should send messages on the
 * "Request" queue.</li>
 * <li>The second one, known as the "server", should be notified of the request
 * messages and send replies on the "Response" queue.</li>
 * </ul>
 */
public class InternalRabbitLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	String APP_CODE_1 = "internalrabbit.client";
	String APP_CODE_2 = "internalrabbit.server";

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		//"client"
		CFJavaProcessing clientJeeProcessing=createCFJavaProcessing(this, "InternalRabbitJEE-Client", APP_CODE_1, ArtefactType.jar);
		existingLDToUpdate.addExecutionNode(clientJeeProcessing);

		//"server"
		CFJavaProcessing serverJeeProcessing=createCFJavaProcessing(this, "InternalRabbitJEE-Server", APP_CODE_2, ArtefactType.jar);
		
		existingLDToUpdate.addExecutionNode(serverJeeProcessing);

		// Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("InternalRabbitJEEClientWebUi", APP_CODE_1, true, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(web);
		clientJeeProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        LogicalRabbitService rabbitRequestService = createInternalRabbitService("Request Queue", "request");
        existingLDToUpdate.addLogicalService(rabbitRequestService);

        LogicalRabbitService rabbitReplyService = createInternalRabbitService("Response Queue", "response");
        existingLDToUpdate.addLogicalService(rabbitReplyService);

		clientJeeProcessing.addLogicalServiceUsage(rabbitRequestService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		serverJeeProcessing.addLogicalServiceUsage(rabbitRequestService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		clientJeeProcessing.addLogicalServiceUsage(rabbitReplyService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		serverJeeProcessing.addLogicalServiceUsage(rabbitReplyService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		// Add a datasource
//        LogicalRelationalService rabbitds = createLogicalRelationalService(this, "RabbitDataSource", "probe-db-postgres",
//                LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 100, null);
//        existingLDToUpdate.addLogicalService(rabbitds);
//        serverJeeProcessing.addLogicalServiceUsage(rabbitds, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

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
        return "MyInternalRabbitSample description";
    }

    @Override
    public String getAppCode() {
        return "MyInternalRabbitSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "MyInternalRabbitSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyInternalRabbitSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
