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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.francetelecom.clara.cloud.commons.TechnicalException;

/**
 * Tool to run database scripts.
 */
public class SQLScriptRunner {

	private static Logger logger = LoggerFactory.getLogger(SQLScriptRunner.class);

	private static final String DEFAULT_DELIMITER = ";";

	private final Connection connection;
	private final boolean stopOnError;
	private final boolean autoCommit;

	private String delimiter = DEFAULT_DELIMITER;

	/**
	 * Default constructor.
	 * 
	 * @param connection
	 * @param autoCommit
	 * @param stopOnError
	 */
	public SQLScriptRunner(Connection connection, boolean autoCommit, boolean stopOnError) {
		this.connection = connection;
		this.autoCommit = autoCommit;
		this.stopOnError = stopOnError;
	}

	/**
	 * @param delimiter
	 * @param fullLineDelimiter
	 */
	public void setDelimiter(String delimiter) {
		this.delimiter = delimiter;
	}

	/**
	 * Runs an SQL script (read in using the Reader parameter).
	 * 
	 * @param reader
	 *            - the source of the script
	 * @throws SQLException
	 *             if any SQL errors occur
	 * @throws IOException
	 *             if there is an error reading from the Reader
	 */
	public void runScript(Reader reader) throws SQLException {
		try {
			boolean originalAutoCommit = connection.getAutoCommit();
			try {
				if (originalAutoCommit != autoCommit) {
					connection.setAutoCommit(autoCommit);
				}
				runScript(connection, reader);
			} finally {
				connection.setAutoCommit(originalAutoCommit);
			}
		} catch (SQLException e) {
			throw e;
		} catch (Exception e) {
			throw new TechnicalException("Error running script. Cause: " + e, e);
		}
	}

	interface StatementExecutor {

		void delegateExecuteStatement(String command) throws SQLException;

		void delegateCommitPreviousStatements() throws SQLException;

		void delegateRollbackConnection() throws SQLException;
	}

	/**
	 * Runs an SQL script (read in using the Reader parameter) using the connection passed in.
	 * 
	 * @param conn
	 *            - the connection to use for the script
	 * @param reader
	 *            - the source of the script
	 * @throws SQLException
	 *             if any SQL errors occur
	 * @throws IOException
	 *             if there is an error reading from the Reader
	 */
	private void runScript(final Connection conn, Reader reader) throws SQLException {
		StatementExecutor statementExecutor = new StatementExecutor() {
			@Override
			public void delegateExecuteStatement(String command) throws SQLException {
				executeStatement(conn, command);
			}

			@Override
			public void delegateCommitPreviousStatements() throws SQLException {
				commitPreviousStatements(conn);
			}

			@Override
			public void delegateRollbackConnection() throws SQLException {
				rollbackConnection(conn);
			}
		};

		processScript(reader, statementExecutor);
	}

