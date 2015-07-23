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
package com.francetelecom.clara.cloud.stepdefs;

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.application.impl.DoubleAuthentication;
import com.francetelecom.clara.cloud.commons.AuthorizationException;
import com.francetelecom.clara.cloud.commons.MissingDefaultUserException;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManagePaasUser;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.PaasUserNotFoundException;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDaoTestUtils;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentTypeEnum;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.fest.assertions.Assertions.assertThat;

/**
 *
 */
@ContextConfiguration(locations = "classpath:com/francetelecom/clara/cloud/stepdefs/cucumber-context.xml")
@DirtiesContext
public class CreateAppStepdefs {

    private static final SSOId[] SSO_IDS_TYPE = new SSOId[]{};
    @Autowired(required = true)
    ManagePaasUser managePaasUser;

    @Autowired(required = true)
    ManageApplication manageApplication;

    @Autowired(required = true)
    ManageApplicationRelease manageApplicationRelease;

    @Autowired(required = true)
    ManageEnvironment manageEnvironment;

    @Autowired(required = true)
    ManageLogicalDeployment manageLogicalDeployment;

    @Autowired(required = true)
    EnvironmentRepository environmentRepository;

    @Autowired
    @Qualifier(value = "jeeProbeLogicalModelCatalog")
    SampleAppFactory logicalModelCatalog;

    @Autowired
    @Qualifier("mvnDao")
    protected MvnRepoDao mvnRepoDaoMock;

    private Set<String> createAppMembers;
    private Set<String> updateAppPrivateMembers;
    private ListFilterEnum list_filter;
    private String applicationId;
    private String applicationName;
    private AppVisibilityEnum createAppVisibility;
    private String releaseId;
    private String releaseName;
    private String environmentID;
    private boolean isReleaseAccessible;

    @Given("^the following users are registered:$")
    public void given_the_following_users_have_registered(List<SpecUser> users) throws Throwable {
        for (SpecUser specUser : users) {
            //FIXME: workaround for art #147272: use cuid as firstname
            PaasUser paasUser = new PaasUser(specUser.cuid, specUser.last_name, new SSOId(specUser.cuid), "any@email.com");
            paasUser.setPaasUserRole(specUser.role);
            // persist paas user
            managePaasUser.checkBeforeCreatePaasUser(paasUser);
            // make sure it was properly written by fetching it from DB
            managePaasUser.findPaasUser(specUser.cuid);
        }
    }

    @Given("^I'm logged in as (.*)$")
    public void given_I_m_logged_in_as(String cuid) throws Throwable {
        PaasUser user = managePaasUser.findPaasUser(cuid);
        SecurityContextHolder.getContext().setAuthentication(new DoubleAuthentication(cuid, user.getPaasUserRole()));
    }

    @When("^I request to create a (.*) application with the following list of members: (.*)$")
    public void when_I_request_to_create_a_private_application_with_the_following_list_of_members_member_list(AppVisibilityEnum visibility, String member_list) throws Throwable {
        this.createAppVisibility = visibility;
        this.createAppMembers = parseMembers(member_list);
    }

    @Then("^the application creation request is (.*) potentially with the following error message: (.*)$")
    public void then_the_application_creation_request_is_accepted_or_rejected_with_the_following_error_message_message(RequestStatusEnum status, String message) throws Throwable {
        try {
            switch (createAppVisibility) {
                case PUBLIC:
                    applicationId = manageApplication.createPublicApplication(
                            "aCode",
                            "aLabel",
                            "random desc",
                            new URL("http://any-url.org"),
                            getSsoIdsArrayFromStrings(createAppMembers));
                    break;
                case PRIVATE:
                    applicationId = manageApplication.createPrivateApplication(
                            "aCode",
                            "aLabel",
                            "random desc",
                            new URL("http://any-url.org"),
                            getSsoIdsArrayFromStrings(createAppMembers));
            }
            if (status.shouldReject()) {
                Assert.fail("expected creation to be refused");
            } else {
                //The list of members should be valid, after remov
                Application app = manageApplication.findApplicationByUID(applicationId);
                assertThat(app.listMembers()).containsExactly(getSsoIdsSetFromStrings(createAppMembers).toArray());
            }
        } catch (PaasUserNotFoundException e) {
            assertThat(convertToUiMessage(e).trim()).isEqualTo(message.trim());
        } catch (MissingDefaultUserException e) {
            assertThat(convertToUiMessage(e).trim()).isEqualTo(message.trim());
        } catch (AuthorizationException e) {
            assertThat(convertToUiMessage(e).trim()).isEqualTo(message.trim());
        } catch (Exception e) {
            Assert.fail("expected creation to be " + status + " caught:" + e);
        }
    }

