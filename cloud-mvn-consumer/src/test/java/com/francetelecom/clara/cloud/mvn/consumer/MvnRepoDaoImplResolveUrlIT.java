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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.Assert;
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

import com.francetelecom.clara.cloud.commons.MavenReference;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class MvnRepoDaoImplResolveUrlIT {

	public static final String PROBES_GROUPID = "com.orange.clara.cloud.probes";
	private static Logger logger = LoggerFactory.getLogger(MvnRepoDaoImplResolveUrlIT.class.getName());



	@Autowired
	private MvnRepoDao mvnRepoDao;

	@Autowired
	@Qualifier("systemTestAppsVersion")
	private String systemTestAppsVersion;

	@Autowired
	@Qualifier("elpaasoVersion")
	private String elpaasoVersion;

	@BeforeClass
	public static void setup() throws MalformedURLException {
		// Ignore https certificate
		XTrustProvider.install();

		File basedir = new File(System.getProperty("basedir", "")).getAbsoluteFile();
		System.setProperty("build.dir", new File(basedir, "target").toURI().toURL().toExternalForm());
	}

	@Test
	public void should_resolve_war_for_cf_wicketoo_jpa_deployed_on_internal_repo() {
		MavenReference mavenRef0 = new MavenReference("com.orange.clara.cloud.samples.cf", "cf-wicket-jpa-war", "1.1.4", "war");
		MavenReference mavenRef1 = mvnRepoDao.resolveUrl(mavenRef0);

		assertAccessUrlIsValid(mavenRef1);
	}

	@Test(expected = MavenReferenceResolutionException.class)
	public void should_fail_to_resolve_an_invalid_maven_ref() {
		MavenReference mavenRef0 = new MavenReference("group", "artifact", "version", "ear");
		mvnRepoDao.resolveUrl(mavenRef0);
	}

	@Test
	public void should_resolve_paas_probe_simple_jar_with_systemTestAppsVersion() {
		testResolveUrl(new MavenReference(PROBES_GROUPID, "paas-probe-simple", systemTestAppsVersion, "jar"));
	}


	@Test
	public void should_resolve_ear_artifact_for_jeeprobe_latest_snapshot() {
		MavenReference ear = new MavenReference(PROBES_GROUPID, "paas-probe-jee-ear", "LATEST", "ear");

		MavenReference resolvedUrl = resolveUrlAndAssertResultNotNull(ear);
		assertTrue(ear.getArtifactId() + " snapshot reference :" + resolvedUrl.getAccessUrl().getPath() + " should contain -SNAPSHOT in url",
				resolvedUrl.getAccessUrl().getPath().contains("-SNAPSHOT"));

	}


	@Test
	public void should_resolve_war_artifact_for_elpaaso_latest_snapshot() {
		MavenReference ear = new MavenReference("com.orange.clara.cloud", "cloud-paas-webapp-war", "LATEST", "war");
		MavenReference resolvedUrl = resolveUrlAndAssertResultNotNull(ear);
		assertTrue("Should contain elpaaso snapshot reference :" + resolvedUrl.getAccessUrl().getPath(),
				resolvedUrl.getAccessUrl().getPath().contains(elpaasoVersion));
	}

	@Test
	public void should_resolve_war_artifact_for_elpaaso_latest_release() {
		MavenReference ear = new MavenReference("com.orange.clara.cloud", "cloud-paas-webapp-war", "RELEASE", "war");
		MavenReference resolvedUrl = resolveUrlAndAssertResultNotNull(ear);
		assertFalse("Elpaaso released artifact should not be snapshot : " + resolvedUrl.getAccessUrl().getPath(), resolvedUrl.getAccessUrl().getPath()
				.contains("-SNAPSHOT"));
	}


	private void assertResolveUrlIsValid(MavenReference mvnBefore) {
		MavenReference mvnAfter = this.mvnRepoDao.resolveUrl(mvnBefore);
		assertNotNull(mvnAfter.getAccessUrl());
		logger.info("reference resolue sur le repo " + mvnAfter + " sur URL " + mvnAfter.getAccessUrl());
		checkDownloadIsPossible(mvnAfter);
	}

	private void checkDownloadIsPossible(MavenReference mvnAfter) {
		InputStream data = null;
		try {
			URLConnection connection = mvnAfter.getAccessUrl().openConnection();
			data = connection.getInputStream();
			assertTrue(data.available() > 0);
		} catch (IOException e) {
			fail(e.getLocalizedMessage() + " - " + e.toString());
		} finally {
			if (data != null) {
				try {
					data.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * utility method to test and assert resolve url
	 * 
	 * @param unresolvedReference
	 */
	private void testResolveUrl(MavenReference unresolvedReference) {
		MavenReference mvnBefore = unresolvedReference;
		assertResolveUrlIsValid(mvnBefore);
	}



	private MavenReference resolveUrlAndAssertResultNotNull(MavenReference mavenReference) {
		assertNotNull(mavenReference);
		MavenReference resolvedUrl = mvnRepoDao.resolveUrl(mavenReference);
		assertNotNull(resolvedUrl);
		return resolvedUrl;
	}

	private void assertAccessUrlIsValid(MavenReference mavenRef1) {
		URL url = mavenRef1.getAccessUrl();
		Assert.assertNotNull("access url is null for " + mavenRef1, url);
		Assert.assertTrue("access url is not valid for " + mavenRef1 + " url = " + url, isValidUrl(url));
	}

	private boolean isValidUrl(URL url) {
		HttpURLConnection huc;
		int responseCode = 0;
		try {
			huc = (HttpURLConnection) url.openConnection();
			huc.setRequestMethod("GET");
			huc.setReadTimeout(5000);
			huc.connect();
			responseCode = huc.getResponseCode();
		} catch (IOException e) {
			logger.error("unable to test url " + url, e);
		}

		return responseCode == 200;
	}

}