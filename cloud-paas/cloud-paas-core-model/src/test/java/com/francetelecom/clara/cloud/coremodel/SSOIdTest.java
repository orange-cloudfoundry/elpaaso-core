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

import org.junit.Assert;
import org.junit.Test;

public class SSOIdTest {

	@Test(expected=IllegalArgumentException.class)
	public void sso_id_value_can_not_be_empty() {
		new SSOId("");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void sso_id_value_can_not_be_null() {
		new SSOId(null);
	}

	@Test
	public void sso_id_toto1234_is_valid() {
		new SSOId("toto1234");
	}
	
	@Test
	public void sso_id_gberche_is_valid() {
		new SSOId("gberche");
	}

	@Test(expected=IllegalArgumentException.class)
	public void sso_id_1234_is_NOT_valid() {
		new SSOId("1234");
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void sso_id_test_hyphen_1234_is_NOT_valid() {
		new SSOId("test-1234");
	}

	@Test
	public void ssoids_with_same_values_are_equals() throws Exception {
		Assert.assertEquals(new SSOId("abcd3467") , new SSOId("abcd3467"));
	}

	@Test
	public void ssoids_with_same_values_but_different_cases_are_equals() throws Exception {
		Assert.assertEquals(new SSOId("abcd3467") , new SSOId("AbCd3467"));
	}
	
}
