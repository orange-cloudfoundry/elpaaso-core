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

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;

public class CfProjectionServiceImplTest {

    @Mock
    ConfigOverrideProjectionRule configOverrideProjectionRule;
    @Mock
    TechnicalDeploymentInstance tdi;
    @Mock
    ApplicationRelease applicationRelease;

    CfProjectionServiceImpl cfProjectionService = new CfProjectionServiceImpl();


    @Test(expected = IllegalArgumentException.class)
    public void updateDeploymentTemplateInstance_rejects_null_role_list() throws Exception {
        cfProjectionService.updateDeploymentTemplateInstance(tdi, applicationRelease, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateDeploymentTemplateInstance_rejects_empty_role_list() throws Exception {
        cfProjectionService.updateDeploymentTemplateInstance(tdi, applicationRelease, new ArrayList<ConfigRole>());
    }
}