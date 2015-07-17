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

public class LoginPage extends BasePage {

	@FindBy(id = "username")
	private WebElement nomInput;

	@FindBy(id = "password")
	private WebElement passwordInput;

	@FindBy(id = "buttonValidForm")
	private WebElement submitButton;

	Logger logger = LoggerFactory.getLogger(LoginPage.class);

	public LoginPage(WebDriver wd) {
		super(wd);
		logger.debug("current url is : " + wd.getCurrentUrl());
		logger.debug("page title : " + wd.getTitle());
		// assert that we are on the right page
		if (!"Orange PaaS login".equals(wd.getTitle())) {
			throw new IllegalStateException("this is not the login page, title page = "+wd.getTitle());
		}
		logger.debug("you are in login page");
	}

	public HomePage loginAs(String user, String password) {
		// enter login
		nomInput.sendKeys(user);
		// enter password
		passwordInput.sendKeys(password);
		// click login button
		submitButton.click();
		logger.debug("login button has been clicked");
		return PageFactory.initElements(getWd(), HomePage.class);
	}

	public HomePage loginAsDefaultUser() {
		return loginAs("testuser", "ce6utEtH");
	}

	public boolean isAuthenticationError() {
		throw new UnsupportedOperationException("not implemented yet");
		// return
		// wd.findElement(By.tagName("html")).getText().contains("Authentication failed. Please try again");
	}

}
