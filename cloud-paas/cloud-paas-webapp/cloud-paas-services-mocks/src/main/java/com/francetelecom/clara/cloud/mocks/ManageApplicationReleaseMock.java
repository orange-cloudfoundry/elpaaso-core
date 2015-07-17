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
package com.francetelecom.clara.cloud.mocks;

import static com.francetelecom.clara.cloud.mocks.SecurityUtils.currentUser;
import static com.francetelecom.clara.cloud.mocks.SecurityUtils.currentUserIsAdmin;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.technicalservice.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.PaasUserNotFoundException;
import com.google.common.base.Predicate;

/**
 * Created by IntelliJ IDEA. User: lzxv3002 Date: 09/06/11 Time: 14:58 To change
 * this template use File | Settings | File Templates.
 */
@Service("manageApplicationRelease")
public class ManageApplicationReleaseMock extends CoreItemServiceMock<ApplicationRelease> implements ManageApplicationRelease {

	@Autowired(required = true)
	private ManageLogicalDeploymentMock manageLogicalDeployment;

	@Autowired(required = true)
	private ManageEnvironmentMock manageEnvironment;

	@Autowired(required = true)
	private ManageApplicationMock manageApplication;

	@Autowired
	private ManagePaasUserMock managePaasUser;

	@Override
	public ApplicationRelease findApplicationReleaseByUID(String uid) throws ApplicationReleaseNotFoundException {
		ApplicationRelease release = null;
		try {
			release = findByUID(uid);
		} catch (ObjectNotFoundException e) {
			throw new ApplicationReleaseNotFoundException(e);
		}
		return release;
	}

	@Override
	public String createApplicationRelease(String applicationUID, String ssoId, String version) throws PaasUserNotFoundException, ApplicationNotFoundException,
			DuplicateApplicationReleaseException {
		return createApplicationRelease(applicationUID, ssoId, version, null, null, null);
	}

	@Override
	public String createApplicationRelease(String applicationUID, String ssoId, String version, String description, URL versionControleUrl, String middlewareProfil) throws PaasUserNotFoundException,
			ApplicationNotFoundException, DuplicateApplicationReleaseException {
		ApplicationRelease applicationRelease = null;
		// given paas user with ssoId aSsoId exists
		try {
			try {
				applicationRelease = new ApplicationRelease(manageApplication.findApplicationByUID(applicationUID), version);
				if (middlewareProfil != null) {
					applicationRelease.setMiddlewareProfileVersion(middlewareProfil);
				}
				if (description != null) {
					applicationRelease.setDescription(description);
				}
				if (versionControleUrl != null) {
					applicationRelease.setVersionControlUrl(versionControleUrl);
				}
			} catch (ObjectNotFoundException e) {
				throw new ApplicationNotFoundException("Application[" + applicationUID + "] not found:");
			}
			create(applicationRelease);

			// We need to persist logical deployment to
			manageLogicalDeployment.create(applicationRelease.getLogicalDeployment());

			// should never be thrown just need to be here in mocks
		} catch (DuplicateApplicationException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		} catch (ObjectNotFoundException e) {
			e.printStackTrace(); // To change body of catch statement use File |
									// Settings | File Templates.
		}
		return applicationRelease.getUID();
	}

	@Override
	public ApplicationRelease updateApplicationRelease(ApplicationRelease applicationRelease) throws ApplicationReleaseNotFoundException {
		ApplicationRelease updated = null;
		try {
			updated = update(applicationRelease);
		} catch (ObjectNotFoundException e) {
			throw new ApplicationReleaseNotFoundException(e);
		}
		return updated;
	}

	@Override
	public void deleteApplicationRelease(String applicationReleaseUID) throws ApplicationReleaseNotFoundException {
		if (!canBeDeleted(applicationReleaseUID)) {
			throw new IllegalStateException("Application release cannot be deleted until environment exists");
		}
		delete(applicationReleaseUID);
	}

