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
package com.francetelecom.clara.cloud.paas.it.services;

import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SimpleProbeLogicalModelCatalog;
import com.francetelecom.clara.cloud.paas.it.services.spring.ServicesEnvCFContext;
import org.springframework.context.annotation.*;

@Configuration
@Primary
@Import(ServicesEnvCFContext.class)
public class PaasServicesEnvSimpleProbeITContext {

    @Bean(name="logicalModelCatalog")
    public SampleAppFactory getLogicalModelCatalog(){
        return new SimpleProbeLogicalModelCatalog();
    }

    @Bean(name = "expectedJavaVersion")
    public String getExpectedJavaVersion(){
        return "1.8";
    }

    @Bean(name = "expectedOS")
    public String getExpectedOS(){
        return "Ubuntu 14.04.2 LTS";
    }
}
