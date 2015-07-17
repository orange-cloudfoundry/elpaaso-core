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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.RouteActivationService;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;

public class RouteActivationServiceDefault implements RouteActivationService {

	private static Logger LOGGER = LoggerFactory.getLogger(RouteActivationServiceDefault.class.getName());

	private CfAdapter cfAdapter;

	@Autowired
	public RouteActivationServiceDefault(CfAdapter cfAdapter) {
		this.cfAdapter = cfAdapter;
	}


	@Override
	public void delete(final Route route) {
		LOGGER.info("deleting cloud foundry route <" + route + ">");
		if (!cfAdapter.routeExists(route, route.getSpace().getValue())) {
			LOGGER.warn("will not delete route<" + route + ">. route<" + route + "> does no exist.");
		} else {
			cfAdapter.deleteRoute(route, route.getSpace().getValue());
		}
	}

	public class RouteAlreadyExists extends BusinessException {

		public RouteAlreadyExists(String message) {
			super(message);
		}

	}

	@Override
	public RouteUri activate(Route route) throws RouteAlreadyExists {
		// We need to register any subdomain, otherwise the route is
		// rejected by
		// vcap-java-client
		if (!cfAdapter.domainExists(route.getDomain(),route.getSpace().getValue())) {
			try {
				cfAdapter.addDomain(route.getDomain(),route.getSpace().getValue());
				LOGGER.info("Added subdomain " + route.getDomain() + " to register " + route);
			} catch (Exception e) {
                LOGGER.warn("Unable to create missing domain " + route.getDomain() + " into " + route.getSpace().getValue() + " Possibly because its a private share domain, not yet listed by cf-java-client 1.11 ?");
				throw new TechnicalException("Unable to register subdomain: " + route.getDomain() + " caught:" + e
						+ " Please check another Paas instance/test has not reserved the same domain on the same CF instance.", e);
			}
		}

		LOGGER.info("creating cloud foundry route <" + route + ">");
		if (cfAdapter.routeExists(route,route.getSpace().getValue())) {
			String message = "a route " + route.getUri() + " already exists in space " + route.getSpace().getValue();
			LOGGER.info(message);
			throw new RouteAlreadyExists(message);
		}
		return cfAdapter.createRoute(route, route.getSpace().getValue());
	}

}
