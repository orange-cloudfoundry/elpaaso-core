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
package com.francetelecom.clara.cloud.webapp.acceptancetest.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PopulatePage extends BasePage {

	@FindBy(name = "appPopulationCreationsubmit")
	private WebElement submitButton;

	Logger logger = LoggerFactory.getLogger(PopulatePage.class);

    public PopulatePage(WebDriver wd) {
		super(wd);
		logger.debug("current url is : " + wd.getCurrentUrl());
		logger.debug("page title : " + wd.getTitle());
		// assert that we are on the right page
		if (!"orange paas - populate data".equals(wd.getTitle())) {
			throw new IllegalStateException("this is not the populate data page, title page = " + wd.getTitle());
		}
		logger.debug("you are in populate data page");
	}

    public HomePage   createJeeProbeAppAndItsRelease(){
        return createAppAndItsReleaseFor("jeeProbeLogicalModelCatalog");
    }
        
    public HomePage   createSimpleProbeAppAndItsRelease(){
        return createAppAndItsReleaseFor("simpleProbeLogicalModelCatalog");
    }
    
    public HomePage createAppAndItsReleaseFor(String populateApplicationName) {
        logger.debug("Looking for app {}", populateApplicationName);
        WebElement checkbox = getWd().findElement(By.id(populateApplicationName));
        click(checkbox);
        return submitAndGetHomePage();
    }

    private void click(WebElement checkbox){
        checkbox.click();
        logger.debug("{} checkbox has been checked", checkbox.getAttribute("id"));
    }

    private HomePage submitAndGetHomePage() {
        // click on "populate datas" button
        submitButton.click();
        logger.debug("populate datas button has been clicked");
        return PageFactory.initElements(getWd(), HomePage.class);
    }
}
