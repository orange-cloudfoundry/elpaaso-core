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
package com.francetelecom.clara.cloud.spring;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.paas.activation.v1.ActivationPluginStrategy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.web.context.support.StandardServletEnvironment;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.*;

public class PluginConfigurationIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigurationIT.class);

    private static final Set<Class<?>> ignoredClasses = new HashSet<Class<?>>();

    static {
        ignoredClasses.add(TechnicalDeploymentTemplate.class);
        //Deactivated subscriptions
    }

    /*
     * Initialize a JNDI repository with all props from propertiesFile
     */
    private Properties initJndiValues(String propertiesFile) throws NamingException, IOException {
        InputStream propsStream = this.getClass().getClassLoader().getResourceAsStream(propertiesFile);
        assertNotNull("no property file found for " + propertiesFile, propsStream);
        Properties props = new Properties();
        props.load(propsStream);

        SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        InitialContext initialContext = new InitialContext();
        for (Object key : props.keySet()) {
            initialContext.bind((String) key, props.get(key));
        }
        return props;
    }


    @Test
    public void should_not_found_any_dependant_model_item_without_active_plugin_in_production() throws Exception {
        String datacenter = System.getProperty("datacenter", "hudson");
        initJndiValues("com/francetelecom/clara/cloud/commons/testconfigurations/credentials-" + datacenter + ".properties");

        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext(new String[]{
                "classpath:/spring-config/application-context-plugins.xml",
                "classpath:/spring-config/datasources-hsqldb-context.xml",
                "classpath:/com/francetelecom/clara/cloud/commons/testconfigurations/mock-liquibase-context.xml",
                "classpath:/META-INF/spring/paas-activation-stubbed-context.xml", "classpath:/spring-config/mock-spring-authentication-manager-context.xml"}) {
            @Override
            protected ConfigurableEnvironment createEnvironment() {
                return new StandardServletEnvironment();
            }
        };

        ActivationPluginStrategy strategy = (ActivationPluginStrategy) context.getBean("pluginStrategy");

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(DependantModelItem.class));

        Set<BeanDefinition> findCandidateComponents = provider.findCandidateComponents("com/francetelecom/clara/cloud/model");
        ActivationStepEnum[] steps = ActivationStepEnum.values();

        Set<Class<?>> errorItems = new HashSet<Class<?>>();

        for (BeanDefinition beanDefinition : findCandidateComponents) {
            ActivationPlugin foundPlugin = null;
            Class<?> testedClass = Class.forName(beanDefinition.getBeanClassName());
            LOGGER.info("Lookin for a plugin handling {}", testedClass.getSimpleName());
            for (ActivationStepEnum activationStepEnum : steps) {
                if (foundPlugin == null) {
                    try {
                        foundPlugin = strategy.getPlugin(testedClass, activationStepEnum);
                    } catch (TechnicalException e) {
                        //Ignore not found error
                    }
                }
            }

            if (foundPlugin == null) {
                if (ignoredClasses.contains(testedClass)) {
                    LOGGER.info("No plugin found for {} but ignored !", testedClass.getSimpleName());
                } else {
                    LOGGER.info("No plugin found for {}", testedClass.getSimpleName());
                }
                errorItems.add(testedClass);
            } else {
                assertFalse(testedClass.getSimpleName() + " is ignored but a plugin has been found : " + foundPlugin.getClass().getSimpleName() + ".\nDid you activate new feature without refactoring this test specs ?", ignoredClasses.contains(testedClass));
                LOGGER.info("Plugin found for {} : {}", testedClass.getSimpleName(), foundPlugin.getClass().getSimpleName());
            }
        }

        errorItems.removeAll(ignoredClasses);

        assertTrue("No plugin found for some items " + errorItems.toString() + ".\nDid you miss some configuration in cloud-paas-webapp spring config?", errorItems.isEmpty());
    }

}
