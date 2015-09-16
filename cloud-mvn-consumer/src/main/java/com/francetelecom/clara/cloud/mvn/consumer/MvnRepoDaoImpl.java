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

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.mvn.consumer.aether.AetherConfigurer;
import com.francetelecom.clara.cloud.mvn.consumer.aether.ProxyManager;
import com.francetelecom.clara.cloud.mvn.consumer.maven.MavenDeployer;

import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.internal.impl.EnhancedLocalRepositoryManagerFactory;
import org.eclipse.aether.internal.impl.Maven2RepositoryLayoutFactory;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.repository.Proxy;
import org.eclipse.aether.repository.ProxySelector;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.transfer.NoRepositoryLayoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

//import org.eclipse.aether.util.layout.MavenDefaultLayout;
//import org.eclipse.aether.util.layout.RepositoryLayout;

/*
 import org.apache.maven.shared.runtime.DefaultMavenRuntime;
 import org.apache.maven.shared.runtime.MavenProjectProperties;
 import org.apache.maven.shared.runtime.MavenRuntime;
 import org.apache.maven.shared.runtime.MavenRuntimeException;
 */

/**
 * Implementation class for Maven Repository Data Access Object
 * <p>
 * - manages remote repo acces (read and write) - manages local working space,
 * and maven local embedded projects builds.
 * <p>
 * <p>
 * <p>
 * links : http://nexus.sonatype.org/nexus-test-harness.html
 * http://nexus.sonatype.org/oss-repository-hosting.html
 * <p>
 * http://maven.apache.org/shared/index.html
 * <p>
 * <p>
 * https://docs.sonatype.org/display/AETHER/Home
 * <p>
 * http://maven.apache.org/ref/3.0.5/
 * http://maven.apache.org/docs/3.0.5/release-notes.html
 */
public class MvnRepoDaoImpl implements MvnRepoDao {

    private static Logger logger = LoggerFactory.getLogger(MvnRepoDaoImpl.class);

    protected AetherConfigurer aetherConfigurer;
    protected MvnConsumerConfigurer mvnConsumerConfigurer;
    protected MavenDeployer mavenDeployer;

    private ProxySelector mvnProxySelector;
    private final RepositoryPolicy disabledRepoPolicy = new RepositoryPolicy(false, null, null);

    @Autowired
    private ProxyManager proxyManager;

    public MvnRepoDaoImpl() {
    }

    /**
     * Initial configuration
     *
     * @throws IOException
     */
    public void init() throws Exception {
        XTrustProvider.install();

        logger.debug("creating settings.xml");

        mvnProxySelector = proxyManager.selectProxies();
        logger.debug("creating settings.xml end");
    }

    private List<RemoteRepository> initRemoteRepositories() {
        List<RemoteRepository> remoteRepositories = new ArrayList<RemoteRepository>();
        List<RemoteRepository.Builder> builders = new ArrayList<RemoteRepository.Builder>();

        List<RemoteRepository> pullRemoteRepo = this.mvnConsumerConfigurer.getPullRemoteRepo();
        for (RemoteRepository aPullRemoteRepository : pullRemoteRepo) {
            builders.add(new RemoteRepository.Builder(aPullRemoteRepository));
        }


        // Add this to support proxy configuration
        for (RemoteRepository.Builder builder : builders) {
            logger.debug("Processing builder {}",builder);
            if (mvnProxySelector != null) {
                logger.debug("Proxy selector {} found for builder {}",mvnProxySelector,builder);
                RemoteRepository remoteRepo = builder.build();
                Proxy proxy = mvnProxySelector.getProxy(remoteRepo);
                if (proxy != null) {
                    logger.debug("[repo:{}] => setProxy({})", remoteRepo.getHost(), proxy.toString());
                    builder.setProxy(proxy);
                }
            }
            remoteRepositories.add(builder.build());
        }
        return remoteRepositories;
    }



    private Artifact convertToArtifact(MavenReference mavenReference) {
        MavenReference mavenReferenceEmpty = mavenReference.duplicateWithEmpty();
        Artifact artifact = new DefaultArtifact(mavenReferenceEmpty.getGroupId(), mavenReferenceEmpty.getArtifactId(), mavenReferenceEmpty.getClassifier(),
                mavenReferenceEmpty.getExtension(), mavenReferenceEmpty.getVersion());

        return artifact;
    }

    @Override
    public MavenReference resolveUrl(MavenReference mavenReference) {
        MavenReference mavenRef = new MavenReference(mavenReference);
        updateUrl(mavenRef);
        if (!isArtifactAvailable(mavenRef)) {
            throw new MavenReferenceResolutionException(mavenRef, "artifact is not available at " + mavenRef.getAccessUrl());
        }
        return mavenRef;

    }

