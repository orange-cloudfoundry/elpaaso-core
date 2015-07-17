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
package com.francetelecom.clara.cloud.activation.plugin.dbaas;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.activation.plugin.dbaas.SQLScriptRunner;

@RunWith(MockitoJUnitRunner.class)
public class SQLScriptRunnerTest {

	@Test
	public void testSingleLine() throws IOException, SQLException {
		processScriptAndDumpOutputToDisk("single_line.sql", "UTF-16LE");
	}

	@Test
	public void testRealWorldExtract() throws IOException, SQLException {
		processScriptAndDumpOutputToDisk("extracted.sql", "UTF-8");
	}

	@Test
	public void testSimulateurRetraite() throws IOException, SQLException {
		processScriptAndDumpOutputToDisk("simulateurRetraite.sql", "UTF-16LE");
	}

	@Test
	public void testMultiLines() throws IOException, SQLException {
		processScriptAndDumpOutputToDisk("multiline.sql", "UTF-16LE");
	}

	@Test
	public void testProblematicTwoLines() throws IOException, SQLException {
		processScriptAndDumpOutputToDisk("problematic_two_lines.sql", "UTF-16LE");
	}

	@Test
	public void testSpringoo() throws IOException, SQLException {
		processScriptAndDumpOutputToDisk("springoo.sql", "UTF-8");
	}

	public void processScriptAndDumpOutputToDisk(String scriptName, String charsetName) throws IOException, SQLException {

		InputStream inputStream = SQLScriptRunnerTest.class.getClassLoader().getResourceAsStream("sql/" + scriptName);
		InputStreamReader reader = new InputStreamReader(inputStream, charsetName);
		final OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("target/processed_" + scriptName), charsetName);
		SQLScriptRunner sqlScriptRunner = new SQLScriptRunner(mock(Connection.class), true, true);
		SQLScriptRunner.StatementExecutor statementExecutor = new SQLScriptRunner.StatementExecutor() {
			@Override
			public void delegateExecuteStatement(String command) throws SQLException {
				try {
					writer.write(command);
					writer.write(";\n");
				} catch (IOException e) {
					Assert.fail(e.getMessage());
				}
			}

			@Override
			public void delegateCommitPreviousStatements() throws SQLException {
				try {
					writer.flush();
				} catch (IOException e) {
					Assert.fail(e.getMessage());
				}
			}

			@Override
			public void delegateRollbackConnection() throws SQLException {

			}
		};
		sqlScriptRunner.processScript(reader, statementExecutor);
		writer.close();
		assertThat(new File("src/test/resources/sql/" + scriptName + ".ref")).hasSameContentAs(new File("target/processed_" + scriptName));
	}
}
