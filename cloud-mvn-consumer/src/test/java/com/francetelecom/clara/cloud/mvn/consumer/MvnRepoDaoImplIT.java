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

    public static void assertSpringTestDependencies(List<MavenReference> dependencies) {

        assertEquals(16, dependencies.size());

        List<MavenReference> expectedDependencies = new ArrayList<MavenReference>();

        expectedDependencies.add(MavenReference.fromGavString("org.aspectj:aspectjrt:1.6.1"));
        expectedDependencies.add(MavenReference.fromGavString("org.aspectj:aspectjweaver:1.6.1"));
        expectedDependencies.add(MavenReference.fromGavString("commons-logging:commons-logging:1.1.1"));
        expectedDependencies.add(MavenReference.fromGavString("javax.persistence:persistence-api:1.0"));
        expectedDependencies.add(MavenReference.fromGavString("javax.portlet:portlet-api:1.0"));
        expectedDependencies.add(MavenReference.fromGavString("javax.servlet:jsp-api:2.0"));
        expectedDependencies.add(MavenReference.fromGavString("javax.servlet:servlet-api:2.4"));
        expectedDependencies.add(MavenReference.fromGavString("junit:junit:3.8.1"));
        expectedDependencies.add(MavenReference.fromGavString("taglibs:standard:1.1.2"));
        expectedDependencies.add(MavenReference.fromGavString("org.springframework:spring-beans:2.5.6"));
        expectedDependencies.add(MavenReference.fromGavString("org.springframework:spring-context:2.5.6"));
        expectedDependencies.add(MavenReference.fromGavString("org.springframework:spring-core:2.5.6"));
        expectedDependencies.add(MavenReference.fromGavString("org.springframework:spring-jdbc:2.5.6"));
        expectedDependencies.add(MavenReference.fromGavString("org.springframework:spring-orm:2.5.6"));
        expectedDependencies.add(MavenReference.fromGavString("org.springframework:spring-tx:2.5.6"));
        expectedDependencies.add(MavenReference.fromGavString("org.springframework:spring-webmvc:2.5.6"));

        assertTrue(dependencies.containsAll(expectedDependencies));
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
		MavenReference mavenReference = new MavenReference("com.orange.clara.cloud.samples", "petclinic-sql-postgres", systemTestAppsVersion, "sql");
        File f = mvnRepoDao.getFileFromLocalRepository(mavenReference);
        logger.info("testGeFile - mavenRef:" + mavenReference + " file:" + f);
        assertNotNull(f);
    }

	@Test
	public void should_get_petclinic_sql_using_paas_sample_version_from_local_repository() {
        assertValidMavenRef(mvnRepoDao, systemTestAppsVersion);
	}

	@Test(expected = MavenReferenceResolutionException.class)
	public void should_fail_to_get_an_invalid_maven_ref_from_local() {
		MavenReference mavenReference = new MavenReference("group", "artifact", "version", "ear");
		File f = mvnRepoDao.getFileFromLocalRepository(mavenReference);
		assertNotNull(f);
	}

	@Test
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
