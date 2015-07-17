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

public interface ProjectionPlanStrategy {

	/**
	 * Encapsulates the decision on the total number of application service
	 * instances to create (one per platform server for now), as well as the
	 * memory size of each WAS
	 * 
	 * @param node
	 *            The ExecutionNode to size (to access its SLO)
	 * @return
	 */
	ProjectionPlan getApplicationServerProjectionPlan(ProcessingNode node,DeploymentProfileEnum profile);

}
