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
package com.francetelecom.clara.cloud.coremodel;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

/**
 * List of currently defined MiddlewareProfiles.
 * 
 * Profiles are sortable by ordinal (i.e. order of definition in this enum)
 *
 */
public enum MiddlewareProfile {
	V200_CF("2.0.0-cf", MiddlewareProfileStatus.SUPPORTED),V210_CF("2.1.0-cf", MiddlewareProfileStatus.EXPERIMENTAL);

	public static final String DEFAULT_PROFILE = "2.0.0-cf";

	public static MiddlewareProfile getDefault() {
		return fromVersion(DEFAULT_PROFILE);
	}

	/**
	 * Filter a collection of MiddlewareProfile to collect only visible ones
	 * (based on user parameter)
	 * 
	 * @param user
	 * @param profiles
	 * @return
	 */
	public static Set<MiddlewareProfile> filter(final PaasUser user, Collection<MiddlewareProfile> profiles) {
		return Sets.newHashSet(Iterables.filter(profiles, new Predicate<MiddlewareProfile>() {
			@Override
			public boolean apply(MiddlewareProfile profile) {
				return profile.getStatus().isAllowedFor(user.getPaasUserRole());
			}
		}));
	}

	public static MiddlewareProfile fromVersion(String version) {
		for (MiddlewareProfile profile : MiddlewareProfile.values()) {
			if (profile.getVersion().equals(version)) {
				return profile;
			}
		}
		return null;
	}

	/**
	 * Enum defining existing profile status and a set of role allowed to access
	 * them
	 */
	public enum MiddlewareProfileStatus {
		INTERNAL, 
 DEPRECATED(PaasRoleEnum.ROLE_ADMIN),
		SUPPORTED(PaasRoleEnum.values()), 
 EXPERIMENTAL(PaasRoleEnum.ROLE_ADMIN);

		private PaasRoleEnum[] allowedRoles;

		private MiddlewareProfileStatus(PaasRoleEnum... allowedRoles) {
			this.allowedRoles = allowedRoles;
		}

		private boolean isAllowedFor(PaasRoleEnum role) {
			for (PaasRoleEnum allowedRole : allowedRoles) {
				if (allowedRole == role) {
					return true;
				}
			}
			return false;
		}
	}

	private String label;
	private MiddlewareProfileStatus status;

	private MiddlewareProfile(String label, MiddlewareProfileStatus status) {
		this.label = label;
		this.status = status;
	}

	public String getVersion() {
		return label;
	}

	public MiddlewareProfileStatus getStatus() {
		return status;
	}
}
