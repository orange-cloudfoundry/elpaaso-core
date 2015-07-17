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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import javax.activation.DataHandler;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.lang3.Validate;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.deployment.DeployRequest;
import org.eclipse.aether.deployment.DeployResult;
import org.eclipse.aether.deployment.DeploymentException;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.util.artifact.SubArtifact;
import org.eclipse.aether.util.repository.DefaultProxySelector;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.mvn.consumer.FileRef;
import com.francetelecom.clara.cloud.mvn.consumer.MvnConsumerConfigurer;
import com.francetelecom.clara.cloud.mvn.consumer.aether.AetherConfigurer;

public class MavenDeployer {
	/**
	 * Implementation class for Maven Repository Data Access Object
	 * 
	 * - manages remote repo acces (read and write) - manages local working
	 * space, and maven local embedded projects builds.
	 * 
	 * links : http://nexus.sonatype.org/nexus-test-harness.html
	 * http://nexus.sonatype.org/oss-repository-hosting.html
	 * 
	 * http://maven.apache.org/shared/index.html
	 * 
	 * 
	 * https://docs.sonatype.org/display/AETHER/Home
	 * 
	 * http://maven.apache.org/ref/3.0-beta-3/
	 * http://maven.apache.org/docs/3.0-beta-3/release-notes.html
	 * 
	 * 
	 */

	private static final Logger logger = LoggerFactory.getLogger(MavenDeployer.class);

	protected SettingsGenerator settingsGenerator;
	protected PomGenerator pomGenerator;

	protected AetherConfigurer aetherConfigurer;
	protected MvnConsumerConfigurer mvnConsumerConfigurer;

	protected File settingsFile;

	private ProxySelector mvnProxySelector;

	public MavenDeployer(MvnConsumerConfigurer mvnConsumerConfigurer) {
		this.mvnConsumerConfigurer = mvnConsumerConfigurer;

	}

	/**
	 * Initial configuration
	 * 
	 * @throws IOException
	 */
	public void init() throws Exception {
		logger.debug("creating settings.xml");
		String settingsFilename = this.mvnConsumerConfigurer.getLocalWorkDir().getAbsolutePath() + "/.m2/settings.xml";
		settingsFile = settingsGenerator.generateAndWrite(settingsFilename);
		logger.debug("created settings.xml as " + this.settingsFile.getAbsolutePath());
		mvnProxySelector = selectProxies();
		logger.debug("creating settings.xml end"); // added to check duration of
													// this method (take 1
													// minute !?)
	}

	private ProxySelector selectProxies() throws Exception {
		DefaultProxySelector proxySelector = new DefaultProxySelector();
		String httpProxyHost = System.getProperty("http.proxyHost");
		if (httpProxyHost != null) {
			int proxyPort = -1;
			try {
				proxyPort = Integer.parseInt(System.getProperty("http.proxyPort"));
			} catch (NumberFormatException nfe) {
				throw new Exception("Invalid (or null) http.proxyPort specified into system.properties");
			}
			String nonProxyHosts = System.getProperty("http.nonProxyHosts");
			nonProxyHosts = (nonProxyHosts != null ? nonProxyHosts : "");
			Proxy proxy = new Proxy("http", httpProxyHost, proxyPort, null);
			proxySelector.add(proxy, nonProxyHosts);
			logger.debug("MvnRepo proxy set to {}:{}" + (nonProxyHosts != null ? " (nonProxyHosts:" + nonProxyHosts + ")" : ""), httpProxyHost, proxyPort);

		}
		return proxySelector;
	}

	private MavenReference convertToMavenReference(Artifact artifact) {
		MavenReference result = new MavenReference(artifact.getGroupId(), artifact.getArtifactId(), artifact.getBaseVersion(), artifact.getExtension(),
				artifact.getClassifier());
		return result.duplicateWithNull();
	}

	private Artifact convertToArtifact(MavenReference mavenReference) {
		MavenReference mavenReferenceEmpty = mavenReference.duplicateWithEmpty();
		Artifact artifact = new DefaultArtifact(mavenReferenceEmpty.getGroupId(), mavenReferenceEmpty.getArtifactId(), mavenReferenceEmpty.getClassifier(),
				mavenReferenceEmpty.getExtension(), mavenReferenceEmpty.getVersion());

		return artifact;
	}

