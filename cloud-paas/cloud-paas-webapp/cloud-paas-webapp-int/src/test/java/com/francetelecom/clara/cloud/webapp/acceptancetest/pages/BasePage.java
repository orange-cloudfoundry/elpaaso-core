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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BasePage {

	Logger logger = LoggerFactory.getLogger(BasePage.class);

	public static final long DEFAULT_AJAX_TIMEOUT = 10000;
	
	private final WebDriver wd;

	public BasePage(WebDriver wd) {
		super();
		this.wd = wd;
//		logger.debug("Page source of '" + wd.getTitle() + "': " + wd.getPageSource());
	}

	protected WebDriver getWd() {
		return wd;
	}

}
