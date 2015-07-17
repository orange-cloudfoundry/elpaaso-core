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
package com.francetelecom.clara.cloud.logicalmodel.samplecatalog;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalServiceAccessTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * Supports testing javax.sql.DataSource (i.e. the relational dbs in the service
 * catalog)
 */
public class DatasourcesLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	@Autowired
	private SampleAppProperties sampleAppProperties;

	boolean testMysql = true;

	boolean testPostgres = true;

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}

		// EAR
		ProcessingNode logicalExecNode = createJeeProcessing(this, "DataSourcesJEE", "datasource-probe");
		existingLDToUpdate.addExecutionNode(logicalExecNode);

		// WebGUI
		LogicalWebGUIService web = createLogicalWebGuiService("DataSourcesEchoConsumerWebUi", "datasource-probe", true, false, 1, 20);
		existingLDToUpdate.addLogicalService(web);
		logicalExecNode.addLogicalServiceUsage(web, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		
		if (testMysql) {
			// Datasource MySQL
			LogicalRelationalService dsMysql = createLogicalRelationalService(this, "DataSourceMySql", "jndi/dsMysql",
					LogicalRelationalServiceSqlDialectEnum.MYSQL_DEFAULT, 10, null);
			existingLDToUpdate.addLogicalService(dsMysql);
			logicalExecNode.addLogicalServiceUsage(dsMysql, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		}

		if (testPostgres) {
			// Datasource Postgresql
			LogicalRelationalService dsPostgresql = createLogicalRelationalService(this, "DataSourcePostgreSql", "jndi/dsPostgresql",
					LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT, 10, null);
			existingLDToUpdate.addLogicalService(dsPostgresql);
			logicalExecNode.addLogicalServiceUsage(dsPostgresql, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
		}
		
		return existingLDToUpdate;
	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	/**
	 * @param baseUrl Correspond to the context root of the EAR to test. Can be use to
	 *          filter multiples EAR tests. Ignore it if you just have one EAR.
	 * @return list of urls and corresponding keywords used to check application
	 *         deployment
	 */
	@Override
	public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		return urls;
	}

	@Override
	public String getAppCode() {
		return "MyDataSourcesSampleCODE";
	}

	@Override
	public String getAppLabel() {
		return "MyDataSourcesSample";
	}

	@Override
	public String getAppReleaseDescription() {
		return "MyDataSourcesSample release description";
	}

	@Override
	public String getAppReleaseVersion() {
		return "G00R01";
	}

	@Override
	public String getAppDescription() {
		return "A sample app that demonstrates the use of the relational database service, also used by engineering team as an integration test. There is not yet UI to access it. Datasources are loaded on demand following a spring RPC call on the WebGui access point.";
	}

	public boolean isTestMysql() {
		return testMysql;
	}

	public void setTestMysql(boolean testMysql) {
		this.testMysql = testMysql;
	}

	public boolean isTestPostgres() {
		return testPostgres;
	}

	public void setTestPostgres(boolean testPostgres) {
		this.testPostgres = testPostgres;
	}
}
