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

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.sql.DataSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.test.database.DbaasDatabase;

/**
 * <pre>
 * Test database migration on an existing database
 * This tests create a copy of a reference database into a test database
 * Then it executes a Spring init. of jpa and liquibase beans to verify that liquibase properly migrates the data
 * 
 * The reference database must be pre-loaded with some representative data 
 * Typically it could be a copy of the FUT database that will be migrated when the next release will be deployed.
 * 
 * To export/import fut database into the reference database you can use following commands
 * 
 * Export: pg_dump -h [dbHost] -p [dbPort] -U [dbUser] -W -n public -v -x -O -f [exportFile.sql] [dbName]
 * Import: psql -h [dbHost] -p [dbPort] -d [dbName] -U [dbUser] -W -v ON_ERROR_STOP=ON -f [exportFile.sql]
 * 
 * Note: database host, port and name must be set; password will be prompted
 * 
 * This test is restricted to postgresql. 
 * It uses pg_dump and psql posgresql commands; those commands must be available in PATH environment
 * </pre>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class CheckDatabaseMigrationIT {

	private static Logger logger = LoggerFactory.getLogger(CheckDatabaseMigrationIT.class.getName());

	// Following properties are worked-out during set up from db url
	@Value("${test.liquibase.futcopy.host}")
	String referenceDbHost;
	@Value("${test.liquibase.futcopy.port}")
	String referenceDbPort;
	@Value("${test.liquibase.futcopy.dbname}")
	String referenceDbName;
	@Value("${test.liquibase.futcopy.user}")
	String referenceDbUser;
	@Value("${test.liquibase.futcopy.password}")
	String referenceDbPassword;
	
	// Test database
	@Autowired(required = true)
	DbaasDatabase testDatabase;

	// Datasource used to connect on the test database
	@Autowired
	public DataSource datasource;

	// Some temporary files used in test
	File exportFile;
	File deleteTablesScriptFile;
	File deleteSequencesScriptFile;

	@Before
	public void setup() throws IOException {

		// export test database properties so that they can be used during test which initializes a spring context
		System.setProperty("testDatabase.url", testDatabase.getUrl());
		System.setProperty("testDatabase.user", testDatabase.getUser());
		System.setProperty("testDatabase.password", testDatabase.getPassword());

		logger.debug("PATH=" + System.getenv("PATH"));

		// Create temporary files
		exportFile = File.createTempFile("export", ".sql");
		deleteTablesScriptFile = File.createTempFile("delete_tables", ".sql");
		deleteSequencesScriptFile = File.createTempFile("delete_sequences", ".sql");

		String exportFileName = exportFile.getPath();
		logger.info("Setup test database - START");
		exportData(referenceDbHost, referenceDbPort, referenceDbName, referenceDbUser, referenceDbPassword, exportFileName);
		
		importData(testDatabase.getHost(), testDatabase.getPort(), testDatabase.getName(), testDatabase.getUser(), testDatabase.getPassword(), exportFileName);
		logger.info("Setup test database - END");
	}

	@After
	public void cleanup() {
		// no need to purge test database as it will be dropped during spring context tear-down
		// delete temporary files
		exportFile.delete();
		deleteTablesScriptFile.delete();
		deleteSequencesScriptFile.delete();
	}

	@Test
	public void testMigration() {
		// Before running test we verify that test database contains some data (to avoid testing migration on empty database or not relevant data)
		assertTestDataIsRelevant();
		logger.info("Init spring context on test database - START");
		// We use a dedicated spring configuration which initializes liquibase and hibernate using the default production configuration
		ApplicationContext context = new ClassPathXmlApplicationContext(
				"com/francetelecom/clara/cloud/db/liquibase/CheckDatabaseMigrationIT-InitLiquibaseHibernateProduction-context.xml");
		logger.info("Init spring context on test database - END");
	}

	/**
	 * Verify that test data are relevant We simply checks that there is at least 1 environment in database
	 */
	private void assertTestDataIsRelevant() {
		logger.info("Verifying test database contains relevant data");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(datasource);
		int n = jdbcTemplate.queryForInt("select count(*) from environment where status <> 'REMOVED'");
		logger.debug("Test database contains " + n + " environments");
		assertTrue("Test data may not be relevant: it should includes at least one environemnt", n > 0);
	}

	/**
	 * Export database data into a file udsing pg_dump
	 * 
	 * @param dbHost
	 * @param dbPort
	 * @param dbName
	 * @param dbUser
	 * @param dbPassword
	 * @param exportFile
	 */
	private void exportData(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String exportFile) {
		execPgdump(dbHost, dbPort, dbName, dbUser, dbPassword, exportFile);
	}

	/**
	 * Purge database by dropping all tables and sequences using psql scripts
	 * 
	 * @param dbHost
	 * @param dbPort
	 * @param dbName
	 * @param dbUser
	 * @param dbPassword
	 */
	private void purgeDatabase(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword) {

		String deleteTablesScriptName = deleteTablesScriptFile.getPath();
		String deleteSequencesScriptName = deleteSequencesScriptFile.getPath();

		execPsql(dbHost, dbPort, dbName, dbUser, dbPassword, "-t", "-o", deleteTablesScriptName, "-c",
				"select 'drop table '||table_name||' cascade;' from information_schema.tables where table_schema='public' order by table_name;");
		execPsql(dbHost, dbPort, dbName, dbUser, dbPassword, "-q", "-f", deleteTablesScriptName);
		execPsql(dbHost, dbPort, dbName, dbUser, dbPassword, "-t", "-o", deleteSequencesScriptName, "-c",
				"select 'drop sequence '||sequence_name||';' from information_schema.sequences where sequence_schema='public' order by sequence_name;");
		execPsql(dbHost, dbPort, dbName, dbUser, dbPassword, "-q", "-f", deleteSequencesScriptName);
	}

	/**
	 * Import database data using psql
	 * 
	 * @param dbHost
	 * @param dbPort
	 * @param dbName
	 * @param dbUser
	 * @param dbPassword
	 * @param exportFile
	 */
	private void importData(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String exportFile) {
		execPsql(dbHost, dbPort, dbName, dbUser, dbPassword, "-f", exportFile);
	}

	/**
	 * Execute psql command
	 * 
	 * @param dbHost
	 * @param dbPort
	 * @param dbName
	 * @param dbUser
	 * @param dbPassword
	 * @param psqlOptions
	 */
	private void execPsql(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String... psqlOptions) {
		// variable used to store and display actual command that is executed
		String command = "";
		try {
			String[] psqlCommonOptions = { "psql", "-h", dbHost, // database host
					"-p", dbPort, // database port
					"-d", dbName, // database name
					"-U", dbUser, // database user
					"-w", // do not prompt for password
					"-v", "ON_ERROR_STOP=ON", // stop on first error
			};
			// Build the list of command options
			List<String> psqlCommands = new Vector<String>();
			for (String e : Arrays.asList(psqlCommonOptions))
				psqlCommands.add(e);
			for (String e : Arrays.asList(psqlOptions))
				psqlCommands.add(e);
			// Build and log command
			ProcessBuilder processBuilder = new ProcessBuilder(psqlCommands);
			for (String p : processBuilder.command())
				command = command + p + " ";
			logger.debug("Process command:" + command);
			// redirect error stream into stdout
			processBuilder.redirectErrorStream(true);
			// Password needs to be put in env variables as it can not be set as psql command parameter
			processBuilder.environment().put("PGPASSWORD", dbPassword);
			Process p = processBuilder.start();
			// read and log console messages
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			Vector<String> consoleMessages = new Vector<String>();
			String consoleMessage = null;
			while ((consoleMessage = stdInput.readLine()) != null) {
				logger.debug("Process console: " + consoleMessage);
				consoleMessages.add(consoleMessage);
			}
			// wait for process to complete
			int exitCode = p.waitFor();
			logger.debug("Process exit code is:" + exitCode);
			if (exitCode != 0) {
				// add the last 5 messages from console into exception
				int i0 = Math.max(0, consoleMessages.size() - 5);
				String lastMessages = "...";
				for (int i = i0; i < consoleMessages.size(); i++)
					lastMessages += "\n" + consoleMessages.get(i);
				throw new TechnicalException("psql command failed: " + command + " ; exit code = " + exitCode + "\n" + lastMessages);
			}
		} catch (IOException e) {
			logger.error("Exception during psql execution", e);
			throw new TechnicalException("psql command failed: " + command, e);
		} catch (InterruptedException e) {
			logger.error("Exception during psql execution", e);
			throw new TechnicalException("psql command failed: " + command, e);
		}

	}

	/**
	 * Execute pg_dump command
	 * 
	 * @param dbHost
	 * @param dbPort
	 * @param dbName
	 * @param dbUser
	 * @param dbPassword
	 * @param dumpFile
	 */
	private void execPgdump(String dbHost, String dbPort, String dbName, String dbUser, String dbPassword, String dumpFile) {
		// variable used to store and display actual command that is executed
		String command = "";
		try {
			ProcessBuilder processBuilder = new ProcessBuilder("pg_dump", "-h", dbHost, // database host
					"-p", dbPort, // database port
					"-n", "public", // export only public schema
					"-w", // do not prompt for password
					"-U", dbUser, // database user
					"-v", // verbose mode
					"-x", // do not export grants
					"-O", // do not export ownership
					"-f", dumpFile, // export file
					dbName // database name (must be last parameter)
			);
			// Build and log command
			for (String p : processBuilder.command())
				command = command + p + " ";
			logger.debug("Process command:" + command);
			// redirect error stream into stdout
			processBuilder.redirectErrorStream(true);
			// Password needs to be put in env variables as it can not be set as psql command parameter
			processBuilder.environment().put("PGPASSWORD", dbPassword);
			// Start execution
			Process p = processBuilder.start();
			// read and log console messages
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
			Vector<String> consoleMessages = new Vector<String>();
			String consoleMessage = null;
			while ((consoleMessage = stdInput.readLine()) != null) {
				logger.debug("Process console: " + consoleMessage);
				consoleMessages.add(consoleMessage);
			}
			// wait for process to complete
			int exitCode = p.waitFor();
			logger.debug("Process exit code is:" + exitCode);
			if (exitCode != 0) {
				// add the last 5 messages from console into exception
				int i0 = Math.max(0, consoleMessages.size() - 5);
				String lastMessages = "...";
				for (int i = i0; i < consoleMessages.size(); i++)
					lastMessages += "\n" + consoleMessages.get(i);
				throw new TechnicalException("pg_dump command failed: " + command + " ; exit code = " + exitCode + "\n" + lastMessages);
			}
		} catch (IOException e) {
			logger.error("Exception during pg_dump execution", e);
			throw new TechnicalException("pg_dump command failed: " + command, e);
		} catch (InterruptedException e) {
			logger.error("Exception during pg_dump execution", e);
			throw new TechnicalException("pg_dump command failed: " + command, e);
		}
	}
}
