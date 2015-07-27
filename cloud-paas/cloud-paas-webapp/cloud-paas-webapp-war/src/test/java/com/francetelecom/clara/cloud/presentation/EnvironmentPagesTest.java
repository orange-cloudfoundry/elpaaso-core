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
package com.francetelecom.clara.cloud.presentation;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.core.service.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.EnvironmentNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.PaasUserNotFoundException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.EchoProviderLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceEditPanel;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.environments.SelectedEnvironmentPage;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;
import com.francetelecom.clara.cloud.presentation.utils.CreateObjectsWithJava;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestApplication;
import com.francetelecom.clara.cloud.presentation.utils.PaasTestSession;
import com.francetelecom.clara.cloud.presentation.utils.PaasWicketTester;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto.LinkTypeEnum;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.test.ApplicationContextMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.doReturn;

/**
 * Created with IntelliJ IDEA. User: shjn2064 Date: 04/06/12 Time: 14:50
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-config/environmentPagesTest-context.xml")
public class EnvironmentPagesTest {

	private LogicalDeployment logicalDeployment;

	@Autowired
	protected ManageEnvironment manageEnvironment;
	@Autowired
	protected ManageApplicationRelease manageApplicationRelease;
	@Autowired
	protected ManageApplication manageApplication;
	@Autowired
	protected ManagePaasUser managePaasUser;
	@Autowired
	SampleAppFactory echoProviderLogicalModelCatalog;
	@Autowired
	ManageLogicalDeployment manageLogicalDeployment;
	@Autowired
	private DelegatingDesignerServices delegatingDesignerServices;
	@Autowired
	private List<LogicalServiceEditPanel> delegates;
	
	@Autowired
	private ContactUsBean contactUsBean;
	
	@Autowired
	private AuthenticationManager authenticationManager;

	protected PaasWicketTester myTester;

	private String cuid = "testuser";
	private PaasRoleEnum role = PaasRoleEnum.ROLE_USER;

	@Before
	public void init() {

		myTester = new PaasWicketTester(new PaasTestApplication(getApplicationContextMock(), false));
		((PaasTestSession) myTester.getSession()).setPaasUser(CreateObjectsWithJava.createPaasUserMock(cuid, role));

		delegates = new ArrayList<LogicalServiceEditPanel>();
		logicalDeployment = echoProviderLogicalModelCatalog.populateLogicalDeployment(null);

		try {
			// Mock all WS Call
			doReturn(createNewEnvironmentDetailsDto()).when(manageEnvironment).findEnvironmentDetails(anyString());
			doReturn(createApplicationRelease()).when(manageApplicationRelease).findApplicationReleaseByUID(anyString());
			doReturn(createApplication()).when(manageApplication).findApplicationByUID(anyString());
			doReturn(CreateObjectsWithJava.createPaasUserMock(cuid, role)).when(managePaasUser).findPaasUser(anyString());
			doReturn(createEchoProvider()).when(manageLogicalDeployment).findLogicalDeployment(anyInt());
			doReturn(new EmptyPanel("id")).when(delegatingDesignerServices).createPanelFor(anyString(), any(LogicalModelItem.class), any(DesignerHelperPage.class), eq(false),
					eq(true), anyBoolean());
		} catch (ObjectNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Mingle #2075 WSP in PaaS unofficially brokered to WSCs Scenario 5: WSP
	 * IOSW credentials (project name, service name, minor, major, IOWS access
	 * url) are displayed on the environment details
	 * 
	 * @throws EnvironmentNotFoundException
	 * @throws PaasUserNotFoundException
	 * @throws ApplicationReleaseNotFoundException
	 */
	@Test
	public void wsp_info_detail_panel_sbould_be_displayed() throws EnvironmentNotFoundException, PaasUserNotFoundException, ApplicationReleaseNotFoundException {
		// Create parameters for selectedEnvPage
		PageParameters pageParameters = new PageParameters();
		pageParameters.add("appUid", "12");
		pageParameters.add("releaseUid", "11");
		pageParameters.add("envUid", "toto");
		// Start EnvPage
		myTester.startPage(SelectedEnvironmentPage.class, pageParameters);
		// Click on wspInfoBtn
		myTester.executeAjaxEvent("envDetailsPanel:env-design:matrixContainer:listRows:1:listCols:0:content:cell-wspInfo", "onclick");
		// Assert there are values displayed
		List<LinkDto> linkDtos = getWspLinkDtosMap().get(logicalDeployment.listLogicalServices().get(0).getName());
		Map<String, String> serviceBindings = linkDtos.get(0).getServiceBindings();

		myTester.assertContains(serviceBindings.get("soap.service.subscription.projectcode"));
		myTester.assertContains(serviceBindings.get("soap.service.subscription.servicename"));
		myTester.assertContains(serviceBindings.get("soap.service.subscription.version"));
		myTester.assertContains(serviceBindings.get("soap.service.subscription.url"));
		myTester.assertContains(serviceBindings.get("soap.service.subscription.proxifiedserviceurl"));

	}

	@Test
	public void overalls_link_should_be_displayed() throws EnvironmentNotFoundException, PaasUserNotFoundException, ApplicationReleaseNotFoundException {
		// Create parameters for selectedEnvPage
		PageParameters pageParameters = new PageParameters();
		pageParameters.add("appUid", "12");
		pageParameters.add("releaseUid", "11");
		pageParameters.add("envUid", "toto");
		// Start EnvPage
		myTester.startPage(SelectedEnvironmentPage.class, pageParameters);

		// Assert there are values displayed
		assertThat(myTester.getTagById("logsURL").getAttribute("href")).isEqualTo("http://10.11.12.13/logs");
		assertThat(myTester.getTagById("monitoringURL").getAttribute("href")).isEqualTo("http://10.11.12.13/metrics");
	}

	@Test
	public void should_display_environment_id() throws EnvironmentNotFoundException, PaasUserNotFoundException, ApplicationReleaseNotFoundException {
		// Create parameters for selectedEnvPage
		PageParameters pageParameters = new PageParameters();
		pageParameters.add("appUid", "12");
		pageParameters.add("releaseUid", "11");
		pageParameters.add("envUid", "toto");
		// Start EnvPage
		myTester.startPage(SelectedEnvironmentPage.class, pageParameters);

		// Assert there are values displayed
		assertThat(myTester.getTagByWicketId("env-internal-name").getValue()).isEqualTo("fabulous-seb_test");
	}

	private EnvironmentDetailsDto createNewEnvironmentDetailsDto() {
		String uid = "env";
		String enviromentId = "fabulous-seb_test";
		String label = "label";
		String applicationLabel = "applicationLabel";
		String releaseUID = "releaseUid";
		String releaseVersion = EchoProviderLogicalModelCatalog.SOAP_SERVICE_MAJOR_VERSION + "-" + EchoProviderLogicalModelCatalog.SOAP_SERVICE_MINOR_VERSION;
		String ownerId = "testUserId";
		String ownerName = "testUserName";
		Date creationDate = new Date();
		EnvironmentDto.EnvironmentTypeEnum type = com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum.DEVELOPMENT;
		EnvironmentDto.EnvironmentStatusEnum status = EnvironmentDto.EnvironmentStatusEnum.CREATED;
		String statusMessage = "CREATED";
		int statusPercent = 100;
		String comment = "";
		String tdName = "tdName";
		EnvironmentDetailsDto environmentDetailsDto = new EnvironmentDetailsDto(uid, enviromentId, label, applicationLabel, releaseUID, releaseVersion, ownerId, ownerName,
				creationDate, type, status, statusMessage, statusPercent, comment, tdName);

		environmentDetailsDto.setLinkDtoMap(getWspLinkDtosMap());
		environmentDetailsDto.getLinkDtoMap().putAll(getEnvironmentOverallsLinks());

		return environmentDetailsDto;
	}

	private Map<String, List<LinkDto>> getEnvironmentOverallsLinks() {
		Map<String, List<LinkDto>> environmentOverallsLinks = new HashMap<String, List<LinkDto>>();

		LinkDto logsLink = new LinkDto();
		logsLink.setLinkType(LinkTypeEnum.LOGS_LINK);
		try {
			logsLink.setUrl(new URL("http://10.11.12.13/logs"));
		} catch (Exception e) {
			throw new TechnicalException(e);
		}

		LinkDto metricsLink = new LinkDto();
		;
		metricsLink.setLinkType(LinkTypeEnum.METRICS_LINK);
		try {
			metricsLink.setUrl(new URL("http://10.11.12.13/metrics"));
		} catch (Exception e) {
			throw new TechnicalException(e);
		}

		List<LinkDto> links = new ArrayList<LinkDto>();
		links.add(logsLink);
		links.add(metricsLink);

		environmentOverallsLinks.put("tdName", links);

		return environmentOverallsLinks;
	}

	private ApplicationRelease createApplicationRelease() {
		return new ApplicationRelease(createApplication(), "releaseVersion");
	}

	private Application createApplication() {
		Application application = new Application("appliLabel", "code");
		return application;
	}

	private LogicalDeployment createEchoProvider() {
		if (logicalDeployment == null) {
			logicalDeployment = echoProviderLogicalModelCatalog.populateLogicalDeployment(null);
		}
		return logicalDeployment;
	}

	/**
	 * Create an applicationContextMock to inject in Spring for Wicket
	 * 
	 * @return
	 */
	private ApplicationContextMock getApplicationContextMock() {
		ApplicationContextMock applicationContextMock = new ApplicationContextMock();
		applicationContextMock.putBean(manageEnvironment);
		applicationContextMock.putBean(manageApplication);
		applicationContextMock.putBean(manageApplicationRelease);
		applicationContextMock.putBean(managePaasUser);
		applicationContextMock.putBean(echoProviderLogicalModelCatalog);
		applicationContextMock.putBean(manageLogicalDeployment);
		applicationContextMock.putBean(delegatingDesignerServices);
		applicationContextMock.putBean(delegates);
		applicationContextMock.putBean(contactUsBean);
		applicationContextMock.putBean("authenticationManager",authenticationManager);
		return applicationContextMock;
	}

	private Map<String, List<LinkDto>> getWspLinkDtosMap() {

		LinkDto linkDto = new LinkDto();

		String project = "PROJECT";
		String serviceName = EchoProviderLogicalModelCatalog.SOAP_SERVICE_NAME;
		String serviceVersion = EchoProviderLogicalModelCatalog.SOAP_SERVICE_MAJOR_VERSION + "-" + EchoProviderLogicalModelCatalog.SOAP_SERVICE_MINOR_VERSION;
		String accessUrl = "http://10.0.0.1:9043/accessUrl";
		String proxifiedAccessUrl = "http://10.0.0.2:9043/proxifiedUrl";
		String logicalModelItemName = "logicalModelItem";
		String logicalModelItemId = logicalDeployment.listLogicalServices().get(1).getName();

		Map<String, String> serviceBinding = new HashMap<String, String>();
		serviceBinding.put("soap.service.subscription.projectcode", project);
		serviceBinding.put("soap.service.subscription.servicename", serviceName);
		serviceBinding.put("soap.service.subscription.version", serviceVersion);
		serviceBinding.put("soap.service.subscription.url", accessUrl);
		serviceBinding.put("soap.service.subscription.proxifiedserviceurl", proxifiedAccessUrl);
		serviceBinding.put("soap.service.subscription.name", logicalModelItemName);
		serviceBinding.put("soap.service.subscription.logicalmodelid", logicalModelItemId);

		try {
			linkDto.setUrl(new URL(accessUrl));
		} catch (MalformedURLException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
		linkDto.setLinkType(LinkDto.LinkTypeEnum.ACCESS_LINK);
		linkDto.setTargetUser(LinkDto.TargetUserEnum.PAAS_USER);
		linkDto.setServiceBindings(serviceBinding);

		List<LinkDto> linkDtos = new ArrayList<LinkDto>();
		linkDtos.add(linkDto);

		Map<String, List<LinkDto>> linkDtosMap = new HashMap<String, List<LinkDto>>();
		linkDtosMap.put(logicalDeployment.listLogicalServices().get(0).getName(), linkDtos);

		return linkDtosMap;
	}
}
