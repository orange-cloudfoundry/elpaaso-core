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
package com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure;

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.ServiceActivationStatus;
import com.francetelecom.clara.cloud.archive.ManageArchive;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.techmodel.cf.*;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.google.common.net.InternetDomainName;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.cloudfoundry.client.lib.*;
import org.cloudfoundry.client.lib.domain.*;
import org.cloudfoundry.client.lib.domain.CloudApplication.AppState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.HttpServerErrorException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 *
 */
public class CfAdapterImpl implements CfAdapter {

    public static final Logger logger = LoggerFactory.getLogger(CfAdapterImpl.class);
    private static final int MAX_RETRY = 5;
    private FileFetcherUtil fileFetcherUtil;

    protected URL target;
    protected boolean isUsingHttpProxy;
    protected String httpProxyHost;
    protected int httpProxyPort;
    protected String email;
    protected String password;
    protected boolean trustSelfSignedCerts;
    protected String org;
    protected String space;
    protected InternetDomainName normalizedDomain;

    protected String domain;

    @Autowired
    private ManageArchive archiver;

    /**
     * @param httpProxyHost
     * @param httpProxyPort
     * @param target
     * @param email
     * @param password
     * @param org                  CloudFoundry space to use
     * @param space                CloudFoundry space to use. Expected to be already created.
     * @param domain               optional DNS domain for app (e.g.
     *                             jee-probe.qa.cfapps.redacted-domain.org or jee-probe.qa.cfapps.io)
     * @param trustSelfSignedCerts TODO
     */
    public CfAdapterImpl(String httpProxyHost, int httpProxyPort, URL target, String email, String password, String org, String space, String domain, Boolean trustSelfSignedCerts) {
        this.httpProxyHost = httpProxyHost;
        this.httpProxyPort = httpProxyPort;
        if (target == null) {
            throw new TechnicalException("mandatory target is null");
        }
        this.target = target;
        if (email == null) {
            throw new TechnicalException("mandatory email is null");
        }
        this.email = email;
        if (password == null) {
            throw new TechnicalException("mandatory password is null");
        }
        this.password = password;
        if (org == null) {
            throw new TechnicalException("mandatory org is null");
        }
        this.org = org;
        this.space = space;
        if (domain == null || domain.isEmpty()) {
            throw new TechnicalException("mandatory domain is null or empty");
        }
        if (trustSelfSignedCerts == null) {
            throw new TechnicalException("mandatory trustSelfSignedCerts is null or empty");
        }
        this.trustSelfSignedCerts = trustSelfSignedCerts;
        normalizedDomain = InternetDomainName.from(domain); // as optimization,
        // cache normalized
        // value to ease
        // safe normalized
        // comparisons
        this.domain = normalizedDomain.name();
    }

    protected CloudFoundryOperations login() {
        logger.info("Running on " + target + " on behalf of " + email);
        logger.info("Using space " + space + " of organization " + org + " with domain " + domain);

        CloudCredentials cloudCredentials = new CloudCredentials(email, password);
        HttpProxyConfiguration httpProxyConfiguration = httpProxyConfiguration();

        CloudFoundryClient clientWithinTargetSpace = new CloudFoundryClient(cloudCredentials, target, org, space, httpProxyConfiguration, trustSelfSignedCerts);
        clientWithinTargetSpace.login();
        // createDomain(domain);

        return clientWithinTargetSpace;
    }

    protected CloudFoundryOperations login(String spaceName) {
        logger.info("Running on " + target + " on behalf of " + email);
        logger.info("Using space " + spaceName + " of organization " + org + " with domain " + domain);

        CloudCredentials cloudCredentials = new CloudCredentials(email, password);
        HttpProxyConfiguration httpProxyConfiguration = httpProxyConfiguration();

        CloudFoundryClient clientWithinTargetSpace = new CloudFoundryClient(cloudCredentials, target, org, spaceName, httpProxyConfiguration, trustSelfSignedCerts);
        clientWithinTargetSpace.login();
        // createDomain(domain);

        return clientWithinTargetSpace;
    }

