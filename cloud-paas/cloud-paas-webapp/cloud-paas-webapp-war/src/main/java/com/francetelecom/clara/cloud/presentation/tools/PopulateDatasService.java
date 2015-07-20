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
package com.francetelecom.clara.cloud.presentation.tools;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.validation.ConstraintViolationException;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;

/**
 * Created by IntelliJ IDEA. User: Thomas Escalle - tawe8231 Entity :
 * FT/OLNC/RD/MAPS/MEP/MSE Date: 02/11/11
 * 
 * Allows user to create automatically applications / releases / logical model /
 * environment, invoking "populate page"
 * 
 */

public class PopulateDatasService {

	/**
	 * Logger
	 */
	private static final transient Logger logger = LoggerFactory.getLogger(PopulateDatasService.class);

	/**
	 * @todo BVA/TES dont work on mock version !? Spring automatically injects
	 *       all known beans of type
	 *       {@link com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory}
	 *       . See http://static.springsource.org/spring/docs/current/spring-
	 *       framework-reference/html/beans.html#beans-annotation-config
	 * @todo : WARN "Autowired" notation is only for test, and must be replaced
	 *       by context bean definition
	 */
	@Autowired
	Map<String, SampleAppFactory> sampleAppsCatalog;

	private ManageApplication manageApplication;

	private ManageApplicationRelease manageApplicationRelease;

	private ManageLogicalDeployment manageLogicalDeployment;

	private ManageEnvironment manageEnvironment;

	PaasUser user = null;

	// needed by spring
	public PopulateDatasService() {
		logger.debug("PopulateDatasService()");
	}

	private SampleAppFactory getAppFactory(String appName) {
		for (Map.Entry<String, SampleAppFactory> entry : sampleAppsCatalog.entrySet()) {
			String beanName = entry.getKey();
			SampleAppFactory appFactory = entry.getValue();
			if (beanName.startsWith(appName)) {
				return appFactory;
			}
		}

		return null;
	}

	private Application createApp(String beanName, SampleAppFactory appFactory) throws BusinessException, MalformedURLException {

		// create app
		String appLabel = appFactory.getAppLabel();
		String appCode = appFactory.getAppCode();
		String appDescription = appFactory.getAppDescription();
		String appVersionControl = appFactory.getApplicationVersionControl();

		if (appLabel == null) {
			appLabel = beanName.substring(0, beanName.indexOf("LogicalModelCatalog"));
		}
		if (appCode == null) {
			appCode = appLabel.substring(0, 4) + 1;
		}
		if (appDescription == null) {
			appDescription = "Sample app description for " + appLabel;
		}
		if (appVersionControl == null) {
			appVersionControl = "http://default.version.control.url";
		}

		String applicationUID;
        try {
            applicationUID = manageApplication.createPublicApplication(appCode, appLabel, appDescription, new URL(appVersionControl), WicketSession.get().getPaasUser().getSsoId());
            return manageApplication.findApplicationByUID(applicationUID);
        } catch (DataIntegrityViolationException dataException) {
            logger.error(dataException.getMessage());
            throw new BusinessException(dataException);
        } catch (BusinessException be) {
            throw be;
        } catch (Exception e) {
            Throwable rootCauseException = ExceptionUtils.getRootCause(e);
            if (rootCauseException instanceof ConstraintViolationException) {
                logger.error(rootCauseException.getMessage());
                throw new BusinessException(rootCauseException);
            }
            logger.error(e.getMessage());
            throw new TechnicalException(e);
        }
	}

	private ApplicationRelease createRelease(Application app, SampleAppFactory appFactory, String version) throws MalformedURLException,
			DuplicateApplicationReleaseException, ObjectNotFoundException {
		ApplicationRelease release;

		// create app
        String releaseDescription = appFactory.getAppReleaseDescription();
		String releaseVersionControl = appFactory.getApplicationReleaseVersionControl();

		if (releaseDescription == null) {
			releaseDescription = app.getDescription() + " and it's release " + version;
		}
		if (releaseVersionControl == null) {
			releaseVersionControl = "http://default.version.control.url";
		}

		// Creates and persists release
		String releaseUid = manageApplicationRelease.createApplicationRelease(app.getUID(), WicketSession.get().getPaasUser().getSsoId().getValue(), version);
		// Find the created release
		release = manageApplicationRelease.findApplicationReleaseByUID(releaseUid);
		// Set other properties and save change
		release.setDescription(releaseDescription);
		release.setVersionControlUrl(new URL(releaseVersionControl));

		// release.addPaasUser(WicketSession.get().getPaasUser(),
		// PaasRoleEnum.RELEASE_MANAGER);
		manageApplicationRelease.updateApplicationRelease(release);
		return release;
	}

