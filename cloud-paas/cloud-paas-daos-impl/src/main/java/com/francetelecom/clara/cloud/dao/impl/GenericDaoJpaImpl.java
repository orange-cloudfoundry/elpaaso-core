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
package com.francetelecom.clara.cloud.dao.impl;

import com.francetelecom.clara.cloud.dao.GenericDaoJpa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.lang.reflect.ParameterizedType;
import java.util.List;


/**
 * Generic DAO JPA implementation. An EntityManager is injected by Spring
 * container. A transaction should be started by container before calling these
 * DAO services.
 * 
 * @author Clara
 * @param <T,PK>
 *            Table and primary key
 */
@Repository
public abstract class GenericDaoJpaImpl<T, PK> implements GenericDaoJpa<T, PK> {

    private static final Logger LOG = LoggerFactory.getLogger(GenericDaoJpaImpl.class);

	protected Class<T> type = null;

	@PersistenceContext
	protected EntityManager em;

	/**
	 * Empty constructor. The real type T is found with generic reflection
	 */
	protected GenericDaoJpaImpl() {
		this.type = this.getParameterizedType(this.getClass());
	}

	/**
	 * Generic reflection. Find and set generic type used
	 */
	@SuppressWarnings("unchecked")
	private Class<T> getParameterizedType(Class clazz) {
		Class<T> specificType = null;
		ParameterizedType parameterizedType = (ParameterizedType) clazz
				.getGenericSuperclass();
		specificType = (Class<T>) parameterizedType.getActualTypeArguments()[0];
		return specificType;
	}

	public void flush() {
		em.flush();
	}

	public void persist(T o) {
		em.persist(o);
	}
	
	public T merge(T o) {
		return em.merge(o);
	}

	public void remove(T o) {
		em.remove(o);
	}

	public void refresh(T o) {
		em.refresh(o);
	}

	public T find(PK primaryKey) {
		return (T) em.find(type, primaryKey);
	}

	public T getReference(PK primaryKey) {
		return (T) em.getReference(type, primaryKey);
	}

	@SuppressWarnings("unchecked")
	public List<T> findAll() {
		return (List<T>) em.createQuery(
				"SELECT x FROM " + type.getSimpleName() + " x").getResultList();
	}

	public void setEm(EntityManager em) {
		this.em = em;
	}

}
