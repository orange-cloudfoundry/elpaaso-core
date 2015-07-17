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
package com.francetelecom.clara.cloud.service.backdoor;

import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.core.domain.PaasUserRepository;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.PaasRoleEnum;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.technicalservice.exception.EnvironmentNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

public class BackdoorServiceImpl implements BackdoorService {

	private static final Logger log = LoggerFactory.getLogger(BackdoorServiceImpl.class);

	private EnvironmentRepository environmentRepository;

	private PaasUserRepository paasUserRepository;

	public void setEnvironmentRepository(EnvironmentRepository repository) {
		this.environmentRepository = repository;
	}

	@Override
	public Environment findEnvironmentByUID(String uid) throws EnvironmentNotFoundException {
		Environment environment = environmentRepository.findByUID(uid);
		if (environment == null) {
			String message = "Environment with uid[" + uid + "] does not exist";
			log.info(message);
			throw new EnvironmentNotFoundException(message);
		} else {
			return environment;
		}
	}

	@Override
	public Environment findEnvironmentByApplicationReleaseAndLabel(String releaseUid, String environmentLabel) throws EnvironmentNotFoundException {
		Environment environment = environmentRepository.findByApplicationReleaseUIDAndLabel(releaseUid, environmentLabel);
		if (environment == null) {
			String message = "Environment with label[" + environmentLabel + "] of release with uid[" + releaseUid + "] does not exist";
			log.info(message);
			throw new EnvironmentNotFoundException(message);
		} else {
			return environment;
		}
	}

	@Override
	@Transactional
	public void createPaasUser(String ssoId, String username, String mail) {

		if (paasUserRepository.findBySsoId(new SSOId(ssoId)) == null) {
			log.info("Creating user {} (ssoid : {})", username, ssoId);
			PaasUser paasUser = new PaasUser(username, username, new SSOId(ssoId), mail);
			paasUser.setPaasUserRole(PaasRoleEnum.ROLE_ADMIN);
			paasUserRepository.save(paasUser);

		} else {
			log.info("Failed to create user {} (ssoid: {}) already exists", username, ssoId);
		}

	}

	public void setPaasUserRepository(PaasUserRepository repository) {
		this.paasUserRepository = repository;
	}

}
