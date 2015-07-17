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

import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

import static org.junit.Assert.assertTrue;

public class ElPaaSoLogicalModelCatalogTest {
    protected static Logger LOG = LoggerFactory.getLogger(ElPaaSoLogicalModelCatalogTest.class);

	private static final String CONTEXTPROPERTIES_PATH = "com/francetelecom/clara/cloud/commons/testconfigurations/credentials-reference";
	private static final String REFERENCE_PROPERTIES_FILE = "com/francetelecom/clara/cloud/commons/testconfigurations/diff/last_released_credentials.properties";
	private static final String ADDED_PROPERTIES_FILE = "com/francetelecom/clara/cloud/commons/testconfigurations/diff/added_since_last_released.properties";
	private static final String REMOVED_PROPERTIES_FILE = "com/francetelecom/clara/cloud/commons/testconfigurations/diff/removed_since_last_released.properties";
	private static final String DEVELOPMENT_PROPERTIES_FILE = "com/francetelecom/clara/cloud/commons/testconfigurations/diff/credentials-development_specific.properties";

	private ElPaaSoLogicalTestModelCatalog elPaaSoLogicalModelCatalog = new ElPaaSoLogicalTestModelCatalog();
	private LogicalConfigServiceUtils configServiceUtils = new LogicalConfigServiceUtils();
	private SampleAppProperties sampleAppProperties = new SampleAppProperties();
	private Set<String> developmentSpecificPropertiesKeys;

	@Before
	public void setup() throws InvalidConfigServiceException {
		elPaaSoLogicalModelCatalog.setContextPropertiesPath(CONTEXTPROPERTIES_PATH);
		elPaaSoLogicalModelCatalog.setSampleAppProperties(sampleAppProperties);
		developmentSpecificPropertiesKeys = configServiceUtils.loadKeysFromFile(DEVELOPMENT_PROPERTIES_FILE);
	}

	@Test
	public void ensure_every_properties_change_is_documented() throws Exception {
		// Compare catalog properties to reference paas.properties (extract from FUT)
		PropertyDiff foundDiffs = diff(elPaaSoLogicalModelCatalog, REFERENCE_PROPERTIES_FILE);

		// List keys declared as added since last release
		Set<String> officiallyAdded = configServiceUtils.loadKeysFromFile(ADDED_PROPERTIES_FILE);
		// List keys declared as removed since last release
		Set<String> officiallyRemoved = configServiceUtils.loadKeysFromFile(REMOVED_PROPERTIES_FILE);

		// Compare declaration to effective properties found in catalog
		PropertyDiff unexceptedDiffs = new PropertyDiff(Sets.difference(foundDiffs.getAdded(), officiallyAdded), Sets.difference(foundDiffs.getRemoved(),
				officiallyRemoved));

		// If there are some difference, just log it and fire an error
		assertTrue(
				unexceptedDiffs.toString()
						+ "\nDon't forget to update (cloud-commons-testconfigurations):"
						+ "\t-added_since_last_released.properties\n"
                        + "\t-removed_since_last_released.properties files.\n"
                        + "#Have a look at http://elpaaso_shp/index.php/Credentials.properties#Updating_properties to update this file",
				unexceptedDiffs.isSame());
	}

	@Test
	public void ensure_every_added_properties_is_not_already_in_last_release_properties() throws InvalidConfigServiceException {
		// List keys from last release
		Set<String> lastReleaseKeys = configServiceUtils.loadKeysFromFile(REFERENCE_PROPERTIES_FILE);

		// List keys declared as added since last release
		Set<String> officiallyAddedKeys = configServiceUtils.loadKeysFromFile(ADDED_PROPERTIES_FILE);

		// List all keys that are in both Set
		Set<String> addedKeysAlreadyInLastRelease = Sets.intersection(lastReleaseKeys, officiallyAddedKeys);

		// Ensure that the intersection of both Set is empty
		assertTrue(ADDED_PROPERTIES_FILE + " contains keys that are already in " + REFERENCE_PROPERTIES_FILE + addedKeysAlreadyInLastRelease,
				addedKeysAlreadyInLastRelease.isEmpty());
	}

	@Test
	public void ensure_every_added_properties_is_realy_declared_in_context_properties() throws InvalidConfigServiceException {
        // List development properties keys
		Set<String> developmentPropertiesKeys = getReferencePropertiesKeys(elPaaSoLogicalModelCatalog);

		// List keys declared as added since last release
		Set<String> officiallyAddedKeys = configServiceUtils.loadKeysFromFile(ADDED_PROPERTIES_FILE);

		Set<String> addedKeysNotFoundInDevelopmentKeys = Sets.difference(officiallyAddedKeys, developmentPropertiesKeys);

		// Validate that all added keys are realy in development keys
        String initialDevPropertiesFile = elPaaSoLogicalModelCatalog.getContextPropertiesPath();

        assertTrue("Some keys into added keys are not declared in development properties [" + initialDevPropertiesFile + "] : "
                + addedKeysNotFoundInDevelopmentKeys,
				addedKeysNotFoundInDevelopmentKeys.isEmpty());
	}

