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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReleasesOverviewPage extends BasePage {

	@FindBy(id = "newReleaseLink")
	private WebElement newReleaseButton;

	Logger logger = LoggerFactory.getLogger(ReleasesOverviewPage.class);

	public ReleasesOverviewPage(WebDriver wd) {
		super(wd);
		logger.debug("current url is : " + wd.getCurrentUrl());
		logger.debug("page title : " + wd.getTitle());
		// assert that we are on the right page
		if (!"orange paas - releases overview".equals(wd.getTitle())) {
			throw new IllegalStateException("this is not the releases overview page, title page = "+wd.getTitle());
		}
		logger.debug("you are in release overview page");
	}

}
