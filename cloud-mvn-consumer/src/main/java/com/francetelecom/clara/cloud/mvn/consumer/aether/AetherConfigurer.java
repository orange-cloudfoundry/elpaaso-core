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
package com.francetelecom.clara.cloud.mvn.consumer.aether;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.internal.impl.DefaultRepositoryLayoutProvider;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.resolution.ResolutionErrorPolicy;
import org.eclipse.aether.resolution.ResolutionErrorPolicyRequest;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.SimpleResolutionErrorPolicy;
import org.springframework.beans.factory.annotation.Autowired;

public class AetherConfigurer {

	@Autowired
	ProxyManager proxyManager;

	public AetherConfigurer() {
	}

	public RepositorySystem newRepositorySystem() {
        /*
         * Aether's components implement org.eclipse.aether.spi.locator.Service to ease manual wiring and using the
         * prepopulated DefaultServiceLocator, we only need to register the repository connector and transporter
         * factories.
         */
		DefaultServiceLocator defaultLocator = MavenRepositorySystemUtils.newServiceLocator();
		defaultLocator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
		defaultLocator.addService(TransporterFactory.class, FileTransporterFactory.class);
		defaultLocator.addService(TransporterFactory.class, HttpTransporterFactory.class);


		defaultLocator.setErrorHandler(new DefaultServiceLocator.ErrorHandler() {
			@Override
			public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
				exception.printStackTrace();
			}
		});

		return defaultLocator.getService(RepositorySystem.class);

	}

	public RepositorySystemSession newSession(RepositorySystem system, LocalRepository localRepository) {
		DefaultRepositorySystemSession session;
		session = MavenRepositorySystemUtils.newSession();
		final LocalRepositoryManager localRepositoryManager = system.newLocalRepositoryManager(session,localRepository);
		session.setLocalRepositoryManager(localRepositoryManager);
		session.setTransferListener(new ConsoleTransferListener(System.out));
		session.setRepositoryListener(new ConsoleRepositoryListener(System.out));
		disableLocalCaching(session);
		disableArtifactNotFoundCache(session);
		disableHttpsCertificateCheck(session);
		session.setProxySelector(proxyManager.selectProxies());
		session.setReadOnly();
		return session;
	}

	public void disableHttpsCertificateCheck(DefaultRepositorySystemSession session) {
		session.setConfigProperty("maven.wagon.http.ssl.insecure", "true");
		session.setConfigProperty("maven.wagon.http.ssl.allowall", "true");
	}

	public void disableArtifactNotFoundCache(DefaultRepositorySystemSession mavenSession) {
		  mavenSession.setResolutionErrorPolicy(new SimpleResolutionErrorPolicy(false,false));
				//.setNotFoundCachingEnabled(false);
	}

	private DefaultRepositorySystemSession disableLocalCaching(DefaultRepositorySystemSession mavenSession) {
		mavenSession.setUpdatePolicy("always");
		return mavenSession;
	}

}
