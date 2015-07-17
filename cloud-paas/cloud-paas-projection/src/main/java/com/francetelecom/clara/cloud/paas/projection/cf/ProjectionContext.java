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

import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.springframework.util.Assert;

/**
 * Created by YSBU7453 on 27/02/2015.
 */
public class ProjectionContext {
    private String applicationName;
    private String releaseVersion;
    private DeploymentProfileEnum deploymentProfile;
    private Space space;


    public ProjectionContext(String applicationName, String releaseVersion, DeploymentProfileEnum deploymentProfile, Space space) {
        Assert.hasText(applicationName, "Unable to set projection context. no application name has been provided.");
        this.applicationName = applicationName;
        Assert.hasText(releaseVersion, "Unable to set projection context. no release version has been provided.");
        this.releaseVersion = releaseVersion;
        Assert.notNull(deploymentProfile, "Unable to set projection context. no deployment profile has been provided.");
        this.deploymentProfile = deploymentProfile;
        Assert.notNull(space, "Unable to set projection context. no space has been provided.");
        this.space = space;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getReleaseVersion() {
        return releaseVersion;
    }

    public DeploymentProfileEnum getDeploymentProfile() {
        return deploymentProfile;
    }

    public Space getSpace() {
        return space;
    }

}
