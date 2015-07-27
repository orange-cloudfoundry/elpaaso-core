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
package com.francetelecom.clara.cloud.core.service;

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.commons.AuthorizationException;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.EnvironmentNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.environment.impl.EnvironmentMapper;
import com.francetelecom.clara.cloud.environment.impl.ManageEnvironmentImpl;
import com.francetelecom.clara.cloud.environment.impl.ManageEnvironmentImplUtils;
import com.francetelecom.clara.cloud.model.*;
import com.francetelecom.clara.cloud.paas.activation.ManagePaasActivation;
import com.francetelecom.clara.cloud.paas.activation.TaskStatusActivation;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.Date;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test Business implementation for ManageEnvironment component
 *
 * @author lzxv3002
 */
@RunWith(MockitoJUnitRunner.class)
public class ManageEnvironmentImplTest {

    private static final PaasUser JOHN_LENNON = new PaasUser("john", "Lennon", new SSOId("john123"), "john@orange.com");

    protected static Logger LOG = LoggerFactory.getLogger(ManageEnvironmentImplTest.class);

    @Spy
    ManageEnvironmentImpl manageEnvironment = new ManageEnvironmentImpl();
    @Mock
    ManageEnvironmentImplUtils manageEnvironmentImplUtilsMock;
    @Mock
    ManagePaasActivation managePaasActivationMock;
    @Mock
    TechnicalDeploymentRepository technicalDeploymentRepository;
    @Mock
    EnvironmentRepository environnementRepository;
    @Mock
    ApplicationReleaseRepository releaseRepository;

    Environment environment;
    private final String generatedEnvUid = "generatedEnvUid";
    private final String environmentName = "environmentName";
    private ApplicationRelease release;

    @Before
    public void setup() throws ObjectNotFoundException, MalformedURLException, UnsupportedProjectionException {
        Application app = new Application("app", "basicat");
        release = new ApplicationRelease(app, "1.1");
        TechnicalDeploymentTestFactory technicalDeploymentTestFactory = new TechnicalDeploymentTestFactory();
        TechnicalDeployment td = technicalDeploymentTestFactory.createWicketJpaTD("foo", "foo.groupid:foo.artifactid:foo.version");
        td.setLogicalModelId("td");

        TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
        TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
        // create jonas
        // create db
        // create apache
        environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "My env", release, JOHN_LENNON, tdi);

        when(environnementRepository.findOne(Mockito.anyInt())).thenReturn(environment);
        when(environnementRepository.findByUid(Mockito.matches(environment.getUID()))).thenReturn(environment);
        manageEnvironment.setEnvironmentRepository(environnementRepository);

        when(manageEnvironmentImplUtilsMock.createTDI(anyString(), any(DeploymentProfileEnum.class), anyString(), anyString(), anyListOf(String.class))).thenReturn(
                generatedEnvUid);
        manageEnvironment.setUtils(manageEnvironmentImplUtilsMock);

        when(managePaasActivationMock.activate(anyInt())).thenReturn(new TaskStatusActivation());
        manageEnvironment.setManagePaasActivation(managePaasActivationMock);


        when(releaseRepository.findByUID(release.getUID())).thenReturn(release);
        manageEnvironment.setApplicationReleaseRepository(releaseRepository);

