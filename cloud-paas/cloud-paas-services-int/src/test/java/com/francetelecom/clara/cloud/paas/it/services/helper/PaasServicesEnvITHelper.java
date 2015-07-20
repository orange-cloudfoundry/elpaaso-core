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
package com.francetelecom.clara.cloud.paas.it.services.helper;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentOpsDetailsDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.coremodel.exception.*;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.internal.AssumptionViolatedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

public class PaasServicesEnvITHelper {
    protected static Logger logger = LoggerFactory.getLogger(PaasServicesEnvITHelper.class.getName());

    /**
     * configuration of the ServicesEnvIT Test
     */
    protected PaasServicesEnvITConfiguration itConfiguration;

    protected String environmentUID = null;
    protected Application application;
    // logical deployment ID is kept so that we can get access on logical model for assertions
    protected int logicalDeploymentID;
    protected int maxRequests = 10;


    protected int maxSessions = 10;

    protected boolean isStateful = false;

    protected EnvironmentDto.EnvironmentTypeEnum envType = EnvironmentDto.EnvironmentTypeEnum.DEVELOPMENT;

    /**
     * environment creation timeout in minutes (default is 90 (1h30);
     * can be overridden by each test) default is 1h30 in order to
     * include image generation if any example : when 2 appliance are
     * generated (20min) + published (7min) + started (workaround
     * included : 30min) then 60min is not enough
     */
    private int environmentCreationTimeoutMin = 90;
    /**
     * environment stop timeout in minutes (default is 10; can be
     * overridden by each test)
     */
    private int environmentStopTimeoutMin = 10;
    /**
     * environment start timeout in minutes (default is 15; can be
     * overridden by each test)
     */
    private int environmentStartTimeoutMin = 10;

    /**
     * environment delete timeout in minutes (default is 10; can be
     * overridden by each test)
     */
    private int environmentDeleteTimeout = 10;

    /**
     * Test url attempt count
     */
    private int webAppTestAttempts = 2;
    /**
     * test url wait time in second
     */
    private int webAppTestWaitTime = 5;

    private Boolean skipDeleteEnvironmentAtTheEnd;

    private PaasUser currentUser;
    private EnvironmentDto.EnvironmentStatusEnum environmentExpectedStatus;

    /**
     * constructor :
     *
     * @param itConfiguration test configuration
     */
    public PaasServicesEnvITHelper(PaasServicesEnvITConfiguration itConfiguration) {
        this.itConfiguration = itConfiguration;
        environmentExpectedStatus = EnvironmentDto.EnvironmentStatusEnum.RUNNING;
    }

    public void setEnvironmentExpectedStatus(EnvironmentDto.EnvironmentStatusEnum environmentExpectedStatus) {
        this.environmentExpectedStatus = environmentExpectedStatus;
    }

    public Set<String> getAllDistinctHostFromEnvironment() {
        List<EnvironmentOpsDetailsDto.VMAccessDto> vmAccessDtos = getEnvironmentVmAccessDtos();
        Set<String> envHosts = new HashSet<String>();
        for (EnvironmentOpsDetailsDto.VMAccessDto vmAccessDto : vmAccessDtos) {
            envHosts.add(vmAccessDto.getIp());
        }
        return envHosts;
    }

    public Set<String> getAllDistinctFqdnFromEnvironment() {
        List<EnvironmentOpsDetailsDto.VMAccessDto> vmAccessDtos = getEnvironmentVmAccessDtos();
        Set<String> envFqdns = new HashSet<String>();
        for (EnvironmentOpsDetailsDto.VMAccessDto vmAccessDto : vmAccessDtos) {
            envFqdns.add(vmAccessDto.getHostname());
        }
        return envFqdns;
    }

    public Map<String, String> getAllFqdnAndHostFromEnvironment() {
        List<EnvironmentOpsDetailsDto.VMAccessDto> vmAccessDtos = getEnvironmentVmAccessDtos();
        Map<String, String> envFQdnIps = new HashMap<String, String>();
        for (EnvironmentOpsDetailsDto.VMAccessDto vmAccessDto : vmAccessDtos) {
            envFQdnIps.put(vmAccessDto.getHostname(), vmAccessDto.getIp());
        }
        return envFQdnIps;
    }

