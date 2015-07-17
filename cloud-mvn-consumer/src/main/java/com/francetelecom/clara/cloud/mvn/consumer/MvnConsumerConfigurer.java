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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;

import com.francetelecom.clara.cloud.commons.TechnicalException;

public class MvnConsumerConfigurer {
	private static Logger logger = LoggerFactory.getLogger(MvnConsumerConfigurer.class.getName());

	protected String pullRepositoryUrl;

	protected String releaseRepositoryRefreshIntervalInMin;
	protected String snapshotRepositoryRefreshIntervalInMin;
	protected String pushSnapshotRepositoryUrl;
	protected String pushSnapshotRepositoryUser;
	protected String pushSnapshotRepositoryPassword;
	protected String pushReleaseRepositoryUrl;
	protected String pushReleaseRepositoryUser;
	protected String pushReleaseRepositoryPassword;

	protected File localM2RepoPath;
	protected File localWorkDir;
	protected LocalRepository localM2Repo;
	protected RemoteRepository pullRemoteRepo;

	protected InputStream assemblyTemplateFile;
	protected String assemblyTemplate;
	protected String assemblyDescriptionFileLocation;

	protected String paasRepositoryId;
	protected boolean deleteLocalRepoOnStartup = true;

	public MvnConsumerConfigurer() {
	}

	public void init() throws IOException {

		if (deleteLocalRepoOnStartup) {
			deleteLocalRepo();
		} else {
			logger.info("Skipping local repo cleanup");
		}

		logger.info("initial config for Maven Connector");
		this.localM2Repo = new LocalRepository(this.localM2RepoPath);

		initPullRepoInterval();
		loadAssemblyTemplateFile();
	}

	public void deleteLocalRepo() throws IOException {
		logger.info("Deleting any existing local Maven repository: {}", this.localM2RepoPath);
		if (this.localM2RepoPath.isDirectory()) {
			FileUtils.deleteDirectory(this.localM2RepoPath);
		} else {
			logger.info("Cannot delete local Maven repository: {}. It doesn't exist or not a directory", this.localM2RepoPath);
		}
	}

	private void initPullRepoInterval() {
		final RemoteRepository.Builder builder = new RemoteRepository.Builder("remoteRepo", "default", this.pullRepositoryUrl);
		RepositoryPolicy releasePolicy = new RepositoryPolicy(true,RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":" + releaseRepositoryRefreshIntervalInMin,RepositoryPolicy.CHECKSUM_POLICY_WARN);
		builder.setReleasePolicy(releasePolicy);

		RepositoryPolicy snapshotPolicy = new RepositoryPolicy(true,RepositoryPolicy.UPDATE_POLICY_INTERVAL + ":" + snapshotRepositoryRefreshIntervalInMin,RepositoryPolicy.CHECKSUM_POLICY_WARN);
		builder.setSnapshotPolicy(snapshotPolicy);
		this.pullRemoteRepo=builder.build();
	}

