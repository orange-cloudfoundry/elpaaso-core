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
package com.francetelecom.clara.cloud.logicalmodel.samplecatalog;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;

public class ElPaaSoLogicalTestModelCatalog extends ElPaaSoTomcatLogicalModelCatalog {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElPaaSoLogicalTestModelCatalog.class);
	private static final String DEVELOPMENT_PROPERTIES_FILE = "com/francetelecom/clara/cloud/commons/testconfigurations/diff/credentials-development_specific.properties";

	private LogicalConfigServiceUtils configServiceUtils = new LogicalConfigServiceUtils();

	@Override
	protected StringBuffer createConfigContent() {
		ResourceBundle contextProperties = null;
		if (getContextPropertiesPath() != null) {
			try {
				contextProperties = ResourceBundle.getBundle(getContextPropertiesPath());
			} catch (MissingResourceException e) {
				LOGGER.error("contextPropertiesPath property of ElPaaSoLogicalModelCatalog bean not found!", e);
			}
		}
		StringBuffer configBuffer = new StringBuffer();
		if (contextProperties != null) {
			Set<String> developmentSpecificPropertyKeys = null;
			try {
				developmentSpecificPropertyKeys = configServiceUtils.loadKeysFromFile(DEVELOPMENT_PROPERTIES_FILE);
			} catch (InvalidConfigServiceException e) {
				throw new TechnicalException("Problem while loading development specific properties", e);
			}

			for (String key : contextProperties.keySet()) {
				// Don't add development specific properties
				if (developmentSpecificPropertyKeys.contains(key)) {
					LOGGER.debug("Property {} is considered as development property and will be excluded from generated config string.", key);
				} else {
					configBuffer.append(key);
					configBuffer.append("=");
					configBuffer.append(contextProperties.getString(key));
					configBuffer.append("\n");
				}
			}

			final String passphrase = System.getenv("PAAS_ENCRYPTION_PASSWORD");
			if (passphrase != null && passphrase.length() > 0) {
				configBuffer.append("jasypt.secret=");
				configBuffer.append(passphrase);
				configBuffer.append("\n");
			}
		} else {
			configBuffer.append("#Please fill here content from a credentials.properties file\n");
		}
		return configBuffer;
	}
}
