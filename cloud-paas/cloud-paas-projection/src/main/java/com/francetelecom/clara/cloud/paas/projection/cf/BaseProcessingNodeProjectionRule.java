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
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.paas.constraint.ProjectionPlan;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseProcessingNodeProjectionRule {

    private static Logger logger = LoggerFactory.getLogger(JeeProcessingProjectionRule.class.getName());

    private ProjectionPlanStrategy projectionPlanStrategy;

    private MvnRepoDao mvnDao;

    protected App toApp(Space space, TechnicalDeployment td, ProjectionContext projectionContext, ProcessingNode processingNode, String buildPack) {
        // to fail early, however the actual URL will be resolved at activation
        // time
        assertMavenReferenceIsAvailable(processingNode);

        ProjectionPlan projectionPlan = this.projectionPlanStrategy.getApplicationServerProjectionPlan(processingNode, projectionContext.getDeploymentProfile());
        int instanceCount = projectionPlan.getInstanceCount();
        //TODO move to projectionPlan ?
        int ramMb = getMemory(projectionPlan.getMemoryMbPerWas());

        App app = new App(td, space, processingNode.getLabel(), processingNode.getSoftwareReference(), buildPack, ramMb, instanceCount);
        
        //TODO: should externalize if cloudfoundry stacks change often and should be managed by middleware profile
        app.setStack("cflinuxfs2");
        app.setDiskSizeMb(processingNode.getMinDiskMbHint());
        app.setLogicalModelId(processingNode.getName());
        app.setOptionalApplicationBinaries(processingNode.isOptionalSoftwareReference());
        return app;
    }

    protected void assertMavenReferenceIsAvailable(ProcessingNode processingNode) throws UnsupportedProjectionException {
        // Resolve URL to be sure that the reference is correct (unless artifact
        // is marked optional)
        if (processingNode.isOptionalSoftwareReference()) {
            logger.info("skip artifact reference validation (marked optional) for " + processingNode.toString());
            return;
        }
        mvnDao.resolveUrl(processingNode.getSoftwareReference());
    }

    protected int getMemory(int ramInMb) throws UnsupportedProjectionException {
        if (ramInMb <= 128) {
            return 128;
        } else if (ramInMb <= 256) {
            return 256;
        } else if (ramInMb <= 512) {
            return 512;
        } else if (ramInMb <= 1024) {
            return 1024;
        } else if (ramInMb <= 2048) {
            return 2048; // May need to tune this w.r.t. to CF instance
        } else {
            throw new UnsupportedProjectionException("invalid projection plan proposing more than 2048 RAM", false);
        }
    }

    public void setProjectionPlanStrategy(ProjectionPlanStrategy projectionPlanStrategy) {
        this.projectionPlanStrategy = projectionPlanStrategy;
    }

    public void setMvnDao(MvnRepoDao mvnDao) {
        this.mvnDao = mvnDao;
    }

}
