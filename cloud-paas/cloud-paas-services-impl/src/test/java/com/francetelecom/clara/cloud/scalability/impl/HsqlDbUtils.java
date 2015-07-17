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
package com.francetelecom.clara.cloud.scalability.impl;

import com.francetelecom.clara.cloud.commons.file.FileHelper;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class HsqlDbUtils {
    private static final transient org.slf4j.Logger logger
            = LoggerFactory.getLogger(HsqlDbUtils.class);

    @PersistenceContext
    protected EntityManager em;

    public static int snapshotCount = 0;
    public static final String DB_SNAPSHOT_FILENAME = "dbSnapshot_X.txt";

    public void removeSnapshot(List<DbSnapshot> dbSnapshots) {
        for (DbSnapshot dbSnapshot : dbSnapshots) {
            File f = dbSnapshot.f;
            if (f.exists() && f.canWrite()) {
                f.delete();
            }
        }
    }

    public class DbSnapshot {
        File f;

        public DbSnapshot(File f) {
            this.f = f;
        }
    }

    public HsqlDbUtils() {
    }

    private String giveMeSnapshotName() {
        return DB_SNAPSHOT_FILENAME.replace("X", String.valueOf(snapshotCount++));

    }

    /**
     * make an hsql database snapshot (txt file)
     * SCRIPT native query is used
     *    doc : http://www.hsqldb.org/doc/2.0/guide/management-chapt.html#N144AE
     * @param deleteIfExists
     * @return
     * @throws Exception
     */
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.DEFAULT)
    public DbSnapshot makeDatabaseSnapshot(boolean deleteIfExists) throws Exception {
        File snapshotfile = new File(giveMeSnapshotName());
        if (snapshotfile.exists() && deleteIfExists) {
            snapshotfile.delete();
        }
        if (snapshotfile.exists()) {
            throw new Exception("unable to snapshot : file already exists " + snapshotfile.getAbsolutePath());
        }
        // String exportFileAbsolutePath = snapshotfile.getAbsolutePath();
        // String hsqldbExport = "SCRIPT " + exportFileAbsolutePath.replaceAll("\\\\","/");
        String hsqldbExport = "SCRIPT '" + snapshotfile.getName() + "'";
        logger.info("export query :{}", hsqldbExport);
        Query nativeQuery = em.createNativeQuery(hsqldbExport);
        nativeQuery.executeUpdate();
        return new DbSnapshot(snapshotfile);
    }

    String[] diffSnapShotExclusions = {
            "^CREATE USER SA",
            "^ALTER USER SA",
            "^SET FILES",
            "^SET DATABASE",
            "^SET SCHEMA",
            "^ALTER TABLE",
            "^CREATE MEMORY TABLE",
            "^GRANT ",
            "ID RESTART WITH",
            "^INSERT INTO HIBERNATE_SEQUENCES"
    };


    public void assertSnapshotEquals(String message, boolean shouldRemoveExclusions,
                                     DbSnapshot snapExpected, DbSnapshot snapActual) throws IOException {
        String snapExpectedContent = new String(FileHelper.getBytesFromFile(snapExpected.f));
        String snapActualContent = new String(FileHelper.getBytesFromFile(snapActual.f));

        String assertMessage = "database snapshots should be equals";
        if (message != null && !message.isEmpty()) {
            assertMessage = message;
        }
        if (shouldRemoveExclusions) {
            snapExpectedContent = removeExclusions(snapExpectedContent);
            snapActualContent = removeExclusions(snapActualContent);
            assertMessage += " *shouldRemoveExclusions enabled*";
        }
        assertEquals(assertMessage, snapExpectedContent, snapActualContent);
    }

    private String removeExclusions(String snapContent) {
        String cleanContent = "";
        String[] lines = snapContent.split("[\n\r]");
        for(String l:lines) {
            // String cleanLine = l.trim();
            if (!l.equals("")
             && !matchWithExclusions(l)) {
                cleanContent = cleanContent + l + "\n";
            }
        }
        return cleanContent;
    }

    private boolean matchWithExclusions(String cleanLine) {
        for (String diffSnapShotExclusion : diffSnapShotExclusions) {
            if (matchWithExlusion(cleanLine, diffSnapShotExclusion)){
                return true;
            }
        }
        return false;
    }

    private boolean matchWithExlusion(String cleanLine, String diffSnapShotExclusion) {
        if (diffSnapShotExclusion.startsWith("^")) {
            String startWithExclusion = diffSnapShotExclusion.substring(1);
            if (cleanLine.startsWith(startWithExclusion)) {
                return true;
            }
        }
        if (!diffSnapShotExclusion.startsWith("^")) {
            if (cleanLine.contains(diffSnapShotExclusion)) {
                return true;
            }
        }
        return false;
    }

    /** DBUnit
     **
     * dbunit not compatible with hsqldb 2.x
     *  erreur :
     *  org.dbunit.dataset.DataSetException: java.sql.SQLSyntaxErrorException: user lacks privilege or object not found: KEY
     *
     * CyclicTablesDependencyException
     *  post : http://dbunit.996259.n3.nabble.com/first-test-OK-but-then-I-get-an-exception-td62.html
     *  issue 169 : https://sourceforge.net/p/dbunit/feature-requests/169/
     * @param file
     * @throws java.sql.SQLException
     * @throws DatabaseUnitException
     * @throws java.io.IOException
    private void dbUnitmakeDatabaseSnapshot(File file) throws SQLException, DatabaseUnitException, IOException {
    logger.info("make a db snapshot into {}", file.getAbsolutePath());
    IDatabaseConnection connection = getConnexion();
    if (depTables == null) {
    logger.info("get env dep tables");
    depTables = TablesDependencyHelper.getAllDependentTables(connection, "PUBLIC.ENVIRONMENT");
    }
    IDataSet depDataset = connection.createDataSet( depTables );
    FlatXmlDataSet.write(depDataset, new FileOutputStream(file));
    // ITableFilter filter = new DatabaseSequenceFilter(connection);
    // IDataSet dataset    = new FilteredDataSet(filter, connection.createDataSet());
    // FlatXmlDataSet.write(dataset, new FileOutputStream(file));
    }

    private IDatabaseConnection getConnexion() throws SQLException, DatabaseUnitException {
    DatabaseConnection databaseConnection
    = new DatabaseConnection(emFactory.getDataSource().getConnection(), "public");
    DatabaseConfig config = databaseConnection.getConfig();
    config.setProperty(DatabaseConfig.PROPERTY_DATATYPE_FACTORY,
    // new PostgresqlDataTypeFactory());
    new HsqldbDataTypeFactory());
    config.setProperty(DatabaseConfig.FEATURE_QUALIFIED_TABLE_NAMES, true);
    return databaseConnection;
    }
     */
}

