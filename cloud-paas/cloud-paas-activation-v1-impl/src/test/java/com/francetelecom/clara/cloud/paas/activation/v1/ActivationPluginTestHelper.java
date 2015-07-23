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
import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentCrud;
import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.commons.NotFoundException;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.dao.TechnicalDeploymentCloner;
import com.francetelecom.clara.cloud.model.*;
import com.francetelecom.clara.cloud.model.validators.ModelItemGenericValidationUtils;
import com.francetelecom.clara.cloud.paas.activation.ActivationPlugin;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * plugin test helpers' parent Sample usage : see samples in
 * cloud-iaas-(name)-consumer/test/resources Last update : $LastChangedDate:
 * 2012-09-14 15:42:13 +0200 (Fri, 14 Sep 2012) $
 *
 * @author : $Author: dwvd1206 $
 * @version : $Revision: 20019 $
 */
@ContextConfiguration
public abstract class ActivationPluginTestHelper {

    private static final PaasUser BOB_DYLAN = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");

    /**
     * logger
     */
    private static Logger logger = LoggerFactory.getLogger(ActivationPluginTestHelper.class);

    @Autowired
    protected ManageTechnicalDeploymentCrud manageTechnicalDeploymentCrud;

    @Autowired
    protected ManageTechnicalDeploymentTemplate manageTechnicalDeploymentTemplate;

    @Autowired
    @Qualifier("manageTechnicalDeploymentInstance")
    protected ManageTechnicalDeploymentInstance managerTdi;

    @Autowired
    @Qualifier("manageModelItem")
    protected ManageModelItem manager;

    @Autowired(required = true)
    protected TechnicalDeploymentCloner tdCloner;

    protected ActivationPlugin plugin;

    @Autowired
    private TechnicalDeploymentTestFactory technicalDeploymentTestFactory;

    @Autowired
    private ApplicationRepository applicationRepository;
    @Autowired
    private PaasUserRepository paasUserRepository;
    @Autowired
    private ApplicationReleaseRepository applicationReleaseRepository;
    @Autowired
    private EnvironmentRepository environmentRepository;

    private String earUrl;

    private boolean verboseValidation = false;

    /**
     * Called to verify that ACTIVATE step has been well done.
     *
     * @param entityId
     * @return true if ACTIVATE step is ok
     */
    public abstract boolean isActivated(int tdiId, int entityId) throws NotFoundException;

    /**
     * Called to verify that START or FIRSTSTART step has been well done.
     *
     * @param entityId
     * @return true if START or FIRSTSTART step is ok
     */
    public abstract boolean isStarted(int tdiId, int entityId) throws NotFoundException;

    /**
     * Called to verify that STOP step has been well done.
     *
     * @param entityId
     * @return true if STOP step is ok
     */
    public abstract boolean isStopped(int tdiId, int entityId) throws NotFoundException;

    /**
     * Called to verify that DELETE step has been well done.
     *
     * @param entityId
     * @return true if DELETE step is ok
     */
    public abstract boolean isDeleted(int tdiId, int entityId) throws NotFoundException;

    /**
     * Clean up VM
     *
     * @param entityId
     */
    public void cleanup(int tdiId, int entityId, Class<? extends ModelItem> entityType) throws NotFoundException {
        TechnicalDeploymentInstance tdi = managerTdi.findTechnicalDeploymentInstance(tdiId);
    }

    /**
     * Set-up method to prepare test VM (create and start VM by default)
     *
     * @param entityId
     */

    public void setup(int tdiId, int entityId) throws NotFoundException {
        // create and start VM by default
        TechnicalDeploymentInstance tdi = managerTdi.findTechnicalDeploymentInstance(tdiId);
    }

    @Transactional
    /**
     * @param iaasSubscription either VCDIaasSubscription.class or AWSIaasSubscription.class
     */
    public TechnicalDeploymentInstance createTDI() {
        // Create TD
        TechnicalDeployment td = technicalDeploymentTestFactory.createWicketJpaTD("dynamic app name" + "_" + System.currentTimeMillis(), earUrl);

        // Hook to customize TD according to tested plugin
        td = customizeTD(td);

        // Create TDT from TD
        TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);

