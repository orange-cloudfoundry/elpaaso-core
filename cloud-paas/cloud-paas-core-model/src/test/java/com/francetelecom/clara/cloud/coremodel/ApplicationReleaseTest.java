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
package com.francetelecom.clara.cloud.coremodel;


import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import com.francetelecom.clara.cloud.commons.TechnicalException;

public class ApplicationReleaseTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test(expected = IllegalArgumentException.class)
	public void creatingApplicationRelaseWithNoVersionShouldFail() {
		new ApplicationRelease(new Application("aLabel", "aCode"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void creatingApplicationRelaseWithEmptyVersionShouldFail() {
		new ApplicationRelease(new Application("aLabel", "aCode"), "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void creatingApplicationRelaseWithNoApplicationShouldFail() {
		new ApplicationRelease(null, "aVersion");
	}

	@Test(expected = IllegalArgumentException.class)
	public void creatingApplicationRelaseWithRemovedApplicationShouldFail() {
		Application application = new Application("aLabel", "aCode");
		application.markAsRemoved();
		new ApplicationRelease(application, "aVersion");
	}

	@Test(expected = IllegalArgumentException.class)
	public void settingNullVersionIntoExistingApplicationReleaseShouldFail() {
		ApplicationRelease release = new ApplicationRelease(new Application("aLabel", "aCode"), "aVersion");
		release.setReleaseVersion(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void settingEmptyVersionIntoExistingApplicationReleaseShouldFail() {
		ApplicationRelease release = new ApplicationRelease(new Application("aLabel", "aCode"), "aVersion");
		release.setReleaseVersion("");
	}

	@Test(expected = TechnicalException.class)
	public void fail_to_lock_release_if_state_is_EDITING() throws Exception {
		ApplicationRelease release = new ApplicationRelease(new Application("aLabel", "aCode"), "aVersion");
		release.lock();
	}
	
	@Test
	public void locking_a_release_should_make_it_LOCKED() throws Exception {
		ApplicationRelease release = new ApplicationRelease(new Application("aLabel", "aCode"), "aVersion");
		release.validate();
		release.lock();
		Assertions.assertThat(release.isLocked()).isTrue();
	}
}
