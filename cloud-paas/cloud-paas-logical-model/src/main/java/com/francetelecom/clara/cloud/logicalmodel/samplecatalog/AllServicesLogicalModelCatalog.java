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
import java.util.Map;

import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalMomService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalOnlineStorageService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalQueueReceiveService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalQueueSendService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapConsumer;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 27/07/12
 */
public class AllServicesLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    public static final String APP_CODE = "AllServices";
    public static final String JNDI_PREFIX_FOR_CONFIG  = "";

    public static final String QSS_APP_NAME = "PtpmBackOffice";
    public static final String QSS_APP_VERSION = "G1R2C0";
    public static final String QSS_APP_BASICAT = "bck";
    public static final String QSS_SERVICE_NAME = "ping";
    public static final String QSS_SERVICE_VERSION = "G1R1C0";

    public static final String QRS_APP_NAME = "PtpmFrontOffice";
    public static final String QRS_APP_VERSION = "G1R1C0";
    public static final String QRS_APP_BASICAT = "frt";
    public static final String QRS_SERVICE_NAME = "pong";
    public static final String QRS_SERVICE_VERSION = "G1R1C0";

    public static final String SOAP_PROVIDER_SERVICE_LABEL = "SoapProvider";
    public static final String SOAP_PROVIDER_SERVICE_NAME = "Echo";
    public static final String SOAP_PROVIDER_SERVICE_JNDI_PREFIX = "EchoService";
    public static final int SOAP_PROVIDER_SERVICE_MAJOR_VERSION = 1;
    public static final int SOAP_PROVIDER_SERVICE_MINOR_VERSION = 2;

    public static String configSetContent = "#Defines the email contact that appears in the UI for asking help. Should be overriden per environment\n" +
            "support-email-contact=delis-dev-support@orange.com\n" +
            "#Ability to turn off the UI to rejects HTTP requests without the SSO gassi header. Use only on dev environments as a convenient for local testing\n"+
            "reject_unauthenticated_gassi_logins=true";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

        if (existingLDToUpdate == null) {
            existingLDToUpdate = new LogicalDeployment();
        }

		LogicalWebGUIService webGui = createLogicalWebGuiService(APP_CODE + "WebUi", "springoo", true, false, 1, 20);
        LogicalQueueSendService qss = createLogicalQueueSend(APP_CODE + "QSS", QSS_SERVICE_NAME, QSS_SERVICE_VERSION, QSS_APP_BASICAT, QSS_APP_NAME, QSS_APP_VERSION, "ping.out", 5, 50, 1);
        LogicalQueueReceiveService qrs = createLogicalQueueReceive(APP_CODE + "QRS", QRS_SERVICE_NAME, QRS_SERVICE_VERSION, "pong.in", 10, 10, 1);
        LogicalSoapConsumer soapConsumer = createLogicalSoapConsumer("echoConsumerSoap", "API_SHOP", "Echo", 1, 0, "Echo");
        LogicalSoapService soapProvider = createLogicalSoapService(this, SOAP_PROVIDER_SERVICE_LABEL, SOAP_PROVIDER_SERVICE_NAME, SOAP_PROVIDER_SERVICE_MAJOR_VERSION, SOAP_PROVIDER_SERVICE_MINOR_VERSION, SOAP_PROVIDER_SERVICE_JNDI_PREFIX, null, "ElPaaso Logical Model catalog : Echo SOAP Service", "echoService");
        LogicalMomService mom = createInternalMomService(APP_CODE + "InternlMom", "Request", 3, 2000, "DeadLetterQueue");
        LogicalConfigService config = createLogicalConfigService(APP_CODE + "Config", JNDI_PREFIX_FOR_CONFIG, configSetContent);
        LogicalRelationalService relationalDB = createLogicalRelationalService(this, "SpringooRds", "postgres-MyDataSource", LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 1000, "springoo");
        LogicalOnlineStorageService blobStore = createLogicalOnlineStorage(APP_CODE + "BlobStore", "blob_prefix", 20);

        ProcessingNode jeeProcessing1 = createJeeProcessing(this, APP_CODE+"JeeProcessing1", "springoo");
        ProcessingNode jeeProcessing2 = createJeeProcessing(this, APP_CODE+"JeeProcessing2", "wicketoo");
        //ProcessingNode jeeProcessing3 = createJeeProcessing(this, APP_CODE+"JeeProcessing3", "petclinic");
        ProcessingNode cfjavaProcessing1 = createCFJavaProcessing(this, APP_CODE+"CFJavaProcessing3", "petclinic", ArtefactType.ear);

        existingLDToUpdate.addLogicalService(webGui);
        existingLDToUpdate.addLogicalService(qss);
        existingLDToUpdate.addLogicalService(qrs);
        existingLDToUpdate.addLogicalService(soapConsumer);
        existingLDToUpdate.addLogicalService(soapProvider);
        existingLDToUpdate.addLogicalService(mom);
        existingLDToUpdate.addLogicalService(config);
        existingLDToUpdate.addLogicalService(relationalDB);
        existingLDToUpdate.addLogicalService(blobStore);
        existingLDToUpdate.addExecutionNode(jeeProcessing1);
        existingLDToUpdate.addExecutionNode(jeeProcessing2);
        existingLDToUpdate.addExecutionNode(cfjavaProcessing1);

        jeeProcessing1.addLogicalServiceUsage(webGui, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(qss, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(qrs, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(soapConsumer, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(soapProvider, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(mom, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(relationalDB, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing1.addLogicalServiceUsage(blobStore, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        jeeProcessing2.addLogicalServiceUsage(webGui, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing2.addLogicalServiceUsage(qss, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing2.addLogicalServiceUsage(qrs, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        jeeProcessing2.addLogicalServiceUsage(soapConsumer, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        cfjavaProcessing1.addLogicalServiceUsage(webGui, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        cfjavaProcessing1.addLogicalServiceUsage(qss, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        cfjavaProcessing1.addLogicalServiceUsage(qrs, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        cfjavaProcessing1.addLogicalServiceUsage(soapConsumer, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        cfjavaProcessing1.addLogicalServiceUsage(soapProvider, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        cfjavaProcessing1.addLogicalServiceUsage(mom, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        cfjavaProcessing1.addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        cfjavaProcessing1.addLogicalServiceUsage(relationalDB, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        cfjavaProcessing1.addLogicalServiceUsage(blobStore, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public String getAppCode() {
        return "ALLSERV";
    }

    @Override
    public String getAppLabel() {
        return "All services test application";
    }

    @Override
    public String getAppReleaseVersion() {
        return "TEST0";
    }
}
