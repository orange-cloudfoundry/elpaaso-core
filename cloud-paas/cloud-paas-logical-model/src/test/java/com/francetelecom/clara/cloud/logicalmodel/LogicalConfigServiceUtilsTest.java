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
package com.francetelecom.clara.cloud.logicalmodel;

import static org.fest.assertions.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LogicalConfigServiceUtilsTest {

	private static final String SAMPLE_PROPERTY_FILE = "com/francetelecom/clara/cloud/logicalmodel/sample-property-file.properties";

	@Spy
	private LogicalConfigServiceUtils configServiceUtils = new LogicalConfigServiceUtils();

	@Test
	public void load_key_should_return_all_keys_from_a_property_file() throws Exception {
		// Given
		Set<String> expectedKeys = new HashSet<String>();
		expectedKeys.add("test.prop.property1");
		expectedKeys.add("test.prop.property2");
		expectedKeys.add("test.prop.property3");

		// When
		Set<String> propertiesKeys = configServiceUtils.loadKeysFromFile(SAMPLE_PROPERTY_FILE);

		// Then
		assertThat(propertiesKeys).isEqualTo(expectedKeys);
	}

	@Test
	public void load_key_should_thow_exceptions_from_parse_config() throws Exception {
		// Given
		doThrow(new InvalidConfigServiceException("Exception while loading configuration")).when(configServiceUtils).parseConfigContent(
				any(InputStreamReader.class));

		// When
		try {
			configServiceUtils.loadKeysFromFile(SAMPLE_PROPERTY_FILE);
			fail();
		} catch (InvalidConfigServiceException e) {
			// Then
			verify(configServiceUtils).parseConfigContent(any(InputStreamReader.class));
		}
	}
}
