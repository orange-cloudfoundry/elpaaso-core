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
package com.francetelecom.clara.cloud.webapp;

import com.francetelecom.clara.cloud.webapp.acceptancetest.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Created by wooj7232 on 09/03/2015.
 */
@Configuration
@PropertySource(value = "classpath:com/francetelecom/clara/cloud/webapp/elpaaso.properties")
public class ElpaasoEndpointContext {

    @Autowired
    private Environment environment;

    @Bean(name = "elpaasoBaseEndpoint")
    public String getEndpointPrefixAddress(){
        String host =environment.getProperty("paas.instance.host","hostShouldBeSetByFailsafePluginThroughResourcePlugin");
        String port =environment.getProperty("paas.instance.port","9999");
        String earContext = environment.getProperty("ear.context","");

        return host+":"+port+earContext;
    }

//    @Bean
    public Utils disableSslCertificateCheck(){
        Utils.disableSslCertificateCheck();
        return new Utils();
    }

}
