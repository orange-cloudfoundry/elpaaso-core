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
package com.francetelecom.clara.cloud.providersoap.administration.v4.service;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.providersoap.mapping.SoapMapper;
import com.francetelecom.clara.cloud.services.dto.ApplicationDTO;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.PaasUserNotFoundException;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.orange.clara.cloud.providersoap.administration.v4.exception.ApplicationNotFoundError;
import com.orange.clara.cloud.providersoap.administration.v4.exception.DuplicateApplicationError;
import com.orange.clara.cloud.providersoap.administration.v4.exception.DuplicateReleaseError;
import com.orange.clara.cloud.providersoap.administration.v4.exception.PaasUserNotFoundError;
import com.orange.clara.cloud.providersoap.administration.v4.exception.ReleaseNotFoundError;
import com.orange.clara.cloud.providersoap.administration.v4.model.ApplicationModel;
import com.orange.clara.cloud.providersoap.administration.v4.model.CreateApplicationCommand;
import com.orange.clara.cloud.providersoap.administration.v4.model.CreateReleaseCommand;
import com.orange.clara.cloud.providersoap.administration.v4.model.MiddlewareProfile;
import com.orange.clara.cloud.providersoap.administration.v4.model.ReleaseModel;
import com.orange.clara.cloud.providersoap.administration.v4.service.ApplicationNotFoundErrorFault;
import com.orange.clara.cloud.providersoap.administration.v4.service.DuplicateApplicationErrorFault;
import com.orange.clara.cloud.providersoap.administration.v4.service.DuplicateReleaseErrorFault;
import com.orange.clara.cloud.providersoap.administration.v4.service.PaasAdministrationService;
import com.orange.clara.cloud.providersoap.administration.v4.service.PaasUserNotFoundErrorFault;
import com.orange.clara.cloud.providersoap.administration.v4.service.ReleaseNotFoundErrorFault;
import com.orange.clara.cloud.providersoap.security.v1.Credentials;

@javax.jws.WebService(serviceName = "PaasAdministrationService", portName = "PaasAdministrationServicePort", targetNamespace = "http://www.orange.com/paas/administration/v4/PaasAdministrationService", endpointInterface = "com.orange.clara.cloud.providersoap.administration.v4.service.PaasAdministrationService")
@org.apache.cxf.annotations.SchemaValidation(enabled = true)
public class PaasAdministrationServiceImpl implements PaasAdministrationService {

	private static final Logger LOG = LoggerFactory.getLogger(PaasAdministrationServiceImpl.class);

	private ManageApplication manageApplication;
	private ManageApplicationRelease manageApplicationRelease;

	private SoapMapper mapper;

	/**
	 * Create an application.
	 * 
	 * @param command
	 * 
	 * @param credentials
	 *            user credentials
	 * 
	 * @throws DuplicateApplicationErrorFault
	 *             when application already exists
	 * @throws MissingDefaultUserErrorFault
	 *             when application is private and no default user is specified
	 * @throws PaasUserNotFoundErrorFault
	 *             when no paas user matches specified user id
	 */
	@Override
	public String createApplication(CreateApplicationCommand command, Credentials credentials) throws PaasUserNotFoundErrorFault,
			DuplicateApplicationErrorFault {
		try {
			Assert.notNull(credentials, "no credentials provided");
			Assert.notNull(command, "no command provided");

			String label = command.getLabel();
			String code = command.getCode();
			String description = command.getDescription();
			String registryUrl = command.getRegistryUrl();
			Boolean isPublic = command.isIsPublic();

			// boolean isPublicApplication = isPublic != null ? isPublic : true;
			boolean isPublicApplication = isPublic == null ? true : isPublic;
			SSOId ssoid = new SSOId(credentials.getSsoid());
			if (isPublicApplication) {
				return manageApplication.createPublicApplication(code, label, description, registryUrl == null ? null : new URL(registryUrl), ssoid);
			} else {
				return manageApplication.createPrivateApplication(code, label, description, registryUrl == null ? null : new URL(registryUrl), ssoid);
			}
			// returns application generated uid
		} catch (DuplicateApplicationException e) {
			LOG.warn("exception : " + e);
			throw (DuplicateApplicationErrorFault) mapper.map(e, DuplicateApplicationErrorFault.class, DuplicateApplicationError.class);
		} catch (MalformedURLException e) {
			LOG.warn("exception : " + e);
			throw new IllegalArgumentException(e);
		} catch (PaasUserNotFoundException e) {
			LOG.error("exception : " + e);
			throw (PaasUserNotFoundErrorFault) mapper.map(e, PaasUserNotFoundErrorFault.class, PaasUserNotFoundError.class);
		}
	}

