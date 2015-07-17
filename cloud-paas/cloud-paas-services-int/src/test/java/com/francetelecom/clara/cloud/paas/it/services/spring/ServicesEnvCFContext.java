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
package com.francetelecom.clara.cloud.paas.it.services.spring;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.JeeProbeLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.paas.it.services.helper.PaasServicesEnvITConfiguration;
import com.francetelecom.clara.cloud.paas.it.services.helper.PaasServicesEnvITHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by wooj7232 on 26/01/2015.
 */
@Configuration
@ImportResource(value = {"classpath:com/francetelecom/clara/cloud/paas/it/services/helper/PaasServicesEnv-context.xml",
        "classpath:com/francetelecom/clara/cloud/paas/it/services/helper/PaasServicesEnv-context-persistence.xml",
        "classpath:com/francetelecom/clara/cloud/paas/it/services/helper/PaasServicesEnv-context-properties.xml",
        "classpath:com/francetelecom/clara/cloud/paas/it/services/helper/PaasServicesEnv-Plugins-CF-context.xml"})
public class ServicesEnvCFContext {


    @Value("${internet.access.proxyHost}")
    String httpProxyHost;
    @Value("${internet.access.proxyPort}")
    private int httpProxyPort;

    @Value("${test.it.user.email}")
    private String testUserEmail;

    @Autowired
    private ManagePaasUser managePaasUser;
    @Autowired
    private ManageApplication manageApplication;
    @Autowired
    private ManageApplicationRelease manageApplicationRelease;
    @Autowired
    private ManageLogicalDeployment manageLogicalDeployment;
    @Autowired
    private ManageEnvironment manageEnvironment;
    @Autowired
    private SampleAppFactory logicalModelCatalog;

    @Bean(name = "logicalModelCatalog")
    public SampleAppFactory getLogicalModelCatalog() {
        return new JeeProbeLogicalModelCatalog();
    }

    @Bean(name = "expectedJavaVersion")
    public String getExpectedJavaVersion() {
        return "1.7";
    }

    @Bean(name = "itConfiguration")
    public PaasServicesEnvITConfiguration getPaasServicesEnvITConfiguration() {
        PaasServicesEnvITConfiguration itConfiguration = new PaasServicesEnvITConfiguration();
        itConfiguration.setManagePaasUser(managePaasUser);
        itConfiguration.setManageApplication(manageApplication);
        itConfiguration.setManageApplicationRelease(manageApplicationRelease);
        itConfiguration.setManageLogicalDeployment(manageLogicalDeployment);
        itConfiguration.setManageEnvironment(manageEnvironment);
                itConfiguration.setUseSshTunnel(false);
        itConfiguration.setUseHttpIgeProxy(false);
                itConfiguration.setHttpProxyHost(httpProxyHost);
                itConfiguration.setHttpProxyPort(httpProxyPort);

        itConfiguration.setLogicalModelCatalog(logicalModelCatalog);
                itConfiguration.setTestUserEmail(testUserEmail);
                itConfiguration.setName("cf_it");

        return itConfiguration;
    }


    @Bean(name = "paasServicesEnvITHelper")
    public PaasServicesEnvITHelper getPaasServicesEnvITHelper() {
        PaasServicesEnvITHelper paasServicesEnvITHelper = new PaasServicesEnvITHelper(getPaasServicesEnvITConfiguration());

    /*
   pending debug: seems we initially get 404 for a small time, maybe while jonas is starting
           for now that the profile is experimental retry 10 times with 5 seconds wait time*/
        paasServicesEnvITHelper.setWebAppTestAttempts(10);
        paasServicesEnvITHelper.setWebAppTestWaitTime(5);

        return paasServicesEnvITHelper;
    }
}
