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
package com.francetelecom.clara.cloud.mvn.consumer.maven;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.ResourceNotFoundException;
import com.francetelecom.clara.cloud.mvn.consumer.FileRef;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class MavenDeployerIT {

	private static final Logger logger = LoggerFactory.getLogger(MavenDeployerIT.class);

	@Autowired
	MavenDeployer deployer;

	@BeforeClass
	public static void setup() throws MalformedURLException {
		File basedir = new File(System.getProperty("basedir", "")).getAbsoluteFile();
		System.setProperty("build.dir", new File(basedir, "target").toURI().toURL().toExternalForm());
	}

	/**
	 * retrieve binary artifact
	 * 
	 * @throws ResourceNotFoundException
	 */
	// @Test
	// public void testRetrieveBinaryArtifact() throws ResourceNotFoundException
	// {
	// MavenReference gav = new MavenReference("log4j", "log4j", "1.2.14");
	// InputStream binary = this.mvnRepoDao.retrieveBinaryArtifact(gav);
	// try {
	// assertTrue(binary != null && binary.available() > 0);
	// } catch (IOException e) {
	// fail(e.getLocalizedMessage());
	// }
	// }

	// @Test
	// public void testRetrieveComplexArtifact() throws
	// ResourceNotFoundException {
	// MavenReference gav = new
	// MavenReference("com.francetelecom.clara.cloud.catalog.product",
	// "cloud-products-software-maven", "2.2.1-SNAPSHOT", "tar.gz");
	// InputStream binary = this.mvnRepoDao.retrieveBinaryArtifact(gav);
	// try {
	// assertTrue(binary != null && binary.available() > 0);
	// } catch (IOException e) {
	// fail(e.getLocalizedMessage());
	// }
	//
	// }

	@Test
	public void should_deploy_dummy_Rar() {
		MavenReference gav = new MavenReference("com.francetelecom.clara.cloud.dummy", "dummy-rar", "1.1-SNAPSHOT", "rar");

		ArrayList<FileRef> fileSet = new ArrayList<FileRef>();
		fileSet.add(new FileRef("META-INF/ra.xml", "<the content of my ra xml file/>"));
		fileSet.add(new FileRef("META-INF/jonas-ra.xml", "<the content of my jonas xml file/>"));

		assertNull(gav.getAccessUrl());
		DeployResult deployResult = this.deployer.deployFileset(gav, fileSet);
		assertNotNull(deployResult);
		int pomCount = 0;
		int rarCount = 0;
		for (Artifact artifact : deployResult.getArtifacts()) {
			assertEquals(gav.getGroupId(), artifact.getGroupId());
			assertEquals(gav.getArtifactId(), artifact.getArtifactId());
			assertFalse(artifact.getVersion().contains("SNAPSHOT"));
			pomCount += artifact.getExtension().equals("pom") ? 1 : 0;
			rarCount += artifact.getExtension().equals("rar") ? 1 : 0;
		}
		assertEquals("Should have deployed one rar file", 1, pomCount);
		assertEquals("Should have deployed one pom file", 1, rarCount);

	}

	@Test
	public void should_deploy_dummy_TarGz() {
		MavenReference gav = new MavenReference("com.francetelecom.clara.cloud.dummy", "new-dummy-tar-gz", "1.0-SNAPSHOT", "tar.gz");

		ArrayList<FileRef> fileSet = new ArrayList<FileRef>();
		fileSet.add(new FileRef("install.sh", "<the content of my script file/>"));
		fileSet.add(new FileRef("package.bin", "<the content of my bin file/>"));

		assertNull(gav.getAccessUrl());
		DeployResult deployResult = this.deployer.deployFileset(gav, fileSet);
		assertNotNull(deployResult);
		int pomCount = 0;
		int rarCount = 0;
		for (Artifact artifact : deployResult.getArtifacts()) {
			assertEquals(gav.getGroupId(), artifact.getGroupId());
			assertEquals(gav.getArtifactId(), artifact.getArtifactId());
			assertFalse(artifact.getVersion().contains("SNAPSHOT"));
			pomCount += artifact.getExtension().equals("pom") ? 1 : 0;
			rarCount += artifact.getExtension().equals("tar.gz") ? 1 : 0;
		}
		assertEquals("Should have deployed one rar file", 1, pomCount);
		assertEquals("Should have deployed one pom file", 1, rarCount);

	}

}
