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
package com.francetelecom.clara.cloud.presentation.designer;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.designer.panels.DesignerArchitectureMatrixPanel;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.presentation.utils.*;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.PaasUserNotFoundException;

import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;
import java.util.Map;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 18/01/12
 * Time: 15:49
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config/designer-context.xml")
public abstract class AbstractTestApplication {
    private static Logger LOGGER = LoggerFactory.getLogger(AbstractTestApplication.class);

    private final String JEE_PROCESSING = "JeeProcessing";
    private final String CFJAVA_PROCESSING = "CFJavaProcessing";

    @Autowired
    private ManageApplication manageApplication;
    @Autowired
    private ManageApplicationRelease manageApplicationRelease;
    @Autowired
    private ManagePaasUser managePaasUser;
    @Autowired
	private ManageLogicalDeployment manageLogicalDeployment;
    @Autowired
    protected MvnRepoDao mvnDao;
    @Autowired
    private LogicalServicesHelper logicalServicesHelper;
    @Autowired
    private DelegatingDesignerServices delegatingDesignerServices;
    @Autowired
    private SplunkBean splunkBean;
    @Autowired
    private HypericBean hypericBean;
    @Autowired
    private Map<String, SampleAppFactory> sampleAppFactoryMap;

    private LogicalDeployment logicalDeployment;

    private PaasWicketTester myTester;

    private String releaseUid;
    private String appUid;

    private String cuid = "testuser";
    private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

    private String defaultProcessingType;

    @Autowired
	private AuthenticationManager authenticationManager;
    
    @Autowired
	private ContactUsBean contactUsBean;

    protected AbstractTestApplication(){
        enableOnlyJeeProcessingTest();
    }

    @Before
    public void init() {

    	//Admin is authenticated
    	AuthenticationUtil.connectAsAdmin();
    	
        myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
        ((PaasTestSession)myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
        managePaasUser.checkBeforeCreatePaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));

        myTester.startPage(HomePage.class);

        // Given : an empty application / application release / empty logical deployment
        createApplicationAndFirstRelease(sampleAppFactoryMap.get(getSampleAppCatalogName()));
        // Given: logical deployment to build from catalog sample
        createLogicalDeployment();

        // go on designer step one page to access internal services
        NavigationUtils.goOnDesignerPage(myTester, releaseUid);

