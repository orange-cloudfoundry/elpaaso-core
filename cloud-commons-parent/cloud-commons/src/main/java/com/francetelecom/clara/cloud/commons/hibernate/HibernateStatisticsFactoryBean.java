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
package com.francetelecom.clara.cloud.commons.hibernate;

import javax.persistence.EntityManagerFactory;

import org.hibernate.SessionFactory;
import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.hibernate.stat.Statistics;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Workaound to get hibernate statistic from 4.3
 * @see https://hibernate.atlassian.net/browse/HHH-6190
 * @author pierre
 *
 */
public class HibernateStatisticsFactoryBean implements FactoryBean<Statistics> {

	  @Autowired
	  private EntityManagerFactory entityManagerFactory;

	  @Override
	  public Statistics getObject() throws Exception {
	    SessionFactory sessionFactory = ((HibernateEntityManagerFactory) entityManagerFactory).getSessionFactory();
	    return sessionFactory.getStatistics();
	  }

	  @Override
	  public Class<?> getObjectType() {
	    return Statistics.class;
	  }

	  @Override
	  public boolean isSingleton() {
	    return true;
	  }
	}