    private List<EnvironmentOpsDetailsDto.VMAccessDto> getEnvironmentVmAccessDtos() {
        EnvironmentOpsDetailsDto environmentOpsDetails;
        try {
            environmentOpsDetails = getEnvironmentOpsDetails();
        } catch (ObjectNotFoundException e) {
            throw new TechnicalException("Unable to find test environment", e);
        }
        return environmentOpsDetails.listVMAccesses();
    }

    /**
     * get all web gui access link from the env
     *
     * @return access links list
     */
    private List<LinkDto> getWebGuiAccessLinks() {
        EnvironmentDetailsDto envDto;
        LogicalDeployment ld;
        try {
            envDto = getEnvironmentDetails();
            ld = getLogicalDeployment();
        } catch (ObjectNotFoundException e) {
            throw new TechnicalException("Unable to find test environment", e);
        }
        Set<LogicalWebGUIService> webGuiServices = ld.listLogicalServices(LogicalWebGUIService.class);
        if (webGuiServices.isEmpty()) {
            logger.warn("No web gui to be tested");
        }

        Map<String, List<LinkDto>> linkDtosMap = envDto.getLinkDtoMap();
        List<LinkDto> guiAccessLinks = new ArrayList<LinkDto>();
        for (LogicalWebGUIService webGuiService : webGuiServices) {
            List<LinkDto> linkDtos = linkDtosMap.get(webGuiService.getName());
            for (LinkDto link : linkDtos) {
                if (link.getLinkType() == LinkDto.LinkTypeEnum.ACCESS_LINK) {
                    guiAccessLinks.add(link);
                }
            }
        }
        return guiAccessLinks;
    }


    /**
     * Ensure that the EAR is deployed and accessible
     */
    public void application_should_be_accessible(boolean accessibleExpected) {
        List<LinkDto> guiAccessLinks = getWebGuiAccessLinks();
        Assert.assertFalse("No access url corresponding to a webGui service have been found",
                guiAccessLinks.isEmpty());
        getApplicationAccessHelper()
                .checkWebGuiServicesAccess(guiAccessLinks, accessibleExpected, itConfiguration);
    }

    public LogicalDeployment getLogicalDeployment() throws ObjectNotFoundException {
        return itConfiguration.getManageLogicalDeployment().findLogicalDeployment(logicalDeploymentID);
    }

    protected PaasServicesEnvApplicationAccessHelper getApplicationAccessHelper() {
        return new PaasServicesEnvApplicationAccessHelper(
                itConfiguration.getLogicalModelCatalog(),
                webAppTestAttempts,
                webAppTestWaitTime);
    }

    public String setUp() {
        checkThatAutowiredFieldIsNotNull(itConfiguration);
        checkThatAutowiredFieldIsNotNull(itConfiguration.getManageApplication());
        currentUser = AuthenticationHelper.loginAsAdmin();

        if (environmentUID == null) {
            environmentUID = createTestEnvironment(environmentExpectedStatus);
        }
        // else : environmentUID != null
        assumeEnvironmentNotFailed(environmentUID);
        return environmentUID;
    }

    public static void checkThatAutowiredFieldIsNotNull(Object autowiredField) {
        if (autowiredField == null) {
            throw new TechnicalException("declared autowired fields null : \n"
                    + "\t- either you start an @Ignore test\n"
                    + "\t- or check the context..");
        }
    }

