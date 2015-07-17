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

import com.francetelecom.clara.cloud.webapp.acceptancetest.pages.HomePage;

/**
 * FIXME: There is no logout test because it's not supported by htmlunit driver.<br>
 */
public class HomePageIT extends BasePageIT {

	@Test
	public void shouldDisplayApplicationsOverviewPage() {
		// Given I am in login page
		HomePage homePage = goToHomePage();
		// When I want to display 'applications overview' page
		homePage.displayApplicationsOverviewPage();
		// Then I should access 'application overview' page
	}

	@Test
	public void shouldDisplayReleasesOverviewPage() {
		// Given I am in login page
		HomePage homePage = goToHomePage();
		// When I want to display 'releases overview' page
		homePage.displayReleasesOverviewPage();
		// Then I should access 'releases overview' page
	}

	@Test
	public void shouldDisplayEnvironmentsOverviewPage() {
		// Given I am in login page
		HomePage homePage = goToHomePage();
		// When I want to display 'environment overview' page
		homePage.displayEnvironmentsOverviewPage();
		// Then I should access 'environment overview' page
	}

	@Test
	public void shouldDisplayPopulateDatasPage() {
		// Given I am in login page
		HomePage homePage = goToHomePage();
		// When I want to display 'populate' page
		homePage.displayPopulatePage(populatePageURL().toString());
		// Then I should access 'populate' page
	}

	private HomePage goToHomePage() {
		return loginPage.loginAsDefaultUser();
	}
}
