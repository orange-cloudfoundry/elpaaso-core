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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.ServiceActivationStatus;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.techmodel.cf.*;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;

import java.util.UUID;

/**
 * Internal synchronous interface which provides a synthetic view to the
 * vcap_java_client SDK.
 */
public interface CfAdapter {

	/**
	 * Create an app.
	 * 
	 * @param app
	 *            the app
	 * @param spaceName
	 *            TODO
	 * @return TODO
	 */
	UUID createApp(App app, String spaceName);

	/**
	 * Start an app.
	 * 
	 * @param app
	 *            the app
	 * @param spaceName
	 *            TODO
	 */
	 void startApp(App app, String spaceName);

	/**
	 * Stop an app.
	 * 
	 * @param app
	 *            the app
	 * @param spaceName
	 *            TODO
	 */
	 void stopApp(App app, String spaceName) throws TechnicalException;

	/**
	 * Delete an app.
	 * 
	 * @param app
	 *            the app
	 * @param spaceName
	 *            TODO
	 */
	 void deleteApp(App app, String spaceName);

	/**
	 * 
	 * @param spaceName
	 *            TODO
	 * @return true if app exists
	 */
	 boolean appExists(String appName, String spaceName);

	/**
	 * 
	 * @param spaceName
	 *            TODO
	 * @return true if app is started
	 */
	 boolean isAppStarted(String appName, String spaceName);

	/**
	 * 
	 * @param spaceName
	 *            TODO
	 * @return true if app is stopped
	 */
	 boolean isAppStopped(String appName, String spaceName);

	/**
	 * Log diagnostics for an app: staging/crash logs, jonas logs, stats ...
	 * 
	 * @param appName
	 * @param spaceName
	 *            TODO
	 */
	void logAppDiagnostics(String appName, String spaceName);

	/**
	 * @param spaceName
	 *            TODO
	 * @return the number of instance properly started
	 */
	int peekAppStartStatus(int instanceCount, String appName, String spaceName);

	/**
	 * Create a service.
	 * 
	 * @param connector
	 *            the connector
	 * @param spaceName
	 *            TODO
	 */
	 void createService(UserProvidedService connector, String spaceName);

	 void createService(ManagedService service, String spaceName);

	/**
	 * unbind and delete service
	 * 
	 * @param serviceName
	 *            name of service
	 * @param spaceName
	 *            TODO
	 */
	 void deleteService(String serviceName, String spaceName);

	 void deleteAllServices(String spaceName);

	/**
	 * 
	 * Associate (provision) a service with an app.
	 * 
	 * @param appName
	 *            the app name
	 * @param serviceName
	 *            the service name
	 * @param spaceName
	 *            TODO
	 */
	 void bindService(String appName, String serviceName, String spaceName);

	/**
	 * Un-associate (unprovision) a service from an app.
	 * 
	 * @param appName
	 *            the app name
	 * @param serviceName
	 *            the service name
	 * @param spaceName
	 *            TODO
	 */
	 void unbindService(String appName, String serviceName, String spaceName);

	/**
	 * 
	 * @param spaceName
	 *            TODO
	 * @return true if service exists
	 */
	 boolean serviceExists(String serviceName, String spaceName);

	/**
	 * 
	 * @param spaceName
	 *            TODO
	 * @return true if service is bound to app
	 */
	 boolean isServiceBound(String appName, String serviceName, String spaceName);

	/**
	 * create space
	 * 
	 * @param spaceName
	 *            the space
	 */
	 void createSpace(SpaceName spaceName);

	/**
	 * delete space
	 * 
	 * @param spaceName
	 *            the space name
	 */
	 void deleteSpace(SpaceName spaceName);

	/**
	 * 
	 * @return true if space exists
	 */
	 boolean spaceExists(SpaceName spaceName);

	/**
	 * assign manager role to current user for space
	 * 
	 * @param spaceName
	 *            the space name
	 */
	 void associateManagerWithSpace(SpaceName spaceName);

	String getCurrentOrganizationName();

	ServiceActivationStatus getServiceInstanceState(String serviceName, String spaceName);

	interface AppConflictHandler {

		String getNextUriCandidate(String uri, int attempts);
	}

	 int getHttpProxyPort();

	 String getHttpProxyHost();

	 String getSpace();

	 void associateDeveloperWithSpace(SpaceName spaceName);

	 void associateAuditorWithSpace(SpaceName spaceName);

	 SpaceName getValidSpaceName(String spaceSuffix);

	 boolean domainExists(String domainName, String spaceName);
	
	 void addDomain(String domainName, String spaceName);
	
	 boolean routeExists(Route route, String spaceName);

	 void deleteRoute(Route route, String spaceName);

	 RouteUri createRoute(Route route, String spaceName);

}
