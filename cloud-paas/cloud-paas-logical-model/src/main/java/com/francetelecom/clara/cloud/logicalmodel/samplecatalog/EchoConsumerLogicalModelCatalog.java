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
 * EchoConsumerLogicalModelCatalog
 *
 * Sample app which supports black box tests for the WSC service in the catalog.
 * The Echo Probe application sends an echo request to a WSP, and receives back an echo response.
 *
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public class EchoConsumerLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

    private static final String SERVICE_NAME = "Echo";
	private static final int SERVICE_MINOR_VERSION = 2;
	private static final int SERVICE_MAJOR_VERSION = 1;
	private static final String SERVICE_PROVIDER_NAME = "ECHOCONSUMERTEST";

	@Override
    public String getAppDescription() {
        return "Sample app which supports black box tests for the WebService Consummer service in the catalog.\n"
        +"This application sends an echo request to a WSP, and receives back an echo response.\n\n"
        +"UI : use jsp/echoTest.jsp and jsp/status.jsp relative Urls.";
    }

    @Override
    public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		// EAR
		ProcessingNode jeeProcessing = createJeeProcessing(this, "echoConsumerJEE", "echo");
        existingLDToUpdate.addExecutionNode(jeeProcessing);

		// Update context root of webGUI
		LogicalWebGUIService web = createLogicalWebGuiService("echoConsumerWebUi", "echo", true, false, 1, 20);
		existingLDToUpdate.addLogicalService(web);
        jeeProcessing.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// SOAP Consumer
		// TODO : check LogicalSoapConsumer parameters
		// On WDM Portal : service = Echo_SoniaVM, service provider = API_SHOP, Version = 1.0
		LogicalSoapConsumer soapOut= createLogicalSoapConsumer("echoConsumerSoap", SERVICE_PROVIDER_NAME, SERVICE_NAME, SERVICE_MAJOR_VERSION, SERVICE_MINOR_VERSION, SERVICE_NAME);
        existingLDToUpdate.addLogicalService(soapOut);
		jeeProcessing.addLogicalServiceUsage(soapOut, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

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
		urls.put("/echoTest.do", SERVICE_NAME);
		return urls;
	}

    @Override
    public boolean isInstantiable() {
        return true;
    }

    @Override
    public String getAppCode() {
        return "MyEchoConsumerSampleCODE";
    }

    @Override
    public String getAppLabel() {
        return "MyEchoConsumerSample";
    }

    @Override
    public String getAppReleaseDescription() {
        return "MyEchoConsumerSample release description";
    }

    @Override
    public String getAppReleaseVersion() {
        return "G00R01";
    }
}
