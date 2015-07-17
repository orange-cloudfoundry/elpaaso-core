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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.webapp.acceptancetest.utils.Utils;

public class EnvironmentDetailsPageSeleniumImpl extends BasePage implements EnvironmentDetailsPage {

	// fetch all web ui urls
	@FindBy(xpath = "//a[contains(@id,'webURL')]")
	private List<WebElement> webURLLinks;

	// fetch env status
	@FindBy(name = "env-status-label")
	private WebElement status;

	// fetch env status message
	@FindBy(name = "env-activation-error")
	private WebElement statusMessage;

	// fetch reload link
	@FindBy(name = "envReloadLink")
	private WebElement reloadLink;

	// fetch delete link
	@FindBy(name = "envDeleteLink")
	private WebElement deleteLink;

	Logger logger = LoggerFactory.getLogger(EnvironmentDetailsPageSeleniumImpl.class);

	public EnvironmentDetailsPageSeleniumImpl(WebDriver wd) {
		super(wd);
		assertWeAreInTheRequiredPage(wd);
	}

	@Override
	public boolean isRunning() {
		logger.debug("Environment status is: "+status.getText());
		return "RUNNING".equalsIgnoreCase(status.getText());
	}

	@Override
	public boolean isFailed() {
		logger.debug("Environment status is: "+status.getText());
		return "FAILED".equalsIgnoreCase(status.getText());
	}

	@Override
	public boolean isCreating() {
		logger.debug("Environment status is: "+status.getText());
		return "CREATING".equalsIgnoreCase(status.getText());
	}

	@Override
	public boolean isDeleting() {
		logger.debug("Environment status is: "+status.getText());
		return "REMOVING".equalsIgnoreCase(status.getText());
	}
	
	@Override
	public boolean isDeleted() {
		logger.debug("Environment status is: "+status.getText());
		return "REMOVED".equalsIgnoreCase(status.getText());
	}
	
	@Override
	public boolean isOperational() {
		return isRunning() && canReachWebUIs();
	}

	@Override
	public String statusMessage() {
		return statusMessage.getText();
	}

	@Override
	public void refresh() {
		reloadLink.click();
	}

	@Override
	public void delete() {
		deleteLink.click();
	}

	private void assertWeAreInTheRequiredPage(WebDriver wd) {
		try {
			wd.findElement(By.xpath("//title[contains(.,'orange paas - environment details')]"));
		} catch (NoSuchElementException e) {
			logger.error("you are not in environment details page because current page title does not contain [orange paas - environment details]");
			logger.error("current page title is : " + wd.getTitle());
			logger.error("current url is : " + wd.getCurrentUrl());
			logger.error("current page source is: " + wd.getPageSource());
			throw new IllegalStateException("this is not the environment detail page");
		}
		logger.debug("current url is : " + wd.getCurrentUrl());
		logger.debug("current page title is : " + wd.getTitle());
	}

	private boolean canReachWebUIs() {
		String url;
		for (WebElement link : webURLLinks) {
			url = link.getAttribute("href");
			if (!Utils.canReachURL(url)) {
				logger.debug("cannot reach web ui: " + url);
				return false;
			}
		}
		return true;
	}

}
