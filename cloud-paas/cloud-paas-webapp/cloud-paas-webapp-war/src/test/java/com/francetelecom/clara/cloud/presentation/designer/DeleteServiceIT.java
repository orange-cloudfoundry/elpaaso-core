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

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.core.service.exception.*;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.designer.panels.DesignerArchitectureMatrixPanel;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.presentation.tools.DeleteConfirmationUtils;
import com.francetelecom.clara.cloud.presentation.utils.*;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.URL;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA. User: wwnl9733 Date: 01/02/12 Time: 12:18 To change
 * this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config/designer-context.xml")
public class DeleteServiceIT {

	@Autowired
	private ManageApplication manageApplication;
	@Autowired
	private ManageApplicationRelease manageApplicationRelease;
	@Autowired
	private ManagePaasUser managePaasUser;
	@Autowired
	private ManageLogicalDeployment manageLogicalDeployment;
	@Autowired
	private MvnRepoDao mvnDao;
	@Autowired
	private LogicalServicesHelper logicalServicesHelper;
	@Autowired
	private DelegatingDesignerServices delegatingDesignerServices;
	@Autowired
	private SplunkBean splunkBean;
	@Autowired
	private HypericBean hypericBean;
	@Autowired
	@Qualifier("allServicesLogicalModelCatalog")
	private SampleAppFactory allServicesLogicalSample;
	
	@Autowired
  	private AuthenticationManager authenticationManager;

	private LogicalDeployment logicalDeployment;

	private PaasWicketTester myTester;

	private String releaseUid;
	private String appUid;

	private String cuid = "testuser";
	private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

