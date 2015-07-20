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
package com.francetelecom.clara.cloud.core.infrastructure;

import com.francetelecom.clara.cloud.core.domain.ApplicationReleaseRepository;
import com.francetelecom.clara.cloud.core.domain.ApplicationRepository;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.PaasUserRepository;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.model.*;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/com/francetelecom/clara/cloud/services/application-context.xml"})
public class ApplicationReleaseDaoJpaImplPurgeTest {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationReleaseDaoJpaImplPurgeTest.class);

    @Autowired
    private TechnicalDeploymentTemplateRepository technicalDeploymentTemplateRepository;

    @Autowired
    private EnvironmentRepository environmentRepository;

    @Autowired
    private ApplicationReleaseRepository applicationReleaseRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @Autowired
    private PaasUserRepository paasUserRepository;
    private Application application;
    private PaasUser manager;

    /*
     * @Autowired DataSource dataSource;
     */
    @Before
    @Transactional
    public void setup() throws Exception {
        Assert.assertNotNull(applicationReleaseRepository);
        application = new Application("aLabel", "aCode");
        applicationRepository.persist(application);
        applicationRepository.flush();
        // given bob is a paas user
        manager = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
        paasUserRepository.save(manager);
        paasUserRepository.flush();
    }

    @Test
    @Transactional
    public void should_find_removed_releases_without_environment() throws MalformedURLException {
        // GIVEN
        // release not removed (should not be returned)
        ApplicationRelease arNotRemoved = new ApplicationRelease(application, "G1R0C0");
        arNotRemoved.setVersionControlUrl(new URL("file://url.txt"));
        arNotRemoved.setDescription("arNotRemoved");
        applicationReleaseRepository.persist(arNotRemoved);
        Assume.assumeFalse(arNotRemoved.isRemoved());

        // removed release without environment (should be returned)
        ApplicationRelease arRemovedWithoutEnvironment = new ApplicationRelease(application, "G1R0C0");
        arRemovedWithoutEnvironment.setVersionControlUrl(new URL("file://url.txt"));
        arRemovedWithoutEnvironment.setDescription("arRemovedWithoutEnvironment");
        arRemovedWithoutEnvironment.markAsRemoved();
        applicationReleaseRepository.persist(arRemovedWithoutEnvironment);
        Assume.assumeTrue(arRemovedWithoutEnvironment.isRemoved());

        applicationReleaseRepository.flush();

        // removed release with environment  (should not be returned)
        ApplicationRelease arRemovedWithEnvironment = new ApplicationRelease(application, "G1R0C0");
        arRemovedWithEnvironment.setVersionControlUrl(new URL("file://url.txt"));
        arRemovedWithEnvironment.markAsRemoved();
        applicationReleaseRepository.persist(arRemovedWithEnvironment);
        Assume.assumeTrue(arRemovedWithEnvironment.isRemoved());

        // given td exists
        TechnicalDeployment technicalDeployment = new TechnicalDeployment("foo");
        // given tdt exists
        TechnicalDeploymentTemplate technicalDeploymentTemplate = new TechnicalDeploymentTemplate(technicalDeployment, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
        technicalDeploymentTemplateRepository.save(technicalDeploymentTemplate);
        // given tdi exists
        TechnicalDeploymentInstance technicalDeploymentInstance = new TechnicalDeploymentInstance(technicalDeploymentTemplate, technicalDeployment);
        Environment environment = new Environment(DeploymentProfileEnum.DEVELOPMENT, "intrus", arRemovedWithEnvironment, manager, technicalDeploymentInstance);
        environmentRepository.persist(environment);

        // removed release with removed environment (should not be returned)
        ApplicationRelease arRemovedWithRemovedEnvironment = new ApplicationRelease(application, "G1R0C0");
        arRemovedWithRemovedEnvironment.setVersionControlUrl(new URL("file://url.txt"));
        arRemovedWithRemovedEnvironment.markAsRemoved();
        applicationReleaseRepository.persist(arRemovedWithRemovedEnvironment);
        Assume.assumeTrue(arRemovedWithRemovedEnvironment.isRemoved());

        // given td exists
        TechnicalDeployment technicalDeploymentRm = new TechnicalDeployment("fooRm");
        // given tdt exists
        TechnicalDeploymentTemplate technicalDeploymentTemplateRm = new TechnicalDeploymentTemplate(technicalDeploymentRm, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
        technicalDeploymentTemplateRepository.save(technicalDeploymentTemplateRm);
        // given tdi exists
        TechnicalDeploymentInstance technicalDeploymentInstanceRm = new TechnicalDeploymentInstance(technicalDeploymentTemplateRm, technicalDeploymentRm);
        Environment environmentRm = new Environment(DeploymentProfileEnum.DEVELOPMENT, "intrus", arRemovedWithRemovedEnvironment, manager, technicalDeploymentInstanceRm);
        environment.setStatus(EnvironmentStatus.REMOVED);
        environmentRepository.persist(environmentRm);

        environmentRepository.flush();

        // WHEN
        List<ApplicationRelease> removedReleasesWithoutEnvironment = applicationReleaseRepository.findRemovedReleasesWithoutEnvironment();

        // THEN
        assertThat(removedReleasesWithoutEnvironment).containsExactly(arRemovedWithoutEnvironment);
    }

}