    /**
     * Global tear-down consists in deleting XaaS resources created during the
     * test
     */
    public void tearDown() {
        logger.info("/////////////////////////////////////////////////////");
        logger.info("//////////////// TEAR DOWN - start //////////////////");
        logger.info("/////////////////////////////////////////////////////");
        if (application == null) {
            // nothing to remove
            return;
        }

        boolean skipDelete = skipDelete();

        String firstError = null;
        try {
            for (ApplicationRelease release : itConfiguration.getManageApplicationRelease().findApplicationReleasesByAppUID(application.getUID())) {
                firstError = removeReleaseAndRelatedEnvironments(skipDelete, firstError, release);
            }
            if (!skipDelete) {
                itConfiguration.getManageApplication().deleteApplication(application.getUID());
                application = null;
            }
            if (firstError != null) {
                throw new TechnicalException("Error during tearDown, check for not deleted resources: " + firstError);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        AuthenticationHelper.logout();

        logger.info("//////////////// TEAR DOWN - end //////////////////");
    }

    private boolean skipDelete() {
        if (skipDeleteEnvironmentAtTheEnd == null) {
            setSkipDeleteEnvironmentAtTheEnd(Boolean.getBoolean("skipDeleteEnvironmentAtTheEnd"));
        }
        return skipDeleteEnvironmentAtTheEnd;
    }

    private String removeReleaseAndRelatedEnvironments(boolean skipDelete, String firstError, ApplicationRelease release) throws ApplicationReleaseNotFoundException {
        for (EnvironmentDto env : itConfiguration.getManageEnvironment().findEnvironmentsByAppRelease(release.getUID())) {
            try {
                EnvironmentOpsDetailsDto environmentDetails = itConfiguration.getManageEnvironment().findEnvironmentOpsDetailsByUID(env.getUid());
                logger.info("Splunk logs available at " + environmentDetails.getLinkDtoMap());
                if (skipDelete) {
                    logger.info("skipping clean up of resources for environment " + env.getUid()
                            + ", please perform this clean up manually. Details follow.");
                    displayEnvironmentDetailsInLogs(environmentDetails);
                    break;
                } else {
                    itConfiguration.getManageEnvironment().deleteEnvironment(env.getUid());
                    waitForStatus(env.getUid(), EnvironmentDto.EnvironmentStatusEnum.REMOVED, environmentDeleteTimeout);

                    for (EnvironmentOpsDetailsDto.VMAccessDto access : environmentDetails.listVMAccesses()) {
                        String curVMLogInfo = "VM " + access.getHostname() + " for environment " + env.getUid();
                        if (access.getIaasId() != null) {
                            logger.warn("Cannot retrieve infos for iaasId {}: {}", access.getIaasId(), curVMLogInfo);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                if (firstError == null) {
                    firstError = "Error : " + e.getMessage();
                }
            }
        }
        if (!skipDelete) {
            itConfiguration.getManageApplicationRelease().deleteApplicationRelease(release.getUID());
        }
        return firstError;
    }

    public void assumeEnvironmentNotFailed(String environmentUID) {
        if (environmentExpectedStatus == EnvironmentDto.EnvironmentStatusEnum.FAILED){
            return;
        }
        EnvironmentDto envDto;
        try {
            envDto = itConfiguration.getManageEnvironment().findEnvironmentByUID(environmentUID);
        } catch (ObjectNotFoundException e) {
            throw new TechnicalException("Unable to create test environment", e);
        }
        boolean envStatusFailed = (EnvironmentDto.EnvironmentStatusEnum.FAILED == envDto.getStatus());
        if (envStatusFailed) {
            logger.error("Skipping test because environment creation has failed in previous test.");
        }
        Assume.assumeTrue(!envStatusFailed);
    }

    public String createTestEnvironment(EnvironmentDto.EnvironmentStatusEnum environmentExpectedStatus) {
        PaasUser paasUser = createTestUser();
        application = createApplication(paasUser);
        ApplicationRelease applicationRelease = createApplicationRelease(getMiddlewareProfileVersion(), paasUser);
        logicalDeploymentID = createLogicalDeployment(applicationRelease);

        // *************** createEnvironment *************************
        String environmentLabel = itConfiguration.getName() + "-" + envType.toString();
        try {
            environmentUID = itConfiguration.getManageEnvironment().createEnvironment(
                    applicationRelease.getUID(),
                    envType,
                    paasUser.getSsoId().getValue(),
                    environmentLabel);
        } catch (BusinessException e) {
            throw new TechnicalException("Unable to create test environment", e);
        }

        try {
            waitForStatus(environmentUID, environmentExpectedStatus, environmentCreationTimeoutMin);
        } catch (ObjectNotFoundException e) {
            throw new AssumptionViolatedException(e, new TestComponentExpected(EnvironmentDetailsDto.class, environmentUID, "test environment"));
        }

        displayEnvironmentDetailsInLogs();
        return environmentUID;
    }


    public void displayEnvironmentDetailsInLogs() {
        if (environmentUID == null) {
            return;
        }
        EnvironmentDetailsDto environmentDetails;
        try {
            environmentDetails = getEnvironmentDetails();
        } catch (ObjectNotFoundException e) {
            throw new AssumptionViolatedException(e, new TestComponentExpected(EnvironmentDetailsDto.class, environmentUID, "test environment"));

        }
        displayEnvironmentDetailsInLogs(environmentDetails);
    }

    private int createLogicalDeployment(ApplicationRelease applicationRelease) {
        // Refetch to eagerly fetch all fields.
        LogicalDeployment logicalDeployment;
        int ldId = applicationRelease.getLogicalDeployment().getId();
        try {
            logicalDeployment = itConfiguration.getManageLogicalDeployment().findLogicalDeployment(ldId);
        } catch (ObjectNotFoundException e) {
            throw new AssumptionViolatedException(e, new TestComponentExpected(LogicalDeployment.class, String.valueOf(ldId), "test logical deployment"));
        }

        itConfiguration.getLogicalModelCatalog().populateLogicalDeployment(logicalDeployment);

        // Update maxSession and maxRequest of logical web gui services
        for (LogicalWebGUIService s : logicalDeployment.listLogicalServices(LogicalWebGUIService.class)) {
            s.setMaxNumberSessions(maxSessions);
            s.setMaxReqPerSeconds(maxRequests);
            s.setStateful(isStateful);
        }
        try {
            logicalDeployment = itConfiguration.getManageLogicalDeployment().updateLogicalDeployment(logicalDeployment);
        } catch (Exception e) {
            throw new TechnicalException("Unable to update test logical deployment", e);
        }
        // we need to call
        // checkOverallConsistencyAndUpdateLogicaldeployment() to trigger
        // maven references resolution
        try {
            itConfiguration.getManageLogicalDeployment().checkOverallConsistencyAndUpdateLogicalDeployment(logicalDeployment);
        } catch (BusinessException e) {
            throw new TechnicalException("Unable to check overall consistency and update logical deployment: " + e.getMessage(), e);
        }
        return ldId;

    }

    /**
     * Creates and persist application release
     *
     * @param middlewareProfileVersion middleware profile version
     * @param paasUser                 application release owner
     * @return created applicationRelease
     */
    private ApplicationRelease createApplicationRelease(String middlewareProfileVersion, PaasUser paasUser) {
        String applicationReleaseUID;
        try {
            applicationReleaseUID = itConfiguration.getManageApplicationRelease().createApplicationRelease(application.getUID(), paasUser.getSsoId().getValue(), "1");
        } catch (Exception e) {
            throw new TechnicalException("Unable to create test application release", e);
        }
        // Fetch applicationRelease from DB
        ApplicationRelease applicationRelease;
        try {
            applicationRelease = itConfiguration.getManageApplicationRelease().findApplicationReleaseByUID(applicationReleaseUID);
        } catch (ApplicationReleaseNotFoundException e) {
            throw new AssumptionViolatedException(e, new TestComponentExpected(ApplicationRelease.class, applicationReleaseUID, "application release"));
        }

        // set middleware profile version (as overriden by each sample)
        applicationRelease.setMiddlewareProfileVersion(middlewareProfileVersion);
        try {
            applicationRelease = itConfiguration.getManageApplicationRelease().updateApplicationRelease(applicationRelease);
        } catch (ApplicationReleaseNotFoundException e) {
            throw new AssumptionViolatedException(e, new TestComponentExpected(ApplicationRelease.class, applicationReleaseUID, "application release update"));
        }
        return applicationRelease;
    }

    private Application createApplication(PaasUser paasUser) {
        String applicationUID;
        try {
            SampleAppFactory logicalModelCatalog = itConfiguration.getLogicalModelCatalog();
            applicationUID = itConfiguration.getManageApplication().createPublicApplication(
                    logicalModelCatalog.getAppCode(),
                    logicalModelCatalog.getAppLabel(),
                    logicalModelCatalog.getAppDescription(),
                    null,
                    paasUser.getSsoId());
        } catch (DuplicateApplicationException | PaasUserNotFoundException e) {
            throw new TechnicalException("Unable to create test application", e);
        }
        // fetch application from DB
        try {
            return itConfiguration.getManageApplication().findApplicationByUID(applicationUID);
        } catch (ApplicationNotFoundException e) {
            throw new AssumptionViolatedException(e, new TestComponentExpected(Application.class, applicationUID, "test application"));
        }
    }

    private PaasUser createTestUser() {
        PaasUser paasUser = currentUser;

        itConfiguration.getManagePaasUser().checkBeforeCreatePaasUser(paasUser);

        try {
            paasUser = itConfiguration.getManagePaasUser().findPaasUser(paasUser.getSsoId().getValue());
        } catch (ObjectNotFoundException e) {
            throw new AssumptionViolatedException(e, new TestComponentExpected(PaasUser.class, paasUser.getSsoId().getValue(), "test user"));
        }
        return paasUser;
    }


    /**
     * MiddlewareProfile is owned by logical model instance
     * if null; the DEFAULT_VCD_MIDDLEWARE_PROFILE is used
     *
     * @return
     */
    public String getMiddlewareProfileVersion() {
        String logicalModelMiddlewareProfile = itConfiguration.getLogicalModelCatalog() != null ?
                itConfiguration.getLogicalModelCatalog().getAppReleaseMiddlewareProfile()
                : null;
        if (logicalModelMiddlewareProfile != null) {
            return logicalModelMiddlewareProfile;
        }
        String middlewareProfileToUse = MiddlewareProfile.getDefault().getVersion();
        logger.info("no AppReleaseMiddlewareProfile specified into the LogicalModel, so use the default one : {}", middlewareProfileToUse);
        return middlewareProfileToUse;
    }

    public static void shutdown(PaasServicesEnvITHelper sPaasServicesEnvITHelper) {
        if (sPaasServicesEnvITHelper == null) {
            logger.warn("tear down aborted : no helper found");
            return;
        }
        sPaasServicesEnvITHelper.tearDown();
    }

    public EnvironmentDetailsDto getAndAssumeEnvironmentDetails() {
        EnvironmentDetailsDto environmentDetails = null;
        try {
            environmentDetails = getEnvironmentDetails();
            if (environmentDetails != null) {
                return environmentDetails;
            }
        } catch (ObjectNotFoundException e) {
        }
        throw new AssumptionViolatedException(null, new TestComponentExpected(EnvironmentDetailsDto.class, environmentUID, "test environment"));
    }

    protected class TestComponentExpected extends BaseMatcher {
        private Class testComponentClass;
        private String testComponentId;
        private String details;

        public TestComponentExpected(Class componentClass, String testComponentId, String details) {
            this.testComponentClass = componentClass;
            this.testComponentId = testComponentId;
            this.details = details;
        }

        @Override
        public boolean matches(Object o) {
            return false;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("test component (type:" + testComponentClass.getSimpleName()
                    + ")#" + testComponentId + " should exists : " + details);
        }
    }

    private void waitForStatus(String environmentId, EnvironmentDto.EnvironmentStatusEnum expectedStatus, int timeoutInMinutes) throws ObjectNotFoundException {
        long timeout = System.currentTimeMillis() + timeoutInMinutes * 60 * 1000;

        EnvironmentDto envDto = itConfiguration.getManageEnvironment().findEnvironmentByUID(environmentId);
        while (expectedStatus != envDto.getStatus()) {
            logDebugCurrentWaitingStatus(expectedStatus, envDto);
            if (System.currentTimeMillis() > timeout) {
                Assert.fail("Timeout: environment not " + expectedStatus + " after " + timeoutInMinutes + " minutes");
            }
            if (!envDto.getStatus().toString().endsWith("ING") && envDto.getStatus() != EnvironmentDto.EnvironmentStatusEnum.RUNNING) {
                // In a final step, will not change until an action is requested
                Assert.assertEquals("Activation process failed : " + envDto.getStatusMessage(), expectedStatus, envDto.getStatus());
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                // ignore
            }
            envDto = itConfiguration.getManageEnvironment().findEnvironmentByUID(environmentId);
        }
    }

    /**
     * log the expected state and the current waiting state status message is
     * appended if not null (only)
     *
     * @param expectedStatus status expected
     * @param envDto         env state
     */
    private void logDebugCurrentWaitingStatus(EnvironmentDto.EnvironmentStatusEnum expectedStatus, EnvironmentDto envDto) {
        if (!logger.isDebugEnabled())
            return;
        StringBuilder sb = new StringBuilder();
        sb.append("Waiting for ").append(expectedStatus).append(" environment ").append("; current status : ").append(envDto.getStatus());
        if (envDto.getStatusPercent() > 0) {
            sb.append(" ").append(envDto.getStatusPercent()).append("%");
        }
        String curMessage = envDto.getStatusMessage();
        if (curMessage != null) {
            sb.append(" / ").append(envDto.getStatusMessage());
        }
        logger.debug(sb.toString());
    }

    private void displayEnvironmentDetailsInLogs(EnvironmentDetailsDto environmentDetails) {
        // show splunk logs links (if any)
        List<LinkDto> logsLinkDtosMap = environmentDetails.getSpecificLinkDto(LinkDto.LinkTypeEnum.LOGS_LINK);
        if (logsLinkDtosMap != null && logsLinkDtosMap.size() > 0) {
            logger.info("Environment splunk logs available at: ");
            for (LinkDto link : logsLinkDtosMap) {
                logger.info("  - <a href=\"" + link.getUrl().toExternalForm() + "\">Link type : " + link.getLinkType() + " - Url : " + link.getUrl().toString()
                        + "</a>");
            }
        }
        // show access point (if any)
        List<LinkDto> accessLinkDtosMap = environmentDetails.getSpecificLinkDto(LinkDto.LinkTypeEnum.ACCESS_LINK);
        if (accessLinkDtosMap != null && accessLinkDtosMap.size() > 0) {
            logger.info("Environment access points are: ");
            for (LinkDto link : accessLinkDtosMap) {
                logger.info("  - <a href=\"" + link.getUrl().toExternalForm() + "\">Link type : " + link.getLinkType() + " - Url : " + link.getUrl().toString()
                        + "</a>");
            }
        }
    }

    public void setDefaultConfigurationItName() {
        String finalName = "NoName";
        if (itConfiguration != null && itConfiguration.getLogicalModelCatalog() != null) {
            finalName = itConfiguration.getLogicalModelCatalog().getAppCode();
        }
        logger.info("Renaming itConfiguration to {} (old name: {})",finalName,itConfiguration.getName());
        itConfiguration.setName(finalName);
    }

    /**
     * Ensure that the EARs are deployed and accessible after a stop and a start
     * (old name : testStopStart())
     */
    public void application_should_be_accessible_after_environment_restart() {
        EnvironmentDetailsDto envDetailsDto;
        try {
            EnvironmentDto envDto = getEnvironment();
            Assert.assertEquals("Env should be running at this step", EnvironmentDto.EnvironmentStatusEnum.RUNNING, envDto.getStatus());

            environment_restart();

            envDetailsDto = getEnvironmentDetails();
        } catch (ObjectNotFoundException e) {
            throw new TechnicalException("Unable to find test environment", e);
        }
        Assert.assertEquals("Env should be running at this step", EnvironmentDto.EnvironmentStatusEnum.RUNNING, envDetailsDto.getStatus());
        application_should_be_accessible(true);
    }

    public void environment_restart() throws ObjectNotFoundException {
        ManageEnvironment manageEnvironment = itConfiguration.getManageEnvironment();
        EnvironmentDto envDto;
        EnvironmentDetailsDto envDetailsDto;
        manageEnvironment.stopEnvironment(environmentUID);
        waitForStatus(environmentUID, EnvironmentDto.EnvironmentStatusEnum.STOPPED, environmentStopTimeoutMin);

        envDto = getEnvironment();
        Assert.assertEquals("Env should be stopped at this step", EnvironmentDto.EnvironmentStatusEnum.STOPPED, envDto.getStatus());

        envDetailsDto = getEnvironmentDetails();
        Assert.assertEquals("Env should be stopped at this step", EnvironmentDto.EnvironmentStatusEnum.STOPPED, envDetailsDto.getStatus());

        manageEnvironment.startEnvironment(environmentUID);
        waitForStatus(environmentUID, EnvironmentDto.EnvironmentStatusEnum.RUNNING, environmentStartTimeoutMin);
    }

    public EnvironmentDetailsDto getEnvironmentDetails() throws ObjectNotFoundException {
        if (environmentUID == null) {
            return null;
        }
        ManageEnvironment manageEnvironment = itConfiguration.getManageEnvironment();
        return manageEnvironment.findEnvironmentDetails(environmentUID);
    }

    public EnvironmentOpsDetailsDto getEnvironmentOpsDetails() throws ObjectNotFoundException {
        if (environmentUID == null) {
            return null;
        }
        ManageEnvironment manageEnvironment = itConfiguration.getManageEnvironment();
        return manageEnvironment.findEnvironmentOpsDetailsByUID(environmentUID);
    }

    public EnvironmentDto getEnvironment() throws ObjectNotFoundException {
        if (environmentUID == null) {
            return null;
        }
        ManageEnvironment manageEnvironment = itConfiguration.getManageEnvironment();
        return manageEnvironment.findEnvironmentByUID(environmentUID);
    }

    public <T> T executeRestRequest(LinkDto link, String path, Class<T> clazz) {

        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        if (this.getItConfiguration().isUseHttpIgeProxy()) {
            final String httpProxyHost = this.getItConfiguration().getHttpProxyHost();
            final int httpProxyPort = this.getItConfiguration().getHttpProxyPort();
            logger.info("Use proxy {}:{} to access Simple Probe", httpProxyHost, httpProxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort));

            clientHttpRequestFactory.setProxy(proxy);
        }
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        T result = restTemplate.getForEntity(link.getUrl().toString()+path, clazz).getBody();

        return result;
    }

    //~getters && setters

    public PaasServicesEnvITConfiguration getItConfiguration() {
        return itConfiguration;
    }

    public String getEnvironmentUID() {
        return environmentUID;
    }

    public int getLogicalDeploymentID() {
        return logicalDeploymentID;
    }

    public void setMaxRequests(int maxRequests) {
        this.maxRequests = maxRequests;
    }

    public void setMaxSessions(int maxSessions) {
        this.maxSessions = maxSessions;
    }

    public void setStateful(boolean stateful) {
        isStateful = stateful;
    }

    public void setEnvType(EnvironmentDto.EnvironmentTypeEnum envType) {
        this.envType = envType;
    }

    public void setEnvironmentCreationTimeoutMin(int environmentCreationTimeoutMin) {
        this.environmentCreationTimeoutMin = environmentCreationTimeoutMin;
    }

    public void setWebAppTestAttempts(int webAppTestAttempts) {
        this.webAppTestAttempts = webAppTestAttempts;
    }

    public void setWebAppTestWaitTime(int webAppTestWaitTime) {
        this.webAppTestWaitTime = webAppTestWaitTime;
    }

    public void setEnvironmentStartTimeoutMin(int environmentStartTimeoutMin) {
        this.environmentStartTimeoutMin = environmentStartTimeoutMin;
    }

    public void setEnvironmentStopTimeoutMin(int environmentStopTimeoutMin) {
        this.environmentStopTimeoutMin = environmentStopTimeoutMin;
    }

    public void setEnvironmentDeleteTimeout(int environmentDeleteTimeout) {
        this.environmentDeleteTimeout = environmentDeleteTimeout;
    }

    public void setSkipDeleteEnvironmentAtTheEnd(Boolean skipDeleteEnvironmentAtTheEnd) {
        this.skipDeleteEnvironmentAtTheEnd = skipDeleteEnvironmentAtTheEnd;
    }

}
