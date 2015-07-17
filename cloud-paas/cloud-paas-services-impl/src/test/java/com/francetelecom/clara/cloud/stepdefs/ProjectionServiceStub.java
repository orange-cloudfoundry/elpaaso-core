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
package com.francetelecom.clara.cloud.stepdefs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.ConfigRoleHelper;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.model.validators.ModelItemGenericValidationUtils;
import com.francetelecom.clara.cloud.paas.projection.ProjectionService;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.stepdefs.ConfigureEnvStepDefs.ResultProperty;

public class ProjectionServiceStub implements ProjectionService {

	List<ResultProperty> environmentProperties = new ArrayList<>();
	String exceptionMessage;

	@Override
	public TechnicalDeploymentTemplate generateNewDeploymentTemplate(ApplicationRelease applicationRelease, DeploymentProfileEnum profile)
	        throws UnsupportedProjectionException {
		String logicalNameWithId = applicationRelease.getUID() + "-" + System.nanoTime();
		TechnicalDeployment technicalDeployment = new TechnicalDeployment("td-" + logicalNameWithId);
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(technicalDeployment, profile, applicationRelease.getUID(),
		        applicationRelease.getMiddlewareProfileVersion());
		ModelItemGenericValidationUtils.validateModel(tdt, LoggerFactory.getLogger("test"));
		return tdt;
	}

	@Override
	public List<MiddlewareProfile> findAllMiddlewareProfil() {
		return Arrays.asList(MiddlewareProfile.values());
	}

	@Override
	public void updateDeploymentTemplateInstance(TechnicalDeploymentInstance tdi, ApplicationRelease applicationRelease, List<ConfigRole> configRoles) {
		LogicalDeployment logicalDeployment = applicationRelease.getLogicalDeployment();
		List<ProcessingNode> executionNodes = logicalDeployment.listProcessingNodes();
		for (ProcessingNode jeeProcessing : executionNodes) {

			// processingNode.listLogicalServices(filteredClass)

			Properties mergedConfigServicesProperties = null;
			try {
				mergedConfigServicesProperties = ConfigRoleHelper.getMergedConfigServicesProperties(jeeProcessing, configRoles);
				for (String key : mergedConfigServicesProperties.stringPropertyNames()) {
					environmentProperties.add(new ResultProperty("", key, mergedConfigServicesProperties.getProperty(key), "", jeeProcessing.getLabel(), false));
				}
			} catch (InvalidConfigServiceException e) {
				exceptionMessage = e.getMessage();
			}
		}

	}
}
