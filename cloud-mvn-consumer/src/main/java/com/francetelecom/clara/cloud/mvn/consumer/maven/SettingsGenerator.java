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
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.maven.settings.Mirror;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.io.DefaultSettingsWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.mvn.consumer.MvnConsumerConfigurer;

public class SettingsGenerator {

	private static Logger logger = LoggerFactory.getLogger(SettingsGenerator.class);

	String localRepository;
	String mirrorId;
	String mirrorUrl;
	String releaseRepositoryUser;
	String releaseRepositoryPassword;
	String snapshotRepositoryPassword;
	String snapshotRepositoryUser;

	public SettingsGenerator(MvnConsumerConfigurer mvnConsumerConfigurer) {


		mirrorId = mvnConsumerConfigurer.getPaasRepositoryId();

		localRepository = mvnConsumerConfigurer.getLocalM2RepoPath().getAbsolutePath();
		mirrorUrl = mvnConsumerConfigurer.getPullPrimaryRepository().getUrl();

		releaseRepositoryUser = mvnConsumerConfigurer.getPushReleaseRepositoryUser();
		releaseRepositoryPassword = mvnConsumerConfigurer.getPushReleaseRepositoryPassword();

		snapshotRepositoryUser = mvnConsumerConfigurer.getPushSnapshotRepositoryUser();
		snapshotRepositoryPassword = mvnConsumerConfigurer.getPushSnapshotRepositoryPassword();
	}

	/**
	 * methode to generate a custom mvn consumer settings.xml file
	 * 
	 * @return
	 */
	private String generateSettingsXml() {
		Settings settings = new Settings();
		settings.setLocalRepository(localRepository);

		//		
		// <mirror>
		// <id>FTRDProxy_central</id>
		// <mirrorOf>central</mirrorOf>
		// <name>FTRD Maven Proxy mirroring central (maven 2) repository</name>
		// </mirror>
		//		

		Mirror mirror = new Mirror();
		mirror.setId(mirrorId);
		mirror.setName("Paas Maven Repo");
		mirror.setUrl(mirrorUrl);
		mirror.setMirrorOf("*");
		settings.getMirrors().add(mirror);

		List<org.apache.maven.settings.Server> servers = new ArrayList<org.apache.maven.settings.Server>();

		org.apache.maven.settings.Server paasInhouse = new org.apache.maven.settings.Server();
		paasInhouse.setId("paas.push.release.repo");
		paasInhouse.setUsername(releaseRepositoryUser);
		paasInhouse.setPassword(releaseRepositoryPassword);
		servers.add(paasInhouse);

		org.apache.maven.settings.Server paasInhouseSnapshot = new org.apache.maven.settings.Server();
		paasInhouseSnapshot.setId("paas.push.snapshot.repo");
		paasInhouseSnapshot.setUsername(snapshotRepositoryUser);
		paasInhouseSnapshot.setPassword(snapshotRepositoryPassword);
		servers.add(paasInhouseSnapshot);

		settings.setServers(servers);

		DefaultSettingsWriter settingsWriter = new DefaultSettingsWriter();

		try {

			StringWriter aWriter = new StringWriter();
			settingsWriter.write(aWriter, null, settings);

			logger.info("settings xml for project sucessfully generated");

			return aWriter.toString();

		} catch (IOException e) {
			logger.error("settings.xml construction failure, JaxbException :" + e.getMessage());
			throw new TechnicalException(e);
		}
	}

	public File writeSettings(String settingsContent, String settingsFilename) throws IOException {

		File settingsFile = new File(settingsFilename);
		FileUtils.writeStringToFile(settingsFile, settingsContent);
		
		return settingsFile;
	}

	public File generateAndWrite(String settingsFilename) {
		File settingsFile=null;
		String settingsContent = generateSettingsXml();
		try {
			settingsFile=writeSettings(settingsContent, settingsFilename);
		} catch (IOException e) {
			new TechnicalException("Cannot write generated settings file.", e);
		}

		if (settingsFile == null) {
			throw new TechnicalException("Cannot generate settings. Unknown reason."); 
		}
		
		return settingsFile;
	}

}
