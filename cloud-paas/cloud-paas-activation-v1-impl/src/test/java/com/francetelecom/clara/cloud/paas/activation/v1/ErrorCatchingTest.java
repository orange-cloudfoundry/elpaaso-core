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

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class    ErrorCatchingTest {
    private static Logger LOGGER = LoggerFactory.getLogger(ErrorCatchingTest.class);

    @Autowired
    RuntimeService runtimeService;

    @Autowired
    RepositoryService repositoryService;

    @Test
    // sometimes this test fail on FaaS on assertProcessEnded but we don't know why :-(
    @IfProfileValue(name="test-groups", values={"activiti"})
    public void test() throws InterruptedException {

        // deploy the process
        repositoryService
                .createDeployment()
                .addClasspathResource(
                        "com/francetelecom/clara/cloud/paas/activation/v1/error-catching.bpmn20.xml")
                .deploy();

        // start a new process instance
        final ProcessInstance pi = runtimeService
                .startProcessInstanceByKey("error-catching-process");

        // fetch active executions
        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceId(pi.getId()).list();

        assertEquals("there should be 2 pending executions", 2,
                executions.size());

        // one is for the fork

        // fetch active execution
        Execution failingtaskExec = runtimeService.createExecutionQuery()
                .processInstanceId(pi.getId())
                .activityId("failingtask").singleResult();
        // assert that a 'failingtask' execution exists
        assertNotNull(failingtaskExec);

        runtimeService.signal(failingtaskExec.getId());

        assertProcessEnded(pi);


    }

    private void assertProcessEnded(final ProcessInstance pi) {
        // fetch active executions
        RetryTemplate retryTemplate = new RetryTemplate();
        final SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(5);
        retryTemplate.setRetryPolicy(retryPolicy);
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        retryTemplate.setBackOffPolicy(backOffPolicy);
        retryTemplate.execute(new RetryCallback<Void, RuntimeException>() {
            @Override
            public Void doWithRetry(RetryContext retryContext) throws RuntimeException {
                List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().list();
                int processCount = processInstances.size();
                LOGGER.info("waiting for process to end (Still {} process running).", processCount);
                if (processCount > 0) {
                    for (ProcessInstance processInstance : processInstances) {
                        LOGGER.info("Process info: {} ", dumpprocessInstance(processInstance));
                    }
                    throw new IllegalStateException("Some process still running. Left :" + processCount);
                }
                return null;
            }
        });
    }

    private String dumpprocessInstance(ProcessInstance processInstance) {
        if (processInstance == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();

        builder.append("Name: ");
        builder.append(processInstance.getName());
        builder.append(" - BusinessKey: ");
        builder.append(processInstance.getBusinessKey());
        builder.append(" - ProcessDefinitionName: ");
        builder.append(processInstance.getProcessDefinitionName());
        builder.append(" - isSuspended: ");
        builder.append(processInstance.isSuspended());
        builder.append(" - isEnded: ");
        builder.append(processInstance.isEnded());

        return builder.toString();
    }

}
