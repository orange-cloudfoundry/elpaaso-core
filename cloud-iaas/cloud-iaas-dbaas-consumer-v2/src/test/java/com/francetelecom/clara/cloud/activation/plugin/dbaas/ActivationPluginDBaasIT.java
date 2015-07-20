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

import com.francetelecom.clara.cloud.coremodel.ActivationContext;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.paas.activation.v1.ActivationPluginIT;
import com.francetelecom.clara.cloud.paas.activation.v1.ActivationPluginTestHelper;
import com.francetelecom.clara.cloud.paas.activation.v1.ActivationPluginTestHelper.EntityInfo;
import com.francetelecom.clara.cloud.commons.NotFoundException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class ActivationPluginDBaasIT extends ActivationPluginIT {
	@Autowired
	protected ActivationPluginTestHelper helper;

    @Test
	public void activateStopStartDelete() throws NotFoundException, InterruptedException {
        TechnicalDeploymentInstance tdi = helper.createTDI();
        int tdiId = helper.createTdiEnvironment(tdi.getId());

		Set<EntityInfo> acceptedEntityInfos = helper.getEntityInfos(tdiId);
		assertTrue("No entity to test?", acceptedEntityInfos.size() > 0);
		
		for (EntityInfo entityInfo : acceptedEntityInfos) {
			try {
				helper.setup(tdiId, entityInfo.getId());
                helper.activate(tdiId, entityInfo.getId(), entityInfo.getType(), new ActivationContext(UUID.randomUUID().toString(), "envTest"));
                assertTrue("Not activated", helper.isActivated(tdiId, entityInfo.getId()));
				helper.firststart(tdiId, entityInfo.getId(), entityInfo.getType());
				assertTrue("Not started", helper.isStarted(tdiId, entityInfo.getId()));
				helper.stop(tdiId, entityInfo.getId(), entityInfo.getType());
				assertTrue("Not stopped", helper.isStopped(tdiId, entityInfo.getId()));
				helper.start(tdiId, entityInfo.getId(), entityInfo.getType());
				assertTrue("Not started", helper.isStarted(tdiId, entityInfo.getId()));
				helper.delete(tdiId, entityInfo.getId(), entityInfo.getType());
				assertTrue("Not deleted", helper.isDeleted(tdiId, entityInfo.getId()));
			} finally {
				// Cleanup even if there are errors
				helper.cleanup(tdiId, entityInfo.getId(), entityInfo.getType());
			}
		}
	}
}
