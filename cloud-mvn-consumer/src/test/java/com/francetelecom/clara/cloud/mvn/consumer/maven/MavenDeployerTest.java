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
package com.francetelecom.clara.cloud.mvn.consumer.maven;


import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.mvn.consumer.MvnConsumerConfigurer;

@RunWith(MockitoJUnitRunner.class)
public class MavenDeployerTest {

	private static final MavenReference dummyMavenRef = new MavenReference("dummyGroup", "dummyArt", "dummyVers");

	@Mock
	MvnConsumerConfigurer configurer;

	@InjectMocks
	MavenDeployer deployer;

	@Before
	public void setUp() throws Exception {
	}

	@Test(expected = NullPointerException.class)
	public void deployFileset_should_throw_exception_when_maven_ref_is_null() {
		deployer.deployFileset(null, null);
		fail("should not be here");
	}

	@Test(expected = NullPointerException.class)
	public void deployFileset_should_throw_exception_when_fileset_is_null() {
		deployer.deployFileset(dummyMavenRef, null);
		fail("should not be here");
	}

	@Test(expected = NullPointerException.class)
	public void deployBin_should_throw_exception_when_maven_ref_is_null() {
		deployer.deployBin(null, null);
		fail("should not be here");
	}

	@Test(expected = NullPointerException.class)
	public void deployBin_should_throw_exception_when_bin_is_null() {
		deployer.deployBin(dummyMavenRef, null);
		fail("should not be here");
	}


}
