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

import com.francetelecom.clara.cloud.commons.MissingDefaultUserException;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;

public class ApplicationTest {

	@Before
	public void setUp() throws Exception {
	}
	
	@Test
	public void shouldRemoveApplication() {
		Application application = new Application("aLabel", "aCode");
		application.markAsRemoved();
		Assert.assertTrue(application.isRemoved());
	}
	
	@Test
	public void shouldSetApplicationAsPublic() {
		Application application = new Application("aLabel", "aCode");
		application.setAsPublic();
		Assert.assertTrue(application.isPublic());
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void creatingApplicationWithNullLabelShouldFail() {
		new Application(null, "aCode");
	}

	@Test(expected = IllegalArgumentException.class)
	public void settingNullLabelShouldFail() {
		Application application = new Application("aLabel", "aCode");
		application.setLabel(null);
	}
	

	@Test(expected = IllegalArgumentException.class)
	public void creatingApplicationWithEmptyLabelShouldFail() {
		new Application("", "aCode");
	}

	@Test(expected = IllegalArgumentException.class)
	public void settingEmptyLabelShouldFail() {
		Application application = new Application("aLabel", "aCode");
		application.setLabel("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void creatingApplicationWithNullCodeShouldFail() {
		new Application("aLabel", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void settingNullCodeIntoExistingApplicationShouldFail() {
		Application application = new Application("aLabel", "aCode");
		application.setCode(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void creatingApplicationWithEmptyCodeShouldFail() {
		new Application("aLabel", "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void settingEmptyCodeIntoExistingApplicationShouldFail() {
		Application application = new Application("aLabel", "aCode");
		application.setCode("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void settingNullApplicationRegistryURLShouldFail() {
		Application application = new Application("aLabel", "aCode");
		application.setApplicationRegistryUrl(null);
	}

	@Test
	public void defaultVisibilityForApplicationIsPublic() {
		Application application = new Application("aLabel", "aCode");
		assertTrue(application.isPublic());
	}

	@Test
	public void defaultMembersListIsEmpty() {
		Application application = new Application("aLabel", "aCode");
		assertTrue(application.listMembers().isEmpty());
	}

	@Test(expected = UnsupportedOperationException.class)
	public void membersListIsUnmodifiable() {
		Application application = new Application("aLabel", "aCode");
		application.listMembers().add(new SSOId("bob123"));
	}

	@Test
	public void should_set_application_as_private() {
		// given application joyn
		Application joyn = new Application("joyn", "joyn");
		joyn.setAsPrivate();
		Assertions.assertThat(joyn.isPublic()).isEqualTo(false);
	}
	
	@Test
	public void should_set_application_as_public() {
		// given application joyn
		Application joyn = new Application("joyn", "joyn");
		joyn.setAsPublic();
		Assertions.assertThat(joyn.isPublic()).isEqualTo(true);
	}

	@Test
	public void alice_should_be_a_member_of_application_with_members_bob_and_alice() {
		// given application joyn
		Application joyn = new Application("joyn", "joyn");
		// given bob is a paas user
		SSOId bob = new SSOId("bob123");
		// given alice is a paas user
		SSOId alice = new SSOId("alice123");
		
		// when I set joyn as private with members alice and bob
		HashSet<SSOId> members = new HashSet<SSOId>();
		members.add(alice);
		members.add(bob);
		joyn.setMembers(members);
		
		Assert.assertTrue(joyn.hasForMember(alice));
	}
	
	@Test(expected=MissingDefaultUserException.class)
	public void fail_to_set_null_members() {
		// given application joyn
		Application joyn = new Application("joyn", "joyn");
			
		// when I set joyn as private with members alice and bob
		HashSet<SSOId> members = null;
		joyn.setMembers(members);
		
	}
	
	@Test(expected=MissingDefaultUserException.class)
	public void fail_to_set_no_member() {
		// given application joyn
		Application joyn = new Application("joyn", "joyn");
			
		// when I set joyn as private with members alice and bob
		HashSet<SSOId> members = new HashSet<>();
		joyn.setMembers(members);
		
	}
	
	@Test
	public void alice_should_not_be_a_member_of_application_with_members_bob() {
		// given application joyn
		Application joyn = new Application("joyn", "joyn");
		// given bob is a paas user
		SSOId bob = new SSOId("bob123");
		// given alice is a paas user
		SSOId alice = new SSOId("alice123");
		
		// when I set joyn as private with members alice and bob
		HashSet<SSOId> members = new HashSet<SSOId>();
		members.add(bob);
		joyn.setMembers(members);
		
		Assert.assertFalse(joyn.hasForMember(alice));
	}

	@Test
	public void application_can_be_editable() throws Exception {
		//given application joyn
		Application joyn = new Application("joyn", "joyn");
		//when application joyn is set as editable
		joyn.setEditable(true);
		//then it should be editable
		Assert.assertTrue(joyn.isEditable());
	}
	
	@Test
	public void application_can_be_non_editable() throws Exception {
		//given application joyn
		Application joyn = new Application("joyn", "joyn");
		//when application joyn is set as non editable
		joyn.setEditable(false);
		//then it should be editable
		Assert.assertFalse(joyn.isEditable());
	}

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_set_code_when_application_is_in_removed_state() throws Exception {
		//given application joyn is in removed state
		Application joyn = new Application("joyn", "joyn");
		joyn.markAsRemoved();
		//when I set application code
		joyn.setCode("code");
		//then it should fail
	}

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_set_label_when_application_is_in_removed_state() throws Exception {
		//given application joyn is in removed state
		Application joyn = new Application("joyn", "joyn");
		joyn.markAsRemoved();
		//when I set application label
		joyn.setLabel("label");
		//then it should fail
	}

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_set_registry_url_when_application_is_in_removed_state() throws Exception {
		//given application joyn is in removed state
		Application joyn = new Application("joyn", "joyn");
		joyn.markAsRemoved();
		//when I set application registry url
		joyn.setApplicationRegistryUrl(new URL("http://localhost"));
		//then it should fail
	}

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_set_members_when_application_is_in_removed_state() throws Exception {
		//given application joyn is in removed state
		Application joyn = new Application("joyn", "joyn");
		joyn.markAsRemoved();
		//when I set application label
		HashSet<SSOId> members = new HashSet<SSOId>();
		members.add(new SSOId("bob123"));
		joyn.setMembers(members);
		//then it should fail
	}

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_add_config_role_when_application_is_in_removed_state() throws Exception {
		//given application joyn is in removed state
		Application joyn = new Application("joyn", "joyn");
		joyn.markAsRemoved();
		//when I set application label
		joyn.addConfigRole(new ConfigRole());
		//then it should fail
	}



}
