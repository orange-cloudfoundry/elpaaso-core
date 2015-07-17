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
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;

/**
 * Sample application for the Spring Cloud Microservices  support (hystrix/ eureka, ...) 
 */
public class SpringCloudMicroservicesLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	
	String DASHBOARD="microservices.dashboard";
	String CONFIG_SERVER="microservices.configServer";
	String CONFIG_CLIENT="microservices.configClient";
	String EUREKA="microservices.eureka";
	String TURBINE="microservices.turbine";
	String HYSTRIX="microservices.hystrix";
	String ZUUL="microservices.zuul";
			
	

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		
		
		//processings
		CFJavaProcessing configServer=createCFJavaProcessing(this, "config-server",CONFIG_SERVER, ArtefactType.jar);
		CFJavaProcessing configClient=createCFJavaProcessing(this, "config-client",CONFIG_CLIENT, ArtefactType.war);
		CFJavaProcessing dashboard=createCFJavaProcessing(this, "hystrix-dashboard",DASHBOARD, ArtefactType.jar);
		CFJavaProcessing eureka=createCFJavaProcessing(this, "eureka",EUREKA, ArtefactType.jar);
		CFJavaProcessing turbine=createCFJavaProcessing(this, "turbine",TURBINE, ArtefactType.jar);
		CFJavaProcessing hystrix=createCFJavaProcessing(this, "hystrix",HYSTRIX, ArtefactType.jar);		
		CFJavaProcessing zuul=createCFJavaProcessing(this, "zuul",ZUUL, ArtefactType.jar);		
		
		
		existingLDToUpdate.addExecutionNode(configServer);
		existingLDToUpdate.addExecutionNode(configClient);
		existingLDToUpdate.addExecutionNode(dashboard);
		existingLDToUpdate.addExecutionNode(eureka);
		existingLDToUpdate.addExecutionNode(turbine);
		existingLDToUpdate.addExecutionNode(hystrix);
		existingLDToUpdate.addExecutionNode(zuul);		
		

		
		//web access
		LogicalWebGUIService eurekaWeb = createLogicalWebGuiService("eureka-web", EUREKA, true, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(eurekaWeb);
		eureka.addLogicalServiceUsage(eurekaWeb, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		LogicalWebGUIService configServerWeb = createLogicalWebGuiService("config-server-web", CONFIG_SERVER, true, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(configServerWeb);
		configServer.addLogicalServiceUsage(configServerWeb, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		LogicalWebGUIService configClientWeb = createLogicalWebGuiService("config-client-web", CONFIG_CLIENT, true, false, 1, 20, ArtefactType.war);
		existingLDToUpdate.addLogicalService(configClientWeb);
		configClient.addLogicalServiceUsage(configClientWeb, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		LogicalWebGUIService dashboardWeb = createLogicalWebGuiService("dashboard-web", DASHBOARD, true, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(dashboardWeb);
		dashboard.addLogicalServiceUsage(dashboardWeb, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		LogicalWebGUIService hystrixWeb = createLogicalWebGuiService("hystrix-web", HYSTRIX, true, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(hystrixWeb);
		hystrix.addLogicalServiceUsage(hystrixWeb, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		

		LogicalWebGUIService zuulWeb = createLogicalWebGuiService("zuul-web", ZUUL, true, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(zuulWeb);
		zuul.addLogicalServiceUsage(zuulWeb, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		
		//turbine

        LogicalRabbitService rabbitTurbine = createInternalRabbitService("Turbine Queue", "turbine");
        existingLDToUpdate.addLogicalService(rabbitTurbine);
        turbine.addLogicalServiceUsage(rabbitTurbine, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        hystrix.addLogicalServiceUsage(rabbitTurbine, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        zuul.addLogicalServiceUsage(rabbitTurbine, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

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
        return "micro-services app";
    }

    @Override
    public String getAppCode() {
        return "MMS";
    }

    @Override
    public String getAppLabel() {
        return "microservices";
    }

    @Override
    public String getAppReleaseDescription() {
        return "microservices Release";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
