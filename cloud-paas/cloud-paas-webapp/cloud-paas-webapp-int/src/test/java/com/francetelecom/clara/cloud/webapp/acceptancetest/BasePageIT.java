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

import java.net.URL;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.openqa.selenium.support.PageFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.webapp.acceptancetest.pages.LoginPage;

@ContextConfiguration(classes = BasePageITContext.class)
@RunWith(SpringJUnit4ClassRunner.class)
public abstract class BasePageIT {

	protected LoginPage loginPage;

	@Autowired
	private BaseTestConfig config;

	@Before
	public void setup() {
		// handle async
		config.getWebDriver().manage().timeouts().implicitlyWait(60, TimeUnit.SECONDS);
		config.getWebDriver().manage().timeouts().setScriptTimeout(60, TimeUnit.SECONDS);
		// enable javascript
		config.getWebDriver().setJavascriptEnabled(true);
		// go to login page
		config.getWebDriver().get(config.getWebappLocation().toString());

		loginPage = PageFactory.initElements(config.getWebDriver(), LoginPage.class);
	}

	@After
	public void teardown() {
		config.getWebDriver().quit();
	}

	public URL populatePageURL() {
		return config.getPopulatePageLocation();
	}
}
