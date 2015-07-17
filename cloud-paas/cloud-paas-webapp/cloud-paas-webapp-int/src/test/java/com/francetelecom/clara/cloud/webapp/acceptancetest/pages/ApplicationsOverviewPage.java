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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationsOverviewPage extends BasePage {

	@FindBy(id = "newAppLink")
	private WebElement newApplicationButton;

	Logger logger = LoggerFactory.getLogger(ApplicationsOverviewPage.class);

	public ApplicationsOverviewPage(WebDriver wd) {
		super(wd);
		logger.debug("current url is : " + wd.getCurrentUrl());
		logger.debug("page title : " + wd.getTitle());
		// assert that we are on the right page
		if (!"orange paas - applications overview".equals(wd.getTitle())) {
			throw new IllegalStateException("this is not the aplications page, title page = "+wd.getTitle());
		}
		logger.debug("you are in applications overview page");
	}

	public ApplicationCreationPage displayApplicationCreationPage() {
		// click on "new application" button
		newApplicationButton.click();
		logger.debug("new application button has been clicked");
		return PageFactory.initElements(getWd(), ApplicationCreationPage.class);
	}

}
