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
package com.francetelecom.clara.cloud.application.impl;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.core.service.exception.InvalidLogicalDeploymentException;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeploymentImpl;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.mvn.consumer.MavenReferenceResolutionException;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.net.MalformedURLException;
import java.net.URL;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Test Business implementation for LogicalDeployment component
 * 
 * Last updated : $LastChangedDate$ Last author : $Author$
 * 
 * @author Clara
 * @version : $Revision$
 */
public class ManageLogicalDeploymentImplTest {

	ManageLogicalDeploymentImpl manageLogicalDeployment = null;

	@Before
	public void setup() {
		manageLogicalDeployment = new ManageLogicalDeploymentImpl();
	}

	@Test
	public void testUpdateLogicalDeployment() throws BusinessException {

		// test data

		LogicalDeployment logicalDeployment = new LogicalDeployment();
		LogicalService logicalService1 = createLogicalWebGUIService(logicalDeployment, "LS1-XXX");
		LogicalService logicalService2 = createLogicalWebGUIService(logicalDeployment, "LS1-XXX");
		ProcessingNode jeeProcessing1 = createJeeProcessing(logicalDeployment, "LEN1-XXX");
		ProcessingNode jeeProcessing2 = createJeeProcessing(logicalDeployment, "LEN2-XXX");

		// mock setup: LogicalDeploymentDao
		LogicalDeploymentRepository logicalDeploymentRepositoryMock = mock(LogicalDeploymentRepository.class);
		manageLogicalDeployment.setLogicalDeploymentRepository(logicalDeploymentRepositoryMock);

		when(logicalDeploymentRepositoryMock.findOne(anyInt())).thenReturn(logicalDeployment);
		when(logicalDeploymentRepositoryMock.save(logicalDeployment)).thenReturn(logicalDeployment);

		// mock setup: MvnRepoDao
		MvnRepoDao mvnRepoDaoMock = mock(MvnRepoDao.class);
		manageLogicalDeployment.setMvnRepoDao(mvnRepoDaoMock);

		MavenReference resolvedDummyMavenRef = createResolvedMavenReference(new MavenReference("group", "artifact", "version", "type"));
		when(mvnRepoDaoMock.resolveUrl(any(MavenReference.class))).thenReturn(resolvedDummyMavenRef);

		// test run
		manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);

