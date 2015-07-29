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
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.AllServicesLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;
import com.francetelecom.clara.cloud.presentation.models.HypericBean;
import com.francetelecom.clara.cloud.presentation.models.SplunkBean;
import com.francetelecom.clara.cloud.presentation.utils.*;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.TagTester;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.net.URL;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.when;

/**
 * Created by IntelliJ IDEA. User: wwnl9733 Date: 07/02/12 Time: 10:19 To change
 * this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config/designer-context.xml")
public class EditServiceIT {

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
	private AllServicesLogicalModelCatalog allServicesLogicalSample;
	@Autowired
	private DelegatingDesignerServices delegatingDesignerServices;
	@Autowired
	private SplunkBean splunkBean;
	@Autowired
	private HypericBean hypericBean;
	@Autowired
	private ContactUsBean contactUsBean;
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

		// given Admin is authenticated
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

		// go on designer step one page to access internal services
		NavigationUtils.goOnDesignerPage(myTester, releaseUid);

	}

@Test
	public void testEditShowsValues() {

		for (final LogicalService service : logicalDeployment.listLogicalServices()) {

			DeleteEditObjects.editServiceAtRow(myTester, GetObjectsUtils.getPositionForItem(myTester, service));

			myTester.assertVisible(NavigationUtils.designerParamFormPath);
			Form form = GetObjectsUtils.getParamsForm(myTester);
			form.visitFormComponents(new IVisitor<FormComponent, Void>() {
				@Override
				public void component(FormComponent formComponent, IVisit<Void> visit) {

					if (!(formComponent instanceof Button)) {
						String name = ((FormComponent) formComponent).getInputName();
						// We don't test complex fields, such as maven reference
						// parameters
						// Full Validation field don't need to be tested
						if (!name.contains(".") && !name.equals("fullvalidationContent:fullvalidation")) {
							try {
								Field field = GetObjectsUtils.getAnyField(service.getClass(), name);
								field.setAccessible(true);
								if (field.get(service) != null) {
									Assert.assertEquals(field.get(service).toString(), ((FormComponent) formComponent).getDefaultModelObject()
											.toString());
								}
							} catch (NoSuchFieldException e) {
								Assert.fail("field " + name + " could not be found");
							} catch (IllegalAccessException e) {
								Assert.fail("illegal access on " + name + " field");
							}
						}
					}
					visit.dontGoDeeper();
				}
			});

			NavigationUtils.submitParamsForm(myTester);
		}
	}

	@Test
	public void testEditWithoutModification() {

		for (LogicalService service : logicalDeployment.listLogicalServices()) {

			// At this time there is a problem with config logical service
			// When we open the form of the config logical service and we save
			// it without modifing it
			// line breaker characters are added, so equals method result is
			// false
			// TODO : correct it to avoid add or modify equals method of
			// LogicalConfigService

			// When: I edit a service, without modifying any field, and i choose
			// to update the service
			int index = GetObjectsUtils.getPositionForItem(myTester, service);
			Assert.assertNotSame("service not found in architecture matrix", -1, index);
			// check that selected service in portal is the good one
			myTester.assertLabel("matrix:matrixContainer:listRows:" + index + ":listCols:0:content:label", service.getLabel());

			DeleteEditObjects.editServiceAtRow(myTester, index);
			myTester.assertVisible(NavigationUtils.designerParamFormPath);

			FormTester formTester = NavigationUtils.getParamsFormTester(myTester);

			Assert.assertEquals("service label in form is not correct", service.getLabel(), formTester.getTextComponentValue("label"));

			LogicalService ls1 = (LogicalService) formTester.getForm().getModelObject();

			NavigationUtils.submitParamsForm(myTester);

			DeleteEditObjects.editServiceAtRow(myTester, index);
			myTester.assertVisible(NavigationUtils.designerParamFormPath);
			formTester = NavigationUtils.getParamsFormTester(myTester);
			LogicalService ls2 = (LogicalService) formTester.getForm().getModelObject();

			Assert.assertEquals(ls1, ls2);

			// Then: the resulting service content matches the values of the
			// sample service

			index = GetObjectsUtils.getPositionForItem(myTester, service);
			Assert.assertTrue("unable to find the following service into the appended services in the grid. " + service, index >=0);
			LogicalService realService = GetObjectsUtils.getServiceAtRow(myTester, index);

			LogicalService expectedService = logicalDeployment.listLogicalServices(service.getClass(), service.getName()).iterator().next();

			Assert.assertEquals("services must be equals.", expectedService, realService);

		}

		for (ProcessingNode node : logicalDeployment.listProcessingNodes()) {
			// When: I edit a service, without modifying any field, and i choose
			// to update the service
			int index = GetObjectsUtils.getPositionForItem(myTester, node);
			Assert.assertNotSame("node not found in architecture matrix", -1, index);
			// check that selected service in portal is the good one
			myTester.assertLabel("matrix:matrixContainer:listRows:0:listCols:" + index + ":content:label", node.getLabel());

			DeleteEditObjects.editNodeAtCol(myTester, index);
			myTester.assertVisible(NavigationUtils.designerParamFormPath);

			FormTester formTester = NavigationUtils.getParamsFormTester(myTester);
			Assert.assertEquals("service label in form is not correct", node.getLabel(), formTester.getTextComponentValue("label"));

			NavigationUtils.submitParamsForm(myTester);
			// Then: the resulting service content matches the values of the
			// sample service
			index = GetObjectsUtils.getPositionForItem(myTester, node);
			ProcessingNode realExecutionNode = GetObjectsUtils.getNodeAtCol(myTester, index);
			int nodeIndex = logicalDeployment.listProcessingNodes().indexOf(node);

			ProcessingNode expectedExecutionNode = logicalDeployment.listProcessingNodes().get(nodeIndex);

			Assert.assertEquals("jee processing services must be equals.", expectedExecutionNode, realExecutionNode);
		}
	}

	@Test
	public void testEditWithModification() throws NoSuchFieldException {

		// for each logical service of logical deployment it will
		for (LogicalService service : logicalDeployment.listLogicalServices()) {

			// get index of selected service in designer matrix
			int index = GetObjectsUtils.getPositionForItem(myTester, service);
			// assert that selected service exists in designer matrix
			Assert.assertNotSame("service not found in architecture matrix", -1, index);
			// click on selected service edit button and display edit service
			// form
			DeleteEditObjects.editServiceAtRow(myTester, index);
			// assert that edit form is visible
			myTester.assertVisible(NavigationUtils.designerParamFormPath);
			// modify service label. just add row index at the end of original
			// label			
			DeleteEditObjects.modifyServiceLabelAtRow(myTester, index);
			myTester.dumpPage();
			// test value of service label in designer matrix
			Assert.assertEquals("matrix panel has not been updated", service.getLabel() + index,
					myTester.getComponentFromLastRenderedPage(NavigationUtils.getPathForCell(index, 0) + ":label").getDefaultModelObject().toString());
			// test that new value has been persisted in database
			Assert.assertFalse("new service label has not been persisted",
					service.getName().equals(logicalDeployment.listLogicalServices(service.getClass(), service.getLabel())));
			// test that displayed value is the same that persisted value
			Assert.assertFalse("displayed service label is not equals to persisted service label",
					(myTester.getComponentFromLastRenderedPage(NavigationUtils.getPathForCell(index, 0) + ":label").getDefaultModelObject()
							.toString()).equals(logicalDeployment.listLogicalServices(service.getClass(), service.getLabel())));

		}

		// for each jee processing service of logical deployment it will
		for (ProcessingNode jeeProcessing : logicalDeployment.listProcessingNodes()) {

			// get index of selected service in designer matrix
			int index = GetObjectsUtils.getPositionForItem(myTester, jeeProcessing);
			// assert that selected service exists in designer matrix
			Assert.assertNotSame("service not found in architecture matrix", -1, index);
			// click on selected service edit button and display edit service
			// form
			DeleteEditObjects.editNodeAtCol(myTester, index);
			// assert that edit form is visible
			myTester.assertVisible(NavigationUtils.designerParamFormPath);
			// modify service label. just add row index at the end of original
			// label
			DeleteEditObjects.modifyServiceLabelAtRow(myTester, index);
			// test value of service label in designer matrix
			Assert.assertEquals("matrix panel has not been updated", jeeProcessing.getLabel() + index,
					myTester.getComponentFromLastRenderedPage(NavigationUtils.getPathForCell(0, index) + ":label").getDefaultModelObject().toString());

		}
	}

	@Test
	public void testEditReadOnly() {

		try {
			ApplicationRelease release = manageApplicationRelease.findApplicationReleaseByUID(myTester.getLastRenderedPage().getPageParameters()
					.get("releaseUid").toString());
			release.validate();
			release.lock();
			manageApplicationRelease.updateApplicationRelease(release);
		} catch (ObjectNotFoundException e) {
			Assert.fail("could not find application release with id "
					+ myTester.getLastRenderedPage().getPageParameters().get("releaseUid").toString());
		}

		// go on designer step one page to access internal services
		NavigationUtils.goOnDesignerPage(myTester, releaseUid);
		myTester.assertRenderedPage(DesignerPage.class);
		for (final LogicalService service : logicalDeployment.listLogicalServices()) {
			// When: I try to edit the service

			int index = GetObjectsUtils.getPositionForItem(myTester, service);
			Assert.assertFalse("service not found in architecture matrix", -1 == index);
			myTester.assertVisible(NavigationUtils.getPathForCell(index, 0) + ":cell-view");
			DeleteEditObjects.viewServiceAtRow(myTester, index);
			myTester.assertVisible(NavigationUtils.modalPath);
			// Then: all displayed fields are read-only
			Form form = GetObjectsUtils.getModalParamsForm(myTester);

			form.visitFormComponents(new IVisitor<FormComponent, Void>() {
				@Override
				public void component(FormComponent formComponent, IVisit<Void> visit) {
					if (!(formComponent instanceof Button)) {
						TagTester tagTester = myTester.getTagByWicketId(((FormComponent) formComponent).getId());
						if (tagTester != null) {
							Assert.assertNotNull(tagTester.getAttribute("disabled"));
						}
					} else {
						if ("addUpdateButton".equals(((Button) formComponent).getId())) {
							Assert.assertFalse("add/update button should not be visible", ((Button) formComponent).isVisible());
						} else if ("cancelCloseButton".equals(((Button) formComponent).getId())) {
							Label buttonLabel = (Label) ((FormComponent) formComponent).get("cancelLabel");
							Assert.assertTrue("cancel/close button should display \"close\"",
									"close".equals(buttonLabel.getDefaultModelObjectAsString()));
						}
					}
					visit.dontGoDeeper();
				}
			});
		}
		for (final ProcessingNode node : logicalDeployment.listProcessingNodes()) {
			// When: I try to edit the node

			int index = GetObjectsUtils.getPositionForItem(myTester, node);
			Assert.assertFalse("node not found in architecture matrix", -1 == index);
			myTester.assertVisible(NavigationUtils.getPathForCell(0, index) + ":cell-view");
			DeleteEditObjects.viewNodeAtCol(myTester, index);
			myTester.assertVisible(NavigationUtils.modalPath);
			// Then: all displayed fields are read-only
			Form form = GetObjectsUtils.getModalParamsForm(myTester);
			form.visitFormComponents(new IVisitor<FormComponent, Void>() {
				@Override
				public void component(FormComponent object, IVisit<Void> visit) {
					if (!(object instanceof Button)) {
						// Assert.assertFalse(((FormComponent)formComponent).getInputName()
						// + " should be disabled",
						// ((FormComponent)formComponent).isEnabled());
					}
					visit.dontGoDeeper();
				}
			});
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
		} catch (ObjectNotFoundException e) {
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
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (InvalidMavenReferenceException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
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
		applicationContextMock.putBean(allServicesLogicalSample);
		applicationContextMock.putBean(delegatingDesignerServices);
		applicationContextMock.putBean(mvnDao);
		applicationContextMock.putBean(splunkBean);
		applicationContextMock.putBean(hypericBean);
		applicationContextMock.putBean(contactUsBean);
		applicationContextMock.putBean("authenticationManager",authenticationManager);
		return applicationContextMock;
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
