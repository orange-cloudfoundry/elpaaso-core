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
package com.francetelecom.clara.cloud.environment.impl;

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.commons.AuthorizationException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.environment.log.BaseSearchURL;
import com.francetelecom.clara.cloud.environment.log.LogService;
import com.francetelecom.clara.cloud.environment.log.LogServiceSplunkImpl;
import com.francetelecom.clara.cloud.model.*;
import com.francetelecom.clara.cloud.paas.activation.ManagePaasActivation;
import com.francetelecom.clara.cloud.paas.activation.TaskStatusActivation;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import com.francetelecom.clara.cloud.coremodel.exception.EnvironmentNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Business implementation for ManageEnvironment component
 */
@RunWith(MockitoJUnitRunner.class)
public class ManageEnvironmentImplOverallsLinksTest {

    private static final PaasUser JOHN_LENNON = new PaasUser("john", "Lennon", new SSOId("john123"), "john@orange.com");

    protected static Logger LOG = LoggerFactory.getLogger(ManageEnvironmentImplOverallsLinksTest.class);
    public static final String SPLUNK_IP = "10.170.232.227";
    public static final String SPLUNK_PORT = "8080";

    @Spy
    ManageEnvironmentImpl manageEnvironment = new ManageEnvironmentImpl();
    @Mock
    ManageEnvironmentImplUtils manageEnvironmentImplUtilsMock;
    @Mock
    ManagePaasActivation managePaasActivationMock;
    @Mock
    TechnicalDeploymentRepository technicalDeploymentRepository;
    @Mock
    EnvironmentRepository environmentRepository;

    Environment environment;
    private final String generatedEnvUid = "generatedEnvUid";
    private final String environmentName = "environmentName";

    @Before
    public void setup() throws ObjectNotFoundException, MalformedURLException, UnsupportedProjectionException {
        //login as admin
        TestHelper.loginAsAdmin();

        Application app = new Application("app", "basicat");
        ApplicationRelease release = new ApplicationRelease(app, "1.1");
        TechnicalDeploymentTestFactory technicalDeploymentTestFactory = new TechnicalDeploymentTestFactory();
        TechnicalDeployment td = technicalDeploymentTestFactory.createWicketJpaTD("foo", "foo.groupid:foo.artifactid:foo.version");
        td.setLogicalModelId("td");
        TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
        TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
        // create jonas
        // create db
        // create apache

        environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "My env", release, JOHN_LENNON, tdi);

        // configure mocks
        manageEnvironment.setTechnicalDeploymentRepository(technicalDeploymentRepository);

        when(environmentRepository.find(anyInt())).thenReturn(environment);
        when(environmentRepository.findByUID(Mockito.matches(environment.getUID()))).thenReturn(environment);
        manageEnvironment.setEnvironmentRepository(environmentRepository);

        when(manageEnvironmentImplUtilsMock.createTDI(anyString(), any(DeploymentProfileEnum.class), anyString(), anyString(), anyListOf(String.class))).thenReturn(generatedEnvUid);
        manageEnvironment.setUtils(manageEnvironmentImplUtilsMock);

        when(managePaasActivationMock.activate(anyInt())).thenReturn(new TaskStatusActivation());
        manageEnvironment.setManagePaasActivation(managePaasActivationMock);

        LogService logService = new LogServiceSplunkImpl(new BaseSearchURL(SPLUNK_IP, SPLUNK_PORT, false));

