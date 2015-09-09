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
package com.francetelecom.clara.cloud.paas.projection.cf;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.xstream.XStreamUtils;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.paas.projection.ProjectionService;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.techmodel.cf.Organization;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class CfProjectionServiceImpl implements ProjectionService {

	private static Logger logger = LoggerFactory.getLogger(CfProjectionServiceImpl.class.getName());
    private Set<ServiceProjectionRule> serviceProjectionRules;

    private Set<ProcessingNodeProjectionRule> processingNodeProjectionRules;

    private Set<AssociationProjectionRule> associationProjectionRules;

    private ConfigOverrideProjectionRule configOverrideProjectionRule;

    @Override
    public TechnicalDeploymentTemplate generateNewDeploymentTemplate(ApplicationRelease applicationRelease, DeploymentProfileEnum profile) throws UnsupportedProjectionException {
        LogicalDeployment logicalDeployment = applicationRelease.getLogicalDeployment();
        if (logicalDeployment.noProcessingNodes()) {
			throw new UnsupportedProjectionException("At least one processing service is required", true);
		}

		TechnicalDeployment td = new TechnicalDeployment(UUID.randomUUID().toString());
		TechnicalDeploymentTemplate technicalDeploymentTemplate = new TechnicalDeploymentTemplate(td, profile, applicationRelease.getUID(),
				applicationRelease.getMiddlewareProfileVersion());

        Organization organization=new Organization();
        td.add(organization);

        // generate space
		Space space = new Space(organization);
        td.add(space);

		String applicationName = applicationRelease.getApplication().getLabel();
		String releaseVersion = applicationRelease.getReleaseVersion();

        final ProjectionContext projectionContext = new ProjectionContext(applicationName, releaseVersion, profile, space);

        for (ProcessingNodeProjectionRule processingNodeProjectionRule : processingNodeProjectionRules) {
            processingNodeProjectionRule.apply(logicalDeployment, td, projectionContext);
        }

        for (ServiceProjectionRule serviceProjectionRule : serviceProjectionRules) {
            serviceProjectionRule.apply(logicalDeployment, td, projectionContext);
        }

        for (AssociationProjectionRule associationProjectionRule : associationProjectionRules) {
            associationProjectionRule.apply(logicalDeployment, td, projectionContext);
        }

		if (logger.isDebugEnabled()) {
			XStream xStream = XStreamUtils.instanciateXstreamForHibernate();
			String xmlDump = xStream.toXML(technicalDeploymentTemplate);
			logger.debug("Resulting tdt for profile {} is: {}", profile, xmlDump);
		}

		return technicalDeploymentTemplate;
	}

	@Override
	public List<MiddlewareProfile> findAllMiddlewareProfil() {
		throw new TechnicalException("only dispatcher impl implements this method that should not have been defined in the interface");
	}

	@Override
	public void updateDeploymentTemplateInstance(TechnicalDeploymentInstance tdi, ApplicationRelease applicationRelease, List<ConfigRole> configRoles) {
        if (configRoles == null || configRoles.isEmpty()) {
            throw new IllegalArgumentException("Expected a valid of config roles to apply, got:" + configRoles);
        } else {
            configOverrideProjectionRule.updateDeploymentTemplateInstance(tdi, applicationRelease, configRoles);
        }
    }

    public void setServiceProjectionRules(Set<ServiceProjectionRule> serviceProjectionRules) {
        this.serviceProjectionRules = serviceProjectionRules;
    }

    public void setProcessingNodeProjectionRules(Set<ProcessingNodeProjectionRule> processingNodeProjectionRules) {
        this.processingNodeProjectionRules = processingNodeProjectionRules;
    }

    public void setAssociationProjectionRules(Set<AssociationProjectionRule> associationProjectionRules) {
        this.associationProjectionRules = associationProjectionRules;
    }

    public ConfigOverrideProjectionRule getConfigOverrideProjectionRule() {
        return configOverrideProjectionRule;
    }

    public void setConfigOverrideProjectionRule(ConfigOverrideProjectionRule configOverrideProjectionRule) {
        this.configOverrideProjectionRule = configOverrideProjectionRule;
    }
}