        when(mvnDao.resolveUrl(isA(MavenReference.class))).thenAnswer(new Answer<MavenReference>() {
            @Override
            public MavenReference answer(InvocationOnMock invocation) throws Throwable {
                MavenReference input = (MavenReference) invocation.getArguments()[0];

                if (input == null) {
                    input = new MavenReference();
                }

                MavenReference output = null;
                // Surprisingly, invocation is sometimes null...
                if (input != null) {
                    output = new MavenReference(input);
                    // compute a representative accessurl
                    output.setAccessUrl(new URL("http://myrepo:80/" + input.getArtifactName()));
                }
                return output;

            }
        });

    }

    @Test
    public void createArchitectureForJeeProcessing() {
        Assume.assumeTrue("Skipped, as it should only be run with JEE Processing", JEE_PROCESSING.equals(defaultProcessingType));
        createExternalServices();

        // Go on designer step two of the wizard
        NavigationUtils.goOnNextStep(myTester);

        createJEEProcessing();
        createInternalServices();
        createAssociationsForJeeProcessing();

        // Go on designer step three of the wizard
        NavigationUtils.goOnNextStep(myTester);

        checkConsistency(JEE_PROCESSING);
    }

    @Test
    public void createArchitectureForCFJavaProcessing() {
        Assume.assumeTrue("Skipped, as it should only be run with CF Java Processing",CFJAVA_PROCESSING.equals(defaultProcessingType));

        createExternalServices();

        // Go on designer step two of the wizard
        NavigationUtils.goOnNextStep(myTester);

        createCFJavaProcessing();
        createInternalServices();
        createAssociationsForCFJavaProcessing();

        // Go on designer step three of the wizard
        NavigationUtils.goOnNextStep(myTester);

        checkConsistency(CFJAVA_PROCESSING);
    }

    private void createExternalServices() {

        for (LogicalService service :logicalDeployment.listLogicalServices()) {
            if (isExternal(service.getClass())) {
                if (service instanceof LogicalWebGUIService) {
                    LogicalWebGUIService realService = (LogicalWebGUIService)service;
                    CreateObjectsWithGUI.createWebGUIService(myTester, realService.getLabel(), realService.getContextRoot().getValue(), realService.isStateful(), realService.isSecure(), realService.getMaxNumberSessions(), realService.getMaxReqPerSeconds());
                } else if (service instanceof LogicalSoapConsumer) {
                    LogicalSoapConsumer realService = (LogicalSoapConsumer)service;
                    CreateObjectsWithGUI.createWebServiceConsumerService(myTester, realService.getLabel(), realService.getJndiPrefix()
                            , realService.getWsDomain(), realService.getServiceProviderName(), realService.getServiceName()
                            , realService.getServiceMajorVersion(), realService.getServiceMinorVersion());
                } else if (service instanceof LogicalQueueSendService) {
                    LogicalQueueSendService realService = (LogicalQueueSendService) service;
                    CreateObjectsWithGUI.createQueueSendService(myTester, realService.getLabel(), "cloud"
                            , realService.getTargetApplicationName(), realService.getTargetApplicationVersion()
                            , realService.getTargetServiceName(), realService.getTargetServiceVersion(), realService.getJndiQueueName()
                            , realService.getMsgMaxSizeKB(), realService.getMaxNbMsgPerDay(), realService.getNbRetentionDay());
                } else if (service instanceof LogicalQueueReceiveService) {
                    LogicalQueueReceiveService realService = (LogicalQueueReceiveService) service;
                    CreateObjectsWithGUI.createQueueReceiveService(myTester, realService.getLabel(), realService.getServiceName()
                            , realService.getServiceVersion(), realService.getJndiQueueName()
                            , realService.getMsgMaxSizeKB(), realService.getMaxNbMsgPerDay(), realService.getNbRetentionDay());
                } else if (service instanceof LogicalSoapService) {
                    LogicalSoapService realService = (LogicalSoapService)service;
                    CreateObjectsWithGUI.createSoapService(myTester, realService.getLabel(), realService.getJndiPrefix(), realService.getServiceName(), realService.getServiceMajorVersion(), realService.getServiceMinorVersion(), realService.getDescription(), realService.getServiceAttachmentType(), realService.getContextRoot().getValue(), realService.getServiceAttachments(), realService.getInboundAuthenticationPolicy(), realService.getOutboundAuthenticationPolicy(), realService.getIdentityPropagation(), realService.getServicePath().getValue());
                } else {
                    Assert.fail("Unknow type of service found");
                }
                myTester.assertNoErrorMessage();


            }
        }


        // Test that all external services are displayed in the matrix
        DesignerArchitectureMatrixPanel matrixPanel = GetObjectsUtils.getArchitecturePanel(myTester);

        for (LogicalService service :logicalDeployment.listLogicalServices()) {
            if (isExternal(service.getClass())) {
                int serviceRowIndex = matrixPanel.getIndexOfService(service);
                Assert.assertTrue("service "+service.getLabel()+" has not been added to logical model", serviceRowIndex != -1);
            }
        }

    }

    private void createJEEProcessing() {

        for (ProcessingNode node : logicalDeployment.listProcessingNodes(JeeProcessing.class)) {
            MavenReference ref = node.getSoftwareReference();
            CreateObjectsWithGUI.createJEEProcessing(myTester, node.getLabel(), ref.getGroupId(), ref.getArtifactId(), ref.getVersion(), ref.getClassifier(), false, node.getMinMemoryMbHint());
            myTester.assertNoErrorMessage();
        }

    }

    private void createCFJavaProcessing() {

        for (ProcessingNode node : logicalDeployment.listProcessingNodes(CFJavaProcessing.class)) {
            MavenReference ref = node.getSoftwareReference();
            CreateObjectsWithGUI.createCFJavaProcessing(myTester, node.getLabel(), ref.getGroupId(), ref.getArtifactId(), ref.getVersion(), ref.getExtension(), ref.getClassifier(), false, node.getMinMemoryMbHint());
            myTester.assertNoErrorMessage();
        }

    }

    private void createInternalServices() {

        for (LogicalService service : logicalDeployment.listLogicalServices()) {
            if (!isExternal(service.getClass())) {
                if (service instanceof LogicalRelationalService) {
                    LogicalRelationalService realService = (LogicalRelationalService) service;
                    CreateObjectsWithGUI.createRelationalDatabaseService(myTester, realService.getLabel(), realService.getServiceName()
                            , realService.getSqlVersion(), realService.getCapacityMo());
                } else if (service instanceof LogicalOnlineStorageService) {
                    LogicalOnlineStorageService realService = (LogicalOnlineStorageService) service;
                    CreateObjectsWithGUI.createOnlineStorage(myTester, realService.getLabel(), realService.getServiceName(), realService.getStorageCapacityMb());
                } else if (service instanceof LogicalConfigService) {
                    LogicalConfigService realService = (LogicalConfigService) service;
                    CreateObjectsWithGUI.createConfig(myTester, realService.getLabel(), realService.getKeyPrefix(), realService.getConfigSetContent());
                } else if (service instanceof LogicalMomService) {
                    LogicalMomService momService = (LogicalMomService) service;
                    CreateObjectsWithGUI.createMom(myTester, momService.getLabel(), momService.getDestinationName(), momService.getDestinationCapacity(), momService.getDeadLetterQueueName());
                }else if (service instanceof LogicalRabbitService) {
                    	LogicalRabbitService rabbitService = (LogicalRabbitService) service;
                        CreateObjectsWithGUI.createRabbitMQService(myTester, rabbitService.getLabel(), rabbitService.getServiceName());
                } else {
                        Assert.fail("Unknown type of service found");
                }
               myTester.assertNoErrorMessage();
                
            }
        }

        // Test that all external services are displayed in the matrix
        DesignerArchitectureMatrixPanel matrixPanel = GetObjectsUtils.getArchitecturePanel(myTester);
        for (LogicalService service :logicalDeployment.listLogicalServices()) {
            if (!isExternal(service.getClass())) {
                int serviceRowIndex = matrixPanel.getIndexOfService(service);
                Assert.assertTrue("service "+service.getLabel()+" has not been added to logical model", serviceRowIndex != -1);
            }
        }

    }

    private void createAssociationsForJeeProcessing() {

        for (ProcessingNode node : logicalDeployment.listProcessingNodes(JeeProcessing.class)) {
            createAssociationsForLogicalServices(node);
        }

    }

    private void createAssociationsForCFJavaProcessing() {

        for (ProcessingNode node : logicalDeployment.listProcessingNodes(CFJavaProcessing.class)) {
            createAssociationsForLogicalServices(node);
        }

    }

	private void createAssociationsForLogicalServices(ProcessingNode node) {
		for (LogicalNodeServiceAssociation association : node.listLogicalServicesAssociations()) {

		    int serviceIndex = ((DesignerArchitectureMatrixPanel)myTester.getComponentFromLastRenderedPage(NavigationUtils.matrixPath)).getIndexOfService(association.getLogicalService());
		    int nodeProcessingIndex = ((DesignerArchitectureMatrixPanel)myTester.getComponentFromLastRenderedPage(NavigationUtils.matrixPath)).getIndexOfNode(node);
		    Assert.assertFalse("service not found in architecture", -1 == serviceIndex);
		    CreateObjectsWithGUI.createAssociationAtCell(myTester, serviceIndex, nodeProcessingIndex);
		    myTester.assertNoErrorMessage();

		    // check association between service and jeeprocessing is correctly done
		    Assert.assertTrue(DeleteEditObjects.isCellAssociated(myTester, serviceIndex, nodeProcessingIndex));
		}
	}
 

    
    private void checkConsistency(String nodeProcessing) {
        // Then :
        // I can see a table which reflects all services, and associations defined
        // Testing the table contains all the services defined in reference logical model
        // Test that all services are displayed in the matrix
        DesignerArchitectureMatrixPanel matrixPanel = GetObjectsUtils.getArchitecturePanel(myTester);

        for (LogicalService service :logicalDeployment.listLogicalServices()) {
            if (!isExternal(service.getClass())) {
                int serviceRowIndex = matrixPanel.getIndexOfService(service);
                Assert.assertTrue("service "+service.getLabel()+" has not been added to logical model", serviceRowIndex != -1);
            }
        }

        for (LogicalService service :logicalDeployment.listLogicalServices()) {
            if (isExternal(service.getClass())) {
                int serviceRowIndex = matrixPanel.getIndexOfService(service);
                Assert.assertTrue("service "+service.getLabel()+" has not been added to logical model", serviceRowIndex != -1);
            }
        }

        if (nodeProcessing.equalsIgnoreCase(JEE_PROCESSING)) {
        	listAllJeeProcessing(matrixPanel);
        } else {
        	listAllCFJavaProcessing(matrixPanel);
        }
        	
        // I see no error or warning messages
        myTester.assertNoErrorMessage();

        // I can ask for the creation of a new environment
        myTester.assertVisible("firstPartContainer:architectureSummaryPanel:newEnvLink");
    }



	private void listAllJeeProcessing(DesignerArchitectureMatrixPanel matrixPanel) {
		for (ProcessingNode jeeProcessing :logicalDeployment.listProcessingNodes(JeeProcessing.class)) {
             int jeeProcessingIndex = matrixPanel.getIndexOfNode(jeeProcessing);
             Assert.assertTrue("service "+jeeProcessing.getLabel()+" has not been deleted from logical model", jeeProcessingIndex != -1);
        }
	}

	private void listAllCFJavaProcessing(DesignerArchitectureMatrixPanel matrixPanel) {
		for (ProcessingNode cfjavaProcessing :logicalDeployment.listProcessingNodes(CFJavaProcessing.class)) {
            int cfjavaProcessingIndex = matrixPanel.getIndexOfNode(cfjavaProcessing);
            Assert.assertTrue("service "+cfjavaProcessing.getLabel()+" has not been deleted from logical model", cfjavaProcessingIndex != -1);
        }
	}

 
    
    
    private void createApplicationAndFirstRelease(SampleAppFactory sample) {

        try {
            releaseUid = manageApplicationRelease.createApplicationRelease(createApplication(sample), cuid, sample.getAppReleaseVersion());
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    private String createApplication(SampleAppFactory sample) {
        try {
            appUid = manageApplication.createPublicApplication(sample.getAppCode(), sample.getAppLabel(), sample.getAppDescription(), null, new SSOId(cuid));
        } catch (DuplicateApplicationException | PaasUserNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return appUid;
    }

    private void createLogicalDeployment() {
        logicalDeployment = sampleAppFactoryMap.get(getSampleAppCatalogName()).populateLogicalDeployment(null);
    }

    /**
     * Create an applicationContextMock to inject in Spring for Wicket
     */
    private ApplicationContextMock getApplicationContextMock() {
        ApplicationContextMock applicationContextMock = new ApplicationContextMock();

        applicationContextMock.putBean(manageApplication);
        applicationContextMock.putBean(manageApplicationRelease);
        applicationContextMock.putBean(managePaasUser);
        applicationContextMock.putBean(manageLogicalDeployment);
        applicationContextMock.putBean(logicalServicesHelper);
        applicationContextMock.putBean(delegatingDesignerServices);
        applicationContextMock.putBean(sampleAppFactoryMap);
        applicationContextMock.putBean(mvnDao);
        applicationContextMock.putBean(splunkBean);
        applicationContextMock.putBean(hypericBean);
        applicationContextMock.putBean(contactUsBean);
        applicationContextMock.putBean("authenticationManager",authenticationManager);
        

        return applicationContextMock;
    }

    protected boolean isExternal(Class<?> serviceClass) {

        GuiClassMapping annotation = serviceClass.getAnnotation(GuiClassMapping.class);
        Assert.assertNotNull("missing annotation for " + serviceClass, annotation);
        return annotation.isExternal();
    }

    @After
    public void clean() {

        try {

            manageApplicationRelease.deleteApplicationRelease(releaseUid);
            manageApplication.deleteApplication(appUid);

        } catch (BusinessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        myTester.destroy();
    }


    public void enableOnlyCfJavaProcessingTest(){
        this.defaultProcessingType=CFJAVA_PROCESSING;
    }

    public void enableOnlyJeeProcessingTest(){
        this.defaultProcessingType=JEE_PROCESSING;
    }

    protected abstract String getSampleAppCatalogName();

    public PaasWicketTester getMyTester() {
        return myTester;
    }

    public String getReleaseUid() {
        return releaseUid;
    }
}
