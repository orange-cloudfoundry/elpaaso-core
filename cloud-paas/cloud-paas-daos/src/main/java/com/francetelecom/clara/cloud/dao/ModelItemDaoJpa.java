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
package com.francetelecom.clara.cloud.dao;

import org.springframework.dao.DataAccessException;

import com.francetelecom.clara.cloud.model.ModelItem;

public interface ModelItemDaoJpa extends GenericDaoJpa<ModelItem, Integer> {
	
	/**
	 * load a persistent object with specified id, return null if it does not
	 * exist
	 * 
	 * @param primaryKey
	 *            the persistent object primaryKey
	 * @return T the persistent object found in database
	 * @throws DataAccessException
	 */
    public <T extends ModelItem> T find(Integer primaryKey, Class<T> clazz);

}