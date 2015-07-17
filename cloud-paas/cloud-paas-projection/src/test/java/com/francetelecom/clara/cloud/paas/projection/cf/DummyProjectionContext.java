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

/**
 * Created by YSBU7453 on 27/02/2015.
 */
public class DummyProjectionContext extends ProjectionContext {

    public static final String APP_NAME = "dummyAppName";
    public static final String APP_RELEASE_VERSION = "G0R0C0";
    public static final DeploymentProfileEnum PROFILE = DeploymentProfileEnum.DEVELOPMENT;


    public DummyProjectionContext(Space space) {
        super(APP_NAME, APP_RELEASE_VERSION, PROFILE, space);
    }
}