    public HttpProxyConfiguration httpProxyConfiguration() {
        logger.info("Connection settings: isUsingHttpProxy=" + isUsingHttpProxy + " httpProxyHost=" + httpProxyHost + " httpProxyPort=" + httpProxyPort);
        HttpProxyConfiguration httpProxyConfiguration;

        if (isUsingHttpProxy && (getHttpProxyHost() != null)) {
            logger.debug("proxy is active");
            httpProxyConfiguration = new HttpProxyConfiguration(httpProxyHost, httpProxyPort);
        } else {
            logger.debug("proxy is NOT active");
            httpProxyConfiguration = null;
        }
        return httpProxyConfiguration;
    }

    public String getDomain() {
        return domain;
    }

    @Override
    public String getHttpProxyHost() {
        return httpProxyHost;
    }

    @Override
    public int getHttpProxyPort() {
        return httpProxyPort;
    }

    public String getEmail() {
        return email;
    }

    public String getOrg() {
        return org;
    }

    public String getPassword() {
        return password;
    }

    public String getSpace() {
        return space;
    }

    public URL getTarget() {
        return target;
    }

    @Override
    public UUID createApp(final App app, String spaceName) {

        final CloudFoundryOperations cfClient = login(spaceName);
        try {
            createApplicationWithRoutes(app, cfClient);
            adjustApplicationInstance(app, cfClient);
            addEnvironmentVariableToApplication(app, cfClient);

            uploadApplicationBinaries(app, cfClient);

            CloudApplication application = cfClient.getApplication(app.getAppName());

            return application.getMeta().getGuid();

        } finally {
            cfClient.logout();
        }
    }

    private void adjustApplicationInstance(App app, CloudFoundryOperations cfClient) {
        int instanceCount = app.getInstanceCount();
        if (instanceCount > 1) {
            logger.debug("Multiple instance requested for {}. Updating instance count to {}",app.getAppName(),instanceCount);
            cfClient.updateApplicationInstances(app.getAppName(), instanceCount);
        }
    }

    private void uploadApplicationBinaries(final App app, final CloudFoundryOperations cfClient) {
        FileFetcherUtil.FileProcessor fileProcessor = getUploader(app, cfClient);

        MavenReference appBinaries = app.getAppBinaries();
        if (appBinaries.getAccessUrl() == null && app.isOptionalApplicationBinaries()) {
            generateApplicationBinariesAndApplyProcessing(appBinaries, fileProcessor);
        } else {
            logger.debug("upload application binaries from Maven for {}",app.getAppName());
            fileFetcherUtil.fetchMavenReferenceAndApplyProcessing(appBinaries, fileProcessor);
        }
    }

    private void generateApplicationBinariesAndApplyProcessing(MavenReference appBinariesRef, FileFetcherUtil.FileProcessor fileProcessor) {
        logger.info("Generating default application for {}", appBinariesRef);
        File appBinaries;

        switch (appBinariesRef.getExtension()) {
            case "ear":
                appBinaries = archiver.generateMinimalEar(appBinariesRef, "/");
                break;
            case "jar":
                logger.warn("Jar type detected for {}, but generating default war!", appBinariesRef);
            case "war":
                appBinaries = archiver.generateMinimalWar(appBinariesRef, "/");
                break;
            default:
                throw new TechnicalException("Cannot generate default application for " + appBinariesRef + ". Unsupported extension: " + appBinariesRef.getExtension());
        }

        logger.info("Trying to upload {} for {}", appBinaries, appBinariesRef);
        fileFetcherUtil.readFileAndApplyProcessing(appBinaries, fileProcessor);
        FileUtils.deleteQuietly(appBinaries.getParentFile());
    }

    private FileFetcherUtil.FileProcessor getUploader(final App app, final CloudFoundryOperations cfClient) {
        return new FileFetcherUtil.FileProcessor() {
            @Override
            public void process(String filename, String filetype, File file) {
                String canonicalPath;
                try {
                    canonicalPath = file.getCanonicalPath();
                } catch (IOException e) {
                    throw new TechnicalException("unable to display file path:" + file, e);
                }

                if (!file.exists()) {
                    throw new TechnicalException("Expected valid app file at: " + canonicalPath);
                }

                try {
                    cfClient.uploadApplication(app.getAppName(), canonicalPath);
                } catch (IOException e) {
                    throw new TechnicalException("Unable to upload file at: " + canonicalPath, e);
                }
            }
        };
    }

