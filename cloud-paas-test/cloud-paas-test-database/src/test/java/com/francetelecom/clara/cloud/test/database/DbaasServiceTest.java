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
package com.francetelecom.clara.cloud.test.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Matchers.refEq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Answers;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.orange.clara.cloud.dbaas.wsdl.data.DatabaseUserInfo;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.BackupPlanWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.DatabaseUserTypeWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.EngineWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.JobStateWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.NetworkZoneWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.ServiceClassWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.SloWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.UsageWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.service.DatabaseAlreadyExistsFault;
import com.orange.clara.cloud.dbaas.wsdl.service.DbaasApiRemote;

public class DbaasServiceTest {

	DbaasService sut = new DbaasService();
	DbaasApiRemote dbaasStub = mock(DbaasApiRemote.class, Answers.RETURNS_DEEP_STUBS.get());
	DbaasDatabase database = new DbaasDatabase();

	/**
	 * This rule is used to provide better assertions on expected exception (if any)
	 */
	@Rule
    public ExpectedException thrown= ExpectedException.none();
	
	@Before
	public void setUp() throws Exception {
		sut.setDbaasStub(dbaasStub);
		sut.setDbaasGroupName("elpaaso");
		
		// Database to be created
		database.setUser("testdbUser");
		database.setName(null);
		database.setPassword("testDbPassword");
		database.setEngine(EngineWsEnum.POSTGRESQL);
		database.setDescription("testDb description");
		
		// configure dbaasStub mock (default)
		DbaasServiceTestUtils.configureDbaasStubCreateDatabase(dbaasStub, "generatedName", "generatedUUId");
		DbaasServiceTestUtils.configureDbaasStubMockGetJob(dbaasStub, JobStateWsEnum.FINISHED, "");
		DbaasServiceTestUtils.configureDbaasStubMockGetDatabaseDetails(dbaasStub, "192.168.1.100", "5406");

	}

	@Test
	public void testCreate_success() throws Exception {
		// Given a database (created in setUp)
		// When create is called on a dbaas wrapper
		sut.createDatabase(database);

		List<DatabaseUserInfo> users = new ArrayList<DatabaseUserInfo>();
		DatabaseUserInfo user = new DatabaseUserInfo();
		user.setLogin(database.getUser());
		user.setPassword(database.getPassword());
		user.setDatabaseUserType(DatabaseUserTypeWsEnum.OWNER);
		users.add(user);

		// Then dbaas web service stub should have been called
		verify(dbaasStub).createDatabase(
				isNull(String.class), 
				eq("elpaaso"),
				eq(1), 
				eq(ServiceClassWsEnum.XXXS), 
				eq(EngineWsEnum.POSTGRESQL), 
				isNull(String.class), 
				refEq(users),
				eq(SloWsEnum.STANDARD), 
				eq(false), 
				eq(UsageWsEnum.DEVTEST), 
				eq("Default"), 
				eq(NetworkZoneWsEnum.RSC), 
				eq(""), 
				eq("Default"), 
				eq("Sun:20:00-04"), 
				eq(BackupPlanWsEnum.NONE), 
				eq("22:00-04"), 
				eq(true), 
				eq("testDb description"));
	}

	@Test
	public void testCreate_host_and_port_are_set() {
		// Exercise SUT
		sut.createDatabase(database);
		assertEquals("192.168.1.100",database.getHost());
		assertEquals("5406", database.getPort());
	}

	@Test
	public void testCreate_name_is_used_when_set()  throws Exception {
		String specifiedDatabaseName = "my-database-name";
		database.setName(specifiedDatabaseName);
		sut.createDatabase(database);
		// verify that specified name is used when calling dbaas web service
		verify(dbaasStub).createDatabase(
				eq(specifiedDatabaseName), 
				eq("elpaaso"),
				anyInt(), 
				any(ServiceClassWsEnum.class), 
				any(EngineWsEnum.class), 
				anyString(), 
				anyListOf(DatabaseUserInfo.class),
				any(SloWsEnum.class), 
				anyBoolean(), 
				any(UsageWsEnum.class), 
				anyString(), 
				any(NetworkZoneWsEnum.class), 
				anyString(), 
				anyString(), 
				anyString(), 
				any(BackupPlanWsEnum.class), 
				anyString(), 
				anyBoolean(), 
				anyString()); 
		// verify that name is unchanged
		assertEquals(specifiedDatabaseName, database.getName());
	}

	@Test
	public void testCreate_name_is_generated_when_not_set() throws Exception {
		DbaasServiceTestUtils.configureDbaasStubCreateDatabase(dbaasStub, "generatedName", "generatedUUId");
		sut.createDatabase(database);
		assertNotNull("database name is not set", database.getName());
		assertEquals("generatedName",database.getName());
	}

	@Test
	public void testCreate_database_already_created() {
		// 1st call to create database
		sut.createDatabase(database);
		reset(dbaasStub);
		// 2nd call
		sut.createDatabase(database);
		verifyZeroInteractions(dbaasStub);
	}