    @Given("^I successfully created a (.*) application with (.*) as member list$")
    public void given_I_successfully_created_an_application_with_given_member_list(AppVisibilityEnum visibility, String members) throws Throwable {
        switch (visibility) {
            case PRIVATE:
                applicationId = manageApplication.createPrivateApplication(
                        "aCode" + System.currentTimeMillis(), //make the label and code unique to avoid collision among step of a single scenario
                        "aLabel" + System.currentTimeMillis(),
                        "random desc",
                        new URL("http://any-url.org"),
                        getSsoIdsArrayFromStrings(parseMembers(members)));
                break;
            case PUBLIC:
                applicationId = manageApplication.createPublicApplication(
                        "aCode" + System.currentTimeMillis(), //make the label and code unique to avoid collision among step of a single scenario
                        "aLabel" + System.currentTimeMillis(),
                        "random desc",
                        new URL("http://any-url.org"),
                        getSsoIdsArrayFromStrings(parseMembers(members)));
                break;
        }
        Application app = manageApplication.findApplicationByUID(applicationId);
        assertThat(app.listMembers()).containsExactly(getSsoIdsArrayFromStrings(parseMembers(members)));
    }

    @When("^I request to update the application with the following list of members: (.*)$")
    public void when_I_request_to_update_the_application_with_the_following_list_of_members_member_list(String member_list) throws Throwable {
        this.updateAppPrivateMembers = parseMembers(member_list);
    }

    @When("^I request the application (.*)$")
    public void when_I_request_the_given_application(String app_name) throws ApplicationNotFoundException {
        Collection<Application> apps = manageApplication.findApplications();
        for (Application application : apps) {
            if (application.getLabel().equals(app_name)) {
                applicationId = application.getUID();
                return;
            }
        }
        Assert.fail("Application " + app_name + " should have been found.");
    }

    @When("^I request the release (.*) of the application (.*)$")
    public void when_I_request_the_given_release(String release_name, String app_name) throws Throwable {
        applicationName = app_name;
        releaseName = release_name;

        Authentication previousAuth = SecurityContextHolder.getContext().getAuthentication();
        TestHelper.loginAsAdmin();

        Collection<ApplicationRelease> releases = manageApplicationRelease.findApplicationReleases(0, Integer.MAX_VALUE);
        for (ApplicationRelease release : releases) {
            if (!release.getApplication().getLabel().equals(app_name)) {
                continue;
            }
            if (release.getReleaseVersion().equals(release_name)) {
                releaseId = release.getUID();
                SecurityContextHolder.getContext().setAuthentication(previousAuth);
                return;
            }
        }
        Assert.fail("Release " + app_name + '-' + release_name + " should have been found.");
    }

    @When("^I request to see details on environment (.*) associated to application (.*) and release (.*)$")
    public void when_I_request_the_given_environment(String env_name, String app_name, String release_name) throws Throwable {
        // find releaseID first
        when_I_request_the_given_release(release_name, app_name);

        Authentication previousAuth = SecurityContextHolder.getContext().getAuthentication();
        TestHelper.loginAsAdmin();

        Collection<EnvironmentDto> envs = manageEnvironment.findEnvironments(0, Integer.MAX_VALUE, "label", "ASC");
        for (EnvironmentDto environmentDto : envs) {
            if (environmentDto.getReleaseUID().equals(releaseId)) {
                environmentID = environmentDto.getUid();

                // Update environment state to RUNNING instead of CREATING as activation is mocked
                // This allows to later start/stop/delete the environment
                Environment env = environmentRepository.findByUID(environmentID);
                environmentRepository.updateEnvironmentStateByTDI(env.getTechnicalDeploymentInstance().getId(), EnvironmentStatus.RUNNING, "", 0);

                SecurityContextHolder.getContext().setAuthentication(previousAuth);
                return;
            }
        }
    }

