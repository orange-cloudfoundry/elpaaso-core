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
import com.francetelecom.clara.cloud.coremodel.PaasUserRepository;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import org.junit.Assert;
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
import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/services/application-context.xml" })
public class ApplicationDaoJpaImplPurgeTest {

	private static final Logger LOG = LoggerFactory.getLogger(ApplicationDaoJpaImplPurgeTest.class);

	@Autowired
	private ApplicationRepository applicationRepository;

	@Autowired
	private ApplicationReleaseRepository applicationReleaseRepository;

	@Autowired
	private PaasUserRepository paasUserRepository;
	private PaasUser manager;

	@Before
	@Transactional
	public void setup() throws Exception {
		Assert.assertNotNull(applicationRepository);
		// given bob is a paas user
		manager = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
		paasUserRepository.save(manager);
		paasUserRepository.flush();
	}

	@Test
	@Transactional
	public void should_find_removed_applications_without_release() throws MalformedURLException {
		// GIVEN
		// application not removed (should not be returned)
		Application appNotRemoved = new Application("appNotRemoved", "CODE");
		applicationRepository.persist(appNotRemoved);
		assertThat(appNotRemoved.isRemoved()).isFalse();

		// removed application without application release (should be returned)
		Application applicationWithoutRelease = new Application("appWithoutAR", "CODE2");
		applicationWithoutRelease.markAsRemoved();
		applicationRepository.persist(applicationWithoutRelease);
		assertThat(applicationWithoutRelease.isRemoved()).isTrue();

		applicationRepository.flush();

		// removed application with release (should not be returned)
		Application applicationWithRelease = new Application("appWithAR", "CODE3");
		applicationRepository.persist(applicationWithRelease);

		ApplicationRelease appRelease = new ApplicationRelease(applicationWithRelease, "G1R0C0");
		appRelease.setVersionControlUrl(new URL("file://url.txt"));
		appRelease.markAsRemoved();
		applicationReleaseRepository.persist(appRelease);
		assertThat(appRelease.isRemoved());

		applicationWithRelease.markAsRemoved();
		applicationRepository.merge(applicationWithRelease);
		assertThat(applicationWithRelease.isRemoved()).isTrue();

		// WHEN
		List<Application> removedApplicationsWithoutRelease = applicationRepository.findRemovedApplicationWithoutRelease();

		// THEN
		assertThat(removedApplicationsWithoutRelease).containsExactly(applicationWithoutRelease);
	}

	@Test
	@Transactional
	public void should_purge_only_removed_application() {
		// GIVEN
		// application not removed (should not be purged)
		Application appNotRemoved = new Application("appNotRemoved", "CODE4");
		applicationRepository.persist(appNotRemoved);
		assertThat(appNotRemoved.isRemoved()).isFalse();

		applicationRepository.flush();

		List<Application> applicationsToPurge = Arrays.asList(appNotRemoved);
		int applicationsCount = applicationsToPurge.size();

		// ~ WHEN
		LOG.info("would like to purge {} applications(s)", applicationsCount);
		int nbRemoved = applicationRepository.purgeApplications(applicationsToPurge);
		LOG.info("had purge {} applications(s)", nbRemoved);

		// THEN
		// release that should be retrieved by id
		assertThat(applicationRepository.find(appNotRemoved.getId())).as("not removed application release should exist").isEqualTo(appNotRemoved);
		// check return
		assertThat(nbRemoved).isEqualTo(0);
	}

	@Test
	@Transactional
	public void should_not_purge_removed_application_with_related_release() throws MalformedURLException {
		// GIVEN
		// removed application with release (should not be purged)
		Application applicationWithRelease = new Application("appWithRelease", "CODE3");
		applicationRepository.persist(applicationWithRelease);

		ApplicationRelease arRemoved = new ApplicationRelease(applicationWithRelease, "G1R0C0");
		arRemoved.setVersionControlUrl(new URL("file://url.txt"));
		arRemoved.markAsRemoved();
		applicationReleaseRepository.persist(arRemoved);
		assertThat(arRemoved.isRemoved());
		applicationReleaseRepository.flush();

		applicationWithRelease.markAsRemoved();
		applicationRepository.merge(applicationWithRelease);
		assertThat(applicationWithRelease.isRemoved()).isTrue();

		List<Application> applicationsToPurge = Arrays.asList(applicationWithRelease);
		int applicationsCount = applicationsToPurge.size();

		// ~ WHEN
		LOG.info("would like to purge {} applications(s)", applicationsCount);
		int nbRemoved = applicationRepository.purgeApplications(applicationsToPurge);
		LOG.info("had purge {} applications(s)", nbRemoved);

		// THEN
		// app that should be retrieved by id
		assertThat(applicationRepository.find(applicationWithRelease.getId())).as("application should not have been purged (because of release presence)")
				.isNotNull();
		// check return
		assertThat(nbRemoved).isEqualTo(0);
		// although release is not visible as it is marked removed
		List<ApplicationRelease> remainingReleases = applicationReleaseRepository.findAll();
		assertThat(remainingReleases).as("application related release must not be visible").isEmpty();
	}

	@Test
	@Transactional
	public void should_purge_removed_application_without_related_release() throws MalformedURLException {
		// ~ GIVEN
		// application : removed application without release (should be purged)
		Application applicationWithoutRelease = new Application("appWithoutRelease", "CODE3");
		applicationWithoutRelease.markAsRemoved();
		applicationRepository.persist(applicationWithoutRelease);
		assertThat(applicationWithoutRelease.isRemoved()).isTrue();

		List<Application> applicationsToPurge = Arrays.asList(applicationWithoutRelease);
		int applicationsCount = applicationsToPurge.size();

		// ~ WHEN
		LOG.info("would like to purge {} applications(s)", applicationsCount);
		int nbRemoved = applicationRepository.purgeApplications(applicationsToPurge);
		LOG.info("had purge {} applications(s)", nbRemoved);

		// ~ THEN

		// release that should be retrieved by id
		assertThat(applicationRepository.find(applicationWithoutRelease.getId())).as("application should have been purged").isNull();

		// check return
		assertThat(nbRemoved).isEqualTo(1);
	}
}
