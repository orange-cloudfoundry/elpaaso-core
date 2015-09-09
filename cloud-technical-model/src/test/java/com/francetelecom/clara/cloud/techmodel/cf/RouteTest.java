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
package com.francetelecom.clara.cloud.techmodel.cf;

import org.fest.assertions.Assertions;
import org.junit.Test;

/**
 *
 */
public class RouteTest {

	@Test
	public void should_prefix_route_uri() throws Exception {
		Route route = new Route(new RouteUri("this-is-an-host.my.domain.fr"), "contextRoot", new Space());

		route.prefix("a_prefix");

		Assertions.assertThat(route.getUri()).startsWith("a_prefix-");
		Assertions.assertThat(route.getUri()).endsWith("this-is-an-host.my.domain.fr");
	}

	@Test
	public void http_full_access_url_should_end_with_context_root() throws Exception {
		Route route = new Route(new RouteUri("this-is-an-host.my.domain.fr"), "/contextRoot", new Space());

		Assertions.assertThat(route.getFullHttpAccessUrl().toExternalForm()).isEqualTo("http://this-is-an-host.my.domain.fr:80/contextRoot");
	}

	@Test
	public void http_full_access_url_should_not_end_with_slash_even_if_context_root_is_slash() throws Exception {
		Route route = new Route(new RouteUri("this-is-an-host.my.domain.fr"), "/", new Space());

		Assertions.assertThat(route.getFullHttpAccessUrl().toExternalForm()).isEqualTo("http://this-is-an-host.my.domain.fr:80");
	}

}
