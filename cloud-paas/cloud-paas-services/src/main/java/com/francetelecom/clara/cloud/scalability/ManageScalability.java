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
package com.francetelecom.clara.cloud.scalability;

import java.util.Collection;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;

/**
 * ManageScalability
 * Class that provide a way to populate the PaaS with some scalability.scalability.scalability scenarii
 *
 * ---In progress---
 *
 * Sample usage :
 * Last update  : $LastChangedDate$
 *
 * @author : $Author$
 * @version : $Revision$
 * @link http://elpaaso_shp/index.php/Scalability_service
 * @link http://10.177.111.51/projects/el_paso_project/cards/1244
 */
public interface ManageScalability {
    void razData() throws BusinessException;

    void razDataWithActivation() throws BusinessException;

    /**
     * this method will create (nbToCreate) users
     * @param namePrefix
     * @param nbToCreate
     * @return
     */
    Collection<PaasUser> createPaasUsers(String namePrefix, int nbToCreate);

    /**
     * this method will create a fake team (paas users) : 5 people (mgr, arch, dev1, dev2, qa)
     * @param namePrefix
     * @return
     */
    Collection<PaasUser> createTeam(String namePrefix);

    /**
     * this method will create a single app & app release for test purpose (ie. portal), if createEnv is true, then a DEV env will be created.
     */
    Application populateSimpleTestPhase(PaasUser author, boolean createEnv) throws BusinessException;

    /**
     * this method will create sample data : 5 users; 5 apps; 5×5 releases with logical models of the following size; 1 exec nodes; 1 DBs; 1 web gui; no env
     */
    Collection<ApplicationRelease> populatePortalPhase(int nbApp, int nbReleasePerApp, int nbEnvPerRelease)
            throws BusinessException;

    /**
     *
     * @param pattern a string that include :
     *         - 'G' to create a gui
     *         - 'N' to create an execution node,
     *         - 'D' to create a relational database
     *         - 'S' to create an online store
     * @param teamName name of the set of sample users
     * @param nbApp number of application to create
     * @param nbReleasePerApp number of release per app to create
     * @param nbEnvPerRelease number of environment per release to create
     * @return Collection of created ApplicationRelease
     * @throws BusinessException
     */
	Collection<ApplicationRelease> populate(String pattern, String teamName, int nbApp, int nbReleasePerApp, int nbEnvPerRelease) throws BusinessException;

}
