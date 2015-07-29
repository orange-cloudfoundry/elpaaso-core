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
package com.francetelecom.clara.cloud.commons;

import java.net.MalformedURLException;
import java.net.URL;


import org.junit.Assert;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class MavenReferenceTest {

	@Test
	public void fromGavString() {
		try {
			MavenReference.fromGavString(null);
			fail("Should not be here");
		} catch (IllegalArgumentException iae) {
		}

		try {
			MavenReference.fromGavString("");
			fail("Should not be here");
		} catch (IllegalArgumentException iae) {
		}

		try {
			MavenReference.fromGavString("a:::");
			fail("Should not be here");
		} catch (IllegalArgumentException iae) {
		}

		try {
			MavenReference.fromGavString("com.francelecom:::");
			fail("Should not be here");
		} catch (IllegalArgumentException iae) {
		}

		try {
			MavenReference.fromGavString("com.francelecom:apt::");
			fail("Should not be here");
		} catch (IllegalArgumentException iae) {
		}

		MavenReference ref = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3");
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "com.francelecom", ref.getGroupId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "apt-cloud", ref.getArtifactId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "1.3", ref.getVersion());
		Assert.assertNull("Testing groupid with MavenReference.fromGavString", ref.getClassifier());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "jar", ref.getExtension());

		ref = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3:test:ear");
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "com.francelecom", ref.getGroupId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "apt-cloud", ref.getArtifactId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "1.3", ref.getVersion());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "test", ref.getClassifier());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "ear", ref.getExtension());

		ref = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3::ear");
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "com.francelecom", ref.getGroupId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "apt-cloud", ref.getArtifactId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "1.3", ref.getVersion());
		Assert.assertNull("Testing groupid with MavenReference.fromGavString", ref.getClassifier());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "ear", ref.getExtension());

		ref = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3::");
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "com.francelecom", ref.getGroupId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "apt-cloud", ref.getArtifactId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "1.3", ref.getVersion());
		Assert.assertNull("Testing groupid with MavenReference.fromGavString", ref.getClassifier());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "jar", ref.getExtension());

		ref = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3:");
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "com.francelecom", ref.getGroupId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "apt-cloud", ref.getArtifactId());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "1.3", ref.getVersion());
		Assert.assertNull("Testing groupid with MavenReference.fromGavString", ref.getClassifier());
		Assert.assertEquals("Testing groupid with MavenReference.fromGavString", "jar", ref.getExtension());
	}

	@Test
	public void testCopyConst() throws MalformedURLException {
		MavenReference origin = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3:draft:ear");
		String accessUrl = "http://orange.fr";
		origin.setAccessUrl(new URL(accessUrl));

		MavenReference copy = new MavenReference(origin);
		assertNotSame(origin, copy);
		assertEquals(origin.getArtifactId(), copy.getArtifactId());
		assertEquals(origin.getClassifier(), copy.getClassifier());
		assertEquals(origin.getGroupId(), copy.getGroupId());
		assertEquals(origin.getExtension(), copy.getExtension());
		assertEquals(origin.getVersion(), copy.getVersion());
		assertNotSame(origin.getAccessUrl(), copy.getAccessUrl());
		assertEquals(origin.getAccessUrl().toExternalForm(), copy.getAccessUrl().toExternalForm());

		origin.setAccessUrl(null);
		copy = new MavenReference(origin);
		assertNotSame(origin, copy);
		assertEquals(origin.getArtifactId(), copy.getArtifactId());
		assertEquals(origin.getClassifier(), copy.getClassifier());
		assertEquals(origin.getGroupId(), copy.getGroupId());
		assertEquals(origin.getExtension(), copy.getExtension());
		assertEquals(origin.getVersion(), copy.getVersion());
		assertNull(copy.getAccessUrl());
	}

	@Test
	public void testToGavString() throws MalformedURLException {
		MavenReference artifactRef = new MavenReference();
		artifactRef.setArtifactId("apt-cloud");
		assertEquals("[inherited]:apt-cloud:[inherited]::", artifactRef.toGavString());

		MavenReference gavRef = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3");
		assertEquals("com.francelecom:apt-cloud:1.3::jar", gavRef.toGavString());

		MavenReference gavcRef = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3:draft");
		assertEquals("com.francelecom:apt-cloud:1.3:draft:jar", gavcRef.toGavString());

		MavenReference gavceRef = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3:draft:ear");
		assertEquals("com.francelecom:apt-cloud:1.3:draft:ear", gavceRef.toGavString());
		String testUrl = "http://orange.fr";
		gavceRef.setAccessUrl(new URL(testUrl));
		assertEquals("com.francelecom:apt-cloud:1.3:draft:ear", gavceRef.toGavString());

	}

	@Test
	public void testToString() throws MalformedURLException {
		MavenReference artifactRef = new MavenReference();
		artifactRef.setArtifactId("apt-cloud");
		assertEquals("[inherited]:apt-cloud:[inherited]::", artifactRef.toString());

		MavenReference gavRef = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3");
		assertEquals("com.francelecom:apt-cloud:1.3::jar", gavRef.toString());

		MavenReference gavcRef = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3:draft");
		assertEquals("com.francelecom:apt-cloud:1.3:draft:jar", gavcRef.toString());

		MavenReference gavceRef = MavenReference.fromGavString("com.francelecom:apt-cloud:1.3:draft:ear");
		assertEquals("com.francelecom:apt-cloud:1.3:draft:ear", gavceRef.toString());
		String testUrl = "http://orange.fr";
		gavceRef.setAccessUrl(new URL(testUrl));
		assertEquals("com.francelecom:apt-cloud:1.3:draft:ear", gavceRef.toString());

	}

	@Test
	public void testGetArtifactName() {
		MavenReference ref1 = new MavenReference();
		assertNotNull(ref1.getArtifactName());

		MavenReference nullRef = new MavenReference(null, null, null);
		assertEquals("", nullRef.getArtifactName());

		MavenReference jarRef = new MavenReference("com.francetelecom", "apt-cloud", "1.3");
		assertEquals("apt-cloud-1.3.jar", jarRef.getArtifactName());

		MavenReference tarRef = new MavenReference("com.francetelecom", "apt-cloud", "1.3", "tar.gz");
		assertEquals("apt-cloud-1.3.tar.gz", tarRef.getArtifactName());

		MavenReference tarClassifierRef = new MavenReference("com.francetelecom", "apt-cloud", "1.3", "tar.gz", "bin");
		assertEquals("apt-cloud-1.3-bin.tar.gz", tarClassifierRef.getArtifactName());

	}

	@Test
	public void testEquals() {

		MavenReference ref1 = new MavenReference("com.francetelecom", "apt-cloud", "1.3");
		assertFalse(ref1.equals(null));
		MavenReference expected1 = new MavenReference("com.francetelecom", "apt-cloud", "1.3", "jar", null);
		assertTrue(ref1.equals(expected1));

		assertFalse(ref1.equals(MavenReference.fromGavString("test:tutu:45")));

		assertTrue(ref1.equals(MavenReference.fromGavString("com.francetelecom:apt-cloud:1.3")));

		assertTrue(ref1.equals(ref1));

		ref1.setExtension("war");
		assertFalse(ref1.equals(MavenReference.fromGavString("com.francetelecom:apt-cloud:1.3")));

		MavenReference ref2 = MavenReference.fromGavString("com.francetelecom:apt-cloud:1.3");
		ref2.setExtension("EAR");
		assertFalse(ref1.equals(ref2));

		ref2.setExtension("war");
		assertTrue(ref1.equals(ref2));

		ref2 = new MavenReference();
		MavenReference expected2 = new MavenReference(null, null, null, null, null);
		assertTrue(ref2.equals(expected2));

	}

	@Test
	public void validateDuplicateWithEmpty() {

		MavenReference ref1 = new MavenReference("com.francetelecom", "apt-cloud", "1.3");
		MavenReference expected1 = new MavenReference("com.francetelecom", "apt-cloud", "1.3", "jar", "");
		assertFalse(ref1.equals(expected1));
		assertTrue("Ref1 " + ref1 + "is not equals to expected: " + expected1 + ". Check null <=> \"\" conversion", ref1.duplicateWithEmpty().equals(expected1));

		MavenReference ref2 = new MavenReference();
		MavenReference expected2 = new MavenReference("", "", "", "", "");
		assertFalse(ref2.equals(expected2));
		assertTrue("Ref2 " + ref2 + "is not equals to expected: " + expected2 + ". Check null <=> \"\" conversion", ref2.duplicateWithEmpty().equals(expected2));

	}

	@Test
	public void validateDuplicateWithNull() {

		MavenReference ref1 = new MavenReference("com.francetelecom", "apt-cloud", "1.3", "", "");
		MavenReference expected1 = new MavenReference("com.francetelecom", "apt-cloud", "1.3", null, null);
		assertFalse(ref1.equals(expected1));
		assertTrue("Ref1 " + ref1 + "is not equals to expected: " + expected1 + ". Check null <=> \"\" conversion", ref1.duplicateWithNull().equals(expected1));

		MavenReference ref2 = new MavenReference("", "", "", "", "");
		MavenReference expected2 = new MavenReference();
		assertFalse(ref2.equals(expected2));
		assertTrue("Ref2 " + ref2 + "is not equals to expected: " + expected2 + ". Check null <=> \"\" conversion", ref2.duplicateWithNull().equals(expected2));

	}

}
