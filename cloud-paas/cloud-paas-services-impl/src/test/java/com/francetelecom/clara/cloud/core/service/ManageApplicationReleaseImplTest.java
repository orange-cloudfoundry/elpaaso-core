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
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.core.domain.ApplicationReleaseRepository;
import com.francetelecom.clara.cloud.core.domain.ApplicationRepository;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.PaasUserRepository;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplateRepository;
import com.francetelecom.clara.cloud.paas.projection.ProjectionService;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.PaasUserNotFoundException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Test Business implementation for ApplicationRelease component
 *
 * @author Clara
 */
@RunWith(MockitoJUnitRunner.class)
public class ManageApplicationReleaseImplTest {

    private static final PaasUser JOE_DALTON = new PaasUser("Joe", "Dalton", new SSOId("jdalton"), "joe.dalton@alcatraz.com");

    ManageApplicationReleaseImpl manageApplicationRelease;

    @Mock
    ApplicationReleaseRepository applicationReleaseRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    TechnicalDeploymentTemplateRepository deploymentTemplateRepository;

    @Mock
    PaasUserRepository paasUserRepository;

    @Mock
    EnvironmentRepository environmentRepositoryMock;

    @Mock
    private ProjectionService projectionServiceMock;

    @Before
    public void setup() {
        manageApplicationRelease = new ManageApplicationReleaseImpl();
        manageApplicationRelease.setApplicationReleaseRepository(applicationReleaseRepository);
        manageApplicationRelease.setTechnicalDeploymentTemplateRepository(deploymentTemplateRepository);
        manageApplicationRelease.setPaasUserRepository(paasUserRepository);
        manageApplicationRelease.setApplicationRepository(applicationRepository);
        manageApplicationRelease.setEnvironmentRepository(environmentRepositoryMock);
        manageApplicationRelease.setProjectionService(projectionServiceMock);

    }

    @After
    public void cleanSecurityContext() {
        TestHelper.logout();
        Mockito.reset(applicationReleaseRepository, applicationReleaseRepository, applicationRepository, deploymentTemplateRepository, paasUserRepository, environmentRepositoryMock, projectionServiceMock);
    }

    @Test(expected = ApplicationReleaseNotFoundException.class)
    public void ShouldFailToDeleteUnknownApplicationRelease() throws ApplicationReleaseNotFoundException {
        // given no release with uid unknown exists
        Mockito.when(applicationReleaseRepository.findByUID("unknown")).thenReturn(null);
        // when I delete release with uid unknown
        manageApplicationRelease.deleteApplicationRelease("unknown");
        // then It should failed
    }

    @Test(expected = AuthorizationException.class)
    public void non_admin_user_fail_to_create_a_release_of_public_application_she_is_not_a_member_of() throws ApplicationReleaseNotFoundException, PaasUserNotFoundException,
            ApplicationNotFoundException, DuplicateApplicationReleaseException {
        // given non admin performs following actions
        TestHelper.loginAsUser();
        // given elpaaso public application
        Application elpaaso = new Application("elpaaso", "elpaaso");
        // given user JOE_DALTON exists
        Mockito.when(paasUserRepository.findBySsoId(JOE_DALTON.getSsoId())).thenReturn(JOE_DALTON);
        // given application elpaaso exists
        Mockito.when(applicationRepository.findByUid(elpaaso.getUID())).thenReturn(elpaaso);
        // when user create release 1.0 of application elpaaso
        manageApplicationRelease.createApplicationRelease(elpaaso.getUID(), JOE_DALTON.getSsoId().getValue(), "1.0", null, null, null);
        // then it should be authorized
    }

    @Test
    public void non_admin_user_can_create_a_release_of_private_application_she_is_a_member_of() throws ApplicationReleaseNotFoundException, PaasUserNotFoundException,
            ApplicationNotFoundException, DuplicateApplicationReleaseException {
        // given non admin performs following actions
        TestHelper.loginAsUser();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_USER_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given user JOE_DALTON exists
        Mockito.when(paasUserRepository.findBySsoId(JOE_DALTON.getSsoId())).thenReturn(JOE_DALTON);
        // given application elpaaso exists
        Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
        // when user create release 1.0 of application joyn
        manageApplicationRelease.createApplicationRelease(joyn.getUID(), JOE_DALTON.getSsoId().getValue(), "1.0", null, null, null);
        // then it should be authorized
    }

