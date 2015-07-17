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

import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.paas.constraint.ApplicationCustomizationRule;
import com.francetelecom.clara.cloud.paas.constraint.ProjectionPlan;
import com.francetelecom.clara.cloud.paas.constraint.WebGuiSLO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectionPlanStrategyProdCfImpl implements ProjectionPlanStrategy {

    private static final Logger logger = LoggerFactory.getLogger(ProjectionPlanStrategyProdCfImpl.class);

    private ApplicationCustomizationRule defaultAppCustomizationRule;

    @Override
    public ProjectionPlan getApplicationServerProjectionPlan(ProcessingNode node, DeploymentProfileEnum profile) {

        int totalMaxConcurrentSessions = 0;
        for (LogicalWebGUIService service : node.listLogicalServices(LogicalWebGUIService.class)) {
            if (service.isStateful())
                totalMaxConcurrentSessions += service.getMaxNumberSessions();
        }
        WebGuiSLO webGuiSLO = new WebGuiSLO();
        webGuiSLO.setNumSession(totalMaxConcurrentSessions);

        int minMemoryMbHintForApp = node.getMinMemoryMbHint();
        int minMemoryKbForSessions = totalMaxConcurrentSessions * defaultAppCustomizationRule.getMemoryKoPerSession();

        int minTotalAppMemoryMb = minMemoryMbHintForApp + (minMemoryKbForSessions / 1000);

        // arbitrary favor 512 MB JVM for now
        int nb512Instances = computeNbInstances(minTotalAppMemoryMb);
        ProjectionPlan projectionPlan = new ProjectionPlan();
        projectionPlan.setWasPerVm(1);
        projectionPlan.setVmNumber(nb512Instances);
        projectionPlan.setMemoryMbPerVm(512);
        projectionPlan.setMemoryMbPerWas(512);
        return projectionPlan;
    }

    public int computeNbInstances(int minTotalAppMemoryMb) {
        int nb512Instances;
        if (minTotalAppMemoryMb <= 1024) {
            nb512Instances = 2;
        } else {
            double ratio = (double) minTotalAppMemoryMb / (double) 512;
            nb512Instances = (int) Math.ceil(ratio);
        }
        return nb512Instances;
    }

    /**
     * IOC
     *
     * @param defaultAppCustomizationRule
     */
	public void setDefaultAppCustomizationRule(ApplicationCustomizationRule defaultAppCustomizationRule) {
		this.defaultAppCustomizationRule = defaultAppCustomizationRule;
	}

}
