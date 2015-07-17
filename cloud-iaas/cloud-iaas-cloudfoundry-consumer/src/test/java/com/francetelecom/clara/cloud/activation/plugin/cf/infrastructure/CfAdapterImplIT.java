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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assume.assumeThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.cloudfoundry.client.lib.CloudFoundryOperations;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;
import com.google.common.net.InternetDomainName;

@RunWith(org.mockito.runners.MockitoJUnitRunner.class)
public class CfAdapterImplIT {

	private static final String SPACE = "space";

	private CfAdapterImpl cfAdapter;

	@Mock
	CloudFoundryOperations cfClient;

	@Before
	public void setUp() throws MalformedURLException {
		URL apiUrl = new URL("http://localhost");
		cfAdapter = new CfAdapterImpl("proxy", 3128, apiUrl, "email", "pwd", "org", SPACE, "cfapps.redacted-domain.org", true) {

			@Override
			protected CloudFoundryOperations login(String spaceName) {
				return cfClient;
			}

		};
	}

	@Test
	public void creates_an_http_configuration_from_configured_credentials_with_proxy() {
		// given
		cfAdapter.setUsingHttpProxy(true);

		// when
		// then
		assertThat(cfAdapter.httpProxyConfiguration()).isNotNull();
		assertThat(cfAdapter.httpProxyConfiguration().getProxyHost()).isEqualTo("proxy");
		assertThat(cfAdapter.httpProxyConfiguration().getProxyPort()).isEqualTo(3128);
	}

	@Test
	public void creates_an_http_configuration_from_configured_credentials_without_proxy() {
		// given
		cfAdapter.setUsingHttpProxy(false);

		// when
		// then
		assertThat(cfAdapter.httpProxyConfiguration()).isNull();
	}

	@Test
	public void the_class_InternetDomainName_normalizes_domain_names() {
		assertThat(InternetDomainName.from("UPPERCASE.net").name()).isEqualTo("uppercase.net");
	}

	@Test
	public void predicts_jonas_log_file_name() {
		Calendar calendar = new GregorianCalendar();
		calendar.set(2013, 8 - 1, 28); // months start at 0
		Date time = calendar.getTime();
		String jonasLogFileName = cfAdapter.getJonasLogFileName(time);
		assertThat(jonasLogFileName).isEqualTo("singleServerName-2013-08-28.3.log");
	}

	@Test
	public void registersUriAndTheirParentDomain() {
		// given
		TechnicalDeployment td = new TechnicalDeployment("");
		Space space = new Space(td);
		space.activate(new SpaceName(SPACE));
		Route route = new Route(new RouteUri("webGui.CfConsummerIT.gberche-dev-box.guillaume-berche.cfapps.redacted-domain.org"), "", space, td);

		// when
		cfAdapter.createRoute(route, SPACE);

		// then
		verify(cfClient).addRoute(Mockito.eq("webgui"), Mockito.eq("cfconsummerit.gberche-dev-box.guillaume-berche.cfapps.redacted-domain.org")); // register
																																			// the
																																			// route
																																			// lower
																																			// case
	}

