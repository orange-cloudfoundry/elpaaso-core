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

import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentCrud;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business implementation for TechnicalDeployment management
 * <p/>
 * All methods are defined as transactional. If no transaction is in progress
 * during method call, then it will start a new transaction.
 */
public class ManageTechnicalDeploymentCrudImpl implements ManageTechnicalDeploymentCrud {

    private static final Logger log = LoggerFactory.getLogger(ManageTechnicalDeploymentCrudImpl.class);

    @Autowired(required = true)
    private TechnicalDeploymentRepository technicalDeploymentRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = {"BusinessException"})
    public void createTechnicalDeployment(TechnicalDeployment td) {
        log.debug("/******* persisting td - [" + td.getName() + "] **********/");
        technicalDeploymentRepository.save(td);
    }

}
