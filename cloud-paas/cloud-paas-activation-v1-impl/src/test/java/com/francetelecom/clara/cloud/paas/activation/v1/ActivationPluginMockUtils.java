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
package com.francetelecom.clara.cloud.paas.activation.v1;

import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.ModelItem;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import com.francetelecom.clara.cloud.commons.NotFoundException;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

public class ActivationPluginMockUtils {

    private static Logger logger = LoggerFactory.getLogger(ActivationPluginMockUtils.class.getName());

    @Autowired
    private ManageModelItem manageModelItem;

    @Transactional
    public void init(int entityId, Class<? extends ModelItem> entityClass) throws IOException, NotFoundException, InterruptedException {
        logger.info("Initializing " + entityId);
        Thread.sleep(1000);
        ModelItem modelItem = manageModelItem.findModelItem(entityId, entityClass);
        if (modelItem instanceof XaasSubscription) {
            XaasSubscription sub = (XaasSubscription) modelItem;
            logger.info(sub.getClass().getName() + " description=" + sub.getDescription());
        }
        if (modelItem instanceof DependantModelItem) {
            DependantModelItem entity = (DependantModelItem) modelItem;
            // Set deployment state and check that dependant item has already this same state
            entity.setDeploymentState(DeploymentStateEnum.CHECKED);
            for (DependantModelItem entityDep : entity.listDepedencies()) {
                Assert.assertEquals("Wrong state for dependant entity " + entityDep.getClass().getName() + "#" + entityDep.getId() + " <- " + entity.getClass().getName() + "#" + entity.getId() + ". Is the activation process done in correct order?", DeploymentStateEnum.CHECKED, entityDep.getDeploymentState());
            }
        }
    }

    @Transactional
    public void activate(int entityId, Class<? extends ModelItem> entityClass) throws IOException, NotFoundException, InterruptedException {
        logger.info("Activating " + entityId);
        Thread.sleep(1000);
        ModelItem modelItem = manageModelItem.findModelItem(entityId, entityClass);
        if (modelItem instanceof DependantModelItem) {
            DependantModelItem entity = (DependantModelItem) modelItem;
            // Set deployment state and check that dependant item has already this same state
            entity.setDeploymentState(DeploymentStateEnum.CREATED);
            for (DependantModelItem entityDep : entity.listDepedencies()) {
                Assert.assertEquals("Wrong state for dependant entity " + entityDep.getClass().getName() + "#" + entityDep.getId() + " <- " + entity.getClass().getName() + "#" + entity.getId() + ". Is the activation process done in correct order?", DeploymentStateEnum.CREATED, entityDep.getDeploymentState());
            }
        }
    }

    @Transactional
    public void firststart(int entityId, Class<? extends ModelItem> entityClass) throws IOException, NotFoundException, InterruptedException {
        logger.info("First starting " + entityId);
        Thread.sleep(1000);
        ModelItem modelItem = manageModelItem.findModelItem(entityId, entityClass);
        if (modelItem instanceof DependantModelItem) {
            DependantModelItem entity = (DependantModelItem) modelItem;
            // Set deployment state and check that dependant item has already this same state
            entity.setDeploymentState(DeploymentStateEnum.STARTED);
            for (DependantModelItem entityDep : entity.listDepedencies()) {
                Assert.assertEquals("Wrong state for dependant entity " + entityDep.getClass().getName() + "#" + entityDep.getId() + " <- " + entity.getClass().getName() + "#" + entity.getId() + ". Is the activation process done in correct order?", DeploymentStateEnum.STARTED, entityDep.getDeploymentState());
            }
        }
    }

    @Transactional
    public void start(int entityId, Class<? extends ModelItem> entityClass) throws IOException, NotFoundException, InterruptedException {
        logger.info("Starting " + entityId);
        ModelItem modelItem = manageModelItem.findModelItem(entityId, entityClass);
        if (modelItem instanceof DependantModelItem) {
            DependantModelItem entity = (DependantModelItem) modelItem;
            // Set deployment state and check that dependant item has already this same state
            entity.setDeploymentState(DeploymentStateEnum.STARTED);
            for (DependantModelItem entityDep : entity.listDepedencies()) {
                Assert.assertEquals("Wrong state for dependant entity " + entityDep.getClass().getName() + "#" + entityDep.getId() + " <- " + entity.getClass().getName() + "#" + entity.getId() + ". Is the activation process done in correct order?", DeploymentStateEnum.STARTED, entityDep.getDeploymentState());
            }
        }
    }

    @Transactional
    public void stop(int entityId, Class<? extends ModelItem> entityClass) throws IOException, NotFoundException, InterruptedException {
        logger.info("Stopping " + entityClass.getSimpleName() + "#" + entityId);
        ModelItem modelItem = manageModelItem.findModelItem(entityId, entityClass);
        if (modelItem instanceof DependantModelItem) {
            DependantModelItem entity = (DependantModelItem) modelItem;
            // Set deployment state and check that dependant item has already this same state
            entity.setDeploymentState(DeploymentStateEnum.STOPPED);
            for (DependantModelItem entityDep : entity.listDepedencies()) {
                // assertNotSame because the processus is inverted
                Assert.assertNotSame("Wrong state for (" + entityDep.getDeploymentState().name() + ") dependant entity " + entityDep.getClass().getName() + "#" + entityDep.getId() + " <- " + entity.getClass().getName() + "#" + entity.getId() + ". Is the activation process done in correct order?", DeploymentStateEnum.STOPPED, entityDep.getDeploymentState());
            }
        }
    }

    @Transactional
    public void delete(int entityId, Class<? extends ModelItem> entityClass) throws IOException, NotFoundException, InterruptedException {
        logger.info("Deleting " + entityId);
        ModelItem modelItem = manageModelItem.findModelItem(entityId, entityClass);
        if (modelItem instanceof DependantModelItem) {
            DependantModelItem entity = (DependantModelItem) modelItem;
            // Set deployment state and check that dependant item has already this same state
            entity.setDeploymentState(DeploymentStateEnum.REMOVED);
            for (DependantModelItem entityDep : entity.listDepedencies()) {
                // assertNotSame because the processus is inverted
                Assert.assertNotSame("Wrong state for dependant entity " + entityDep.getClass().getName() + "#" + entityDep.getId() + " <- " + entity.getClass().getName() + "#" + entity.getId() + ". Is the activation process done in correct order?", DeploymentStateEnum.REMOVED, entityDep.getDeploymentState());
            }
        }
    }

    public void setManageModelItem(ManageModelItem manageModelItem) {
        this.manageModelItem = manageModelItem;
    }
}
