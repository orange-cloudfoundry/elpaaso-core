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

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cloudfoundry.client.lib.CloudCredentials;
import org.cloudfoundry.client.lib.CloudFoundryException;
import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.cloudfoundry.client.lib.HttpProxyConfiguration;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudDomain;
import org.cloudfoundry.client.lib.domain.CloudRoute;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;

/*
 * Copyright 2009-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Test class for V2 cloud controller.
 * 
 * You need to create organization, space and user account via the portal and
 * set these values using system properties.
 * 
 * @author Thomas Risberg
 */
public abstract class AbstractCfAdapterIT {

	public static final Logger logger = LoggerFactory.getLogger(AbstractCfAdapterIT.class);

	@Autowired
	private MvnRepoDao mvnRepoDao;

	@Autowired
	private SampleAppProperties sampleAppProperties;

	@Autowired
	@Qualifier("datacenter")
	protected String datacenter;

	protected String ccEmail;

	protected CfAdapterImpl cfAdapter;

	protected String cfSubdomain;

	protected String cfDefaultSpace;

	@Value("${cf.ccng.use_proxy}")
	protected boolean isUsingHttpProxy;

	@Value("${cf.ccng.proxyHost}")
	protected String httpProxyHost;

	@Value("${cf.ccng.proxyPort}")
	protected int httpProxyPort;

	private CloudFoundryOperations cfClient;
	/**
	 * The default domain without owner, i.e. cfapps.io
	 */
	protected static String defaultDomainName = null;

	protected static String defaultNamespace(String email) {
		String name_without_domain = email.substring(0, email.indexOf('@')).replaceAll("\\.", "-").replaceAll("\\+", "-").replaceAll("_", "-");
		// Keep it short, 5 chars max
		name_without_domain = StringUtils.left(name_without_domain, 5);
		return name_without_domain;
	}

	@Test
	public void creates_the_requested_domain_if_missing() {
		// given
		String domainNameToCreate = "newdomain.tocreate.com";
		try {

			if (cfAdapter.domainExists(domainNameToCreate, cfDefaultSpace)) {
				cfClient.deleteDomain(domainNameToCreate);
			}

			// when
			cfAdapter.addDomain(domainNameToCreate, cfDefaultSpace);

			// then
			boolean domainWasCreated = cfAdapter.domainExists(domainNameToCreate, cfDefaultSpace);
			assertThat(domainWasCreated).as(domainNameToCreate).isTrue();
		} finally {
			if (cfAdapter.domainExists(domainNameToCreate, cfDefaultSpace)) {
				cfClient.deleteDomain(domainNameToCreate);
			}
		}

	}

	@Test
	@Ignore("Re-enable if you need to debug jonas buildpack and compare against native buildpack")
	public void provisions_starts_stops_deletes_a_small_war_in_tomcat() throws IOException {
		MavenReference mavenReference = new MavenReference("groupId", "artefactId", "version", "war");
		URL accessUrl = CfAdapterImpl.class.getClassLoader().getResource("apps/hello-env.war");
		mavenReference.setAccessUrl(accessUrl);

		String testRequestPath = "/"; // default buildpacks "mount" any wars to
										// ROOT
		provisionStartStopDeletesApp(mavenReference, getJavaBuildpackUrl(), getTestAppName() + "-" + "travel_test-" + "upload1", 512, testRequestPath);
	}

	@Test
	@Ignore("Re-enable if you need to debug jeeprobe and compare against hello-env")
	public void provisions_starts_stops_deletes_a_small_war_on_jonas() throws IOException {
		MavenReference mavenReference = new MavenReference("groupId", "artefactId", "version", "war");
		URL accessUrl = CfAdapterImpl.class.getClassLoader().getResource("apps/hello-env.war");
		mavenReference.setAccessUrl(accessUrl);

		String testRequestPath = "app/"; // buildpacks "mount" any wars to
											// app.war, and jonas exposes them
											// as app
		provisionStartStopDeletesApp(mavenReference, getJonasBuildpackUrl(), getTestAppName() + "-" + "travel_test-" + "upload1", 512, testRequestPath);
	}

	@Test
	public void provisions_starts_stops_deletes_jeeprobe_ear() throws IOException {
		MavenReference jeeProbeMavenReference = sampleAppProperties.getMavenReference("jeeprobe","ear");
		MavenReference jeeProbeEarRef = mvnRepoDao.resolveUrl(jeeProbeMavenReference);

		String testRequestPath = "/jeeprobe/"; // JeeProbe EAR specified a
												// context-root which is honored
												// by Jonas
		int ramMb = 1024; // temporary oversized to 1 Gb to workaround warden
							// bug
		provisionStartStopDeletesApp(jeeProbeEarRef, getJonasBuildpackUrl(), getTestAppName() + "-" + "jeeProbe", ramMb, testRequestPath);
	}

