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
package com.francetelecom.clara.cloud.paas.activation;

import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import org.fest.assertions.Assertions;
import org.junit.Test;

public class ActivationContextHandlerImplTest {

    @Test
    public void activation_context_should_contain_env_label() throws Exception {

        final EnvironmentDescriptionHandlerImpl environmentDescriptionHandler = new EnvironmentDescriptionHandlerImpl();
        TechnicalDeployment td = new TechnicalDeployment("td");
        TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
        ApplicationRelease applicationRelease = new ApplicationRelease(new Application("label", "code"), "version");
        final Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "envlabel", applicationRelease, new PaasUser("herve", "Vilard", new SSOId("Vilard"), "vilard@orange.com"), new TechnicalDeploymentInstance(tdt, td));

        final ActivationContext activationContext = environmentDescriptionHandler.toActivationContext(environment);

        Assertions.assertThat(activationContext.getEnvLabel()).isEqualTo("envlabel");
    }


    @Test
    public void activation_context_should_contain_env_uid() throws Exception {

        final EnvironmentDescriptionHandlerImpl environmentDescriptionHandler = new EnvironmentDescriptionHandlerImpl();
        TechnicalDeployment td = new TechnicalDeployment("td");
        TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
        ApplicationRelease applicationRelease = new ApplicationRelease(new Application("label", "code"), "version");
        final Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "envlabel", applicationRelease, new PaasUser("herve", "Vilard", new SSOId("Vilard"), "vilard@orange.com"), new TechnicalDeploymentInstance(tdt, td));

        final ActivationContext activationContext = environmentDescriptionHandler.toActivationContext(environment);

        Assertions.assertThat(activationContext.getEnvUID()).isEqualTo(environment.getUID());
    }
}