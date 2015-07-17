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

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Supports FUT demos. Note: Diane is a fictive application which can not be yet instanciated
 */
public class DianeLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    /**
     * logger
     */
    private static Logger logger= LoggerFactory.getLogger(DianeLogicalModelCatalog.class.getName());

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

         // Initialize logical model
         if (existingLDToUpdate == null) {
             existingLDToUpdate = new LogicalDeployment();
         }


        // Jee Processing 1
        // there is no artifact on nexus for application diane , so we need to create the jee processing with springoo conf and after we need to set maven reference built "a la mano"
         ProcessingNode jeeProcessingM2M = createJeeProcessing(this, "Diane_M2M_JEE", "springoo");
        existingLDToUpdate.addExecutionNode(jeeProcessingM2M);

        // Jee Processing 2
        // there is no artifact on nexus for application diane , so we need to create the jee processing with springoo conf and after we need to set maven reference built "a la mano"
        ProcessingNode jeeProcessingAdmin = createJeeProcessing(this, "Diane_admin_JEE", "springoo");
        existingLDToUpdate.addExecutionNode(jeeProcessingAdmin);

        // Jee Processing 3
        // there is no artifact on nexus for application diane , so we need to create the jee processing with springoo conf and after we need to set maven reference built "a la mano"
        ProcessingNode jeeProcessingUser = createJeeProcessing(this, "Diane_user_JEE", "springoo");
        existingLDToUpdate.addExecutionNode(jeeProcessingUser);

        // WebUI service Diane Admin
        LogicalWebGUIService webUiAdmin = createLogicalWebGuiService("Diane_admin_WebUI", "dianeAdmin", false, false, 100, 20);
        existingLDToUpdate.addLogicalService(webUiAdmin);
        jeeProcessingAdmin.addLogicalServiceUsage(webUiAdmin, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        // WebUI service Diane User
		LogicalWebGUIService webUiUser = createLogicalWebGuiService("Diane_user_WebUI", "dianeUser", true, false, 300, 20);
        existingLDToUpdate.addLogicalService(webUiUser);
        jeeProcessingUser.addLogicalServiceUsage(webUiUser, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// SOAP Consumer
		LogicalSoapConsumer brasilWSConsumer = createLogicalSoapConsumer("Brasil_WebService_Consumer", "brasil", "DianeBrasilService", 1, 2, "jndi/BrasilWebServiceConsumer");
		existingLDToUpdate.addLogicalService(brasilWSConsumer);
        jeeProcessingM2M.addLogicalServiceUsage(brasilWSConsumer, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        // Point to point message consumer
        LogicalQueueReceiveService qrsEFB = createLogicalQueueReceive("EFB_queue_consumer", "EFBQueueReceiver", "7.0.0", "srvcrequestqueue", 5, 5, 5);
		existingLDToUpdate.addLogicalService(qrsEFB);
        jeeProcessingM2M.addLogicalServiceUsage(qrsEFB, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        LogicalQueueSendService qssSeba = createLogicalQueueSend("Seba_queue_producer", "subscribeLine", "G1R0C0", "seba", "SEBA IN PAAS", "G6R0C0", "srvcrequestqueue", 5, 5, 5);
		existingLDToUpdate.addLogicalService(qssSeba);
        jeeProcessingM2M.addLogicalServiceUsage(qssSeba, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        // =============================================================
        // == Create database element
        // =============================================================

        // SQL  maven ref: springoo SQL
        // DB 2 : use common database
        // there is no artifact on nexus for application diane , so we need to create the jee processing with springoo conf and after we need to set maven reference built "a la mano"
        LogicalRelationalService rds = createLogicalRelationalService(this, "diane database", "jdbc/jndiDianeDatabase", LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 1000, "springoo");
        existingLDToUpdate.addLogicalService(rds);

        jeeProcessingM2M.addLogicalServiceUsage(rds, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessingAdmin.addLogicalServiceUsage(rds, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessingUser.addLogicalServiceUsage(rds, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
        return new HashMap<String, String>();
    }

    protected URL buildMavenUrl(String artifactUrl) {
        URL url = null;
        try {
            url = new URL(artifactUrl);
        } catch (MalformedURLException e) {
               logger.error("Invalid url: "+artifactUrl, e);
               throw new TechnicalException("Invalid url: "+artifactUrl,e);
        }
        return url;
    }

    @Override
    public String getAppDescription() {
        return "Models the DbaaS use of the paas libraries for instanciating a DBaaS pg instance.";
    }

    @Override
    public String getAppCode() {
        return "MyDianeSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "MyDianeSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyDianeSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
