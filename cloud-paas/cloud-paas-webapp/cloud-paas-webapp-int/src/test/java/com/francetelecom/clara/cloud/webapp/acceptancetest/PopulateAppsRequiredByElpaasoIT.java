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

import com.francetelecom.clara.cloud.webapp.acceptancetest.pages.ReleasesOverviewPage;
import org.junit.Test;

import com.francetelecom.clara.cloud.webapp.acceptancetest.pages.PopulatePage;

public class PopulateAppsRequiredByElpaasoIT extends BasePageIT {


    @Test
    public void shouldCreateElpaasoServicesAppAndItsRelease(){
        // Given I am in 'populate data' page
        PopulatePage populatePage = goToPopulateDataPage();
        // When I want to create a Elpaaso Services application and its release
        populatePage.createAppAndItsReleaseFor("elpaasoServicesLogicalModelCatalog");
        // Then my data should be populated
    }

    @Test
    public void shouldCreateElpaasoAppAndItsRelease(){
        // Given I am in 'populate data' page
        PopulatePage populatePage = goToPopulateDataPage();
        // When I want to create a Elpaaso application and its release
        populatePage.createAppAndItsReleaseFor("elPaaSoTomcatLogicalModelCatalog");
        // Then my data should be populated
    }

    @Test
    public void shouldCreatePwmAppAndItsRelease(){
        // Given I am in 'populate data' page
        PopulatePage populatePage = goToPopulateDataPage();
        // When I want to create a PWM application and its release
        populatePage.createAppAndItsReleaseFor("pwmLogicalModelCatalog");
        // Then my data should be populated
    }

	@Test
	public void shouldCreateDbaasCoreAppAndItsRelease() {
		// Given I am in 'populate data' page
		PopulatePage populatePage = goToPopulateDataPage();
		// When I want to create a wicketoo application and its release
        populatePage.createAppAndItsReleaseFor("dbaasCoreLogicalModelCatalog");
		// Then my data should be populated
	}

    @Test
    public void shouldCreateDbaasGuiWarAppAntItsRelease() {
        // Given I am in 'populate data' page
        PopulatePage populatePage = goToPopulateDataPage();
        // When I want to create a cloudfoundry wicket cxf jpa application and its release
        populatePage.createAppAndItsReleaseFor("dbaasPortalLogicalModelCatalog");
        // Then my data should be populated
    }

    @Test
    public void shouldCreateConfigProbeJarAppAntItsRelease() {
        // Given I am in 'populate data' page
        PopulatePage populatePage = goToPopulateDataPage();
        // When I want to create a simpleConfigProbe application and its release
        populatePage.createAppAndItsReleaseFor("simpleConfigProbeLogicalModelCatalog");
        // Then my data should be populated
    }


    private PopulatePage goToPopulateDataPage() {
		return loginPage.loginAsDefaultUser().displayPopulatePage(populatePageURL().toString());
	}


    private ReleasesOverviewPage goToReleaseOverviewPage() {
        return loginPage.loginAsDefaultUser().displayReleasesOverviewPage();
    }

}