    @Then("^the application update request is (.*) potentially with the following error message: (.*)$")
    public void then_the_application_update_request_is_accepted_or_rejected_with_the_following_error_message_message(RequestStatusEnum status, String message) throws Throwable {
        try {
            Application app = manageApplication.findApplicationByUID(applicationId);
            app.setMembers(getSsoIdsSetFromStrings(updateAppPrivateMembers));
            manageApplication.updateApplication(app);
            if (status.shouldReject()) {
                Assert.fail("expected update to be refused");
            } else {
                app = manageApplication.findApplicationByUID(applicationId);
                Object[] requestedMembersWithoutDuplicates = getSsoIdsSetFromStrings(updateAppPrivateMembers).toArray();
                assertThat(app.listMembers()).containsExactly(requestedMembersWithoutDuplicates);
            }
        } catch (PaasUserNotFoundException e) {
            assertThat(convertToUiMessage(e).trim()).isEqualTo(message.trim());
        } catch (MissingDefaultUserException e) {
            assertThat(convertToUiMessage(e).trim()).isEqualTo(message.trim());
        } catch (Exception e) {
            Assert.fail("expected update to be " + status + " caught:" + e);
        }
    }

    @When("^I list applications using the \"(.*)\" filter$")
    public void when_I_list_applications_using_the_given_filter(ListFilterEnum filter) throws Throwable {
        list_filter = filter;
    }

    @When("^I list releases using the \"(.*)\" filter$")
    public void when_I_list_releases_using_the_given_filter(ListFilterEnum filter) throws Throwable {
        list_filter = filter;
    }

    @When("^I list environments using the \"(.*)\" filter$")
    public void when_I_list_environments_using_the_given_filter(ListFilterEnum filter) throws Throwable {
        list_filter = filter;
    }

    @And("^the following applications were created:$")
    public void given_the_following_applications_were_created(List<SpecApp> createdApplications) throws Throwable {
        TestHelper.loginAsAdmin();
        for (SpecApp specApp : createdApplications) {
            String applicationUID = null;
            switch (specApp.visibility) {
                case PRIVATE:
                    applicationUID = manageApplication.createPrivateApplication(
                            specApp.app_name,
                            specApp.app_name,
                            "random desc",
                            new URL("http://any-url.org"),
                            getSsoIdsArrayFromStrings(parseMembers(specApp.member_list)));
                    break;
                case PUBLIC:
                    applicationUID = manageApplication.createPublicApplication(
                            specApp.app_name,
                            specApp.app_name,
                            "random desc",
                            new URL("http://any-url.org"),
                            getSsoIdsArrayFromStrings(parseMembers(specApp.member_list)));
                    break;
            }
            for (int i = 0; i < specApp.nb_releases; i++) {
                manageApplicationRelease.createApplicationRelease(applicationUID, "jlt0000", i + ".0");
            }
        }
        TestHelper.logout();
    }

    @And("^the following releases were created:$")
    public void given_the_following_releases_were_created(List<SpecRelease> createdReleases) throws Throwable {
        TestHelper.loginAsAdmin();
        for (SpecRelease specRelease : createdReleases) {
            String applicationUID = null;
            Collection<Application> apps = manageApplication.findApplications();
            for (Application application : apps) {
                if (application.getLabel().equals(specRelease.app_name)) {
                    applicationUID = application.getUID();
                    break;
                }
            }
            assertThat(applicationUID).isNotNull();
            manageApplicationRelease.createApplicationRelease(applicationUID, "jlt0000", specRelease.app_release);
        }
        TestHelper.logout();
    }

