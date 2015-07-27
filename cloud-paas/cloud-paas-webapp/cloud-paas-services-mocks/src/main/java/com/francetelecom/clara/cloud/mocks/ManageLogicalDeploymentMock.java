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
package com.francetelecom.clara.cloud.mocks;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException.ErrorType;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.service.exception.InvalidReleaseException;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.deployment.logical.service.LogicalDeploymentCloner;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Mock impl of LogicalDeployment using in plain JVM references as persistence
 */
@Service("manageLogicalDeployment")
public class ManageLogicalDeploymentMock extends LogicalModelServiceMock<LogicalDeployment> implements ManageLogicalDeployment {

	@Autowired
	private ManageApplicationReleaseMock manageApplicationReleaseMock;

	@Autowired
	private LogicalDeploymentCloner cloner;

	// FIXME: transient hack for working around design issue of name vs db id vs
	// labels
	private int cloneIndex;

	private static final LogicalConfigServiceUtils LOGICAL_CONFIG_SERVICE_UTILS = new LogicalConfigServiceUtils();

	public ManageLogicalDeploymentMock() {
	}

	public ManageLogicalDeploymentMock getManageLogicalDeploymentMock() {
		return this;
	}

	@Override
	public void checkLogicalSoapServiceConsistency(LogicalSoapService logSoapService, boolean fullValidation) throws BusinessException {
		// assume always valid
		return;
	}

	@Override
	public void checkOverallConsistency(LogicalDeployment logicalDeployment) throws BusinessException {
		Set<LogicalConfigService> logicalConfigServices = logicalDeployment.listLogicalServices(LogicalConfigService.class);
		for (LogicalConfigService logicalConfigService : logicalConfigServices) {
			LOGICAL_CONFIG_SERVICE_UTILS.parseConfigContent(logicalConfigService.getConfigSetContent());
		}
		logicalDeployment.checkOverallConsistency();
	}

	@Override
	public LogicalDeployment updateLogicalDeployment(LogicalDeployment logicalDeployment) throws ObjectNotFoundException {

		List<ProcessingNode> jeeProcessings = logicalDeployment.listProcessingNodes();
		for (ProcessingNode jeeProcessing : jeeProcessings) {
			resolveMavenURL(jeeProcessing);
		}


		return update(logicalDeployment);
	}

	@Override
	public LogicalDeployment findLogicalDeployment(int i) throws ObjectNotFoundException {
		return find(i);
	}

	@Override
	public void resolveMavenURL(ProcessingNode jeeProcessing) {
		MavenReference mavenRef = jeeProcessing.getSoftwareReference();
		if (mavenRef.getAccessUrl() == null) {
			try {
				URL url = new URL("http://maven.mock.url");
				mavenRef.setAccessUrl(url);
			} catch (MalformedURLException e) {
				e.printStackTrace(); // To change body of catch statement use
										// File | Settings | File Templates.
			}
		}
	}


	@Override
	public List<String> getQrsApplicationVersions(String domain, String appName) {
		List<String> lstApplicationVersions = new ArrayList<String>();

		if ("ADV".equalsIgnoreCase(appName)) {
			lstApplicationVersions.add("G1R0C0");
			lstApplicationVersions.add("G2R0C0");
			lstApplicationVersions.add("G3R0C0");
		} else if ("IODA".equalsIgnoreCase(appName) || "SOFT".equalsIgnoreCase(appName)) {
			lstApplicationVersions.add("G1R0C0");
			lstApplicationVersions.add("G2R0C0");
		} else if ("PtpmFrontOffice".equalsIgnoreCase(appName)) {
			lstApplicationVersions.add("G1R1C0");
		} else if ("PtpmBackOffice".equalsIgnoreCase(appName)) {
			lstApplicationVersions.add("G1R2C0");
		} else if ("SEBA IN PAAS".equalsIgnoreCase(appName) || ("SEBA IN CLOUD".equalsIgnoreCase(appName)) || "SEBA OUT CLOUD".equalsIgnoreCase(appName)) {
			lstApplicationVersions.add("G6R0C0");
		} else if ("Momoo Client".equalsIgnoreCase(appName) || "Momoo Server".equalsIgnoreCase(appName)) {
			lstApplicationVersions.add("G6R0C0");
		}
		return lstApplicationVersions;
	}

	@Override
	public List<String> getQrsApplications(String domain) {
		List<String> lstApplicationName = new ArrayList<String>();
		lstApplicationName.add("ADV");
		lstApplicationName.add("IODA");
		lstApplicationName.add("DLIS");
		lstApplicationName.add("SOFT");
		lstApplicationName.add("PtpmFrontOffice");
		lstApplicationName.add("PtpmBackOffice");
		lstApplicationName.add("SEBA IN PAAS");
		lstApplicationName.add("SEBA IN CLOUD");
		lstApplicationName.add("SEBA OUT CLOUD");
		lstApplicationName.add("Momoo Client");
		lstApplicationName.add("Momoo Server");
		return lstApplicationName;
	}