	/**
	 * Deploy a Maven component reference
	 * 
	 * @return
	 */
	public DeployResult deployFileset(MavenReference mavenReference, List<FileRef> fileset) {
		Validate.notNull(mavenReference, "Shoud not be null");
		Validate.notNull(fileset, "Shoud not be null");
		File projectDirectory = getProjectDirectory(mavenReference);
		return deploy(mavenReference, fileset, projectDirectory);
	}

	public DeployResult deployBin(MavenReference mavenReference, DataHandler binaryContent) {
		Validate.notNull(mavenReference, "Shoud not be null");
		Validate.notNull(binaryContent, "Shoud not be null");

		File projectDirectory = getProjectDirectory(mavenReference);
		File artifact = new File(projectDirectory, mavenReference.getArtifactName());
		try {
			binaryContent.writeTo(new FileOutputStream(artifact));
		} catch (IOException e) {
			logger.error("failure storing artifact stream " + mavenReference, e);
			throw new TechnicalException(e);
		}
		return install(mavenReference, artifact, projectDirectory);
	}

	private File getProjectDirectory(MavenReference mavenReference) {
		return new File(this.mvnConsumerConfigurer.getLocalWorkDir(), mavenReference.getGroupId() + "/" + mavenReference.getArtifactId());
	}

	private DeployResult deploy(MavenReference mavenReference, List<FileRef> fileset, File projectDirectory) {

		try {
			File archive = createArchive(projectDirectory, mavenReference, fileset);
			return install(mavenReference, archive, projectDirectory);
		} catch (IOException e) {
			logger.error("failure creating archive from fileset " + mavenReference, e);
			throw new TechnicalException(e);
		}
	}

	private DeployResult install(MavenReference mavenReference, File archive, File projectDirectory) {

		try {
			Artifact artifactToDeploy = convertToArtifact(mavenReference).setFile(archive);
			Artifact pom = new SubArtifact(artifactToDeploy, null, "pom").setFile(pomGenerator.generatePom(projectDirectory, mavenReference));

			RemoteRepository selectedRepository = selectRepositoryToDeploy(artifactToDeploy);
			DeployResult deploymentResult = deployUsingAether(artifactToDeploy, pom, selectedRepository);
			logger.debug(mavenReference + " has been deployed on " + selectedRepository + " - DeploymentResult: " + deploymentResult.toString());
			return deploymentResult;
			// updateUrl(mavenReference);

		} catch (IOException e) {
			logger.error("failure creating mvn work dir for project " + mavenReference, e);
			throw new TechnicalException(e);
		} catch (DeploymentException e) {
			logger.error("deployment failed for " + mavenReference, e);
			throw new TechnicalException(e);
		}
	}

	private RemoteRepository selectRepositoryToDeploy(Artifact artifactToDeploy) {
		if (artifactToDeploy == null) {
			throw new IllegalArgumentException("artifactToDeploy should not be null");
		}

		RemoteRepository.Builder snapRepoBuilder =  new RemoteRepository.Builder("snapshot-repository", "default", mvnConsumerConfigurer.getPushSnapshotRepositoryUrl());
		RepositoryPolicy disabledRepo = null;
		snapRepoBuilder.setReleasePolicy(disabledRepo);
		Authentication snapshotRepositoryAuthen = new AuthenticationBuilder().addUsername(mvnConsumerConfigurer.getPushSnapshotRepositoryUser()).addPassword(
				mvnConsumerConfigurer.getPushSnapshotRepositoryPassword()).build();
		snapRepoBuilder.setAuthentication(snapshotRepositoryAuthen);

		RemoteRepository.Builder releaseRepoBuilder = new RemoteRepository.Builder("release-repository", "default", mvnConsumerConfigurer.getPushReleaseRepositoryUrl());
		releaseRepoBuilder.setReleasePolicy(disabledRepo);
		Authentication releaseRepositoryAuthen = new AuthenticationBuilder().addUsername(mvnConsumerConfigurer.getPushReleaseRepositoryUser()).addPassword(
				mvnConsumerConfigurer.getPushReleaseRepositoryPassword()).build();
		releaseRepoBuilder.setAuthentication(releaseRepositoryAuthen);

		RemoteRepository result;
		if (artifactToDeploy.isSnapshot()) {
			result = snapRepoBuilder.build();
		} else {
			result = releaseRepoBuilder.build();
		}

		return result;
	}

