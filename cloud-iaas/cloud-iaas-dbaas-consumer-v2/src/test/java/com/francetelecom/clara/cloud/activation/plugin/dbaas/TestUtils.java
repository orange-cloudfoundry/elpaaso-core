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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.activation.plugin.dbaas.DBaasConsumer;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;

public class TestUtils {

	static public int timeoutInMinute = 30;
	static public void waitForTaskCompletion(DBaasConsumer dbaasConsumer,
                                             TaskStatus taskStatus, DBaasSubscriptionV2 dbaasSubscription, long pollSleepDelayMs) {

		long taskId = taskStatus.getTaskId();

		// Waiting for task completion
		try {
			long startTime = System.currentTimeMillis();
            TaskStatus status = dbaasConsumer.giveCurrentTaskStatus(taskStatus);
			while (!status.isComplete() && !isTimeoutExpired(startTime)) {
                status = dbaasConsumer.giveCurrentTaskStatus(taskStatus);
                Thread.sleep(pollSleepDelayMs);
            }

            if (status.getTaskStatus() == TaskStatusEnum.FINISHED_FAILED) {
                String errorMessage = status.getErrorMessage();
                if (errorMessage == null || "".equals(errorMessage)) {
					errorMessage = "<Created database is in error - No specific message from DBAAS server - Is timeout expired : "
							+ isTimeoutExpired(startTime) + ">";
                }
                dbaasSubscription.setDeploymentState(DeploymentStateEnum.UNKNOWN);
                throw new TechnicalException("The task " + taskId + " is in ERROR or CANCELLED state. Message : " + errorMessage);
			}
		} catch (Exception exc) {
            throw new TechnicalException(exc);
		}
	}
	
	private static boolean isTimeoutExpired(long startTime) {
		final long currentTime = System.currentTimeMillis();

		return currentTime - startTime > timeoutInMinute * 1000 * 60;
	}

	@PersistenceContext
	EntityManager em;

	@Transactional
	public void persistObjects(Object... objects) {
		for (Object object : objects) {
			persistObject(object, true);
		}
		em.flush();
	}

	@Transactional
	public void persistObject(Object object) {
		persistObject(object, false);
	}

	@Transactional
	public void mergeObject(Object o) {
		em.merge(o);
	}

	private void persistObject(Object object, boolean skipFlush) {
		// Then persist with JPA.
		em.persist(object);
		if (!skipFlush) {
			em.flush();
		}
	}

}
