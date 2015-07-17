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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import static org.junit.Assert.assertEquals;

/**
 * Tests the JNDI loading of properties using JndiAwarePropertyPlaceholderConfigurer
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class JndiLoadingTest {


	private static Logger logger = LoggerFactory.getLogger(JndiLoadingTest.class.getName());

    @Autowired
    private TestBean testBeanFromJndi;

    @Test
    public void testLoadingFromJndi()  {
        assertEquals("invalid prop in bean", "value1FromJndi", testBeanFromJndi.getSampleProp1());
        assertEquals("invalid prop in bean", "value2FromPropertiesFileOnly", testBeanFromJndi.getSampleProp2());
        assertEquals("invalid prop in bean", "value3FromSystemOnly", testBeanFromJndi.getSampleProp3());
        assertEquals("expecting JNDI to have precedence to system and properties files", "value4FromJndi", testBeanFromJndi.getSampleProp4());
    }

    @BeforeClass
    public static void simulateJndiAndSystemPropertiesInjection() throws NamingException {
        //Initialize the JNDI context (none is present by default in J2SE)
        SimpleNamingContextBuilder.emptyActivatedContextBuilder();

        InitialContext initialContext = new InitialContext();
        initialContext.bind("key1FromJndiOnly", "value1FromJndi");
        initialContext.bind("key4InJndiPropsSystem", "value4FromJndi");

        System.setProperty("key3FromSystemOnly", "value3FromSystemOnly");
        System.setProperty("key4InJndiPropsSystem", "value4InPropsSystem");
    }



}