	@Test
	public void testCreate_dbaas_failure() throws Exception {
		// Expected exception
		thrown.expectMessage("test error");
		thrown.expect(TechnicalException.class);
		
		// configure dbaasStub mock to generate an error when task status is requested
		DbaasServiceTestUtils.configureDbaasStubMockGetJob(dbaasStub, JobStateWsEnum.ERROR, "test error");
		
		// exercise sut
		sut.createDatabase(database);
	}
	
	@Test
	public void testCreate_dbaas_exception_database_name_already_exists_failure_when_name_is_set() throws Exception {
		// Expected exception
		thrown.expectMessage("databaseName already exist");
		thrown.expect(TechnicalException.class);
		
		// set database name
		database.setName("mydatabase");
		
		// configure dbaasStub mock to first generate an exception when create database
		Exception nameAlreadyExistException = new DatabaseAlreadyExistsFault("databaseName already exist");
		DbaasServiceTestUtils.whenCreateDatabase(dbaasStub).thenThrow(nameAlreadyExistException);
		
		// exercise sut
		sut.createDatabase(database);
	}
	

	@Test
	public void testCreate_dbaas_timeout() throws Exception {
		// configure dbaasStub mock to to stay in PROCESSING state
		DbaasServiceTestUtils.configureDbaasStubMockGetJob(dbaasStub, JobStateWsEnum.PROCESSING, "");
		// configure dbaasWrapper to time out after 100ms
		sut.setDbaasTimeout(100);
		
		// exercise sut
		TechnicalException caughtException = null;
		try {
			sut.createDatabase(database);
		} catch(TechnicalException e) {
			caughtException = e;
		}
		verify(dbaasStub).deleteDatabase(database.getUUId());
		String errorMessage = caughtException.getMessage();
		assertTrue("caught exception is not a timeout exception: "+ errorMessage,
				errorMessage.matches(".*timeout.*"));
		
	}

	@Test
	public void testDelete_sucess() throws Exception {
		// Given a database whose name, host and port are set
		String dbName = "myDatabase";
		String dbUUId = "myDatabaseUUId";
		database.setName(dbName);
		database.setUUId(dbUUId);
		database.setHost("host");
		database.setPort("3456");
		// When I request to delete the database
		sut.deleteDatabase(database);
		// Then delete operation of dbaas web service should have been called
		verify(dbaasStub).deleteDatabase(dbUUId);
		// And database should be tagged as deleted
		assertTrue(database.isDeleted());
	}

	@Test
	public void testDelete_database_not_created() throws Exception {
		// Given a database whose host and port are not set
		String dbName = "myDatabase";
		String dbUUId = "myDatabaseUUId";
		database.setName(dbName);
		database.setUUId(dbUUId);
		database.setHost(null);
		database.setPort(null);
		// When I request to delete the database
		sut.deleteDatabase(database);
		// Then dbaas web service should not be called
		verify(dbaasStub, never()).deleteDatabase(anyString());
		// And no exception is thrown
	}

	@Test
	public void testDelete_database_already_deleted() throws Exception {
		// Given a database whose is set as deleted
		database.setDeleted(true);
		// When I request to delete the database
		sut.deleteDatabase(database);
		// Then dbaas web service should not be called
		verify(dbaasStub, never()).deleteDatabase(anyString());
		// And no exception is thrown
	}

	@Test
	public void testDelete_dbaas_failure() throws Exception {
		// Expected exception
		thrown.expectMessage("test error");
		thrown.expect(TechnicalException.class);
		
		// Given a database that has been created (uuid, name, host and port are set)
		String dbName = "myDatabase";
		String dbUUId = "myDatabaseUUId";
		database.setName(dbName);
		database.setUUId(dbUUId);
		database.setHost("host");
		database.setPort("5678");

		// And dbaasStub mock configured to generate an error when task status is requested
		DbaasServiceTestUtils.configureDbaasStubMockGetJob(dbaasStub, JobStateWsEnum.ERROR, "test error");
		
		// When deleteDatabase() is called 
		sut.deleteDatabase(database);
		
		// Then expected exception shall be thrown
	}

	@Test
	public void testDelete_dbaas_timeout() throws Exception {
		// Given a database that has been created (uuid, name, host and port are set)
		String dbName = "myDatabase";
		String dbUUId = "myDatabaseUUId";
		database.setName(dbName);
		database.setUUId(dbUUId);
		database.setHost("host");
		database.setPort("5678");

		// Expected exception
		thrown.expectMessage("timeout");
		thrown.expect(TechnicalException.class);
		
		// configure dbaasStub mock to to stay in PROCESSING state
		DbaasServiceTestUtils.configureDbaasStubMockGetJob(dbaasStub, JobStateWsEnum.PROCESSING, "");
		// configure dbaasWrapper to time out after 100ms
		sut.setDbaasTimeout(100);
		
		// exercise sut
		sut.deleteDatabase(database);
	}

}