        manageEnvironment.setLogService(logService);

    }

    @After
    public void teardown() {
        //logout
        TestHelper.logout();
    }

    private Environment createEnvMock() {
        TechnicalDeployment td = new TechnicalDeployment("tdTest");
        Space space = new Space(td);
        space.activate(new SpaceName("joynspace"));
        App joyn = new App(td, space, Mockito.mock(MavenReference.class), "joyn");
        joyn.activate(UUID.fromString("55dd956f-05b3-462c-aa5f-10aba84cd82d"));

        TechnicalDeploymentInstance envTdiStub = new TechnicalDeploymentInstance(new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE), td);
        envTdiStub.setName(environmentName);

        Environment justCreatedEnvMock = mock(Environment.class);
        when(justCreatedEnvMock.getTechnicalDeploymentInstance()).thenReturn(envTdiStub);
        when(justCreatedEnvMock.getUID()).thenReturn(generatedEnvUid);
        when(environmentRepository.findByUID(generatedEnvUid)).thenReturn(justCreatedEnvMock);
        return justCreatedEnvMock;
    }

    private Environment createEnvDetailedMock() {
        Environment justCreatedEnvMock = createEnvMock();

        Application mockedApp = mock(Application.class);
        ApplicationRelease mockedAppRelease = mock(ApplicationRelease.class);
        PaasUser mockedPaasUser = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
        when(justCreatedEnvMock.getApplicationRelease()).thenReturn(mockedAppRelease);
        when(mockedAppRelease.getApplication()).thenReturn(mockedApp);
        when(justCreatedEnvMock.getPaasUser()).thenReturn(mockedPaasUser);
        when(justCreatedEnvMock.getType()).thenReturn(DeploymentProfileEnum.DEVELOPMENT);
        when(justCreatedEnvMock.getStatus()).thenReturn(EnvironmentStatus.CREATING);
        return justCreatedEnvMock;
    }

    @Test(expected = AuthorizationException.class)
    public void non_admin_user_fails_to_see_details_of_environment_of_application_he_is_not_a_member_of() throws EnvironmentNotFoundException {
        TestHelper.loginAsUser();
        // Given
        Environment envMock = createEnvDetailedMock();

        Mockito.when(environmentRepository.findByUID(envMock.getUID())).thenReturn(envMock);

        manageEnvironment.findEnvironmentDetails(envMock.getUID());
    }

    @Test
    public void environment_details_dto_should_include_splunk_link_for_environment_overalls() throws ObjectNotFoundException {
        // Given
        Environment envMock = createEnvDetailedMock();
        // when
        EnvironmentDetailsDto environmentDetails = manageEnvironment.findEnvironmentDetails(envMock.getUID());
        LinkDto overallsLinkDto = environmentDetails.getEnvironmentOverallsLinkDto();
        // then
        assertThat(overallsLinkDto).as("overallsLinkDto should not be null").isNotNull();
        LOG.info("overallsLinkDto : {}", overallsLinkDto);
        assertThat(overallsLinkDto.getUrl()).as("overallsLinkDtoUrl should not be null").isNotNull();
        StringBuilder expectedSplunkOverallsSearch = new StringBuilder
                ("http://").append(SPLUNK_IP).append(":").append(SPLUNK_PORT)
                .append("/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20");
        expectedSplunkOverallsSearch.append("index%3D%22*%22+source%3D%22tcp%3A12345%22+appname%3D%2255dd956f-05b3-462c-aa5f-10aba84cd82d%22");
        assertThat(overallsLinkDto.getUrl().toString()).isEqualTo(expectedSplunkOverallsSearch.toString());
    }

    @Test
    public void environment_details_dto_should_include_logs_link_for_environment_overalls() throws Exception {
        // Given
        Environment envMock = createEnvDetailedMock();
        // when
        EnvironmentDetailsDto environmentDetails = manageEnvironment.findEnvironmentDetails(envMock.getUID());
        URL logsURL = environmentDetails.getURLLinkFromType(LinkDto.LinkTypeEnum.LOGS_LINK);
        // then
        StringBuilder expectedSplunkOverallsSearch = new StringBuilder
                ("http://").append(SPLUNK_IP).append(":").append(SPLUNK_PORT)
                .append("/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20");
        expectedSplunkOverallsSearch.append("index%3D%22*%22+source%3D%22tcp%3A12345%22+appname%3D%2255dd956f-05b3-462c-aa5f-10aba84cd82d%22");
        assertThat(logsURL.toString()).isEqualTo(expectedSplunkOverallsSearch.toString());
    }

    @Test
    public void environment_details_dto_should_splunk_logs_links_with_ids() throws ObjectNotFoundException, MalformedURLException {
        EnvironmentDetailsDto dto = manageEnvironment.findEnvironmentDetails(environment.getUID());
        List<LinkDto> links = dto.getSpecificLinkDto(LinkDto.LinkTypeEnum.LOGS_LINK);
        Assert.assertEquals(1, links.size());
        boolean envOK = false;
        for (LinkDto link : links) {
            LOG.info("Log link: " + link.getTargetUser() + " -> " + link.getUrl().toString());
            envOK = envOK || link.getUrl().toString().contains("appname");
        }
        Assert.assertTrue("At least one log link has not been found", envOK);
    }
}
