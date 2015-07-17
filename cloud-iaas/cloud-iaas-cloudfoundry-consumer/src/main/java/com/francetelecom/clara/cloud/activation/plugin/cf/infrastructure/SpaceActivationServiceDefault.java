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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.SpaceActivationService;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;

public class SpaceActivationServiceDefault implements SpaceActivationService {

	private static Logger LOGGER = LoggerFactory.getLogger(SpaceActivationServiceDefault.class.getName());

	private CfAdapter cfAdapter;

	@Autowired
	public SpaceActivationServiceDefault(CfAdapter cfAdapter) {
		this.cfAdapter = cfAdapter;
	}

	@Override
	public SpaceName activate(String spaceSuffix) {

		SpaceName spaceName = cfAdapter.getValidSpaceName(spaceSuffix);

		cfAdapter.createSpace(spaceName);

		cfAdapter.associateManagerWithSpace(spaceName);
		cfAdapter.associateAuditorWithSpace(spaceName);
		cfAdapter.associateDeveloperWithSpace(spaceName);

		return spaceName;
	}

	@Override
	public void delete(SpaceName spaceName) {
		if (!cfAdapter.spaceExists(spaceName)) {
			LOGGER.warn("will not delete cloud foundry space<" + spaceName + ">. space does no exist.");
		} else {
			cfAdapter.deleteSpace(spaceName);
		}
	}

}
