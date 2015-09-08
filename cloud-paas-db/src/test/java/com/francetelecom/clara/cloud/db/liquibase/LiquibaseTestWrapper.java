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

import liquibase.CatalogAndSchema;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.ObjectDifferences;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.structure.DatabaseObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;

/**
 * Test helper class  wrapping liquibase functions
 * Note that using Main class is not correct as it does an exit and hence stops the jvm
 */
public class LiquibaseTestWrapper {

    private static Logger logger = LoggerFactory.getLogger(LiquibaseTestWrapper.class.getName());

    /**
     * Purge a database using dropAll liquibase function
     *
     * @param dbUrl
     * @param dbUser
     * @param dbPassword
     * @throws SQLException
     * @throws LiquibaseException
     */
    public void purgeDatabase(String dbUrl, String dbUser, String dbPassword) throws SQLException, LiquibaseException {
        logger.debug("Purging database " + dbUrl);
        Database db = null;
        String nullChangeLogFile = null;
        try {
            db = createDatabase(dbUrl, dbUser, dbPassword);
            Liquibase liquibase = new Liquibase(nullChangeLogFile, null, db);
            liquibase.forceReleaseLocks();
            liquibase.dropAll();
        } finally {
            closeDatabase(db);
        }
    }

    /**
     * Apply change log using liquibase update function
     *
     * @param dbUrl
     * @param dbUser
     * @param dbPassword
     * @param changeLogFile
     * @throws SQLException
     * @throws LiquibaseException
     */
    public void applyChangeLog(String dbUrl, String dbUser, String dbPassword, String changeLogFile) throws SQLException, LiquibaseException {
        logger.debug("Applying changes in database " + dbUrl + " using " + changeLogFile);
        Database db = null;
        try {
            db = createDatabase(dbUrl, dbUser, dbPassword);
            ResourceAccessor resourceAccessor = null;
            if (changeLogFile.startsWith("classpath:")) {
                resourceAccessor = new ClassLoaderResourceAccessor();
                changeLogFile = changeLogFile.substring(10);
            } else {
                resourceAccessor = new FileSystemResourceAccessor(".");
            }

            Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, db);

            liquibase.update("");
        } finally {
            logger.debug("closing connection to:" + db);
            closeDatabase(db);
        }
    }

    /**
     * call syncChangeLog function of liquibase
     *
     * @param dbUrl
     * @param dbUser
     * @param dbPassword
     * @param changeLogFile
     * @throws SQLException
     * @throws LiquibaseException
     */
    public void syncChangeLog(String dbUrl, String dbUser, String dbPassword, String changeLogFile) throws SQLException, LiquibaseException {
        logger.debug("Sync changes in database " + dbUrl + " using " + changeLogFile);
        Database db = null;
        try {
            ResourceAccessor resourceAccessor = null;
            db = createDatabase(dbUrl, dbUser, dbPassword);
            if (changeLogFile.startsWith("classpath:")) {
                resourceAccessor = new ClassLoaderResourceAccessor();
                changeLogFile = changeLogFile.substring(10);
            } else {
                resourceAccessor = new FileSystemResourceAccessor(".");
            }

            Liquibase liquibase = new Liquibase(changeLogFile, resourceAccessor, db);

            liquibase.changeLogSync("");
        } finally {
            closeDatabase(db);
        }
    }

    /**
     * Generate liquibase changeLog using liquibase
     *
     * @param dbUrl
     * @param dbUser
     * @param dbPassword
     * @param changeLogFile
     * @return true if changeLog is not empty
     * @throws LiquibaseException
     * @throws IOException
     * @throws SQLException
     * @throws ParserConfigurationException
     */
    public void generateChangeLog(String dbUrl, String dbUser, String dbPassword, String changeLogFile) throws LiquibaseException, IOException, SQLException, ParserConfigurationException {
        logger.info("Generating changelog from database {}@{} to {}", dbUser, dbUrl, changeLogFile);
        if (changeLogFile == null) {
            changeLogFile = ""; //will output to stdout
        }
        removeExistingChangeLog(changeLogFile);


        Database db = createDatabase(dbUrl, dbUser, dbPassword);

        DiffOutputControl requireTablespaceForDiff = getTablespaceOnlyDiff();


        CatalogAndSchema[] defaultCatalogAndSchema = new CatalogAndSchema[]{CatalogAndSchema.DEFAULT};
        String requireAllTypesForSnapshot = null;
        try {
            CommandLineUtils.doGenerateChangeLog(changeLogFile, db, defaultCatalogAndSchema, requireAllTypesForSnapshot, "paas", null, null, requireTablespaceForDiff);

        } finally {
            closeDatabase(db);
        }
    }

    public DiffOutputControl getTablespaceOnlyDiff() {
        boolean includeCatalog = false;
        boolean includeSchema = false;
        boolean includeTablespace = true;
        return new DiffOutputControl(includeCatalog, includeSchema, includeTablespace);
    }

    private void removeExistingChangeLog(String changeLogFile) {
        // By default the generateChangeLog command is destructive, and
        // Liquibase's attempt to append doesn't work properly. Delete
        // fail the build if the file already exists.
        File file = new File(changeLogFile);
        if (file.exists()) {
            logger.warn("ChangeLogFile {} already exists, deleting it!", changeLogFile);
            file.delete();
        }
    }

    /**
     * Compare 2 databases using diff liquibase function
     *
     * @param paasTestDbUrl
     * @param paasTestDbUser
     * @param paasTestDbPassword
     * @param paasTestDb2Url
     * @param paasTestDb2User
     * @param paasTestDb2Password
     * @return SimpleDiffResult containing a flag indicating if diffreneces have been found and a text representation of those differences
     * @throws SQLException
     * @throws LiquibaseException
     * @throws IOException
     * @throws ParserConfigurationException
     */
    public DiffResult diff(String paasTestDbUrl, String paasTestDbUser, String paasTestDbPassword, String paasTestDb2Url, String paasTestDb2User,
                           String paasTestDb2Password) throws SQLException, LiquibaseException, IOException, ParserConfigurationException {

        logger.debug("Running liquibase diff between db: " + paasTestDbUrl + " and db: " + paasTestDb2Url);
        Database referenceDatabase = null;
        Database targetDatabase = null;

        try {
            referenceDatabase = createDatabase(paasTestDbUrl, paasTestDbUser, paasTestDbPassword);
            targetDatabase = createDatabase(paasTestDb2Url, paasTestDb2User, paasTestDb2Password);

            final DiffGeneratorFactory generatorFactory = DiffGeneratorFactory.getInstance();
            final CompareControl compareControl = new CompareControl();


            final DiffResult diffResult = generatorFactory.compare(referenceDatabase, targetDatabase, compareControl);

            boolean ignoreDefaultValueDifference = false;
            if (ignoreDefaultValueDifference) {
                Map<DatabaseObject, ObjectDifferences> changedObjects = diffResult.getChangedObjects();
                for (DatabaseObject changedDbObject : changedObjects.keySet()) {
                    ObjectDifferences objectDifferences = changedObjects.get(changedDbObject);
                    if (objectDifferences.removeDifference("defaultValue")) {
                        logger.info("Ignoring default value for {}", changedDbObject.toString());
                    }
                    if (!objectDifferences.hasDifferences()) {
                        logger.info("removing {}, no difference left.", changedDbObject.toString());
                        changedObjects.remove(objectDifferences);
                    }
                }
            }

            return diffResult;

        } finally {
            closeDatabase(referenceDatabase);
            closeDatabase(targetDatabase);
        }
    }

    /**
     * Utility method to close a liquibase database
     *
     * @param db
     */
    private void closeDatabase(Database db) {
        if (db != null) {
            try {
                logger.debug("closing connection to:" + db);
                db.close();
            } catch (DatabaseException e) {
                logger.error("unable to close database " + db + " exception:" + e.getMessage());
            }
        }

    }

    /**
     * Utility method to get a liquibase database object
     *
     * @param dbUrl
     * @param dbUser
     * @param dbPassword
     * @return
     * @throws SQLException
     * @throws LiquibaseException
     */
    private Database createDatabase(String dbUrl, String dbUser, String dbPassword) throws SQLException, LiquibaseException {
        Connection c = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
        JdbcConnection liquibaseDbConnection = new JdbcConnection(c);

        Liquibase liquibase = new Liquibase(null, null, liquibaseDbConnection);

        return liquibase.getDatabase();
    }

}