    @And("^the following environments were created:$")
    public void given_the_following_environments_were_created(List<SpecEnvironment> createdEnvironments) throws Throwable {
        TestHelper.loginAsAdmin();
        for (SpecEnvironment specEnvironment : createdEnvironments) {
            String releaseUID = null;
            int logicalDeploymentId = -1;
            Collection<ApplicationRelease> releases = manageApplicationRelease.findApplicationReleases(0, Integer.MAX_VALUE);
            for (ApplicationRelease release : releases) {
                if (!release.getApplication().getLabel().equals(specEnvironment.app_name)) {
                    continue;
                }
                if (release.getReleaseVersion().equals(specEnvironment.app_release)) {
                    releaseUID = release.getUID();
                    logicalDeploymentId = release.getLogicalDeployment().getId();
                    break;
                }
            }
            assertThat(releaseUID).isNotNull();

            // Configure MvnRepoDao Mock
            MvnRepoDaoTestUtils.mockResolveUrl(mvnRepoDaoMock);

            // Refetch to eagerly fetch all fields.
            LogicalDeployment logicalDeployment = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentId);

            logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
            logicalDeployment = manageLogicalDeployment.updateLogicalDeployment(logicalDeployment);
            logicalDeployment = manageLogicalDeployment.checkOverallConsistencyAndUpdateLogicalDeployment(logicalDeployment);

            manageEnvironment.createEnvironment(releaseUID, EnvironmentTypeEnum.DEVELOPMENT, "jlt0000", specEnvironment.env_name);
        }
        TestHelper.logout();
    }

    @Then("^the listed applications are:$")
    public void then_the_listed_applications_are(List<ListedApplication> expectedListedApplications) throws Throwable {
        Set<ListedApplication> apps = listApplications(list_filter);
        Assert.assertEquals(new HashSet<>(expectedListedApplications), apps);
    }

    @Then("^the listed releases are:$")
    public void then_the_listed_releases_are(List<SpecRelease> expectedListedReleases) throws Throwable {
        Set<SpecRelease> releases = listReleases(list_filter);
        Assert.assertEquals(new HashSet<>(expectedListedReleases), new HashSet<>(releases));
    }

    @Then("^the listed environments are:$")
    public void then_the_listed_environments_are(List<SpecEnvironment> expectedListedEnvironments) throws Throwable {
        Set<SpecEnvironment> environments = listEnvironments(list_filter);
        Assert.assertEquals(new HashSet<>(expectedListedEnvironments), new HashSet<>(environments));
    }

    @Then("^I (.*) edit the application$")
    public void then_I_can_edit_the_application_or_not(AccessRightsEnum can_edit) throws Throwable {
        Application requestedApp = manageApplication.findApplicationByUID(applicationId);
        switch (can_edit) {
            case CAN:
                Assert.assertTrue("Application " + requestedApp.getLabel() + " must be editable.", requestedApp.isEditable());
                manageApplication.updateApplication(requestedApp);
                break;
            case CANNOT:
                Assert.assertFalse("Application " + requestedApp.getLabel() + " should not be editable.", requestedApp.isEditable());
                try {
                    manageApplication.updateApplication(requestedApp);
                    Assert.fail("Application " + requestedApp + " should not be editable.");
                } catch (AuthorizationException exc) {
                }
                break;
        }
    }

    @Then("^I (.*) edit the release$")
    public void then_I_can_edit_the_release_or_not(AccessRightsEnum can_edit) throws Throwable {
        ApplicationRelease requestedRelease;
        if (isReleaseAccessible) {
            requestedRelease = manageApplicationRelease.findApplicationReleaseByUID(releaseId);
        } else {
            Authentication previousAuth = SecurityContextHolder.getContext().getAuthentication();
            TestHelper.loginAsAdmin();
            requestedRelease = manageApplicationRelease.findApplicationReleaseByUID(releaseId);
            SecurityContextHolder.getContext().setAuthentication(previousAuth);
        }

        switch (can_edit) {
            case CAN:
                if (isReleaseAccessible) {
                    // The editable flag makes sense only for accessible release
                    Assert.assertTrue("Release " + requestedRelease.getApplication().getLabel() + '-' + requestedRelease.getReleaseVersion()
                            + " must be editable.", requestedRelease.getApplication().isEditable());
                }
                manageApplicationRelease.updateApplicationRelease(requestedRelease);
                break;
            case CANNOT:
                if (isReleaseAccessible) {
                    // The editable flag makes sense only for accessible release
                    Assert.assertFalse("Release " + requestedRelease.getApplication().getLabel() + '-' + requestedRelease.getReleaseVersion()
                            + " should not be editable.", requestedRelease.getApplication().isEditable());
                }
                try {
                    manageApplicationRelease.updateApplicationRelease(requestedRelease);
                    Assert.fail("Release should not be editable.");
                } catch (AuthorizationException exc) {
                }
                break;
        }
    }

    @Then("^I (.*) access the release$")
    public void then_I_can_access_the_release_or_not(AccessRightsEnum can_access) throws Throwable {
        switch (can_access) {
            case CAN:
                isReleaseAccessible = true;
                manageApplicationRelease.findApplicationReleaseByUID(releaseId);
                break;
            case CANNOT:
                isReleaseAccessible = false;
                try {
                    manageApplicationRelease.findApplicationReleaseByUID(releaseId);
                    Assert.fail("Release should not be accessible.");
                } catch (AuthorizationException exc) {
                }
                break;
        }
    }

    @Then("^I (.*) access the environment$")
    public void then_I_can_access_the_environment_or_not(AccessRightsEnum can_access) throws Throwable {
        switch (can_access) {
            case CAN:
                manageEnvironment.findEnvironmentByUID(environmentID);
                break;
            case CANNOT:
                try {
                    manageEnvironment.findEnvironmentByUID(environmentID);
                    Assert.fail("Environment should not be accessible.");
                } catch (AuthorizationException exc) {
                }
                break;
        }
    }

    @Then("^I (.*) create a new release for this application$")
    public void then_I_can_create_a_new_release_or_not(AccessRightsEnum can_create) throws Throwable {
        Application requestedApp = manageApplication.findApplicationByUID(applicationId);
        switch (can_create) {
            case CAN:
                String releaseUid = manageApplicationRelease.createApplicationRelease(requestedApp.getUID(), "jlt0000", "1.0-test");
                manageApplicationRelease.deleteApplicationRelease(releaseUid);
                break;
            case CANNOT:
                try {
                    manageApplicationRelease.createApplicationRelease(requestedApp.getUID(), "jlt0000", "1.0-test");
                    Assert.fail("Expected creation to be refused.");
                } catch (AuthorizationException e) {
                }
                break;
        }
    }

    @Then("^I (.*) list the releases$")
    public void then_I_can_list_the_releases_or_not(AccessRightsEnum can_list) throws Throwable {
        Application requestedApp = manageApplication.findApplicationByUID(applicationId);
        int releaseListSize = manageApplicationRelease.findApplicationReleasesByAppUID(requestedApp.getUID()).size();
        switch (can_list) {
            case CAN:
                assertThat(releaseListSize).as("Release list size of application " + requestedApp.getLabel()).isPositive();
                break;
            case CANNOT:
                assertThat(releaseListSize).as("Release list size of application " + requestedApp.getLabel()).isZero();
                break;
        }
    }

    @Then("^I (.*) start the environment$")
    public void then_I_can_start_the_environment_or_not(AccessRightsEnum can_start) throws Throwable {
        switch (can_start) {
            case CAN:
                manageEnvironment.startEnvironment(environmentID);
                break;
            case CANNOT:
                try {
                    manageEnvironment.startEnvironment(environmentID);
                    Assert.fail("Expected environment start to be refused.");
                } catch (AuthorizationException e) {
                }
                break;
        }
    }

    @Then("^I (.*) stop the environment$")
    public void then_I_can_stop_the_environment_or_not(AccessRightsEnum can_stop) throws Throwable {
        switch (can_stop) {
            case CAN:
                manageEnvironment.stopEnvironment(environmentID);
                break;
            case CANNOT:
                try {
                    manageEnvironment.stopEnvironment(environmentID);
                    Assert.fail("Expected environment stop to be refused.");
                } catch (AuthorizationException e) {
                }
                break;
        }
    }

    @Then("^I (.*) delete the environment$")
    public void then_I_can_delete_the_environment_or_not(AccessRightsEnum can_delete) throws Throwable {
        switch (can_delete) {
            case CAN:
                manageEnvironment.deleteEnvironment(environmentID);
                break;
            case CANNOT:
                try {
                    manageEnvironment.deleteEnvironment(environmentID);
                    Assert.fail("Expected environment deletion to be refused.");
                } catch (AuthorizationException e) {
                }
                break;
        }
    }

    @Then("^I see member_list=(.*)$")
    public void then_i_see_member_list(String member_list) throws Throwable {
        Application requestedApp = manageApplication.findApplicationByUID(applicationId);
        Assert.assertEquals(getSsoIdsSetFromStrings(parseMembers(member_list)), new HashSet<>(requestedApp.listMembers()));
    }

    private String convertToUiMessage(MissingDefaultUserException e) {
        return "an application needs to have at least one member specified";
    }

    private String convertToUiMessage(AuthorizationException e) {
        return "[should add warning: lost membership of this application]";
    }

    private String convertToUiMessage(PaasUserNotFoundException e) {
        String missingUserId = e.getMissingUserId().toString();
        return "unable to set user \"" + missingUserId + "\" as member as it is unknown.";
    }

    private static SSOId[] getSsoIdsArrayFromStrings(Collection<String> ssoIdStrings) {
        Set<SSOId> ssoIds = getSsoIdsSetFromStrings(ssoIdStrings);
        return ssoIds.toArray(SSO_IDS_TYPE);
    }

    private static Set<SSOId> getSsoIdsSetFromStrings(Collection<String> ssoIdStrings) {
        Set<SSOId> ssoIds = new HashSet<>();
        for (String userId : ssoIdStrings) {
            SSOId ssoId = new SSOId(userId);
            ssoIds.add(ssoId);
        }
        return ssoIds;
    }

    private static Set<String> parseMembers(String member_list) {
        Set<String> members;
        if (member_list.isEmpty()) {
            members = new HashSet<>();
        } else {
            members = new HashSet<>(asList(member_list.split(" +")));
        }
        return members;
    }

    private Set<ListedApplication> listApplications(ListFilterEnum list_application_filter) throws ApplicationNotFoundException {
        Set<ListedApplication> listedApplications = new HashSet<>();
        Collection<Application> foundApplications = null;

        switch (list_application_filter) {
            case ALL_APPLICATIONS:
                foundApplications = manageApplication.findApplications();
                break;
            case DEFAULT:
                foundApplications = manageApplication.findMyApplications();
                break;
            default:
                Assert.fail("Unexpected filter: " + list_application_filter);
        }

        for (Application app : foundApplications) {
            ListedApplication listedApplication = new ListedApplication(app.getLabel(), app.listMembers(), app.isEditable());
            listedApplications.add(listedApplication);
        }
        return listedApplications;
    }

    private Set<SpecRelease> listReleases(ListFilterEnum list_filter) throws ApplicationNotFoundException {
        Set<SpecRelease> listedReleases = new HashSet<>();
        Collection<ApplicationRelease> foundReleases = null;

        switch (list_filter) {
            case ALL_RELEASES:
                foundReleases = manageApplicationRelease.findApplicationReleases(0, Integer.MAX_VALUE);
                break;
            case DEFAULT:
                foundReleases = manageApplicationRelease.findMyApplicationReleases();
                break;
            default:
                Assert.fail("Unexpected filter: " + list_filter);
        }

        for (ApplicationRelease release : foundReleases) {
            SpecRelease listedRelease = new SpecRelease();
            listedRelease.app_name = release.getApplication().getLabel();
            listedRelease.app_release = release.getReleaseVersion();
            listedReleases.add(listedRelease);
        }
        return listedReleases;
    }

    private Set<SpecEnvironment> listEnvironments(ListFilterEnum list_filter) throws ApplicationNotFoundException {
        Set<SpecEnvironment> listedEnvironments = new HashSet<>();
        Collection<EnvironmentDto> foundEnvironments = null;

        switch (list_filter) {
            case ALL_ENVIRONMENTS:
                foundEnvironments = manageEnvironment.findEnvironments(0, Integer.MAX_VALUE, "creationDate", "ASC");
                break;
            case DEFAULT:
                foundEnvironments = manageEnvironment.findMyEnvironments(0, Integer.MAX_VALUE, "creationDate", "ASC");
                break;
            default:
                Assert.fail("Unexpected filter: " + list_filter);
        }

        for (EnvironmentDto environment : foundEnvironments) {
            SpecEnvironment listedEnvironment = new SpecEnvironment();
            listedEnvironment.app_name = environment.getApplicationLabel();
            listedEnvironment.app_release = environment.getReleaseVersion();
            listedEnvironment.env_name = environment.getLabel();
            listedEnvironments.add(listedEnvironment);
        }
        return listedEnvironments;
    }

    public static class SpecEnvironment {
        private String app_name;
        private String app_release;
        private String env_name;

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((app_name == null) ? 0 : app_name.hashCode());
            result = prime * result + ((app_release == null) ? 0 : app_release.hashCode());
            result = prime * result + ((env_name == null) ? 0 : env_name.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SpecEnvironment other = (SpecEnvironment) obj;
            if (app_name == null) {
                if (other.app_name != null)
                    return false;
            } else if (!app_name.equals(other.app_name))
                return false;
            if (app_release == null) {
                if (other.app_release != null)
                    return false;
            } else if (!app_release.equals(other.app_release))
                return false;
            if (env_name == null) {
                if (other.env_name != null)
                    return false;
            } else if (!env_name.equals(other.env_name))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }

    }

    public static class SpecRelease {
        private String app_name;
        private String app_release;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((app_name == null) ? 0 : app_name.hashCode());
            result = prime * result + ((app_release == null) ? 0 : app_release.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SpecRelease other = (SpecRelease) obj;
            if (app_name == null) {
                if (other.app_name != null)
                    return false;
            } else if (!app_name.equals(other.app_name))
                return false;
            if (app_release == null) {
                if (other.app_release != null)
                    return false;
            } else if (!app_release.equals(other.app_release))
                return false;
            return true;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }
    }

    public static class SpecUser {
        private String cuid;
        private String first_name;
        private String last_name;
        private PaasRoleEnum role;
    }

    public static class SpecApp {
        private String app_name;
        private String member_list;
        private AppVisibilityEnum visibility;
        private int nb_releases;
    }

    public static class ListedApplication {
        private String app_name;
        private String member_list;
        private boolean can_edit;

        public ListedApplication(String app_name, Collection<SSOId> listMembers, boolean can_edit) {
            this.app_name = app_name;
            this.member_list = "";
            for (SSOId ssoId : listMembers) {
                member_list += ssoId.getValue();
                member_list += " ";
            }
            member_list = member_list.trim();
            this.can_edit = can_edit;
        }

        @Override
        public String toString() {
            return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((app_name == null) ? 0 : app_name.hashCode());
            result = prime * result + (can_edit ? 1231 : 1237);
            result = prime * result + ((member_list == null) ? 0 : parseMembers(member_list).hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ListedApplication other = (ListedApplication) obj;
            if (app_name == null) {
                if (other.app_name != null)
                    return false;
            } else if (!app_name.equals(other.app_name))
                return false;
            if (can_edit != other.can_edit)
                return false;
            if (member_list == null) {
                if (other.member_list != null)
                    return false;
            } else if (!parseMembers(member_list).equals(parseMembers(other.member_list)))
                return false;
            return true;
        }
    }
}
