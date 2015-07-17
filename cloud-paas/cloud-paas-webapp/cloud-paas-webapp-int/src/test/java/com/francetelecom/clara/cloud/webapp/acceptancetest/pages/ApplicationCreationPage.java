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

import com.francetelecom.clara.cloud.webapp.acceptancetest.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationCreationPage extends BasePage {

	@FindBy(name = "appLabel")
	private WebElement applicationNameInput;

	@FindBy(name = "appCode")
	private WebElement applicationCodeInput;

    // releaseFieldsetPanel::releaseVersion
	@FindBy(id = "releaseVersion")
	private WebElement releaseVersionInput;

	@FindBy(name = "addAppButton")
	private WebElement submitButton;

	Logger logger = LoggerFactory.getLogger(ApplicationCreationPage.class);

	public ApplicationCreationPage(WebDriver wd) {
		super(wd);
		logger.debug("current url is : " + wd.getCurrentUrl());
		logger.debug("page title : " + wd.getTitle());

		// assert that we are on the right page
		SeleniumUtils.waitForElement(wd, "addAppButton", DEFAULT_AJAX_TIMEOUT);
		if (!wd.findElement(By.tagName("html")).getText().contains("create application")) {
			throw new IllegalStateException("this is not the new application page");
		}
		logger.debug("you are in application creation page");
	}

	public void createApplication(String applicationName, String applicationCode, String releaseVersion) {
		// enter application name
		applicationNameInput.sendKeys(applicationName);
		// enter application code
		applicationCodeInput.sendKeys(applicationCode);
		// enter application version
		releaseVersionInput.sendKeys(releaseVersion);
		// submit application creation
		submitButton.click();
		logger.debug("create application button has been clicked");
	}
}
