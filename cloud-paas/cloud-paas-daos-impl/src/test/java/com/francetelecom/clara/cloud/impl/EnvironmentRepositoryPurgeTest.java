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
package com.francetelecom.clara.cloud.impl;

import com.francetelecom.clara.cloud.commons.DateHelper;
import com.francetelecom.clara.cloud.core.domain.ApplicationReleaseRepository;
import com.francetelecom.clara.cloud.coremodel.ApplicationRepository;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.coremodel.PaasUserRepository;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.model.*;
import org.apache.commons.lang3.time.DateUtils;
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

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/services/application-context.xml" })
public class EnvironmentRepositoryPurgeTest {

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
    private PaasUserRepository paasUserRepository;

    private ApplicationRelease release;
    private TechnicalDeploymentInstance technicalDeploymentInstance;

	private PaasUser bob;

    /*
     * @Autowired DataSource dataSource;
     */
    @Before
    @Transactional
    public void setup() throws Exception {
        Assert.assertNotNull(environmentRepository);
        // given paas user with ssoId aSsoId exists
        bob = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
        paasUserRepository.save(bob);
        paasUserRepository.flush();
        // given application with label aLabel and code aCode exists
        Application application = new Application("aLabel", "aCode");
        applicationRepository.save(application);
        applicationRepository.flush();
        // given release with version aVersion exists
        release = new ApplicationRelease(application, "aVersion");
        applicationReleaseRepository.persist(release);
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
    public void should_find_older_removed_environments() {
        // GIVEN
        int fiveDay = 5;

        Date jMinusFourDayAndTwentyThreeHours = DateHelper.getDateDeltaDay(-4);
        jMinusFourDayAndTwentyThreeHours = DateUtils.addHours(jMinusFourDayAndTwentyThreeHours, -23);

        Date jMinusFiveDayAndOneSecond = DateHelper.getDateDeltaDay(-5);
        jMinusFiveDayAndOneSecond = DateUtils.addSeconds(jMinusFiveDayAndOneSecond, -1);
        Date jMinusHeight = DateHelper.getDateDeltaDay(-8);

        // default environment
		Environment environment = new Environment(DeploymentProfileEnum.PRODUCTION, "default", release, bob,technicalDeploymentInstance);
        environmentRepository.persist(environment);
        LOG.info("test env {}", environment);

        // default old environment
        DateHelper.setNowDate(jMinusFiveDayAndOneSecond);
        Environment environmentOld = new Environment(DeploymentProfileEnum.PRODUCTION, "old", release, bob,technicalDeploymentInstance);
        Assume.assumeTrue(environmentOld.getCreationDate().equals(jMinusFiveDayAndOneSecond));
        DateHelper.resetNow();
        environmentRepository.persist(environmentOld);
        LOG.info("test env {}", environmentOld);

        // given default removed environment
        DateHelper.setNowDate(jMinusFourDayAndTwentyThreeHours);
        Environment removed = new Environment(DeploymentProfileEnum.PRODUCTION, "defaultRemoved", release, bob,technicalDeploymentInstance);
        removed.setStatus(EnvironmentStatus.REMOVED);
        DateHelper.resetNow();
        environmentRepository.persist(removed);
        LOG.info("test env {}", removed);

        // given older removed environment A (j-6)
        DateHelper.setNowDate(jMinusFiveDayAndOneSecond);
        Environment oldRemovedA = new Environment(DeploymentProfileEnum.PRODUCTION, "oldRemovedA", release, bob,technicalDeploymentInstance);
        oldRemovedA.setStatus(EnvironmentStatus.REMOVED);
        DateHelper.resetNow();
        Assume.assumeTrue(oldRemovedA.getDeletionDate().equals(jMinusFiveDayAndOneSecond));
        environmentRepository.persist(oldRemovedA);
        LOG.info("test env {}", oldRemovedA);

        // given older removed environment B  (j-8)
        DateHelper.setNowDate(jMinusHeight);
        Environment oldRemovedB = new Environment(DeploymentProfileEnum.PRODUCTION, "oldRemovedB", release, bob,technicalDeploymentInstance);
        oldRemovedB.setStatus(EnvironmentStatus.REMOVED);
        DateHelper.resetNow();
        Assume.assumeTrue(oldRemovedB.getDeletionDate().equals(jMinusHeight));
        environmentRepository.persist(oldRemovedB);
        LOG.info("test env {}", oldRemovedA);

        // WHEN
        List<Environment> results = environmentRepository.findRemovedOlderThanNDays(fiveDay);

        // THEN
        assertThat(results).containsExactly(oldRemovedA, oldRemovedB);
    }


    @Test
    @Transactional
    public void retention_delay_should_prevent_to_find_older_environments_recently_removed() {
        // GIVEN
        int retentionDelayIsFiveDay = 5;

        Date jMinusFiveDayAndOneSecond = DateHelper.getDateDeltaDay(-5);
        jMinusFiveDayAndOneSecond = DateUtils.addSeconds(jMinusFiveDayAndOneSecond, -1);

        // oldEnv but recently removed
        DateHelper.setNowDate(jMinusFiveDayAndOneSecond);
		Environment oldEnv = new Environment(DeploymentProfileEnum.PRODUCTION, "default", release, bob,technicalDeploymentInstance);
        Assume.assumeTrue(oldEnv.getCreationDate().equals(jMinusFiveDayAndOneSecond));
        DateHelper.resetNow();
        Date removedDate = new Date();
        DateHelper.setNowDate(removedDate );
        oldEnv.setStatus(EnvironmentStatus.REMOVED);
        DateHelper.resetNow();
        Assume.assumeTrue(oldEnv.getDeletionDate().equals(removedDate));
        environmentRepository.persist(oldEnv);
        LOG.info("test env {}", oldEnv);

        // WHEN
        List<Environment> results = environmentRepository.findRemovedOlderThanNDays(retentionDelayIsFiveDay);

        // THEN
        assertThat(results).isEmpty();
    }

    @Test
    @Transactional
    public void should_purge_environments() {
        // GIVEN

    	// default environment
		Environment environment = new Environment(DeploymentProfileEnum.PRODUCTION, "default", release, bob,technicalDeploymentInstance);
        environmentRepository.persist(environment);

        // removed environment
        Environment removedEnvironment = new Environment(DeploymentProfileEnum.PRODUCTION, "removedEnvironment", release, bob,technicalDeploymentInstance);
        removedEnvironment.setStatus(EnvironmentStatus.REMOVED);
        environmentRepository.persist(removedEnvironment);

        List <Environment> environmentsToPurge = Arrays.asList(environment, removedEnvironment);
        int envCount = environmentsToPurge.size();

        // WHEN
        LOG.info("purge {} environment(s)", envCount);
        int nbRemoved = environmentRepository.purgeEnvironments(environmentsToPurge);

        // THEN

        // env that should be retrieved by id
        assertThat(environmentRepository.find(environment.getId()))
                .as("not removed env should should exist").isEqualTo(environment);
        // env that should not be retrieved by id
        assertThat(environmentRepository.find(removedEnvironment.getId()))
                .as("removed env should not exist").isNull();

        // assert database state (using findAll)
        List<Environment> keepedEnvironment = environmentRepository.findAll();
        LOG.info("after purge env count: {}", keepedEnvironment.size());
        assertThat(keepedEnvironment).as("not removed env must not be purged").contains(environment);
        assertThat(keepedEnvironment).as("removed env should have been purged").containsExactly(environment);

        // method return
        assertThat(nbRemoved).as("purge count result is wrong").isEqualTo(1);

        environmentRepository.flush();
    }


}
