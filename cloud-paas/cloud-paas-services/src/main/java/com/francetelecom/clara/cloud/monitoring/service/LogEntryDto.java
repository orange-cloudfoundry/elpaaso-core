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
package com.francetelecom.clara.cloud.monitoring.service;

import java.io.Serializable;
import java.util.Date;

public class LogEntryDto implements Serializable {

	private static final long serialVersionUID = 2721406314659672747L;

	public enum Level {UNKNOWN, DEBUG, INFO, WARN, ERROR}
	
	private Level level;
	
	private Date timestamp;
	
	private String message;
	
	private String threadName;

	private String loggerName;

	public LogEntryDto(Level level, Date timestamp, String message, String threadname, String loggerName) {
		super();
		this.level = level;
		this.timestamp = timestamp;
		this.message = message;
		this.threadName = threadname;
		this.loggerName = loggerName;
	}

	public Level getLevel() {
		return level;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getMessage() {
		return message;
	}

	public String getThreadName() {
		return threadName;
	}

	public String getLoggerName() {
		return loggerName;
	}
}