	public void loadAssemblyTemplateFile() {
		Validate.notNull(assemblyTemplateFile, "assemblyTemplateFile should not be null");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(assemblyTemplateFile));
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			this.assemblyTemplate = sb.toString();
		} catch (IOException e) {
			logger.error(e.getLocalizedMessage());
			throw new TechnicalException(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				logger.error(e.getLocalizedMessage());
				throw new TechnicalException(e);
			}
		}
	}


	/**
	 * IOC
	 *
	 * @param repositoryUrl
	 * @deprecated please use setPullRepositoryUrl instead
	 */
	@Deprecated
	public void setRepositoryUrl(String repositoryUrl) {
		this.pullRepositoryUrl = repositoryUrl;
	}

	public void setPullRepositoryUrl(String repositoryUrl) {
		this.pullRepositoryUrl = repositoryUrl;
	}

	/**
	 * IOC
	 * 
	 * @param localM2RepoPath
	 */
	public void setLocalM2RepoPath(File localM2RepoPath) {
		this.localM2RepoPath = localM2RepoPath;
	}

	/**
	 * IOC
	 * 
	 * @param localWorkDir
	 */
	public void setLocalWorkDir(File localWorkDir) {
		this.localWorkDir = localWorkDir;
	}

	public void setPushSnapshotRepositoryUrl(String snapshotRepositoryUrl) {
		this.pushSnapshotRepositoryUrl = snapshotRepositoryUrl;
	}

	public void setPushReleaseRepositoryUrl(String releaseRepositoryUrl) {
		this.pushReleaseRepositoryUrl = releaseRepositoryUrl;
	}

	public void setPushSnapshotRepositoryUser(String snapshotRepositoryUser) {
		this.pushSnapshotRepositoryUser = snapshotRepositoryUser;
	}

	public void setPushSnapshotRepositoryPassword(String snapshotRepositoryPassword) {
		this.pushSnapshotRepositoryPassword = snapshotRepositoryPassword;
	}

	public void setPushReleaseRepositoryUser(String releaseRepositoryUser) {
		this.pushReleaseRepositoryUser = releaseRepositoryUser;
	}

	public void setPushReleaseRepositoryPassword(String releaseRepositoryPassword) {
		this.pushReleaseRepositoryPassword = releaseRepositoryPassword;
	}

	public String getAssemblyDescriptionFileLocation() {
		return assemblyDescriptionFileLocation;
	}

	public void setAssemblyDescriptionFileLocation(String assemblyDescriptionFileLocation) {
		this.assemblyDescriptionFileLocation = assemblyDescriptionFileLocation;
	}

	public void setAssemblyTemplateFile(InputStream templateFile) {
		this.assemblyTemplateFile = templateFile;
	}

	public String getAssemblyTemplate() {
		return assemblyTemplate;
	}

	public LocalRepository getLocalM2Repo() {
		return localM2Repo;
	}

	/**
	 * @deprecated use {@linkplain getPullRemoteRepo}
	 */
	@Deprecated
	public RemoteRepository getRemoteM2Repo() {
		return pullRemoteRepo;
	}

	public RemoteRepository getPullRemoteRepo() {
		return pullRemoteRepo;
	}

	public String getReleaseRepositoryRefreshIntervalInMin() {
		return releaseRepositoryRefreshIntervalInMin;
	}

	public void setReleaseRepositoryRefreshIntervalInMin(String pullReleaseRepositoryFrequencyInMin) {
		this.releaseRepositoryRefreshIntervalInMin = pullReleaseRepositoryFrequencyInMin;
	}

	public String getSnapshotRepositoryRefreshIntervalInMin() {
		return snapshotRepositoryRefreshIntervalInMin;
	}

	public void setSnapshotRepositoryRefreshIntervalInMin(String snapshotRepositoryRefreshIntervalInMin) {
		this.snapshotRepositoryRefreshIntervalInMin = snapshotRepositoryRefreshIntervalInMin;
	}

	/**
	 * @deprecated use {@linkplain getPullRepositoryUrl}
	 */
	@Deprecated
	public String getRepositoryUrl() {
		return pullRepositoryUrl;
	}

	public String getPullRepositoryUrl() {
		return pullRepositoryUrl;
	}

	public String getPushSnapshotRepositoryUrl() {
		return pushSnapshotRepositoryUrl;
	}

	public String getPushSnapshotRepositoryUser() {
		return pushSnapshotRepositoryUser;
	}

	public String getPushSnapshotRepositoryPassword() {
		return pushSnapshotRepositoryPassword;
	}

	public String getPushReleaseRepositoryUrl() {
		return pushReleaseRepositoryUrl;
	}

	public String getPushReleaseRepositoryUser() {
		return pushReleaseRepositoryUser;
	}

	public String getPushReleaseRepositoryPassword() {
		return pushReleaseRepositoryPassword;
	}

	public File getLocalM2RepoPath() {
		return localM2RepoPath;
	}

	public File getLocalWorkDir() {
		return localWorkDir;
	}

	public String getPaasRepositoryId() {
		return paasRepositoryId;
	}

	public void setPaasRepositoryId(String paasRepositoryId) {
		this.paasRepositoryId = paasRepositoryId;
	}

	public void setDeleteLocalRepoOnStartup(boolean deleteLocalRepoOnStartup) {
		this.deleteLocalRepoOnStartup = deleteLocalRepoOnStartup;
	}

}