    private void addEnvironmentVariableToApplication(App app, CloudFoundryOperations cfClient) {
        Map<String, String> optionsMap1 = new HashMap<String, String>();

        // java-buildpack debugging. Prints out the  buildpack git command and additional traces.
        logger.debug("prepare debug env variable");
        optionsMap1.put("JBP_LOG_LEVEL", "DEBUG");
        // optionsMap.put("DEBUG_TOGIST_CMD",
        // "echo customized;date;vmstat;ps -AF --cols=2000;vmstat -s");
        Map<String, String> optionsMap = optionsMap1;

        //FIXME : should variables like JBP_LOG_LEVEL set @projection level ?
        logger.debug("prepare env variable wi");
        for (Map.Entry<EnvVariableKey, EnvVariableValue> var : app.listEnvVariables().entrySet()) {
            optionsMap.put(var.getKey().getKey(), var.getValue().getValue());
        }
        logger.debug("set declared env variable for {}",app.getAppName());
        cfClient.updateApplicationEnv(app.getAppName(), optionsMap);
    }

    private void createApplicationWithRoutes(App app, CloudFoundryOperations cfClient) {

        String buildPackUrl = app.getBuildPackUrl();
        String stack = app.getStack();

        int healthCheckTimeOut = 180; // TODO: externalize this ? needed by
        // low startup app (like elpaaso)
        Staging staging = new Staging(null, buildPackUrl, stack, healthCheckTimeOut);

        MavenReference appBinaries = app.getAppBinaries();

        Set<Route> routes = app.getRoutes();
        List<String> uris = new ArrayList<String>();
        for (Route route : routes) {
            uris.add(route.getUri());
        }
        logger.info("Provisionning app: {}" + app);

        cfClient.createApplication(app.getAppName(), staging, app.getDiskSizeMb(),app.getRamMb(), uris, app.getServiceNames());
        
    }

