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
 * ElPaaSoTomcatLogicalModelCatalog Describes the El Paaso portal application so it
 * can be deployed on Paas as a Tomcat War
 * 
 * @author : $Author: apog7416 $
 * http://elpaaso_shp/index.php/PaasDogFooding
 */
public class ElPaaSoTomcatLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	private static final String PAAS_PORTAL = "paas-portal";
	private String contextPropertiesPath;

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {
		LogicalDeployment ldtToUpdate = existingLDToUpdate;

		if (ldtToUpdate == null) {
			ldtToUpdate = new LogicalDeployment();
		}

		// PaaS WAR definition
		CFJavaProcessing javaProcessing=createCFJavaProcessing(this, "ElPaasoTomcat", "elpaaso-tomcat", ArtefactType.war);
		
		javaProcessing.setMinMemoryMbHint(2048);
		javaProcessing.setMinDiskMbHint(2000);
		ldtToUpdate.addExecutionNode(javaProcessing);

		// ~ add PaaS GUI
		LogicalWebGUIService web = createLogicalWebGuiService(PAAS_PORTAL, "elpaaso-tomcat", true, false, 1, 20,ArtefactType.war);
		//TODO: reactive statefull when HttpSession clustering available
		web.setStateful(false);
		
		ldtToUpdate.addLogicalService(web);
		javaProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);



		// ~ add PaaS PostgreSQL dbs
		// portal Datasource
		
		
		//FIXME : jndi name equivalent in CloudFoundry must be service name ? ...
		LogicalRelationalService webAppDS = createLogicalRelationalService(this, "elpaaso-rds", "postgres-db-paas",
				LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 1000, null);
		ldtToUpdate.addLogicalService(webAppDS);
		javaProcessing.addLogicalServiceUsage(webAppDS, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// activiti Datasource (used by activation process)
		LogicalRelationalService activitiDS = createLogicalRelationalService(this, "elpaaso-activitiDS", "postgres-activiti-paas",
				LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 1000, null);
		ldtToUpdate.addLogicalService(activitiDS);
		javaProcessing.addLogicalServiceUsage(activitiDS, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// Set configuration with credentials so it can vary for each datacenter
		StringBuffer configBuffer = createConfigContent();

		// Prefix must be empty (not null) otherwise you must change all
		// property refrences in clara-paas-agregate :-)
		
		
		//FIXME : properties in tomcat have slight difference (ie /home/vcap for maven local repo location)
		
		LogicalConfigService demoConfig = createLogicalConfigService("elpaaso-config", "", configBuffer.toString());
		ldtToUpdate.addLogicalService(demoConfig);
		javaProcessing.addLogicalServiceUsage(demoConfig, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		
		//FIXME. add rabbitmq service (to permit switch from in JVM ActiveMQ to cf service messaging)
		
		LogicalRabbitService rabbitService=createInternalRabbitService("ActivationAmqpBroker", "activationAmqpBroker");
		ldtToUpdate.addLogicalService(rabbitService);
		javaProcessing.addLogicalServiceUsage(rabbitService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

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
		// northbound API
		urls.put("/api/soap/", "PaasEnvironmentService");
		// portal login
		urls.put("/portal/home", "Login");
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
		return "MyElPaasoTomcatSampleCODE";
	}

	@Override
	public String getAppLabel() {
		return "MyElPaasoTomcatSample";
	}

	@Override
	public String getAppReleaseDescription() {
		return "MyElPaasoTomcatSample release description";
	}

	@Override
	public String getAppReleaseVersion() {
		return "G00R01";
	}
}
