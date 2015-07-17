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

import org.openqa.selenium.htmlunit.HtmlUnitDriver;

public class BaseTestConfig {

	private HtmlUnitDriver webDriver;

	private URL webappLocation;

	private URL populatePageLocation;

	public URL getPopulatePageLocation() {
		return populatePageLocation;
	}

	public void setPopulatePageLocation(URL populatePageLocation) {
		this.populatePageLocation = populatePageLocation;
	}

	public void setWebDriver(HtmlUnitDriver webDriver) {
		this.webDriver = webDriver;
	}

	public void setWebappLocation(URL webappLocation) {
		this.webappLocation = webappLocation;
	}

	public HtmlUnitDriver getWebDriver() {
		return webDriver;
	}

	public URL getWebappLocation() {
		return webappLocation;
	}

}
