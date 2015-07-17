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
package com.francetelecom.clara.cloud.environment.log;

import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.TechnicalException;

public class BaseSearchURL {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseSearchURL.class.getName());

	private static final String SECURED_HTTP_PREFIX = "https://";

	private static final String HTTP_PREFIX = "http://";
	
	private String splunkIp;

	private String splunkPort;

	private boolean secured;

	public BaseSearchURL(String splunkIp, String splunkPort, boolean secured) {
		super();
		this.splunkIp = splunkIp;
		this.splunkPort = splunkPort;
		this.secured = secured;
	}
	
	public URL getValue() {
		StringBuilder splunkServerUrl = null;
		if (secured) {
			splunkServerUrl = new StringBuilder(SECURED_HTTP_PREFIX);
		} else {
			splunkServerUrl = new StringBuilder(HTTP_PREFIX);
		}
		splunkServerUrl.append(splunkIp).append(":").append(splunkPort).append("/en-US/app/elpaaso/flashtimeline?auto_pause=true&q=search%20");
		try {
			return new URL(splunkServerUrl.toString());
		} catch (MalformedURLException e) {
			LOGGER.error("Bad URL: " + e.getMessage(), e);
			throw new TechnicalException("Bad URL: " + e.getMessage(), e);
		}
	}

}
