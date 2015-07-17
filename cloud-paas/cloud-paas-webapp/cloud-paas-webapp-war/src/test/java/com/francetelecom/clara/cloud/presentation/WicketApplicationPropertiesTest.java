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
package com.francetelecom.clara.cloud.presentation;

import static org.fest.assertions.Assertions.assertThat;

import java.io.IOException;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.francetelecom.clara.cloud.commons.testconfigurations.PropertiesHelper;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class WicketApplicationPropertiesTest {

	private static final String FRENCH_PROPERTIES_FILE = "/com/francetelecom/clara/cloud/presentation/WicketApplication_fr.properties";
	private static final String ENGLISH_PROPERTIES_FILE = "/com/francetelecom/clara/cloud/presentation/WicketApplication.properties";
	private Set<String> frenchKeys;
	private Set<String> englishKeys;
	
	@Before
	public void setup() throws IOException {
		frenchKeys = PropertiesHelper.loadKeys(FRENCH_PROPERTIES_FILE, getClass().getResourceAsStream(FRENCH_PROPERTIES_FILE));
		englishKeys = PropertiesHelper.loadKeys(ENGLISH_PROPERTIES_FILE, getClass().getResourceAsStream(ENGLISH_PROPERTIES_FILE));
	}
	
	@Test
	public void every_english_property_key_should_be_present_in_french_properties(){
		SetView<String> englishKeysNotPresentInFrenchKeys = Sets.difference(englishKeys, frenchKeys);
		
		assertThat(englishKeysNotPresentInFrenchKeys).isEmpty();
	}
	
	@Test
	public void every_french_property_key_should_be_present_in_english_properties(){
		SetView<String> frenchKeysNotPresentInEnglishKeys = Sets.difference(frenchKeys, englishKeys);
		
		assertThat(frenchKeysNotPresentInEnglishKeys).isEmpty();
	}
}
