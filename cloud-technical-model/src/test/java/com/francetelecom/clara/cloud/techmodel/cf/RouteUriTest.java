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

public class RouteUriTest {

	@Test(expected = IllegalArgumentException.class)
	public void uri_should_not_be_null() {
		new RouteUri(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void uri_should_not_be_empty() {
		new RouteUri("");
	}

	@Test
	public void uri_is_made_up_of_an_host_and_a_domain() {
		RouteUri routeUri = new RouteUri("this-is-an-host.my.domain.fr");
		
		Assertions.assertThat(routeUri.getHost()).isEqualTo("this-is-an-host");
		Assertions.assertThat(routeUri.getDomain()).isEqualTo("my.domain.fr");
		Assertions.assertThat(routeUri.getValue()).isEqualTo("this-is-an-host.my.domain.fr");

	}

	@Test
	public void should_get_uri_with_host_prefix_to_avoid_host_collision() throws Exception {
		RouteUri routeUri = new RouteUri("this-is-an-host.my.domain.fr");
		
		Assertions.assertThat(routeUri.withHostPrefix("a_prefix").getHost()).startsWith("a_prefix-");
		Assertions.assertThat(routeUri.withRandomHostPrefix().getHost()).endsWith("this-is-an-host");
		//should not change domain
		Assertions.assertThat(routeUri.withRandomHostPrefix().getDomain()).isEqualTo("my.domain.fr");

	}
	
	@Test
	public void should_get_uri_with_random_host_prefix_to_avoid_host_collision() throws Exception {
		RouteUri routeUri = new RouteUri("this-is-an-host.my.domain.fr");
		
		Assertions.assertThat(routeUri.withRandomHostPrefix().getHost()).startsWith("c");
		Assertions.assertThat(routeUri.withRandomHostPrefix().getHost()).endsWith("this-is-an-host");
		//should not change domain
		Assertions.assertThat(routeUri.withRandomHostPrefix().getDomain()).isEqualTo("my.domain.fr");

	}
	
	@Test
	public void uris_with_same_host_and_same_domain_are_equals() {
		RouteUri routeUri = new RouteUri("this-is-an-host.my.domain.fr");
		RouteUri routeUri2 = new RouteUri("this-is-an-host.my.domain.fr");
		RouteUri routeUri3 = new RouteUri("this-is-another-host.my.domain.fr");
		RouteUri routeUri4 = new RouteUri("this-is-an-host.another.domain.fr");
		
		Assertions.assertThat(routeUri).isEqualTo(routeUri2);
		Assertions.assertThat(routeUri).isNotEqualTo(routeUri3);
		Assertions.assertThat(routeUri).isNotEqualTo(routeUri4);
		Assertions.assertThat(routeUri3).isNotEqualTo(routeUri4);

	}
	
}
