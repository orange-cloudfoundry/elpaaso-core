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
package com.francetelecom.clara.cloud.deployment.technical.service;

import com.francetelecom.clara.cloud.commons.NotFoundException;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplateRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Business implementation for TechnicalDeploymentTemplate management
 * 
 * All methods are defined as transactional. If no transaction is in progress
 * during method call, then it will start a new transaction.
 */
@Service
public class ManageTechnicalDeploymentTemplateImpl implements ManageTechnicalDeploymentTemplate {

	private static final Logger log = LoggerFactory.getLogger(ManageTechnicalDeploymentTemplateImpl.class);

	private TechnicalDeploymentTemplateRepository technicalDeploymentTemplateRepository;

	public ManageTechnicalDeploymentTemplateImpl() {
	}

	@Autowired(required = true)
	public ManageTechnicalDeploymentTemplateImpl(TechnicalDeploymentTemplateRepository technicalDeploymentTemplateDao) {
		super();
		this.technicalDeploymentTemplateRepository = technicalDeploymentTemplateDao;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public TechnicalDeploymentTemplate findTechnicalDeploymentTemplate(int tdtId) throws NotFoundException {
		log.debug("/******* looking up tdt - ID[" + tdtId + "] **********/");
		TechnicalDeploymentTemplate tdt = technicalDeploymentTemplateRepository.findOne(tdtId);
		if (tdt == null) {
			String message = "no technical deployment template found with id <" + tdtId + ">";
			log.error(message);
			throw new NotFoundException(message);
		}
		return tdt;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public void createTechnicalDeploymentTemplate(TechnicalDeploymentTemplate tdt) {
		log.debug("/******* persisting tdt - [" + tdt.getName() + "] **********/");
		Assert.notNull(tdt, "cannot save technical deployment template<" + tdt + ">. no technical deployment template has been provided");

		technicalDeploymentTemplateRepository.saveAndFlush(tdt);

	}

	@Override
	public TechnicalDeploymentTemplate findTechnicalDeploymentTemplate(DeploymentProfileEnum profile, String releaseId) throws NotFoundException {
		Assert.notNull(profile, "cannot find technical deployment template for release<" + releaseId + "> with profile <" + profile + ">. no profile has been provided");
		Assert.notNull(releaseId, "cannot find technical deployment template for release<" + releaseId + "> with profile <" + profile + ">. no release id has been provided");

		TechnicalDeploymentTemplate tdt = technicalDeploymentTemplateRepository.findByDeploymentProfileAndReleaseId(profile, releaseId);
		if (tdt == null) {
			String message = "no technical deployment template found for release<" + releaseId + "> with profile <" + profile + ">";
			log.error(message);
			throw new NotFoundException(message);
		}

		return tdt;
	}

}
