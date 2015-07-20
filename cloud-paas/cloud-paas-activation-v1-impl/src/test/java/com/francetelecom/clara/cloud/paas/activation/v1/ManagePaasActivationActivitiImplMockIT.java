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

import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.paas.activation.ManagePaasActivation;
import com.francetelecom.clara.cloud.paas.activation.TaskStatusActivation;
import com.francetelecom.clara.cloud.commons.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBException;
import java.io.IOException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@IfProfileValue(name = "test-groups", values = { "legacy-test" })
public class ManagePaasActivationActivitiImplMockIT {
    /**
     * logger
     */
	private static Logger logger = LoggerFactory.getLogger(ManagePaasActivationActivitiImplMockIT.class);
	
	@Autowired
	private ManagePaasActivationActivitiImplMockTestUtils utils;

	@Autowired
	ManagePaasActivation managePaasActivation;
	
    protected static final int timeoutActivateMinutes = 9;
    protected static final int timeoutStartStopDeleteSeconds = 180;
    protected static final int poolPeriodSeconds = 5;

    /**
     * wait for the status to be complete until timeoutSeconds
     * @param status
     * @param currentAction : string that describe the current task (log purpose)
     * @param timeoutSeconds
     * @throws InterruptedException
     */
	private void waitUntilComplete(TaskStatusActivation status, String currentAction, int timeoutSeconds)
            throws InterruptedException {
		long start = System.currentTimeMillis();
        long currentDurationMs = 0;
        currentAction = "waitUntilComplete '"+ currentAction + "'";
        logger.info("**** " + currentAction + " ...");
		while (!status.isComplete()) {
			status = managePaasActivation.giveCurrentTaskStatus(status);
            float timeoutMinutes = timeoutSeconds / 60;
            long timeoutMs = (long) (1000 * timeoutSeconds);
            currentDurationMs = System.currentTimeMillis() - start;
			//TaskStatusActivation.displayTaskStatus(status);
			Assert.assertTrue(timeoutMinutes + " minutes should be enough for "
                            + currentAction + " in mock... please check it.",
                              currentDurationMs < timeoutMs);
			Thread.sleep(poolPeriodSeconds*1000);
		}
        currentDurationMs = System.currentTimeMillis()-start;
        float currentDurationSec = currentDurationMs / 1000;
        String strDuration = "[duration = "+Float.toString(currentDurationSec)+" sec]";
        logger.info("**** " + currentAction + " end " + strDuration);
        String errorMsg = "Error while " + currentAction + " details:" + status.getErrorMessage();
		Assert.assertEquals(errorMsg, TaskStatusEnum.FINISHED_OK, status.getTaskStatus());
	}
	
	@Test
    public void should_create_environment() throws IOException, JAXBException, InterruptedException, NotFoundException {
        assertDefaultStateTransitions(utils.createCfWicketJpaTd(), "MonoVM1Jonas");
    }

    private void assertDefaultStateTransitions(int tdiId, String testConfig) throws NotFoundException, InterruptedException {
    	TaskStatusActivation status;
        utils.checkState(tdiId, DeploymentStateEnum.TRANSIENT);
        status = utils.activate(tdiId);
        //utils.checkState(tdiId, DeploymentStateEnum.STARTING);
        waitUntilComplete(status, "activate " + testConfig, timeoutActivateMinutes*60);
        utils.checkState(tdiId, DeploymentStateEnum.STARTED);
        status = utils.stop(tdiId);
        //utils.checkState(tdiId, DeploymentStateEnum.STOPPING);
        waitUntilComplete(status, "stop " + testConfig, timeoutStartStopDeleteSeconds);
        utils.checkState(tdiId, DeploymentStateEnum.STOPPED);
        status = utils.start(tdiId);
        //utils.checkState(tdiId, DeploymentStateEnum.STARTING);
        waitUntilComplete(status, "start " + testConfig, timeoutStartStopDeleteSeconds);
        utils.checkState(tdiId, DeploymentStateEnum.STARTED);
        status = utils.delete(tdiId);
        //utils.checkState(tdiId, DeploymentStateEnum.REMOVING);
        waitUntilComplete(status, "delete " + testConfig, timeoutStartStopDeleteSeconds);
        utils.checkState(tdiId, DeploymentStateEnum.REMOVED);
    }

}
