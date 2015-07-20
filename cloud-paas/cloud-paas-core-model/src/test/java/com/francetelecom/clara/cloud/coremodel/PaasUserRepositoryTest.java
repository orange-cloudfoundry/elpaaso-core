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

import com.francetelecom.clara.cloud.commons.NotFoundException;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/coremodel/application-context.xml" })
public class PaasUserRepositoryTest {

	@Autowired
	private PaasUserRepository paasUserRepository;

	@Test
	public void should_find_no_paas_users_if_no_ssoid_supplied() throws NotFoundException {
		// when I search for null paauser
		PaasUser paasUser = paasUserRepository.findBySsoId(null);
		// then I should get no paas user
		Assert.assertTrue(paasUser == null);
	}
	
	@Test
	@Transactional
	public void should_ignore_case_when_finding_user_by_existing_ssoid() throws NotFoundException {
		// given bob is a paas user
		PaasUser bob = new PaasUser("bob", "Dylan", new SSOId("bob123"), "bob@orange.com");
		paasUserRepository.save(bob);
	
		// when I search for bob
		PaasUser actual = paasUserRepository.findBySsoId(new SSOId("Bob123"));
		
		//then I should  get bob
		Assert.assertTrue(actual!=null);
		Assert.assertEquals("bob", actual.getFirstName());
		Assert.assertEquals("Dylan", actual.getLastName());
		Assert.assertEquals(new SSOId("bob123"), actual.getSsoId());
		Assert.assertEquals("bob@orange.com", actual.getMail());
		
	}

}
