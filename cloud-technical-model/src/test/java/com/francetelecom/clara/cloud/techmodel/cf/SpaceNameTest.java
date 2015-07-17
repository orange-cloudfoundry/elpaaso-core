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
package com.francetelecom.clara.cloud.techmodel.cf;

import org.fest.assertions.Assertions;
import org.junit.Test;

public class SpaceNameTest {

	@Test(expected = IllegalArgumentException.class)
	public void space_name_should_not_be_null() {
		new SpaceName(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void space_name_should_not_be_empty() {
		new SpaceName("");
	}
	
	@Test
	public void space_name_generation_has_a_fixed_suffix() {
		SpaceName spaceName = SpaceName.randomSpaceNameWithSuffix("env");
			
		Assertions.assertThat(spaceName.getValue()).endsWith("-env");
	}
	
	@Test
	public void space_name_generation_can_be_randomized() {
		SpaceName spaceName1 = SpaceName.randomSpaceNameWithSuffix("env");
		SpaceName spaceName2 = SpaceName.randomSpaceNameWithSuffix("env");
		SpaceName spaceName3 = SpaceName.randomSpaceNameWithSuffix("env");
		
		Assertions.assertThat(spaceName1).isNotEqualTo(spaceName2);
		Assertions.assertThat(spaceName1).isNotEqualTo(spaceName3);
		Assertions.assertThat(spaceName2).isNotEqualTo(spaceName3);
	}

}