	@Test
	public void should_create_then_delete_a_route() {
		TechnicalDeployment td = new TechnicalDeployment("depl");
		Space space = new Space(td);
		Route route = new Route(new RouteUri("demo-elpaasofrontend13beta." + getTestDomainName()), "root1", space, td);

		Assertions.assertThat(cfAdapter.routeExists(route, cfDefaultSpace)).isFalse();

		cfAdapter.createRoute(route, cfDefaultSpace);
		Assertions.assertThat(cfAdapter.routeExists(route, cfDefaultSpace)).isTrue();

		cfAdapter.deleteRoute(route, cfDefaultSpace);
		Assertions.assertThat(cfAdapter.routeExists(route, cfDefaultSpace)).isFalse();

	}
	
	@Test(expected=CloudFoundryException.class)
	@Ignore
	public void fail_to_create_2_routes_with_same_host_for_same_domain() {
		TechnicalDeployment td = new TechnicalDeployment("depl");
		Space space = new Space(td);
		Route route1 = new Route(new RouteUri("test-host-conflict." + getTestDomainName()), "root1", space, td);
		Route route2 = new Route(new RouteUri("test-host-conflict." + getTestDomainName()), "root1", space, td);

		cfAdapter.createRoute(route1, cfDefaultSpace);
		cfAdapter.createRoute(route2, cfDefaultSpace);
		
		//then it should fail

	}

	@Test
	@Ignore("should be tested at upper level")
	public void handles_uri_conflicts() {
		MavenReference mavenReference = new MavenReference("groupId", "artefactId", "version", "war");
		URL accessUrl = CfAdapterImpl.class.getClassLoader().getResource("apps/hello-env.war");
		mavenReference.setAccessUrl(accessUrl);

		TechnicalDeployment td = new TechnicalDeployment("depl");
		Space space = new Space(td);
		space.activate(new SpaceName(cfDefaultSpace));

		final App cfApp = new App(td, space, getTestAppName(), mavenReference, getJonasBuildpackUrl(), 512, 1);
		Route route1 = new Route(new RouteUri("demo-elpaasofrontend13beta." + getTestDomainName()), "root1", space, td);
		Route route2 = new Route(new RouteUri("demo-elpaasobackend13beta." + getTestDomainName()), "root2", space, td);

		cfApp.mapRoute(route1);
		cfApp.mapRoute(route2);

		cfAdapter.createApp(cfApp, cfDefaultSpace);

		CloudApplication app = cfClient.getApplication(cfApp.getAppName());
		assertNotNull(app);
		assertEquals(CloudApplication.AppState.STOPPED, app.getState());
		for (String uri : cfApp.getRouteURIs()) {
			assertThat(uri.startsWith("c")).isFalse(); // should not have uri
														// conflicts on the
														// first app.
		}

		// Try to provision a second app with with same params (name and uri)
		final App secondApp = new App(td, space, getTestAppName() + "-2", cfApp.getAppBinaries(), cfApp.getBuildPackUrl(), cfApp.getRamMb(), 1);
		secondApp.mapRoute(route1);
		secondApp.mapRoute(route2);

		cfAdapter.createApp(secondApp, cfDefaultSpace);

		app = cfClient.getApplication(secondApp.getAppName());
		assertNotNull(app);
		assertEquals(CloudApplication.AppState.STOPPED, app.getState());

		for (String uri : secondApp.getRouteURIs()) {
			assertThat(uri).as("conflict-prefixed uri").startsWith("c");
		}
	}

	private void provisionStartStopDeletesApp(MavenReference mavenReference, String buildpackUrl, String appName, int ramMb, String testRequestPath) throws IOException {
		TechnicalDeployment td = new TechnicalDeployment("depl");
		Space space = new Space(td);
		App cfApp = new App(td, space, appName, mavenReference, buildpackUrl, ramMb, 1);
		Route route = new Route(new RouteUri("webgui." + getTestDomainName()), testRequestPath, space, td);
		cfApp.mapRoute(route);

		// when
		cfAdapter.createRoute(route, cfDefaultSpace);
		// then route should have been created
		assertThat(cfAdapter.routeExists(route, cfDefaultSpace)).isTrue();

		// when
		cfAdapter.createApp(cfApp, cfDefaultSpace);
		// then app should have been created
		assertThat(cfAdapter.appExists(appName, cfDefaultSpace)).isTrue();
		// then app should be stopped
		assertThat(cfAdapter.isAppStopped(appName, cfDefaultSpace)).isTrue();

		cfAdapter.startApp(cfApp, cfDefaultSpace);
		// because starting an app is async
		pollAppStartStatus(cfApp);
		// then app should be started
		assertThat(cfAdapter.isAppStarted(appName, cfDefaultSpace)).isTrue();

		String firstUri = cfApp.getRouteURIs().get(0);

		try {
			Map<String, String> logs = cfClient.getLogs(appName);
			logger.info("logs for " + appName + "are:\n" + logs);
		} catch (Exception e) {
			logger.info("unable to get app log for " + appName, e);
		}

		testRemoteAppWebGui(firstUri, testRequestPath, appName);

		// when
		cfAdapter.stopApp(cfApp, cfDefaultSpace);
		// then app should be stopped
		assertThat(cfAdapter.isAppStopped(appName, cfDefaultSpace)).isTrue();

		// when
		cfAdapter.deleteApp(cfApp, cfDefaultSpace);
		// then app should have been removed
		assertThat(cfAdapter.appExists(appName, cfDefaultSpace)).isFalse();
		// then domain should have been removed
		assertThat(cfAdapter.domainExists(route.getDomain(), cfDefaultSpace)).isFalse();
		// then route should have been removed
		assertThat(cfAdapter.routeExists(route, cfDefaultSpace)).isFalse();

	}

