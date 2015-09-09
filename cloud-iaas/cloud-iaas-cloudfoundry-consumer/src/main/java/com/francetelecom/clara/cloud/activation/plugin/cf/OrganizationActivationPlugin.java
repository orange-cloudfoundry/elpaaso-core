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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.OrganizationActivationService;
import com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure.CfAdapter;
import com.francetelecom.clara.cloud.commons.NotFoundException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.Failure;
import com.francetelecom.clara.cloud.commons.tasks.Success;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.coremodel.ActivationContext;
import com.francetelecom.clara.cloud.model.ModelItemRepository;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.techmodel.cf.Organization;
import com.francetelecom.clara.cloud.techmodel.cf.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;


/**
 * Wraps the {@link CfAdapter} with TaskStatus expected
 */
@Service
public class OrganizationActivationPlugin extends ActivationPlugin<Organization> {

    private OrganizationActivationService organizationActivationService;

    private OrganizationRepository organizationRepository;

    private static final Logger logger = LoggerFactory.getLogger(OrganizationActivationPlugin.class);


    @Autowired
    public OrganizationActivationPlugin(OrganizationActivationService organizationActivationService, ModelItemRepository modelItemRepository,
                                        OrganizationRepository organizationRepository) {
        super();
        this.organizationActivationService = organizationActivationService;
        this.organizationRepository = organizationRepository;
        this.modelItemRepository = modelItemRepository;
    }

    @Override
    public boolean accept(Class<?> entityClass, ActivationStepEnum step) {
        return (Organization.class.equals(entityClass)) && (ActivationStepEnum.ACTIVATE.equals(step));
    }

    @Override
    public TaskStatus activate(int entityId, Class<Organization> entityClass, ActivationContext context) throws NotFoundException {
        final Organization organization;
        try {
            organization = organizationRepository.findOne(entityId);
            logger.debug("Organization count : + " + organizationRepository.count());
            Assert.notNull(organization, "Organization[" + entityId + "] does not exist.");
            String orgName = organizationActivationService.getCurrentOrganizationName();
            organization.activate(orgName);
            logger.debug("Activating organization : {}", organization);
            organizationRepository.save(organization);
        } catch (Exception e) {
            String message = "Unable to activate Organization. " + e.getMessage();
            logger.error(message, e);
            return new Failure(message);
        }
        return new Success("Organization <" + organization.getOrganizationName() + "> has been activated.");
    }

    @Override
    public TaskStatus giveCurrentTaskStatus(TaskStatus taskStatus) {
        throw new TechnicalException("should not be called. Task should already have been completed");
    }

}
