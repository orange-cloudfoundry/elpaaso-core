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
package com.francetelecom.clara.cloud.commons;

import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;

import org.apache.commons.io.output.NullWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.commons.xstream.XStreamUtils;
import com.thoughtworks.xstream.XStream;

/**
 * Utility to add persistence to tests that don't have some. This is designed so be instanciated by Junit tests that already have a persistence context declared
 * (None is provided as part of cloud-commons module test jar)
 */
public class PersistenceTestUtil {

	private static Logger logger = LoggerFactory.getLogger(PersistenceTestUtil.class.getName());

	@PersistenceContext
	EntityManager em;

	/**
	 * Executes the given Runnable on the current thread within a new Transational context
	 */
	@Transactional
	public void executeWithinTransaction(Runnable runnable) {
		runnable.run();
	}

	/**
	 * Executes the given Calleable on the current thread within a new Transational context, and returns the computed result
	 */
	@Transactional
	public <T> T executeWithinTransaction(Callable<T> calleable) throws Exception {
		return calleable.call();
	}

	@Transactional
	public void persistObjects(Object... objects) {
		for (Object object : objects) {
			persistObject(object, true);
		}
		em.flush();
	}

	@Transactional
	public void persistObject(Object object) {
		persistObject(object, false);
	}

	@Transactional
	public Object mergeObject(Object o) {
		return em.merge(o);
	}

	private void persistObject(Object object, boolean skipFlush) {
		// //First force its JSR 303 model validation to have meaningful exceptions if invalid
		ValidatorUtil.validate(object);

		// Then persist with JPA.
		try {
			em.persist(object);
			if (!skipFlush) {
				em.flush();
			}
		} catch (ConstraintViolationException e) {
			for (ConstraintViolation<?> constraintViolation : e.getConstraintViolations()) {
				logger.error(constraintViolation.getRootBeanClass() + " " + constraintViolation.getPropertyPath() + " " + constraintViolation.getMessage()
						+ " " + constraintViolation.getRootBean() + ", invalid value: [" + constraintViolation.getInvalidValue() + "]");
			}

			throw e;
		}
	}

	/**
	 * Generic persistent eager loading of an object
	 */
	@Transactional
	public <T> T reloadEntity(Class<T> classType, Object objectId, boolean forceEagerFetching) {
		T reloadedEntity = em.find(classType, objectId);
        if (reloadedEntity != null && forceEagerFetching) {
			forceEagerFetching(reloadedEntity);
		}
		return reloadedEntity;
	}



	private static void forceEagerFetching(Object entity) {
		XStream xStream = XStreamUtils.instanciateXstreamForHibernate();
		if (logger.isDebugEnabled()) {
			String xmlDump = xStream.toXML(entity);
			logger.debug("Xml dump:" + xmlDump);
		} else {
			// dump to equivalent of /dev/null
			xStream.toXML(entity, NullWriter.NULL_WRITER);
		}
	}


}