	/**
	 * Get all middleware profiles.
	 * 
	 * @param credentials
	 *            user credentials
	 * @return List of all middleware profiles available on this platform
	 */
	@Override
	public List<MiddlewareProfile> getAllMiddlewareProfiles(Credentials credentials) {
		List<com.francetelecom.clara.cloud.coremodel.MiddlewareProfile> findAllMiddlewareProfil = manageApplicationRelease.findAllMiddlewareProfil();
		return Lists.transform(findAllMiddlewareProfil, new Function<com.francetelecom.clara.cloud.coremodel.MiddlewareProfile, MiddlewareProfile>() {
			public MiddlewareProfile apply(com.francetelecom.clara.cloud.coremodel.MiddlewareProfile profile) {
				return mapper.map(profile, MiddlewareProfile.class);
			}
		});
	}

	/**
	 * Get all applications.
	 * 
	 * @param credentials
	 *            user credentials
	 * @return List of all applications
	 */
	@Override
	public List<ApplicationModel> getAllApplications(Credentials credentials) {
		// we want to get a collection of applications
		Collection<Application> applications = manageApplication.findApplications();

		List<ApplicationModel> target = new ArrayList<ApplicationModel>();

		if (applications != null) {
			for (Application application : applications) {
				target.add(mapper.map(application, ApplicationModel.class));
			}
		}

		// returns applications
		return target;
	}

