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
package com.francetelecom.clara.cloud.commons.xstream.logback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConfigurationDto {

	List<AppenderDto> appenders;
	List<LoggerDto> loggers;
	RootDto root;

	String debug = "true";
	String scan = "true"; 
	String scanPeriod = "30 seconds";
	
	public ConfigurationDto() {
		appenders = new ArrayList<AppenderDto>();
		loggers = new ArrayList<LoggerDto>();
	}

	public List<AppenderDto> listAppenders() {
		return Collections.unmodifiableList(appenders);
	}
	public List<LoggerDto> listLoggers() {
		return Collections.unmodifiableList(loggers);
	}

	public String getDebug() {
		return debug;
	}

	public void setDebug(String debug) {
		this.debug = debug;
	}

	public String getScan() {
		return scan;
	}

	public void setScan(String scan) {
		this.scan = scan;
	}

	public String getScanPeriod() {
		return scanPeriod;
	}

	public void setScanPeriod(String scanPeriod) {
		this.scanPeriod = scanPeriod;
	}
}