		// assertions
		Mockito.verify(logicalDeploymentRepositoryMock).findOne(Mockito.anyInt());
		Mockito.verify(logicalDeploymentRepositoryMock).save(logicalDeployment);
	}

	@Test(expected = ObjectNotFoundException.class)
	public void testUpdateLogicalDeploymentThrowsNotFoundException() throws TechnicalException, ObjectNotFoundException, InvalidMavenReferenceException {

		// test setup
		LogicalDeploymentRepository logicalDeploymentRepositoryMock = Mockito.mock(LogicalDeploymentRepository.class);
		manageLogicalDeployment.setLogicalDeploymentRepository(logicalDeploymentRepositoryMock);

		// mock setup
		Mockito.when(logicalDeploymentRepositoryMock.findOne(Mockito.anyInt())).thenReturn(null);

		// test run
		manageLogicalDeployment.updateLogicalDeployment(new LogicalDeployment());

	}

	@Test
	public void testConsultLogicalDeployment() throws BusinessException {

		// test data

		LogicalDeployment logicalDeployment = new LogicalDeployment();

		// mock setup: LogicalDeploymentDao
		LogicalDeploymentRepository logicalDeploymentRepositoryMock = mock(LogicalDeploymentRepository.class);
		manageLogicalDeployment.setLogicalDeploymentRepository(logicalDeploymentRepositoryMock);

		when(logicalDeploymentRepositoryMock.findOne(999)).thenReturn(logicalDeployment);

		// test run
		LogicalDeployment logicalDeploymentFound = logicalDeployment = manageLogicalDeployment.findLogicalDeployment(999);

		// assertions
		Mockito.verify(logicalDeploymentRepositoryMock).findOne(999);
		Assert.assertNotNull(logicalDeploymentFound);

	}

	@Test(expected = ObjectNotFoundException.class)
	public void testConsultLogicalDeploymentThrowsNotFoundException() throws InvalidLogicalDeploymentException, ObjectNotFoundException {

		// test setup
		LogicalDeploymentRepository logicalDeploymentRepositoryMock = Mockito.mock(LogicalDeploymentRepository.class);
		manageLogicalDeployment.setLogicalDeploymentRepository(logicalDeploymentRepositoryMock);

		// mock setup
		Mockito.when(logicalDeploymentRepositoryMock.findOne(999)).thenReturn(null);

		// / test run
		manageLogicalDeployment.findLogicalDeployment(999);

	}




	@Test(expected = InvalidMavenReferenceException.class)
	public void testCheckMavenReference_InvalidReference() throws InvalidMavenReferenceException {

		// test data
		MavenReference mr = new MavenReference("group", "artifact", "1.0");

		// Mock MvnRepoDaoImpl
		MvnRepoDao mvnRepoDaoMock = mock(MvnRepoDao.class);
		when(mvnRepoDaoMock.resolveUrl(any(MavenReference.class))).thenThrow(new MavenReferenceResolutionException(mr, "invalid maven reference"));

		// configure SUT
		manageLogicalDeployment.setMvnRepoDao(mvnRepoDaoMock);

		// exercise SUT
		try {
			manageLogicalDeployment.checkMavenReference(mr);
		} catch (InvalidMavenReferenceException e) {
			// Check exception properties
			Assert.assertEquals("Incorrect maven ref in exception", mr, e.getMavenReference());
			Assert.assertEquals("Incorrect error type in exception", InvalidMavenReferenceException.ErrorType.ARTIFACT_NOT_FOUND, e.getType());
			// Forward exception which is expected
			throw e;
		}
	}

	@Test
	public void testCheckMavenReference_ValidReference() throws InvalidMavenReferenceException, MalformedURLException {

		// test data
		MavenReference mr0 = new MavenReference("group", "artifact", "1.0");
		MavenReference mr1 = new MavenReference("group", "artifact", "1.0");

		URL expectedUrl = new URL("http://myrepo:8181/mygroup/myartifact/1.0/artifact-1.0.ear");
		mr1.setAccessUrl(expectedUrl);

		// Mock MvnRepoDaoImpl
		MvnRepoDao mvnRepoDaoMock = mock(MvnRepoDao.class);
		when(mvnRepoDaoMock.resolveUrl(any(MavenReference.class))).thenReturn(mr1);

		// configure SUT
		manageLogicalDeployment.setMvnRepoDao(mvnRepoDaoMock);

		// exercise SUT
		URL actualUrl = manageLogicalDeployment.checkMavenReference(mr0);

		// Assertions

		Assert.assertEquals("Incorrect access URL:", expectedUrl, actualUrl);

	}

	@Test
	public void testCheckLogicalSoapServiceConsistency_NoError() throws BusinessException {
		LogicalSoapService logicalSoapService = createLogicalSoapService("echoProviderSoap");

		// mock setup: MvnRepoDao
		MavenReference svcAttRef = logicalSoapService.getServiceAttachments();
		MavenReference svcAttResolvedRef = createResolvedMavenReference(svcAttRef);
		MvnRepoDao mvnRepoDaoMock = mock(MvnRepoDao.class);
		manageLogicalDeployment.setMvnRepoDao(mvnRepoDaoMock);
		when(mvnRepoDaoMock.resolveUrl(svcAttRef)).thenReturn(svcAttResolvedRef);

		manageLogicalDeployment.checkLogicalSoapServiceConsistency(logicalSoapService, false);

		verify(mvnRepoDaoMock).resolveUrl(logicalSoapService.getServiceAttachments());
	}

	@Test
	public void testCheckOverallConsistency_NoError() throws BusinessException, MalformedURLException {

		// test data
		LogicalDeployment logicalDeployment = new LogicalDeployment();

		ProcessingNode jee1 = createJeeProcessing(logicalDeployment, "jee1");
		ProcessingNode jee2 = createJeeProcessing(logicalDeployment, "jee2");
		LogicalRelationalService rdb1 = createLogicalRelationnalService(logicalDeployment, "rdb1");
		LogicalRelationalService rdb2 = createLogicalRelationnalService(logicalDeployment, "rdb2");
		jee1.addLogicalServiceUsage(rdb1, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		jee2.addLogicalServiceUsage(rdb2, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		MavenReference jee1ExpectedResolvedRef = createResolvedMavenReference(jee1.getSoftwareReference());
		MavenReference jee2ExpectedResolvedRef = createResolvedMavenReference(jee2.getSoftwareReference());

		// mock setup: LogicalDeploymentDao
		LogicalDeploymentRepository logicalDeploymentRepositoryMock = mock(LogicalDeploymentRepository.class);
		manageLogicalDeployment.setLogicalDeploymentRepository(logicalDeploymentRepositoryMock);

		when(logicalDeploymentRepositoryMock.findOne(anyInt())).thenReturn(logicalDeployment);

		// mock setup: MvnRepoDao

		MvnRepoDao mvnRepoDaoMock = mock(MvnRepoDao.class);
		manageLogicalDeployment.setMvnRepoDao(mvnRepoDaoMock);

		when(mvnRepoDaoMock.resolveUrl(jee1.getSoftwareReference())).thenReturn(jee1ExpectedResolvedRef);
		when(mvnRepoDaoMock.resolveUrl(jee2.getSoftwareReference())).thenReturn(jee2ExpectedResolvedRef);

		// exercise SUT

		manageLogicalDeployment.checkOverallConsistency(logicalDeployment);

		// assertions

		// have we tried to resolve url for each maven reference ?
		verify(mvnRepoDaoMock).resolveUrl(jee1.getSoftwareReference());
		verify(mvnRepoDaoMock).resolveUrl(jee2.getSoftwareReference());

		// have we tried to update logical deployment ? should be no
		verify(logicalDeploymentRepositoryMock, never()).save(logicalDeployment);
	}

	@Test(expected = LogicalModelNotConsistentException.class)
	public void testCheckOverallConsistency_InvalidMavenReferences() throws BusinessException {

		LogicalDeployment logicalDeployment = new LogicalDeployment();

		ProcessingNode jee1 = createJeeProcessing(logicalDeployment, "jee1");
		ProcessingNode jee2 = createJeeProcessing(logicalDeployment, "jee2");
		LogicalRelationalService rdb1 = createLogicalRelationnalService(logicalDeployment, "rdb1");
		LogicalRelationalService rdb2 = createLogicalRelationnalService(logicalDeployment, "rdb2");
		jee1.addLogicalServiceUsage(rdb1, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		jee2.addLogicalServiceUsage(rdb2, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// mock setup: LogicalDeploymentDao
		LogicalDeploymentRepository logicalDeploymentRepositoryMock = mock(LogicalDeploymentRepository.class);
		manageLogicalDeployment.setLogicalDeploymentRepository(logicalDeploymentRepositoryMock);

		when(logicalDeploymentRepositoryMock.findOne(anyInt())).thenReturn(logicalDeployment);

		// mock setup: MvnRepoDao

		MvnRepoDao mvnRepoDaoMock = mock(MvnRepoDao.class);
		manageLogicalDeployment.setMvnRepoDao(mvnRepoDaoMock);

		MavenReference mr = new MavenReference("group", "artifact-x", "1.0");
		when(mvnRepoDaoMock.resolveUrl(any(MavenReference.class))).thenThrow(new MavenReferenceResolutionException(mr, "test error"));

		// exercise SUT

		try {
			manageLogicalDeployment.checkOverallConsistency(logicalDeployment);
		} catch (LogicalModelNotConsistentException e) {
			// assert exception content
			// 2 errors
			Assert.assertEquals("Incorrect number of errors", 2, e.getErrors().size());
			// All errors should be of type InvalidMavenReferenceException
			for (BusinessException error : e.getErrors()) {
				Assert.assertTrue("Eror is not a maven reference error: " + error.getMessage(), (error instanceof InvalidMavenReferenceException));
			}
			// Forward exception which is expected
			throw e;
		}
	}

	@Test
	public void testCheckOverallConsistencyAndUpdateLogicalDeployment_NoError() throws BusinessException, MalformedURLException {

		// test data
		LogicalDeployment logicalDeployment = new LogicalDeployment();

		ProcessingNode jee1 = createJeeProcessing(logicalDeployment, "jee1");
		ProcessingNode jee2 = createJeeProcessing(logicalDeployment, "jee2");
		LogicalRelationalService rdb1 = createLogicalRelationnalService(logicalDeployment, "rdb1");
		LogicalRelationalService rdb2 = createLogicalRelationnalService(logicalDeployment, "rdb2");
		jee1.addLogicalServiceUsage(rdb1, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		jee2.addLogicalServiceUsage(rdb2, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		MavenReference jee1ExpectedResolvedRef = createResolvedMavenReference(jee1.getSoftwareReference());
		MavenReference jee2ExpectedResolvedRef = createResolvedMavenReference(jee2.getSoftwareReference());

		// mock setup: LogicalDeploymentDao
		LogicalDeploymentRepository logicalDeploymentRepositoryMock = mock(LogicalDeploymentRepository.class);
		manageLogicalDeployment.setLogicalDeploymentRepository(logicalDeploymentRepositoryMock);

		when(logicalDeploymentRepositoryMock.findOne(anyInt())).thenReturn(logicalDeployment);
		when(logicalDeploymentRepositoryMock.save(logicalDeployment)).thenReturn(logicalDeployment);

		// mock setup: MvnRepoDao

		MvnRepoDao mvnRepoDaoMock = mock(MvnRepoDao.class);
		manageLogicalDeployment.setMvnRepoDao(mvnRepoDaoMock);

		when(mvnRepoDaoMock.resolveUrl(jee1.getSoftwareReference())).thenReturn(jee1ExpectedResolvedRef);
		when(mvnRepoDaoMock.resolveUrl(jee2.getSoftwareReference())).thenReturn(jee2ExpectedResolvedRef);

		// exercise SUT

		LogicalDeployment updatedLogicalDeployment = manageLogicalDeployment.checkOverallConsistencyAndUpdateLogicalDeployment(logicalDeployment);

		// assertions

		// have we stored our updated logical model ?
		verify(logicalDeploymentRepositoryMock).save(logicalDeployment);

		// Is updatedLogicalDeployment the one which has been persisted ?
		Assert.assertEquals("Updated logical deployment is not the expected one", logicalDeployment, updatedLogicalDeployment);

		// are our access url updated ?
		// we need to check actual maven reference set at logical services level

		Assert.assertEquals("access url not updated", jee1ExpectedResolvedRef.getAccessUrl(), jee1.getSoftwareReference().getAccessUrl());
		Assert.assertEquals("access url not updated", jee2ExpectedResolvedRef.getAccessUrl(), jee2.getSoftwareReference().getAccessUrl());
	}

	@Test
	public void testCheckOverallConsistencyAndUpdateLogicalDeployment_NullMavenRef() throws BusinessException, MalformedURLException {

		// test data
		LogicalDeployment logicalDeployment = new LogicalDeployment();

		ProcessingNode jee1 = createJeeProcessing(logicalDeployment, "jee1");
		LogicalRelationalService rdb1 = createLogicalRelationnalService(logicalDeployment, "rdb1");
		jee1.setSoftwareReference(null);
		rdb1.setInitialPopulationScript(null);
		jee1.addLogicalServiceUsage(rdb1, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// mock setup: LogicalDeploymentDao
		LogicalDeploymentRepository logicalDeploymentRepositoryMock = mock(LogicalDeploymentRepository.class);
		manageLogicalDeployment.setLogicalDeploymentRepository(logicalDeploymentRepositoryMock);

		when(logicalDeploymentRepositoryMock.findOne(anyInt())).thenReturn(logicalDeployment);
		when(logicalDeploymentRepositoryMock.save(logicalDeployment)).thenReturn(logicalDeployment);

		// mock setup: MvnRepoDao

		MvnRepoDao mvnRepoDaoMock = mock(MvnRepoDao.class);
		manageLogicalDeployment.setMvnRepoDao(mvnRepoDaoMock);

		// exercise SUT

		LogicalDeployment updatedLogicalDeployment = manageLogicalDeployment.checkOverallConsistencyAndUpdateLogicalDeployment(logicalDeployment);

		// assertions

		// have we stored our updated logical model ?
		verify(logicalDeploymentRepositoryMock).save(logicalDeployment);

		// Is updatedLogicalDeployment the one which has been persisted ?
		Assert.assertEquals("Updated logical deployment is not the expected one", logicalDeployment, updatedLogicalDeployment);
	}

	// ===============================================================================================
	// == Utility and setup methods
	// ===============================================================================================
	/**
	 * Create a test LogicalSoapService
	 * 
	 * @param label
	 * @return
	 */
	private LogicalSoapService createLogicalSoapService(String label) {
		LogicalSoapService service = new LogicalSoapService();
		service.setLabel(label);

		String description = "Elpaaso ManageLogicalDeploymentImplTest";
		String serviceName = "echo";
		service.setDescription(description);
		service.setServiceName(serviceName); // WSP name
		service.setServiceMajorVersion(1);
		service.setServiceMinorVersion(2);
		service.setDescription("ManageLogicalDeploymentImplTest");
		service.setContextRoot(new ContextRoot("/api"));
		service.setIdentityPropagation(LogicalIdentityPropagationEnum.HTTP);
		service.setInboundAuthenticationPolicy(new LogicalInboundAuthenticationPolicy());
		service.setOutboundAuthenticationPolicy(new LogicalOutboundAuthenticationPolicy());
		service.setJndiPrefix("echoProviderSoap");
		service.setRootFileName(null);
		MavenReference mavenRefPaaSWS = new MavenReference();
		mavenRefPaaSWS.setGroupId("com.francetelecom.clara.cloud");
		mavenRefPaaSWS.setArtifactId("cloud-paas-ws-environment-wsdl");
		mavenRefPaaSWS.setVersion("2.1.0");
		try {
			mavenRefPaaSWS
					.setAccessUrl(new URL(
							"http://ORANGE_MAVEN_REPO/proxy/content/repositories/inhouse/com/francetelecom/clara/cloud/cloud-paas-ws-environment-wsdl/2.1/cloud-paas-ws-environment-wsdl-2.1.jar"));
		} catch (MalformedURLException e) {
			throw new TechnicalException("EchoTestProvider : invalid maven ref access url");
		}
		service.setServiceAttachments(mavenRefPaaSWS);
		service.setServiceAttachmentType(LogicalAttachmentTypeEnum.NONE);
		return service;
	}

	/**
	 * Create a test LogicalWebGUIService
	 * 
	 * @param logicalDeployment
	 * @param label
	 * @return
	 */
	private LogicalWebGUIService createLogicalWebGUIService(LogicalDeployment logicalDeployment, String label) {
		LogicalWebGUIService service = new LogicalWebGUIService();
		service.setContextRoot(new ContextRoot("/root"));
		logicalDeployment.addLogicalService(service);
		service.setLabel(label);
		return service;
	}

	/**
	 * Create a test LogicalRelationService with a unresolved maven reference
	 * 
	 * @param logicalDeployment
	 * @param label
	 * @return
	 */
	private LogicalRelationalService createLogicalRelationnalService(LogicalDeployment logicalDeployment, String label) {
		LogicalRelationalService lrs = new LogicalRelationalService();
		logicalDeployment.addLogicalService(lrs);
		lrs.setLabel(label);
		lrs.setServiceName("jndi");
		return lrs;
	}

	/**
	 * Create a test JeeProcessing with a unresolved maven reference
	 * 
	 * @param logicalDeployment
	 * @param label
	 * @return
	 */
	private ProcessingNode createJeeProcessing(LogicalDeployment logicalDeployment, String label) {
		ProcessingNode node = new JeeProcessing();
		logicalDeployment.addExecutionNode(node);
		node.setLabel(label);
		MavenReference mr = new MavenReference("group", "artifact-" + label, "1.0", "ear");
		node.setSoftwareReference(mr);
		return node;
	}

	/**
	 * Create a test resolved MavenReference from an unresolved MavenReference Build access url is
	 * http://myrepo:8181/releases/<group>/<artifact>/<version>/<artifact>.<type>
	 * 
	 * @param mavenReference
	 * @return resolved MavenReference
	 */
	private MavenReference createResolvedMavenReference(MavenReference mavenReference) {
		MavenReference resolvedMavenReference = new MavenReference(mavenReference);
		try {
			URL url = new URL("http://myrepo:8181/releases/" + mavenReference.getGroupId() + "/" + mavenReference.getArtifactId() + "/"
					+ mavenReference.getVersion() + "/" + mavenReference.getArtifactId() + "." + mavenReference.getType());
			resolvedMavenReference.setAccessUrl(url);
		} catch (Exception e) {
			throw new TechnicalException(e);
		}

		return resolvedMavenReference;
	}

}