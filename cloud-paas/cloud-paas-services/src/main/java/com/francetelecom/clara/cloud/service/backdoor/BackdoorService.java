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
package com.francetelecom.clara.cloud.service.backdoor;

import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.exception.EnvironmentNotFoundException;

public interface BackdoorService {

	/**
	 * Find an environment by its uid.
	 * 
	 * @param uid
	 *            environment uid
	 * @return environment
	 * @throws EnvironmentNotFoundException
	 *             when environment does not exists
	 */
	Environment findEnvironmentByUID(String uid) throws EnvironmentNotFoundException;

	/**
	 * Find an environment by release uid and environment label.
	 * 
	 * @param releaseUid
	 *            application release uid
	 * @param environmentLabel
	 *            environment label
	 * @return environment
	 * @throws EnvironmentNotFoundException
	 *             when environment does not exists
	 */
	Environment findEnvironmentByApplicationReleaseAndLabel(String releaseUid, String environmentLabel) throws EnvironmentNotFoundException;

	void createPaasUser(String ssoId, String username, String mail);

}