	public void processScript(Reader reader, StatementExecutor statementExecutor) throws SQLException {
		String command = null;
		try {

			BufferedReader bufferedReader = new BufferedReader(reader);
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				sb.append(line);
				sb.append('\n');
			}
			List<String> statements = new ArrayList<String>();
			splitSqlScript(sb.toString(), delimiter, statements);

			for (String statement : statements) {
				command = statement;
				statementExecutor.delegateExecuteStatement(statement);
			}
			statementExecutor.delegateCommitPreviousStatements();
		} catch (SQLException e) {
			e.fillInStackTrace();
			logger.error("Error executing: " + command, e);
			throw new TechnicalException("SQLException while processing SQL script: " + e.getMessage()
					+ ". Hint: please check SQL file is UTF-8 encoded. Executed command was: " + command, e);
		} catch (IOException e) {
			e.fillInStackTrace();
			logger.error("Error executing: " + command, e);
			throw new TechnicalException("IOException while processing SQL script: " + e.getMessage()
					+ ". Hint: please check SQL file is UTF-8 encoded. Executed command was: " + command, e);
		} finally {
			statementExecutor.delegateRollbackConnection();
		}
	}

	private void rollbackConnection(Connection conn) throws SQLException {
		conn.rollback();
	}

	private void commitPreviousStatements(Connection conn) throws SQLException {
		if (!autoCommit) {
			conn.commit();
		}
	}

	private void executeStatement(Connection conn, String command) throws SQLException {
		Statement statement = conn.createStatement();

		logger.debug("command : " + command);

		boolean hasResults = false;
		if (stopOnError) {
			hasResults = statement.execute(command);
		} else {
			try {
				statement.execute(command);
			} catch (SQLException e) {
				e.fillInStackTrace();
				logger.error("Error executing: " + command, e);
			}
		}

		if (autoCommit && !conn.getAutoCommit()) {
			conn.commit();
		}

		ResultSet rs = statement.getResultSet();
		if (hasResults && rs != null) {
			ResultSetMetaData md = rs.getMetaData();
			int cols = md.getColumnCount();
			for (int i = 1; i <= cols; i++) {
				String name = md.getColumnLabel(i);
				logger.debug(name + "\t");
			}

			while (rs.next()) {
				for (int i = 1; i <= cols; i++) {
					String value = rs.getString(i);
					logger.debug(value + "\t");
				}

			}
		}

		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (statement != null)
				statement.close();
		} catch (Exception e) {
			e.printStackTrace();
			// Ignore to workaround a bug in Jakarta DBCP
		}
		Thread.yield();
	}

	/**
	 * Method extracted from spring JdbcUtils class and enhanced to handle '//', '#' and '/*' comments.
	 * <p>
	 * Split an SQL script into separate statements delimited by the provided delimiter string. Each individual
	 * statement will be added to the provided {@code List}.
	 * <p>
	 * Within a statement, the comment prefixes '//', '#' and '--' will be honored; any text beginning with these
	 * comment prefixes and extending to the end of the line will be omitted from the statement. In addition, comments
	 * between '/*' and '{@literal *}{@literal /}' will also be removed. Finally, multiple adjacent whitespace
	 * characters will be collapsed into a single space.
	 * 
	 * @param script
	 *            the SQL script
	 * @param delim
	 *            character delimiting each statement &mdash; typically a ';' character
	 * @param statements
	 *            the List that will contain the individual statements
	 */
	private static void splitSqlScript(String script, String delim, List<String> statements) {
		StringBuilder sb = new StringBuilder();
		boolean inLiteral = false;
		boolean inEscape = false;
		char[] content = script.toCharArray();
		for (int i = 0; i < script.length(); i++) {
			char c = content[i];
			if (inEscape) {
				inEscape = false;
				sb.append(c);
				continue;
			}
			// MySQL style escapes
			if (c == '\\') {
				inEscape = true;
				sb.append(c);
				continue;
			}
			if (c == '\'') {
				inLiteral = !inLiteral;
			}
			if (!inLiteral) {
				if (script.startsWith(delim, i)) {
					// we've reached the end of the current statement
					if (sb.length() > 0) {
						statements.add(sb.toString());
						sb = new StringBuilder();
					}
					i += delim.length() - 1;
					continue;
				} else if (script.startsWith("--", i) || script.startsWith("//", i) || script.startsWith("#", i)) {
					// skip over any content from the start of the comment to the EOL
					int indexOfNextNewline = script.indexOf("\n", i);
					if (indexOfNextNewline > i) {
						i = indexOfNextNewline;
						continue;
					} else {
						// if there's no newline after the comment, we must be at the end
						// of the script, so stop here.
						break;
					}
				} else if (script.startsWith("/*", i)) {
					// skip to the end of comment
					int indexOfCommenEnding = script.indexOf("*/", i + 2);
					if (indexOfCommenEnding > i) {
						i = indexOfCommenEnding + 1;
						continue;
					} else {
						// if there's no comment ending, we must be at the end
						// of the script, so stop here.
						break;
					}
				} else if (c == ' ' || c == '\n' || c == '\t') {
					// avoid multiple adjacent whitespace characters
					if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
						c = ' ';
					} else {
						continue;
					}
				}
			}
			sb.append(c);
		}
		if (StringUtils.hasText(sb)) {
			statements.add(sb.toString());
		}
	}

}
