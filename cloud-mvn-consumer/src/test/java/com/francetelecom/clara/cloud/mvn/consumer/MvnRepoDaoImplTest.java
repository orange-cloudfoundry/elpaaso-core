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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.francetelecom.clara.cloud.commons.MavenReference;

public class MvnRepoDaoImplTest {

	// SUT
	MvnRepoDaoImpl mvnRepoDao;

	@Before
	public void setup() {

		// Mockito is used to spy a MavenRepoDao
		// As we don't want to actually call Aether API we mock some method
		// calls

		this.mvnRepoDao = spy(new MvnRepoDaoImpl());
		doNothing().when(mvnRepoDao).updateUrl(any(MavenReference.class));
		doReturn(true).when(mvnRepoDao).isArtifactAvailable(any(MavenReference.class));

	}

	@Test
	public void should_resolveUrl_for_elpaaso_snapshot_war() {
		// test data
		MavenReference mavenRef = new MavenReference("com.francetelecom.clara.cloud", "cloud-paas-webapp-war", "0.0.5-SNAPSHOT", "war");
		// exercise sut
		mvnRepoDao.resolveUrl(mavenRef);
		// check method call
		verify(mvnRepoDao).updateUrl(mavenRef);
	}

	@Test
	public void should_resolveUrl_for_elpaaso_release_war() {
		// test data
		MavenReference mavenRef = new MavenReference("com.francetelecom.clara.cloud", "cloud-paas-webapp-war", "0.0.5", "war");
		// exercise sut
		mvnRepoDao.resolveUrl(mavenRef);
		// check method call
		verify(mvnRepoDao).updateUrl(mavenRef);
	}

	@Test
	public void should_resolveUrl_for_standart_release_jar() {
		// test data
		MavenReference mavenRef = new MavenReference(new MavenReference("mysql", "bnd-mysql-connector-java", "5.1.14", "jar"));
		// exercise sut
		mvnRepoDao.resolveUrl(mavenRef);
		// check method call
		verify(mvnRepoDao).updateUrl(mavenRef);

	}


	@Test
	public void testResolveUrl_always_checks_that_resolved_url_points_to_an_available_artifact() {
		// test data
		MavenReference mavenRef = new MavenReference(new MavenReference("groupId", "artifactId", "1.0", "ear"));
		// exercise sut
		mvnRepoDao.resolveUrl(mavenRef);
		// check method call
		verify(mvnRepoDao).isArtifactAvailable(mavenRef);
	}

	@Test(expected=MavenReferenceResolutionException.class)
	public void testResolveUrl_fails_when_resolved_url_does_not_point_to_an_available_artifact() {
		// stub isArtifactAvailable to return false 
		doReturn(false).when(mvnRepoDao).isArtifactAvailable(any(MavenReference.class));

		// test data
		MavenReference mavenRef = new MavenReference(new MavenReference("groupId", "artifactId", "1.0", "ear"));
		
		// exercise sut
		mvnRepoDao.resolveUrl(mavenRef);
	}
	
	@Test
	public void testIsArtifactAvailable_returns_false_when_maven_reference_url_is_not_resolved() {
		// ensure nothing is stubbed
		reset(mvnRepoDao);
		// test data: an unresolved maven reference
		MavenReference mavenRef = new MavenReference(new MavenReference("groupId", "artifactId", "1.0", "ear"));
		// run test
		boolean isAvailable = mvnRepoDao.isArtifactAvailable(mavenRef);
		// assertions
		assertFalse(isAvailable);
	}

	@Test
	public void testIsArtifactAvailable_returns_false_when_maven_reference_url_can_not_be_reached() throws MalformedURLException {
		// ensure nothing is stubbed
		reset(mvnRepoDao);
		// test data
		MavenReference mavenRef = new MavenReference(new MavenReference("groupId", "artifactId", "1.0", "ear"));
		mavenRef.setAccessUrl(new URL("http://myrepo/myartifact"));
		// stub isValidurl to return false
		doReturn(false).when(mvnRepoDao).isValidUrl(any(URL.class));
		// run test
		boolean isAvailable = mvnRepoDao.isArtifactAvailable(mavenRef);
		// assertions
		assertFalse(isAvailable);
	}

	@Test
	public void testIsArtifactAvailable_returns_true_when_maven_reference_url_can_be_reached() throws MalformedURLException {
		// ensure nothing is stubbed
		reset(mvnRepoDao);
		// test data
		MavenReference mavenRef = new MavenReference(new MavenReference("groupId", "artifactId", "1.0", "ear"));
		mavenRef.setAccessUrl(new URL("http://myrepo/myartifact"));
		// stub isValidurl to return false
		doReturn(true).when(mvnRepoDao).isValidUrl(any(URL.class));
		// run test
		boolean isAvailable = mvnRepoDao.isArtifactAvailable(mavenRef);
		// assertions
		assertTrue(isAvailable);
	}
	
	/**
	 * When verifying an artifact url we need to set a long enough timeout to be protected againt slow network
	 */
	@Test
	public void isValidUrl_uses_a_long_read_timeout() throws IOException {
		// ensure nothing is stubbed
		reset(mvnRepoDao);
		
		// Mock and stub test data
		// note: don't use http://myrepo/myartifact as it makes test longer due to argument matching on URL object
		URL url = new URL("http:myrepo/myartifact");
		HttpURLConnection httpConnection = mock(HttpURLConnection.class);
		doReturn(httpConnection).when(mvnRepoDao).openHttpUrlConnection(url);
		
		// execute test
		mvnRepoDao.isValidUrl(url);
		
		// verify that read timeout has been set to 30s for used http connection
		verify(httpConnection).setReadTimeout(30000);
		
	}
	
	@Test
	public void isValidUrl_closes_connection() throws IOException {
		// ensure nothing is stubbed
		reset(mvnRepoDao);
		
		// Mock and stub test data
		// note: don't use http://myrepo/myartifact as it makes test longer due to argument matching on URL object
		URL url = new URL("http:myrepo/myartifact");
		HttpURLConnection httpConnection = mock(HttpURLConnection.class);
		doReturn(httpConnection).when(mvnRepoDao).openHttpUrlConnection(url);
		
		// execute test
		mvnRepoDao.isValidUrl(url);
		
		// verify that read timeout has been set to 30s for used http connection
		verify(httpConnection).disconnect();
		
	}

	@Test
	public void isValidUrl_closes_connection_even_in_case_of_failure() throws IOException {
		// ensure nothing is stubbed
		reset(mvnRepoDao);
		
		// Mock and stub test data
		// note: don't use http://myrepo/myartifact as it makes test longer due to argument matching on URL object
		URL url = new URL("http:myrepo/myartifact");
		HttpURLConnection httpConnection = mock(HttpURLConnection.class);
		doReturn(httpConnection).when(mvnRepoDao).openHttpUrlConnection(url);
		doThrow(new IOException("test")).when(httpConnection).getResponseCode();
		
		// execute test
		mvnRepoDao.isValidUrl(url);
		
		// verify that read timeout has been set to 30s for used http connection
		verify(httpConnection).disconnect();
		
	}

}
