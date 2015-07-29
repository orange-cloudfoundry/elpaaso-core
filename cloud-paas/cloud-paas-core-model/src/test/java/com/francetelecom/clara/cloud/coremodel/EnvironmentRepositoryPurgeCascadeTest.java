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
package com.francetelecom.clara.cloud.coremodel;

import com.francetelecom.clara.cloud.model.*;
import org.apache.commons.lang3.Validate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/francetelecom/clara/cloud/coremodel/application-context.xml"})
@DirtiesContext(classMode= DirtiesContext.ClassMode.AFTER_CLASS)
public class EnvironmentRepositoryPurgeCascadeTest {

    private static final Logger LOG = LoggerFactory.getLogger(EnvironmentRepositoryTest.class);

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private ApplicationReleaseRepository applicationReleaseRepository;

    @Autowired
    private TechnicalDeploymentTemplateRepository technicalDeploymentTemplateRepository;

    @Autowired
    private TechnicalDeploymentInstanceRepository technicalDeploymentInstanceRepository;

    @Autowired
    private PaasUserRepository paasUserRepository;

    //@Autowired
    //private TechnicalDeploymentCloner tdCloner;

    private PaasUser manager;
    private ApplicationRelease release;
    private TechnicalDeploymentInstance technicalDeploymentInstance;

    /*
     * @Autowired DataSource dataSource;
     */
    @Before
    @Transactional
    public void setup() throws Exception {
        Assert.assertNotNull(environmentRepository);
        // given paas user with ssoId aSsoId exists
        manager = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
        paasUserRepository.save(manager);
        paasUserRepository.flush();
        // given application with label aLabel and code aCode exists
        Application application = new Application("aLabel", "aCode");
        applicationRepository.save(application);
        applicationRepository.flush();
        // given release with version aVersion exists
        release = new ApplicationRelease(application, "aVersion");
        applicationReleaseRepository.save(release);
        applicationReleaseRepository.flush();

        // given td exists
        TechnicalDeployment technicalDeployment = new TechnicalDeployment("foo");
        // given tdt exists
        TechnicalDeploymentTemplate technicalDeploymentTemplate = new TechnicalDeploymentTemplate(technicalDeployment, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
        technicalDeploymentTemplateRepository.save(technicalDeploymentTemplate);
        // given tdi exists
        technicalDeploymentInstance = new TechnicalDeploymentInstance(technicalDeploymentTemplate, technicalDeployment);

    }

    @Test
    @Transactional
    public void should_purge_environments_with_technical_model_items() {
        // GIVEN

        // default environment
        Environment environment = new Environment(DeploymentProfileEnum.PRODUCTION, "default", release, manager, technicalDeploymentInstance);
        environmentRepository.save(environment);

        // removed environment with service
        // TDT
        TechnicalDeployment tdtTd = new TechnicalDeployment("tdtTd");
        TechnicalDeploymentTemplate tdtWithService = new TechnicalDeploymentTemplate(tdtTd, DeploymentProfileEnum.PRODUCTION, release.getUID(), MiddlewareProfile.DEFAULT_PROFILE);

        // XaaS Subscription
        // PlatformServer
        // Directories
        technicalDeploymentTemplateRepository.save(tdtWithService);

        // TD of TDI
        // TechnicalDeployment tdiTd = new TechnicalDeployment("tdiTd");
        //TechnicalDeployment tdClone = tdCloner.deepCopy(tdtTd);
        TechnicalDeploymentInstance tdiWithService = new TechnicalDeploymentInstance(tdtWithService, tdtTd);
        technicalDeploymentInstanceRepository.save(tdiWithService);

        // PlatformServer

        // Directories

        Environment removedEnvironmentWithService = new Environment(DeploymentProfileEnum.PRODUCTION, "removedEnvironmentWithService", release, manager, tdiWithService);
        removedEnvironmentWithService.setStatus(EnvironmentStatus.REMOVED);
        environmentRepository.save(removedEnvironmentWithService);

        environmentRepository.flush();
        Validate.notNull(environmentRepository.findOne(removedEnvironmentWithService.getId()));

        List<Environment> environmentsToPurge = Arrays.asList(environment, removedEnvironmentWithService);

        // ///////////////////////////////
        // WHEN IS RIGHT HERE
        // ///////////////////////////////
        environmentRepository.delete(environmentsToPurge);
        // ///////////////////////////////
        environmentRepository.flush();
        LOG.info("after remove");

        // THEN

        // env that should be retrieved by id
        assertThat(environmentRepository.findOne(environment.getId())).as("not removed env should should exist").isEqualTo(null);

        // check one dependent item of removed env
        TechnicalDeploymentTemplate shouldBeKeepedTDT = technicalDeploymentTemplateRepository.findOne(tdtWithService.getId());
        assertThat(shouldBeKeepedTDT).as("purged environment associated TDT should not have been purged").isEqualTo(tdtWithService);

        // env that should not be retrieved by id
        assertThat(environmentRepository.findOne(removedEnvironmentWithService.getId())).as("removed env with service should not exist").isNull();

        // assert database state (using findAll)
        List<Environment> keepedEnvironment = environmentRepository.findAll();
        assertThat(keepedEnvironment).isEmpty();

        // method return
        TechnicalDeploymentInstance shouldBeRemovedTDI = technicalDeploymentInstanceRepository.findOne(tdiWithService.getId());
        assertThat(shouldBeRemovedTDI).as("purged environment associated TDI should not have been purged").isNull();

        // check directories component

        // check platform server

        environmentRepository.flush();
    }

}
