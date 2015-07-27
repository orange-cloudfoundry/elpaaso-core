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
package com.francetelecom.clara.cloud.mvn.consumer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.net.MalformedURLException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * MvnRepoDaoImplUsingProxyIT
 * 
 * GIVEN a wrong http.proxyHost | http.proxyPort / a correct http.nonProxyHosts
 * "localhost|*.redacted-domain.org" WHEN maven resolution (ref and or dep..) THEN
 * maven resolution is a success
 * 
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class MvnRepoDaoImplUsingWrongProxyIT {

	private static Logger logger = LoggerFactory.getLogger(MvnRepoDaoImplIT.class.getName());

	@Autowired
	private MvnRepoDaoImpl mvnRepoDao;

	@Autowired
	@Qualifier("systemTestAppsVersion")
	private String systemTestAppsVersion;

	@BeforeClass
	public static void setup() throws MalformedURLException {
		logger.info("MvnRepoDaoImplUsingWrongProxyIT setup");

		File basedir = new File(System.getProperty("basedir", "")).getAbsoluteFile();
		System.setProperty("build.dir", new File(basedir, "target").toURI().toURL().toExternalForm());
	}

	/**

     */
	@Test
	public void testGetFileFromRemoteRepositoryUsingProxy_ValidMavenRef() {
		assertTrue("Should use a proxy for pull repo", mvnRepoDao.isUsingProxyForPullRepo());
		MvnRepoDaoImplIT.assertValidMavenRef(mvnRepoDao, systemTestAppsVersion);
	}

}
