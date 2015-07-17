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
package com.francetelecom.clara.cloud.service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.francetelecom.clara.cloud.commons.DateHelper;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;

/**
 * OpsServiceImpl
 */
public class OpsServiceImpl implements OpsService {
	private final static Logger logger = LoggerFactory.getLogger(OpsServiceImpl.class.getName());
	private ManageEnvironment manageEnvironment;
	private ManageApplicationRelease manageRelease;
	private ManageApplication manageApplication;
	private String buildVersion;
	private String buildDate;
	private String buildUser;

	@Override
	public void purgeDatabase() {
		//we must be authenticated as admin to run purge against applications, releases and environments
		AuthenticationHelper authenticationHelper = new AuthenticationHelper();
		authenticationHelper.loginAsAdmin();
		String action = "purge database : old environments";
		try {
			purgeEnvironments();
			action = "purge database : old releases";
			purgeReleases();
			action = "purge database : old applications";
			purgeApplications();
		} catch (Throwable throwable) {
			logger.error("Exception while {} : {}", action, throwable.getMessage());
			logger.error("Purge exception:", throwable);
			throwable.printStackTrace();
		} finally {
			authenticationHelper.logout();
		}
	}

	private void purgeApplications() {
		manageApplication.purgeOldRemovedApplications();
	}

	private void purgeReleases() {
		manageRelease.purgeOldRemovedReleases();
	}

	private void purgeEnvironments() {
		List<Environment> oldRemovedEnvironments = manageEnvironment.findOldRemovedEnvironments();
		for (Environment e : oldRemovedEnvironments) {
			try {
				manageEnvironment.purgeRemovedEnvironment(e.getUID());
			} catch (Throwable e1) {
				logger.error("Unable to purge environment {}", e.toString());
				e1.printStackTrace();
			}
		}
	}

	public void setManageApplication(ManageApplication manageApplication) {
		this.manageApplication = manageApplication;
	}

	public void setManageEnvironment(ManageEnvironment manageEnvironment) {
		this.manageEnvironment = manageEnvironment;
	}

	public void setManageRelease(ManageApplicationRelease manageRelease) {
		this.manageRelease = manageRelease;
	}

	public String getServerDate() {
		String dateLogFormat = DateHelper.getDateLogFormat(DateHelper.getNow());
		return dateLogFormat;
	}

	public void setBuildDate(String buildDate) {
		this.buildDate = buildDate;
	}

	public String getBuildDate() {
		return buildDate;
	}

	public void setBuildUser(String buildUser) {
		this.buildUser = buildUser;
	}

	public String getBuildUser() {
		return buildUser;
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	private class AuthenticationHelper {

		public void loginAsAdmin() {
			SecurityContextHolder.getContext().setAuthentication(new Authentication() {

				@Override
				public String getName() {
					return "ops";
				}

				@Override
				public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
				}

				@Override
				public boolean isAuthenticated() {
					return true;
				}

				@Override
				public Object getPrincipal() {
					return null;
				}

				@Override
				public Object getDetails() {
					return null;
				}

				@Override
				public Object getCredentials() {
					return null;
				}

				@Override
				public Collection<? extends GrantedAuthority> getAuthorities() {
					return Arrays.asList(new SimpleGrantedAuthority(PaasRoleEnum.ROLE_ADMIN.toString()));

				}
			});
		}

		public void logout() {
			SecurityContextHolder.getContext().setAuthentication(null);
		}

	}

}
