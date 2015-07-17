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
package com.francetelecom.clara.cloud.ws.service;

import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.transport.http.AbstractHTTPDestination;
import org.slf4j.MDC;


public class LogInInterceptor extends AbstractSoapInterceptor {

	public static final String LOG_KEY_REMOTE_ADDR = "user_ip";
	
	public static final String LOG_KEY_SESSION_ID = "session"; 
	
	public LogInInterceptor() {
		super(Phase.USER_PROTOCOL);
	}

	@Override
	public void handleMessage(SoapMessage msg) throws Fault {
		HttpServletRequest request = (HttpServletRequest) msg.get(AbstractHTTPDestination.HTTP_REQUEST);
		if (null != request) {
			String addr = request.getRemoteAddr();
			MDC.put(LOG_KEY_REMOTE_ADDR, addr);
			if (request.getSession() != null) {
				String sessionId = request.getSession().getId();
				MDC.put(LOG_KEY_SESSION_ID, sessionId);
			}
		}
	}

}