	private DeployResult deployUsingAether(Artifact artifact, Artifact pom, RemoteRepository deploymentRepository) throws DeploymentException {
		RepositorySystem system = aetherConfigurer.newRepositorySystem();
		RepositorySystemSession session = aetherConfigurer.newSession(system, mvnConsumerConfigurer.getLocalM2Repo());
		DeployRequest deployRequest = new DeployRequest();
		deployRequest.addArtifact(artifact).addArtifact(pom);
		deployRequest.setRepository(deploymentRepository);
		DeployResult result = system.deploy(session, deployRequest);
		return result;
	}

	private File createArchive(File projectDirectory, MavenReference mavenReference, List<FileRef> fileset) throws IOException {
		File archive = new File(projectDirectory, mavenReference.getArtifactName());
		if ("tar.gz".equals(mavenReference.getExtension())) {
			populateTgzArchive(archive, fileset);
		} else {
			populateJarArchive(archive, fileset);
		}
		return archive;
	}

	private File populateTgzArchive(File archive, List<FileRef> fileset) throws IOException {
		archive.getParentFile().mkdirs();
		CompressorOutputStream zip = new GzipCompressorOutputStream(new FileOutputStream(archive));
		TarArchiveOutputStream tar = new TarArchiveOutputStream(zip);
		for (FileRef fileRef : fileset) {
			TarArchiveEntry entry = new TarArchiveEntry(new File(fileRef.getRelativeLocation()));
			byte[] bytes = fileRef.getContent().getBytes();
			entry.setSize(bytes.length);
			tar.putArchiveEntry(entry);
			tar.write(bytes);
			tar.closeArchiveEntry();
		}
		tar.close();
		return archive;
	}

	private File populateJarArchive(File archive, List<FileRef> fileset) throws IOException {
		archive.getParentFile().mkdirs();
		JarOutputStream target = new JarOutputStream(new FileOutputStream(archive));
		for (FileRef fileRef : fileset) {
			JarEntry jarAdd = new JarEntry(fileRef.getRelativeLocation());
			target.putNextEntry(jarAdd);
			target.write(fileRef.getContent().getBytes());
		}
		target.close();
		return archive;
	}

	private URI convertDosPathToUri(String dosPath) throws URISyntaxException {
		return new URI(dosPath.replaceAll("\\\\", "/"));
	}

	public void setMvnConsumerConfigurer(MvnConsumerConfigurer mvnConsumerConfigurer) {
		this.mvnConsumerConfigurer = mvnConsumerConfigurer;
	}

	public void setSettingsGenerator(SettingsGenerator settingsGenerator) {
		this.settingsGenerator = settingsGenerator;
	}

	public void setPomGenerator(PomGenerator pomGenerator) {
		this.pomGenerator = pomGenerator;
	}

	public void setAetherConfigurer(AetherConfigurer aetherConfigurer) {
		this.aetherConfigurer = aetherConfigurer;
	}

	/**
	 * Verify that artifact referenced by a given {@link MavenReference} is
	 * available
	 * 
	 * @param mavenRef
	 * @return true if available, false if not
	 */
	boolean isArtifactAvailable(MavenReference mavenRef) {
		if (mavenRef.getAccessUrl() == null)
			return false;
		return isValidUrl(mavenRef.getAccessUrl());
	}

	/**
	 * Verify that a given url can be reached
	 * 
	 * @param url
	 * @return true if valid, false if not
	 */
	boolean isValidUrl(URL url) {
		logger.debug("Testing url: " + url);
		HttpURLConnection huc = null;
		int responseCode = 0;
		try {
			huc = openHttpUrlConnection(url);
			huc.setRequestMethod("HEAD");
			// set a long enough timeout to be protected against slow
			// network/server response time
			huc.setReadTimeout(30000);
			huc.connect();
			responseCode = huc.getResponseCode();
		} catch (IOException e) {
			logger.error("unable to test url " + url, e);
		} finally {
			if (huc != null)
				huc.disconnect();
		}
		boolean isValid = (responseCode == 200);
		if (!isValid)
			logger.warn("Http HEAD on Url " + url + " returns " + responseCode);
		return isValid;
	}

	/**
	 * Wrap {@link URL#openConnection()} so that this method can be mocked in
	 * tests
	 */
	HttpURLConnection openHttpUrlConnection(URL url) throws IOException {
		return (HttpURLConnection) url.openConnection();
	}
}
