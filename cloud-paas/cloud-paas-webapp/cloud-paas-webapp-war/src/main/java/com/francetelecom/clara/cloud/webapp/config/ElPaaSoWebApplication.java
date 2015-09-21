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

import org.apache.wicket.protocol.http.WicketFilter;
import org.apache.wicket.spring.SpringWebApplicationFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.jms.JmsAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.security.SecurityAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.DelegatingFilterProxy;

import javax.annotation.PreDestroy;
import javax.servlet.*;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by wooj7232 on 09/01/2015.
 */
@SpringBootApplication(exclude={/*ManagementSecurityAutoConfiguration.class,SecurityAutoConfiguration.class,*/ /*RabbitAutoConfiguration.class,*/JmsAutoConfiguration.class,DataSourceTransactionManagerAutoConfiguration.class,DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class, FreeMarkerAutoConfiguration.class})
// No qualifying bean of type [javax.sql.DataSource] is defined: expected single matching bean but found 2: postgres-activiti-paas,postgres-db-paas
//      DataSourceTransactionManagerAutoConfiguration.class,DataSourceAutoConfiguration.class, HibernateJpaAutoConfiguration.class
// NoUniqueBeanDefinitionException: No qualifying bean of type [javax.jms.ConnectionFactory] is defined: expected single matching bean but found 2: jmsConnectionFactory,asyncTaskHandlingConnectionFactory
//      JmsAutoConfiguration
// Invocation of init method failed; nested exception is java.lang.IllegalArgumentException: Cannot find template location(s): [classpath:/templates/] (please add some templates, check your FreeMarker configuration, or set spring.freemarker.checkTemplateLocation=false)
//      FreeMarkerAutoConfiguration
@Component
@ImportResource({"/WEB-INF/spring-config/application-context.xml"})
public class ElPaaSoWebApplication extends SpringBootServletInitializer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElPaaSoWebApplication.class);

	@Value("${application.context.location}")
	String applicationContextFilename;

	public static void main(String[] args) {
		SpringApplication.run(ElPaaSoWebApplication.class);
	}

	@Autowired
	ApplicationContext applicationContext;

	/**
	 * spring boot war
	 */
	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(ElPaaSoWebApplication.class);
	}


//    <!-- Spring param -->
//    <context-param>
//    <param-name>contextConfigLocation</param-name>
//    <param-value>
//    ${application.context}
//    /WEB-INF/spring-config/security-app-context.xml
//            </param-value>
//    </context-param>
//
//
//    <listener>
//    <display-name>spring context loader</display-name>
//    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
//    </listener>
//
//    <!-- spring security filter -->
//    <filter>
//    <filter-name>springSecurityFilterChain</filter-name>
//    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
//    </filter>
//    <filter-mapping>
//    <filter-name>springSecurityFilterChain</filter-name>
//    <url-pattern>/*</url-pattern>
//	</filter-mapping>
//    <session-config>
//    <cookie-config>
//    <http-only>true</http-only>
//    </cookie-config>
//    </session-config>

	@Override
	public void onStartup(ServletContext context) throws ServletException {

		LOGGER.info("Initializing ElPaaSo");

		SessionCookieConfig cookieConfig = context.getSessionCookieConfig();
		cookieConfig.setHttpOnly(true);
		super.onStartup(context);

		dumpServletContext(context);
	}

	private void dumpServletContext(ServletContext context) {
		Map<String, ? extends ServletRegistration> servletRegistrations = context.getServletRegistrations();
		LOGGER.info("Dump Servlets:");
		for (Map.Entry<String, ? extends ServletRegistration> entry : servletRegistrations.entrySet()) {
			LOGGER.info("Servlet: {} - {}", entry.getKey(),entry.getValue().getClassName());
			Map<String, String> initParams = entry.getValue().getInitParameters();
			for (Map.Entry<String, String> aParam : initParams.entrySet()) {
				LOGGER.info("Servlet {} param {} - {}", entry.getKey(),aParam.getKey(),aParam.getValue());

			}
		}
		Map<String, ? extends FilterRegistration> filters = context.getFilterRegistrations();
		LOGGER.info("Dump filters:");
		for (Map.Entry<String, ? extends FilterRegistration> aFilterEntry : filters.entrySet()) {
			LOGGER.info("Filter: {} - {}", aFilterEntry.getKey(), aFilterEntry.getValue().getClassName(),aFilterEntry.getValue().getUrlPatternMappings().toArray());
			Map<String, String> initParams = aFilterEntry.getValue().getInitParameters();
			for (Map.Entry<String, String> aParam : initParams.entrySet()) {
				LOGGER.info("Filter {} param {} - {}", aFilterEntry.getKey(),aParam.getKey(),aParam.getValue());
			}
		}
	}

	@PreDestroy
	public void onShutdown(){
		LOGGER.info("Elpaaso is shutting down now!!!");
	}

}