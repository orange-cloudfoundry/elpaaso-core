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
 * ElPaaSoLogicalModelCatalog Describes the El Paaso portal application so it
 * can be deployed on Paas. Sample usage : cf. PaasServicesEnvElPaaSoIT Last
 * 
 *  http://elpaaso_shp/index.php/PaasDogFooding
 *  http://elpaaso_shp/index.php/El_PaaSo_Installation_Guide#Automatic_installation
 */
public class ElPaaSoLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	private static final String PAAS_PORTAL_JMX = "paas-portal-jmx";
	private static final String PAAS_PORTAL = "paas-portal";
	private String contextPropertiesPath;

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {
		LogicalDeployment ldtToUpdate = existingLDToUpdate;

		if (ldtToUpdate == null) {
			ldtToUpdate = new LogicalDeployment();
		}

		// PaaS EAR definition
		ProcessingNode jeeProcessing = createJeeProcessing(this, "ElPaasoJEE", "elpaaso");
		jeeProcessing.setMinMemoryMbHint(1024);
		ldtToUpdate.addExecutionNode(jeeProcessing);

		// ~ add PaaS GUI
		LogicalWebGUIService web = createLogicalWebGuiService(PAAS_PORTAL, "elpaaso", true, false, 1, 20);
		ldtToUpdate.addLogicalService(web);
		jeeProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// ~ add Jmx Exposition point
		LogicalWebGUIService jmx = createLogicalWebGuiService(PAAS_PORTAL_JMX, "elpaaso", true, false, 1, 20);
		ldtToUpdate.addLogicalService(jmx);
        jmx.setContextRoot(new ContextRoot(sampleAppProperties.getProperty("elpaaso", "ear", "context-root-jmx")));

		jeeProcessing.addLogicalServiceUsage(jmx, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// ~ add PaaS PostgreSQL dbs
		// portal Datasource
		LogicalRelationalService webAppDS = createLogicalRelationalService(this, "elpaaso-rds", "jdbc/cloud-webappDataSource",
				LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 10, null);
		ldtToUpdate.addLogicalService(webAppDS);
		jeeProcessing.addLogicalServiceUsage(webAppDS, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// activiti Datasource (used by activation process)
		LogicalRelationalService activitiDS = createLogicalRelationalService(this, "elpaaso-activitiDS", "jdbc/activitiDS",
				LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 10, null);
		ldtToUpdate.addLogicalService(activitiDS);
		jeeProcessing.addLogicalServiceUsage(activitiDS, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// Set configuration with credentials so it can vary for each datacenter
		StringBuffer configBuffer = createConfigContent();

		// Prefix must be empty (not null) otherwise you must change all
		// property refrences in clara-paas-agregate :-)
		LogicalConfigService demoConfig = createLogicalConfigService("elpaaso-config", "", configBuffer.toString());
		ldtToUpdate.addLogicalService(demoConfig);
		jeeProcessing.addLogicalServiceUsage(demoConfig, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		LogicalMomService momService = createInternalMomService("ActivationRequest Queue", "jms/activationRequestQueue", 10, 5000,
				"jms/activationDeadLetterQueue");
		ldtToUpdate.addLogicalService(momService);
		jeeProcessing.addLogicalServiceUsage(momService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		momService = createInternalMomService("ActivationReply Queue", "jms/activationReplyQueue", 3, 5000, "jms/activationDeadLetterQueue");
		ldtToUpdate.addLogicalService(momService);
		jeeProcessing.addLogicalServiceUsage(momService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		momService = createInternalMomService("ActivationError Queue", "jms/activationErrorQueue", 3, 5000, null);
		ldtToUpdate.addLogicalService(momService);
		jeeProcessing.addLogicalServiceUsage(momService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		return ldtToUpdate;

	}

	protected StringBuffer createConfigContent() {
		StringBuffer configBuffer = new StringBuffer();
		configBuffer.append("#Please fill here content from a credentials.properties file\n");
		return configBuffer;
	}

	@Override
	public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		if (baseUrl.toString().contains("jmx")) {
			// Cannot check url "/info/" neither "/info/list" as these pages
			// require authentication
			urls.put("/help.html", "JMX Usage Page");
		} else {
			// northbound API
			urls.put("/api/soap/", "PaasEnvironmentService");
			// portal login
			urls.put("/portal/home", "Login");
		}
		return urls;
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	public String getContextPropertiesPath() {
		return contextPropertiesPath;
	}

	public void setContextPropertiesPath(String contextPropertiesPath) {
		this.contextPropertiesPath = contextPropertiesPath;
	}

	@Override
	public String getAppDescription() {
		return "Models the PaaS internal implementation. This is used by the paas engineering team to automate the paas deployment. We call it: eat-your-own-dog-food, some prefer drink-your-own-champagne :-)";
	}

	@Override
	public String getAppCode() {
		return "MyElPaasoSampleCODE";
	}

	@Override
	public String getAppLabel() {
		return "MyElPaasoSample";
	}

	@Override
	public String getAppReleaseDescription() {
		return "MyElPaasoSample release description";
	}

	@Override
	public String getAppReleaseVersion() {
		return "G00R01";
	}
}
