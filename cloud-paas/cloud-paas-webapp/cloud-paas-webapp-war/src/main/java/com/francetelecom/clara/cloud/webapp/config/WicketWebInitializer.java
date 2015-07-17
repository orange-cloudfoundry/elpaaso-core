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

import com.francetelecom.clara.cloud.presentation.WicketApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.spring.SpringWebApplicationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.web.WebApplicationInitializer;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Created by WOOJ7232 on 20/05/2015.
 */
@Configuration
@Import(WicketContextApplication.class)
public class WicketWebInitializer implements WebApplicationInitializer {
	private static Logger LOGGER = LoggerFactory.getLogger(WicketWebInitializer.class);

	@Value("${wicket.configuration:deployment}")
	private String configuration="deployment";

	@Value("${tests.mode:false}")
	private boolean testMode=false;

	@Value("${mock.mode:false}")
	private boolean mockMode=false;


	@Autowired
	WicketApplication wicketApplication;

//	<filter>
//	<filter-name>wicket.filter</filter-name>
//	<filter-class>
//	org.apache.wicket.protocol.http.WicketFilter
//			</filter-class>
//	<init-param>
//	<param-name>applicationFactoryClassName</param-name>
//	<param-value>org.apache.wicket.spring.SpringWebApplicationFactory</param-value>
//	</init-param>
//	<!-- See maven profile : development or deployment -->
//	<init-param>
//	<param-name>configuration</param-name>
//	<param-value>${wicket.configuration}</param-value>
//	</init-param>
//
//	<init-param>
//	<param-name>testsMode</param-name>
//	<param-value>${tests.mode}</param-value>
//	</init-param>
//
//	<init-param>
//	<param-name>mockMode</param-name>
//	<!--<param-value>true</param-value>-->
//	<param-value>${mock.mode}</param-value>
//	</init-param>
//	</filter>
//	<!-- No "/*" because we use relative URLs for resources files (.css, .jpg, ...) inside HTML -->
//	<filter-mapping>
//	<filter-name>wicket.filter</filter-name>
//	<url-pattern>/portal/*</url-pattern>
//    </filter-mapping>


	@Bean
	public FilterRegistrationBean wicketFilterRegistration() {

		FilterRegistrationBean registration = new FilterRegistrationBean();
		WicketFilter wicketFilter = new WicketFilter();
		registration.setFilter(wicketFilter);
		registration.setName("wicketFilter");
		registration.addInitParameter(WicketFilter.APP_FACT_PARAM,
				SpringWebApplicationFactory.class.getName());
		registration.addInitParameter("configuration", configuration);
		registration.addInitParameter("testsMode", String.valueOf(testMode));
		registration.addInitParameter("mockMode",String.valueOf(mockMode));
		registration.addInitParameter(WicketFilter.FILTER_MAPPING_PARAM, "/portal/*");
		registration.addInitParameter(WicketFilter.IGNORE_PATHS_PARAM,"/favicon.ico");
		registration.addUrlPatterns("/portal/*");
		registration.setDispatcherTypes(DispatcherType.REQUEST,DispatcherType.FORWARD);
		registration.setMatchAfter(true);


		return registration;
	}

	@Override
	public void onStartup(ServletContext sc) throws ServletException {
		LOGGER.info("Initializing Wicket");

	}
}
