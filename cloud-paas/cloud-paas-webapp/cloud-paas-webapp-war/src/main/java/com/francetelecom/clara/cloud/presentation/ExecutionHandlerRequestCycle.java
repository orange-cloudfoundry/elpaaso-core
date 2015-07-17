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

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.wicket.Session;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.cycle.IRequestCycleListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.ResourceNotFoundException;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;
import com.francetelecom.clara.cloud.scalability.ManageStatistics;
import com.francetelecom.clara.cloud.scalability.helper.PaasStats;

/**
 * Exception handler to display messages Updated : $LastChangedDate$
 * 
 */
public class ExecutionHandlerRequestCycle implements IRequestCycleListener {
	/**
	 * logger
	 */
	private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ExecutionHandlerRequestCycle.class);

	private ManageStatistics manageStatistics;
	private long statsSnapshotId;

	private WebPageFactory webPageFactory;

	private static Integer lastRequestId = 0;
	private static final Integer MAX_VALUE = Integer.MAX_VALUE;
	/**
	 * Key in logback context for connected user SSOID when available
	 */
	public static final String LOG_KEY_USER = "user_ssoid";
	/**
	 * Key in logback context for connected user name when available
	 */
	public static final String LOG_KEY_USERNAME = "user_name";
	/**
	 * Key in logback context for connected wicket session ID when available
	 */
	public static final String LOG_KEY_SESSION = "session";

	private static final synchronized Integer nextRequestId() {
		logger.trace("lastRequestId = {}", lastRequestId);
		lastRequestId = (lastRequestId % MAX_VALUE) + 1;
		return lastRequestId;
	}

	public ExecutionHandlerRequestCycle(WebApplication application, WebPageFactory webPageFactory) {
		manageStatistics = ((WicketApplication) application).getManageStatistics();
		if (webPageFactory==null) throw new IllegalArgumentException("Invalid value <"+webPageFactory+"> for webPageFactory");
		this.webPageFactory = webPageFactory;
	}

	protected boolean isRequestAMainPage(RequestCycle cycle) {
		String requestPage = cycle.getRequest().getContextPath();
		return !requestPage.endsWith(".css") && !requestPage.endsWith(".js");
	}

	/**
	 * Init of user for wdm-core ActionContext and call parentMethod
	 */
	@Override
	public void onBeginRequest(RequestCycle cycle) {
		if (manageStatistics != null && manageStatistics.isStatEnable() && isRequestAMainPage(cycle)) {
			statsSnapshotId = manageStatistics.startSnapshot(cycle.getRequest().getUrl().getPath()); // request.getPath());
		}
		if (cycle.getRequest() != null) {
			try {
				WicketSession wicketSession = (WicketSession) Session.get();
				if (wicketSession.getPaasUser() != null) {
					MDC.put(LOG_KEY_USER, wicketSession.getPaasUser().getSsoId().getValue());
					MDC.put(LOG_KEY_USERNAME, wicketSession.getPaasUser().getFirstName());
					MDC.put(LOG_KEY_SESSION, wicketSession.getId());
				}
			} catch (Exception e) {
				// Ignore it
			}
			final int requestId = nextRequestId();
			logger.trace("Setting MDC-requestId param to {}", requestId);
			MDC.put("requestId", requestId + "");
			if (cycle.getRequest().getUrl() != null) {
				String urlPath = cycle.getRequest().getUrl().getPath();
				logger.trace("Setting MDC-page param to {}", urlPath);
				MDC.put("page", urlPath);
				String url = cycle.getRequest().getUrl().toString();
				logger.trace("Setting MDC-url param to {}", url);
				MDC.put("url", url);
			}
		}
	}

	@Override
	public void onEndRequest(RequestCycle cycle) {
		logger.trace("Removing MDC-requestId");
		MDC.remove("requestId");
		logger.trace("Removing MDC-page");
		MDC.remove("page");
		logger.trace("Removing MDC-page");
		MDC.remove("page");
		if (manageStatistics != null && manageStatistics.isStatEnable() && isRequestAMainPage(cycle)) {
			try {
				PaasStats stats = manageStatistics.endSnapShot(statsSnapshotId);
				WicketSession session = WicketSession.get();
				session.addStats(stats);
			} catch (ResourceNotFoundException rnfe) {
				logger.error("stats error (url:{}): {}", cycle.getRequest().getUrl(), rnfe.getMessage());
			} catch (BusinessException be) {
				logger.error("stats error : {}", be.getMessage(), be);
			}
		}
		MDC.remove(LOG_KEY_USER);
		MDC.remove(LOG_KEY_USERNAME);
		MDC.remove(LOG_KEY_SESSION);
	}

	@Override
	/*
	 * * This method is executed when an exception is caught by wicket
	 */
	public IRequestHandler onException(RequestCycle cycle, Exception e) {
		logger.error("RunTimeException {}", e);
		cleanMDC();
		//get the required anemic view to display exception
		ExceptionView view = new ExceptionViewFactory(webPageFactory).newView(e);
		//create a specific presenter to inject error details into the view
		ExceptionPresenter presenter = new ExceptionPresenter(view);
		presenter.onException(ExceptionUtils.getRootCause(e));
		//return the view with injected error details
		return new RenderPageRequestHandler(new PageProvider(view.asPage()));
	}

	private void cleanMDC() {
		logger.trace("Removing MDC-requestId");
		MDC.remove("requestId");
		logger.trace("Removing MDC-page");
		MDC.remove("page");
		logger.trace("Removing MDC-url");
		MDC.remove("url");
	}

	@Override
	public void onDetach(RequestCycle cycle) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	@Override
	public void onRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	@Override
	public void onRequestHandlerScheduled(RequestCycle cycle, IRequestHandler handler) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	@Override
	public void onExceptionRequestHandlerResolved(RequestCycle cycle, IRequestHandler handler, Exception exception) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	@Override
	public void onRequestHandlerExecuted(RequestCycle cycle, IRequestHandler handler) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

	@Override
	public void onUrlMapped(RequestCycle cycle, IRequestHandler handler, Url url) {
		// To change body of implemented methods use File | Settings | File
		// Templates.
	}

}
