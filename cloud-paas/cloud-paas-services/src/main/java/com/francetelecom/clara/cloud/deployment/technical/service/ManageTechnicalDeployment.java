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
package com.francetelecom.clara.cloud.deployment.technical.service;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;

import java.util.List;

/**
 * Technical deployment management service facade interface.
 */
public interface ManageTechnicalDeployment {

    /**
     * Retrieves a technical deployment in XML format
     *
     * @param technicalDeploymentId id of searched technical deployment
     * @return an XML dump of the requested technical deployment.
     * @throws ObjectNotFoundException if searched technical deployment does not exist
     * @throws TechnicalException      if technical error during retrieval
     */
    String findTechnicalDeployment(int technicalDeploymentId) throws ObjectNotFoundException, TechnicalException;

    /**
     * Deploys a technical deployment.
     *
     * @param technicalDeploymentId id of technical deployment that must be deployed
     * @param ssoId                 pass user ssoId
     * @throws ObjectNotFoundException if pass user does not exist
     */
    void deploy(int technicalDeploymentId, String ssoId) throws ObjectNotFoundException;

    /**
     * Undeploys a technical deployment
     *
     * @param technicalDeploymentId id of technical deployment that must be deployed
     * @param ssoId                 pass user ssoId
     * @throws ObjectNotFoundException if pass user does not exist
     */
    void undeploy(int technicalDeploymentId, String ssoId) throws ObjectNotFoundException;

    /**
     * Find all Technical Deployments
     *
     * @return
     */
    List<TechnicalDeployment> findTechnicalDeployments();

}
