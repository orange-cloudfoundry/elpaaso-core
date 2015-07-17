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
package com.francetelecom.clara.cloud.db.liquibase;

import static org.junit.Assert.*;

import java.sql.SQLException;
import javax.sql.DataSource;
import liquibase.exception.LiquibaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * This test class verifies that some 'complex' liquibase refactoring commands work as expected
 * This test class shall be enhanced when more complex refactorings need to be tested
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class CheckVariousRefactoringCommandsIT {

	@Autowired
	public DataSource datasource;

	// liquibase wrapper used to call liquibase functions  	
	private LiquibaseTestWrapper liquibaseWrapper = new LiquibaseTestWrapper();

	// test db credentials used by liquibaseTestWrapper
	private String paasTestDbUrl;
	private String paasTestDbUser;
	private String paasTestDbPassword;

	// jdbcTemplate used to execute some sql requests
	private JdbcTemplate jdbcTemplate;
	
	// Default changelog used to set-up test data
	public String basicInitialChangeLofFile = "classpath:./test-changesets/initial-changeset.xml";

	
	/**
	 * Setup of test data: set attributes, purge and initialize test datbase 
	 */
	@Before
	public void setup() throws SQLException, LiquibaseException {
		// Init jdbcTemplate
		this.jdbcTemplate = new JdbcTemplate(datasource);
		
		// Set test db properties
		DriverManagerDataSource ds = (DriverManagerDataSource)datasource;
		this.paasTestDbUrl = ds.getUrl();
		this.paasTestDbUser = ds.getUsername();
		this.paasTestDbPassword = ds.getPassword();

		// purge test database
		liquibaseWrapper.purgeDatabase(paasTestDbUrl, paasTestDbUser, paasTestDbPassword);
		
		// init database state
		liquibaseWrapper.applyChangeLog(paasTestDbUrl, paasTestDbUser, paasTestDbPassword, basicInitialChangeLofFile);
	}
	
	/**
	 * Test clean-up: purge test database 
	 */
	@After 
	public void teardown() throws SQLException, LiquibaseException {
		// purge test database
		liquibaseWrapper.purgeDatabase(paasTestDbUrl, paasTestDbUser, paasTestDbPassword);
	}
	
	
	/** 
	 * Given a database with a table TABLE_TEST_1 with some lines
	 * When liquibase executes following changeset: 
	 * 		<renameTable oldTableName="TABLE_TEST_1" newTableName="table_test_2"/>
	 * Then table 'TABLE_TEST_2' can be requested
	 * And  table 'TABLE_TEST_1' does not exist anymore
	 */
	@Test
	public void testRenameTable() throws SQLException, LiquibaseException {
		// Prepare test data
		jdbcTemplate.execute("insert into table_test_1 values (1717,'test-1717')");

		// Run test
		applyChangeLog("classpath:./test-changesets/rename-table-changeset.xml");

		// Assert table has been renamed
		int n = jdbcTemplate.queryForInt("select count(*) from table_test_2 where id=1717 and text='test-1717'");
		assertEquals(1,n);
		// And old name does not exist
		try {
			jdbcTemplate.queryForInt("select count(*) from table_test_1");
			fail("table_test_1 still exists");
		} catch(DataAccessException e) {
			// ignore as we expect an exception
		}
	}

	/** 
	 * Given a database with a table TABLE_TEST_1 with some lines
	 * When liquibase executes following changeset: 
	 * 		<addColumn tableName="table_test_1">
     *			<column name="new_data" type="varchar(255)" defaultValue="default_value_for_new_column"/>
	 *		</addColumn>
	 * Then 'new_data' column on existing lines is 'default_value_for_new_column'
	 */
	@Test
	public void testNewColumnWithDefaultValue() throws SQLException, LiquibaseException {
		// Prepare test data
		jdbcTemplate.execute("insert into table_test_1 values (1717,'test-1717')");

		// Run test
		applyChangeLog("classpath:./test-changesets/new-column-changeset.xml");

		// Assert new column can be requested and has a default value
		int n = jdbcTemplate.queryForInt("select count(*) from table_test_1 where id=1717 and NEW_DATA='default_value_for_new_column'");
		assertEquals(1,n);
	}

	/** 
	 * Given a database with no sequence
	 * When liquibase executes following changeset: <createSequence sequenceName="my_sequence" incrementBy="2" startValue="1"/>
	 * Then the first value of "my_sequence" is 1
	 * And  the next  value of "my_sequence" is 3
	 */
	@Test
	public void testCreateSequence() throws SQLException, LiquibaseException {
		// Run test
		applyChangeLog("classpath:./test-changesets/create-sequence-changeset.xml");

		// Assert new sequence is created by requesting it twice
		int v1 = jdbcTemplate.queryForInt("select nextval('my_sequence')");
		assertEquals(1,v1);	
		int v2 = jdbcTemplate.queryForInt("select nextval('my_sequence')");
		assertEquals(3,v2);	
	}
	
	/**
	 * This test verifies a pattern to fix art #95049
	 * Given a database in which a changeset adding a not nullable column has been commited without defining a default value
	 * And they are some data in the updated table
	 * When liquibase applies a similar changeset that defines a default value
	 * Then the fixed changeset should be executed without error  
	 * @throws LiquibaseException 
	 * @throws SQLException 
	 */
	@Test
	public void testFixInvalidChangeSetAddNotNullColumnWithNoDefaultValue() throws SQLException, LiquibaseException {
		// Prepare test data: purge test table, apply incorrect change set and insert data in table
		jdbcTemplate.execute("delete from table_test_1");
		applyChangeLog("classpath:./test-changesets/new-column-no-default-value-invalid-changeset.xml");
		jdbcTemplate.execute("insert into table_test_1 values (1717,'test-1717','new-data-1717')");
		
		// Run test
		applyChangeLog("classpath:./test-changesets/new-column-no-default-value-fixed-changeset.xml");		
		
		// Assertion: nothing;  changeset should simply be applied without error	
	}
	
	/**
	 * This test verifies a pattern to implement database denormalization
	 * Given a database with 3 tables table_1, table_1 and table_3 modeling an inheritance entity_1 > entity_2 > entity_3
	 * When schema is denormalized
	 * Then table_1 and table_2 data are moved into table_3
	 */
	@Test
	public void testDenormalization() throws SQLException, LiquibaseException {
		// Prepare test data: complete initialization with additional change set
		applyChangeLog("classpath:./test-changesets/denormalization-init-changeset.xml");
		
		// Run test
		applyChangeLog("classpath:./test-changesets/denormalization-migration-changeset.xml");	

		// Assertions
		SqlRowSet results = jdbcTemplate.queryForRowSet("select id, c1, c2, c3 from table_3");
		assertTrue("no data in SqlRowSet results", results.first());
		do {
			int id = results.getInt("id");
			String c1 = results.getString("c1");
			String c2 = results.getString("c2");
			String c3 = results.getString("c3");
			assertEquals("t1v"+id, c1);
			assertEquals("t2v"+id, c2);
			assertEquals("t3v"+id, c3);
		} while(results.next());
		
	}
	
	// === Utility methods
	
	/**
	 * apply change log by calling liquibase test wrapper
	 * @param changeLog
	 * @throws SQLException
	 * @throws LiquibaseException
	 */
	private void applyChangeLog(String changeLog) throws SQLException, LiquibaseException {
		liquibaseWrapper.applyChangeLog(paasTestDbUrl, paasTestDbUser, paasTestDbPassword,changeLog);		
	}
	
}