	private void populateLogicalDeployment(SampleAppFactory appFactory, ApplicationRelease release) throws BusinessException {

		LogicalDeployment logicalDeploymentToUpdate = manageLogicalDeployment.findLogicalDeployment(release.getLogicalDeployment().getId());
		LogicalDeployment ld = appFactory.populateLogicalDeployment(logicalDeploymentToUpdate);
		manageLogicalDeployment.checkOverallConsistencyAndUpdateLogicalDeployment(ld);
	}

	public List<PopulateApplicationInformation> getApplicationsList() {
		List<PopulateApplicationInformation> appList = new ArrayList<PopulateApplicationInformation>();

		for (Map.Entry<String, SampleAppFactory> entry : sampleAppsCatalog.entrySet()) {
			String beanName = entry.getKey();
			if ((entry.getValue()).isInstantiable()) {
				PopulateApplicationInformation pai = new PopulateApplicationInformation(beanName, "1", "0");
				appList.add(pai);
			}
		}
		return appList;
	}

	public void setSampleAppsCatalog(Map<String, SampleAppFactory> sampleAppsCatalog) {
		logger.debug("  setSampleAppsCatalog");
		this.sampleAppsCatalog = sampleAppsCatalog;
	}

	public void setManageApplication(ManageApplication manageApplication) {
		this.manageApplication = manageApplication;
	}

	public void setManageApplicationRelease(ManageApplicationRelease manageApplicationRelease) {
		this.manageApplicationRelease = manageApplicationRelease;
	}

	public void setManageLogicalDeployment(ManageLogicalDeployment manageLogicalDeployment) {
		this.manageLogicalDeployment = manageLogicalDeployment;
	}

	public void setManageEnvironment(ManageEnvironment manageEnvironment) {
		this.manageEnvironment = manageEnvironment;
	}

	public void setUser(PaasUser user) {
		this.user = user;
	}

	public void populateSingleApp(String appName, String nbOfReleases, String nbOfEnvironments, boolean mock) throws MalformedURLException, BusinessException {
		SampleAppFactory appFactory = getAppFactory(appName);
		String version;

		// Create application
		Application app = createApp(appName, appFactory);

		for (int i = 1; i <= Integer.parseInt(nbOfReleases); i++) {
			version = "G00R0" + i;

			// create release
			ApplicationRelease release = createRelease(app, appFactory, version);

			// Populate logical model.
			populateLogicalDeployment(appFactory, release);

			for (int j = 1; j <= Integer.parseInt(nbOfEnvironments); j++) {

				List<EnvironmentDto.EnvironmentTypeEnum> envTypeList = new ArrayList<EnvironmentDto.EnvironmentTypeEnum>();

                Collections.addAll(envTypeList, EnvironmentDto.EnvironmentTypeEnum.values());

				Random random = new Random();

				EnvironmentDto.EnvironmentTypeEnum envType;

				// if mock mode activate we can select a random type of
				// environment
				if (mock)
					envType = envTypeList.get(random.nextInt(envTypeList.size()));
				// if mock mode deactivate we can only create development
				// environment
				else
					envType = EnvironmentDto.EnvironmentTypeEnum.DEVELOPMENT;

				manageEnvironment.isEnvironmentLabelUniqueForRelease(WicketSession.get().getPaasUser().getSsoId().getValue(), "my env " + j, release.getUID());

				manageEnvironment.createEnvironment(release.getUID(), envType, WicketSession.get().getPaasUser().getSsoId().getValue(), "my env " + j);
			}
		}
	}
}

class PopulateApplicationInformation implements Serializable {

	private static final long serialVersionUID = -6378722506474391605L;

	String appName;
	String nbOfReleases;
	String nbOfEnvironments;

	PopulateApplicationInformation(String appName, String nbOfReleases, String nbOfEnvironments) {
		this.appName = appName;
		this.nbOfReleases = nbOfReleases;
		this.nbOfEnvironments = nbOfEnvironments;
	}

	public String getAppName() {
		return appName;
	}

	public String getNbOfReleases() {
		return nbOfReleases;
	}

	public String getNbOfEnvironments() {
		return nbOfEnvironments;
	}

}
