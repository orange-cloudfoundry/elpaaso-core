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
package com.francetelecom.clara.cloud.webapp.acceptancetest;

import org.junit.Test;

public class LoggingPageIT extends BasePageIT {

	@Test
	public void shouldConnectSuccessfully() {
		// Given there is a user called testuser
		// When I log i as testuser
		loginPage.loginAsDefaultUser();
		// Then I should access home page
	}

	@Test(expected = RuntimeException.class)
	public void shouldFailToConnect() {
		// Given there is no user called toto
		// When I log i as toto
		loginPage.loginAs("toto", "toto");
		// Then I should not access home page
	}

}