    @Test(expected = AuthorizationException.class)
    public void non_admin_user_fail_to_create_a_release_of_private_application_she_is_not_a_member_of() throws ApplicationReleaseNotFoundException, PaasUserNotFoundException,
            ApplicationNotFoundException, DuplicateApplicationReleaseException {
        // given non admin performs following actions
        TestHelper.loginAsUser();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_ADMIN_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given user JOE_DALTON exists
        Mockito.when(paasUserRepository.findBySsoId(JOE_DALTON.getSsoId())).thenReturn(JOE_DALTON);
        // given application elpaaso exists
        Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
        // when user create release 1.0 of application joyn
        manageApplicationRelease.createApplicationRelease(joyn.getUID(), JOE_DALTON.getSsoId().getValue(), "1.0", null, null, null);
        // then it should be authorized
    }

    @Test
    public void admin_user_can_create_a_release_of_public_application() throws ApplicationReleaseNotFoundException, PaasUserNotFoundException, ApplicationNotFoundException,
            DuplicateApplicationReleaseException {
        // given non admin performs following actions
        TestHelper.loginAsAdmin();
        // given elpaaso public application
        Application elpaaso = new Application("elpaaso", "elpaaso");
        // given user JOE_DALTON exists
        Mockito.when(paasUserRepository.findBySsoId(JOE_DALTON.getSsoId())).thenReturn(JOE_DALTON);
        // given application elpaaso exists
        Mockito.when(applicationRepository.findByUid(elpaaso.getUID())).thenReturn(elpaaso);
        // when user create release 1.0 of application elpaaso
        manageApplicationRelease.createApplicationRelease(elpaaso.getUID(), JOE_DALTON.getSsoId().getValue(), "1.0", null, null, null);
        // then it should be authorized
    }

    @Test
    public void admin_user_can_create_a_release_of_private_application_she_is_a_member_of() throws ApplicationReleaseNotFoundException, PaasUserNotFoundException,
            ApplicationNotFoundException, DuplicateApplicationReleaseException {
        // given non admin performs following actions
        TestHelper.loginAsAdmin();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_ADMIN_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);

