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
package com.francetelecom.clara.cloud.webapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.*;

/**
 * Created by WOOJ7232 on 20/05/2015.
 */
@Configuration
public class UselessTogglzInitializer implements WebApplicationInitializer {
	private static Logger LOGGER = LoggerFactory.getLogger(UselessTogglzInitializer.class);

//	<filter>
//	<filter-name>TogglzFilter</filter-name>
//	<filter-class>org.togglz.servlet.TogglzFilter</filter-class>
//	</filter>
//	<filter-mapping>
//	<filter-name>TogglzFilter</filter-name>
//	<url-pattern>/*</url-pattern>
//	</filter-mapping>
//	<servlet>
//	<servlet-name>TogglzConsoleServlet</servlet-name>
//	<servlet-class>org.togglz.console.TogglzConsoleServlet</servlet-class>
//	</servlet>
//	<servlet-mapping>
//	<servlet-name>TogglzConsoleServlet</servlet-name>
//	<url-pattern>/togglz/*</url-pattern>
//	</servlet-mapping>

	@Override
	public void onStartup(ServletContext sc) throws ServletException {
		String message="automatic Togglz registration, thanks to Servlet3";
		LOGGER.info("Initializing Togglz :{}",message);
	}


}
