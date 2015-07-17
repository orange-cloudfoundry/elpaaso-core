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
package com.francetelecom.clara.cloud.activation.plugin.cf.domain;

import com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure.RouteActivationServiceDefault.RouteAlreadyExists;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;

/**
 * route activation service
 *
 */
public interface RouteActivationService {

	/**
	 * Activate a route.
	 * 
	 * @param route
	 *            the route
	 * @return route uri that has eventually been set (if collision exists in CF)
	 * @throws RouteAlreadyExists 
	 */
	public RouteUri activate(Route route) throws RouteAlreadyExists;

	/**
	 * Delete a route.
     * Will not attempt to delete route if route is missing.
     *
     * @param route
	 *            the route
	 */
	public void delete(Route route);

}
