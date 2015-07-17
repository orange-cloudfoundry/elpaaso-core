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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Test class in charge of instanciating target spring context for ElPaaso War.
 * 
 * Test contained into this class should fail on several reason, but those reason need to be spring-specific.
 * example :
 * <ul>
 *  <li>a bean refer another inexisting bean</li>
 *  <li>a required property is not found</li>
 * 	<li>a property is not decrypted</li>
 *  <li>more generally : there is a problem in spring configuration
 * </ul>
 * 
 * @author Ludovic Meurillon
 *
 */
public class SpringContextLaunchTest {

	private static final String DEFAULT_PLACEHOLDER_SUFFIX = PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_SUFFIX;
	private static final String DEFAULT_PLACEHOLDER_PREFIX = PropertyPlaceholderConfigurer.DEFAULT_PLACEHOLDER_PREFIX;

	private static final Logger logger = LoggerFactory.getLogger(SpringContextLaunchTest.class);
	public static final String CREDENTIALS_REFERENCE_PROPERTIES = "com/francetelecom/clara/cloud/commons/testconfigurations/credentials-reference.properties";

	/*
	 * Initialize a JNDI repository with all props from propertiesFile
	 */
	private Properties initJndiValues(String propertiesFile) throws NamingException, IOException {
        InputStream propsStream = this.getClass().getClassLoader().getResourceAsStream(propertiesFile);
        assertNotNull("no property file found for "+propertiesFile, propsStream);
        Properties props = new Properties();
        props.load(propsStream);

		SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        InitialContext initialContext = new InitialContext();
        for (Object key : props.keySet()) {
        	initialContext.bind((String)key, props.get(key));
		}
        return props;
	}

	@Test
	public void spring_should_start_replacing_and_decrypting_properties_from_jndi() throws Exception {
		//Set fictive jndi properties (some of them are encoded)
		Properties properties = initJndiValues(CREDENTIALS_REFERENCE_PROPERTIES);

    	final Set<String> encryptedPropertiesKeys = new HashSet<String>();;
        //We keep a trace of encoded properties (those that need to be decrypted via spring)
        for (Object key : properties.keySet()) {
        	if(PropertyValueEncryptionUtils.isEncryptedValue((String)properties.get(key))){
        		encryptedPropertiesKeys.add((String) key);
        	}
		}

        //This multimap will contains all beans-propertyname couples depending on an encrypted property
    	final Multimap<String, String> encryptedBeansProperties = ArrayListMultimap.create();
        PropertyTestApplicationContext context = new PropertyTestApplicationContext("classpath:/spring-config/application-context.xml"){
			@Override
			public void afterPostProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
				collectEncodedBeans(beanFactory,encryptedPropertiesKeys, encryptedBeansProperties);
			}
		};
		//Refresh start the context manually
		context.refresh();
		
		//Verify for all found beans that they do not have encrypted values for their properties
		for (Entry<String, String> entry : encryptedBeansProperties.entries()) {
			logger.info("verifying that {}:{} is correctly decrypted", entry.getKey(),entry.getValue());
			assertIsDecrypted(context, entry.getKey(), entry.getValue());
		}
	}

	/*
	 * Assert that a specific bean property value is not encrypted
	 */
	private void assertIsDecrypted(ClassPathXmlApplicationContext context, String beanName, String propertyName) {
		TypedStringValue value = (TypedStringValue) context.getBeanFactory().getBeanDefinition(beanName).getPropertyValues().getPropertyValue(propertyName).getValue();
		assertFalse(beanName+":"+propertyName+" was not decrypted by spring", PropertyValueEncryptionUtils.isEncryptedValue(value.getValue()));
	}

	/*
	 * Collect all property name from spring beans definitions where the property key is in searchedKeys collection
	 * 
	 * All found beanName-PropertyName couple are stored in the multimap passed in parameter
	 */
	protected void collectEncodedBeans(ConfigurableListableBeanFactory beanFactory, Set<String> searchedKeys, Multimap<String, String> foundBeanProperties) {
		for (String name : beanFactory.getBeanDefinitionNames()) {
			BeanDefinition beanDefinition = beanFactory.getBeanDefinition(name);
			MutablePropertyValues beanProperties = beanDefinition.getPropertyValues();
			for (PropertyValue value : beanProperties.getPropertyValueList()) {
				String propertyKey = extractPropertyKey(value.getValue());
				if(propertyKey != null && searchedKeys.contains(propertyKey)){
					foundBeanProperties.put(name, value.getName());
				}
			}
		}
	}

	/* 
	 * Extract key from a spring property placeholder reference
	 *  
	 * ex : value="${test.com}" => "test.com" 
	 * 
	 * return null if this is not a text placeholder
	 */
	private String extractPropertyKey(Object value) {
		if(value instanceof TypedStringValue){
			String propertyTextValue = ((TypedStringValue) value).getValue();
			if(propertyTextValue != null){
				propertyTextValue = propertyTextValue.trim();
				if(isPropertyPlaceholder(propertyTextValue)){
					return sanitizeProperty(propertyTextValue);
				}
			}
		}
		return null;
	}

	/* Return true if a String is a placeholder "${test.com}" for example */
	private boolean isPropertyPlaceholder(String propertyTextValue) {
		return propertyTextValue.startsWith(DEFAULT_PLACEHOLDER_PREFIX) && propertyTextValue.endsWith(DEFAULT_PLACEHOLDER_SUFFIX);
	}

	/* Remove placeholder separators from a placeholder "${test.com}" => "test.com" */
	private String sanitizeProperty(String propertyTextValue) {
		return propertyTextValue.replace(DEFAULT_PLACEHOLDER_PREFIX, "").replace(DEFAULT_PLACEHOLDER_SUFFIX, "");
	}
	
}
