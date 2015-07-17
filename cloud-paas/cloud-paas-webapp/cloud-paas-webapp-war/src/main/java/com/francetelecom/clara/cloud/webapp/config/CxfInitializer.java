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

import org.apache.cxf.transport.servlet.CXFServlet;
import org.apache.wicket.protocol.http.WicketFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SessionCookieConfig;

/**
 * Created by WOOJ7232 on 20/05/2015.
 */
@Configuration
@ImportResource("classpath:META-INF/spring/cloud-ws-context.xml")
public class CxfInitializer  {
	private static Logger LOGGER = LoggerFactory.getLogger(CxfInitializer.class);

//	<servlet>
//	<servlet-name>CXFServlet</servlet-name>
//	<servlet-class>org.apache.cxf.transport.servlet.CXFServlet</servlet-class>
//	<load-on-startup>1</load-on-startup>
//	</servlet>
//
//	<servlet-mapping>
//	<servlet-name>CXFServlet</servlet-name>
//	<url-pattern>/api/soap/*</url-pattern>
//	</servlet-mapping>



	@Bean
	public ServletRegistrationBean cxfServlet() {
		LOGGER.info("Initializing CXF");

		ServletRegistrationBean servletDef = new ServletRegistrationBean(new CXFServlet(), "/api/soap/*");
		servletDef.setLoadOnStartup(1);
		return servletDef;
	}





}