    @Override
    public int peekAppStartStatus(int instanceCount, String appName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            boolean pass = false;
            try {
                logger.info("checking if all " + instanceCount + " instance(s) of " + appName + " have started...");
                InstancesInfo instances = getInstancesWithTimeout(cfClient, appName);

                if (instances == null) {
                    logger.info("Null InstanceInfo returned, staging is in progress or has failed. Will retry");
                    return 0;
                }

                List<InstanceInfo> infos = instances.getInstances();
                int currentNbInstances = 0;
                if (instances != null) {
                    currentNbInstances = instances.getInstances().size();
                }
                if (currentNbInstances == 0) {
                    logger.warn("No instances returned, staging is in progress or has failed. Will retry");
                    return 0;
                }
                if (currentNbInstances != instanceCount) {
                    logger.error("expected " + instanceCount + " instances , but only got:" + currentNbInstances);
                }

                int passCount = 0;
                int instanceIndex = 0;
                for (InstanceInfo info : infos) {
                    InstanceState state1 = info.getState();
                    if (InstanceState.RUNNING.equals(state1)) {
                        passCount++;
                        logger.info("app " + appName + " instance#" + instanceIndex + " is now in desired state:" + state1);
                    } else {
                        logger.info("app " + appName + " instance#" + instanceIndex + " is still in undesired state:" + state1);
                    }
                    instanceIndex++;
                }
                return passCount;
            } catch (StagingErrorException e) {
                // No need to wait more, the staging failed.
                String msg = "Unable to start app, caught unrecoverable exception:" + e;
                throw new TechnicalException(msg, e);
            } catch (CloudFoundryException ex) {
                // ignore (we may get this when staging is still ongoing)
                if (ex instanceof NotFinishedStagingException) {
                    logger.debug("Start status of " + appName + " not yet ready: " + ex);
                } else {
                    logger.info("Issue checking start status of " + appName + " caught: " + ex, ex);
                }
                return 0;
            }
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void startApp(App cfApp, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            CloudApplication.AppState state;
            logger.info("Starting app with name=" + cfApp.getAppName());
            try {
                StartingInfo info = cfClient.startApplication(cfApp.getAppName());
                CloudApplication app;
                app = cfClient.getApplication(cfApp.getAppName());
                state = app.getState();
                logger.info("app is in state " + state);
                logger.info("app staging logs are in:" + info.getStagingFile());
                try {
                    String logs = cfClient.getStagingLogs(info, 0);
                    logger.info("app staging logs:" + logs);
                } catch (Exception e) {
                    logger.info("unable to get app staging logs:" + e);
                }

                logger.info("app uris are:" + app.getUris());

                int count;
                count = cfApp.getInstanceCount();
                if (count > 1) {
                    cfClient.updateApplicationInstances(cfApp.getAppName(), count);
                    app = cfClient.getApplication(cfApp.getAppName());
                    if (count != app.getInstances()) {
                        logger.error("expected instances to be updated to:" + count + ", got:" + app.getInstances());
                    }
                }
            } catch (Exception e) {
                throw new TechnicalException("unable to start app:" + cfApp.getAppName(), e);
            }

            if (!CloudApplication.AppState.STARTED.equals(state)) {
                throw new TechnicalException("Unexpected state after start:" + state);
            }
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void logAppDiagnostics(String appName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            try {
                List<ApplicationLog> crashLogs = cfClient.getRecentLogs(appName);
                logger.info("Crashlogs for " + appName + " are:\n" + crashLogs);
            } catch (Exception e) {
                logger.info("Unable to log diagnostic details (crashlogs) for app=" + appName + ", caught:" + e, e);
            }

            try {
                String jonasLogDirPath = "app/.jonas_base/logs/";

                String logsDirContent = cfClient.getFile(appName, 0, jonasLogDirPath);
                logger.info("logs dir content: \n{}", logsDirContent);
                String jonasLogFileName = getJonasLogFileName(new Date());
                if (logsDirContent != null && logsDirContent.contains(jonasLogFileName)) {
                    String jonasLogsContent = cfClient.getFile(appName, 0, jonasLogDirPath + jonasLogFileName);
                    logger.info("jonasLogs Content: \n{}", jonasLogsContent);
                }
            } catch (Exception e) {
                logger.info("Unable to log diagnostic details (jonasLogs) for app=" + appName + ", caught:" + e);
            }

            try {
                String buildpackDiagnosticLogsPath = "app/.buildpack-diagnostics/buildpack.log";

                String buildpackDiagnosticLogs = cfClient.getFile(appName, 0, buildpackDiagnosticLogsPath);
                logger.info("buildpackDiagnosticLogs content: \n{}", buildpackDiagnosticLogs);
            } catch (Exception e) {
                logger.info("Unable to log diagnostic details (buildpack diagnostic logs) for app=" + appName + ", caught:" + e);
            }

            try {
                ApplicationStats applicationStats = cfClient.getApplicationStats(appName);
                List<InstanceStats> records = applicationStats.getRecords();
                int i = 0;
                for (InstanceStats record : records) {
                    logger.info("Stats for instance #" + i + " of App " + appName + " are: " + ReflectionToStringBuilder.toString(record));
                    i++;
                }
            } catch (Exception e) {
                logger.info("Unable to log diagnostic details (app stats) for app=" + appName + ", caught:" + e, e);
            }
        } finally {
            cfClient.logout();
        }
    }

    public String getJonasLogFileName(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = simpleDateFormat.format(date);
        return "singleServerName-" + formattedDate + ".3.log";
    }

    @Override
    public void stopApp(App cfApp, String spaceName) {
        CloudApplication app;
        String appName = cfApp.getAppName();
        logger.info("Stopping app with name=" + appName);
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            try {
                cfClient.stopApplication(appName);
            } catch (Exception e) {
                throw new TechnicalException("unable to stop app:" + appName, e);
            }
            app = cfClient.getApplication(appName);
            CloudApplication.AppState state = app.getState();

            logger.info("app is in state " + state);

            if (!CloudApplication.AppState.STOPPED.equals(state)) {
                throw new TechnicalException("Unexpected state after start:" + state);
            }
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void deleteApp(App app, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            String appName = app.getAppName();

            deleteRoutesAndSubdomain(app, spaceName);

            logger.info("Deleting app with name=" + appName);
            try {
                cfClient.deleteApplication(appName);
            } catch (Exception e) {
                logger.warn("unable to delete app:" + appName, e);
            }
        } finally {
            cfClient.logout();
        }
    }

    protected void deleteRoutesAndSubdomain(App app, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            Set<Route> routes = app.getRoutes();
            Set<String> subdomainsToDelete = new HashSet<String>();
            for (Route route : routes) {
                logger.info("Deleting route with uri=" + route.getUri());
                if (!InternetDomainName.from(route.getDomain()).equals(normalizedDomain)) {
                    subdomainsToDelete.add(route.getDomain());
                }
                try {
                    cfClient.deleteRoute(route.getHost(), route.getDomain());
                } catch (Exception e) {
                    logger.warn("unable to delete route host=" + route.getHost() + " domain=" + route.getDomain() + " caught:" + e, e);
                    // proceed
                }
            }

            for (String subdomain : subdomainsToDelete) {
                try {
                    logger.info("deleting subdomain=" + subdomain);
                    cfClient.deleteDomain(subdomain);
                } catch (Exception e) {
                    logger.info("unable to delete domain=" + subdomain + " Might be in use by another app? Caught:" + e, e);
                    // proceed
                }
            }
        } finally {
            cfClient.logout();
        }
    }

