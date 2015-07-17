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

public class HomePage extends BasePage {

	@FindBy(name = "appsLink")
	private WebElement appsLink;

	@FindBy(name = "releasesLink")
	private WebElement releasesLink;

	@FindBy(name = "envsLink")
	private WebElement envsLink;

	@FindBy(id = "signin_link")
	private WebElement logoutLink;

	Logger logger = LoggerFactory.getLogger(HomePage.class);

	public HomePage(WebDriver wd) {
		super(wd);
		logger.debug("current url is : " + wd.getCurrentUrl());
		logger.debug("page title : " + wd.getTitle());
		// assert that we are on the right page
		if (!"orange paas - home page".equals(wd.getTitle())) {
			throw new IllegalStateException("this is not the home page, title page = " + wd.getTitle());
		}
		logger.debug("you are in home page");
	}

	public ApplicationsOverviewPage displayApplicationsOverviewPage() {
		// click on "applications" in menu
		appsLink.click();
		logger.debug("applications link has been clicked");
		return PageFactory.initElements(getWd(), ApplicationsOverviewPage.class);
	}

	public ReleasesOverviewPage displayReleasesOverviewPage() {
		// click on "releases" in menu
		releasesLink.click();
		logger.debug("releases link has been clicked");
		return PageFactory.initElements(getWd(), ReleasesOverviewPage.class);
	}

	public EnvironmentsOverviewPage displayEnvironmentsOverviewPage() {
		// click on "environments" in menu
		envsLink.click();
		logger.debug("environments link has been clicked");
		return PageFactory.initElements(getWd(), EnvironmentsOverviewPage.class);
	}

	public PopulatePage displayPopulatePage(String populatePageURL) {
		// select populate datas url
		getWd().get(populatePageURL);
		logger.debug("populate datas url has been entered");
		return PageFactory.initElements(getWd(), PopulatePage.class);
	}

	public LoginPage logout() {
		// click on "releases" in menu
		logoutLink.click();
		logger.debug("logout link has been clicked");
		getWd().switchTo().alert().accept();
		return new LoginPage(getWd());

	}
}
