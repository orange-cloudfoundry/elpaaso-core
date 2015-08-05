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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.commons.MavenReference;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class MvnRepoDaoImplIT
{

	private static Logger logger = LoggerFactory.getLogger(MvnRepoDaoImplIT.class.getName());

	@Autowired
	private MvnRepoDao mvnRepoDao;

	@Autowired
	@Qualifier("systemTestAppsVersion")
	private String systemTestAppsVersion;

	@BeforeClass
	public static void setup() throws MalformedURLException {
		logger.info("MvnRepoDaoImplIT setup...");

		File basedir = new File(System.getProperty("basedir", "")).getAbsoluteFile();
		System.setProperty("build.dir", new File(basedir, "target").toURI().toURL().toExternalForm());
	}

	public void should_create_a_maven_ref_representing_Springoo700SnapshotUrl() {
		/*
		 * <dependency>
		 * <groupId>com.francetelecom.clara.prototype.springoojpa</groupId>
		 * <artifactId>springoojpa-ear</artifactId>
		 * <version>7.0.0-SNAPSHOT</version> <classifier>mysql</classifier>
		 * <type>ear</type> </dependency>
		 */

		MavenReference ear = new MavenReference("com.francetelecom.clara.prototype.springoojpa", "springoojpa-ear", "7.0.0-SNAPSHOT", "ear", "mysql");
		assertNotNull(ear);
	}


	static void assertValidMavenRef(MvnRepoDao mvnRepoDao, String systemTestAppsVersion) {
		MavenReference mavenReference = new MavenReference("com.orange.clara.cloud.probes", "out-of-order-probe", systemTestAppsVersion, "jar");
        File f = mvnRepoDao.getFileFromLocalRepository(mavenReference);
        logger.info("testGeFile - mavenRef:" + mavenReference + " file:" + f);
        assertNotNull(f);
    }

	@Test
	public void should_get_out_of_order_probe_using_system_test_apps_version_from_local_repository() {
        assertValidMavenRef(mvnRepoDao, systemTestAppsVersion);
	}

	@Test(expected = MavenReferenceResolutionException.class)
	public void should_fail_to_get_an_invalid_maven_ref_from_local() {
		MavenReference mavenReference = new MavenReference("group", "artifact", "version", "ear");
		File f = mvnRepoDao.getFileFromLocalRepository(mavenReference);
		assertNotNull(f);
	}

	@Test
	@Ignore("no repository available to test deployment")
	public void should_deploy_dummy_TarGz_on_Maven2_and_check_availability_on_paas_proxy() {
		//if this test fail then synchronisation between maven2 and paas doesn't work properly
        MavenReference gav = new MavenReference("com.francetelecom.clara.cloud.dummy", "new-dummy-tar-gz", "1.0-SNAPSHOT", "tar.gz");

		ArrayList<FileRef> fileSet = new ArrayList<FileRef>();
		fileSet.add(new FileRef("install.sh", "<the content of my script file/>"));
		fileSet.add(new FileRef("package.bin", "<the content of my bin file/>"));

		assertNull(gav.getAccessUrl());
		mvnRepoDao.deployFileset(gav, fileSet);
		URL accessUrl = gav.getAccessUrl();
		logger.debug("Access url: " + accessUrl);
		assertNotNull(accessUrl);
		try {
			accessUrl.openStream();
		} catch (IOException e) {
			fail("Invalid URL" + e.getLocalizedMessage());
		}
	}

	@Test
	@Ignore("no repository available to test deployment")
	public void should_deploy_dummy_Rar_on_Maven2_and_check_availability_on_paas_proxy() {
        //if this test fail then synchronisation between maven2 and paas doesn't work properly

		MavenReference gav = new MavenReference("com.francetelecom.clara.cloud.dummy", "dummy-rar", "1.1-SNAPSHOT", "rar");

		ArrayList<FileRef> fileSet = new ArrayList<FileRef>();
		fileSet.add(new FileRef("META-INF/ra.xml", "<the content of my ra xml file/>"));
		fileSet.add(new FileRef("META-INF/jonas-ra.xml", "<the content of my jonas xml file/>"));

		assertNull(gav.getAccessUrl());
		mvnRepoDao.deployFileset(gav, fileSet);
		URL accessUrl = gav.getAccessUrl();
		logger.debug("Access url: " + accessUrl);
		assertNotNull(accessUrl);
		try {
			accessUrl.openStream();
		} catch (IOException e) {
			fail("Invalid URL" + e.getLocalizedMessage());
		}
	}

}
