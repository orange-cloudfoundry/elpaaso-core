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

import com.francetelecom.clara.cloud.application.ManageTechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.commons.NotFoundException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.EnvironmentStatus;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;
import com.francetelecom.clara.cloud.paas.activation.ManagePaasActivation;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * Implementation of {@link ManagePaasActivation} for vCloud API
 */
public class ManagePaasActivationActivitiUtilsImpl {

	private static Logger logger = LoggerFactory.getLogger(ManagePaasActivationActivitiUtilsImpl.class.getName());

	/** TDI manager */
	private ManageTechnicalDeploymentInstance manageTechnicalDeploymentInstance;
	
	private EnvironmentRepository environmentRepository;
	
	/** BPMN2.0 process generator */
	ActivitiProcessFactory activitiProcessFactory;

	/** Activiti */
	protected ProcessEngine processEngine;

	public void setActivitiProcessFactory(ActivitiProcessFactory activitiProcessFactory) {
		this.activitiProcessFactory = activitiProcessFactory;
	}

	public void setManageTechnicalDeploymentInstance(ManageTechnicalDeploymentInstance manageTechnicalDeploymentInstance) {
		this.manageTechnicalDeploymentInstance = manageTechnicalDeploymentInstance;
	}

	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	public void setEnvironmentRepository(EnvironmentRepository environmentdao) {
		this.environmentRepository = environmentdao;
	}

    public String createProcess(TechnicalDeploymentInstance tdi, ActivationStepEnum processType) {
        String processName = "activate-" + tdi.getName() + "-" + processType.getName() + "-" + System.currentTimeMillis();
        Process process;
        try {
            process = activitiProcessFactory.generateProcessFromTDI(processType, tdi);
        } catch (JAXBException | IOException e) {
            throw new TechnicalException("unable to generate process from TDI", e);
        }
 		
    	BpmnModel model = new BpmnModel();
    	model.addProcess(process);
        processEngine.getRepositoryService().createDeployment().addBpmnModel(processName +".bpmn", model).name("Activate " + tdi.getName()).deploy();

        //processEngine.getRepositoryService().createDeployment().name("Activate " + tdi.getName()).addInputStream(processFile.getName(), processIs).deploy();
        return process.getId();
    }

    @Transactional
    public TechnicalDeploymentInstance getTDI(int tdiId) throws NotFoundException {
        TechnicalDeploymentInstance tdi = manageTechnicalDeploymentInstance.findTechnicalDeploymentInstance(tdiId);
        // parse mandatory lazy attributes
        for (XaasSubscription subs : tdi.getTechnicalDeployment().listXaasSubscriptionTemplates()) {
            subs.getName();
            subs.listDepedencies();
        }

        return tdi;
    }

    @Transactional
    public void handleError(String action, Throwable exc, TaskStatus status, int tdiId) {
        String errorMessage = "Internal error while " + action;
        String endUserErrorMessage = "INTERNAL ERROR DURING ACTIVATION PROCESS " + exc.getMessage();
        logger.error(errorMessage, exc);
        status.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
        status.setEndTime(System.currentTimeMillis());
        status.setErrorMessage(endUserErrorMessage);

        final Environment environment = environmentRepository.findByTechnicalDeploymentInstanceId(tdiId);
        environment.updateStatus(EnvironmentStatus.FAILED, endUserErrorMessage, -1);
    }

    public ProcessInstance runProcess(int tdiId, ActivationStepEnum processType, String processId) {
        logger.debug("Starting process "+processType.getName()+" for TechnicalDeploymentInstance#" + tdiId);
        ProcessInstance processInstance = processEngine.getRuntimeService().startProcessInstanceByKey(processId);
        logger.info("Process "+processType.getName()+" for TechnicalDeploymentInstance#" + tdiId + " is started with id=" + processInstance.getId());
        return processInstance;
    }
}
