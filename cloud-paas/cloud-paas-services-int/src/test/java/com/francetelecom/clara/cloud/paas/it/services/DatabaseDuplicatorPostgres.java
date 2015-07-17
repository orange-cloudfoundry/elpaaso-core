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
package com.francetelecom.clara.cloud.paas.it.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.postgresql.core.BaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.TechnicalException;

public class DatabaseDuplicatorPostgres {
	public static String DUPLICATOR_PGSQL_ELPAASO_INSTANCE = "duplicatorPgElpaasoInstance";

	protected static Logger logger = LoggerFactory.getLogger(DatabaseDuplicatorPostgres.class.getName());

	String pgdumpPath = null;
	String psqlPath = null;

	String dbToCopyDBname;
	String dbToCopyHostname;
	String dbToCopyPort;
	String dbToCopyUser;
	String dbToCopyPassword;

	String dbHostname;
	String dbPort;
	String dbName;
	String dbUser;
	String dbPassword;
	
	String datacenter;

	/**
	 * Called by Spring
	 */
	public void init() throws ClassNotFoundException, SQLException, IOException {
		String elpaasoInstance = System.getProperty(DUPLICATOR_PGSQL_ELPAASO_INSTANCE);
		if (elpaasoInstance != null) {
			logger.info("Using instance '"+elpaasoInstance+"' to copy database");
			ResourceBundle bundle = ResourceBundle.getBundle("com.francetelecom.clara.cloud.commons.testconfigurations.credentials-"+datacenter);
			setDbToCopyHostname(bundle.getString("test.monitoring.postgresql_db_"+elpaasoInstance+"_hostname"));
			setDbToCopyPort(bundle.getString("test.monitoring.postgresql_db_"+elpaasoInstance+"_port"));
			setDbToCopyDBname(bundle.getString("test.monitoring.postgresql_db_"+elpaasoInstance+"_dbname"));
			setDbToCopyUser(bundle.getString("test.monitoring.postgresql_db_"+elpaasoInstance+"_user"));
			setDbToCopyPassword(bundle.getString("test.monitoring.postgresql_db_"+elpaasoInstance+"_password"));
		}
		else {
			throw new TechnicalException("You must define system property "+DUPLICATOR_PGSQL_ELPAASO_INSTANCE);
		}
		copyDatabase();
	}

