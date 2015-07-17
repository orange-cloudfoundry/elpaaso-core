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

import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstanceRepository;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.technicalservice.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business implementation for TechnicalDeploymentInstance management
 * 
 * All methods are defined as transactional. If no transaction is in progress
 * during method call, then it will start a new transaction.
 */
public class ManageTechnicalDeploymentInstanceImpl extends ManageModelItemImpl implements ManageTechnicalDeploymentInstance {

	private static final Logger log = LoggerFactory.getLogger(ManageTechnicalDeploymentInstanceImpl.class);

	@Autowired(required = true)
	private TechnicalDeploymentInstanceRepository technicalDeploymentInstanceRepository;

	public void setTechnicalDeploymentInstanceRepository(TechnicalDeploymentInstanceRepository technicalDeploymentInstanceRepository) {
		this.technicalDeploymentInstanceRepository = technicalDeploymentInstanceRepository;
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public TechnicalDeploymentInstance createTechnicalDeploymentInstance(TechnicalDeploymentTemplate tdt, TechnicalDeployment td) {
		log.debug("/******* persisting tdi - [" + td.getName() + "] **********/");
		TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt,td);
		technicalDeploymentInstanceRepository.save(tdi);
		return tdi;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public TechnicalDeploymentInstance findTechnicalDeploymentInstance(int tdiId) throws NotFoundException {
		log.debug("/******* looking up tdi - ID[" + tdiId + "] **********/");
		TechnicalDeploymentInstance entity = technicalDeploymentInstanceRepository.findOne(tdiId);
		if (entity == null) {
			String message = "tdi[" + tdiId + "] does not exist";
			log.error(message);
			throw new NotFoundException(message);
		}
		return entity;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public void updateTechnicalDeploymentInstance(TechnicalDeploymentInstance tdi) throws NotFoundException {
        log.debug("/******* updating tdi - ID[" + tdi.getId() + "] **********/");
		TechnicalDeploymentInstance persisted = technicalDeploymentInstanceRepository.findOne(tdi.getId());
		if (persisted == null) {
			String message = "TechnicalDeploymentInstance[" + tdi.getId() + "] does not exist";
			log.error(message);
			throw new NotFoundException(message);
		}
		// FIXME -> voir si l on peut utiliser une autre solution que le merge
		technicalDeploymentInstanceRepository.save(tdi);
	}
}