	@Override
	public List<String> getQrsServices(String domain, String appName, String appVersion) {
		List<String> lstServices = new ArrayList<String>();
		if ("ADV".equalsIgnoreCase(appName)) {
			lstServices.add("getClient");
			lstServices.add("getMISDN");
			lstServices.add("getIMSI");
		} else if ("IODA".equalsIgnoreCase(appName)) {
			lstServices.add("setIMSI");
			lstServices.add("setMISDN");
		} else if ("DLIS".equalsIgnoreCase(appName)) {
			lstServices.add("getDelayLiv");
		} else if ("SOFT".equalsIgnoreCase(appName)) {
			lstServices.add("setDelayLiv");
		} else if ("PtpmFrontOffice".equalsIgnoreCase(appName)) {
			lstServices.add("pong");
		} else if ("PtpmBackOffice".equalsIgnoreCase(appName)) {
			lstServices.add("ping");
		} else if ("SEBA IN PAAS".equalsIgnoreCase(appName) || "SEBA IN CLOUD".equalsIgnoreCase(appName) || "SEBA OUT CLOUD".equalsIgnoreCase(appName)) {
			lstServices.add("subscribeLine");
		} else if ("Momoo Server".equalsIgnoreCase(appName)) {
			lstServices.add("createMarket");
			lstServices.add("updateMarket");
			lstServices.add("deleteMarket");
			lstServices.add("createCatalog");
			lstServices.add("updateCatalog");
			lstServices.add("deleteCatalog");
			lstServices.add("createArticle");
			lstServices.add("updateArticle");
			lstServices.add("deleteArticle");
		} else if ("Momoo Client".equalsIgnoreCase(appName)) {
			lstServices.add("returnMarket");
			lstServices.add("returnCatalog");
			lstServices.add("returnArticle");
		}
		return lstServices;
	}

	@Override
	public List<String> getQrsServicesVersions(String domain, String appName, String appVersion, String serviceName) {
		List<String> lstServiceVersions = new ArrayList<String>();
		if ("getClient".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R0C0");
			lstServiceVersions.add("G2R0C0");
		} else if ("getMISDN".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R0C0");
			lstServiceVersions.add("G2R0C0");
			lstServiceVersions.add("G2R1C0");
		} else if ("getIMSI".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R0C0");
			lstServiceVersions.add("G2R0C0");
		} else if ("setIMSI".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R0C0");
			lstServiceVersions.add("G2R0C0");
		} else if ("setMISDN".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R0C0");
			lstServiceVersions.add("G2R0C0");
		} else if ("getDelayLiv".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R0C0");
		} else if ("setDelayLiv".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R0C0");
		} else if ("ping".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R1C0");
		} else if ("pong".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R1C0");
		} else if ("subscribeLine".equalsIgnoreCase(serviceName)) {
			lstServiceVersions.add("G1R0C0");
		} else {
			lstServiceVersions.add("G1R0C0");
		}
		return lstServiceVersions;
	}

	@Override
	public void cloneLogicalDeployment(String applicationReleaseSourceInternalName, String applicationReleaseTargetInternalName) throws InvalidReleaseException {
		ApplicationRelease sourceAr = null;
		try {
			sourceAr = manageApplicationReleaseMock.findApplicationReleaseByUID(applicationReleaseSourceInternalName);
		} catch (ObjectNotFoundException e) {
			throw new InvalidReleaseException("Invalid applicationReleaseSourceInternalName" + applicationReleaseSourceInternalName, e);
		}
		ApplicationRelease targetAr = null;
		try {
			targetAr = manageApplicationReleaseMock.findApplicationReleaseByUID(applicationReleaseTargetInternalName);
		} catch (ObjectNotFoundException e) {
			throw new InvalidReleaseException("Invalid applicationReleaseTargetInternalName" + applicationReleaseTargetInternalName, e);
		}

		Application sourceApp = sourceAr.getApplication();
		Application targetApp = targetAr.getApplication();
		try {
			if ((manageApplicationReleaseMock.findApplicationReleasesByAppUID(targetAr.getUID()).size() > 1) && !targetApp.getUID().equals(sourceApp.getUID())) {
				throw new InvalidReleaseException("Invalid applicationReleaseTargetInternalName" + applicationReleaseTargetInternalName);
			}
		} catch (ObjectNotFoundException e1) {
			throw new InvalidReleaseException("Invalid applicationReleaseTargetInternalName" + applicationReleaseTargetInternalName);
		}

		if (targetAr.isDiscarded()) {
			throw new InvalidReleaseException("cannot clone logical deployment to a DISCARDED application release");
		}

		LogicalDeployment sourceLd = sourceAr.getLogicalDeployment();
		LogicalDeployment targetLd = targetAr.getLogicalDeployment();

		LogicalDeployment sourceClone = cloner.deepCopy(sourceLd);
		targetAr.replaceLd(sourceClone);

		LogicalDeployment ld = find(targetLd.getName());
		targetAr.getLogicalDeployment().setLabel(ld.getLabel());
		targetAr.getLogicalDeployment().setId(ld.getId());

		try {
			update(targetAr.getLogicalDeployment());
		} catch (ObjectNotFoundException e) {
			throw new InvalidReleaseException();
		}

	}

	@Override
	public URL checkMavenReference(MavenReference mavenReference) throws InvalidMavenReferenceException {
		URL url = null;
		try {
			url = new URL("http://myrepo:8080/" + mavenReference.getGroupId() + "/" + mavenReference.getArtifactId() + "/" + mavenReference.getVersion() + "/"
					+ mavenReference.getArtifactId() + "." + mavenReference.getType());
		} catch (MalformedURLException e) {
			throw new InvalidMavenReferenceException(mavenReference, ErrorType.UNKNOWN);
		}
		return url;
	}

	@Override
	public LogicalDeployment checkOverallConsistencyAndUpdateLogicalDeployment(LogicalDeployment logicalDeployment) throws BusinessException {
		checkOverallConsistency(logicalDeployment);
		return updateLogicalDeployment(logicalDeployment);
	}

}