        // given user JOE_DALTON exists
        Mockito.when(paasUserRepository.findBySsoId(JOE_DALTON.getSsoId())).thenReturn(JOE_DALTON);
        // given application elpaaso exists
        Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
        // when user create release 1.0 of application joyn
        manageApplicationRelease.createApplicationRelease(joyn.getUID(), JOE_DALTON.getSsoId().getValue(), "1.0", null, null, null);
        // then it should be authorized
    }

    @Test
    public void admin_user_can_create_a_release_of_private_application_she_is_not_a_member_of() throws ApplicationReleaseNotFoundException, PaasUserNotFoundException,
            ApplicationNotFoundException, DuplicateApplicationReleaseException {
        // given non admin performs following actions
        TestHelper.loginAsAdmin();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_USER_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers); // given user JOE_DALTON exists
        Mockito.when(paasUserRepository.findBySsoId(JOE_DALTON.getSsoId())).thenReturn(JOE_DALTON);
        // given application elpaaso exists
        Mockito.when(applicationRepository.findByUid(joyn.getUID())).thenReturn(joyn);
        // when user create release 1.0 of application joyn
        manageApplicationRelease.createApplicationRelease(joyn.getUID(), JOE_DALTON.getSsoId().getValue(), "1.0", null, null, null);
        // then it should be authorized
    }

    @Test(expected = AuthorizationException.class)
    public void non_admin_user_fail_to_update_a_release_of_public_application_she_is_not_a_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsUser();
        // given elpaaso public application
        Application elpaaso = new Application("elpaaso", "elpaaso");
        // given release 1.0 of application elpaaso
        ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
        // given release with version aVersion exists
        Mockito.when(applicationReleaseRepository.findByUID(elpaaso_1_0.getUID())).thenReturn(elpaaso_1_0);
        // when I update release elpaaso_1_0
        manageApplicationRelease.updateApplicationRelease(elpaaso_1_0);
        // then it should be authorized
    }

    @Test
    public void non_admin_user_can_update_a_release_of_private_application_she_is_a_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsUser();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_USER_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given release 1.0 of application joyn
        ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
        // ApplicationRelease spy = Mockito.spy(release);
        Mockito.when(applicationReleaseRepository.findByUID(joyn_1_0.getUID())).thenReturn(joyn_1_0);
        // when I update joyn_1_0
        manageApplicationRelease.updateApplicationRelease(joyn_1_0);
        // then it should be authorized
    }

    @Test(expected = AuthorizationException.class)
    public void non_admin_user_fail_to_update_a_release_of_private_application_she_is_not_a_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsUser();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_ADMIN_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given release 1.0 of application joyn
        ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
        // ApplicationRelease spy = Mockito.spy(release);
        Mockito.when(applicationReleaseRepository.findByUID(joyn_1_0.getUID())).thenReturn(joyn_1_0);
        // when I update joyn_1_0
        manageApplicationRelease.updateApplicationRelease(joyn_1_0);
        // then it should be authorized
    }

    @Test
    public void admin_user_can_update_a_release_of_public_application() throws ApplicationReleaseNotFoundException {
        // given admin performs following actions
        TestHelper.loginAsAdmin();
        // given elpaaso public application
        Application elpaaso = new Application("elpaaso", "elpaaso");
        // given release 1.0 of application elpaaso
        ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
        // given release with version aVersion exists
        Mockito.when(applicationReleaseRepository.findByUID(elpaaso_1_0.getUID())).thenReturn(elpaaso_1_0);
        // when I update release elpaaso_1_0
        manageApplicationRelease.updateApplicationRelease(elpaaso_1_0);
        // then it should be authorized
    }

    @Test
    public void admin_user_can_update_a_release_of_private_application_she_is_a_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsAdmin();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_ADMIN_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given release 1.0 of application joyn
        ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
        // ApplicationRelease spy = Mockito.spy(release);
        Mockito.when(applicationReleaseRepository.findByUID(joyn_1_0.getUID())).thenReturn(joyn_1_0);
        // when I update joyn_1_0
        manageApplicationRelease.updateApplicationRelease(joyn_1_0);
        // then it should be authorized
    }

    @Test
    public void admin_user_can_update_a_release_of_private_application_she_is_not_a_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsAdmin();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_USER_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given release 1.0 of application joyn
        ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
        // ApplicationRelease spy = Mockito.spy(release);
        Mockito.when(applicationReleaseRepository.findByUID(joyn_1_0.getUID())).thenReturn(joyn_1_0);
        // when I update joyn_1_0
        manageApplicationRelease.updateApplicationRelease(joyn_1_0);
        // then it should be authorized
    }

    @Test(expected = AuthorizationException.class)
    public void non_admin_user_fail_to_delete_a_release_of_public_application_she_is_not_a_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsUser();
        // given elpaaso public application
        Application elpaaso = new Application("elpaaso", "elpaaso");
        // given release 1.0 of application elpaaso
        ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
        // given release with version aVersion exists
        Mockito.when(applicationReleaseRepository.findByUID(elpaaso_1_0.getUID())).thenReturn(elpaaso_1_0);
        // given release with version aVersion has no active environment
        Mockito.when((environmentRepositoryMock).countActiveByApplicationReleaseUid(elpaaso_1_0.getUID())).thenReturn(new Long(0));
        // when I delete release with version aVersion
        manageApplicationRelease.deleteApplicationRelease(elpaaso_1_0.getUID());
        // then it should be authorized
    }

    @Test
    public void admin_user_can_delete_a_release_of_public_application() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsAdmin();
        // given elpaaso public application
        Application elpaaso = new Application("elpaaso", "elpaaso");
        // given release 1.0 of application elpaaso
        ApplicationRelease elpaaso_1_0 = new ApplicationRelease(elpaaso, "1.0");
        ApplicationRelease spy = Mockito.spy(elpaaso_1_0);
        // given release with version aVersion exists
        Mockito.when(applicationReleaseRepository.findByUID(spy.getUID())).thenReturn(spy);
        // given release with version aVersion has no active environment
        Mockito.when((environmentRepositoryMock).countActiveByApplicationReleaseUid(spy.getUID())).thenReturn(new Long(0));
        // when I delete release with version aVersion
        manageApplicationRelease.deleteApplicationRelease(spy.getUID());
        // then it should be authorized
        Mockito.verify(spy).markAsRemoved();
    }

    @Test
    public void non_admin_user_can_delete_a_release_of_private_application_she_is_a_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsUser();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_USER_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given release 1.0 of application joyn
        ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
        // ApplicationRelease spy = Mockito.spy(release);
        Mockito.when(applicationReleaseRepository.findByUID(joyn_1_0.getUID())).thenReturn(joyn_1_0);
        // given release with version aVersion has no active environment
        Mockito.when((environmentRepositoryMock).countActiveByApplicationReleaseUid(joyn_1_0.getUID())).thenReturn(new Long(0));
        // when I delete release with version aVersion
        manageApplicationRelease.deleteApplicationRelease(joyn_1_0.getUID());
        // then it should be authorized
    }

    @Test
    public void admin_user_can_delete_a_release_of_private_application_she_is_a_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsAdmin();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_ADMIN_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given release 1.0 of application joyn
        ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
        // ApplicationRelease spy = Mockito.spy(release);
        Mockito.when(applicationReleaseRepository.findByUID(joyn_1_0.getUID())).thenReturn(joyn_1_0);
        // given release with version aVersion has no active environment
        Mockito.when((environmentRepositoryMock).countActiveByApplicationReleaseUid(joyn_1_0.getUID())).thenReturn(new Long(0));
        // when I delete release with version aVersion
        manageApplicationRelease.deleteApplicationRelease(joyn_1_0.getUID());
        // then it should be authorized
    }

    @Test(expected = AuthorizationException.class)
    public void non_admin_user_fail_to_delete_a_release_of_private_application_she_is_not_a_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsUser();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_ADMIN_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given release 1.0 of application joyn
        ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
        // ApplicationRelease spy = Mockito.spy(release);
        Mockito.when(applicationReleaseRepository.findByUID(joyn_1_0.getUID())).thenReturn(joyn_1_0);
        // given release with version aVersion has no active environment
        Mockito.when((environmentRepositoryMock).countActiveByApplicationReleaseUid(joyn_1_0.getUID())).thenReturn(new Long(0));
        // when I delete release with version aVersion
        manageApplicationRelease.deleteApplicationRelease(joyn_1_0.getUID());
        // then it should not be authorized
    }

    @Test
    public void admin_user_can_delete_a_release_of_private_application_she_is_a_not_member_of() throws ApplicationReleaseNotFoundException {
        // given non admin performs following actions
        TestHelper.loginAsAdmin();
        // given joyn private application
        Application joyn = new Application("joyn", "joyn");
        HashSet<SSOId> joynMembers = new HashSet<>();
        joynMembers.add(TestHelper.USER_WITH_USER_ROLE_SSOID);
        joyn.setAsPrivate();
        joyn.setMembers(joynMembers);
        // given release 1.0 of application joyn
        ApplicationRelease joyn_1_0 = new ApplicationRelease(joyn, "1.0");
        // ApplicationRelease spy = Mockito.spy(release);
        Mockito.when(applicationReleaseRepository.findByUID(joyn_1_0.getUID())).thenReturn(joyn_1_0);
        // given release with version aVersion has no active environment
        Mockito.when((environmentRepositoryMock).countActiveByApplicationReleaseUid(joyn_1_0.getUID())).thenReturn(new Long(0));
        // when I delete release with version aVersion
        manageApplicationRelease.deleteApplicationRelease(joyn_1_0.getUID());
        // then it should be authorized
    }

    @Test(expected = IllegalStateException.class)
    public void ShouldFailToDeleteExistingApplicationReleaseWithActiveEnvironments() throws ApplicationReleaseNotFoundException {
        TestHelper.loginAsAdmin();
        // given release with version aVersion exists
        ApplicationRelease release = new ApplicationRelease(new Application("aLabel", "aCode"), "aVersion");
        Mockito.when(applicationReleaseRepository.findByUID(release.getUID())).thenReturn(release);
        // given application with label aLabel and code aCode has 2 active
        // environments
        Mockito.when((environmentRepositoryMock).countActiveByApplicationReleaseUid(release.getUID())).thenReturn(new Long(2));
        // when I delete release with version aVersion
        manageApplicationRelease.deleteApplicationRelease(release.getUID());
        // then It should failed
    }

    @Test(expected = ApplicationReleaseNotFoundException.class)
    public void testDeleteApplicationReleaseThrowsNotFoundException() throws TechnicalException, BusinessException {
        // test setup
        ApplicationReleaseRepository applicationReleaseDaoJpaMock = Mockito.mock(ApplicationReleaseRepository.class);
        manageApplicationRelease.setApplicationReleaseRepository(applicationReleaseDaoJpaMock);
        // mock setup
        Mockito.when(applicationReleaseDaoJpaMock.findByUID("999")).thenReturn(null);
        // test run
        manageApplicationRelease.deleteApplicationRelease("999");

    }

    @Test
    public void admin_users_see_all_releases() {
        TestHelper.loginAsAdmin();

        manageApplicationRelease.findApplicationReleases(0, 10);

        Mockito.verify(applicationReleaseRepository).findAll(0, 10);
    }

    @Test
    public void admin_users_see_all_releases_of_any_application() throws ApplicationNotFoundException {
        TestHelper.loginAsAdmin();

        Mockito.when(applicationRepository.findByUid("appUID")).thenReturn(new Application("aLabel", "aCode"));
        manageApplicationRelease.findApplicationReleasesByAppUID("appUID", 0, 10);

        Mockito.verify(applicationReleaseRepository).findApplicationReleasesByAppUID("appUID", 0, 10);
    }

    @Test
    public void non_admin_users_see_all_releases_of_a_public_application_or_an_application_she_is_member_of() throws ApplicationNotFoundException {
        TestHelper.loginAsUser();

        Mockito.when(applicationRepository.findByUid("appUID")).thenReturn(new Application("aLabel", "aCode"));
        manageApplicationRelease.findApplicationReleasesByAppUID("appUID", 0, 10);

        Mockito.verify(applicationReleaseRepository).findPublicOrPrivateByMemberAndByAppUID(TestHelper.USER_WITH_USER_ROLE_SSOID, "appUID", 0, 10);
    }

    @Test
    public void admin_users_count_all_releases() {
        // given Bob is authenticated
        TestHelper.loginAsAdmin();

        manageApplicationRelease.countApplicationReleases();

        Mockito.verify(applicationReleaseRepository).countApplicationReleases();
    }

    @Test
    public void non_admin_users_see_all_public_releases_and_private_application_they_are_member_of() {
        // given Alice is authenticated
        TestHelper.loginAsUser();

        manageApplicationRelease.findApplicationReleases(0, 10);

        Mockito.verify(applicationReleaseRepository).findAllPublicOrPrivateByMember(TestHelper.USER_WITH_USER_ROLE_SSOID, 0, 10);
    }

    @Test
    public void non_admin_users_count_all_public_releases_and_private_application_they_are_member_of() {
        TestHelper.loginAsUser();

        manageApplicationRelease.countApplicationReleases();

        Mockito.verify(applicationReleaseRepository).countPublicOrPrivateByMember(TestHelper.USER_WITH_USER_ROLE_SSOID);
    }

    @Test
    public void admin_users_see_releases_of_private_applications_they_are_member_of() {
        TestHelper.loginAsAdmin();

        manageApplicationRelease.findMyApplicationReleases(0, 10);

        Mockito.verify(applicationReleaseRepository).findAllByApplicationMember(TestHelper.USER_WITH_ADMIN_ROLE_SSOID, 0, 10);
    }

    @Test
    public void admin_users_see_all_releases_as_his_own() {
        TestHelper.loginAsAdmin();

        manageApplicationRelease.findMyApplicationReleases();

        Mockito.verify(applicationReleaseRepository).findAll();
    }

    @Test
    public void non_admin_users_see_private_releases_as_his_own() {
        TestHelper.loginAsUser();

        manageApplicationRelease.findMyApplicationReleases();

        Mockito.verify(applicationReleaseRepository).findAllByApplicationMember(TestHelper.USER_WITH_USER_ROLE_SSOID, 0, Integer.MAX_VALUE);
    }

    @Test
    public void admin_users_counnt_releases_of_private_applications_they_are_member_of() {
        TestHelper.loginAsAdmin();

        manageApplicationRelease.countMyApplicationReleases();

        Mockito.verify(applicationReleaseRepository).countByApplicationMember(TestHelper.USER_WITH_ADMIN_ROLE_SSOID);
    }

    @Test
    public void non_admin_users_see_releases_of_private_applications_they_are_member_of() {
        TestHelper.loginAsUser();

        manageApplicationRelease.findMyApplicationReleases(0, 10);

        Mockito.verify(applicationReleaseRepository).findAllByApplicationMember(TestHelper.USER_WITH_USER_ROLE_SSOID, 0, 10);
    }

    @Test
    public void non_admin_users_count_releases_of_private_applications_they_are_member_of() {
        TestHelper.loginAsUser();

        manageApplicationRelease.countMyApplicationReleases();

        Mockito.verify(applicationReleaseRepository).countByApplicationMember(TestHelper.USER_WITH_USER_ROLE_SSOID);
    }

    @Test
    public void shouldFindApplicationReleaseByUID() throws ApplicationReleaseNotFoundException {
        // given user with admin role performs actions
        TestHelper.loginAsAdmin();
        // given paas user with ssoId aSsoId exists
        Mockito.when(paasUserRepository.findBySsoId(new SSOId("aSsoId"))).thenReturn(JOE_DALTON);
        // given release with version aVersion with owner aSsoId exists
        ApplicationRelease release = new ApplicationRelease(new Application("aLabel", "aCode"), "aVersion");
        Mockito.when(applicationReleaseRepository.findByUID(release.getUID())).thenReturn(release);
        // when I find release by it uid
        ApplicationRelease result = manageApplicationRelease.findApplicationReleaseByUID(release.getUID());
        // then I should get release with version aVersion
        Assert.assertEquals(release, result);
    }

    @Test(expected = ApplicationReleaseNotFoundException.class)
    public void shouldFailToFindApplicationReleaseByUnknownUID() throws ApplicationReleaseNotFoundException {
        // given no release with uid unknown exists
        Mockito.when(applicationReleaseRepository.findByUID("unknown")).thenReturn(null);
        // when I find release by it uid
        manageApplicationRelease.findApplicationReleaseByUID("unknown");
        // then It should fail
    }

    @Test
    public void shouldUpdateExistingApplicationRelease() throws ApplicationReleaseNotFoundException {
        TestHelper.loginAsAdmin();
        // given release with version aVersion exists
        ApplicationRelease release = new ApplicationRelease(new Application("aLabel", "aCode"), "aVersion");
        Mockito.when(applicationReleaseRepository.findByUID(release.getUID())).thenReturn(release);
        // when I update application
        release.setReleaseVersion("anotherVersion");
        manageApplicationRelease.updateApplicationRelease(release);
        // then release should be updated
        Mockito.verify(applicationReleaseRepository).merge(release);
    }

    @Test(expected = ApplicationReleaseNotFoundException.class)
    public void shouldFailToUpdateUnkwownApplicationRelease() throws ApplicationReleaseNotFoundException {
        // given no release with version aVersion exists
        ApplicationRelease release = new ApplicationRelease(new Application("aLabel", "aCode"), "aVersion");
        Mockito.when(applicationReleaseRepository.findByUID(release.getUID())).thenReturn(null);
        // when I update release
        manageApplicationRelease.updateApplicationRelease(release);
        // then It should fail
    }

    @Test(expected = PaasUserNotFoundException.class)
    public void shouldFailToCreateApplicationReleaseWithUnkownUser() throws PaasUserNotFoundException, ApplicationNotFoundException, DuplicateApplicationReleaseException {
        // given no paas user with ssoId unknown exists
        Mockito.when(paasUserRepository.findBySsoId(new SSOId("unknown"))).thenReturn(null);
        // when I create a release with application uid aUID and owner
        // unknown and version aVersion
        manageApplicationRelease.createApplicationRelease("aUID", "unknown", "aVersion");
        // then it should fail
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void shouldFailToCreateApplicationReleaseWithUnkownApplication() throws PaasUserNotFoundException, ApplicationNotFoundException, DuplicateApplicationReleaseException {
        // given paas user with ssoId aSsoId exists
        Mockito.when(paasUserRepository.findBySsoId(new SSOId("aSsoId"))).thenReturn(JOE_DALTON);
        // given no application with uid unknown exists
        Mockito.when(applicationRepository.findByUid("unknown")).thenReturn(null);
        // when I create a release with application uid unknown and owner
        // aSsoId and version aVersion
        manageApplicationRelease.createApplicationRelease("unknown", "aSsoId", "aVersion");
        // then it should fail
    }

    @Test(expected = DuplicateApplicationReleaseException.class)
    public void shouldFailToCreateApplicationReleaseWithDuplicateVersion() throws PaasUserNotFoundException, ApplicationNotFoundException, DuplicateApplicationReleaseException {
        TestHelper.loginAsAdmin();
        // given paas user with ssoId aSsoId exists
        Mockito.when(paasUserRepository.findBySsoId(new SSOId("aSsoId"))).thenReturn(JOE_DALTON);
        // given application with uid aUID exists
        Mockito.when(applicationRepository.findByUid("aUID")).thenReturn(new Application("aLabel", "aCode"));
        // given release version aVersion is not unique for application with uid
        // aUID
        Mockito.when(applicationReleaseRepository.findByApplicationUIDAndReleaseVersion("aUID", "aVersion")).thenReturn(
                new ApplicationRelease(new Application("aLabel", "aCode"), "aVersion"));
        // when I create a release with application uid aUID and owner aSsoId
        // and version aVersion
        manageApplicationRelease.createApplicationRelease("aUID", "aSsoId", "aVersion");
        // then it should fail
    }

    private void mockUserAndApplication() {
        // given user with admin role performs actions
        TestHelper.loginAsAdmin();
        // given paas user with ssoId aSsoId exists
        Mockito.when(paasUserRepository.findBySsoId(new SSOId("aSsoId"))).thenReturn(JOE_DALTON);
        // given application with uid aUID exists
        Mockito.when(applicationRepository.findByUid("aUID")).thenReturn(new Application("aLabel", "aCode"));
        // given release version aVersion is unique for application with uid
        // aUID
        Mockito.when(applicationReleaseRepository.findByApplicationUIDAndReleaseVersion("aUID", "aVersion")).thenReturn(null);
    }

    @Test
    public void shouldCreateApplicationReleaseWithExistingPaasUserAndExistingApplicationAndUniqueVersion() throws PaasUserNotFoundException, ApplicationNotFoundException,
            DuplicateApplicationReleaseException {
        mockUserAndApplication();
        // when I create a release with application uid aUID and owner
        // aSsoId and version aVersion
        manageApplicationRelease.createApplicationRelease("aUID", "aSsoId", "aVersion");
        // then application should be persisted
        Mockito.verify(applicationReleaseRepository).persist(Mockito.isA(ApplicationRelease.class));
    }

    @Test(expected = TechnicalException.class)
    public void shouldValidateApplicationReleaseWithTooLongVersion() throws PaasUserNotFoundException, ApplicationNotFoundException, DuplicateApplicationReleaseException {
        mockUserAndApplication();
        String version = TestHelper.generateOutOfLengthForDefaultString();
        manageApplicationRelease.createApplicationRelease("aUID", "aSsoId", version);
    }

    @Test(expected = TechnicalException.class)
    public void shouldValidateApplicationReleaseBWithTooLongVersion() throws PaasUserNotFoundException, ApplicationNotFoundException, DuplicateApplicationReleaseException {
        mockUserAndApplication();
        String version = TestHelper.generateOutOfLengthForDefaultString();
        URL versionControlUrl = null;
        String middlewareProfile = null;
        manageApplicationRelease.createApplicationRelease("aUID", "aSsoId", version, "description", versionControlUrl, middlewareProfile);
    }

    @Test(expected = TechnicalException.class)
    public void shouldValidateApplicationReleaseWithTooLongDescription() throws PaasUserNotFoundException, ApplicationNotFoundException, DuplicateApplicationReleaseException {
        mockUserAndApplication();
        String description = TestHelper.generateOutOfLengthForDefaultString();
        URL versionControlUrl = null;
        String middlewareProfile = null;
        manageApplicationRelease.createApplicationRelease("aUID", "aSsoId", "aVersion", description, versionControlUrl, middlewareProfile);
    }

    @Test(expected = TechnicalException.class)
    public void shouldValidateApplicationReleaseWithTooLongMiddlewareProfile() throws PaasUserNotFoundException, ApplicationNotFoundException, DuplicateApplicationReleaseException {
        mockUserAndApplication();
        String middlewareProfile = TestHelper.generateOutOfLengthForDefaultString();
        URL versionControlUrl = null;
        manageApplicationRelease.createApplicationRelease("aUID", "aSsoId", "aVersion", "aDescription", versionControlUrl, middlewareProfile);
    }

    @Test(expected = ApplicationNotFoundException.class)
    public void shouldFailToCountApplicationReleasesForUnkownApplication() throws ApplicationNotFoundException, DuplicateApplicationReleaseException {
        // given no application with uid unknown exists
        Mockito.when(applicationRepository.findByUid("unknown")).thenReturn(null);
        // when I count releases for application uid unknown
        manageApplicationRelease.countApplicationReleasesByAppUID("unknown");
        // then it should fail
    }

    @Test
    public void admin_user_should_count_all_application_releases_as_accessible() throws ApplicationNotFoundException, DuplicateApplicationReleaseException {
        // given user with admin role performs actions
        TestHelper.loginAsAdmin();
        // given application with uid aUID exists
        Mockito.when(applicationRepository.findByUid("aUID")).thenReturn(new Application("aLabel", "aCode"));
        // given application with uid aUID has 3 releases
        Mockito.when(applicationReleaseRepository.countApplicationReleasesByApplicationUID("aUID")).thenReturn(new Long(3));
        // when I count releases for application uid unknown
        // then I should get 3
        Assert.assertEquals("I should get 3 releases", 3, manageApplicationRelease.countApplicationReleasesByAppUID("aUID"));
    }

    @Test
    public void non_admin_user_should_count_public_and_private_application_releases_she_is_member_of_as_accessible() throws ApplicationNotFoundException,
            DuplicateApplicationReleaseException {
        // given user with user role performs actions
        TestHelper.loginAsUser();
        // given application with uid aUID exists
        Mockito.when(applicationRepository.findByUid("aUID")).thenReturn(new Application("aLabel", "aCode"));
        // given application with uid aUID has 3 releases
        Mockito.when(applicationReleaseRepository.countPublicOrPrivateByMemberAndByAppUID(TestHelper.USER_WITH_USER_ROLE_SSOID, "aUID")).thenReturn(new Long(3));
        // when I count releases for application uid unknown
        // then I should get 3
        Assert.assertEquals("I should get 3 releases", 3, manageApplicationRelease.countApplicationReleasesByAppUID("aUID"));
    }

    @Test
    public void find_all_middleware_profil_should_return_same_list_as_projection_service() throws Exception {
        // Given
        when(projectionServiceMock.findAllMiddlewareProfil()).thenReturn(Arrays.asList(MiddlewareProfile.V200_CF));

        // When
        List<MiddlewareProfile> middlewareProfiles = manageApplicationRelease.findAllMiddlewareProfil();

        // then
        assertThat(middlewareProfiles).containsExactly(MiddlewareProfile.V200_CF);
    }

    @Test
    public void should_purge_releases() {
        // GIVEN release A
        ApplicationRelease arA = new ApplicationRelease(new Application("a", "a"), "1");
        // and associated TDT
        TechnicalDeploymentTemplate tdtA_dev = new TechnicalDeploymentTemplate(new TechnicalDeployment("a"), DeploymentProfileEnum.DEVELOPMENT, arA.getUID(), MiddlewareProfile.DEFAULT_PROFILE);
        TechnicalDeploymentTemplate tdtA_prod = new TechnicalDeploymentTemplate(new TechnicalDeployment("a"), DeploymentProfileEnum.PRODUCTION, arA.getUID(), MiddlewareProfile.DEFAULT_PROFILE);
        // Given release B
        ApplicationRelease arB = new ApplicationRelease(new Application("b", "b"), "1");
        ;
        // and associated TDT
        TechnicalDeploymentTemplate tdtB_dev = new TechnicalDeploymentTemplate(new TechnicalDeployment("b"), DeploymentProfileEnum.DEVELOPMENT, arB.getUID(), MiddlewareProfile.DEFAULT_PROFILE);
        TechnicalDeploymentTemplate tdtB_prod = new TechnicalDeploymentTemplate(new TechnicalDeployment("b"), DeploymentProfileEnum.PRODUCTION, arB.getUID(), MiddlewareProfile.DEFAULT_PROFILE);

        List<ApplicationRelease> applicationReleases = Arrays.asList(arA, arB);
        List<TechnicalDeploymentTemplate> templateA = Arrays.asList(tdtA_dev, tdtA_prod);
        List<TechnicalDeploymentTemplate> templateB = Arrays.asList(tdtB_dev, tdtB_prod);

        doReturn(applicationReleases).when(applicationReleaseRepository).findRemovedReleasesWithoutEnvironment();
        doReturn(templateA).when(deploymentTemplateRepository).findAllByReleaseId(arA.getUID());
        doReturn(templateB).when(deploymentTemplateRepository).findAllByReleaseId(arB.getUID());

        // WHEN
        manageApplicationRelease.purgeOldRemovedReleases();

        // THEN
        verify(applicationReleaseRepository).findRemovedReleasesWithoutEnvironment();
        verify(applicationReleaseRepository).remove(arA);
        verify(applicationReleaseRepository).remove(arB);
        verify(deploymentTemplateRepository).delete(templateA);
        verify(deploymentTemplateRepository).delete(templateB);

    }

    @Test
    public void should_purge_no_release() {
        // GIVEN no removed release
        doReturn(null).when(applicationReleaseRepository).findRemovedReleasesWithoutEnvironment();

        // WHEN
        manageApplicationRelease.purgeOldRemovedReleases();

        // THEN
        verify(applicationReleaseRepository).findRemovedReleasesWithoutEnvironment();
        verify(applicationReleaseRepository, Mockito.never()).remove(Mockito.any(ApplicationRelease.class));
        verify(applicationReleaseRepository, Mockito.never()).remove(Mockito.any(ApplicationRelease.class));
        verify(deploymentTemplateRepository, Mockito.never()).delete(Mockito.any(Iterable.class));
        verify(deploymentTemplateRepository, Mockito.never()).delete(Mockito.any(Iterable.class));

    }

    @Test
    public void should_purge_no_technical_deployment_templates() {
        // GIVEN release A
        ApplicationRelease arA = new ApplicationRelease(new Application("a", "a"), "1");
        // Given release B
        ApplicationRelease arB = new ApplicationRelease(new Application("b", "b"), "1");
        ;

        List<ApplicationRelease> applicationReleases = Arrays.asList(arA, arB);

        doReturn(applicationReleases).when(applicationReleaseRepository).findRemovedReleasesWithoutEnvironment();
        doReturn(null).when(deploymentTemplateRepository).findAllByReleaseId(arA.getUID());
        doReturn(null).when(deploymentTemplateRepository).findAllByReleaseId(arB.getUID());

        // WHEN
        manageApplicationRelease.purgeOldRemovedReleases();

        // THEN
        verify(applicationReleaseRepository).findRemovedReleasesWithoutEnvironment();
        verify(applicationReleaseRepository).remove(arA);
        verify(applicationReleaseRepository).remove(arB);
        verify(deploymentTemplateRepository, Mockito.never()).delete(Mockito.any(Iterable.class));
        verify(deploymentTemplateRepository, Mockito.never()).delete(Mockito.any(Iterable.class));

    }
}
