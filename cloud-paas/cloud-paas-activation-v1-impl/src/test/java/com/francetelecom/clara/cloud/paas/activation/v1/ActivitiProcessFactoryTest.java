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

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.paas.activation.ActivationStepEnum;

@RunWith(MockitoJUnitRunner.class)
public class ActivitiProcessFactoryTest {

	@Spy
	@InjectMocks
	ActivitiProcessFactory activitiProcessFactory;

	@Test
	public void must_generate_process_from_tdi() throws JAXBException, IOException {
		// GIVEN
		ActivationStepEnum activationStep = ActivationStepEnum.ACTIVATE;
		TechnicalDeploymentInstance tdi = mock(TechnicalDeploymentInstance.class);
		org.activiti.bpmn.model.Process tprocess = mock(org.activiti.bpmn.model.Process.class);

		reset(activitiProcessFactory);
		doReturn(tprocess).when(activitiProcessFactory).createActivateProcess(tdi);
		doNothing().when(activitiProcessFactory).addBoundaryErrorEvent(tdi, tprocess, activationStep);
		// WHEN
		activitiProcessFactory.generateProcessFromTDI(activationStep, tdi);
		// THEN
		verify(activitiProcessFactory).createActivateProcess(tdi);
		verify(activitiProcessFactory).addBoundaryErrorEvent(tdi, tprocess, activationStep);
		verify(activitiProcessFactory).generateProcessFromTDI(activationStep, tdi);
		verifyNoMoreInteractions(activitiProcessFactory);
	}

	@Test
	public void generated_process_should_be_set_as_executable_by_default() throws JAXBException, IOException {
		// GIVEN
		TechnicalDeploymentInstance tdi = mock(TechnicalDeploymentInstance.class);
		doReturn(new TechnicalDeployment("name")).when(tdi).getTechnicalDeployment();

		reset(activitiProcessFactory);
		// WHEN
		org.activiti.bpmn.model.Process process = activitiProcessFactory.createActivateProcess(tdi);
		// THEN
		Assertions.assertThat(process.isExecutable()).isTrue();
	}
}
