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
import com.francetelecom.clara.cloud.logicalmodel.JeeProcessing;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

public class JeeProcessingProjectionRule extends BaseProcessingNodeProjectionRule implements ProcessingNodeProjectionRule {

    @Value("${cf.jonasBuildPack}")
    private String buildPack;

    public JeeProcessingProjectionRule() {
    }

    public JeeProcessingProjectionRule(String buildPack) {
        this.buildPack = buildPack;
    }

    @Override
    public void apply(LogicalDeployment ld, TechnicalDeployment td, ProjectionContext projectionContext) {
        List<JeeProcessing> jeeProcessings = ld.listProcessingNodes(JeeProcessing.class);
        for (JeeProcessing jeeProcessing : jeeProcessings) {
            try {
                td.add(toApp(projectionContext.getSpace(), projectionContext, jeeProcessing, buildPack));
            } catch (UnsupportedProjectionException e) {
                throw new TechnicalException(e);
            }
        }

    }

    protected App toApp(Space space, ProjectionContext projectionContext, ProcessingNode processingNode) {
        return super.toApp(space, projectionContext, processingNode, buildPack);
    }
}
