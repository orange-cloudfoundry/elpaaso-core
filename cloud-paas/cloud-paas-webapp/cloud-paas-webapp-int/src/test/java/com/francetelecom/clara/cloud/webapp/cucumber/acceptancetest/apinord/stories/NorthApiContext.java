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
package com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.apinord.stories;

import com.francetelecom.clara.cloud.webapp.ElpaasoEndpointContext;
import com.francetelecom.clara.cloud.webapp.acceptancetest.utils.Utils;
import com.orange.clara.cloud.consumersoap.administration.service.PaasAdministrationService;
import com.orange.clara.cloud.consumersoap.environment.service.PaasEnvironmentService;
import com.orange.clara.cloud.consumersoap.incubator.service.PaasIncubatorService;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

/**
 * Created by wooj7232 on 09/03/2015.
 */
@Configuration
@PropertySource(value = "classpath:com/francetelecom/clara/cloud/webapp/elpaaso.properties")
@Import(ElpaasoEndpointContext.class)
public class NorthApiContext {

    @Bean
    public Utils disableSslCertificateCheck(){
        Utils.disableSslCertificateCheck();
        return new Utils();
    }

    @Autowired
    @Qualifier("elpaasoBaseEndpoint")
    String elpaasoBaseEndpoint;

    @Bean
    public PaasAdministrationService getPaasAdministrationServiceProxy(){
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(PaasAdministrationService.class);
        factory.setAddress(elpaasoBaseEndpoint +"/api/soap/administration/v4");
        return factory.create(PaasAdministrationService.class);
    }

    @Bean
    public PaasIncubatorService getPaasIncubatorServiceProxy(){
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(PaasIncubatorService.class);
        factory.setAddress(elpaasoBaseEndpoint +"/api/soap/incubator/v4");
        return factory.create(PaasIncubatorService.class);
    }

    @Bean
    public PaasEnvironmentService getPaasEnvironmentServiceProxy(){
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(PaasEnvironmentService.class);
        factory.setAddress(elpaasoBaseEndpoint +"/api/soap/environment/v3");
        return factory.create(PaasEnvironmentService.class);
    }
}
