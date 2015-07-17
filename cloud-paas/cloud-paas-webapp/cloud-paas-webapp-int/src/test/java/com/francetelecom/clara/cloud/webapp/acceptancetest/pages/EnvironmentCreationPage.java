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
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class EnvironmentCreationPage extends BasePage {

	@FindBy(name = "release")
	// this is a combo box
	private WebElement applicationReleaseInput;

	@FindBy(name = "label")
	private WebElement labelInput;

	@FindBy(name = "type")
	// this is a combo box
	private WebElement typeInput;

	@FindBy(name = "addEnvButton")
	private WebElement submitButton;

	Logger logger = LoggerFactory.getLogger(EnvironmentCreationPage.class);

	public EnvironmentCreationPage(WebDriver wd) throws InterruptedException {
		super(wd);
		logger.debug("current url is : " + wd.getCurrentUrl());
		logger.debug("page title : " + wd.getTitle());

		// assert that we are on the right page                     create environment
		SeleniumUtils.waitForElement(wd, "addEnvButton", DEFAULT_AJAX_TIMEOUT);
		if (!wd.findElement(By.tagName("html")).getText().contains("create environment")) {
			logger.error("This should contains 'create environment'" + wd.findElement(By.tagName("html")).getText());
			throw new IllegalStateException("this is not the new environment page");
		}
		logger.debug("you are in create environment page");
	}

	public EnvironmentDetailsPage createEnvironment(String releaseName, String environmentLabel, String environmentType) {
		// select release name
		selectReleaseName(releaseName);

		// enter environment label
		labelInput.sendKeys(environmentLabel);

		// select environment type
		selectEnvironmentType(environmentType);

		// submit environment creation
		submitButton.click();
		logger.debug("create environment button has been clicked");
		return PageFactory.initElements(getWd(), EnvironmentDetailsPageSeleniumImpl.class);
	}

	private void selectReleaseName(String releaseName) {
		Select releaseNames = new Select(applicationReleaseInput);
		try {
			releaseNames.selectByVisibleText(releaseName);
		} catch (org.openqa.selenium.NoSuchElementException e) {
			throw new NoSuchElementException("Cannot select application release name " + releaseName
					+ ". application release names available for selection are : " + getOptions(releaseNames));
		}
	}

	private void selectEnvironmentType(String environmentType) {
		Select typeInputs = new Select(typeInput);
		try {
			typeInputs.selectByVisibleText(environmentType);
		} catch (org.openqa.selenium.NoSuchElementException e) {
			throw new NoSuchElementException("Cannot select environment type " + environmentType + ". environment types available for selection are : "
					+ getOptions(typeInputs));
		}
	}

	private List<String> getOptions(Select select) {
		List<String> options = new ArrayList<String>();
		for (WebElement element : select.getOptions()) {
			options.add(element.getText());

		}
		return options;
	}
}
