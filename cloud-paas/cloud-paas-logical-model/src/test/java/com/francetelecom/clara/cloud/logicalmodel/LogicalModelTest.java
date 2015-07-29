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

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.ValidatorUtil;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.net.MalformedURLException;
import java.util.*;

import static org.junit.Assert.fail;

@ContextConfiguration(locations = "application-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class LogicalModelTest {

    @Autowired
    SpringooLogicalModelCatalog utilSpringooIntegration;

    @Autowired
    DatasourcesLogicalModelCatalog datasourcesLogicalModelCatalog;

    @Autowired
    DianeLogicalModelCatalog dianeLogicalModelCatalog;

    @Autowired
    Map<String, SampleAppFactory> sampleAppFactoryMap;

	@Autowired
	ElPaaSoLogicalModelCatalog elPaaSoLogicalModelCatalog;

    @Autowired
    LogicalDeploymentRepository logicalDeploymentRepository;
	
	LogicalDeployment deployment;
    LogicalDeployment deployment_bis;
	
    ProcessingNode node;
    ProcessingNode adminNode;
    ProcessingNode jeeProcessing;

	LogicalRelationalService rds;
	LogicalWebGUIService web;
	LogicalWebGUIService webAdmin;
    LogicalWebGUIService webGuiService;

	LogicalSoapConsumer soapOut;
    LogicalSoapService soapIn;

    //external MOM (Momaas)
	LogicalQueueSendService sendMessageService;
	LogicalQueueReceiveService receiveMessageService;

    // internal mom
	LogicalMomService mom;
	LogicalMomService mom2;

	LogicalLogService log;

    private static final Logger logger = LoggerFactory.getLogger(LogicalModelTest.class);

	
	@Before
	public void setUp() throws InvalidConfigServiceException {

		this.deployment=new LogicalDeployment();

		/**
		 * Functionnal cluster
		 */
		this.node = new JeeProcessing("node1", deployment);

		MavenReference earNode1 = new MavenReference("test.group","test.appli","1.0");
		earNode1.setExtension("ear");

		this.node.setSoftwareReference(earNode1);

		this.rds = new LogicalRelationalService("rds",deployment);
        rds.setServiceName("jdbc/MyDataSource");
		node.addLogicalServiceUsage(rds,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		this.web = new LogicalWebGUIService("web", deployment);
		web.setContextRoot(new ContextRoot("/appliWeb"));
		node.addLogicalServiceUsage(web,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		this.soapOut = new LogicalSoapConsumer("soapConsumer", deployment, "project", "service", 1);
		this.soapOut.setJndiPrefix("soapConsumerJndiPrefix");
		node.addLogicalServiceUsage(soapOut,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        MavenReference mvnWsdl = new MavenReference("groupId", "artifactId", "version", "extension", "classifier");
        this.soapIn = new LogicalSoapService("soap", deployment, "myService", 1, 1, "/api", new Path("/service"), mvnWsdl, "desc");
        soapIn.setInboundAuthenticationPolicy(new LogicalInboundAuthenticationPolicy());
        soapIn.setOutboundAuthenticationPolicy(new LogicalOutboundAuthenticationPolicy());
        node.addLogicalServiceUsage(soapIn,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);


        this.sendMessageService = new LogicalQueueSendService("SendMsgService", deployment, "GetMarket", "G1R0C0", "M4K", "ServerMomoo", "G7R0C0", 100, 1500, 1);
		node.addLogicalServiceUsage(sendMessageService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		this.receiveMessageService = new LogicalQueueReceiveService("ReceiveMsgService", deployment, "ResponseGetMarket", "G1R0C0", 5000, 1500, 1);
		node.addLogicalServiceUsage(receiveMessageService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		//internal mom
		this.mom = new LogicalMomService("mom", deployment);
		mom.setDestinationName("myQueue");
		node.addLogicalServiceUsage(mom,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		this.log = new LogicalLogService("log", deployment);
		log.setLogName("logApplicatif");
		node.addLogicalServiceUsage(log,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        LogicalOnlineStorageService storage = new LogicalOnlineStorageService("storage", deployment);
        storage.setServiceName("public-images");
        storage.setStorageCapacityMb(10);
        node.addLogicalServiceUsage(storage, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        LogicalConfigService logicalConfigService = new LogicalConfigService("frontEnd", deployment, "#config1 \nkey=vakue\n");
        node.addLogicalServiceUsage(logicalConfigService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        logicalConfigService.setKeyPrefix("config");

        /**
		 * Admin cluster
		 */
		this.adminNode = new JeeProcessing("adminNode", deployment);
		this.webAdmin = new LogicalWebGUIService("webAdmin", deployment);
		webAdmin.setContextRoot(new ContextRoot("/appliAdmin"));
		adminNode.addLogicalServiceUsage(webAdmin,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		adminNode.addLogicalServiceUsage(rds,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		this.mom2 = new LogicalMomService("mom2", deployment);
		mom2.setDestinationName("myQueue2");
		adminNode.addLogicalServiceUsage(mom2,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		node.addLogicalServiceUsage(mom2, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        // Create new logicalDeployment to test addExecutionNode / updateExecutionNode / addLogicalService / updateLogicalService
        this.deployment_bis = new LogicalDeployment();
        createLogicalWebGui();
        createLogicalExecutionNode();


	}


    /**
	 * Simply checks that the reference Springoo logical model for the March 14 demo can indeed be persisted in JPA
	 * @throws MalformedURLException
	 */
	@Test
	@Transactional
	public void testSpringooLogicalModel() throws MalformedURLException{
		LogicalDeployment springooLogicalModel = utilSpringooIntegration.createLogicalModel("Springoo");
        validateAndPersist(springooLogicalModel);

		//Note: the loading from JPA and test equality against the graph in memory is covered paas-services using DAO instead.
	}

    /**
     * Simply checks that the reference DataSourceProbe logical model for the March 14 demo can indeed be persisted in JPA
     * @throws MalformedURLException
     */
    @Test
    @Transactional
    public void testDataSourcesProbeLogicalModel() throws MalformedURLException{
        datasourcesLogicalModelCatalog.setSimulateMavenReferenceResolution(true);
        LogicalDeployment springooLogicalModel = datasourcesLogicalModelCatalog.createLogicalModel("ProjectionServiceTest", null);

        validateAndPersist(springooLogicalModel);

        //Note: the loading from JPA and test equality against the graph in memory is covered paas-services using DAO instead.
    }


    @Test
    @Transactional
    public void testBasicModelPersistence(){
        validateAndPersist(deployment);
    }

    private void validateAndPersist(LogicalDeployment logicalDeployment) {
        ValidatorUtil.validate(logicalDeployment);
        logicalDeploymentRepository.save(logicalDeployment);
        logicalDeploymentRepository.flush();
    }


    public void testListAccessors(){
		Assert.assertEquals(2, this.deployment.listProcessingNodes().size());
		Assert.assertEquals(11, this.deployment.listLogicalServices().size());

		Assert.assertEquals(10, this.node.listLogicalServices().size());

		Assert.assertEquals(3, this.adminNode.listLogicalServices().size());

		Assert.assertEquals(2, this.rds.listDependentProcessingNodes().size());

		Assert.assertEquals(1, this.webAdmin.listDependentProcessingNodes().size());
		
		
	}
	
	
	@Test
	public void exportApplicationXml() throws JAXBException{
		
		JAXBContext jc = JAXBContext.newInstance(LogicalDeployment.class);
		Marshaller m = jc.createMarshaller();
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT,
		  Boolean.TRUE );
		m.marshal( deployment, System.out );
	}
 
	
	@Test
	@Transactional
	public void testManyToManyPersistence(){
		LogicalDeployment deployment;
		
		ProcessingNode node;
		ProcessingNode adminNode;
		
		LogicalRelationalService rds;
		LogicalWebGUIService web;
		LogicalWebGUIService webAdmin;
		
		LogicalSoapConsumer soapIn;
		LogicalQueueSendService sendMessageService;
		LogicalQueueReceiveService receiveMessageService;
		LogicalMomService mom;
		
		deployment=new LogicalDeployment();

		
		/**
		 * Functionnal
		 */
		node = new JeeProcessing("nodeA", deployment);

		rds = new LogicalRelationalService("myRds",deployment);
        rds.setServiceName("jdbc/MyDataSource");
		rds.setCapacityMo(2000);
		rds.setRelationalReplicaNumber(1);
		node.addLogicalServiceUsage(rds, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		web = new LogicalWebGUIService("myWeb", deployment);
		web.setContextRoot(new ContextRoot("/myWebCtx"));
		node.addLogicalServiceUsage(web,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        this.soapOut = new LogicalSoapConsumer("soapConsumer", deployment, "project", "service", 1);
		this.soapOut.setJndiPrefix("soapConsumerJndiPrefix1");        
        node.addLogicalServiceUsage(soapOut,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		soapIn = new LogicalSoapConsumer("soapC", deployment, "project", "service", 1);
		soapIn.setJndiPrefix("soapConsumerJndiPrefix2");
		node.addLogicalServiceUsage(soapIn,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		sendMessageService = new LogicalQueueSendService("SendMsgService", deployment, "GetMarket", "G1R0C0", "M4K", "ServerMomoo", "G7R0C0", 100, 1500, 1);
		node.addLogicalServiceUsage(sendMessageService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		receiveMessageService = new LogicalQueueReceiveService("ReceiveMsgService", deployment, "ResponseGetMarket", "G1R0C0", 5000, 1500, 1);
		node.addLogicalServiceUsage(receiveMessageService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		mom=new LogicalMomService("myMom", deployment);
		mom.setDestinationName("myQueue");
		node.addLogicalServiceUsage(mom,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		/**
		 * Admin cluster
		 */
		adminNode = new JeeProcessing("nodeAdmin", deployment);
		webAdmin = new LogicalWebGUIService("webAdmin", deployment);
		webAdmin.setContextRoot(new ContextRoot("/myWebAdmin"));		
		adminNode.addLogicalServiceUsage(webAdmin, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        validateAndPersist(deployment);

	}



	@Test
	@Transactional
	public void testMergeModel() throws JAXBException{
			
			LogicalDeployment deployment=new LogicalDeployment();
        logicalDeploymentRepository.save(deployment);
			
			ProcessingNode node=new JeeProcessing("node1", deployment);

			
			LogicalRelationalService rds = new LogicalRelationalService("rds",deployment);
            rds.setServiceName("jdbc/MyDataSource");
			node.addLogicalServiceUsage(rds,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        logicalDeploymentRepository.save(deployment);
			
			LogicalMomService mom = new LogicalMomService("mom", deployment);
			mom.setDestinationName("myQueue");
			node.addLogicalServiceUsage(mom,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
			LogicalLogService log = new LogicalLogService("log", deployment);
			log.setLogName("logApplicatif");
			node.addLogicalServiceUsage(log,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			
			ProcessingNode adminNode = new JeeProcessing("adminNode", deployment);
					
			LogicalWebGUIService webAdmin = new LogicalWebGUIService("webAdmin", deployment);
			webAdmin.setContextRoot(new ContextRoot("/appliAdmin"));
			
			adminNode.addLogicalServiceUsage(webAdmin,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			adminNode.addLogicalServiceUsage(rds,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
	
			LogicalMomService mom2 = new LogicalMomService("mom2", deployment);
			mom2.setDestinationName("myQueue2");
			
			LogicalQueueSendService sendMessageService = new LogicalQueueSendService("SendMsgService", deployment, "GetMarket", "G1R0C0", "M4K", "ServerMomoo", "G7R0C0", 100, 1500, 1);
			node.addLogicalServiceUsage(sendMessageService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			
			LogicalQueueReceiveService receiveMessageService = new LogicalQueueReceiveService("ReceiveMsgService", deployment, "ResponseGetMarket", "G1R0C0", 5000, 1500, 1);
			node.addLogicalServiceUsage(receiveMessageService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			
			adminNode.addLogicalServiceUsage(mom2,LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			node.addLogicalServiceUsage(mom2, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        logicalDeploymentRepository.save(deployment);
						
	
	}

    @Test
    public void testAddLogicalService() {
        this.deployment_bis.addLogicalService(webGuiService);
        Assert.assertEquals(webGuiService, this.deployment_bis.listLogicalServices().toArray()[0]);
    }

    @Test
    public void testRemoveLogicalServiceAssociated() {

        LogicalDeployment dianeLd = dianeLogicalModelCatalog.createLogicalModel("dianeLD");
        dianeLd.addLogicalService(webGuiService);
        Assert.assertNotNull("logical deployment does not contained added service", dianeLd.listLogicalServices(webGuiService.getClass(), webGuiService.getLabel()));

        ProcessingNode node = dianeLd.findProcessingNode("Diane_M2M_JEE");

        node.addLogicalServiceUsage(webGuiService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        boolean isAssociated = false;
        for (LogicalNodeServiceAssociation assoc : node.listLogicalServicesAssociations()) {
            isAssociated = assoc.areAssociated(node, webGuiService);
        }

        Assert.assertTrue("logical node / service association is not valid", isAssociated);

        try {
            dianeLd.removeLogicalService(webGuiService);
            fail("service has been deleted whereas it is still associated");
        } catch (BusinessException e) {

        }


    }

    @Test
//    @Ignore
    public void testRemoveLogicalServiceNotAssciated() {
        LogicalDeployment dianeLd = dianeLogicalModelCatalog.createLogicalModel("dianeLD");

        dianeLd.addLogicalService(webGuiService);

        Assert.assertNotNull("logical deployment does not contained added service", dianeLd.listLogicalServices(webGuiService.getClass(), webGuiService.getLabel()));

        try {
            dianeLd.removeLogicalService(webGuiService);
        } catch (BusinessException e) {
            fail("service can not be deleted. Service is still associated to a node");
        }

        Assert.assertEquals("logical deployment still content supposed deleted service", dianeLd.listLogicalServices(webGuiService.getClass(), webGuiService.getLabel()).size(), 0);
    }

    @Test
    public void testAddLogicalExecutionNode() {
        deployment_bis.addExecutionNode(jeeProcessing);
        Assert.assertEquals(jeeProcessing, this.deployment_bis.listProcessingNodes().toArray()[0]);
    }

    @Test
    public void testRemoveLogicalExecutionNodeNotAssociated() {

        LogicalDeployment dianeLd = dianeLogicalModelCatalog.createLogicalModel("dianeLD");
        dianeLd.addExecutionNode(jeeProcessing);

        boolean existNode = false;
        for (ProcessingNode node : dianeLd.listProcessingNodes()) {
            if (node.equals(jeeProcessing)) {
                existNode = true;
            }
        }

        Assert.assertTrue("logical deployment does not contained added jee processing", existNode);

        dianeLd.removeProcessingNode(jeeProcessing);

        existNode = false;
        for (ProcessingNode node : dianeLd.listProcessingNodes()) {
            if (node.equals(jeeProcessing)) {
                existNode = true;
            }
        }

        Assert.assertFalse("jee processing has not been deleted", existNode);
    }

    @Test
    public void testRemoveLogicalExecutionNodeAssociated() {
        LogicalDeployment dianeLd = dianeLogicalModelCatalog.createLogicalModel("dianeLD");
        ProcessingNode selectedNode = null;
        for (ProcessingNode node : dianeLd.listProcessingNodes()) {
            if (node.getLabel().equals("Diane_M2M_JEE")) {
                selectedNode = node;
            }
        }

        Assert.assertNotNull("logical deployment does not contained the needed jee processing", selectedNode);

        dianeLd.removeProcessingNode(selectedNode);


        selectedNode = null;
        for (ProcessingNode node : dianeLd.listProcessingNodes()) {
            if (node.getLabel().equals("Diane_M2M_JEE")) {
                selectedNode = node;
            }
        }

        Assert.assertNull("jee processing has not been deleted", selectedNode);


    }

    @Test
    public void testDianeRemoveAssociation() {
        LogicalDeployment logicalDeployment = dianeLogicalModelCatalog.populateLogicalDeployment(null);
        dianeRemoveAssoication(logicalDeployment, "diane database");
    }

    @Test
    @Transactional
    public void testDianeRemoveAssociationWithPersistence() {
        LogicalDeployment logicalDeployment = dianeLogicalModelCatalog.populateLogicalDeployment(null);

        validateAndPersist(logicalDeployment);
        logicalDeployment = logicalDeploymentRepository.findOne(logicalDeployment.getId());

        logicalDeployment = dianeRemoveAssoication(logicalDeployment, "diane database");
        validateAndPersist(logicalDeployment);

    }

    @Test
    public void testRemoveAllProcessingNodes() {
        LogicalDeployment logicalDeployment = dianeLogicalModelCatalog.populateLogicalDeployment(null);
        logicalDeployment.removeAllProcessingNodes();
        Assert.assertTrue("there should be no execution node remaining", logicalDeployment.listProcessingNodes().isEmpty());
    }

    @Test
    public void testRemoveAllServices() {
        LogicalDeployment logicalDeployment = dianeLogicalModelCatalog.populateLogicalDeployment(null);
        logicalDeployment.removeAllProcessingNodes();
        Assert.assertTrue("there should be no execution node remaining", logicalDeployment.listProcessingNodes().isEmpty());

        try {
            logicalDeployment.removeAllLogicalService();
        } catch (BusinessException e) {
            Assert.fail("fail on service delete");
        }
        Assert.assertTrue("there should be no service remaining", logicalDeployment.listLogicalServices().isEmpty());
    }

    @Test
    public void testRemoveAllDianeDesign() {
        removeAllDianeDesign(false);
    }

    @Test
    @Transactional
    public void testRemoveAllDianeDesignWithPersistence() {
        removeAllDianeDesign(true);
    }

    /**
     * Verify our sample models are valid as seen by {@link com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment#checkOverallConsistency()}
     * @throws BusinessException
     */
    @Test
    public void testOverallConsistencyOnValidModels() throws BusinessException {
        for (Map.Entry<String, SampleAppFactory> entry : sampleAppFactoryMap.entrySet()) {
            SampleAppFactory sampleAppFactory = entry.getValue();
            String sampleFactoryName = entry.getKey();
            LogicalDeployment logicalDeployment = null;
            try {
                logicalDeployment = sampleAppFactory.populateLogicalDeployment(null);
            } catch (Exception e) {
                logger.info("Ignoring sample app factory [" + sampleFactoryName +  "] which is not self standing, caught:" + e);
            }
            if (logicalDeployment != null) {
                logger.info("Testing validity of model [" + sampleFactoryName + "]");
                logicalDeployment.checkOverallConsistency();
            }
        }
    }

    private void instanciateModelAndAssertViolationDetected(SampleAppFactory sampleAppFactory, LogicalModelModifier logicalModelModifier) throws BusinessException {
        LogicalDeployment logicalDeployment = null;
        try {
            logicalDeployment = sampleAppFactory.populateLogicalDeployment(null);
        } catch (Exception e) {
            assert false: "unexpected invalid sampleAppFactories, caught:" + e;
        }
        if (logicalDeployment != null) {
            if (logicalModelModifier != null) {
                logicalModelModifier.applyModifications(logicalDeployment);
            }
            try {
                logicalDeployment.checkOverallConsistency();
                Assert.fail("Expected violation to javax.validation annotation to be detected");
            } catch (BusinessException e) {
                //Success
                logger.info("Caught as expected: " + e);
            }
        }
    }

    /**
     * Detects that dangling services are properly detected
     */
    @Test
    public void testOverallConsistencyDetectsDanglingServices() throws BusinessException {
        //simply unassociate all services and check that an exception is thrown
        instanciateModelAndAssertViolationDetected(utilSpringooIntegration, new LogicalModelModifier() {
            @Override
            public void applyModifications(LogicalDeployment ld) {
                LogicalRelationalService rdb = ld.listLogicalServices(LogicalRelationalService.class).iterator().next();
                for (ProcessingNode executionNode : rdb.listDependentProcessingNodes()) {
                    executionNode.removeAllLogicalServiceUsage(executionNode.listLogicalServicesAssociations());
                }
            }
        });
    }

    /**
     * Detects that violations to javax.validation constrainsts is indeed detected by {@link com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment#checkOverallConsistency()}
     */
    @Test
    public void testOverallConsistencyOnJavaXValidationViolations() throws BusinessException {
        instanciateModelAndAssertViolationDetected(utilSpringooIntegration, new LogicalModelModifier() {
            @Override
            public void applyModifications(LogicalDeployment ld) {
                ld.listProcessingNodes().get(0).setMinMemoryMbHint(-1);
            }
        });
        instanciateModelAndAssertViolationDetected(utilSpringooIntegration, new LogicalModelModifier() {
            @Override
            public void applyModifications(LogicalDeployment ld) {
                ld.listLogicalServices(LogicalRelationalService.class).iterator().next().setCapacityMo(-1);
            }
        });
        // Test that empty list of execution node throw an exception
        instanciateModelAndAssertViolationDetected(utilSpringooIntegration, new LogicalModelModifier() {
            @Override
            public void applyModifications(LogicalDeployment ld) {
                ld.removeAllProcessingNodes();
            }
        });
        // Check dead letter queue has a name defined when enabled.
		instanciateModelAndAssertViolationDetected(elPaaSoLogicalModelCatalog, new LogicalModelModifier() {
			@Override
			public void applyModifications(LogicalDeployment ld) {
				LogicalMomService momService = ld.listLogicalServices(LogicalMomService.class).iterator().next();
				momService.setHasDeadLetterQueue(true);
				momService.setDeadLetterQueueName("");
			}
		});
		// Check that we don't have duplicate JNDI queue names.
		instanciateModelAndAssertViolationDetected(elPaaSoLogicalModelCatalog, new LogicalModelModifier() {
			@Override
			public void applyModifications(LogicalDeployment ld) {
				Iterator<LogicalMomService> it = ld.listLogicalServices(LogicalMomService.class).iterator();
				LogicalMomService momService1 = it.next();
				LogicalMomService momService2 = it.next();
				momService1.setDestinationName("duplicatedName");
				momService2.setDestinationName("duplicatedName");
			}
		});
		// Check that we don't have duplicate JNDI queue names.
		instanciateModelAndAssertViolationDetected(elPaaSoLogicalModelCatalog, new LogicalModelModifier() {
			@Override
			public void applyModifications(LogicalDeployment ld) {
				Iterator<LogicalMomService> it = ld.listLogicalServices(LogicalMomService.class).iterator();
				LogicalMomService momService1 = it.next();
				LogicalMomService momService2 = it.next();
				momService1.setDestinationName("duplicatedName");
				momService2.setDeadLetterQueueName("duplicatedName");
			}
		});
    }


    private void removeAllDianeDesign(boolean persist) {

        LogicalDeployment ld = dianeLogicalModelCatalog.populateLogicalDeployment(null);

        List<ProcessingNode> lenList = new ArrayList(ld.listProcessingNodes());
        List<LogicalNodeServiceAssociation> lnsaList;
        List<LogicalService> lsList;

        for (ProcessingNode jeeProcessing : lenList) {
            lnsaList = new ArrayList(jeeProcessing.listLogicalServicesAssociations());
            lsList = new ArrayList(jeeProcessing.listLogicalServices());

            for(LogicalNodeServiceAssociation association : lnsaList) {

                for (LogicalService service : lsList) {

                    if (association.areAssociated(jeeProcessing, service)) {
                        ld.findProcessingNode(jeeProcessing.getLabel()).removeLogicalServiceUsage(association);

                        if (persist) {
                            validateAndPersist(ld);
                            ld = logicalDeploymentRepository.findOne(ld.getId());
                        }

                    }
                }

            }

        }

        try {

            lsList = new ArrayList(ld.listLogicalServices());

            for (LogicalService service : lsList) {
                ld.removeLogicalService(service);

                if (persist) {
                    validateAndPersist(ld);
                    ld = logicalDeploymentRepository.findOne(ld.getId());
                }

            }

            lenList = new ArrayList(ld.listProcessingNodes());

            for (ProcessingNode jeeProcessing : lenList) {
                ld.removeProcessingNode(jeeProcessing);

                if (persist) {
                    validateAndPersist(ld);
                    ld = logicalDeploymentRepository.findOne(ld.getId());
                }

            }

        } catch (BusinessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        Assert.assertEquals("logical deployment supposed to to contains no logical service", 0, ld.listLogicalServices().size());
        Assert.assertEquals("logical deployment supposed to to contains no logical jee processing", 0, ld.listProcessingNodes().size());

    }

//
//    @Test
//    @Transactional
//    public void testRemoveAllDianeDesignWithPersistence() {
//
//    }

    private LogicalDeployment dianeRemoveAssoication(LogicalDeployment logicalDeployment, String serviceLabel) {


        LogicalNodeServiceAssociation associationToRemove = getLogicalNodeServiceAssociationFromService(logicalDeployment, serviceLabel);

        List<ProcessingNode> nodes = logicalDeployment.listProcessingNodes();
        for (ProcessingNode node : nodes) {

            if (associationToRemove != null) {
                node.removeLogicalServiceUsage(associationToRemove);
            }

        }

        // we test if we delete the good association
        Assert.assertNull("LogicalNodeServiceAssociation has not been deleted in service", getLogicalNodeServiceAssociationFromService(logicalDeployment, serviceLabel));
        Assert.assertNull("LogicalNodeServiceAssociation has not been deleted in jee processing", getLogicalNodeServiceAssociationFromJee(logicalDeployment));

        return logicalDeployment;
    }



    private LogicalNodeServiceAssociation getLogicalNodeServiceAssociationFromService(LogicalDeployment logicalDeployment, String serviceLabel) {

        Set<LogicalRelationalService> services = logicalDeployment.listLogicalServices(LogicalRelationalService.class, serviceLabel);
        assert services.size() == 1;

        LogicalRelationalService rds = services.iterator().next();
        List<LogicalNodeServiceAssociation> rdsAssociations = rds.listLogicalServicesAssociations();


        LogicalNodeServiceAssociation selectedAssociation = null;

        for (LogicalNodeServiceAssociation association : rdsAssociations) {
            if (association.getProcessingNode().getLabel().equals("Diane_M2M_JEE")) {
                selectedAssociation = association;
            }
        }
        return selectedAssociation;
    }

    private LogicalNodeServiceAssociation getLogicalNodeServiceAssociationFromJee(LogicalDeployment logicalDeployment) {

        LogicalNodeServiceAssociation selectedAssociation = null;

        Set<LogicalRelationalService> services = logicalDeployment.listLogicalServices(LogicalRelationalService.class, "diane database");
        assert services.size() == 1;
        LogicalRelationalService rds = services.iterator().next();

        ProcessingNode jeeProcessing = logicalDeployment.findProcessingNode("Diane_M2M_JEE");
        List<LogicalNodeServiceAssociation> associationsList = jeeProcessing.listLogicalServicesAssociations();

        for (LogicalNodeServiceAssociation association : associationsList) {
            if (association.getLogicalService().equals(rds)) {
                selectedAssociation = association;
            }
        }

        return selectedAssociation;
    }




    private void createLogicalExecutionNode() {
        this.jeeProcessing = new JeeProcessing();
        jeeProcessing.setLabel("my jee processing");
        MavenReference mvn = new MavenReference("groupId", "artifactId", "version", "extension", "classifier");
        jeeProcessing.setSoftwareReference(mvn);

    }
    private void createLogicalWebGui() {

        this.webGuiService = new LogicalWebGUIService();
        webGuiService.setLabel("My Test WebGui ");
        webGuiService.setContextRoot(new ContextRoot("/springoo-jpa"));
        webGuiService.setStateful(false);
        webGuiService.setMaxNumberSessions(10);

    }

}
