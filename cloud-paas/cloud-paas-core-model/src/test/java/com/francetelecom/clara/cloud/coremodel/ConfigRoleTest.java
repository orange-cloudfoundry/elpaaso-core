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

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

public class ConfigRoleTest {

	@Test(expected = IllegalArgumentException.class)
	public void application_uid_can_not_be_empty() {
		new ConfigRole("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void application_uid_can_not_be_null() {
		new ConfigRole(null);
	}

	@Test
	public void config_role_is_ok() {
		ConfigRole configRole = new ConfigRole("myapp");
		configRole.setLastModificationComment("Modified by Guillaume.");
		configRole.setValues(Arrays.asList(new ConfigValue("myconfigset", "mykey", "myvalue", "update mykey to its new value")));
	}

    @Test
    public void too_long_comments_are_left_truncated_to_avoid_breaking_db_schema() {
        ConfigRole configRole = new ConfigRole("myapp");
        String long300CharsComment = StringUtils.rightPad("comment", 300, 'X');
        assertThat(long300CharsComment.length()).isEqualTo(300);
        configRole.setLastModificationComment(long300CharsComment);
        assertThat(configRole.getLastModificationComment().length()).isEqualTo(ConfigRole.MAX_COMMENT_SIZE);
        assertThat(configRole.getLastModificationComment()).startsWith("comment");
    }

}
