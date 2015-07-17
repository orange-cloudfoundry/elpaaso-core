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

import com.francetelecom.clara.cloud.model.TechnicalDeployment;

public class SpaceTest {

	@Test
	public void a_default_space_name_should_be_set() {
		Space space = new Space(new TechnicalDeployment(""));
		Assertions.assertThat(space.getSpaceName()).isEqualTo(new SpaceName("undefined"));
	}
	
	@Test
	public void space_activation_should_update_space_deployment_state() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		Space subscription = new Space(td);
		
		subscription.activate(new SpaceName("newName"));
		
		Assertions.assertThat(subscription.isActivated()).isTrue();
	}
	
	@Test
	public void space_activation_should_update_space_name() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		Space subscription = new Space(td);
		
		subscription.activate(new SpaceName("newName"));
		
		Assertions.assertThat(subscription.getSpaceName()).isEqualTo(new SpaceName("newName"));
	}
	
	@Test
	public void space_deletion_should_update_space_deployment_state() {
		TechnicalDeployment td = new TechnicalDeployment("name");
		Space subscription = new Space(td);
		
		subscription.delete();
		
		Assertions.assertThat(subscription.isRemoved()).isTrue();
	}
	

}
