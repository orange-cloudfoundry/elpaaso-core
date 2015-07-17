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

import com.francetelecom.clara.cloud.environment.log.BaseSearchURL;


public class BaseSearchURLTest {
	
	@Test
	public void should_get_http_url() {
		BaseSearchURL baseSearchURL = new BaseSearchURL("127.0.0.1", "8080", false);
		Assertions.assertThat(baseSearchURL.getValue().toString()).isEqualTo("http://127.0.0.1:8080/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20");
	}
	
	@Test
	public void should_get_secured_http_url() {
		BaseSearchURL baseSearchURL = new BaseSearchURL("127.0.0.1", "8080", true);
		Assertions.assertThat(baseSearchURL.getValue().toString()).isEqualTo("https://127.0.0.1:8080/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20");
	}

}