        manageEnvironment.setEnvironmentMapper(new EnvironmentMapper());
    }

    @After
    public void teardown() {
        // logout
        TestHelper.logout();
    }

    @Test
    public void testEnvironmentDto() throws EnvironmentNotFoundException {
        TestHelper.loginAsAdmin();
        EnvironmentDto dto = manageEnvironment.findEnvironmentByUID(environment.getUID());
        Assert.assertEquals(environment.getUID(), dto.getUid());
        Assert.assertEquals(environment.getLabel(), dto.getLabel());
        Assert.assertEquals(EnvironmentTypeEnum.DEVELOPMENT, dto.getType());
        Assert.assertEquals(EnvironmentStatusEnum.CREATING, dto.getStatus());
        Assert.assertEquals("john123", dto.getOwnerId()); // SSOID
    }

    @Test(expected = EnvironmentNotFoundException.class)
    public void fail_to_find_unknown_environment() throws EnvironmentNotFoundException {
        TestHelper.loginAsAdmin();
        Mockito.when(environnementRepository.findByUid(environment.getUID())).thenReturn(null);
        manageEnvironment.findEnvironmentByUID(environment.getUID());
    }

    @Test(expected = ApplicationReleaseNotFoundException.class)
    public void fail_to_find_environments_by_unknown_release() throws ApplicationReleaseNotFoundException {
        TestHelper.loginAsAdmin();
        Mockito.when(releaseRepository.findByUID(release.getUID())).thenReturn(null);
        manageEnvironment.findEnvironmentsByAppRelease(release.getUID());
    }

    @Test
    public void non_admin_user_should_see_details_of_environment_of_public_application_he_is_not_a_member_of() throws EnvironmentNotFoundException {
        TestHelper.loginAsUser();
        Mockito.when(environnementRepository.findByUid(environment.getUID())).thenReturn(environment);
        manageEnvironment.findEnvironmentByUID(environment.getUID());
    }

    @Test(expected = AuthorizationException.class)
    @Ignore
    public void non_admin_user_fails_to_see_details_of_environment_of_private_application_he_is_not_a_member_of() throws EnvironmentNotFoundException {
        TestHelper.loginAsUser();
        Mockito.when(environnementRepository.findByUid(environment.getUID())).thenReturn(environment);
        manageEnvironment.findEnvironmentByUID(environment.getUID());
    }

    @Test
    @Ignore
    public void non_admin_user_should_see_ops_details_of_environment_of_public_application_he_is_not_a_member_of() throws EnvironmentNotFoundException {
        TestHelper.loginAsUser();
        Mockito.when(environnementRepository.findByUid(environment.getUID())).thenReturn(environment);
        manageEnvironment.findEnvironmentOpsDetailsByUID(environment.getUID());
    }

    @Test(expected = AuthorizationException.class)
    @Ignore
    public void non_admin_user_fails_to_see_ops_details_of_environment_of_private_application_he_is_not_a_member_of() throws EnvironmentNotFoundException {
        TestHelper.loginAsUser();
        Mockito.when(environnementRepository.findByUid(environment.getUID())).thenReturn(environment);
        manageEnvironment.findEnvironmentOpsDetailsByUID(environment.getUID());
    }


    @Test
    public void admin_user_should_update_environment_comment() {
        TestHelper.loginAsAdmin();
        EnvironmentDetailsDto environmentDetailsDto = new EnvironmentDetailsDto(environment.getUID(), environment.getInternalName(), "label", "applicationLabel",
                "releaseUid", "releaseVersion", "ownerId", "ownerName", new Date(), EnvironmentTypeEnum.DEVELOPMENT,
                EnvironmentStatusEnum.CREATED, "statusMessage", 100, "comment", "tdiTdName");
        Environment updated = manageEnvironment.update(environmentDetailsDto);
        Assert.assertEquals(updated.getComment(), "comment");
    }

    @Test(expected = AuthorizationException.class)
    public void non_admin_user_fails_to_update_environment_comment() {
        TestHelper.loginAsUser();
        EnvironmentDetailsDto environmentDetailsDto = new EnvironmentDetailsDto(environment.getUID(), environment.getInternalName(), "label", "applicationLabel",
                "releaseUid", "releaseVersion", "ownerId", "ownerName", new Date(), EnvironmentTypeEnum.DEVELOPMENT,
                EnvironmentStatusEnum.CREATED, "statusMessage", 100, "comment", "tdiTdName");
        Environment updated = manageEnvironment.update(environmentDetailsDto);
        Assert.assertEquals(updated.getComment(), "comment");
    }

    private Environment createEnvMock() {
        TechnicalDeployment td = new TechnicalDeployment("tdTest");
        TechnicalDeploymentInstance envTdiStub = new TechnicalDeploymentInstance(new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE), td);
        envTdiStub.setName(environmentName);

        Environment justCreatedEnvMock = mock(Environment.class);
        when(justCreatedEnvMock.getTechnicalDeploymentInstance()).thenReturn(envTdiStub);
        when(justCreatedEnvMock.getUID()).thenReturn(generatedEnvUid);
        when(environnementRepository.findByUid(generatedEnvUid)).thenReturn(justCreatedEnvMock);
        return justCreatedEnvMock;
    }

    @Test(expected = UnsupportedProjectionException.class)
    public void shouldFailToCreateEnvironmentWhenProjectionIsNotSupported() throws BusinessException {
        ManageEnvironmentImpl manageEnvironment = new ManageEnvironmentImpl();
        manageEnvironment.setEnvironmentRepository(environnementRepository);
        manageEnvironment.setUtils(manageEnvironmentImplUtilsMock);
        // Given tdi cannot be created due to projection error
        doThrow(new UnsupportedProjectionException()).when(manageEnvironmentImplUtilsMock).createTDI("releaseUid", DeploymentProfileEnum.DEVELOPMENT,
                "ownerSsoId", "label", null);
        // When I create environment of type development with label for release
        // releaseUid for owner ownerSsoId,
        manageEnvironment.createEnvironment("releaseUid", EnvironmentTypeEnum.DEVELOPMENT, "ownerSsoId", "label");
    }

    @Test(expected = RuntimeException.class)
    public void testCreateEnvironment_rejectsNullReleaseId_arg() throws BusinessException {
        // complete setup
        createEnvMock();
        // run test
        manageEnvironment.createEnvironment(null, EnvironmentTypeEnum.DEVELOPMENT, "user", "label");
    }

    @Test
    public void testCreateEnvironment_development_is_mapped_to_development() throws BusinessException {
        // complete setup
        createEnvMock();
        // run test
        manageEnvironment.createEnvironment("aRelease", EnvironmentTypeEnum.DEVELOPMENT, "user", "label");
        // assert
        verify(manageEnvironmentImplUtilsMock).createTDI(anyString(), eq(DeploymentProfileEnum.DEVELOPMENT), anyString(), anyString(), anyListOf(String.class));
    }

    @Test
    public void testCreateEnvironment_production_is_mapped_to_production() throws BusinessException {
        // complete setup
        createEnvMock();
        // run test
        manageEnvironment.createEnvironment("aRelease", EnvironmentTypeEnum.PRODUCTION, "user", "label");
        // assert
        verify(manageEnvironmentImplUtilsMock).createTDI(anyString(), eq(DeploymentProfileEnum.PRODUCTION), anyString(), anyString(), anyListOf(String.class));
    }

    @Test
    public void testCreateEnvironment_preprod_is_mapped_to_production() throws BusinessException {
        // complete setup
        createEnvMock();
        // run test
        manageEnvironment.createEnvironment("aRelease", EnvironmentTypeEnum.PRE_PROD, "user", "label");
        // assert
        verify(manageEnvironmentImplUtilsMock).createTDI(anyString(), eq(DeploymentProfileEnum.PRODUCTION), anyString(), anyString(), anyListOf(String.class));
    }

    @Test
    public void testCreateEnvironment_test_is_mapped_to_production() throws BusinessException {
        // complete setup
        createEnvMock();
        // run test
        manageEnvironment.createEnvironment("aRelease", EnvironmentTypeEnum.TEST, "user", "label");
        // assert
        verify(manageEnvironmentImplUtilsMock).createTDI(anyString(), eq(DeploymentProfileEnum.PRODUCTION), anyString(), anyString(), anyListOf(String.class));
    }

    @Test
    public void testCreateEnvironment_loadtest_is_mapped_to_production() throws BusinessException {
        // complete setup
        createEnvMock();
        // run test
        manageEnvironment.createEnvironment("aRelease", EnvironmentTypeEnum.LOAD_TEST, "user", "label");
        // assert
        verify(manageEnvironmentImplUtilsMock).createTDI(anyString(), eq(DeploymentProfileEnum.PRODUCTION), anyString(), anyString(), anyListOf(String.class));
    }


    @Test
    public void admin_users_see_all_environments() {
        TestHelper.loginAsAdmin();

        manageEnvironment.findEnvironments();

        Mockito.verify(environnementRepository).findAllActive();
    }

    @Test
    public void admin_users_see_all_environments_of_a_given_release() throws ApplicationReleaseNotFoundException {
        TestHelper.loginAsAdmin();

        manageEnvironment.findEnvironmentsByAppRelease(release.getUID());

        Mockito.verify(environnementRepository).findAllActiveByApplicationReleaseUid(release.getUID());
    }

    @Test
    public void non_admin_users_see_environments_he_is_member_of() {
        // given Bob is authenticated
        TestHelper.loginAsUser();

        manageEnvironment.findEnvironments();

        Mockito.verify(environnementRepository).findAllPublicOrPrivateByMember(TestHelper.USER_WITH_USER_ROLE_SSOID.getValue());
    }

    @Test
    public void non_admin_users_see_all_environments_of_a_given_release() throws ApplicationReleaseNotFoundException {

        TestHelper.loginAsUser();

        manageEnvironment.findEnvironmentsByAppRelease(release.getUID());

        Mockito.verify(environnementRepository).findAllPublicOrPrivateByMemberAndByApplicationRelease(release.getUID(), TestHelper.USER_WITH_USER_ROLE_SSOID.getValue());
    }

    @Test
    public void admin_users_see_its_environments() {
        TestHelper.loginAsAdmin();

        manageEnvironment.findMyEnvironments();

        Mockito.verify(environnementRepository).findAllActiveByApplicationMember(TestHelper.USER_WITH_ADMIN_ROLE_SSOID.getValue());
    }

    @Test
    public void non_admin_users_see_its_environments() {
        // given Bob is authenticated
        TestHelper.loginAsUser();

        manageEnvironment.findMyEnvironments();

        Mockito.verify(environnementRepository).findAllActiveByApplicationMember(TestHelper.USER_WITH_USER_ROLE_SSOID.getValue());
    }

    @Test
    public void admin_users_count_all_environments() {
        TestHelper.loginAsAdmin();

        manageEnvironment.countEnvironments();

        Mockito.verify(environnementRepository).countActive();
    }

    @Test
    public void non_admin_users_count_environments_he_is_member_of() {
        // given Bob is authenticated
        TestHelper.loginAsUser();

        manageEnvironment.countEnvironments();

        Mockito.verify(environnementRepository).countPublicOrPrivateByMember(TestHelper.USER_WITH_USER_ROLE_SSOID.getValue());
    }

    @Test
    public void admin_users_count_its_environments() {
        TestHelper.loginAsAdmin();

        manageEnvironment.countMyEnvironments();

        Mockito.verify(environnementRepository).countActiveByApplicationMember(TestHelper.USER_WITH_ADMIN_ROLE_SSOID.getValue());
    }

    @Test
    public void non_admin_users_count_its_environments() {
        TestHelper.loginAsUser();

        manageEnvironment.countMyEnvironments();

        Mockito.verify(environnementRepository).countActiveByApplicationMember(TestHelper.USER_WITH_USER_ROLE_SSOID.getValue());
    }

    @Test
    public void admin_users_count_environments_of_a_release() throws ApplicationReleaseNotFoundException {
        TestHelper.loginAsAdmin();
        Mockito.when(releaseRepository.findByUID("uid")).thenReturn(release);

        manageEnvironment.countEnvironmentsByApplicationRelease("uid");

        Mockito.verify(environnementRepository).countActiveByApplicationReleaseUid("uid");
    }

    @Test
    public void non_admin_users_count_environments_of_a_release() throws ApplicationReleaseNotFoundException {
        TestHelper.loginAsUser();
        Mockito.when(releaseRepository.findByUID("uid")).thenReturn(release);

        manageEnvironment.countEnvironmentsByApplicationRelease("uid");

        Mockito.verify(environnementRepository).countAllPublicOrPrivateByMemberAndByApplicationRelease("uid", TestHelper.USER_WITH_USER_ROLE_SSOID.getValue());
    }

}