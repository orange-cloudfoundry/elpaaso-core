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
package com.francetelecom.clara.cloud.logicalmodel;

import com.francetelecom.clara.cloud.PersistenceTestUtil;
import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SpringooLogicalModelCatalog;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests introspection of the logical model through the {@link GuiClassMapping} annotation
 */
@ContextConfiguration(locations = "LogicalModelTest-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class LogicalModelIntrospectionTest {

	@Autowired
    SpringooLogicalModelCatalog utilSpringooIntegration;

    @Autowired
    PersistenceTestUtil persistenceTestUtil;
    
    /**
     * Tests that class-level annotations are indeed available at runtime for the portal to use
     * @param deployment
     */
    public void testClassMapping(final LogicalDeployment deployment) {
        Set<LogicalOnlineStorageService> logicalOnlineStorageServices = deployment.listLogicalServices(LogicalOnlineStorageService.class);
        for (LogicalOnlineStorageService logicalOnlineStorageService : logicalOnlineStorageServices) {
            Class<? extends LogicalOnlineStorageService> introspectedClass = logicalOnlineStorageService.getClass();
            GuiClassMapping annotation = getGuiMappingAnnotation(introspectedClass);
            assertEquals(true, annotation.isExternal());
            assertEquals("Blob store", annotation.serviceCatalogName());
        }
    }

    private GuiClassMapping getGuiMappingAnnotation(Class<? extends LogicalService> introspectedClass) {
        return introspectedClass.getAnnotation(GuiClassMapping.class);
    }

    @Test
    public void testTransientClassMapping() throws MalformedURLException{
        testClassMapping(utilSpringooIntegration.createLogicalModel("ModelIntrospectionTest"));
    }

	@Test
	public void testPersistenceClassMapping() throws MalformedURLException{
        LogicalDeployment springooLogicalModel = utilSpringooIntegration.createLogicalModel("ModelIntrospectionTest");
        try {
            persistenceTestUtil.persistObject(springooLogicalModel);
            LogicalDeployment reloadedLd = persistenceTestUtil.reloadLogicalDeployment(springooLogicalModel);
            testClassMapping(reloadedLd);
        } finally {
            //Clean up the HsqlDB for other tests that run within the same JVM.
            assertNull(persistenceTestUtil.deleteObject(springooLogicalModel));
        }
	}

    @Test
    public void testLogicalServicesListing() throws MalformedURLException{

        GuiClassMapping.StatusType statusType = GuiClassMapping.StatusType.SUPPORTED;
        boolean isExternal = false;
        Set<Class> servicesClasses= LogicalService.listServicesClass();
        assertTrue("missing expected Service", servicesClasses.contains(LogicalWebGUIService.class));

        Set<Class> externalServicesClasses = filterService(servicesClasses, true);
        Set<Class> internalServicesClasses = filterService(servicesClasses, false);

        assertTrue("missing expected Service", externalServicesClasses.contains(LogicalWebGUIService.class));
        Assert.assertFalse("extra ext Service", externalServicesClasses.contains(LogicalRelationalService.class));
        Assert.assertFalse("extra int Service", internalServicesClasses.contains(LogicalWebGUIService.class));
        Assert.assertFalse("extra int Service", internalServicesClasses.contains(LogicalSoapConsumer.class));

    }

    private Set<Class> filterService(Set<Class> servicesClasses, boolean external) {
        Set<Class> externalServicesClasses= new HashSet<Class>();
        for (Class serviceClass : servicesClasses) {
            GuiClassMapping annotation = getGuiMappingAnnotation(serviceClass);
            Assert.assertNotNull("missing annotation for " + serviceClass, annotation);
            if (annotation.isExternal() == external) {
                externalServicesClasses.add(serviceClass);
            }
        }
        return externalServicesClasses;
    }

}
