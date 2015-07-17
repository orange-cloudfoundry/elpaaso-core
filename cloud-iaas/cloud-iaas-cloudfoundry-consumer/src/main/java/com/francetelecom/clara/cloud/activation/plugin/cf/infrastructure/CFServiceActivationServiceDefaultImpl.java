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
package com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure;

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.CFServiceActivationService;
import com.francetelecom.clara.cloud.activation.plugin.cf.domain.ServiceActivationStatus;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.techmodel.cf.UserProvidedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class CFServiceActivationServiceDefaultImpl implements CFServiceActivationService {

    private static Logger LOGGER = LoggerFactory.getLogger(CFServiceActivationServiceDefaultImpl.class.getName());

    private CfAdapter cfAdapter;

    @Autowired
    public CFServiceActivationServiceDefaultImpl(CfAdapter cfAdapter) {
        this.cfAdapter = cfAdapter;
    }

	@Override
    public ServiceActivationStatus activate(final UserProvidedService service) {
        LOGGER.info("creating cloud foundry service <" + service.getServiceName() + ">");
        if (cfAdapter.serviceExists(service.getServiceName(), service.getSpace().getValue())) {
			throw new ServiceAlreadyExists("a service <" + service.getServiceName() + "> already exists in space <" + service.getSpace() + ">");
		}
		cfAdapter.createService(service, service.getSpace().getValue());
        return ServiceActivationStatus.ofService(service.getServiceName(), service.getSpace().getValue()).hasSucceeded();
    }

    @Override
    public ServiceActivationStatus activate(final ManagedService service) {
        LOGGER.info("creating cloud foundry service <" + service.getServiceInstance() + ">...");
        if (cfAdapter.serviceExists(service.getServiceInstance(), service.getSpace().getValue())) {
            throw new ServiceAlreadyExists("a service <" + service.getServiceInstance() + "> already exists in space <" + service.getSpace() + ">");
        }
        cfAdapter.createService(service, service.getSpace().getValue());
        return cfAdapter.getServiceInstanceState(service.getServiceInstance(), service.getSpace().getValue());
    }

    @Override
    public ServiceActivationStatus delete(final UserProvidedService service) {
        String serviceName = service.getServiceName();
        String spaceName = service.getSpace().getValue();
        LOGGER.info("deleting cloud foundry service <" + serviceName + ">");
        if (!cfAdapter.serviceExists(serviceName, spaceName)) {
            LOGGER.warn("will not delete service<" + serviceName + ">. service<" + serviceName + "> does no exist.");
        } else {
            cfAdapter.deleteService(serviceName, spaceName);
        }
        return ServiceActivationStatus.ofService(serviceName, spaceName).hasSucceeded();
    }

    @Override
    public ServiceActivationStatus delete(ManagedService service) {
        String serviceName = service.getServiceInstance();
        String spaceName = service.getSpace().getValue();
        LOGGER.info("deleting cloud foundry service <{}> in space <{}>", serviceName, spaceName);
        if (!cfAdapter.serviceExists(serviceName, spaceName)) {
            LOGGER.warn("will not delete service<" + serviceName + ">. service<" + serviceName + "> does no exist.");
            return ServiceActivationStatus.ofService(serviceName, spaceName).hasSucceeded();
        } else {
            cfAdapter.deleteService(serviceName, spaceName);
            return cfAdapter.getServiceInstanceState(serviceName, spaceName);
        }
    }

    @Override
    public ServiceActivationStatus getServiceActivationStatus(final ServiceActivationStatus status) {
        LOGGER.info("getting activation status of service <" + status.getServiceName() + "> ");
        return cfAdapter.getServiceInstanceState(status.getServiceName(), status.getSpaceName());
    }

    public class ServiceAlreadyExists extends TechnicalException {

        public ServiceAlreadyExists(String message) {
            super(message);
        }

    }

}
