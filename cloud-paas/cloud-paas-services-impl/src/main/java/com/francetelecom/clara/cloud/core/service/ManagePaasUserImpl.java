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
package com.francetelecom.clara.cloud.core.service;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.domain.EnvironmentRepository;
import com.francetelecom.clara.cloud.core.domain.PaasUserRepository;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.PaasUserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Business implementation for PaasUser management
 * 
 * All methods are defined as transactional. If no transaction is in progress
 * during method call, then it will start a new transaction.
 * 
 * @author Clara
 */
public class ManagePaasUserImpl implements ManagePaasUser {

	private static final Logger log = LoggerFactory.getLogger(ManagePaasUserImpl.class);

	@Autowired(required = true)
	private PaasUserRepository paasUserRepository;

	@Autowired(required = true)
	private EnvironmentRepository environmentRepository;

	public void setPaasUserRepository(PaasUserRepository repository) {
		this.paasUserRepository = repository;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public PaasUser findPaasUser(String ssoId) throws ObjectNotFoundException {
		log.debug("/******* recherche du paasUser - ssoId[" + ssoId + "] **********/");
		PaasUser passUser = paasUserRepository.findBySsoId(new SSOId(ssoId));
		if (passUser == null) {
			String message = "PaasUser[" + ssoId + "] does not exist";
			log.debug(message);
			throw new PaasUserNotFoundException(message);
		}
		return passUser;
	}

	public void createPaasUser(PaasUser paasUser) {
		log.debug("/******* creation du paasUser - ssoID[" + paasUser.getSsoId() + "] **********/");
		paasUserRepository.save(paasUser);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public void deletePaasUser(int paasUserId) throws BusinessException {
        log.debug("find user");
		PaasUser paasUser = paasUserRepository.findOne(paasUserId);
		if (paasUser == null) {
			String message = "PaasUser[" + paasUserId + "] does not exist";
			log.error(message);
			throw new PaasUserNotFoundException(message);
		}
        log.debug("find user envs");
		if (environmentRepository.countActiveByOwner(paasUser) > 0) {
			throw new BusinessException("You cannot delete user id=" + paasUserId + " until active environments exists");
		}
        List<Environment> userRemovedEnvs = environmentRepository.findAllByOwner(paasUser);
        environmentRepository.purgeEnvironments(userRemovedEnvs);
		paasUserRepository.delete(paasUser);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public PaasUser findPaasUser(int paasUserId) throws ObjectNotFoundException {
		log.debug("/******* recherche du paasUser - ID[" + paasUserId + "] **********/");
		PaasUser entity = paasUserRepository.findOne(paasUserId);
		if (entity == null) {
			String message = "PaasUser[" + paasUserId + "] does not exist";
			log.debug(message);
			throw new PaasUserNotFoundException(message);
		}
		return entity;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public void updatePaasUser(PaasUser paasUser) throws ObjectNotFoundException {
		PaasUser persisted = paasUserRepository.findOne(paasUser.getId());
		if (persisted == null) {
			String message = "PaasUser[" + paasUser.getFullName() + "] does not exist";
			log.error(message);
			throw new PaasUserNotFoundException(message);
		}
		persisted.setFirstName(paasUser.getFirstName());
		persisted.setLastName(paasUser.getLastName());
		persisted.setMail(paasUser.getMail());
		persisted.setPhone(paasUser.getPhone());
		persisted.setSsoId(paasUser.getSsoId());
		persisted.setMail(paasUser.getMail());
		// Flush to get potential exception
		// TODO
		// paasUserRepository.flush();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
	public List<PaasUser> findAllPaasUsers() {
		log.debug("/******* Looking for all  paasUsers **********/");
		List<PaasUser> passUserList = paasUserRepository.findAll();
		if (passUserList == null) {
			log.warn("No PassUser are registred");
			passUserList = new ArrayList<PaasUser>();
		}
		return passUserList;
	}

	/**
	 * Try to find the pUsr before persist in Database to avoid Constraints
	 * Violation Exception
	 * 
	 * @param pUsr
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = { "BusinessException" })
	public void checkBeforeCreatePaasUser(PaasUser pUsr) {
		try {
			PaasUser user = findPaasUser(pUsr.getSsoId().getValue());
			// Update information
			user.setFirstName(pUsr.getFirstName());
			user.setLastName(pUsr.getLastName());
			user.setMail(pUsr.getMail());
			user.setPaasUserRole(pUsr.getPaasUserRole());
			user.setSubTenant(pUsr.getSubTenant());
			log.debug("PaasUser " + pUsr.getSsoId() + " found ==> No need te recreate one, just update informations about it", pUsr.getFullName());
		} catch (ObjectNotFoundException ex) {
			createPaasUser(pUsr);
			log.debug("No paasUser " + pUsr.getSsoId() + " found ==> Create one ", ex.getCause());
			log.debug(findAllPaasUsers().toString());
		}
	}

}
