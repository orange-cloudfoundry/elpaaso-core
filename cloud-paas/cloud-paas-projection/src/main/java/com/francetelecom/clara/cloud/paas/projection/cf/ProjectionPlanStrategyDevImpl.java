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

import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.paas.constraint.ProjectionPlan;

public class ProjectionPlanStrategyDevImpl implements ProjectionPlanStrategy {

    private int defaultMinMemory = 256;

    public void setDefaultMinMemory(int defaultMinMemory) {
        this.defaultMinMemory = defaultMinMemory;
    }

    @Override
	public ProjectionPlan getApplicationServerProjectionPlan(ProcessingNode node,DeploymentProfileEnum profile) {
		//In a development environment, always keep a single Jonas instance per execution node.
        ProjectionPlan projectionPlan = new ProjectionPlan();
        projectionPlan.setVmNumber(1);
        projectionPlan.setWasPerVm(1);
        // For dev environment, use 256M or min memory requested by user if it is greater
		// note: we don't use memoryPerSessionHint, considering it should be negligible for 1 session
        int userMemoryMbPerWas = node.getMinMemoryMbHint();
        projectionPlan.setMemoryMbPerWas(Math.max(defaultMinMemory, userMemoryMbPerWas));
        return projectionPlan;
	}
}
