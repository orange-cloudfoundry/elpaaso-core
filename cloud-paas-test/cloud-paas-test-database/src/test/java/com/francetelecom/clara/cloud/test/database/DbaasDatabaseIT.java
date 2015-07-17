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

import static org.junit.Assert.*;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This integration test creates 2 databases on DBaaS (one mysql, one postgresql) and tests their connections
 * Databases and datasources are described in spring context
 */
@ContextConfiguration()
@RunWith(SpringJUnit4ClassRunner.class)
public class DbaasDatabaseIT {

	@Autowired
	DataSource ds1;

	@Autowired
	DataSource ds2;

	@Test
	public void test() throws SQLException {
		assertNotNull(ds1.getConnection());
		assertNotNull(ds2.getConnection());
	}
}