	@Before
	public void init() {

		AuthenticationUtil.connectAsAdmin();

		myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
		((PaasTestSession) myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));
		managePaasUser.checkBeforeCreatePaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));

		myTester.startPage(HomePage.class);

		createApplicationAndFirstRelease(allServicesLogicalSample);
		createAllServicesLD();

		// Mock all WS Call
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

		// go on designer step two page to access internal services
		NavigationUtils.goOnDesignerPage(myTester, releaseUid);

	}

	@Test
	public void testDialogOnDelete() {
		// When: I try to delete the first service in the list
		DeleteEditObjects.deleteServiceAtRow(myTester, 1);
		// Then: I'm presented a confirmation dialog
		myTester.assertContains("Are you sure you want to delete this item?");
	}

	@Test
	public void testCancelDelete() {
		LogicalService service = GetObjectsUtils.getServiceAtRow(myTester, 1);

		// When: i try to delete the first service in the list, and choose to
		// cancel the delete operation
		DeleteConfirmationUtils.forceCancel = true;

		DeleteEditObjects.deleteServiceAtRow(myTester, 1);
		// Then: the service is still present

		Assert.assertEquals(GetObjectsUtils.getServiceAtRow(myTester, 1), service);
	}

	@Test
	public void testDeleteWithAssociations() {
		// given the first web gui service in config sample logical model
		LogicalWebGUIService service = (LogicalWebGUIService) logicalDeployment.listLogicalServices(LogicalWebGUIService.class).toArray()[0];
		// When: i try to delete the first WebGui service in the list, which is
		// associated to an execution node
		int index = GetObjectsUtils.getPositionForItem(myTester, service);

		DeleteConfirmationUtils.forceOK = true;
		DeleteEditObjects.deleteServiceAtRow(myTester, index);
		// Then: an error message is displayed indicating the service is still
		// associated with an execution node and the service is still present
		myTester.assertErrorMessages(new String[] { "Can't delete a service already associated to a node" });

		// test that service is still present in designer matrix
		Assert.assertEquals(GetObjectsUtils.getServiceAtRow(myTester, index), service);
	}

	@Test
	public void testDeleteWithoutAssociation() {
		LogicalWebGUIService service = logicalDeployment.listLogicalServices(LogicalWebGUIService.class, "AllServicesWebUi").iterator().next();

		int serviceIndex = GetObjectsUtils.getPositionForItem(myTester, service);

		// Web ui is associated to 3 jee processing. We need to delete each
		// associated to be able to delete this service
		for (int i = 1; i <= 3; i++) {
			if (DeleteEditObjects.isCellAssociated(myTester, serviceIndex, i)) {
				DeleteEditObjects.deleteAssociationAtCell(myTester, serviceIndex, i);
			}
		}

		// When: i try to delete the first service in the list, which is not
		// associated to an execution node
		DeleteConfirmationUtils.forceOK = true;

		DeleteEditObjects.deleteServiceAtRow(myTester, serviceIndex);
		// no error message is displayed
		myTester.assertNoErrorMessage();

		// Then: the service is not displayed anymore in the summary table
		Assert.assertEquals("service has not been deleted in portal", -1, GetObjectsUtils.getPositionForItem(myTester, service));

	}

	@Test
	public void testDeleteAssociation() {

		LogicalWebGUIService service = logicalDeployment.listLogicalServices(LogicalWebGUIService.class, "AllServicesWebUi").iterator().next();
		int serviceIndex = GetObjectsUtils.getPositionForItem(myTester, service);

		// Web ui is associated to 3 jee processing. We need to delete each
		// associated to be able to delete this service
		for (int i = 1; i <= 3; i++) {
			if (DeleteEditObjects.isCellAssociated(myTester, serviceIndex, i)) {
				DeleteEditObjects.deleteAssociationAtCell(myTester, serviceIndex, i);
			}
		}

		Assert.assertFalse("association with first jee processing has not been deleted",
				DeleteEditObjects.isCellAssociated(myTester, serviceIndex, 1));
		Assert.assertFalse("association with second jee processing has not been deleted",
				DeleteEditObjects.isCellAssociated(myTester, serviceIndex, 2));
		Assert.assertFalse("association with third jee processing has not been deleted",
				DeleteEditObjects.isCellAssociated(myTester, serviceIndex, 3));
		// Then: the association is not displayed in the association table
		// Refresh the page so that we are sure persistence is done

		myTester.assertNoErrorMessage();
	}

	@Test
	public void testCompleteDelete() {

		DesignerArchitectureMatrixPanel architectureMatrixPanel = GetObjectsUtils.getArchitecturePanel(myTester);

		// When: I remove all the existing associations
		for (ProcessingNode node : logicalDeployment.listProcessingNodes()) {

			for (LogicalNodeServiceAssociation association : node.listLogicalServicesAssociations()) {

				int rowIndex = architectureMatrixPanel.getIndexOfService(association.getLogicalService());
				int colIndex = architectureMatrixPanel.getIndexOfNode(node);

				if (DeleteEditObjects.isCellAssociated(myTester, rowIndex, colIndex)) {
					DeleteEditObjects.deleteAssociationAtCell(myTester, rowIndex, colIndex);
				}
			}

		}

		// I remove all the existing JEE processing services
		for (ProcessingNode node : logicalDeployment.listProcessingNodes()) {
			DeleteEditObjects.deleteNodeAtCol(myTester, 1);
		}

		// I remove all the existing external and internal services
		for (LogicalService service : logicalDeployment.listLogicalServices()) {
			DeleteEditObjects.deleteServiceAtRow(myTester, 1);
		}

		myTester.assertNoErrorMessage();

		// check that all services have been deleted
		// When: I remove all the existing associations
		for (ProcessingNode jeeProcessing : logicalDeployment.listProcessingNodes()) {
			Assert.assertEquals("jee processing " + jeeProcessing.getLabel() + " has not been deleted", -1,
					GetObjectsUtils.getPositionForItem(myTester, jeeProcessing));
		}

		for (LogicalService service : logicalDeployment.listLogicalServices()) {
			Assert.assertEquals("service " + service.getLabel() + " has not been deleted", -1, GetObjectsUtils.getPositionForItem(myTester, service));
		}

	}

	private void createApplicationAndFirstRelease(SampleAppFactory sample) {

		try {
			releaseUid = manageApplicationRelease.createApplicationRelease(createPublicApplication(sample), cuid, sample.getAppReleaseVersion());
		} catch (PaasUserNotFoundException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (ApplicationNotFoundException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (DuplicateApplicationReleaseException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}

	}

	private String createPublicApplication(SampleAppFactory sample) {
		try {
			appUid = manageApplication.createPublicApplication(sample.getAppCode(), sample.getAppLabel(), sample.getAppDescription(), null, new SSOId(cuid));
		} catch (DuplicateApplicationException | PaasUserNotFoundException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
		return appUid;
	}

	private void createAllServicesLD() {
		try {
			ApplicationRelease release = manageApplicationRelease.findApplicationReleaseByUID(releaseUid);
			LogicalDeployment ld = manageLogicalDeployment.findLogicalDeployment(release.getLogicalDeployment().getId());
			logicalDeployment = allServicesLogicalSample.populateLogicalDeployment(ld);
			manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		} catch (InvalidMavenReferenceException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create an applicationContextMock to inject in Spring for Wicket
	 * 
	 * @return
	 */
	private ApplicationContextMock getApplicationContextMock() {
		ApplicationContextMock applicationContextMock = new ApplicationContextMock();

		applicationContextMock.putBean(manageApplication);
		applicationContextMock.putBean(manageApplicationRelease);
		applicationContextMock.putBean(managePaasUser);
		applicationContextMock.putBean(manageLogicalDeployment);
		applicationContextMock.putBean(logicalServicesHelper);
		applicationContextMock.putBean(delegatingDesignerServices);
		applicationContextMock.putBean(allServicesLogicalSample);
		applicationContextMock.putBean(mvnDao);
		applicationContextMock.putBean(splunkBean);
		applicationContextMock.putBean(hypericBean);
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
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}

		myTester.destroy();
	}

}
