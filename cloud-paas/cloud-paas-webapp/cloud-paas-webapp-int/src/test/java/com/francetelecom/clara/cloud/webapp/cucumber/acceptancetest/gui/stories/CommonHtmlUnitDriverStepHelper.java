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
package com.francetelecom.clara.cloud.webapp.cucumber.acceptancetest.gui.stories;

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.webapp.acceptancetest.pages.LoginPage;

public class CommonHtmlUnitDriverStepHelper {

	private final HtmlUnitDriver webDriver;
	private final URL webappURL;
	private URL populatePageURL;

	Logger logger = LoggerFactory.getLogger(CommonHtmlUnitDriverStepHelper.class);


	public CommonHtmlUnitDriverStepHelper(HtmlUnitDriver webDriver, URL webappURL, URL populatePageURL) {
		logger.debug("DriverSteps init webappURL: {}   -populatePageURL: {} ", webappURL, populatePageURL);
		this.webDriver = webDriver;
		this.webappURL = webappURL;
		this.populatePageURL = populatePageURL;
		initWebDriver();
	}

	protected void initWebDriver() {
		logger.debug("DriverSteps init WebDriver");
		// handle async
		this.webDriver.manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		this.webDriver.manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
		// enable javascript
		this.webDriver.setJavascriptEnabled(true);
	}

	public void afterStory() {
		webDriver.quit();
	}

	public HtmlUnitDriver getWebDriver() {
		return webDriver;
	}

	public URL getWebappURL() {
		return webappURL;
	}

	public URL getPopulatePageURL() {
		return populatePageURL;
	}

	public void setPopulatePageURL(URL populatePageURL) {
		this.populatePageURL = populatePageURL;
	}

	public LoginPage goToLoginPage() {
		// delete authentication info
		getWebDriver().manage().deleteAllCookies();
		// go to login page
		logger.debug("Trying to access login page at {}", getWebappURL());
		getWebDriver().get(getWebappURL().toString());
		logger.debug("you are in : " + getWebDriver().getTitle());
		return PageFactory.initElements(getWebDriver(), LoginPage.class);
	}

}