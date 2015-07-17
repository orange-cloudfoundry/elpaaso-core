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
package com.francetelecom.clara.cloud.techmodel.dbaas;

import org.junit.Test;

public class DbOwnerTest {

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_create_db_owner_with_null_name() {
		new DbOwner(null, "password");
	}

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_create_db_owner_with_empty_name() {
		new DbOwner("", "password");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void fail_to_create_db_owner_with_null_password() {
		new DbOwner("scott", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_create_db_owner_with_empty_password() {
		new DbOwner("scott", "");
	}
	
}