	@Test
	public void ensure_every_removed_properties_was_declared_in_last_release_properties() throws InvalidConfigServiceException {
		// List keys from last release
		Set<String> lastReleaseKeys = configServiceUtils.loadKeysFromFile(REFERENCE_PROPERTIES_FILE);

		// List keys declared as removed since last release
		Set<String> officiallyRemoved = configServiceUtils.loadKeysFromFile(REMOVED_PROPERTIES_FILE);

		Set<String> removedKeysNotFoundInLastRelease = Sets.difference(officiallyRemoved, lastReleaseKeys);

		// Validate that all removed keys was realy in last release keys
		assertTrue("Some keys into removed keys are not declared in last release properties : " + removedKeysNotFoundInLastRelease,
				removedKeysNotFoundInLastRelease.isEmpty());
	}

	@Test
	public void ensure_every_removed_properties_has_been_removed_from_development_properties() throws InvalidConfigServiceException {
		// List development properties keys
		Set<String> developmentPropertiesKeys = getReferencePropertiesKeys(elPaaSoLogicalModelCatalog);

		// List keys declared as removed since last release
		Set<String> officiallyRemoved = configServiceUtils.loadKeysFromFile(REMOVED_PROPERTIES_FILE);

		// List all keys that are in both Set
		Set<String> removedKeysStillDeclaredInReference = Sets.intersection(developmentPropertiesKeys, officiallyRemoved);

		// Ensure that the intersection of both Set is empty
		assertTrue(REMOVED_PROPERTIES_FILE + " contains keys that are still declared in " + REFERENCE_PROPERTIES_FILE + removedKeysStillDeclaredInReference,
				removedKeysStillDeclaredInReference.isEmpty());
	}

	@Test
	public void ensure_every_development_specific_properties_are_declared_in_reference_properties() throws InvalidConfigServiceException {
		// List development properties
		Set<String> developmentPropertiesKeys = configServiceUtils.loadKeysFromFile(CONTEXTPROPERTIES_PATH + ".properties");

		// Validate that all development specific properties keys are in reference properties
		Set<String> developmentSpecificKeysNotFoundInDevelopmenticPropertiesKeys = Sets
				.difference(developmentSpecificPropertiesKeys, developmentPropertiesKeys);
		assertTrue("Some development specific properties are not declared in reference properties"
				+ developmentSpecificKeysNotFoundInDevelopmenticPropertiesKeys, developmentSpecificKeysNotFoundInDevelopmenticPropertiesKeys.isEmpty());
	}

	public PropertyDiff diff(ElPaaSoLogicalTestModelCatalog catalog, String fileName) throws InvalidConfigServiceException {
		// Load properties from a reference properties file
		Set<String> releasePropertiesKeys = configServiceUtils.loadKeysFromFile(fileName);

		Set<String> actualPropertiesKeys = getReferencePropertiesKeys(catalog);

		// Evict all development properties from comparison
		releasePropertiesKeys.removeAll(developmentSpecificPropertiesKeys);

		SetView<String> addedProperties = Sets.difference(actualPropertiesKeys, releasePropertiesKeys);
		SetView<String> removedProperties = Sets.difference(releasePropertiesKeys, actualPropertiesKeys);

		// Return a diff properties containing new and removed properties
		return new PropertyDiff(addedProperties, removedProperties);
	}

	private Set<String> getReferencePropertiesKeys(ElPaaSoLogicalTestModelCatalog catalog) throws InvalidConfigServiceException {
		Set<String> actualPropertiesKeys = Sets.newTreeSet();
		// Merge properties keys from ElpaasoLogicalModelCatalog into one single Set
		Set<LogicalConfigService> listLogicalServices = catalog.createLogicalModel("elpaaso").listLogicalServices(LogicalConfigService.class);
		for (LogicalConfigService logicalConfigService : listLogicalServices) {
			String configSetContent = logicalConfigService.getConfigSetContent();
			StructuredLogicalConfigServiceContent parseConfigContent = configServiceUtils.parseConfigContent(configSetContent);
			actualPropertiesKeys.addAll(parseConfigContent.listKeys());
		}
		return actualPropertiesKeys;
	}

	public class PropertyDiff {
		private Set<String> added;
		private Set<String> removed;

		public PropertyDiff(Set<String> added, Set<String> removed) {
			this.added = added;
			this.removed = removed;
		}

		public boolean isSame() {
			return added.isEmpty() && removed.isEmpty();
		}

		public Set<String> getAdded() {
			return added;
		}

		public void setAdded(Set<String> added) {
			this.added = added;
		}

		public Set<String> getRemoved() {
			return removed;
		}

		public void setRemoved(Set<String> removed) {
			this.removed = removed;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("-- added --\n");
			for (String addedProp : added) {
				builder.append(addedProp);
				builder.append("=\n");
			}
			builder.append("-- removed --\n");
			for (String removedProp : removed) {
				builder.append(removedProp);
				builder.append("=\n");
			}
			return builder.toString();
		}
	}

}
