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
package com.francetelecom.clara.cloud.mocks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.CoreItem;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;
import com.google.common.base.Predicate;

/**
 * Created by IntelliJ IDEA. User: lzxv3002 Date: 08/06/11 Time: 14:37 Generic
 * Mock for services
 */
public class CoreItemServiceMock<T extends CoreItem> {

	private final AtomicInteger sequence = new AtomicInteger();

	// protected List<T> entities = new ArrayList<T>();
	protected Map<Integer, T> entities = new HashMap<Integer, T>();

	protected List<T> findAll() {
		return new ArrayList<T>(entities.values());
	}

	protected List<T> find(Predicate<T> predicate) {
		List<T> entitiesList = new ArrayList<T>(entities.values());
		Iterator<T> entitiesIterator = entitiesList.iterator();
		while (entitiesIterator.hasNext()) {
			if (! predicate.apply(entitiesIterator.next())) {
				entitiesIterator.remove();
			};
		}
		return entitiesList;
	}

	protected List<T> find(int first, int count) {
		List<T> entitiesList = new ArrayList<T>(entities.values());
		return new ArrayList<T>(entitiesList.subList(first, count));
	}

	protected List<T> find(int first, int count, Predicate<T> predicate) {
		List<T> entitiesList = new ArrayList<T>(entities.values());
		Iterator<T> entitiesIterator = entitiesList.iterator();
		while (entitiesIterator.hasNext()) {
			if (! predicate.apply(entitiesIterator.next())) {
				entitiesIterator.remove();
			};
		}
		return new ArrayList<T>(entitiesList.subList(first, count));
	}

	protected void create(T entity) throws DuplicateApplicationException, DuplicateApplicationReleaseException {

		boolean exist = false;

		for (T singleEntity : entities.values()) {
			if (singleEntity.getUID().equals(entity.getUID())) {
				exist = true;
				break;
			}
		}

		if (!exist) {
			entity.setId(sequence.incrementAndGet());

			if (entity.getClass() == ApplicationRelease.class) {
				((ApplicationRelease) entity).getLogicalDeployment().setId(sequence.incrementAndGet());
			}

			entities.put(entity.getId(), entity);
		} else {
			if (entity.getClass().equals(Application.class)) {
				throw new DuplicateApplicationException();
			} else {
				throw new DuplicateApplicationReleaseException();
			}
		}

	}

	protected void delete(String name) {
		List<T> items = new ArrayList<>(entities.values());
		for (T item : items) {
			if (name.equals(item.getUID())) {
				entities.remove(item.getId());
			}
		}
	}

	protected T findByUID(String uid) throws ObjectNotFoundException {
		T foundEntity = null;

		for (T entity : entities.values()) {
			if (entity.getUID().equals(uid))
				foundEntity = entity;
		}

		if (foundEntity == null)
			throw new ObjectNotFoundException();

		return foundEntity;
	}

	protected T update(T item) throws ObjectNotFoundException {

		if (entities.get(item.getId()) == null) {
			throw new ObjectNotFoundException();
		}

		entities.put(item.getId(), item);
		return item;
	}

	protected long count() {
		return entities.size();
	}

	protected long count(Predicate<T> predicate) {
		long count = 0;
		for (T entity : entities.values()) {
			if (predicate.apply(entity)) {
				count ++;
			}
		}
		return count;
	}

}
