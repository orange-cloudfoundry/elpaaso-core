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
package com.francetelecom.clara.cloud.activation.plugin.dbaas;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.model.*;
import com.francetelecom.clara.cloud.paas.activation.v1.ActivationPluginTestHelper;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasVersion;
import com.francetelecom.clara.cloud.technicalservice.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration
public class ActivationPluginTestHelperDBaas extends ActivationPluginTestHelper {

    private DBaasConsumer dBaasConsumer;

    private static Logger logger = LoggerFactory.getLogger(ActivationPluginTestHelperDBaas.class);

    public static final String GAV_POSTGRESQL_POPULATION_SCRIPT = "com.francetelecom.clara.prototype.commons:commons-sql-postgresql:7.0.1::sql";

    /**
     * Called to verify that ACTIVATE step has been well done.
     *
     * @param entityId
     * @return true if ACTIVATE step is ok
     */
    @Override
    @Transactional
    public boolean isActivated(int tdiId, int entityId) throws NotFoundException {
        ModelItem entity = manager.findModelItem(entityId, DBaasSubscriptionV2.class);
        DBaasSubscriptionV2 dbaas = (DBaasSubscriptionV2) entity;
        try {
            DBaasServiceStateEnum status = dBaasConsumer.getStatus(dbaas.getDatabaseUUId());
            return status != null && status.equals(DBaasServiceStateEnum.ACTIVE);
        } catch (TechnicalException e) {
            if (e.getMessage().contains("File does not exist")) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Called to verify that START or FIRSTSTART step has been well done.
     *
     * @param entityId
     * @return true if START or FIRSTSTART step is ok
     */
    @Override
    @Transactional
    public boolean isStarted(int tdiId, int entityId) throws NotFoundException {
        ModelItem entity = manager.findModelItem(entityId, DBaasSubscriptionV2.class);
        DBaasSubscriptionV2 dbaas = (DBaasSubscriptionV2) entity;
        try {
            DBaasServiceStateEnum status = dBaasConsumer.getStatus(dbaas.getDatabaseUUId());
            return status != null && status.equals(DBaasServiceStateEnum.ACTIVE);
        } catch (TechnicalException e) {
            if (e.getMessage().contains("is not deployed")) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Called to verify that STOP step has been well done.
     *
     * @param entityId
     * @return true if STOP step is ok
     * @throws NotFoundException
     */
    @Override
    public boolean isStopped(int tdiId, int entityId) throws NotFoundException {
        ModelItem entity = manager.findModelItem(entityId, DBaasSubscriptionV2.class);
        DBaasSubscriptionV2 dbaas = (DBaasSubscriptionV2) entity;
        try {
            DBaasServiceStateEnum status = dBaasConsumer.getStatus(dbaas.getDatabaseUUId());
            return status != null && status.equals(DBaasServiceStateEnum.STOPPED);
        } catch (TechnicalException e) {
            if (e.getMessage().contains("is not deployed")) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Called to verify that DELETE step has been well done.
     *
     * @param entityId
     * @return true if DELETE step is ok
     * @throws NotFoundException
     */
    @Override
    public boolean isDeleted(int tdiId, int entityId) throws NotFoundException {
        ModelItem entity = manager.findModelItem(entityId, DBaasSubscriptionV2.class);
        DBaasSubscriptionV2 dbaas = (DBaasSubscriptionV2) entity;
        try {
            DBaasServiceStateEnum status = dBaasConsumer.getStatus(dbaas.getDatabaseUUId());
            return status != null && status.equals(DBaasServiceStateEnum.DELETED);
        } catch (TechnicalException e) {
            if (e.getMessage().contains("is not deployed")) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Because plugins does not clean up VM (not necessary) we have this cleanup
     * function to be able to reuse the test VM
     *
     * @param entityId
     */
    @Override
    @Transactional
    public void cleanup(int tdiId, int entityId, Class<? extends ModelItem> entityType) throws NotFoundException {
        ModelItem entity = manager.findModelItem(entityId, DBaasSubscriptionV2.class);
        DBaasSubscriptionV2 dbaas = (DBaasSubscriptionV2) entity;
        try {
            DBaasServiceStateEnum status = dBaasConsumer.getStatus(dbaas.getDatabaseUUId());
            if (status != null && !status.equals(DBaasServiceStateEnum.DELETED) && !status.equals(DBaasServiceStateEnum.DELETING)) {
                dBaasConsumer.deleteDatabase(dbaas);
            }
            // Do not wait since we do not want to throw error from here
        } catch (TechnicalException e) {
            logger.error("Ignored error in cleanup function: " + e.getMessage(), e);
        }
    }

    public void setdBaasConsumer(DBaasConsumer dBaasConsumer) {
        this.dBaasConsumer = dBaasConsumer;
    }

    @Override
    public void setup(int tdiId, int entityId) throws NotFoundException {
        // do not need any VM
        // so overrides default parent behavior and do nothing
    }

    @Override
    @Transactional
    public TechnicalDeploymentInstance createTDI() {
        TechnicalDeployment td = new TechnicalDeployment("name");
        DBaasSubscriptionV2 dbaasSub = new DBaasSubscriptionV2(td);

        dbaasSub.changeStorageCapacity(1000);
        dbaasSub.setInitialPopulationScript(MavenReference.fromGavString(GAV_POSTGRESQL_POPULATION_SCRIPT));
        dbaasSub.setDescription("TechnicalDeploymentTestFactory-generated DBaaS");
        // FIXME: Default to orange DBAAS V1, choice of DBaas version
        // should be dynamic in tests
        dbaasSub.setDbaasVersion(DBaasVersion.DBAAS_10.name());

        // Create TDT from TD
        TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);

        manageTechnicalDeploymentTemplate.createTechnicalDeploymentTemplate(tdt);
        TechnicalDeployment tdCopy = tdCloner.deepCopy(td);
        manageTechnicalDeploymentCrud.createTechnicalDeployment(tdCopy);
        TechnicalDeploymentInstance tdi = managerTdi.createTechnicalDeploymentInstance(tdt, tdCopy);

        return tdi;
    }
}