	/**
	 * Copy data and structure from dbToCopy to db
	 */
	public void copyDatabase() throws ClassNotFoundException, SQLException, IOException {
		if (pgdumpPath != null && psqlPath != null) {
			{
				final List<String> baseCmds = new ArrayList<String>();
				baseCmds.add(pgdumpPath);
				baseCmds.add("-h");
				baseCmds.add(dbToCopyHostname);
				baseCmds.add("-p");
				baseCmds.add(dbToCopyPort);
				baseCmds.add("-U");
				baseCmds.add(dbToCopyUser);
				baseCmds.add("-b"); // blog
				// baseCmds.add("-v"); // verbose
				baseCmds.add("-O");
				baseCmds.add("-x");
				baseCmds.add("-f");
				baseCmds.add("backup-" + dbToCopyDBname + ".sql");
				baseCmds.add(dbToCopyDBname);
				final ProcessBuilder pb = new ProcessBuilder(baseCmds);
				pb.redirectErrorStream(true);

				// Set the password
				final Map<String, String> env = pb.environment();
				env.put("PGPASSWORD", dbToCopyPassword);

				try {
					logger.info("Backup database: " + baseCmds.toString());
					final Process process = pb.start();

					final BufferedReader cin = new BufferedReader(new InputStreamReader(process.getInputStream()));
					int charin = cin.read();
					while (charin != -1) {
						System.err.print((char) charin);
						charin = cin.read();
					}
					cin.close();

					final int dcertExitCode = process.waitFor();
					logger.info(dcertExitCode + " returned by command " + pb.toString());
					if (dcertExitCode != 0) {
						new TechnicalException("Error during backup (see logs)");
					}

				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					throw e;
				} catch (InterruptedException ie) {
					logger.error(ie.getMessage(), ie);
					throw new TechnicalException(ie.getMessage(), ie);
				}
			}
			{
				Class.forName("org.postgresql.Driver");
				BaseConnection connectionTo = (BaseConnection) DriverManager.getConnection("jdbc:postgresql://" + dbHostname + ":" + dbPort + "/" + dbName,
						dbUser, dbPassword);
				ResultSet result = connectionTo
						.execSQLQuery("SELECT 'DROP TABLE ' || table_name || ' CASCADE ;' FROM information_schema.tables WHERE table_schema='public' AND table_type='BASE TABLE';");
				while (result.next()) {
					logger.debug(result.getString(1));
					connectionTo.execSQLUpdate(result.getString(1));
				}
				result = connectionTo
						.execSQLQuery("SELECT 'DROP SEQUENCE ' || sequence_name || ';' FROM information_schema.sequences WHERE sequence_schema='public';");
				while (result.next()) {
					logger.debug(result.getString(1));
					connectionTo.execSQLUpdate(result.getString(1));
				}

				final List<String> baseCmds = new ArrayList<String>();
				baseCmds.add(psqlPath);
				baseCmds.add("-h");
				baseCmds.add(dbHostname);
				baseCmds.add("-p");
				baseCmds.add(dbPort);
				baseCmds.add("-U");
				baseCmds.add(dbUser);
				baseCmds.add("-d");
				baseCmds.add(dbName);
				// baseCmds.add("-q"); // Quiet
				baseCmds.add("-f");
				baseCmds.add("backup-" + dbToCopyDBname + ".sql");
				final ProcessBuilder pb = new ProcessBuilder(baseCmds);
				pb.redirectErrorStream(true);

				try {
					logger.info("Restore database: " + baseCmds.toString());
                    // Set the password
                    final Map<String, String> env = pb.environment();
                    env.put("PGPASSWORD", dbPassword);
                    final Process process = pb.start();
					OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
					writer.append(dbPassword);
					writer.flush();

					final BufferedReader cin = new BufferedReader(new InputStreamReader(process.getInputStream()));
					int charin = cin.read();
					while (charin != -1) {
						System.err.print((char) charin);
						charin = cin.read();
					}
					cin.close();

					final int dcertExitCode = process.waitFor();
					logger.info(dcertExitCode + " returned by command " + pb.toString());
					if (dcertExitCode != 0) {
						new TechnicalException("Error during restore (see logs)");
					}

				} catch (IOException e) {
					logger.error(e.getMessage(), e);
					throw e;
				} catch (InterruptedException ie) {
					logger.error(ie.getMessage(), ie);
					throw new TechnicalException(ie.getMessage(), ie);
				}
			}
		}
		else {
			throw new IllegalArgumentException("You must set test.monitoring.pgdump and test.monitoring.psql properties in order to use DatabaseDuplicatorPostgres");
		}
	}

	public void setDbUser(String dbUser) {
		this.dbUser = dbUser;
	}

	public void setDbPassword(String dbPassword) {
		this.dbPassword = dbPassword;
	}

	public void setDbToCopyUser(String dbToCopyUser) {
		this.dbToCopyUser = dbToCopyUser;
	}

	public void setDbToCopyPassword(String dbToCopyPassword) {
		this.dbToCopyPassword = dbToCopyPassword;
	}

	public void setDbToCopyDBname(String dbToCopyDBname) {
		this.dbToCopyDBname = dbToCopyDBname;
	}

	public void setDbToCopyHostname(String dbToCopyHostname) {
		this.dbToCopyHostname = dbToCopyHostname;
	}

	public void setDbToCopyPort(String dbToCopyPort) {
		this.dbToCopyPort = dbToCopyPort;
	}

	public void setDbHostname(String dbHostname) {
		this.dbHostname = dbHostname;
	}

	public void setDbPort(String dbPort) {
		this.dbPort = dbPort;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public void setPgdumpPath(String pgdumpPath) {
		this.pgdumpPath = pgdumpPath;
	}

	public void setPsqlPath(String psqlPath) {
		this.psqlPath = psqlPath;
	}

	public void setDatacenter(String datacenter) {
		this.datacenter = datacenter;
	}
}
