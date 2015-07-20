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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.SpaceActivationService;
import com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure.CfAdapter;
import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.Failure;
import com.francetelecom.clara.cloud.commons.tasks.Success;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.coremodel.ActivationContext;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceRepository;
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
public class SpaceActivationPlugin extends ActivationPlugin<Space> {

    private SpaceActivationService spaceActivationService;

    private SpaceRepository spaceRepository;

    private static final Logger logger = LoggerFactory.getLogger(SpaceActivationPlugin.class);


    @Autowired
    public SpaceActivationPlugin(SpaceActivationService spaceActivationService, ManageModelItem manageModelItem,
                                 SpaceRepository spaceRepository) {
        super();
        this.spaceActivationService = spaceActivationService;
        this.spaceRepository = spaceRepository;
        this.setManageModelItem(manageModelItem);
    }

    @Override
    public boolean accept(Class<?> entityClass, ActivationStepEnum step) {
        return (Space.class.equals(entityClass)) && (ActivationStepEnum.ACTIVATE.equals(step) || ActivationStepEnum.DELETE.equals(step));
    }

    @Override
    public TaskStatus activate(int entityId, Class<Space> entityClass, ActivationContext context) throws NotFoundException {
        final Space space;
        try {
            space = spaceRepository.findOne(entityId);
            Assert.notNull(space, "Space[" + entityId + "] does not exist.");
            logger.debug("Activating space : {}", space);
            SpaceName spaceName = spaceActivationService.activate(context.getEnvLabel());
            space.activate(spaceName);
            spaceRepository.save(space);
        } catch (Exception e) {
            String message = "Unable to activate Space. " + e.getMessage();
            logger.error(message, e);
            return new Failure(message);
        }
        return new Success("Space <" + space.getSpaceName() + "> has been activated.");
    }

    @Override
    public TaskStatus giveCurrentTaskStatus(TaskStatus taskStatus) {
        throw new TechnicalException("should not be called. Task should already have been completed");
    }

    @Override
    public TaskStatus delete(int entityId, Class<Space> entityClass) throws NotFoundException {
        final Space space;
        try {
            space = spaceRepository.findOne(entityId);
            Assert.notNull(space, "Space[" + entityId + "] does not exist.");
            logger.debug("Deleting space : {}", space);
            if (space.isActivated()) {
                spaceActivationService.delete(space.getSpaceName());
                space.delete();
                spaceRepository.save(space);
            } else {
                logger.warn("Will not delete Space<" + space.getSpaceName() + ">. Space has not been activated yet.");
            }
        } catch (Exception e) {
            String message = "unable to delete Space. " + e.getMessage();
            logger.error(message);
            return new Failure(message);
        }
        return new Success("Space <" + space.getSpaceName() + "> has been deleted.");
    }

}