    protected void updateUrl(MavenReference mavenRef) {
        logger.debug("Resolving maven reference using all repositories for " + mavenRef);
        Artifact artifactToFind = convertToArtifact(mavenRef);
        try {
            RepositorySystem aetherRepoSystem = aetherConfigurer.newRepositorySystem();
            RepositorySystemSession aetherRepoSession = aetherConfigurer.newSession(aetherRepoSystem, this.mvnConsumerConfigurer.getLocalM2Repo());
            ArtifactRequest resolveArtifactRequest = new ArtifactRequest(artifactToFind, initRemoteRepositories(), null);
            ArtifactResult artifactResultFound = aetherRepoSystem.resolveArtifact(aetherRepoSession, resolveArtifactRequest);

            ArtifactRepository artifactDeploymentRepository = artifactResultFound.getRepository();
            URI artifactResolvedUri;
            if (artifactDeploymentRepository instanceof RemoteRepository) {
                RemoteRepository  remoteRepository = (RemoteRepository )artifactDeploymentRepository;
                artifactResolvedUri = getArtifactUriFrom(remoteRepository, aetherRepoSession, artifactResultFound);
            } else if (artifactDeploymentRepository instanceof LocalRepository) {
                LocalRepository localRepository = (LocalRepository)artifactDeploymentRepository;
                artifactResolvedUri = getArtifactUriFrom(localRepository, aetherRepoSession, artifactResultFound);
            } else {
                throw new TechnicalException("Unsupported Maven Repository type: " + artifactDeploymentRepository.getClass().getSimpleName());
            }

            mavenRef.setAccessUrl(artifactResolvedUri.toURL());
        } catch (MalformedURLException e) {
            logger.error("URL for " + mavenRef.getArtifactId() + " is malformed", e);
            throw new TechnicalException("Error in Maven repository URL", e);
        } catch (URISyntaxException e) {
            logger.error("URL for " + mavenRef.getArtifactId() + " is malformed", e);
            throw new TechnicalException("Error in Maven repository URL", e);
        } catch (ArtifactResolutionException e) {
            // log level is warning (not error) as it might be a 'normal' that
            // maven references are incorrect
            logger.warn(e.getMessage());
            String message = e.getCause() == null ? "no cause" : e.getCause().getMessage();
			logger.warn("Error cause : " +message);
            throw new MavenReferenceResolutionException(mavenRef, e);
        }
        logger.debug("Resolved maven reference: " + mavenRef);

    }

    private URI getArtifactUriFrom(LocalRepository localRepository, RepositorySystemSession aetherRepoSession, ArtifactResult artifactResultFound) throws URISyntaxException {
        try {
            final LocalRepositoryManager localRepositoryManager = new EnhancedLocalRepositoryManagerFactory().newInstance(aetherRepoSession, localRepository);
            return new URI(localRepositoryManager.getPathForLocalArtifact(artifactResultFound.getArtifact()));
        } catch (NoLocalRepositoryManagerException e) {
            logger.error("NoLocalRepositoryManagerException", e);
            throw new TechnicalException("Invalid Maven repo layout (NoLocalRepositoryManagerException)", e);
        }
    }

    private URI getArtifactUriFrom(RemoteRepository remoteRepository, RepositorySystemSession aetherRepoSession, ArtifactResult artifactResultFound) throws URISyntaxException {
        URI repositoryUri = new URI(remoteRepository.getUrl());
        try {
            URI artifactUri = new Maven2RepositoryLayoutFactory().newInstance(aetherRepoSession, remoteRepository).getLocation(artifactResultFound.getArtifact(), false);
            return repositoryUri.resolve(artifactUri);
        } catch (NoRepositoryLayoutException e) {
            logger.error("NoRepositoryLayoutException", e);
            throw new TechnicalException("Invalid Maven repo layout (NoRepositoryLayoutException)", e);
        }
    }

    public boolean isUsingProxyForPullRepo() {
        boolean result = false;
        RemoteRepository repository = mvnConsumerConfigurer.getPullPrimaryRepository();
        if (mvnProxySelector != null && mvnProxySelector.getProxy(repository) != null) {
            result = true;
        }

        return result;
    }

    /**
     * resolve an artifact by searching it into repositories using Aether API
     *
     * @param artifactToFind
     * @return
     * @throws ArtifactResolutionException
     */
    protected ArtifactResult resolveArtifact(Artifact artifactToFind) throws ArtifactResolutionException {
        RepositorySystem aetherRepoSystem = aetherConfigurer.newRepositorySystem();
        RepositorySystemSession aetherRepoSession = aetherConfigurer.newSession(aetherRepoSystem, this.mvnConsumerConfigurer.getLocalM2Repo());
        ArtifactRequest resolveArtifactRequest = new ArtifactRequest(artifactToFind, initRemoteRepositories(), null);
        ArtifactResult artifactResultFound = aetherRepoSystem.resolveArtifact(aetherRepoSession, resolveArtifactRequest);
        return artifactResultFound;
    }

    /**
     * try to resolve a maven reference and return corresponding file in local
     * repository mavenReference url is not updated
     */
    @Override
    public File getFileFromLocalRepository(MavenReference mavenRef) {
        Artifact artifactToFind = convertToArtifact(mavenRef);
        ArtifactResult result;
        try {
            result = resolveArtifact(artifactToFind);
        } catch (ArtifactResolutionException e) {
            throw new MavenReferenceResolutionException(mavenRef, e);
        }
        Artifact artifact = result.getArtifact();
        if (artifact == null)
            throw new MavenReferenceResolutionException(mavenRef, "artifact is null");
        return artifact.getFile();
    }

    private URI convertDosPathToUri(String dosPath) throws URISyntaxException {
        return new URI(dosPath.replaceAll("\\\\", "/"));
    }

    public void setMvnConsumerConfigurer(MvnConsumerConfigurer mvnConsumerConfigurer) {
        this.mvnConsumerConfigurer = mvnConsumerConfigurer;
    }

    public void setMavenDeployer(MavenDeployer mavenDeployer) {
        this.mavenDeployer = mavenDeployer;
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

    @Override
    public void deployFileset(MavenReference gav, ArrayList<FileRef> fileSet) {
        this.mavenDeployer.deployFileset(gav, fileSet);
        updateUrl(gav);

    }
}