	/**
	 * Create a release specifyng a middlewareprofileversion.
	 * 
	 * @param command
	 * 
	 * @param credentials
	 *            user credentials
	 * @return release uid
	 * 
	 * @exception DuplicateReleaseErrorFault
	 *                when release already exists
	 * @exception ApplicationNotFoundErrorFault
	 *                when application does not exist
	 * @exception PaasUserNotFoundErrorFault
	 *                when paas user does not exist
	 * 
	 * @see MiddlewareProfile
	 */
	@Override
	public String createRelease(CreateReleaseCommand command, Credentials credentials) throws DuplicateReleaseErrorFault, ApplicationNotFoundErrorFault,
			PaasUserNotFoundErrorFault {

		Assert.notNull(credentials, "no credentials provided");
		Assert.notNull(command, "no command provided");

		String version = command.getVersion();
		String applicationUID = command.getApplicationUID();
		String description = command.getDescription();
		String versionControlUrl = command.getVersionControlUrl();
		String profileVersion = command.getProfileVersion();

		try {
			URL url = null;
			if (versionControlUrl != null) {
				url = new URL(versionControlUrl);
			}

			// Creates and persists release
			return manageApplicationRelease.createApplicationRelease(applicationUID, credentials.getSsoid(), version, description, url, profileVersion);
		} catch (PaasUserNotFoundException e) {
			LOG.warn("exception : " + e);
			throw (PaasUserNotFoundErrorFault) mapper.map(e, PaasUserNotFoundErrorFault.class, PaasUserNotFoundError.class);
		} catch (ApplicationNotFoundException e) {
			LOG.warn("exception : " + e);
			throw (ApplicationNotFoundErrorFault) mapper.map(e, ApplicationNotFoundErrorFault.class, ApplicationNotFoundError.class);
		} catch (DuplicateApplicationReleaseException e) {
			LOG.warn("exception : " + e);
			throw (DuplicateReleaseErrorFault) mapper.map(e, DuplicateReleaseErrorFault.class, DuplicateReleaseError.class);
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * Find application release uid for a given application and release version
	 * 
	 * @param applicationUID
	 *            application uid
	 * @param releaseVersion
	 *            release version
	 * @param credentials
	 *            user credentials
	 * @return application release uid
	 * 
	 * @exception ReleaseNotFoundErrorFault
	 *                when release does not exist
	 */
	@Override
	public String findApplicationReleaseByApplicationUIDAndVersion(String applicationUID, String releaseVersion, Credentials credentials)
			throws ReleaseNotFoundErrorFault {
		try {
			// application uid must be provided
			Assert.hasText(applicationUID, "application uid must not be empty");
			// release version must be provided
			Assert.hasText(releaseVersion, "release version must not be empty");
			// we want to get application release
			ApplicationRelease source = manageApplicationRelease.findApplicationReleaseByApplicationAndReleaseVersion(applicationUID, releaseVersion);
			return source.getUID();
		} catch (ApplicationReleaseNotFoundException e) {
			LOG.warn("exception : " + e);
			throw (ReleaseNotFoundErrorFault) mapper.map(e, ReleaseNotFoundErrorFault.class, ReleaseNotFoundError.class);
		}
	}

	/**
	 * Get the details of a given ApplicationRelease
	 * 
	 * @param uid
	 *            release uid
	 * @param credentials
	 *            user credentials
	 * @return An application release
	 * @exception ReleaseNotFoundErrorFault
	 *                when release does not exist
	 */
	@Override
	public ReleaseModel getApplicationRelease(String uid, Credentials credentials) throws ReleaseNotFoundErrorFault {
		try {
			// the uid of the release must be provided
			Assert.hasText(uid, "the uid of the release must not be empty");
			// we want to get an applicationRelease
			ApplicationRelease source = manageApplicationRelease.findApplicationReleaseByUID(uid);
			// returns the given release
			return mapper.map(source, ReleaseModel.class);
		} catch (ApplicationReleaseNotFoundException e) {
			LOG.warn("exception : " + e);
			throw (ReleaseNotFoundErrorFault) mapper.map(e, ReleaseNotFoundErrorFault.class, ReleaseNotFoundError.class);
		}
	}

	@Override
	public List<ReleaseModel> getApplicationReleasesByApplicationUID(String applicationUID, Credentials credentials) throws ApplicationNotFoundErrorFault {
		try {
			// find releases for a given application
			List<ApplicationRelease> releases = manageApplicationRelease.findApplicationReleasesByAppUID(applicationUID);
			List<ReleaseModel> target = new ArrayList<ReleaseModel>();
			// map releases (from core model) to releases (SOAP)
			for (ApplicationRelease release : releases) {
				target.add(mapper.map(release, ReleaseModel.class));
			}
			// returns releases
			return target;

		} catch (ObjectNotFoundException e) {
			LOG.warn("exception : " + e);
			throw (ApplicationNotFoundErrorFault) mapper.map(e, ApplicationNotFoundErrorFault.class, ApplicationNotFoundError.class);
		}

	}

	@Override
	public void deleteApplicationRelease(String uid, Credentials header) throws ReleaseNotFoundErrorFault {
		try {
			manageApplicationRelease.deleteApplicationRelease(uid);
		} catch (ApplicationReleaseNotFoundException e) {
			LOG.warn("exception : " + e);
			throw (ReleaseNotFoundErrorFault) mapper.map(e, ReleaseNotFoundErrorFault.class, ReleaseNotFoundError.class);
		}
	}

	/**
	 * Find an application uid by label.
	 * 
	 * @param label
	 *            application label
	 * @param credentials
	 *            user credentials
	 * @return application uid
	 * @throws ApplicationNotFoundErrorFault
	 *             when application does not exist
	 */
	@Override
	public String findApplicationByLabel(String label, Credentials credentials) throws ApplicationNotFoundErrorFault {
		try {
			// we want to get application by its label
			ApplicationDTO source = manageApplication.findApplicationByLabel(label);
			// returns the uid of the application
			return source.getUid();
		} catch (ApplicationNotFoundException e) {
			LOG.warn("exception : " + e);
			throw (ApplicationNotFoundErrorFault) mapper.map(e, ApplicationNotFoundErrorFault.class, ApplicationNotFoundError.class);
		}
	}

	public void setManageApplication(ManageApplication manageApplication) {
		if (manageApplication == null)
			throw new IllegalArgumentException("Cannot create PaasAdministrationServiceImpl. manageApplication is required.");
		this.manageApplication = manageApplication;
	}

	public void setManageApplicationRelease(ManageApplicationRelease manageApplicationRelease) {
		if (manageApplicationRelease == null)
			throw new IllegalArgumentException("Cannot create PaasAdministrationServiceImpl. manageApplicationRelease is required.");
		this.manageApplicationRelease = manageApplicationRelease;
	}

	public void setMapper(SoapMapper mapper) {
		if (mapper == null)
			throw new IllegalArgumentException("Cannot create PaasAdministrationServiceImpl. mapper is required.");
		this.mapper = mapper;
	}

	@Override
	public void deleteApplication(String uid, Credentials header) throws ApplicationNotFoundErrorFault {
		try {
			manageApplication.deleteApplication(uid);
		} catch (ApplicationNotFoundException e) {
			LOG.warn("exception : " + e);
			throw (ApplicationNotFoundErrorFault) mapper.map(e, ApplicationNotFoundErrorFault.class, ApplicationNotFoundError.class);
		}

	}

}
