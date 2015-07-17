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

import com.francetelecom.clara.cloud.commons.TechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.amqp.RabbitProperties;
import org.springframework.cloud.Cloud;
import org.springframework.cloud.CloudFactory;
import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.service.ServiceInfo;
import org.springframework.cloud.service.common.AmqpServiceInfo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Created by WOOJ7232 on 26/05/2015.
 */
@Configuration
@Profile("cloud")
public class DatasourceCloudContext extends AbstractCloudConfig{
	private static Logger LOGGER = LoggerFactory.getLogger(DatasourceCloudContext.class);


	@Value("jdbc:postgresql://${cloud.services.postgres-db-paas.connection.host}:${cloud.services.postgres-db-paas.connection.port}/${cloud.services.postgres-db-paas.connection.path}")
	String paasDbUrl;
	@Value("${cloud.services.postgres-db-paas.connection.username}")
	String paasDbUsername;
	@Value("${cloud.services.postgres-db-paas.connection.password}")
	String paasDbPassword;

	@Value("jdbc:postgresql://${cloud.services.postgres-activiti-paas.connection.host}:${cloud.services.postgres-activiti-paas.connection.port}/${cloud.services.postgres-activiti-paas.connection.path}")
	 String activitiDbUrl;
	 @Value("${cloud.services.postgres-activiti-paas.connection.username}")
	  String activitiDbUsername;
	@Value("${cloud.services.postgres-activiti-paas.connection.password}")
	 String activitiDbPassword;

	@Bean(name = "datasource",destroyMethod = "close")
	@Primary
	public DataSource getCloudWebappDatasource() throws NamingException {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
		setCommonProperties(dataSource);
		dataSource.setUrl(paasDbUrl);
		dataSource.setUsername(paasDbUsername);
		dataSource.setPassword(paasDbPassword);

		return dataSource;
	}

	@Bean(name = "activitiDS",destroyMethod = "close")
	@Primary
	public DataSource getActivitiDatasource() throws NamingException {
		org.apache.tomcat.jdbc.pool.DataSource dataSource = new org.apache.tomcat.jdbc.pool.DataSource();
		setCommonProperties(dataSource);
		dataSource.setUrl(activitiDbUrl);
		dataSource.setUsername(activitiDbUsername);
		dataSource.setPassword(activitiDbPassword);

		return dataSource;
	}


	private void setCommonProperties(org.apache.tomcat.jdbc.pool.DataSource dataSource) {
		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setConnectionProperties("sessionVariables=sql_mode='ANSI';characterEncoding=UTF-8;tcpKeepAlive=true;socketTimeout=20");
		dataSource.setJmxEnabled(true);
		dataSource.setMaxActive(20);
		dataSource.setTestWhileIdle(false);
		dataSource.setTestOnBorrow(true);
		dataSource.setValidationQuery("SELECT 2;");
	}


	@Value("${paas.rabbitmq.cf.requestedheartbeat:0}")
	public int requestedHeartbeat;

	@Value("${paas.rabbitmq.cf.channelcachesize:1}")
	public int channelCacheSize;
	@Value("${paas.rabbitmq.cf.connectiontimeout:30000}")
	public int connectionTimeout;

	@Bean(name="rabbitMQConnectionFactory")
	public ConnectionFactory rabbitConnectionFactory() {
		CloudFactory cloudFactory = new CloudFactory();
		Cloud cloud = cloudFactory.getCloud();

		AmqpServiceInfo amqpServiceInfo = (AmqpServiceInfo) cloud.getServiceInfo("activationAmqpBroker");

		String serviceID = amqpServiceInfo.getId();
		ConnectionFactory connectionFactory = cloud.getServiceConnector(serviceID, ConnectionFactory.class, null);
		try{
			LOGGER.info("Setting CachingConnectionFactory specific properties");
			((CachingConnectionFactory)connectionFactory).setChannelCacheSize(channelCacheSize);
			((CachingConnectionFactory)connectionFactory).setRequestedHeartBeat(requestedHeartbeat);
			((CachingConnectionFactory)connectionFactory).setConnectionTimeout(connectionTimeout);
		}   catch (ClassCastException cce){
			throw new TechnicalException("Cannot customize CachingConnectionFactory for rabbitConnectionFactory");
		}
		return connectionFactory;
	}





//	<!-- https://jdbc.postgresql.org/documentation/93/connect.html -->
//	<beans profile="cloud">
//
//	<!--To avoid duplicate Datasource defnition in spring boot with auto-reconfig 		-->
//	<!--	<alias name="postgres-db-paas" alias="datasource"/>-->
//	<!--	<alias name="postgres-activiti-paas" alias="activitiDS"/>-->
//	<!-- end alias-->
//
//	<!-- http://tomcat.apache.org/tomcat-8.0-doc/jdbc-pool.html -->
//	<!--  cloud.services.postgres-db-paas.connection.path ??  -->
//
//	<bean id="datasource" class="org.apache.tomcat.jdbc.pool.DataSource"
//	destroy-method="close">
//	<property name="driverClassName" value="org.postgresql.Driver"></property>
//
//	<property name="url" value="jdbc:postgresql://${cloud.services.postgres-db-paas.connection.host}:${cloud.services.postgres-db-paas.connection.port}/${cloud.services.postgres-db-paas.connection.path}"/>
//	<property name="username" value="${cloud.services.postgres-db-paas.connection.username}"></property>
//	<property name="password" value="${cloud.services.postgres-db-paas.connection.password}"></property>
//
//	<property name="connectionProperties" value="sessionVariables=sql_mode='ANSI';characterEncoding=UTF-8;tcpKeepAlive=true;socketTimeout=20"></property>
//	<property name="jmxEnabled" value="true"></property>
//	<property name="maxActive" value="20"></property>
//	<property name="testWhileIdle" value="false"></property>
//	<property name="testOnBorrow" value="true"></property>
//	<property name="validationQuery" value="SELECT 2;"></property>
//	</bean>


//	<bean id="activitiDS" class="org.apache.tomcat.jdbc.pool.DataSource" destroy-method="close">
//	<property name="driverClassName" value="org.postgresql.Driver"></property>
//	<property name="url" value="jdbc:postgresql://${cloud.services.postgres-activiti-paas.connection.host}:${cloud.services.postgres-activiti-paas.connection.port}/${cloud.services.postgres-activiti-paas.connection.path}">
//
//	</property>
//	<property name="username" value="${cloud.services.postgres-activiti-paas.connection.username}"></property>
//	<property name="password" value="${cloud.services.postgres-activiti-paas.connection.password}"></property>
//
//	<property name="connectionProperties" value="sessionVariables=sql_mode='ANSI';characterEncoding=UTF-8;tcpKeepAlive=true;socketTimeout=20"></property>
//	<property name="jmxEnabled" value="true"></property>
//	<property name="maxActive" value="20"></property>
//	<property name="testWhileIdle" value="false"></property>
//	<property name="testOnBorrow" value="true"></property>
//	<property name="validationQuery" value="SELECT 2;"></property>
//	</bean>

}