	@Override
	public boolean canBeDeleted(String applicationReleaseUID) throws ApplicationReleaseNotFoundException {
		ApplicationRelease release = findApplicationReleaseByUID(applicationReleaseUID);
		Long envCount = manageEnvironment.countEnvironmentsByApplicationRelease(release.getUID());
		return release.getApplication().isEditable() && envCount == 0;
	}

	@Override
	public List<ApplicationRelease> findMyApplicationReleases() {
		return find(new Predicate<ApplicationRelease>() {
			@Override
			public boolean apply(ApplicationRelease release) {
				return release.getApplication().hasForMember(currentUser()) || currentUserIsAdmin();
			}
		});
	}

	@Override
	public List<ApplicationRelease> findApplicationReleasesByAppUID(final String applicationUid) throws ApplicationNotFoundException {
		return find(new Predicate<ApplicationRelease>() {
			@Override
			public boolean apply(ApplicationRelease release) {
				return release.getApplication().getUID().equals(applicationUid);
			}
		});
	}

	@Override
	public List<ApplicationRelease> findApplicationReleases(int firstIndex, int count) {
		return find(firstIndex, count);
	}

	@Override
	public List<ApplicationRelease> findMyApplicationReleases(int firstIndex, int count) {
		return find(firstIndex, count, new Predicate<ApplicationRelease>() {
			@Override
			public boolean apply(ApplicationRelease release) {
				return release.getApplication().hasForMember(currentUser());
			}
		});
	}

	@Override
	public long countApplicationReleases() {
		return count();
	}

	@Override
	public long countMyApplicationReleases() {
		return count(new Predicate<ApplicationRelease>() {
			@Override
			public boolean apply(ApplicationRelease release) {
				return release.getApplication().hasForMember(currentUser());
			}
		});
	}

	@Override
	public long countApplicationReleasesByAppUID(String applicationUid) {
		try {
			return findApplicationReleasesByAppUID(applicationUid).size();
		} catch (ApplicationNotFoundException exc) {
			return 0;
		}
	}

	public void setManageApplication(ManageApplicationMock manageApplication) {
		this.manageApplication = manageApplication;
	}

	@Override
	public List<ApplicationRelease> findApplicationReleasesByAppUID(final String applicationUID, int firstIndex, int count) {
		return find(firstIndex, count, new Predicate<ApplicationRelease>() {
			@Override
			public boolean apply(ApplicationRelease release) {
				return applicationUID.equals(release.getApplication().getUID()) && release.getApplication().isEditable();
			}
		});
	}

	@Override
	public ApplicationRelease findApplicationReleaseByApplicationAndReleaseVersion(final String appUID, final String releaseVersion)
			throws ApplicationReleaseNotFoundException {
		List<ApplicationRelease> releaseList = find(new Predicate<ApplicationRelease>() {
			@Override
			public boolean apply(ApplicationRelease release) {
				return release.getApplication().getUID().equals(appUID) && release.getReleaseVersion().equalsIgnoreCase(releaseVersion);
			}
		});

		if (releaseList.size() == 0) {
			throw new ApplicationReleaseNotFoundException("ApplicationRelease of application " + appUID + " with version " + releaseVersion
					+ " has not been found");
		}

		return releaseList.get(0);
	}

	@Override
	public boolean isReleaseVersionUniqueForApplication(String appUID, String searchReleaseVersion) {
		try {
			findApplicationReleaseByApplicationAndReleaseVersion(appUID, searchReleaseVersion);
		} catch (BusinessException e) {
			return true;
		}
		return false;

	}

	@Override
	public List<MiddlewareProfile> findAllMiddlewareProfil() {
		return Arrays.asList(MiddlewareProfile.values());
	}

    @Override
    public void purgeOldRemovedReleases() {
    }

    @Override
    public void deleteAndPurgeApplicationRelease(String uid) throws ApplicationReleaseNotFoundException {
        // mock is mock
    }

}
