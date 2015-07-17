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
package com.francetelecom.clara.cloud.presentation;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.wicket.WicketRuntimeException;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.AuthorizationException;
import com.francetelecom.clara.cloud.technicalservice.exception.InvalidApplicationException;
import com.francetelecom.clara.cloud.technicalservice.exception.InvalidReleaseException;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;

public class ExceptionViewFactory {

	private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ExceptionViewFactory.class);

	private final transient WebPageFactory webPageFactory;

	public ExceptionViewFactory(WebPageFactory webPageFactory) {
		super();
		if (webPageFactory == null)
			throw new IllegalArgumentException("cannot create ExceptionViewFactory. Invalid value <" + webPageFactory + "> for webPageFactory");
		this.webPageFactory = webPageFactory;
	}

	public ExceptionView newView(Exception e) {
		Throwable rootCause = (ExceptionUtils.getRootCause(e) != null ? ExceptionUtils.getRootCause(e) : e);
		if (rootCause instanceof AuthorizationException) {
			return webPageFactory.getAuthorizationExceptionPage();
		}
		if (rootCause instanceof InvalidApplicationException) {
			return webPageFactory.getInvalidApplicationExceptionPage();
		}
		if (rootCause instanceof InvalidReleaseException) {
			return webPageFactory.getInvalidReleaseExceptionPage();
		}
		if (rootCause instanceof ObjectNotFoundException) {
			return webPageFactory.getObjectNotFoundExceptionPage();
		}
		if (rootCause instanceof WicketRuntimeException && rootCause.getMessage() != null && rootCause.getMessage().contains("Pagemap null is still locked")) {
			ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
			logger.debug("dumping threads for page map issue");
			logger.debug(threadMXBean.dumpAllThreads(true, true).toString());
			return webPageFactory.getUnknownExceptionPage();
		}

		return webPageFactory.getUnknownExceptionPage();
	}

}
