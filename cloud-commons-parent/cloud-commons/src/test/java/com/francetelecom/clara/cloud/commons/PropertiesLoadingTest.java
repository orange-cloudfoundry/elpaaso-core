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
package com.francetelecom.clara.cloud.commons;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Illustrates the loading of the default properties file in spring using PropertyPlaceholderConfigurer
 */
public class PropertiesLoadingTest  {


	private static Logger logger = LoggerFactory.getLogger(PropertiesLoadingTest.class.getName());

    private TestBean testBeanFromProperties;

    static final String KEYNAME = "envKey";
    

    @After
    @Before
    public void reset_keyname_from_system_properties() {
        Properties properties = System.getProperties();
        properties.remove(KEYNAME);
    }
    
    @Test
    public void use_environment_properties_when_environmentKey_is_defined()  {

    	System.getProperties().setProperty(KEYNAME, "test");
    	loadSpringContextAndInitTestBean();
    	
        assertBeansProperties("test");
    }

    @Test
    public void use_default_properties_when_environmentKey_is_not_defined()  {
    	loadSpringContextAndInitTestBean();
    	
        assertBeansProperties("default");
    }

    @Test(expected=BeanInitializationException.class)
    public void fail_when_no_properties_file_are_found_for_specified_environmentKey()  {

    	System.getProperties().setProperty(KEYNAME, "invalid");
    	loadSpringContextAndInitTestBean();
    	
        assertBeansProperties("invalid");
    }

	private void loadSpringContextAndInitTestBean() {
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("/com/francetelecom/clara/cloud/commons/PropertiesLoadingTest-context.xml");
    	testBeanFromProperties = (TestBean) appContext.getBean("beanInitiatedFromProperties");
	}
	
    protected void assertBeansProperties(String fromDataCenter) {
        assertEquals("invalid prop in bean", "value1" + fromDataCenter, testBeanFromProperties.getSampleProp1());
        assertEquals("invalid prop in bean", "value2" + fromDataCenter, testBeanFromProperties.getSampleProp2());
        assertEquals("invalid prop in bean", "value3" + fromDataCenter, testBeanFromProperties.getSampleProp3());
        assertEquals("invalid prop in bean", "value4" + fromDataCenter, testBeanFromProperties.getSampleProp4());
    }


}
