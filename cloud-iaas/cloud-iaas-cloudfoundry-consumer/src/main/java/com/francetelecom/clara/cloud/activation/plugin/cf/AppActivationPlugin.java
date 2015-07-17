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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.AppActivationService;
import com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure.CfAdapter;
import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.tasks.Failure;
import com.francetelecom.clara.cloud.commons.tasks.Success;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.coremodel.ActivationContext;
import com.francetelecom.clara.cloud.mvn.consumer.MavenReferenceResolutionException;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.AppRepository;
import com.francetelecom.clara.cloud.technicalservice.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.UUID;

/**
 * Wraps the {@link CfAdapter} with TaskStatus expected
 */
@Service
public class AppActivationPlugin extends ActivationPlugin<App> {

    private MvnRepoDao mvnDao;

    private AppActivationService appActivationService;

    private AppRepository appRepository;

    private static final Logger logger = LoggerFactory.getLogger(AppActivationPlugin.class);


    @Autowired
    public AppActivationPlugin(AppActivationService appActivationService, ManageModelItem manageModelItem,
                               MvnRepoDao mvnDao, AppRepository appRepository) {
        super();
        this.mvnDao = mvnDao;
        this.appActivationService = appActivationService;
        this.appRepository = appRepository;
        this.setManageModelItem(manageModelItem);
    }

    @Override
    public boolean accept(Class<?> entityClass, ActivationStepEnum step) {
        return entityClass.equals(App.class);
    }

    @Override
    public TaskStatus activate(int entityId, Class<App> entityClass, ActivationContext context) throws NotFoundException {
        final App app;
        try {
            app = appRepository.findOne(entityId);
            Assert.notNull(app, "App[" + entityId + "] does not exist.");
            logger.debug("Activating App : {}", app);
            resolveAppBinaries(app);
            final UUID externalId = appActivationService.activate(app);
            app.activate(externalId);
            //because no JPA transaction is in process we call repository#save
            appRepository.save(app);
        } catch (Exception e) {
            String message = "Fail to activate App. " + e.getMessage();
            logger.error(message, e);
            return new Failure(message);
        }
        return new Success("App <" + app.getAppName() + "> has been activated.");
    }

    @Override
    public TaskStatus firststart(int entityId, Class<App> entityClass) {
        return start(entityId, entityClass);
    }

    @Override
    public TaskStatus start(int entityId, Class<App> entityClass) {
        final App app = appRepository.findOne(entityId);
        Assert.notNull(app, "App[" + entityId + "] does not exist.");
        logger.debug("Starting App : {}", app);
        return appActivationService.start(app);
    }

    @Override
    public TaskStatus giveCurrentTaskStatus(TaskStatus taskStatus) {
        return appActivationService.getAppStatus(taskStatus);
    }

    @Override
    public TaskStatus stop(int entityId, Class<App> entityClass) {
        final App app;
        try {
            app = appRepository.findOne(entityId);
            Assert.notNull(app, "App[" + entityId + "] does not exist.");
            logger.debug("Stopping App : {}", app);
            appActivationService.stop(app);
            app.stop();
            //because no JPA transaction is in process we call repository#save
            appRepository.save(app);
        } catch (Exception e) {
            String message = "Fail to stop App. " + e.getMessage();
            logger.error(message);
            return new Failure(message);
        }
        return new Success("App <" + app.getAppName() + "> has been stopped.");
    }

    @Override
    public TaskStatus delete(int entityId, Class<App> entityClass) {
        final App app;
        try {
            app = appRepository.findOne(entityId);
            Assert.notNull(app, "App[" + entityId + "] does not exist.");
            logger.debug("Deleting App : {}", app);
            if (app.isActivated() || app.isUnkwown()) {
                appActivationService.delete(app);
                app.delete();
                //because no JPA transaction is in process we call repository#save
                appRepository.save(app);
            } else {
                logger.warn("Will not delete App<" + app + ">. App has not been activated yet.");
            }
        } catch (Exception e) {
            String message = "Fail to delete App. " + e.getMessage();
            logger.error(message);
            return new Failure(message);
        }
        return new Success("App <" + app.getAppName() + "> has been deleted.");
    }

    protected void resolveAppBinaries(final App app) {
        MavenReference appBinaries = app.getAppBinaries();
        try {
            if (appBinaries.getAccessUrl() == null) {
                MavenReference softwareReferenceResolved = mvnDao.resolveUrl(appBinaries);
                app.updateAppBinaries(softwareReferenceResolved);
            }
        } catch (MavenReferenceResolutionException mrre) {
            if (!app.isOptionalApplicationBinaries()) {
                throw mrre;
            }
        }
    }

}
