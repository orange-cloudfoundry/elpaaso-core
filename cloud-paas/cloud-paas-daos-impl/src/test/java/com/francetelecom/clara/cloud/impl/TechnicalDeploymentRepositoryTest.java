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
package com.francetelecom.clara.cloud.impl;

import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/services/application-context.xml" })
public class TechnicalDeploymentRepositoryTest {

	@Autowired
	private TechnicalDeploymentRepository technicalDeploymentRepository;

	/*@Autowired
	DataSource dataSource;
*/
	@Before
	@Transactional
	public void setup() throws Exception {
		Assert.assertNotNull(technicalDeploymentRepository);
/*		Assert.assertNotNull(dataSource);
		IDatabaseConnection dbConn = new DatabaseDataSourceConnection(
				dataSource);
		IDataSet dataSet = new FlatXmlDataSetBuilder()
				.build(new FileInputStream(
						"./src/test/resources/logicalDeploymentJPAImplTest.xml"));
		Assert.assertNotNull(dataSet);
		DatabaseOperation.CLEAN_INSERT.execute(dbConn, dataSet);*/
	}

	@Test
	@Transactional
	public void testPersist() {
		// test setup
		TechnicalDeployment toBePersited = new TechnicalDeployment("td-test");
		// test run
		technicalDeploymentRepository.save(toBePersited);
		// assertions
		Assert.assertNotNull("entity does not exist", technicalDeploymentRepository.findOne(toBePersited.getId()));
		technicalDeploymentRepository.flush();
	}
	
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
	
	@Test
	@Transactional
	public void testRemove() {
		// test setup
		TechnicalDeployment toBePersited = new TechnicalDeployment("ld-test");
		technicalDeploymentRepository.save(toBePersited);
		Assert.assertNotNull("entity does not exist", technicalDeploymentRepository.findOne(toBePersited.getId()));
		// test run
		technicalDeploymentRepository.delete(toBePersited);
		// assertions
		Assert.assertNull("entity should not exist", technicalDeploymentRepository.findOne(toBePersited.getId()));
		technicalDeploymentRepository.flush();
	}
	

	@Test
	@Transactional
	public void testFind() {
		// test setup
		TechnicalDeployment toBePersited = new TechnicalDeployment("ld-test");
		technicalDeploymentRepository.save(toBePersited);
		// test run
		TechnicalDeployment entity = technicalDeploymentRepository.findOne(toBePersited.getId());
		// assertions
		Assert.assertNotNull("entity does not exist",entity);
		Assert.assertEquals("ld-test", entity.getName());
		technicalDeploymentRepository.flush();
	}
	
	@Test
	@Transactional
	public void testFindAll() {
		// test setup
		int startCount = technicalDeploymentRepository.findAll().size();
		TechnicalDeployment toBePersited1 = new TechnicalDeployment("ld-test1");
		TechnicalDeployment toBePersited2 = new TechnicalDeployment("ld-test2");
		TechnicalDeployment toBePersited3 = new TechnicalDeployment("ld-test3");
		technicalDeploymentRepository.save(toBePersited1);
		technicalDeploymentRepository.save(toBePersited2);
		technicalDeploymentRepository.save(toBePersited3);
		// test run
		List<TechnicalDeployment> entities = technicalDeploymentRepository.findAll();
		// assertions
		Assert.assertNotNull("entities should not be null",entities);
		Assert.assertEquals("there should be "+(3+startCount)+" entities",3+startCount,entities.size());
		technicalDeploymentRepository.flush();
	}

		

}
