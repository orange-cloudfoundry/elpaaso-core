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
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;

import java.util.List;

/**
 * Paas user management service facade interface.
 * 
 * 
 * @author Clara
 */
public interface ManagePaasUser {

	/**
	 * Retrieve a paas user from its ssoId.
	 * 
	 * @param ssoId
	 *            paas user ssoId
	 * @return paas user
	 * @throws ObjectNotFoundException
	 *             if paas user does not exist
	 */
	public PaasUser findPaasUser(String ssoId) throws ObjectNotFoundException;

	/**
	 * Delete a paas user.
	 * 
	 * @param paasUserId
	 *            id of paas user to be deleted
	 * @throws ObjectNotFoundException
	 *             if paas user to be deleted does not exist
	 */
	public void deletePaasUser(int paasUserId) throws BusinessException;

	/**
	 * Retrieve a paas user from its id.
	 * 
	 * @param paasUserId
	 *            id of paas user to be consulted
	 * @throws ObjectNotFoundException
	 *             if paas user does not exist
	 */
	public PaasUser findPaasUser(int paasUserId) throws ObjectNotFoundException;

	/**
	 * Update a paas user.
	 * 
	 * @param paasUser
	 *            paas user to be updated
	 * @throws ObjectNotFoundException
	 *             if paas user to be updated does not exist
	 */
	void updatePaasUser(PaasUser paasUser) throws ObjectNotFoundException;


    /**
     * Retrieve all Paas User
     *
     * @return a list of Paas Users
    */
    public List<PaasUser> findAllPaasUsers();

    /**
     * Try to find the pUsr before persist in Database to avoid Constraints Violation Exception
     * @param pUsr
     */
    public void checkBeforeCreatePaasUser(PaasUser pUsr);

    /**
     * persist paasUser in database. checkBeforeCreatePaasUser should be prefered to be avoid Constraints Violation Exception
     * @param paasUser
     */
    public void createPaasUser(PaasUser paasUser);

}
