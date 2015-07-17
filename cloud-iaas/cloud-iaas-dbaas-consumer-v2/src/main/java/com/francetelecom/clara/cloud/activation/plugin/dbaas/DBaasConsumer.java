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

import com.francetelecom.clara.cloud.commons.tasks.PollTaskStateInterface;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.francetelecom.clara.cloud.techmodel.dbaas.DbAccessInfo;

public interface DBaasConsumer extends PollTaskStateInterface<TaskStatus>{

	// FIXME : change input/output parameters -> use DbaasSubscription and
	// DbaasSubscriptionInstance

	/**
	 * Deploy the DBaas Consumption. As a side effect the {@link com.francetelecom.clara.cloud.model.connectors.DBaasConnectorService} will be updated with
     * database credentials.
	 * 
	 * @param dbaasSubscription A subscription with a non empty list of DBaasConnectorService. This is where JDBC urls
     *                          of the created database will be injected into.
	 */
	TaskStatus createDatabase(DBaasSubscriptionV2 dbaasSubscription, String envDescription);

	/**
	 * start the DBaas instance,
	 * 
	 * @param dbaasSubscription subscription dbaas
	 */
	TaskStatus startDatabase(DBaasSubscriptionV2 dbaasSubscription);

	/**
	 * stop the DBaas instance,
	 * 
	 * @param dbaasSubscription subscription dbaas
	 */
	TaskStatus stopDatabase(DBaasSubscriptionV2 dbaasSubscription);

	/**
	 * delete the DBaas instance,
	 * 
	 * @param dbaasSubscription subscription dbaas
	 */
	TaskStatus deleteDatabase(DBaasSubscriptionV2 dbaasSubscription);


	/**
	 * Fetch database description 
	 * Call this method after database creation task is completed
	 * 
	 * @param databaseUUId subscription dbaas
	 * @return TODO
	 */
	DbAccessInfo fetchDatabaseDescription(String databaseUUId);

	/**
	 * List all databases available for connected user
	 * @return List of databases informations
	 */
	Iterable<DBaasInformations> listAll();

	/**
	 * Return status of database
	 * @param dbUUId The database ID
	 * @return DB status or null if database was not found
	 */
	public DBaasServiceStateEnum getStatus(String dbUUId);

	/**
	 * Launch SQL scripts
	 * @param dbaasSubscription Information about the DBaas subscription
	 */
	void launchPopulationScript(DBaasSubscriptionV2 dbaasSubscription);


    /**
     * return the current version of DBaaS used
     * @return
     */
    String getDBaaSVersion();
}
