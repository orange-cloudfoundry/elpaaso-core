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

import static com.francetelecom.clara.cloud.deployment.result.CriticityEnum.WARNING;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.deployment.result.RuleValidationMessage;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.services.dto.DeployableMavenArtifactDto;

@Component
public class CheckMavenReferenceRule implements CheckRule {

	private static Logger logger = LoggerFactory.getLogger(CheckMavenReferenceRule.class);

    @Override
    public Set<RuleValidationMessage> validate(DeployableMavenArtifactDto providedArtifact, ProcessingNode node) throws TechnicalException {
        Set<RuleValidationMessage> messages = new HashSet<RuleValidationMessage>();
		MavenReference expectedMavenReference = node.getSoftwareReference();

		logger.debug("Checking GAV");
		
		if (!StringUtils.equals(expectedMavenReference.getGroupId(), providedArtifact.getGroupId())) {
			messages.add(new RuleValidationMessage("Deployment of [" + providedArtifact.getArtifactId()
					+ "] not allowed : provided groupId [" + providedArtifact.getGroupId() + "] does not match expected groupId [" + expectedMavenReference.getGroupId() + "]",
					WARNING));
		}
		if (!StringUtils.equals(expectedMavenReference.getArtifactId(), providedArtifact.getArtifactId())) {
			messages.add(new RuleValidationMessage("Deployment of " + providedArtifact.getArtifactId()
					+ " not allowed : provided artifactId [" + providedArtifact.getArtifactId() + "] does not match expected artifactId [" + expectedMavenReference.getArtifactId() + "]",
					WARNING));
		}
		// Classifier null or empty ("") should be considered equals
		if ((StringUtils.isNotEmpty(expectedMavenReference.getClassifier()) || StringUtils.isNotEmpty(providedArtifact.getClassifier()))
				&& !StringUtils.equals(expectedMavenReference.getClassifier(), providedArtifact.getClassifier())) {
			messages.add(new RuleValidationMessage("Deployment of [" + providedArtifact.getArtifactId()
					+ "] not allowed : provided classifier [" + providedArtifact.getClassifier() + "] does not match expected classifier [" + expectedMavenReference.getClassifier()+ "]",
					WARNING));
		}
		if (!StringUtils.equals(expectedMavenReference.getType(), providedArtifact.getType())) {
			messages.add(new RuleValidationMessage("Deployment of [" + providedArtifact.getArtifactId()
					+ "] not allowed : provided type [" + providedArtifact.getType() + "] does not match expected type [" + expectedMavenReference.getType() + "]",
					WARNING));
		}
		if (!StringUtils.startsWith(providedArtifact.getVersion(), expectedMavenReference.getVersion())) {
			messages.add(new RuleValidationMessage("Deployment of [" + providedArtifact.getArtifactId()
					+ "] not allowed : provided version [" + providedArtifact.getVersion() + "] does not match expected version [" + expectedMavenReference.getVersion() + "]",
					WARNING));
		}
        
        return messages;
    }

}
