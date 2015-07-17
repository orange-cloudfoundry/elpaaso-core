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
package com.francetelecom.clara.cloud.environment.log;

import java.net.URL;

public interface LogService {

	/**
	 * get app logs url
	 * 
	 * @param the
	 *            app name
	 * @return logs url
	 */
	public URL getAppLogsUrl(String appName);

	/**
	 * get route logs url
	 * 
	 * @param the
	 *            route name
	 * @return logs url
	 */
	public URL getRouteLogsUrl(String routeName);

	/**
	 * get environment logs url
	 * 
	 * @param whole
	 *            app names held by the environment
	 * @return logs url
	 */
	public URL getEnvironmentLogsUrl(String... appName);

}
