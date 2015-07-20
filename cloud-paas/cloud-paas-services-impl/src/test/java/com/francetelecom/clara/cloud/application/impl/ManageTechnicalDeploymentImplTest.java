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
package com.francetelecom.clara.cloud.application.impl;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.core.domain.ApplicationReleaseRepository;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.model.*;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Test Business implementation for TechnicalDeployment component
 *
 * @author Clara
 */
public class ManageTechnicalDeploymentImplTest {

    private static final PaasUser JOE_DALTON = new PaasUser("Joe", "Dalton", new SSOId("jdalton"), "joe.dalton@alcatraz.com");
    ManageTechnicalDeploymentImpl manageTechnicalDeployment = null;

    @Before
    public void setup() {
        manageTechnicalDeployment = new ManageTechnicalDeploymentImpl();
    }

    @Test
    public void testConsultTechnicalDeployment() {

        // test setup
        TechnicalDeploymentRepository technicalDeploymentRepository = Mockito.mock(TechnicalDeploymentRepository.class);
        manageTechnicalDeployment.setTechnicalDeploymentRepository(technicalDeploymentRepository);

        // mock setup
        TechnicalDeployment ld1 = new TechnicalDeployment("td-test1");
        Mockito.when(technicalDeploymentRepository.findOne(1)).thenReturn(ld1);

        // test run
        String technicalDeployment = null;
        try {
            technicalDeployment = manageTechnicalDeployment.findTechnicalDeployment(1);
        } catch (ObjectNotFoundException e) {
            Assert.fail();
        } catch (TechnicalException e) {
            Assert.fail();
        }

        // assertions
        Mockito.verify(technicalDeploymentRepository).findOne(1);
        Assert.assertNotNull(technicalDeployment);
        // Assert.assertEquals("td-test1", technicalDeployment.getName());

    }

    @Test(expected = ObjectNotFoundException.class)
    public void testConsultTechnicalDeploymentThrowsNotFoundException() throws TechnicalException, ObjectNotFoundException {

        // test setup
        TechnicalDeploymentRepository technicalDeploymentRepository = Mockito.mock(TechnicalDeploymentRepository.class);
        manageTechnicalDeployment.setTechnicalDeploymentRepository(technicalDeploymentRepository);

        // mock setup
        Mockito.when(technicalDeploymentRepository.findOne(1)).thenReturn(null);

        // / test run
        manageTechnicalDeployment.findTechnicalDeployment(1);

    }

    @Test
    public void testfindAllTechnicalDeployments() throws ObjectNotFoundException {
        // test setup
        ApplicationReleaseRepository applicationReleaseRepository = Mockito.mock(ApplicationReleaseRepository.class);
        // mock setup
        // Application mock setup
        Application applicationMock = new Application("Application-test-1", "code1");
        ApplicationRelease applicationReleaseMock = new ApplicationRelease(applicationMock, "release");

        Mockito.when(applicationReleaseRepository.find(0)).thenReturn(applicationReleaseMock);

        // environment setup
        TechnicalDeployment tdDev = new TechnicalDeployment("td-dev");
        TechnicalDeployment tdTest = new TechnicalDeployment("td-test");
        TechnicalDeployment tdInt = new TechnicalDeployment("td-int");

        TechnicalDeploymentInstance tdInstanceDev = new TechnicalDeploymentInstance(new TechnicalDeploymentTemplate(tdDev, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE), tdDev);
        TechnicalDeploymentInstance tdInstanceTest = new TechnicalDeploymentInstance(new TechnicalDeploymentTemplate(tdTest, DeploymentProfileEnum.TEST, "releaseId", MiddlewareProfile.DEFAULT_PROFILE), tdTest);
        TechnicalDeploymentInstance tdInstanceInt = new TechnicalDeploymentInstance(new TechnicalDeploymentTemplate(tdInt, DeploymentProfileEnum.PRODUCTION, "releaseId", MiddlewareProfile.DEFAULT_PROFILE), tdInt);

        Environment envDev = new Environment(DeploymentProfileEnum.DEVELOPMENT, "dev", applicationReleaseMock, JOE_DALTON, tdInstanceDev);
        Environment envTest = new Environment(DeploymentProfileEnum.TEST, "test", applicationReleaseMock, JOE_DALTON, tdInstanceTest);
        Environment envInt = new Environment(DeploymentProfileEnum.PRODUCTION, "prod", applicationReleaseMock, JOE_DALTON, tdInstanceInt);

        // environment mock setup
        TechnicalDeploymentInstanceRepository technicalDeploymentInstanceRepository = Mockito.mock(TechnicalDeploymentInstanceRepository.class);
        ReflectionTestUtils.setField(manageTechnicalDeployment, "technicalDeploymentInstanceRepository", technicalDeploymentInstanceRepository);

        List<TechnicalDeploymentInstance> technicalDeploymentInstanceListMockitoAnswer = new ArrayList<TechnicalDeploymentInstance>();
        technicalDeploymentInstanceListMockitoAnswer.add(tdInstanceDev);
        technicalDeploymentInstanceListMockitoAnswer.add(tdInstanceTest);
        technicalDeploymentInstanceListMockitoAnswer.add(tdInstanceInt);

        Mockito.when(technicalDeploymentInstanceRepository.findAll()).thenReturn(technicalDeploymentInstanceListMockitoAnswer);

        // test run
        List<TechnicalDeployment> technicalDeployments = manageTechnicalDeployment.findTechnicalDeployments();
        // assertions
        Assert.assertNotNull(technicalDeployments);
        Assert.assertEquals(3, technicalDeployments.size());
    }

}