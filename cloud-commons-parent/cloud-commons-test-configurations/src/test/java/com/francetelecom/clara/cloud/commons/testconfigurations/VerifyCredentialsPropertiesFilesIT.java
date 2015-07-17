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
package com.francetelecom.clara.cloud.commons.testconfigurations;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Set;

import static org.junit.Assert.assertTrue;

/**
 * Used to automate comparison of credentials files when some need to be in sync (e.g. two pipelines)
 */
public class VerifyCredentialsPropertiesFilesIT {

	private static final String HUDSON_PROPERTIES_FILE = "com/francetelecom/clara/cloud/commons/testconfigurations/credentials-hudson.properties";
	private static final String REFERENCE_PROPERTIES_FILE = "/com/francetelecom/clara/cloud/commons/testconfigurations/credentials-reference.properties";

	@Test
	public void reference_and_hudson_keys_are_identical() throws IOException {

		Set<String> referenceKeys = PropertiesHelper.loadKeys(REFERENCE_PROPERTIES_FILE, this.getClass().getResourceAsStream(REFERENCE_PROPERTIES_FILE));
		Set<String> hudsonKeys = PropertiesHelper.loadKeys(HUDSON_PROPERTIES_FILE, new ClassPathResource(HUDSON_PROPERTIES_FILE).getInputStream());
		
		SetView<String> jenkinsKeysNotPresentInHudsonKeys = Sets.difference(referenceKeys, hudsonKeys);
		SetView<String> hudsonKeysNotPresentInJenkinsKeys = Sets.difference(hudsonKeys, referenceKeys);
		
		assertTrue(buildDifferences("jenkins","hudson",jenkinsKeysNotPresentInHudsonKeys, hudsonKeysNotPresentInJenkinsKeys),
			jenkinsKeysNotPresentInHudsonKeys.isEmpty() && hudsonKeysNotPresentInJenkinsKeys.isEmpty());
	}

	private String buildDifferences(String file1, String file2,
			SetView<String> keysFile1NotPresentInFile2,
			SetView<String> keysFile2NotPresentInFile1) {
		String message = "they are differences:";
		
		if(!keysFile1NotPresentInFile2.isEmpty()) {
			message += "\n"+file1+ " keys declared for "+file2+ ": ";
			message += listValues(keysFile1NotPresentInFile2);
		}
		if(!keysFile2NotPresentInFile1.isEmpty()) {
			message += "\n"+file2+  " keys declared for "+file1+ ": ";
			message += listValues(keysFile2NotPresentInFile1);
		}
		return message;
	}

	private String listValues(Set<String> keys) {
		String values = "/ ";
		for(String key:keys) values += key + " / ";
		return values;
	}
}
