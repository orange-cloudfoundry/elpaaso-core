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
package com.francetelecom.clara.cloud.environment.log;

import java.net.URL;

import org.fest.assertions.Assertions;
import org.junit.Test;


public class LogServiceSplunkImplTest {
	
	@Test(expected = IllegalArgumentException.class)
	public void fail_to_find_logs_search_url_when_no_search_base_url_provided() throws Exception {
		// when no base search url has been provided
		new LogServiceSplunkImpl(null);

		// then it should fail
	}

	@Test
	public void should_get_specific_app_logs_search_url() throws Exception {
		BaseSearchURL baseSearchURL = new BaseSearchURL("127.0.0.1", "8080", false);
		LogServiceSplunkImpl logServiceSplunkImpl = new LogServiceSplunkImpl(baseSearchURL);

		// when I get search url for mom paas logs
		URL actual = logServiceSplunkImpl.getAppLogsUrl("f68be117-cece-4811-88a1-f5112a320ed8");

		Assertions.assertThat(actual).isEqualTo(new URL("http://127.0.0.1:8080/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20index%3D%22*%22+source%3D%22tcp%3A12345%22+appname%3D%22f68be117-cece-4811-88a1-f5112a320ed8%22+%5C%5BApp"));

	}

	@Test
	public void should_get_single_app_environment_logs_search_url() throws Exception {
		BaseSearchURL baseSearchURL = new BaseSearchURL("127.0.0.1", "8080", false);
		LogServiceSplunkImpl logServiceSplunkImpl = new LogServiceSplunkImpl(baseSearchURL);
		
		// when I get search url for mom paas logs
		URL actual = logServiceSplunkImpl.getEnvironmentLogsUrl("f68be117-cece-4811-88a1-f5112a320ed8");

		Assertions.assertThat(actual).isEqualTo(new URL("http://127.0.0.1:8080/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20index%3D%22*%22+source%3D%22tcp%3A12345%22+appname%3D%22f68be117-cece-4811-88a1-f5112a320ed8%22"));

	}
	

	@Test
	public void should_get_multiple_apps_environment_logs_search_url() throws Exception {
		BaseSearchURL baseSearchURL = new BaseSearchURL("127.0.0.1", "8080", false);
		LogServiceSplunkImpl logServiceSplunkImpl = new LogServiceSplunkImpl(baseSearchURL);
		
		// when I get search url for mom paas logs
		URL actual = logServiceSplunkImpl.getEnvironmentLogsUrl("f68be117-cece-4811-88a1-f5112a320ed8","56bf442b-1e53-4dde-85e7-8ed9f41e9421");

		Assertions.assertThat(actual).isEqualTo(new URL("http://127.0.0.1:8080/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20index%3D%22*%22+source%3D%22tcp%3A12345%22+appname%3D%22f68be117-cece-4811-88a1-f5112a320ed8%22+OR+appname%3D%2256bf442b-1e53-4dde-85e7-8ed9f41e9421%22"));

	}
	
	@Test
	public void should_get_route_logs_search_url() throws Exception {
		BaseSearchURL baseSearchURL = new BaseSearchURL("127.0.0.1", "8080", false);
		LogServiceSplunkImpl logServiceSplunkImpl = new LogServiceSplunkImpl(baseSearchURL);
	
		// when I get search url for mom paas logs
		URL actual = logServiceSplunkImpl.getRouteLogsUrl("splunk-po.elpaaso.org");

		Assertions.assertThat(actual).isEqualTo(new URL("http://127.0.0.1:8080/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20index%3D%22*%22+source%3D%22tcp%3A12345%22+splunk-po.elpaaso.org+%5C%5BRTR"));

	}

	
}
