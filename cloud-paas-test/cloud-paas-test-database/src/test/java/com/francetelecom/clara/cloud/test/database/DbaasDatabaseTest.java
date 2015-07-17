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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.EngineWsEnum;

public class DbaasDatabaseTest {

	DbaasDatabase database = new DbaasDatabase();
	DbaasService dbaasService = mock(DbaasService.class);

	
	@Before
	public void setUp() throws Exception {
		database.setName("test");
		database.setUUId("testUUId");
		database.setEngine(EngineWsEnum.MYSQL);
		database.setHost("host");
		database.setPort("5678");
		database.setDbaasService(dbaasService);
	}

	@Test
	public void testCreate() throws Exception {
		database.create();
		verify(dbaasService).createDatabase(database);
	}

	@Test
	public void testDelete() throws Exception {
		database.delete();
		verify(dbaasService).deleteDatabase(database);
	}

	@Test
	public void testGetUrl_mysql() {
		database.setEngine(EngineWsEnum.MYSQL);
		
		assertEquals("jdbc:mysql://host:5678/test", database.getUrl());
	}

	@Test
	public void testGetUrl_postgresql() {
		database.setEngine(EngineWsEnum.POSTGRESQL);
		
		assertEquals("jdbc:postgresql://host:5678/test", database.getUrl());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetUrl_unsupported_engine() {
		database.setEngine(EngineWsEnum.fromValue("INVALID"));
		database.getUrl();
	}

	@Test(expected = TechnicalException.class)
	public void testGetUrl_undefined_name() {
		database.setName(null);
		database.getUrl();
	}

	@Test(expected=TechnicalException.class)
	public void testGetUrl_undefined_host() {
		database.setHost(null);
		database.getUrl();
	}

	@Test(expected=TechnicalException.class)
	public void testGetUrl_undefined_port() {
		database.setPort(null);
		database.getUrl();
	}


}
