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
package com.francetelecom.clara.cloud;

import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/model/AbstractPersistenceTest-context.xml" })
public class TechnicalDeploymentRepositoryTest {

	@Autowired
	private TechnicalDeploymentRepository technicalDeploymentRepository;

	@Test(expected = DataAccessException.class)
	@Transactional
	public void testPersistDuplicateName() throws DataAccessException {
		// test setup
		TechnicalDeployment toBePersited1 = new TechnicalDeployment("ld-test1");
		TechnicalDeployment toBePersited2 = new TechnicalDeployment("ld-test1");
		// test run
		technicalDeploymentRepository.save(toBePersited1);
		technicalDeploymentRepository.save(toBePersited2);
		technicalDeploymentRepository.flush();
	}
	


}
