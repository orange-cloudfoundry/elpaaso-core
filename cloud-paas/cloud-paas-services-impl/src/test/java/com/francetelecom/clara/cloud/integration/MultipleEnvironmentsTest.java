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
package com.francetelecom.clara.cloud.integration;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.scalability.ManageScalability;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto.LinkTypeEnum;

/**
 * Verify some assertions when several environments are created for the same release
 * Last update : $LastChangedDate: 2012-02-10 16:14:56 +0100 (ven., 10 févr.
 * 2012) $ Last author : $Author: ngtz7583 $
 * 
 * @version : $Revision: 19441 $
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class MultipleEnvironmentsTest {

	protected static Logger logger = LoggerFactory.getLogger(MultipleEnvironmentsTest.class.getName());

	@Autowired
	protected ManageScalability manageScalability;

	@Autowired
	protected ManageEnvironment manageEnvironment;

	@Autowired(required=true)
	protected MvnRepoDao mvnRepoDaoMock;

	@Before
	public void setUp() throws MalformedURLException  {

		TestHelper.loginAsAdmin();

		// Mock MvnRepoDao resolveUrl() and getFileFromLocalRepository()
		MavenReference dummyResolvedMavenRef = new MavenReference("com.francetelecom.clara.cloud", "test", "1.0.0","ear");
		dummyResolvedMavenRef.setAccessUrl(new URL("http://maven.redacted-domain.org/test-1.0.0.ear"));
		when(mvnRepoDaoMock.resolveUrl(any (MavenReference.class))).thenReturn(dummyResolvedMavenRef);
		when(mvnRepoDaoMock.getFileFromLocalRepository(any (MavenReference.class))).thenReturn(new File("/tmp/test-1.0.0.ear"));
	}

	@After
	public void cleanSecurityContext() {
		TestHelper.logout();
	}

	/**
	 * Given an application release with 2 environments
	 * Then log links of each environment shall be different
	 * 
	 * Note: the test assumes that all log links of one environment are different
	 */
	@Test
    @Transactional
	public void testTwoEnvOnSameReleaseHaveDistinctLogLinks() throws BusinessException, MalformedURLException, InterruptedException {

		/** 
		 * Given an application release with 2 environments
		 */
		Collection<ApplicationRelease> releases = manageScalability.populate("NNNDDDSSGGG", "testLogLinks", 1, 1, 2);
		ApplicationRelease release = releases.iterator().next();
		List<EnvironmentDto> environementsDto = manageEnvironment.findEnvironmentsByAppRelease(release.getUID());
		assertTrue("We expect to get at least 2 environments; actual is "+environementsDto.size(), environementsDto.size()>=2);
		
		/**
		 * Then log links of each environment shall be different
		 */
		Set<String> verifiedLinks = new HashSet<String>();
		for(EnvironmentDto envDto:environementsDto) {
			// Fetch log links
			EnvironmentDetailsDto envDetailsDto = manageEnvironment.findEnvironmentDetails(envDto.getUid());
			List<LinkDto> links = envDetailsDto.getSpecificLinkDto(LinkTypeEnum.LOGS_LINK);
			// For each log links
			for(LinkDto envLink:links) {
				// verify that the link is not the same as one of the ones of other environments
				// We use the string representation as this is the one which is used by users 
				String linkAsString = envLink.getUrl().toExternalForm();
				logger.info("Environment "+envDto.getUid()+"; checking log link: "+linkAsString);
				assertFalse("The same log link is defined twice (possibly on two different environments): "+linkAsString,verifiedLinks.contains(linkAsString));
				verifiedLinks.add(linkAsString);
			}
		}
	}
}