        if (verboseValidation) {
            logger.info("optional model validation (before TDT creation)");
            ModelItemGenericValidationUtils.validateModel(tdt, logger);
        }

        manageTechnicalDeploymentTemplate.createTechnicalDeploymentTemplate(tdt);
        TechnicalDeployment tdCopy = tdCloner.deepCopy(td);
        manageTechnicalDeploymentCrud.createTechnicalDeployment(tdCopy);
        TechnicalDeploymentInstance tdi = managerTdi.createTechnicalDeploymentInstance(tdt, tdCopy);

        return tdi;
    }

    @Transactional
    public int createTdiEnvironment(int tdiId) throws NotFoundException {
        TechnicalDeploymentInstance tdi = managerTdi.findTechnicalDeploymentInstance(tdiId);
        String envLabel = "test env " + this.getClass().getSimpleName();

        Application application = new Application("testApp", "1234");
        applicationRepository.save(application);

        paasUserRepository.save(BOB_DYLAN);

        ApplicationRelease applicationRelease = new ApplicationRelease(application, "GOROCO");
        applicationReleaseRepository.save(applicationRelease);

        Environment environment = new Environment(DeploymentProfileEnum.TEST, envLabel, applicationRelease, BOB_DYLAN, tdi);
        environmentRepository.persist(environment);
        return tdiId;
    }

    /*
     * enable to customize a TD for a specific plugin e.g. add a
     * StoreJCloudsSubscription for Storage activation plugin test
     */
    protected TechnicalDeployment customizeTD(TechnicalDeployment td) {
        return td;
    }

    @Transactional
    public Set<EntityInfo> getEntityInfos(int tdiId) throws NotFoundException {
        Set<EntityInfo> acceptedIds = new HashSet<EntityInfo>();
        TechnicalDeploymentInstance tdi = managerTdi.findTechnicalDeploymentInstance(tdiId);
        // iaas plugins accept TDI as entity
        if (plugin.accept(tdi.getClass(), ActivationStepEnum.ACTIVATE) || plugin.accept(tdi.getClass(), ActivationStepEnum.FIRSTSTART)
                || plugin.accept(tdi.getClass(), ActivationStepEnum.START) || plugin.accept(tdi.getClass(), ActivationStepEnum.STOP)
                || plugin.accept(tdi.getClass(), ActivationStepEnum.DELETE)) {
            acceptedIds.add(new EntityInfo(tdi.getId(), tdi.getClass()));
        }

        for (XaasSubscription sub : tdi.getTechnicalDeployment().listXaasSubscriptionTemplates()) {
            if (plugin.accept(sub.getClass(), ActivationStepEnum.ACTIVATE) || plugin.accept(sub.getClass(), ActivationStepEnum.FIRSTSTART)
                    || plugin.accept(sub.getClass(), ActivationStepEnum.START) || plugin.accept(sub.getClass(), ActivationStepEnum.STOP)
                    || plugin.accept(sub.getClass(), ActivationStepEnum.DELETE)) {
                acceptedIds.add(new EntityInfo(sub.getId(), sub.getClass()));
            }
        }

        return acceptedIds;
    }

    @Transactional
    public void activate(int tdiId, int entityId, Class<? extends ModelItem> entityType, ActivationContext context) throws InterruptedException,
            NotFoundException {

        ModelItem entity = manager.findModelItem(entityId, entityType);
        TechnicalDeploymentInstance tdi = managerTdi.findTechnicalDeploymentInstance(tdiId);

        // Activate it
        if (plugin.accept(entity.getClass(), ActivationStepEnum.ACTIVATE)) {
            TaskStatus status = plugin.activate(entity, context);
            while (!status.isComplete()) {
                status = plugin.giveCurrentTaskStatus(status);
                // TaskStatus.displayTaskStatus(status);
                Thread.sleep(1000);
            }
            Assert.assertEquals(status.getErrorMessage(), TaskStatusEnum.FINISHED_OK, status.getTaskStatus());
        }
    }

    @Transactional
    public void firststart(int tdiId, int entityId, Class<? extends ModelItem> entityType) throws InterruptedException, NotFoundException {

        ModelItem entity = manager.findModelItem(entityId, entityType);
        TechnicalDeploymentInstance tdi = managerTdi.findTechnicalDeploymentInstance(tdiId);

        // First start it
        if (plugin.accept(entity.getClass(), ActivationStepEnum.FIRSTSTART)) {
            TaskStatus status = plugin.firststart(entity);
            while (!status.isComplete()) {
                status = plugin.giveCurrentTaskStatus(status);
                // TaskStatus.displayTaskStatus(status);
                Thread.sleep(1000);
            }
            Assert.assertEquals(status.getErrorMessage(), TaskStatusEnum.FINISHED_OK, status.getTaskStatus());
        }
    }

    @Transactional
    public void stop(int tdiId, int entityId, Class<? extends ModelItem> entityType) throws InterruptedException, NotFoundException {

        ModelItem entity = manager.findModelItem(entityId, entityType);
        TechnicalDeploymentInstance tdi = managerTdi.findTechnicalDeploymentInstance(tdiId);

        // Stop it
        if (plugin.accept(entity.getClass(), ActivationStepEnum.STOP)) {
            TaskStatus status = plugin.stop(entity);
            while (!status.isComplete()) {
                status = plugin.giveCurrentTaskStatus(status);
                // TaskStatus.displayTaskStatus(status);
                Thread.sleep(1000);
            }
            Assert.assertEquals(status.getErrorMessage(), TaskStatusEnum.FINISHED_OK, status.getTaskStatus());
        }
    }

    @Transactional
    public void start(int tdiId, int entityId, Class<? extends ModelItem> entityType) throws InterruptedException, NotFoundException {

        ModelItem entity = manager.findModelItem(entityId, entityType);
        TechnicalDeploymentInstance tdi = managerTdi.findTechnicalDeploymentInstance(tdiId);

        // Start it
        if (plugin.accept(entity.getClass(), ActivationStepEnum.START)) {
            TaskStatus status = plugin.start(entity);
            while (!status.isComplete()) {
                status = plugin.giveCurrentTaskStatus(status);
                // TaskStatus.displayTaskStatus(status);
                Thread.sleep(1000);
            }
            Assert.assertEquals(status.getErrorMessage(), TaskStatusEnum.FINISHED_OK, status.getTaskStatus());
        }
    }

    @Transactional
    public void delete(int tdiId, int entityId, Class<? extends ModelItem> entityType) throws InterruptedException, NotFoundException {

        ModelItem entity = manager.findModelItem(entityId, entityType);
        TechnicalDeploymentInstance tdi = managerTdi.findTechnicalDeploymentInstance(tdiId);

        // Delete it
        if (plugin.accept(entity.getClass(), ActivationStepEnum.DELETE)) {
            TaskStatus status = plugin.delete(entity);
            while (!status.isComplete()) {
                status = plugin.giveCurrentTaskStatus(status);
                // TaskStatus.displayTaskStatus(status);
                Thread.sleep(1000);
            }
            Assert.assertEquals(status.getErrorMessage(), TaskStatusEnum.FINISHED_OK, status.getTaskStatus());
        }
    }

    public ActivationPlugin getPlugin() {
        return plugin;
    }

    public void setPlugin(ActivationPlugin plugin) {
        this.plugin = plugin;
    }

    public void setEarUrl(String earUrl) {
        this.earUrl = earUrl;
    }

    public class EntityInfo {
        private Integer id;

        private Class<? extends ModelItem> type;

        public EntityInfo(Integer id, Class<? extends ModelItem> type) {
            super();
            this.id = id;
            this.type = type;
        }

        public Integer getId() {
            return id;
        }

        public Class<? extends ModelItem> getType() {
            return type;
        }

    }

}
