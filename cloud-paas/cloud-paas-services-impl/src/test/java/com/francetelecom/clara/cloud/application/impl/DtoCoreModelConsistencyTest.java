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
package com.francetelecom.clara.cloud.application.impl;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.francetelecom.clara.cloud.coremodel.ConfigValue;
import com.francetelecom.clara.cloud.services.dto.ConfigOverrideDTO;


public class DtoCoreModelConsistencyTest {

    @Test
    public void max_size_are_consistent_with_ones_defined_in_core_model() {
        Assertions.assertThat(ConfigOverrideDTO.MAX_CONFIG_KEY_LENGTH).isEqualTo(ConfigValue.MAX_CONFIG_KEY_LENGTH);
        Assertions.assertThat(ConfigOverrideDTO.MAX_CONFIG_COMMENT_LENGTH).isEqualTo(ConfigValue.MAX_CONFIG_COMMENT_LENGTH);
        Assertions.assertThat(ConfigOverrideDTO.MAX_CONFIG_VALUE_LENGTH).isEqualTo(ConfigValue.MAX_CONFIG_VALUE_LENGTH);
    }

}