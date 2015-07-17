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

import org.fest.assertions.Assertions;
import org.junit.Test;


public class SplunkQueryBuilderTest {

	@Test
	public void can_set_a_source() throws Exception {
		SplunkQueryBuilder query = new SplunkQueryBuilder(new BaseSearchURL("localhost", "80", false));
		String build = query.source("12345").build().toString();
		Assertions.assertThat(build).isEqualTo("http://localhost:80/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20source%3D%2212345%22");
	}
	
	@Test
	public void can_set_an_index() throws Exception {
		SplunkQueryBuilder query = new SplunkQueryBuilder(new BaseSearchURL("localhost", "80", false));
		String build = query.index("*").build().toString();
		Assertions.assertThat(build).isEqualTo("http://localhost:80/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20index%3D%22*%22");
	}
	
	@Test
	public void can_filter_by_appname() throws Exception {
		SplunkQueryBuilder query = new SplunkQueryBuilder(new BaseSearchURL("localhost", "80", false));
		String build = query.appName("joyn").onlyAppLog().build().toString();
		Assertions.assertThat(build).isEqualTo("http://localhost:80/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20appname%3D%22joyn%22+%5C%5BApp");
	}
	
	@Test
	public void can_filter_by_multiple_appname() throws Exception {
		SplunkQueryBuilder query = new SplunkQueryBuilder(new BaseSearchURL("localhost", "80", false));
		String build = query.appName("joyn").or().appName("libon").build().toString();
		Assertions.assertThat(build).isEqualTo("http://localhost:80/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20appname%3D%22joyn%22+OR+appname%3D%22libon%22");
	}
	
	@Test
	public void can_filter_by_route_url() throws Exception {
		SplunkQueryBuilder query = new SplunkQueryBuilder(new BaseSearchURL("localhost", "80", false));
		String build = query.routeName("myroute.paas.fr").onlyRouteLog().build().toString();
		Assertions.assertThat(build).isEqualTo("http://localhost:80/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20myroute.paas.fr+%5C%5BRTR");
	}
	
	

}
