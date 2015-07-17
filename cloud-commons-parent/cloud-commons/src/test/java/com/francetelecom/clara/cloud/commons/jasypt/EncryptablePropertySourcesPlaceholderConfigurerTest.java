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
package com.francetelecom.clara.cloud.commons.jasypt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.naming.InitialContext;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.properties.PropertyValueEncryptionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.web.context.support.StandardServletEnvironment;

import com.francetelecom.clara.cloud.commons.TestBean;

@RunWith(MockitoJUnitRunner.class)
public class EncryptablePropertySourcesPlaceholderConfigurerTest extends PropertySourcesPlaceholderConfigurer{

	@Mock
	private StringEncryptor encryptor;

	@Mock
	private ConfigurableListableBeanFactory beanFactoryToProcess;

	@Mock
	private ConfigurablePropertyResolver propertyResolver;
	
	@Spy
	@InjectMocks
	private EncryptablePropertySourcesPlaceholderConfigurer configurer;
	
	@Test
	public void convert_property_should_try_to_decrypt_encoded_values() throws Exception {
		//Given
		String encryptedValue = "ENC(azef2132efaz5621azef321qsddf6azefZEAFazefqfazef)";
		assertTrue(PropertyValueEncryptionUtils.isEncryptedValue(encryptedValue));
		when(encryptor.decrypt(anyString())).thenReturn("decrypted!");
		
		//When
		String value = configurer.convertPropertyValue(encryptedValue);
		
		//Then
		assertEquals("decrypted!", value);
		verify(encryptor).decrypt("azef2132efaz5621azef321qsddf6azefZEAFazefqfazef");
	}
	
	@Test
	public void convert_property_should_not_try_to_decrypt_non_encoded_values() throws Exception {
		//Given
		String nonEncryptedValue = "test";
		assertFalse(PropertyValueEncryptionUtils.isEncryptedValue(nonEncryptedValue));
		
		//When
		String value = configurer.convertPropertyValue(nonEncryptedValue);
		
		//Then
		assertEquals("test", value);
		verify(encryptor, never()).decrypt(anyString());
	}

	
	/**
	 * this test is an integration test (use of a spring context)
	 * 
	 * It begins with a Jndi instanciation and then try to load a spring context depending on jndi properties.
	 * 
	 * The goal of this test is to validate that we can read jndi encoded properties from spring
	 */
	@Test
	public void process_properties_should_decode_all_encrypted_props() throws Exception {
		//Jndi setup
		//Set fictive jndi properties (some of them are encoded)
        SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        InitialContext initialContext = new InitialContext();
        initialContext.bind("jndi.prop1", "ENC(VLCeFXg9nPrx7Bmsbcb0h3v5ivRinKQ4)");
        initialContext.bind("jndi.prop2", "testjndiValue2");

        //when
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:/com/francetelecom/clara/cloud/commons/jasypt/jndiPropsDecoding.xml"){
			@Override
			protected ConfigurableEnvironment createEnvironment() {
				return new StandardServletEnvironment();
			}
		};
		
		//Then
		TestBean bean = context.getBean(TestBean.class);
		assertEquals("testjndivalue",bean.getSampleProp1());
		assertEquals("testjndiValue2",bean.getSampleProp2());
		assertEquals("testlocalvalue",bean.getSampleProp3());
		assertEquals("testlocalvalue2",bean.getSampleProp4());
	}
}
