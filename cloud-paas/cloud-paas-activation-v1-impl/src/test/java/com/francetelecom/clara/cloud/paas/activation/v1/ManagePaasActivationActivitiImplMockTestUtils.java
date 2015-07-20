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

import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentCrud;
import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.dao.TechnicalDeploymentCloner;
import com.francetelecom.clara.cloud.model.*;
import com.francetelecom.clara.cloud.paas.activation.ManagePaasActivation;
import com.francetelecom.clara.cloud.paas.activation.TaskStatusActivation;
import com.francetelecom.clara.cloud.commons.NotFoundException;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBException;
import java.io.IOException;

@ContextConfiguration
public class ManagePaasActivationActivitiImplMockTestUtils {

	@Autowired
	private ManageTechnicalDeploymentCrud manageTechnicalDeploymentCrud;

	@Autowired
	private ManageTechnicalDeploymentInstance manageTechnicalDeploymentInstance;

	@Autowired
	private ManageTechnicalDeploymentTemplate manageTechnicalDeploymentTemplate;

	@Autowired
	ManagePaasActivation managePaasActivation;

	@Autowired
	protected TechnicalDeploymentTestFactory technicalDeploymentTestFactory;

	@Autowired
	@Qualifier("technicalDeploymentCloner")
	private TechnicalDeploymentCloner cloner;

	@Transactional
	public int createCfWicketJpaTd() throws IOException, JAXBException, InterruptedException, NotFoundException {
		// Create TD
		TechnicalDeployment td = technicalDeploymentTestFactory
				.createWicketJpaTD(
						"should_create_environment" + System.currentTimeMillis(),
						"com.francetelecom.clara.prototype.springoojpa:springoojpa-ear:7.0.0-SNAPSHOT:postgresql:ear"
				);
		TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
		manageTechnicalDeploymentTemplate.createTechnicalDeploymentTemplate(tdt);
		TechnicalDeployment copy = this.cloner.deepCopy(td);
		manageTechnicalDeploymentCrud.createTechnicalDeployment(copy);
		return manageTechnicalDeploymentInstance.createTechnicalDeploymentInstance(tdt, copy).getId();
	}

	@Transactional
	public void checkState(int tdiId, DeploymentStateEnum state) throws NotFoundException, InterruptedException {
		TechnicalDeploymentInstance tdi = manageTechnicalDeploymentInstance.findTechnicalDeploymentInstance(tdiId);
		Assert.assertNotNull(tdi);
		Assert.assertEquals(state, tdi.getDeploymentState());
		if (!state.equals(DeploymentStateEnum.TRANSIENT)) {
			for (XaasSubscription sub : tdi.getTechnicalDeployment().listXaasSubscriptionTemplates()) {
				// TODO Handle VCDIaasSubscription case
					Assert.assertEquals("XaasSubscription " + sub.getName() + " => is " + sub.getDeploymentState() + " instead of " + state, state,
							sub.getDeploymentState());
			}
		}
	}

	public TaskStatusActivation activate(int tdiId) throws NotFoundException, InterruptedException {
		return managePaasActivation.activate(tdiId);
	}

	public TaskStatusActivation stop(int tdiId) throws NotFoundException, InterruptedException {
		return managePaasActivation.stop(tdiId);
	}

	public TaskStatusActivation start(int tdiId) throws NotFoundException, InterruptedException {
		return managePaasActivation.start(tdiId);
	}

	public TaskStatusActivation delete(int tdiId) throws NotFoundException, InterruptedException {
		return managePaasActivation.delete(tdiId);
	}
}
