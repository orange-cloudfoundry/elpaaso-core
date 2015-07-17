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
package com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure;

import com.francetelecom.clara.cloud.activation.plugin.dbaas.DBaasConsumer;
import com.francetelecom.clara.cloud.activation.plugin.dbaas.TestUtils;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionSqlDialectEnum;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.francetelecom.clara.cloud.techmodel.dbaas.DbAccessInfo;
import com.francetelecom.clara.cloud.techmodel.dbaas.DbConnectionDetails;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * DBaasConsumerITHelper
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public class DBaasConsumerITHelper {
    private static Logger logger = LoggerFactory.getLogger(DBaasConsumerITHelper.class);
    public static final String GAV_MYSQL_POPULATION_SCRIPT
 = "com.francetelecom.clara.prototype.commons:commons-sql-mysql:7.0.1::sql";
    public static final String GAV_POSTGRESQL_POPULATION_SCRIPT
 = "com.francetelecom.clara.prototype.commons:commons-sql-postgresql:7.0.1::sql";
    public static final int POLL_DELAY_MS = 2000;

    /**
     * dbaas consyumer to test
     */
    private DBaasConsumer dBaasConsumer;

    /**
     * test databases
     */
    private static List<DBaasSubscriptionV2> toDelete = new ArrayList<DBaasSubscriptionV2>();

    public DBaasConsumerITHelper(DBaasConsumer dBaasConsumer) {
        this.dBaasConsumer = dBaasConsumer;
    }


    public DBaasSubscriptionV2 createAndTestDBaaS(DBaasSubscriptionSqlDialectEnum sqlDialect) {

        String localhostIp = "<Unknown localhost IP address>";
        try {
            localhostIp = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            logger.error("Unable to resolve local host address", e);
        }
        String description = "DBaasConsumerITHelper run by user=[" + System.getProperty("user.name") + "] from host with ip=[" + localhostIp + "]";

        TechnicalDeployment td = new TechnicalDeployment("td");
        DBaasSubscriptionV2 dbaasSub = new DBaasSubscriptionV2(td);
        dbaasSub.setSqlDialect(sqlDialect);
        dbaasSub.changeStorageCapacity(500);
        dbaasSub.setDescription(description);
 

        // Add subscription to the delete list before creating it because
        // we want to delete eventual allocated resources even in error cases
        toDelete.add(dbaasSub);

        // Call the dBaasConsumer to create database
        TestUtils.waitForTaskCompletion(dBaasConsumer,
                dBaasConsumer.createDatabase(dbaasSub, "[TI DBaasConsumerITHelper.createAndTestDBaaS date=" + (new Date()).toString() + "]"), dbaasSub, 2000);

        // In case of exception : don't delete the DB to debug pbs on DBAAS
        // toDelete.add(dbaasSub);

        DbAccessInfo dbAccessInfo = dBaasConsumer.fetchDatabaseDescription(dbaasSub.getDatabaseUUId());
		
        dbaasSub.activate(dbAccessInfo);

		// Check engine and test connection (executes few queries)
        assertEquals(dbaasSub.getSqlDialect(), determineAndTestDBEngine(dbaasSub));

        // try stopping BDD
        TestUtils.waitForTaskCompletion(dBaasConsumer, dBaasConsumer.stopDatabase(dbaasSub), dbaasSub, POLL_DELAY_MS);

        // try starting BDD
        TestUtils.waitForTaskCompletion(dBaasConsumer, dBaasConsumer.startDatabase(dbaasSub), dbaasSub, POLL_DELAY_MS);

        return dbaasSub;
    }


    private DBaasSubscriptionSqlDialectEnum determineAndTestDBEngine(DbConnectionDetails dbDescription) {
        DBaasSubscriptionSqlDialectEnum engine;

        // First try mysql
        engine = attemptConnectToEngine(dbDescription, DBaasSubscriptionSqlDialectEnum.POSTGRESQL_DEFAULT, "postgresql",
                "org.postgresql.Driver");

        if (engine == null) {
            // Otherwise, check pgresql
            engine = attemptConnectToEngine(dbDescription, DBaasSubscriptionSqlDialectEnum.MYSQL_DEFAULT, "mysql", "com.mysql.jdbc.Driver");
        }
        return engine;
    }

    /**
     * @return the provided expectedEngineEnum engine or null if connection attempt or SQL statements failed
     */
    private DBaasSubscriptionSqlDialectEnum attemptConnectToEngine(DbConnectionDetails dbDescription,
                                                                   DBaasSubscriptionSqlDialectEnum expectedEngineEnum, String dialect, String driverName) {
        if (canLoadDriver(driverName)) {
            String jdbcUrl = constructJdbcUrl(dbDescription, dialect);


            try {
                connectToDbAndExecuteSampleQuery(dbDescription.getUserName(), dbDescription.getUserPassword(), jdbcUrl);
            } catch (SQLException e) {
                logger.warn("SQLException while trying getConnection with " + dialect + " driver : " + e.getMessage());
                return null;
            }
            return expectedEngineEnum;
        }
        return null;
    }

    private boolean canLoadDriver(String className) {
        try {
            Class.forName(className);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }

    private void connectToDbAndExecuteSampleQuery(String bddUsername, String bddUserPwd, String jdbcUrl) throws SQLException {
        DriverManager.setLoginTimeout(60);
        Connection connection = DriverManager.getConnection(jdbcUrl, bddUsername, bddUserPwd);
        if (connection != null) {
            // Test some SQL queries
            Statement sql = connection.createStatement();
            String sqlText = "create table paas_info (code int, text varchar(20))";
            sql.executeUpdate(sqlText);
            sqlText = "insert into paas_info values (1,'One')";
            sql.executeUpdate(sqlText);
            ResultSet results = sql.executeQuery("select * from paas_info");
            if (results != null) {
                while (results.next()) {
                    logger.debug("SQL RESULT : code = " + results.getInt("code") + "; text = " + results.getString(2) + "\n");
                }
                results.close();
            }
            sql.close();
            connection.close();
            logger.info("SQL execution succeed on {}", jdbcUrl);
        } else {
            throw new TechnicalException("Problem during DBaaS instance : " + jdbcUrl + " connection test.");
        }
    }

    private String constructJdbcUrl(DbConnectionDetails dbDescription, String dialect) {
        String jdbcUrl = "jdbc:" + dialect + "://" + dbDescription.getHostname() + ":" + dbDescription.getPort() + "/" + dbDescription.getDbname();
        logger.debug("jdbcUrl : " + jdbcUrl + " / username-password : " + dbDescription.getUserName() + "-" + dbDescription.getUserPassword());
        return jdbcUrl;
    }

    public void should_support_mysql_dialect() {
        // Create and test a database
        DBaasSubscriptionV2 dbaasSub = createAndTestDBaaS(DBaasSubscriptionSqlDialectEnum.MYSQL_DEFAULT);
        // Test SQL script
        dbaasSub.setInitialPopulationScript(
                MavenReference.fromGavString(GAV_MYSQL_POPULATION_SCRIPT));
		dBaasConsumer.launchPopulationScript(dbaasSub);
        // Delete it
        TestUtils.waitForTaskCompletion(dBaasConsumer, dBaasConsumer.deleteDatabase(dbaasSub), dbaasSub, POLL_DELAY_MS);
        toDelete.remove(dbaasSub);
    }

    @Test
    public void should_support_postgresql_dialect() {
        // Create and test a database
        DBaasSubscriptionV2 dbaasSub = createAndTestDBaaS(DBaasSubscriptionSqlDialectEnum.POSTGRESQL_DEFAULT);
        // Test SQL script
        dbaasSub.setInitialPopulationScript(
                MavenReference.fromGavString(GAV_POSTGRESQL_POPULATION_SCRIPT));
		dBaasConsumer.launchPopulationScript(dbaasSub);
        // Delete it
        TestUtils.waitForTaskCompletion(dBaasConsumer, dBaasConsumer.deleteDatabase(dbaasSub), dbaasSub, POLL_DELAY_MS);
        toDelete.remove(dbaasSub);
    }

    public void tearDown() {
        int nbDbToDelete = toDelete.size();
        if (nbDbToDelete == 0) {
            return;
        }
        logger.info("tearDown of DBaasConsumerITHelper: " + nbDbToDelete + " bdd(s) to delete.");
        for (DBaasSubscriptionV2 sub : toDelete) {
            TestUtils.waitForTaskCompletion(dBaasConsumer, dBaasConsumer.deleteDatabase(sub), sub, POLL_DELAY_MS);
        }
        toDelete.clear();
    }
}