    private InstancesInfo getInstancesWithTimeout(CloudFoundryOperations client, String appName) {
        int timeoutMs = 1 * 60 * 1000;
        long start = System.currentTimeMillis();
        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e1) {
                // ignore
            }
            try {
                return client.getApplicationInstances(appName);
            } catch (HttpServerErrorException e) {
                // error 500, keep waiting
            }
            long startWaitTime = System.currentTimeMillis() - start;
            if (startWaitTime > timeoutMs) {
                String msg = "Timed out waiting for app startup:" + startWaitTime + " ms (max is:" + timeoutMs + ")";
                logger.error(msg);
                throw new TechnicalException(msg);
            }
        }

    }

    public boolean isUsingHttpProxy() {
        return isUsingHttpProxy;
    }

    public void setUsingHttpProxy(boolean usingHttpProxy) {
        isUsingHttpProxy = usingHttpProxy;
    }

    public void setFileFetcherUtil(FileFetcherUtil fileFetcherUtil) {
        this.fileFetcherUtil = fileFetcherUtil;
    }

    @Override
    public boolean domainExists(String domainNameToCreate, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            List<CloudDomain> existingDomains = cfClient.getDomainsForOrg();
            for (CloudDomain existingDomain : existingDomains) {
                if (domainNameToCreate.equals(existingDomain.getName())) {
                    return true;
                }
            }
            logger.info("Did not find {} within list of domains present on cf: {}", domainNameToCreate, existingDomains);
            return false;
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void createService(UserProvidedService service, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            CloudService cloudService = new CloudService(null, service.getServiceName());
            String syslogDrainUrl = service.getLogUrl();
            cfClient.createUserProvidedService(cloudService, service.getCredentials(),syslogDrainUrl);
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public boolean serviceExists(String serviceName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            return cfClient.getService(serviceName) != null;
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void deleteService(String serviceName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            // delete cloud service
            // will unbind service from all bound application if exists
            cfClient.deleteService(serviceName);
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void deleteAllServices(String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            cfClient.deleteAllServices();
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void bindService(final String appName, final String serviceName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            cfClient.bindService(appName, serviceName);
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void unbindService(final String appName, final String serviceName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            cfClient.unbindService(appName, serviceName);
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public boolean isServiceBound(final String appName, final String serviceName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            CloudApplication cloudApplication = cfClient.getApplication(appName);
            if (cloudApplication == null)
                throw new TechnicalException("application <" + appName + "> not found");
            List<String> services = cloudApplication.getServices();
            if (services == null)
                return false;
            return services.contains(serviceName);
        } finally {
            cfClient.logout();
        }
    }

    public void addDomain(String newNormalizedDomain, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            boolean targetDomainExists = false;
            List<CloudDomain> domainsForOrg = cfClient.getDomainsForOrg();
            for (CloudDomain browsedDomain : domainsForOrg) {
                String browsedDomainName = browsedDomain.getName();
                String normalizedBrowsedDomainName = InternetDomainName.from(browsedDomainName).name();
                if (newNormalizedDomain.equals(normalizedBrowsedDomainName)) {
                    targetDomainExists = true;
                    logger.info("Found domain {} bound to org {}, no need to register it", browsedDomain, getOrg());
                    break;
                }
            }
            if (!targetDomainExists) {
                logger.info("Did not find domain {} bound to org {} among {}, registering one", new Object[]{newNormalizedDomain, domainsForOrg, getOrg()});
                try {
                    cfClient.addDomain(newNormalizedDomain);
                } catch (Exception e) {
                    throw new TechnicalException("Unable to register domain: " + newNormalizedDomain + " for this paas instance, caught:" + e
                            + " Please check another Paas instance/test has not reserved the same domain on the same CF instance.", e);
                }
            }
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public boolean appExists(String appName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            return (cfClient.getApplication(appName) != null);
        } catch (CloudFoundryException e) {
            if (HttpStatus.NOT_FOUND.equals(e.getStatusCode())) {
                return false;
            } else {
                throw e;
            }
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public boolean isAppStarted(String appName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            return (AppState.STARTED.equals(cfClient.getApplication(appName).getState()));
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public boolean isAppStopped(String appName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            return (AppState.STOPPED.equals(cfClient.getApplication(appName).getState()));
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void createService(ManagedService service, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            CloudService cloudService = new CloudService(null, service.getServiceInstance());
            cloudService.setLabel(service.getService());
            cloudService.setPlan(service.getPlan());
            cfClient.createService(cloudService);
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void createSpace(SpaceName spaceName) {
        CloudFoundryOperations cfClient = login();
        try {
            logger.info("creating cloud foundry space <" + spaceName + ">");
            cfClient.createSpace(spaceName.getValue());
            logger.info("cloud foundry space <" + spaceName + "> has been created.");
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void deleteSpace(SpaceName spaceName) {
        CloudFoundryOperations cfClient = login();
        try {
            logger.info("deleting cloud foundry space <" + spaceName + ">");
            cfClient.deleteSpace(spaceName.getValue());
            logger.info("cloud foundry space <" + spaceName + "> has been deleted.");
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public boolean spaceExists(SpaceName spaceName) {
        CloudFoundryOperations cfClient = login();
        try {
            return (cfClient.getSpace(spaceName.getValue()) != null);
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void associateManagerWithSpace(SpaceName spaceName) {
        CloudFoundryOperations cfClient = login(spaceName.getValue());
        try {
            logger.info("associating manager role to cloud foundry space <" + spaceName + "> ...");
            cfClient.associateManagerWithSpace(spaceName.getValue());
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public String getCurrentOrganizationName() {
return getOrg();
    }

    @Override
    public ServiceActivationStatus getServiceInstanceState(String serviceName, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            logger.info("getting activation status for service <" + serviceName + "> in space <" + spaceName + "> ...");
            final CloudServiceInstance serviceInstance = cfClient.getServiceInstance(serviceName);
            return getServiceActivationStatus(serviceName, spaceName, serviceInstance);
        } finally {
            cfClient.logout();
        }
    }

    protected ServiceActivationStatus getServiceActivationStatus(String serviceName, String spaceName, CloudServiceInstance serviceInstance) {
        if (serviceInstance == null) {
            logger.warn("Cannot get status for service <{}> in space <{}>. Will suppose service is deleted and thus last operation state is SUCCEEDED>", serviceName, spaceName);
            return ServiceActivationStatus.ofService(serviceName, spaceName).hasSucceeded();
        }
        if (OperationState.IN_PROGRESS.equals(serviceInstance.getLastOperation().getState()))
            return ServiceActivationStatus.ofService(serviceName, spaceName).isPending(serviceInstance.getLastOperation().getType() + " is still in progress...");
        if (OperationState.SUCCEEDED.equals(serviceInstance.getLastOperation().getState()))
            return ServiceActivationStatus.ofService(serviceName, spaceName).hasSucceeded();
        if (OperationState.FAILED.equals(serviceInstance.getLastOperation().getState()))
            return ServiceActivationStatus.ofService(serviceName, spaceName).hasFailed(serviceInstance.getLastOperation().getType() + " has failed");
        throw new TechnicalException("unable to extract service activation status. service instance state <" + serviceInstance.getLastOperation().getState() + "> is unknown");
    }

    @Override
    public void associateDeveloperWithSpace(SpaceName spaceName) {
        CloudFoundryOperations cfClient = login(spaceName.getValue());
        try {
            logger.info("associating developer role to cloud foundry space <" + spaceName + "> ...");
            cfClient.associateDeveloperWithSpace(spaceName.getValue());
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void associateAuditorWithSpace(SpaceName spaceName) {
        CloudFoundryOperations cfClient = login(spaceName.getValue());
        try {
            logger.info("associating auditor role to cloud foundry space <" + spaceName + "> ...");
            cfClient.associateAuditorWithSpace(spaceName.getValue());
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public SpaceName getValidSpaceName(String nameSuffix) {
        CloudFoundryOperations cfClient = login();
        try {
            int retry = 0;
            while (retry < MAX_RETRY) {
                SpaceName randomSpaceName = SpaceName.randomSpaceNameWithSuffix(nameSuffix);
                if (cfClient.getSpace(randomSpaceName.getValue()) == null)
                    return randomSpaceName;
                retry++;
            }
            throw new TechnicalException("Fail to get a valid space name after " + MAX_RETRY + " attempts.");
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public boolean routeExists(Route route, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            try {
                List<CloudRoute> routes = cfClient.getRoutes(route.getDomain());
                logger.info("found routes " + routes + " for domain <" + route.getDomain() + ">");
                if (routes == null || routes.size() == 0)
                    return false;
                for (CloudRoute existingRoute : routes) {
                    if (existingRoute.getHost().equals(route.getHost()))
                        return true;
                }
            } catch (IllegalArgumentException e) {
                // raised when domain not found
                return false;
            }
            return false;
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public void deleteRoute(Route route, String spaceName) {
        CloudFoundryOperations cfClient = login(spaceName);
        try {
            cfClient.deleteRoute(route.getHost(), route.getDomain());
        } finally {
            cfClient.logout();
        }
    }

    @Override
    public RouteUri createRoute(final Route route, String spaceName) {
        final CloudFoundryOperations cfClient = login(spaceName);
        try {
            return getRetryTemplate(MAX_RETRY).execute(new RetryCallback<RouteUri, CloudFoundryException>() {
                @Override
                public RouteUri doWithRetry(RetryContext context) throws CloudFoundryException {
                    if (context.getRetryCount() == 0) {
                        logger.info("creating cloud foundry route with uri <" + route.getUri() + ">");
                        cfClient.addRoute(route.getHost(), route.getDomain());
                        return new RouteUri(route.getUri());
                    } else {
                        RouteUri candidateRouteUri = route.candidateRouteUri();
                        logger.info("creating cloud foundry route with uri <" + candidateRouteUri + ">");
                        cfClient.addRoute(candidateRouteUri.getHost(), candidateRouteUri.getDomain());
                        return candidateRouteUri;
                    }
                }
            });
        } finally {
            cfClient.logout();
        }
    }

    /**
     * get a spring customized retry template
     *
     * @param retryAttempts
     * @return a customized retry template
     */
    private static RetryTemplate getRetryTemplate(int retryAttempts) {
        // we set the retry time out
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(retryAttempts);

        // our retry service
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(retryPolicy);
        return retryTemplate;
    }

}
