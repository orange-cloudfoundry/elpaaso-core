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
import com.francetelecom.clara.cloud.core.service.exception.IllegalServiceCall;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Business implementation for TechnicalDeployment component
 * <p/>
 * All methods are defined as transactional. If no transaction in progress
 * during method call, then it will start a new transaction.
 */
@Service
public class ManageTechnicalDeploymentImpl implements ManageTechnicalDeployment {

    private static final Logger log = LoggerFactory.getLogger(ManageTechnicalDeploymentImpl.class);

    @Autowired
    private TechnicalDeploymentRepository technicalDeploymentRepository;

    @Autowired
    private TechnicalDeploymentInstanceRepository technicalDeploymentInstanceRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = {"BusinessException"})
    public void deploy(int technicalDeploymentId, String ssoId)
            throws ObjectNotFoundException {
        TechnicalDeployment technicalDeployment = find(technicalDeploymentId);
        if (technicalDeployment.getState().equals(
                TechnicalDeploymentStateEnum.UNDEPLOYED)) {
            technicalDeployment.setState(TechnicalDeploymentStateEnum.DEPLOYED);
        } else {
            String message = "Illegal operation call. operation = deploy(); technicalDeployment state  = "
                    + technicalDeployment.getState();
            throw new IllegalServiceCall(message);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT, rollbackForClassName = {"BusinessException"})
    public void undeploy(int technicalDeploymentId, String ssoId)
            throws ObjectNotFoundException {
        TechnicalDeployment technicalDeployment = find(technicalDeploymentId);
        if (technicalDeployment.getState().equals(
                TechnicalDeploymentStateEnum.DEPLOYED)) {
            technicalDeployment
                    .setState(TechnicalDeploymentStateEnum.UNDEPLOYED);
        } else {
            String message = "Illegal operation call. operation = undeploy(); technicalDeployment state  = "
                    + technicalDeployment.getState();
            throw new IllegalServiceCall(message);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public String findTechnicalDeployment(int technicalDeploymentId)
            throws ObjectNotFoundException, TechnicalException {
        TechnicalDeployment technicalDeployment = find(technicalDeploymentId);

        try {
            JAXBContext jc = JAXBContext
                    .newInstance(TechnicalDeployment.class);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            StringWriter st = new StringWriter();
            m.marshal(technicalDeployment, st);
            return st.toString();
        } catch (JAXBException e) {
            String message = "Data access error while retrieving TechnicalDeployment["
                    + technicalDeploymentId + "]";
            log.error(message, e);
            throw new TechnicalException(message, e);
        }
    }

    private TechnicalDeployment find(int technicalDeploymentId) throws ObjectNotFoundException {
        TechnicalDeployment technicalDeployment = technicalDeploymentRepository.findOne(technicalDeploymentId);
        if (technicalDeployment == null) {
            String message = "TechnicalDeployment[" + technicalDeploymentId + "] does not exist";
            log.error(message);
            throw new ObjectNotFoundException(message);
        }

        return technicalDeployment;
    }

    public void setTechnicalDeploymentRepository(TechnicalDeploymentRepository technicalDeploymentRepository) {
        this.technicalDeploymentRepository = technicalDeploymentRepository;
    }

    public void setTechnicalDeploymentInstanceRepository(TechnicalDeploymentInstanceRepository technicalDeploymentInstanceRepository) {
        this.technicalDeploymentInstanceRepository = technicalDeploymentInstanceRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public List<TechnicalDeployment> findTechnicalDeployments() {

        List<TechnicalDeploymentInstance> tdis = technicalDeploymentInstanceRepository.findAll();
        ArrayList<TechnicalDeployment> tds = new ArrayList<TechnicalDeployment>();

        for (TechnicalDeploymentInstance tdi : tdis) {
            tds.add(tdi.getTechnicalDeployment());
        }

        return tds;
    }

}
