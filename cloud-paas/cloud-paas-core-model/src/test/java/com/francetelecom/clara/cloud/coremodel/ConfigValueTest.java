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

import org.junit.Test;

public class ConfigValueTest {

	@Test(expected = IllegalArgumentException.class)
	public void config_key_can_not_be_empty() {
		new ConfigValue("myconfigset", "", "myvalue", "update mykey to its new value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void config_key_can_not_be_null() {
		new ConfigValue("myconfigset", null, "myvalue", "update mykey to its new value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void config_set_can_not_be_empty() {
		new ConfigValue("", "mykey", "myvalue", "update mykey to its new value");
	}

	@Test(expected = IllegalArgumentException.class)
	public void config_set_can_not_be_null() {
		new ConfigValue(null, "mykey", "myvalue", "update mykey to its new value");
	}

	@Test
	public void config_value_can_be_empty() {
		new ConfigValue("myconfigset", "mykey", "", "update mykey to its new value");
	}

	@Test
	public void config_comment_can_be_empty() {
		new ConfigValue("myconfigset", "mykey", "myvalue", "");
	}

	@Test
	public void config_value_is_ok() {
		new ConfigValue("myconfigset", "mykey", "myvalue", "update mykey to its new value");
	}

}