	/**
	 * 
	 * @param virtualHost
	 * @param testRequestPath
	 * @param appNameToDumpDiagnosticLogs
	 *            the name of the app to display the logs of if the webgui is
	 *            unreacheable, or null to no display such logs.
	 * @throws IOException
	 */
	protected void testRemoteAppWebGui(String virtualHost, String testRequestPath, String appNameToDumpDiagnosticLogs) throws IOException {

		HttpClientConfig defaultProxyConfig = getHttpProxyConfigToQueryWebGuiRoutes();
		int retry = 0;
		int maxRetries = 10;

		String testResponse = null;
		StringBuffer testFailureDetails = new StringBuffer();
		do {
			logger.info("Querying " + getWebGuiURL(virtualHost, testRequestPath) + " using proxyConfig=" + defaultProxyConfig + " ...");
			try {
				testResponse = fetchRoutedContentAsString(getWebGuiURL(virtualHost, testRequestPath), defaultProxyConfig);
				break;
			} catch (HttpResponseException e) {
				String msg = "Querying " + getWebGuiURL(virtualHost, testRequestPath) + " ... done. Caught: " + e;
				logger.info(msg);
				testFailureDetails.append(msg);
				testFailureDetails.append("\n");
				if (e.getStatusCode() == 404) {
					logger.info("Sleeping for 10s before next retry (" + retry + "/" + maxRetries + ")");

					if (appNameToDumpDiagnosticLogs != null) {
						cfAdapter.logAppDiagnostics(appNameToDumpDiagnosticLogs, cfDefaultSpace);
					}
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e1) {
						// Ignore
					}
					retry++;
				} else {
					break; // no retries for unexpected errors
				}
			}
		} while (retry < maxRetries);

