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
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.junit.Test;

public class ProjectionContextTest {

    @Test(expected = IllegalArgumentException.class)
    public void projection_context_requires_application_name_to_be_set() throws Exception {
        new ProjectionContext(null, "release", DeploymentProfileEnum.DEVELOPMENT, new Space(new TechnicalDeployment("")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void projection_context_requires_release_version_to_be_set() throws Exception {
        new ProjectionContext("application", null, DeploymentProfileEnum.DEVELOPMENT, new Space(new TechnicalDeployment("")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void projection_context_requires_deployment_profile_to_be_set() throws Exception {
        new ProjectionContext("application", "release", null, new Space(new TechnicalDeployment("")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void projection_context_requires_space_to_be_set() throws Exception {
        new ProjectionContext("application", "release", DeploymentProfileEnum.DEVELOPMENT, null);
    }
}