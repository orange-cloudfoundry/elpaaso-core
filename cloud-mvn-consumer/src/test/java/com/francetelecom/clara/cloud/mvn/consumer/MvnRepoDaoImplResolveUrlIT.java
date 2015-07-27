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
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.commons.Constants;
import com.francetelecom.clara.cloud.commons.MavenReference;

@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class MvnRepoDaoImplResolveUrlIT {

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
	public void should_resolve_ear_for_wicketoo_7_0_1() {
		MavenReference mavenRef0 = new MavenReference("com.francetelecom.clara.prototype.wicketoo", "wicketoo-ear", "7.0.1", "ear");
		MavenReference mavenRef1 = mvnRepoDao.resolveUrl(mavenRef0);

		assertAccessUrlIsValid(mavenRef1);
	}


	@Test
	public void should_resolve_sql_for_prototype_postgres_7_0_1() {
		MavenReference mavenRef0 = new MavenReference("com.francetelecom.clara.prototype.commons", "commons-sql-postgresql", "7.0.1", "sql");
		MavenReference mavenRef1 = mvnRepoDao.resolveUrl(mavenRef0);

		assertAccessUrlIsValid(mavenRef1);
	}

	@Test
	public void should_resolve_jar_for_bnd_mysql_connector_java_5_1_30() {
		MavenReference driver = new MavenReference(Constants.mvnRefMysqlDriver);
		MavenReference mavenRef1 = mvnRepoDao.resolveUrl(driver);

		assertAccessUrlIsValid(mavenRef1);
	}

	@Test
	public void should_resolve_jar_for_bnd_postgresql_9_1_901() {
		MavenReference driver = new MavenReference(Constants.mvnRefPostgresqlDriver);
		MavenReference mavenRef1 = mvnRepoDao.resolveUrl(driver);

		assertAccessUrlIsValid(mavenRef1);
	}


	@Test(expected = MavenReferenceResolutionException.class)
	public void should_fail_to_resolve_an_invalid_maven_ref() {
		MavenReference mavenRef0 = new MavenReference("group", "artifact", "version", "ear");
		mvnRepoDao.resolveUrl(mavenRef0);
	}


	@Test
	public void should_resolve_rar_for_prototype_xa_snapshot() {

		MavenReference mvnBefore = new MavenReference("com.francetelecom.clara.prototype.commons", "commons-rar-jonas-postgresql-xa", "7.0.2-SNAPSHOT", "rar");

		assertResolveUrlIsValid(mvnBefore);
	}

	/**
	 * This test validates fix for bug #83336 (i.e. publication of SQL scripts
	 * using the build helper).
	 */
	@Test
	public void should_resolve_sql_for_petclinic_release() {
		/*
		 * <dependency> <groupId>com.orange.clara.cloud.samples</groupId>
		 * <artifactId>petclinic-springoo-sql-postgres</artifactId>
		 * <version>1.0.5</version> <type>sql</type> </dependency>
		 */
		MavenReference mvnBefore = new MavenReference("com.orange.clara.cloud.samples", "petclinic-springoo-sql-postgres", systemTestAppsVersion, "sql");

		assertResolveUrlIsValid(mvnBefore);
	}

	// @Ignore("Don't work with local repo")
	@Test
	public void should_resolve_jar_for_prototype_release() {
		MavenReference mvnBefore = new MavenReference("com.francetelecom.clara.prototype.commons", "commons-utils", "7.0.1", "jar");

		assertResolveUrlIsValid(mvnBefore);
	}

	@Test
	@Ignore("No more tar.gz used directly as dependencies")
	public void should_resolveUrl_targz_for_cloud_products_software_mysql_5_0_0_SNAPSHOT() {
		MavenReference mysqlReference = new MavenReference("com.francetelecom.clara.cloud.catalog.product", "cloud-products-software-mysql", "5.0.0-SNAPSHOT",
				"tar.gz");
		MavenReference updatedMysqlReference = this.mvnRepoDao.resolveUrl(mysqlReference);
		Assert.assertNotNull(updatedMysqlReference.getAccessUrl());
	}

	@Test
	@Ignore("No more tar.gz used directly as dependencies")
	public void should_resolve_targz_for_cloud_products_software_apache_2_2_3_SNAPSHOT() {
		testResolveUrl(new MavenReference("com.francetelecom.clara.cloud.catalog.product", "cloud-products-software-apache", "2.2.3-SNAPSHOT", "tar.gz"));
	}

	@Test
	public void should_resolve_sql_url_for_petClinic_paas_sample_version_sql() {
		/*
		 * 
		 * <dependency> <groupId>com.orange.clara.cloud.samples</groupId>
		 * <artifactId>petclinic-sql-postgres</artifactId>
		 * <version>1.0.0</version> <type>sql</type> </dependency>
		 */
		testResolveUrl(new MavenReference("com.orange.clara.cloud.samples", "petclinic-sql-postgres", systemTestAppsVersion, "sql"));
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

	@Test
	public void should_resolve_ear_artifact_for_wicketoo_latest_Snapshot() {
		/*
		 * <dependency>
		 * <groupId>com.francetelecom.clara.prototype.wicketoo</groupId>
		 * <artifactId>wicketoo-ear</artifactId> <version>LATEST</version>
		 * <type>ear</type> </dependency>
		 */

		MavenReference ear = new MavenReference("com.francetelecom.clara.prototype.wicketoo", "wicketoo-ear", "7.0.2-SNAPSHOT", "ear");
		assertNotNull(ear);
		MavenReference resolvedUrl = mvnRepoDao.resolveUrl(ear);
		assertNotNull(resolvedUrl);
		assertTrue(resolvedUrl.getVersion().endsWith("-SNAPSHOT"));

	}

	@Test
	public void should_resolve_war_artifact_for_elpaaso_latest_Snapshot() {
		/*
		 * <dependency> <groupId>com.orange.clara.cloud.samples</groupId>
		 * <artifactId>cloud-paas-webapp-ear</artifactId>
		 * <version>LATEST</version> <type>ear</type> </dependency>
		 */

		MavenReference ear = new MavenReference("com.orange.clara.cloud", "cloud-paas-webapp-war", "LATEST", "war");
		assertNotNull(ear);
		MavenReference resolvedUrl = mvnRepoDao.resolveUrl(ear);
		assertNotNull(resolvedUrl);
		assertTrue("Should contain elpaaso snapshot reference :" + resolvedUrl.getAccessUrl().getPath(),
				resolvedUrl.getAccessUrl().getPath().contains(elpaasoVersion));
	}

	@Test
	public void should_resolve_ear_artifact_for_elpaaso_latest_release() {
		/*
		 * <dependency> <groupId>com.orange.clara.cloud</groupId>
		 * <artifactId>cloud-paas-webapp-ear</artifactId>
		 * <version>LATEST</version> <type>ear</type> </dependency>
		 */

		MavenReference ear = new MavenReference("com.orange.clara.cloud", "cloud-paas-webapp-ear", "RELEASE", "ear");
		assertNotNull(ear);
		MavenReference resolvedUrl = mvnRepoDao.resolveUrl(ear);
		assertNotNull(resolvedUrl);
		assertFalse("Elpaaso released artifact should not be snapshot : " + resolvedUrl.getAccessUrl().getPath(), resolvedUrl.getAccessUrl().getPath()
				.contains("-SNAPSHOT"));
	}

	@Test
	public void should_resolve_sql_url_for_protoypes_postgres_latest_snapshot() {
		// <dependency>
		// <groupId>com.francetelecom.clara.prototype.commons</groupId>
		// <artifactId>commons-sql-postgresql</artifactId>
		// <version>7.0.2-SNAPSHOT</version>
		// <type>sql</type>
		// </dependency>
		MavenReference sql = new MavenReference("com.francetelecom.clara.prototype.commons", "commons-sql-postgresql", "LATEST", "sql");
		assertNotNull(sql);
		assertResolveUrlIsValid(sql);
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