	/*
	 * @Test
	 * 
	 * @Ignore("handled at application level") public void
	 * handles_conflicting_uri_without_duplicates_and_normalizes_uris() {
	 * //given CfAdapter.AppConflictHandler appConflictHandler = new
	 * CfAdapter.AppConflictHandler() {
	 * 
	 * @Override public String getNextUriCandidate(String uri, int attempts) {
	 * assertThat(uri).doesNotMatch("c\\d+\\-.*"); //should not be passed
	 * previous deconflicting prefixes return "c"+ attempts + "-" + uri; } };
	 * 
	 * doThrow(new
	 * org.cloudfoundry.client.lib.CloudFoundryException(HttpStatus.BAD_REQUEST,
	 * "busy route 1")) .doThrow(new
	 * org.cloudfoundry.client.lib.CloudFoundryException(HttpStatus.BAD_REQUEST,
	 * "busy route 2")) .doNothing() .when(cfClient).addRoute(anyString(),
	 * anyString()); doThrow(new
	 * org.cloudfoundry.client.lib.CloudFoundryException(HttpStatus.BAD_REQUEST,
	 * "busy app name 1")) .doThrow(new
	 * org.cloudfoundry.client.lib.CloudFoundryException(HttpStatus.BAD_REQUEST,
	 * "busy app name 2")) .doNothing()
	 * .when(cfClient).createApplication(anyString(), any(Staging.class),
	 * anyInt(), anyList(), anyList());
	 * 
	 * //when String requestedUri =
	 * "webGui.CfConsummerIT.gberche-dev-box.guillaume-berche.cfapps.redacted-domain.org"
	 * ; String conflictFreeUri =
	 * cfAdapter.getAndRegisterNormalizedConflictFreeUri(requestedUri,
	 * appConflictHandler, SPACE);
	 * 
	 * //then verify(cfClient).addDomain(
	 * "cfconsummerit.gberche-dev-box.guillaume-berche.cfapps.redacted-domain.org");
	 * //register parent domain assertThat(conflictFreeUri).startsWith("c");
	 * //no accumulated prefixes such as c1-c2-
	 * assertThat(conflictFreeUri).endsWith
	 * ("webgui.cfconsummerit.gberche-dev-box.guillaume-berche.cfapps.redacted-domain.org"
	 * ); //no accumulated prefixes such as c1-c2-
	 * assertThat(InternetDomainName.
	 * from(conflictFreeUri).name()).isEqualTo(conflictFreeUri); //uri is
	 * normalized }
	 * 
	 * @Test
	 * 
	 * @Ignore("handled at application level") public void
	 * should_register_routes_but_not_topdomain_at_env_instanciation() {
	 * assumeThat("inconsistent domain", cfAdapter.getDomain(),
	 * is("cfapps.redacted-domain.org")); //given CfAdapter.AppConflictHandler
	 * appConflictHandler = mock(CfAdapter.AppConflictHandler.class);
	 * 
	 * //when String requestedUri =
	 * "myenv-jeeprobewe-myjeeprobetestc-uat.cfapps.redacted-domain.org"; String
	 * conflictFreeUri =
	 * cfAdapter.getAndRegisterNormalizedConflictFreeUri(requestedUri,
	 * appConflictHandler, SPACE);
	 * 
	 * //then verify(cfClient, never()).addDomain("cfapps.redacted-domain.org");
	 * verify(cfClient
	 * ).addRoute(Mockito.endsWith("myenv-jeeprobewe-myjeeprobetestc-uat"),
	 * Mockito.eq("cfapps.redacted-domain.org")); }
	 */

	@Test
	public void deleting_application_also_deletes_routes_domains_and_ignores_deletion_failures() {
		assumeThat("inconsistent domain", cfAdapter.getDomain(), is("cfapps.redacted-domain.org"));

		TechnicalDeployment td = new TechnicalDeployment("");
		Space space = new Space(td);
		App app = new App(td, space, "appName", mock(MavenReference.class), "java", 512, 1);
		Route route1 = new Route(new RouteUri("host1.mysubdomain.cfapps.redacted-domain.org"), "root1", space, td);
		Route route2 = new Route(new RouteUri("host2.mysubdomain.cfapps.redacted-domain.org"), "root2", space, td);
		app.mapRoute(route1);
		app.mapRoute(route2);

		doThrow(new org.cloudfoundry.client.lib.CloudFoundryException(HttpStatus.NOT_FOUND, "simulating no such app")).when(cfClient).deleteApplication(anyString());
		doThrow(new org.cloudfoundry.client.lib.CloudFoundryException(HttpStatus.NOT_FOUND, "simulating no such route")).when(cfClient).deleteRoute(anyString(), anyString());
		doThrow(new org.cloudfoundry.client.lib.CloudFoundryException(HttpStatus.NOT_FOUND, "simulating no such domain")).when(cfClient).deleteDomain(anyString());

		// when
		cfAdapter.deleteApp(app, SPACE);

		// then
		verify(cfClient).deleteApplication("appName");
		verify(cfClient).deleteRoute("host1", "mysubdomain.cfapps.redacted-domain.org");
		verify(cfClient).deleteRoute("host2", "mysubdomain.cfapps.redacted-domain.org");
		verify(cfClient).deleteDomain("mysubdomain.cfapps.redacted-domain.org");
	}

	@Test
	public void deleting_application_should_not_delete_paas_domain_shared_by_all_apps() {
		assumeThat("inconsistent domain", cfAdapter.getDomain(), is("cfapps.redacted-domain.org"));

		TechnicalDeployment td = new TechnicalDeployment("");
		Space space = new Space(td);
		App app = new App(td, space, "appName", mock(MavenReference.class), "java", 512, 1);
		Route route1 = new Route(new RouteUri("myenv-jeeprobewe-myjeeprobetestc-uat.cfapps.redacted-domain.org"), "root1", space, td);
		app.mapRoute(route1);

		// when
		cfAdapter.deleteApp(app, SPACE);

		// then
		verify(cfClient).deleteRoute("myenv-jeeprobewe-myjeeprobetestc-uat", "cfapps.redacted-domain.org");
		verify(cfClient, never()).deleteDomain("cfapps.redacted-domain.org");
	}

}
