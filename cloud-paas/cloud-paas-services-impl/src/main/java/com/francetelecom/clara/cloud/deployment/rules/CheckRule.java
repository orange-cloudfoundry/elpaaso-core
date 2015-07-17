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
package com.francetelecom.clara.cloud.deployment.rules;

import java.util.Set;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.deployment.result.RuleValidationMessage;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.services.dto.DeployableMavenArtifactDto;

/**
 * Interface for artifact validation before deployment
 */
public interface CheckRule {

	/**
	 * Validate an artifact against an execution node
	 * @param providedArtifact
	 * @param node
	 * @return
	 * @throws Exception
	 */
    Set<RuleValidationMessage> validate(DeployableMavenArtifactDto providedArtifact, ProcessingNode node) throws TechnicalException;
}
