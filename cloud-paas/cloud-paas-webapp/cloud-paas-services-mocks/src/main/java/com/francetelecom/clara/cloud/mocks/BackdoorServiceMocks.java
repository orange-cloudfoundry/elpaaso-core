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
package com.francetelecom.clara.cloud.mocks;

import org.springframework.stereotype.Service;

import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.service.backdoor.BackdoorService;
import com.francetelecom.clara.cloud.coremodel.exception.EnvironmentNotFoundException;

@Service("backdoorService")
public class BackdoorServiceMocks implements BackdoorService {

	@Override
	public Environment findEnvironmentByUID(String uid) throws EnvironmentNotFoundException {
		throw new EnvironmentNotFoundException();
	}

	@Override
	public Environment findEnvironmentByApplicationReleaseAndLabel(String releaseUid, String environmentLabel) throws EnvironmentNotFoundException {
		throw new EnvironmentNotFoundException();
	}

	@Override
	public void createPaasUser(String ssoId, String username, String mail) {
	}

}
