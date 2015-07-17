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

import static java.text.MessageFormat.format;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.TechnicalException;


public class SplunkQueryBuilder {

	private static final String SPACE = " ";
	private static final String SOURCE_PARAMETER = "source";
	private static final String INDEX_PARAMETER = "index";
	private static final String APPNAME_PARAMETER = "appname";
	private static final String OR_OPERAND = "OR";
	private static final String APP_FILTER = "\\[App";
	private static final String RTR_FILTER = "\\[RTR";

	private static final Logger LOGGER = LoggerFactory.getLogger(SplunkQueryBuilder.class.getName());

	private StringBuilder builder;
	private BaseSearchURL baseSearchURL;
	

	public SplunkQueryBuilder(BaseSearchURL baseSearchURL) {
		this.builder = new StringBuilder();
		this.baseSearchURL = baseSearchURL;
	}

	public SplunkQueryBuilder source(String value) {
		builder.append(SOURCE_PARAMETER).append("=").append("\"").append(value).append("\"").append(SPACE);
		return this;
	}

	public SplunkQueryBuilder index(String value) {
		builder.append(INDEX_PARAMETER).append("=").append("\"").append(value).append("\"").append(SPACE);
		return this;
	}

	public SplunkQueryBuilder appName(String value) {
		builder.append(APPNAME_PARAMETER).append("=").append("\"").append(value).append("\"").append(SPACE);
		return this;
	}

	public SplunkQueryBuilder routeName(String value) {
		builder.append(value).append(SPACE);
		return this;
	}

	public SplunkQueryBuilder or() {
		builder.append(OR_OPERAND).append(SPACE);
		return this;
	}

	public SplunkQueryBuilder onlyAppLog() {
		builder.append(APP_FILTER).append(SPACE);
		return this;
	}
	
	public SplunkQueryBuilder onlyRouteLog() {
		builder.append(RTR_FILTER).append(SPACE);
		return this;
	}
	
	public URL build() {
		try {
			return new URL(baseSearchURL.getValue().toString() + URLEncoder.encode(format(builder.toString().trim()), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Unsupported encoding UTF-8 : " + e.getMessage(), e);
			throw new TechnicalException("Unsupported encoding UTF-8 : " + e.getMessage(), e);
		} catch (MalformedURLException e) {
			LOGGER.error("Bad URL: " + e.getMessage(), e);
			throw new TechnicalException("Bad URL: " + e.getMessage(), e);
		}
		
	}


}