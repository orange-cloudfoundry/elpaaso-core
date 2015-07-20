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
package com.francetelecom.clara.cloud.activation.plugin.cf;

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.CFServiceActivationService;
import com.francetelecom.clara.cloud.activation.plugin.cf.domain.ServiceActivationStatus;
import com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure.CfAdapter;
import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.commons.tasks.Failure;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.coremodel.ActivationContext;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.techmodel.cf.ManagedServiceRepository;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.francetelecom.clara.cloud.commons.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Wraps the {@link CfAdapter} with TaskStatus expected
 */
@Service
public class ManagedServiceActivationPlugin extends ActivationPlugin<ManagedService> {

    private CFServiceActivationService cfServiceActivationService;

    private ManagedServiceRepository manageServiceRepository;

    private static final Logger logger = LoggerFactory.getLogger(ManagedServiceActivationPlugin.class);

    @Autowired
    public ManagedServiceActivationPlugin(CFServiceActivationService cfServiceActivationService, ManageModelItem manageModelItem,
                                          ManagedServiceRepository managedServiceRepository) {
        super();
        this.cfServiceActivationService = cfServiceActivationService;
        this.manageServiceRepository = managedServiceRepository;
        this.setManageModelItem(manageModelItem);
    }

    @Override
    public boolean accept(Class<?> entityClass, ActivationStepEnum step) {
        return ManagedService.class.isAssignableFrom(entityClass) && (ActivationStepEnum.ACTIVATE.equals(step) || ActivationStepEnum.DELETE.equals(step));
    }

    @Override
    public TaskStatus activate(int entityId, Class<ManagedService> entityClass, ActivationContext context) throws NotFoundException {
        final ManagedService service;
        final ServiceActivationStatus status;
        try {
            service = manageServiceRepository.findOne(entityId);
            Assert.notNull(service, "Managed Service[" + entityId + "] does not exist.");
            status = cfServiceActivationService.activate(service);
            service.activate();
            manageServiceRepository.save(service);
        } catch (Exception e) {
            String message = "Unable to activate Managed Service. " + e.getMessage();
            logger.error(message, e);
            return new Failure(message);
        }
        return status;
    }

    @Override
    public TaskStatus delete(int entityId, Class<ManagedService> entityClass) throws NotFoundException {
        final ManagedService service;
        final ServiceActivationStatus status;
        try {
            service = manageServiceRepository.findOne(entityId);
            Assert.notNull(service, "Managed Service[" + entityId + "] does not exist.");
            status = cfServiceActivationService.delete(service);
            service.delete();
            manageServiceRepository.save(service);
        } catch (Exception e) {
            String message = "Unable to delete Managed Service. " + e.getMessage();
            logger.error(message);
            return new Failure(message);
        }
        return status;
    }

    @Override
    public TaskStatus firststart(final ManagedService managedService) {
        return start(managedService);
    }

    @Override
    public TaskStatus giveCurrentTaskStatus(TaskStatus taskStatus) {
        return cfServiceActivationService.getServiceActivationStatus((ServiceActivationStatus) taskStatus);
    }

}
