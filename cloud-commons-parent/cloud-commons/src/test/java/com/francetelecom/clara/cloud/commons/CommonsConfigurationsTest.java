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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;

/**
 * Test the loading of default test configuration entries from cloud-commons-test-configurations
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class CommonsConfigurationsTest {


	private static Logger logger = LoggerFactory.getLogger(CommonsConfigurationsTest.class.getName());

    @Autowired
    private TestBean testBeanFromProperties;

    /**
     * Test current situation where we load from credentials.properties in spring context
     */
    @Test
    public void testLoadingFromPropertiesFile()  {
        String datacenter = System.getProperty("datacenter");
        if (datacenter == null) {
            logger.info("using default datacenter");
            datacenter = "reference";
        }
        assertBeansProperties(datacenter);
    }

    protected void assertBeansProperties(String fromDataCenter) {
        assertEquals("invalid prop in bean", fromDataCenter, testBeanFromProperties.getSampleProp1());
    }


}
