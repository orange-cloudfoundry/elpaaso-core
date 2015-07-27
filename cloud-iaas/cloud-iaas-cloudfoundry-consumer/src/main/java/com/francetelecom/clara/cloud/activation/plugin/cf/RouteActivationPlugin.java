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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.RouteActivationService;
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
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.RouteRepository;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

/**
 * Wraps the {@link CfAdapter} with TaskStatus expected
 */
@Service
public class RouteActivationPlugin extends ActivationPlugin<Route> {

    private RouteActivationService routeActivationService;

    private RouteRepository routeRepository;

    private static final Logger logger = LoggerFactory.getLogger(RouteActivationPlugin.class);


    @Autowired
    public RouteActivationPlugin(RouteActivationService routeActivationService, ModelItemRepository modelItemRepository,
                                 RouteRepository routeRepository) {
        super();
        this.routeActivationService = routeActivationService;
        this.routeRepository = routeRepository;
        this.modelItemRepository = modelItemRepository;
    }

    @Override
    public boolean accept(Class<?> entityClass, ActivationStepEnum step) {
        return entityClass.equals(Route.class) && (ActivationStepEnum.ACTIVATE.equals(step) || ActivationStepEnum.DELETE.equals(step));
    }

    @Override
    public TaskStatus activate(int entityId, Class<Route> entityClass, ActivationContext context) throws NotFoundException {
        final Route route;
        try {
            route = routeRepository.findOne(entityId);
            Assert.notNull(route, "Route[" + entityId + "] does not exist.");
            //prefix route uri with environment label to avoid collision between environments
            route.prefix(context.getEnvLabel());
            logger.debug("Activating Route : {}", route);
            RouteUri routeUri = routeActivationService.activate(route);
            route.activate(routeUri);
            routeRepository.save(route);
        } catch (Exception e) {
            String message = "Unable to activate Route. " + e.getMessage();
            logger.error(message, e);
            return new Failure(message);
        }
        return new Success("Route <" + route.getUri() + "> has been activated.");
    }

    @Override
    public TaskStatus delete(int entityId, Class<Route> entityClass) throws NotFoundException {
        final Route route;
        try {
            route = routeRepository.findOne(entityId);
            Assert.notNull(route, "Route[" + entityId + "] does not exist.");
            logger.debug("Deleting Route : {}", route);
            if (route.isActivated()) {
                routeActivationService.delete(route);
                route.delete();
                routeRepository.save(route);
            } else {
                logger.warn("Will not delete Route<" + route + ">. Route has not been activated yet.");
            }
        } catch (Exception e) {
            String message = "Unable to delete Route. " + e.getMessage();
            logger.error(message, e);
            return new Failure(message);
        }
        return new Success("Route <" + route.getUri() + "> has been deleted.");
    }

    @Override
    public TaskStatus giveCurrentTaskStatus(TaskStatus taskStatus) {
        throw new TechnicalException("should not be called. Task should already have been completed");
    }

}
