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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ConfigRole;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.services.dto.ApplicationDTO;
import com.francetelecom.clara.cloud.services.dto.ConfigOverrideDTO;
import com.francetelecom.clara.cloud.technicalservice.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.ConfigRoleNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.technicalservice.exception.InvalidConfigOverrideException;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.PaasUserNotFoundException;
import com.google.common.base.Predicate;

/**
 * Created by IntelliJ IDEA. User: lzxv3002 Date: 07/06/11 Time: 18:06 Mock for
 * portal use without database
 */
@Service("manageApplication")
public class ManageApplicationMock extends CoreItemServiceMock<Application> implements ManageApplication {

	@Autowired
	private ManageApplicationRelease manageApplicationRelease;

	@Override
	public List<Application> findApplications() {
		return findAll();
	}

	@Override
	public List<Application> findApplications(int first, int count, String sortProperty, String sortType) {
		return find(first, count);
	}

	@Override
	public List<Application> findMyApplications() {
		return find(new Predicate<Application>() {
			@Override
			public boolean apply(Application app) {
				return app.hasForMember(currentUser()) || currentUserIsAdmin();
			}
		});
	}

	@Override
	public List<Application> findMyApplications(int first, int count, String sortProperty, String sortType) {
		return find(first, count, new Predicate<Application>() {
			@Override
			public boolean apply(Application app) {
				return app.hasForMember(currentUser());
			}
		});
	}

	@Override
	public String createPublicApplication(String code, String label, String description, URL applicationRegistryUrl, SSOId... members)
			throws DuplicateApplicationException {
		Application application = new ApplicationMock(label, code);
		application.setDescription(description);
		if (applicationRegistryUrl != null) {
			application.setApplicationRegistryUrl(applicationRegistryUrl);
		}
		try {
			create(application);
		} catch (DuplicateApplicationReleaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return application.getUID();
	}

	@Override
	public String createPrivateApplication(String code, String label, String description, URL applicationRegistryUrl, SSOId... ssoIds)
			throws DuplicateApplicationException, PaasUserNotFoundException {
		Application application = new ApplicationMock(label, code);
		application.setDescription(description);
		if (applicationRegistryUrl != null) {
			application.setApplicationRegistryUrl(applicationRegistryUrl);
		}
		application.setAsPrivate();
		application.setMembers(new HashSet<SSOId>(Arrays.asList(ssoIds)));
		
		try {
			create(application);
		} catch (DuplicateApplicationReleaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return application.getUID();
	}

	@Override
	public void deleteApplication(String applicationUID) throws ApplicationNotFoundException {
		if (!canBeDeleted(applicationUID)) {
			throw new IllegalStateException("Application cannot be deleted until application release exists");
		}
		delete(applicationUID);
	}

	@Override
	public boolean canBeDeleted(String applicationUID) throws ApplicationNotFoundException {
		return findApplicationByUID(applicationUID).isEditable() && manageApplicationRelease.countApplicationReleasesByAppUID(applicationUID) == 0;
	}

	@Override
	public Application findApplicationByUID(String applicationUID) throws ApplicationNotFoundException {
		Application application = null;
		try {
			application = findByUID(applicationUID);
		} catch (ObjectNotFoundException e) {
			throw new ApplicationNotFoundException(e);
		}
		return application;
	}

	@Override
	public Application updateApplication(Application application) throws ApplicationNotFoundException, DuplicateApplicationException {
		try {
			return update(application);
		} catch (ObjectNotFoundException e) {
			throw new ApplicationNotFoundException(e);
		}
	}

	@Override
	public long countApplications() {
		return count();
	}

	@Override
	public long countMyApplications() {
		return count(new Predicate<Application>() {
			@Override
			public boolean apply(Application app) {
				return app.hasForMember(currentUser());
			}
		});
	}

	@Override
	public boolean isApplicationLabelUnique(String searchAppLabel) throws BusinessException {
		boolean isUnique = false;

		try {
			findApplicationByLabel(searchAppLabel);
		} catch (ApplicationNotFoundException e) {
			isUnique = true;
			return isUnique;
		}

		return isUnique;
	}

	@Override
	public ApplicationDTO findApplicationByLabel(String searchLabel) throws ApplicationNotFoundException {

		List<Application> appList = (List<Application>) findApplications();
		List<ApplicationDTO> applicationDTOs = new ArrayList<ApplicationDTO>();

		for (Application app : appList) {
			if (app.getLabel().equalsIgnoreCase(searchLabel)) {
				applicationDTOs.add(new ApplicationDTO(app.getUID(), app.getCode(), app.getLabel(), app.getDescription(), app
						.getApplicationRegistryUrl()));
			}
		}

		if (applicationDTOs.size() == 0) {
			throw new ApplicationNotFoundException("Application with label " + searchLabel + " has not been found");
		}
		return applicationDTOs.get(0);

	}

	@Override
	public int purgeOldRemovedApplications() {
		return 0;
	}

	@Override
	public int deleteAndPurgeApplication(String uid) throws ApplicationNotFoundException {
		return 0;
	}

	@Override
	public List<Application> findAccessibleApplications() {
		return find(new Predicate<Application>() {
			@Override
			public boolean apply(Application app) {
				return app.isEditable() || app.isPublic();
			}
		});
	}

	@Override
    public String createConfigRole(String applicationUID, String configRoleLabel, List<ConfigOverrideDTO> overrideConfigs)
            throws ApplicationNotFoundException, InvalidConfigOverrideException {
		// TODO Auto-generated method stub
		return null;
	}

    @Override
    public ConfigRole findConfigRole(String configRoleUID) throws ConfigRoleNotFoundException {
        return null;
    }

}
