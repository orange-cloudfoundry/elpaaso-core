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
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.ElPaaSoLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

/**
 * A set of LogicalModel tests which do not require persistence context and are grouped into
 * a distinct class to make their execution faster.
 *  
 * @author skwg9735
 */
@ContextConfiguration(locations = "LogicalModelTest-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class LogicalModelEqualsHashCodeTest {

    /**
     * Creates the default app used for some specific test with empty constructor
     */
	@Autowired
    @Qualifier(value = "springooLogicalModelCatalog")
    SampleAppFactory defaultSampleAppFactory;

    /**
     * The factories to test all of our sample apps.
     */
	@Autowired
    Map<String, SampleAppFactory> sampleAppFactories;

    @Autowired
    PersistenceTestUtil persistenceTestUtil;

    @Autowired
	private EntityManagerFactory emf;

    private static Logger logger = LoggerFactory.getLogger(LogicalModelEqualsHashCodeTest.class.getName());


    @Test
    public void testUiidSize() {
        UUID uuid = UUID.randomUUID();
        String uuidString = uuid.toString();
        int length = uuidString.length();
        assertEquals("unexpected UUID generated toString size", 36, length);
    }

    /**
     * Tests that a {@link LogicalWebGUIService} obtained from the sample app catalog, and one filled manually using
     * its fields may be considered equals ignoring the associations (i.e. using {@link LogicalService#equalsShallow(LogicalService)}
     */
    @Test
    public void testStandaloneLogicalWebGuiEquals() {

        assertStandaloneServiceEquals(LogicalWebGUIService.class, new LogicalEntityPropertyCopier<LogicalWebGUIService>() {
            @Override
            public void copyFields(LogicalWebGUIService logicalService, LogicalWebGUIService webGUIServiceBeingPopulated) {
				webGUIServiceBeingPopulated.setStateful(logicalService.isStateful());
				webGUIServiceBeingPopulated.setSecure(logicalService.isSecure());
                webGUIServiceBeingPopulated.setLabel(logicalService.getLabel());
                webGUIServiceBeingPopulated.setContextRoot(logicalService.getContextRoot());
                webGUIServiceBeingPopulated.setMaxNumberSessions(logicalService.getMaxNumberSessions());
                webGUIServiceBeingPopulated.setMaxReqPerSeconds(logicalService.getMaxReqPerSeconds());
            }
        });
    }

    /**
     * Tests that a {@link LogicalWebGUIService} obtained from the sample app catalog, and one filled manually using
     * its fields may be considered equals ignoring the associations (i.e. using {@link LogicalService#equalsShallow(LogicalService)}
     */
    @Test
    public void testStandaloneSoapConsumersEquals() {
        assertStandaloneServiceEquals(LogicalSoapConsumer.class, new LogicalEntityPropertyCopier<LogicalSoapConsumer>() {
            @Override
            public void copyFields(LogicalSoapConsumer soapConsumer, LogicalSoapConsumer soapConsummerBeingPopulated) {
                soapConsummerBeingPopulated.setLabel(soapConsumer.getLabel());
                soapConsummerBeingPopulated.setWsDomain(soapConsumer.getWsDomain());
                soapConsummerBeingPopulated.setServiceProviderName(soapConsumer.getServiceProviderName());
                soapConsummerBeingPopulated.setServiceName(soapConsumer.getServiceName());
                soapConsummerBeingPopulated.setServiceName(soapConsumer.getServiceName());
                soapConsummerBeingPopulated.setServiceMajorVersion(soapConsumer.getServiceMajorVersion());
                soapConsummerBeingPopulated.setServiceMinorVersion(soapConsumer.getServiceMinorVersion());
                soapConsummerBeingPopulated.setJndiPrefix(soapConsumer.getJndiPrefix());
            }
        });
    }

    /**
     * Factors out the logic to test services equality among classes
     */
    private <E extends LogicalService> void assertStandaloneServiceEquals(Class<E> serviceClass, LogicalEntityPropertyCopier logicalEntityPropertyCopier) {
        int totalSampleService= 0;
        for (Map.Entry<String, SampleAppFactory> sampleAppFactoryEntry : sampleAppFactories.entrySet()) {
            String sampleFactoryName = sampleAppFactoryEntry.getKey();
            SampleAppFactory sampleAppFactory = sampleAppFactoryEntry.getValue();

            LogicalDeployment sampleLogicalModel1 = null;
            try {
                sampleLogicalModel1 = sampleAppFactory.populateLogicalDeployment(null);
            } catch (Throwable e) {
                logger.info("Skipping sample app [" + sampleFactoryName + "] as it seems not working in our context: Caught: " +e);
                continue;
            }
            Set<E> logicalWebGUIServices = sampleLogicalModel1.listLogicalServices(serviceClass);
            for (E logicalService : logicalWebGUIServices) {
                totalSampleService++;
                E webGUIServiceBeingPopulated = null;
                try {
                    webGUIServiceBeingPopulated = serviceClass.newInstance();
                } catch (Exception e) {
                    fail("could not instanciate service of class [" + serviceClass + "] caught:" + e);
                }
                logicalEntityPropertyCopier.copyFields(logicalService, webGUIServiceBeingPopulated);
                assertServicesEqualsShallow(sampleFactoryName, logicalService, webGUIServiceBeingPopulated);
            }
        }
        assertTrue("expected at least one " + serviceClass.getSimpleName() + "in all samples", totalSampleService >= 1);
    }

    private void assertServicesEqualsShallow(String sampleFactoryName, LogicalService logicalWebGUIService, LogicalService webGUIServiceBeingPopulated) {
        assertTrue("expected identical objects as returned by equalsShallow() for sample app [" + sampleFactoryName +  "]. expected 1 got 2: (please ignore associations in manual inspection)\n1=" + logicalWebGUIService.toString() + "\n2=" + webGUIServiceBeingPopulated , webGUIServiceBeingPopulated.equalsShallow(logicalWebGUIService));
    }

    /**
     * Tests that a {@link LogicalWebGUIService} obtained from the sample app catalog, and one filled manually using
     * its fields may be considered equals ignoring the associations (i.e. using {@link LogicalService#equalsShallow(LogicalService)}
     */
    public void testStandaloneLogicalExecutionNodeEquals() {
        for (Map.Entry<String, SampleAppFactory> sampleAppFactoryEntry : sampleAppFactories.entrySet()) {
            String sampleFactoryName = sampleAppFactoryEntry.getKey();
            SampleAppFactory sampleAppFactory = sampleAppFactoryEntry.getValue();

            LogicalDeployment sampleLogicalModel1 = sampleAppFactory.populateLogicalDeployment(null);
            List<ProcessingNode> jeeProcessings = sampleLogicalModel1.listProcessingNodes();
            assertTrue(jeeProcessings.size() >=1);
            for (ProcessingNode jeeProcessing : jeeProcessings) {
            	ProcessingNode jeeProcessingBeingConstructed = new JeeProcessing();
                MavenReference mavenReference = new MavenReference(jeeProcessing.getSoftwareReference());
                jeeProcessingBeingConstructed.setSoftwareReference(mavenReference);

                assertEquals("expected ExecNode to match the one from sample catalog [" + sampleFactoryName +  "]", jeeProcessing, jeeProcessingBeingConstructed);
            }
        }
    }

	/**
	 * Checks that two distinct logical model trees created the same way are equals
	 */
	@Test
	public void testTransientSpringooLogicalModelEquals() {
		testSampleLogicalModelEquals(false, defaultSampleAppFactory, null);
	}

	/**
	 * Checks that two distinct logical model trees created the same way and persistent are equals
	 */
	@Test
//    @Ignore("Waiting a fix for bug #80111")
	public void testPersistentSpringooLogicalModelEquals() {
		testSampleLogicalModelEquals(true, defaultSampleAppFactory, null);
	}

	/**
	 * Check modifications that do not bring semantic changes are ignored
	 */
	@Test
	public void testSpringooLogicalModelEqualsIgnoringSemanticlessChanges() {
		//JPA id is ignored in LogicalDeployment from exclusion in base class LogicalModelItem
		testSampleLogicalModelEquals(false, defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                ld.id = 23;
            }
        });
		testSampleLogicalModelEquals(false, defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                ld.name = "toto";
            }
        });
		
		//JPA id needs to be excluded explicitly in each subclass
		
		//JeeProcessing
		testSampleLogicalModelEquals(false, defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
            	ProcessingNode executionNode = ld.listProcessingNodes().iterator().next();
                executionNode.name = "toto";
            }
        });

		//LogicalService
		testSampleLogicalModelEquals(false, defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                for (LogicalService logicalService : ld.listLogicalServices()) {
                    logicalService.name = "toto";
                    //if (logicalService instanceof LogicalRelationalService) {
                    //}
                }
            }
        });
		testSampleLogicalModelEquals(false, defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                for (LogicalService logicalService : ld.listLogicalServices()) {
                    logicalService.id = 23;
                }
            }
        });
		testSampleLogicalModelEquals(false, defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                for (LogicalService logicalService : ld.listLogicalServices()) {
                    logicalService.logicalDeployment = null;
                }
            }
        });

	}
	
	private void testSampleLogicalModelEquals(boolean persistModels, SampleAppFactory sampleAppFactory, LogicalModelModifier modifier) {
	
		LogicalDeployment sampleLogicalModel1 = sampleAppFactory.populateLogicalDeployment(null);
		LogicalDeployment sampleLogicalModel2 = sampleAppFactory.populateLogicalDeployment(null);

        if (persistModels) {
            persistenceTestUtil.persistObject(sampleLogicalModel1);
            persistenceTestUtil.persistObject(sampleLogicalModel2);
            sampleLogicalModel1 = persistenceTestUtil.reloadLogicalDeployment(sampleLogicalModel1);
            sampleLogicalModel2 = persistenceTestUtil.reloadLogicalDeployment(sampleLogicalModel2);
        }

		if(modifier != null) {
			modifier.applyModifications(sampleLogicalModel2);
		}

        if (! sampleLogicalModel1.equals(sampleLogicalModel2)) {
            logger.error("Unexpected different objects:\nobject 1=" + sampleLogicalModel1.toString() + "\nobject 2=" + sampleLogicalModel2.toString());
        }
		Assert.assertEquals("expected unmodified objects to be identical", sampleLogicalModel1, sampleLogicalModel2);
		Assert.assertNotSame(sampleLogicalModel1, sampleLogicalModel2);

	}

    /**
      * Checks that two distinct @{link JeeProcessing} with different labels are seen as distinct
     */
    @Test
    public void testConfigLogicalModelEqualsDetectsDifferentObjects() {

		//JeeProcessing properly compared
		testSpringooLogicalModelEqualsHashCodeContracts(defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
            	ProcessingNode executionNode = ld.listProcessingNodes().iterator().next();
                executionNode.setLabel(executionNode.getLabel() + "suffix");
            }
        });
    }

	/**
 	 * Checks that two distinct logical model trees with semantic modifications are not equals. 
	 */
	@Test
	public void testSpringooLogicalModelEqualsDetectsDifferentObjects() {
		
		//LogicalDeployment properly compared
		testSpringooLogicalModelEqualsHashCodeContracts(defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                ld.setTemplate(false);
            }
        });
		//JeeProcessing properly compared
		testSpringooLogicalModelEqualsHashCodeContracts(defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
            	ProcessingNode executionNode = ld.listProcessingNodes().iterator().next();
                executionNode.setSoftwareReference(null);
            }
        });
		testSpringooLogicalModelEqualsHashCodeContracts(defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
            	ProcessingNode executionNode = ld.listProcessingNodes().iterator().next();
                executionNode.setAvailable(true);
            }
        });

		//MavenReference properly compared
		testSpringooLogicalModelEqualsHashCodeContracts(defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                MavenReference softwareReference = ld.listProcessingNodes().iterator().next().getSoftwareReference();
                softwareReference.setArtifactId("brokenValue");
            }
        });

		//LogicalRelationalService properly compared
		testSpringooLogicalModelEqualsHashCodeContracts(defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                for (LogicalService logicalService : ld.listLogicalServices()) {
                    if (logicalService instanceof LogicalRelationalService) {
                        LogicalRelationalService rdbs = (LogicalRelationalService) logicalService;
                        rdbs.setCapacityMo(1);
                    }
                }
            }
        });

		//LogicalRelationalService properly compared
		testSpringooLogicalModelEqualsHashCodeContracts(defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                for (LogicalService logicalService : ld.listLogicalServices()) {
                    if (logicalService instanceof LogicalRelationalService) {
                        LogicalRelationalService rdbs = (LogicalRelationalService) logicalService;
                        rdbs.setSqlVersion(LogicalRelationalServiceSqlDialectEnum.MYSQL_DEFAULT);
                    }
                }
            }
        });

		//LogicalWebGUIService properly compared
		testSpringooLogicalModelEqualsHashCodeContracts(defaultSampleAppFactory, new LogicalModelModifier() {
            public void applyModifications(LogicalDeployment ld) {
                for (LogicalService logicalService : ld.listLogicalServices()) {
                    if (logicalService instanceof LogicalWebGUIService) {
                        LogicalWebGUIService gui = (LogicalWebGUIService) logicalService;
                        gui.setContextRoot(new ContextRoot("/"));
                    }
                }
            }
        });
	}

    /**
     * Utility method to assert that the {@link Object#equals(Object)} and {@link Object#hashCode()} are indeed
     * respected
     * @param utilSpringooIntegration
     * @param modifier
     */
	public void testSpringooLogicalModelEqualsHashCodeContracts(SampleAppFactory utilSpringooIntegration, LogicalModelModifier modifier) {
		final LogicalDeployment springooLogicalModel1 = utilSpringooIntegration.populateLogicalDeployment(null);
		final LogicalDeployment springooLogicalModel2 = utilSpringooIntegration.populateLogicalDeployment(null);

        //Test that two semantically equivalent instances are equals, and have identical hashcodes
        Assert.assertEquals("expected unmodified objects to be equals", springooLogicalModel1, springooLogicalModel2);
        Assert.assertEquals("expected unmodified objects to have identical hashcodes", springooLogicalModel1.hashCode(), springooLogicalModel2.hashCode());
        Assert.assertEquals("expected unmodified objects to have the same toString()", springooLogicalModel1.toString(), springooLogicalModel2.toString());

        //Then apply some modifications
		modifier.applyModifications(springooLogicalModel2);

        //And asserts that equals indeed report non equals objects
		Assert.assertTrue("expected modified objects to not be equals", ! springooLogicalModel1.equals(springooLogicalModel2));
        Assert.assertFalse("expected modified objects to not have same toString(): \n" + springooLogicalModel1.toString()
                , springooLogicalModel1.toString().equals(springooLogicalModel2.toString()));
	}

    /**
     * Utility interface to allow for factoring out {@link LogicalModelEqualsHashCodeTest#assertServicesEqualsShallow}
     * @param <E> the concrete class to apply the modifs on. This avoids manual casts
     */
    private abstract static class LogicalEntityPropertyCopier<E extends LogicalEntity> {
        public abstract void copyFields(E from, E to); 
    }

    @Test
	public void testEmptyLd() {
		testPersistence(new LogicalDeployment());
	}


    @Test
    public void testCatalogsPersistence() {
        for (SampleAppFactory factory : sampleAppFactories.values()) {
            // Can't test for elpaaso catalog, need datacenter:prismo property
            if (!(factory instanceof ElPaaSoLogicalModelCatalog)) {
                testTypedAndEmptyConstructorLd(factory);
            }
        }
    }

    private void testTypedAndEmptyConstructorLd(SampleAppFactory sampleAppFactory) {
        testPersistence(sampleAppFactory.populateLogicalDeployment(new LogicalDeployment()));
    }

    public void testPersistence(LogicalDeployment toBePersisted) {

		// Need an entity Manager
		EntityManager em=emf.createEntityManager();
		// Need a transaction Manager
		EntityTransaction tx;
		tx = em.getTransaction();
		try {
			tx.begin();
			em.persist(toBePersisted);
			tx.commit();
		} finally{
			em.close();
		}

		em=emf.createEntityManager();
		tx = em.getTransaction();
		LogicalDeployment readFromDb = null;
		try {
			tx.begin();
			readFromDb = em.find(LogicalDeployment.class,toBePersisted.getId());

			tx.commit();

			assertNotNull("entity does not exist",readFromDb);
			assertNotSame(readFromDb, toBePersisted);
			//Note: this equals is would trigger lazy loading, we need to assert while the entity manager is still opened
			assertEquals(toBePersisted, readFromDb);
		} finally{

			if (readFromDb != null) {
				tx.begin();
				em.remove(readFromDb);
				tx.commit();
			}
			em.close();
		}
    }
    
}
