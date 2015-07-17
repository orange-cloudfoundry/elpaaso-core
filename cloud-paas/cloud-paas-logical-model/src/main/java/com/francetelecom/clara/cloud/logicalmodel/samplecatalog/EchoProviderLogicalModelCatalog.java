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
import com.francetelecom.clara.cloud.logicalmodel.LogicalInboundAuthenticationPolicy;
import com.francetelecom.clara.cloud.logicalmodel.LogicalOutboundAuthenticationPolicy;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * EchoServiceLogicalModelCatalog
 * Sample app which supports black box tests for the WSP service in the catalog.
 * The Echo Service Probe application provide an echo web service.
 *
 * Last updated : $LastChangedDate$
 * Last author  : $Author$
 * @version     : $Revision$
 */
public class EchoProviderLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    public static final String APPLICATION_CODE = "ECHOPROVIDERTEST";
	public static final String SOAP_SERVICE_LABEL = "echoSoapService";
    public static final String SOAP_SERVICE_JNDI_PREFIX = "EchoService";
    public static final String SOAP_SERVICE_NAME = "Echo";
    public static final int SOAP_SERVICE_MAJOR_VERSION = 1;
    public static final int SOAP_SERVICE_MINOR_VERSION = 2;
    public static final String WEBGUI_SERVICE_LABEL = "echoServiceWebUi";

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {
    if (existingLDToUpdate == null) {
            existingLDToUpdate = new LogicalDeployment();
        }
        // EAR
    	ProcessingNode jeeProcessing = createJeeProcessing(this, "echoServiceJEE", "echoService");
        existingLDToUpdate.addExecutionNode(jeeProcessing);

        // Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService(WEBGUI_SERVICE_LABEL, "echoService", true, false, 1, 20);
        existingLDToUpdate.addLogicalService(web);
        jeeProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        String serviceRootFilename;
        // SOAP Service
        LogicalSoapService soapService= createLogicalSoapService(
                this,
                SOAP_SERVICE_LABEL,
                SOAP_SERVICE_NAME,
                SOAP_SERVICE_MAJOR_VERSION,
                SOAP_SERVICE_MINOR_VERSION,
                SOAP_SERVICE_JNDI_PREFIX,
                null,
                "ElPaaso Logical Model catalog : Echo SOAP Service",
                "echoService");

        soapService.setInboundAuthenticationPolicy(new LogicalInboundAuthenticationPolicy());
        soapService.setOutboundAuthenticationPolicy(new LogicalOutboundAuthenticationPolicy());

        existingLDToUpdate.addLogicalService(soapService);
        jeeProcessing.addLogicalServiceUsage(soapService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        return existingLDToUpdate;

    }

    /**
     * @param baseUrl Correspond to the context root of the EAR to test. Can be use to
     *          filter multiples EAR tests. Ignore it if you just have one EAR.
     * @return list of urls and corresponding keywords used to check application
     *         deployment
     */
    @Override
    public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
        HashMap<String, String> urls = new HashMap<String, String>();
        urls.put("/", "Echo");
        return urls;
    }

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public String getAppDescription() {
        return "Sample app which supports black box tests for the WSP service in the catalog.\n" +
                "The Echo Service Probe application provide an echo web service.";
    }

    @Override
    public String getAppCode() {
        return APPLICATION_CODE;
    }

    @Override
    public String getAppLabel() {
        return "MyEchoProviderSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyEchoProviderSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