		logger.info("Querying " + getWebGuiURL(virtualHost, testRequestPath) + " ... done. Returned: " + StringUtils.abbreviate(testResponse, 50));
		assertThat(testResponse).as("webGui response").isNotNull().isNotEmpty();
		assertThat(retry).overridingErrorMessage("Expecting zero retries on webGui polling, got " + retry + " retries. Details:" + testFailureDetails).isLessThanOrEqualTo(1); // Expect
		// the app to immediately return a valid response, retries are only here
		// to help diagnostics
	}

	protected HttpClientConfig getHttpProxyConfigToQueryWebGuiRoutes() {
		// By default consider routes are reacheable without proxies
		HttpClientConfig defaultProxyConfig = new HttpClientConfig() {
			@Override
			public void applyConfig(DefaultHttpClient httpclient) {
			}

			@Override
			public String toString() {
				return "HttpClientConfig { direct connection, no proxy }";
			}
		};
		return defaultProxyConfig;
	}

	interface HttpClientConfig {
		void applyConfig(DefaultHttpClient httpclient);
	}

	private String fetchRoutedContentAsString(String uri, HttpClientConfig httpClientConfig) throws IOException {
		String contentA;

		DefaultHttpClient httpclient = new DefaultHttpClient();
		if (httpClientConfig != null) {
			httpClientConfig.applyConfig(httpclient);
		}
		try {
			HttpGet httpget = new HttpGet(uri);
			contentA = httpclient.execute(httpget, new BasicResponseHandler());
		} finally {
			httpclient.getConnectionManager().shutdown();
		}
		return contentA;
	}

	public String getTestAppName() {
		return "AbstractCfAdapterIT-" + datacenter + "-" + defaultNamespace(ccEmail) + cfSubdomain;
	}

	public String getTestDomainName() {
		return "cfconsummerit." + datacenter + "." + defaultNamespace(ccEmail) + "." + cfSubdomain; // lower
																									// case
	}

	protected String getWebGuiURL(String virtualHost, String testRequestPath) {
		if (testRequestPath != null && testRequestPath.startsWith("/"))
			return "http://" + virtualHost + testRequestPath;
		else
			return "http://" + virtualHost + "/" + testRequestPath;
	}

	public HttpProxyConfiguration httpProxyConfiguration() {
		logger.info("cfAdapter Connection settings: isUsingHttpProxy=" + cfAdapter.isUsingHttpProxy + " httpProxyHost=" + cfAdapter.httpProxyHost + " httpProxyPort="
				+ cfAdapter.httpProxyPort);
		HttpProxyConfiguration httpProxyConfiguration;

		if (cfAdapter.isUsingHttpProxy && (cfAdapter.httpProxyHost != null)) {
			httpProxyConfiguration = new HttpProxyConfiguration(cfAdapter.httpProxyHost, cfAdapter.httpProxyPort);
		} else {
			httpProxyConfiguration = null;
		}
		return httpProxyConfiguration;
	}

	@Before
	public void setUp() {
		cfClient = CFClientFactory.login(new CloudCredentials(cfAdapter.getEmail(), cfAdapter.getPassword()), cfAdapter.getTarget(), cfAdapter.getSpace(), cfAdapter.getOrg(),
				cfAdapter.getDomain(), cfAdapter.trustSelfSignedCerts, httpProxyConfiguration());
		List<CloudApplication> applications = cfClient.getApplications();
		for (CloudApplication application : applications) {
			if (application.getName().contains(getTestAppName())) {
				cfClient.deleteApplication(application.getName());
			}
			List<String> boundServices = application.getServices();
			List<String> services = boundServices;
			for (String service : services) {
				cfClient.deleteService(service);
			}
			clearDomain(getTestDomainName(), true);
			clearDomain(getDefaultDomain(), false);
		}
		cfAdapter.addDomain(getTestDomainName(), cfDefaultSpace);
	}

	private String getDefaultDomain() {
		for (CloudDomain domain : cfClient.getDomainsForOrg()) {
			if (domain.getOwner().getName().equals("none")) {
				return domain.getName();
			}
		}
		return null;
	}

	@After
	public void tearDown() {
		List<CloudApplication> cloudApps = cfClient.getApplications();
		for (CloudApplication cloudApp : cloudApps) {
			if (cloudApp.getName().contains(getTestAppName())) {
				cfClient.deleteApplication(cloudApp.getName());
			}
		}

		List<CloudService> cloudServices = cfClient.getServices();
		for (CloudService cloudService : cloudServices) {
			if (cloudService.getName().contains(getTestAppName())) {
				cfClient.deleteService(cloudService.getName());
			}
		}

		clearDomain(getTestDomainName(), true);
		clearDomain(getDefaultDomain(), false);

		cfClient.logout();
	}

	private void clearDomain(String domainToClear, boolean deleteDomain) {
		for (CloudDomain domain : cfClient.getDomainsForOrg()) {
			String domainName = domain.getName();
			if (domainName.contains(domainToClear)) {
				List<CloudRoute> routes = cfClient.getRoutes(domainName);
				for (CloudRoute route : routes) {
					cfClient.deleteRoute(route.getHost(), route.getDomain().getName());
				}
				if (deleteDomain) {
					cfClient.deleteDomain(domainName);
				}
			}
		}
	}

	public abstract String getJonasBuildpackUrl();

	public abstract String getJavaBuildpackUrl();

	private void pollAppStartStatus(App app) {
		try {

			int timeoutMs = 5 * 60 * 1000;
			long start = System.currentTimeMillis();
			boolean pass = false;
			int i;
			for (i = 0; i < 50 && pass == false; i++) {
				int nbPass = cfAdapter.peekAppStartStatus(app.getInstanceCount(), app.getAppName(), cfDefaultSpace);
				pass = (nbPass == app.getInstanceCount());
				if (!pass) {
					long elapsed = System.currentTimeMillis() - start;
					if (elapsed > timeoutMs) {
						logger.info("timeout waiting for app" + app.getAppName() + " to start: polled " + i + "times and waited " + elapsed + "ms (max is:" + timeoutMs + " ms)");
						break;
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// ignore
					}
				}
			}
			boolean startWasOk = pass;
			if (!startWasOk) {
				cfAdapter.logAppDiagnostics(app.getAppName(), cfDefaultSpace);
				long elapsed = System.currentTimeMillis() - start;
				throw new TechnicalException("Unable to successfully start " + app.getAppName() + ": polled " + i + " times and waited " + elapsed + " ms (max is:" + timeoutMs
						+ " ms)");
			} else {
				logger.info("all " + app.getInstanceCount() + " instance(s) have properly started");
			}
		} catch (Exception e) {
			throw new TechnicalException("unable to start app:" + app.getAppName(), e);
		}
	}

}
