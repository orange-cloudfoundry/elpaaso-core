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
package com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.SpaceActivationService;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;

@RunWith(MockitoJUnitRunner.class)
public class SpaceActivationServiceDefaultTest {

	@Mock
	CfAdapter cfAdapter;

	@Test
	public void should_not_fail_to_delete_space_if_space_does_not_exist() {
		SpaceActivationService spaceActivationService = new SpaceActivationServiceDefault(cfAdapter);

		// given space myspace does not exists
		SpaceName myspace = new SpaceName("myspace");
		Mockito.doReturn(false).when(cfAdapter).spaceExists(myspace);

		// when I delete space myspace
		spaceActivationService.delete(myspace);

		// then it should succeed
	}

	@Test
	public void space_activation_should_generate_a_new_space_name() {
		SpaceActivationService spaceActivationService = new SpaceActivationServiceDefault(cfAdapter);

		// given space myspace does not exist
		SpaceName myspace = new SpaceName("myspace");
		Mockito.doReturn(myspace).when(cfAdapter).getValidSpaceName("env1");

		// when I create space myspace with fixed suffix env1
		SpaceName spaceName = spaceActivationService.activate("env1");

		// then space activation should generate a new spacename
		Assertions.assertThat(spaceName).isEqualTo(myspace);

	}

	@Test
	public void should_associate_manager_and_auditor_and_developer_roles_to_user_space_creator() {
		SpaceActivationService spaceActivationService = new SpaceActivationServiceDefault(cfAdapter);

		// given space myspace does not exist
		SpaceName myspace = new SpaceName("myspace");
		Mockito.doReturn(myspace).when(cfAdapter).getValidSpaceName("env1");

		// when I create space myspace with fixed suffix env1
		spaceActivationService.activate("env1");

		// then it should associate manager role to space user creator
		Mockito.verify(cfAdapter).associateManagerWithSpace(myspace);
		// then it should associate auditor role to space user creator
		Mockito.verify(cfAdapter).associateAuditorWithSpace(myspace);
		// then it should associate developer role to space user creator
		Mockito.verify(cfAdapter).associateDeveloperWithSpace(myspace);

	}
}
