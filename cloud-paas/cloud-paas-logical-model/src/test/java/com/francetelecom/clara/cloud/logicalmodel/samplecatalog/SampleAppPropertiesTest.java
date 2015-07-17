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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.francetelecom.clara.cloud.commons.MavenReference;

public class SampleAppPropertiesTest {

	SampleAppProperties mavenRefCatalog;
	

	@Before
	public void setup() {
		mavenRefCatalog = new SampleAppProperties();
	}
	
	@Test
	public void testGetSpringooEarMavenRef() {
		MavenReference ear = mavenRefCatalog.getMavenReference("springoo","ear");
		checkMavenReference(ear,"ear");
	}

	@Test
	public void testGetPaasEarMavenRef() {
		MavenReference ear = mavenRefCatalog.getMavenReference("paas","ear");
		checkMavenReference(ear,"ear");
	}

	@Test
	public void testGetConfigProbeEarMavenRef() {
		MavenReference ear = mavenRefCatalog.getMavenReference("config-probe","ear");
		checkMavenReference(ear,"ear");
	}

	@Test
	public void checkSimpleProbeJarMavenRef() {
		MavenReference jar = mavenRefCatalog.getMavenReference("simple-probe", "jar");
		checkMavenReference(jar, "jar");
	}

	@Test
	public void checkJeeProbeEarMavenRef() {
		MavenReference ear = mavenRefCatalog.getMavenReference("jeeprobe", "ear");
		checkMavenReference(ear, "ear");
	}

	private void checkMavenReference(MavenReference mr, String type) {

		assertNotNull(mr);
		assertNotNull("group-id is null "+mr, mr.getGroupId());
		assertNotNull("artifact-id is null "+mr, mr.getArtifactId());
		assertNotNull("version is null "+mr, mr.getVersion());
		assertEquals("invalid type " + mr, type, mr.getExtension());
//		assertNotNull("classifier is null "+mr,mr.getClassifier());
		
		String VERSION_PATTERN = "(\\d+\\.)*.*";
		assertTrue("version is valid "+mr, mr.getVersion().matches(VERSION_PATTERN));
	}

	
}
