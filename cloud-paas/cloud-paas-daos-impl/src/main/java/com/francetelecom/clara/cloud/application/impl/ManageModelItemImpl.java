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
package com.francetelecom.clara.cloud.application.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.dao.ModelItemDaoJpa;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.ModelItem;
import com.francetelecom.clara.cloud.technicalservice.exception.NotFoundException;

/**
 * Business implementation for ModelItem management
 * 
 * All methods are defined as transactional. If no transaction is in progress
 * during method call, then it will start a new transaction.
 */
public class ManageModelItemImpl implements ManageModelItem {

	private static final Logger log = LoggerFactory.getLogger(ManageModelItemImpl.class);

	@Autowired(required = true)
	private ModelItemDaoJpa modelItemDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public void setDeploymentState(int modelItemId, Class<? extends ModelItem> clazz,DeploymentStateEnum newState) throws NotFoundException {
		ModelItem persisted = modelItemDao.find(modelItemId,clazz);
		if (persisted == null) {
			String message = "TechnicalDeploymentInstance[" + modelItemId + "] does not exist";
			log.error("TechnicalDeploymentInstance[" + modelItemId + "] does not exist");
			throw new NotFoundException(message);
		}
		persisted.setDeploymentState(newState);
	}

	@Override
	public ModelItem findModelItem(int modelItemId,
			Class<? extends ModelItem> clazz) throws NotFoundException {
		log.debug("/******* recherche du modelItem - ID[" + modelItemId + "] **********/");
		ModelItem entity = modelItemDao.find(modelItemId,clazz);
		if (entity == null) {
			String message = "modelItem[" + modelItemId + "] does not exist";
			log.error(message);
			throw new NotFoundException(message);
		}
		return entity;
	}
}
