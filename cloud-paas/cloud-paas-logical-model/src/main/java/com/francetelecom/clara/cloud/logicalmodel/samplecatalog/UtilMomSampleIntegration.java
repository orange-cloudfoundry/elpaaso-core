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

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.JeeProcessing;
import com.francetelecom.clara.cloud.logicalmodel.LogicalMomService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * A utility class which creates a reference logical model for Mom Sample (most easier than momoo).
 * 
 * This is used by projection unit tests tests as the reference logical model. 
 * 
 * This is shared by cloud-paas-integration-test for projection/activation integgration tests.
 * This is also shared with portal tests which assert the generated LogicalModel by the portal is identical.
 *
 * TODO: rename into MomaasLogicalModelCatalog
 * TODO: refactor for Momoo logical model catalog.
 */
public class UtilMomSampleIntegration {

	public static LogicalDeployment createMomSampleLogicalModel(LogicalDeployment logicalDeployment) throws MalformedURLException {
		 
		//TODO: import this from cloud-iaas-api
		//TODO: consider using properties files pointing to MAVEN repository instead of hard coding it? 
		JeeProcessing logicalClientExecNode = new JeeProcessing("MomClientApplication", logicalDeployment);
		MavenReference mavenMomClientRef = createMavenMomClientReference();
		logicalClientExecNode.setSoftwareReference(mavenMomClientRef);

		JeeProcessing logicalServerExecNode = new JeeProcessing("MomServerApplication", logicalDeployment);
		MavenReference mavenMomServerRef = createMavenMomServerReference();
		logicalServerExecNode.setSoftwareReference(mavenMomServerRef);

		LogicalMomService requestMomService = new LogicalMomService("requestService", logicalDeployment);
		requestMomService.setDestinationCapacity(500);
		requestMomService.setDestinationName("request");
		logicalClientExecNode.addLogicalServiceUsage(requestMomService, LogicalServiceAccessTypeEnum.WRITE_ONLY);
		logicalServerExecNode.addLogicalServiceUsage(requestMomService, LogicalServiceAccessTypeEnum.READ_ONLY);
		
		LogicalMomService responseMomService = new LogicalMomService("responseService", logicalDeployment);
		responseMomService.setDestinationCapacity(500);
		responseMomService.setDestinationName("response");
		logicalClientExecNode.addLogicalServiceUsage(responseMomService, LogicalServiceAccessTypeEnum.READ_ONLY);
		logicalServerExecNode.addLogicalServiceUsage(responseMomService, LogicalServiceAccessTypeEnum.WRITE_ONLY);
		
		return logicalDeployment;
}


	public static MavenReference createMavenMomClientReference() throws MalformedURLException {
		URL accessUrl = new URL("http://10.170.232.172/mom-client.ear");
		return createMavenMomClientReference(accessUrl);
	}


	public static MavenReference createMavenMomServerReference() throws MalformedURLException {
		URL accessUrl = new URL("http://10.170.232.172/mom-server.ear");
		return createMavenMomServerReference(accessUrl);
	}

	public static MavenReference createMavenMomClientReference(URL accessUrl) {
		MavenReference mavenMomClientRef = new MavenReference("com.francetelecom.clara.cloud.mom", "mom-client", "1.0.0-SNAPSHOT", "ear");
		mavenMomClientRef.setAccessUrl(accessUrl);
		return mavenMomClientRef;
	}
	
	public static MavenReference createMavenMomServerReference(URL accessUrl) {
		MavenReference mavenMomServerRef = new MavenReference("com.francetelecom.clara.cloud.mom", "mom-server", "1.0.0-SNAPSHOT", "ear");
		mavenMomServerRef.setAccessUrl(accessUrl);
		return mavenMomServerRef;
	}
}
