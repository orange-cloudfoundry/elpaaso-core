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

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionSqlDialectEnum;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import org.apache.commons.io.input.BOMInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Helper class to factor out the sql connection in DbaasConsummerImpl (and reuse in CF implementation)
 */
public class DbHelper {

    private static Logger logger = LoggerFactory.getLogger(DbHelper.class);


	public static void launchPopulationScript(DBaasSubscriptionV2 dbaasSubscription, MvnRepoDao mvnDao) {
        MavenReference initialPopulationScript = dbaasSubscription.getInitialPopulationScript();
        if (initialPopulationScript != null) {
            try {
                // All connector point to the same database so we just get URL
                // from the first connector
                MavenReference initialPopulationScriptResolved = mvnDao.resolveUrl(initialPopulationScript);
                URL sqlUrl = initialPopulationScriptResolved.getAccessUrl();
                if (sqlUrl == null) {
                    throw new TechnicalException("accessUrl of SQL script (maven reference) is null: " + initialPopulationScript.toGavString());
                }
                switch (dbaasSubscription.getSqlDialect()) {
                    case POSTGRESQL_DEFAULT:
                        Class.forName("org.postgresql.Driver");
                        break;
                    case MYSQL_DEFAULT:
                        Class.forName("com.mysql.jdbc.Driver");
                        break;
                    default:
                        logger.warn("Cannot load driver class: unknown SQL version " + dbaasSubscription.getSqlDialect().name());
                }

				launchPopulationScript(sqlUrl, getDriverProtocol(dbaasSubscription.getSqlDialect()), dbaasSubscription.getHostname(),
						dbaasSubscription.getPort(), dbaasSubscription.getDbname(), dbaasSubscription.getUserName(), dbaasSubscription.getUserPassword());
            } catch (ClassNotFoundException e) {
                throw new TechnicalException(e);
            }
        }
    }

    private static String getDriverProtocol(DBaasSubscriptionSqlDialectEnum dialect) {
    	   switch (dialect) {
    	   case MYSQL_DEFAULT:
    		   return "jdbc:mysql";
           case POSTGRESQL_DEFAULT:
               return "jdbc:postgresql";
           default:
        	   throw new TechnicalException("cannot determine diver protocol for sql dialect : " + dialect);
       }
	}

	public static void launchPopulationScript(URL sqlUrl, String scheme, String host, long port, String dbName, String user, String password) {

        logger.debug("launchSqlScripts for dbaasSubscription " + sqlUrl);
        Reader reader = null;
        String jdbcUrl = scheme + "://" + host + ":" + port + "/" + dbName;
        try {
            DriverManager.setLoginTimeout(60);
            Connection connection = DriverManager.getConnection(jdbcUrl, user, password);
            // Autocommit = false and stopOnError = true
            SQLScriptRunner scriptRunner = new SQLScriptRunner(connection, false, true);

			runScript(sqlUrl, scriptRunner);

        } catch (SQLException e) {
            throw new TechnicalException("SQLException during launchPopulationScript on  : " + jdbcUrl + " / " + e.getMessage(), e);
        } catch (IOException e) {
            throw new TechnicalException("IOEXception during launchPopulationScript on  : " + jdbcUrl + " / " + e.getMessage(), e);
		}
    }

	static void runScript(URL sqlUrl, SQLScriptRunner scriptRunner) throws SQLException, IOException {
		Reader reader;
		try (InputStream scriptInputStream = sqlUrl.openStream()) {
			logger.debug("Removing BOM marker if present");
			BOMInputStream bOMInputStream = new BOMInputStream(scriptInputStream);
			reader = new InputStreamReader(bOMInputStream, Charset.forName("UTF-8"));
			scriptRunner.runScript(reader);
		}
	}
}