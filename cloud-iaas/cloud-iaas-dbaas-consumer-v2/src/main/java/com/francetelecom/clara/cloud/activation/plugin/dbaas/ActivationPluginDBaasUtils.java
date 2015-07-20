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

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.transaction.annotation.Transactional;

import com.francetelecom.clara.cloud.application.ManageModelItem;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.XaasSubscriptionNotFound;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.techmodel.dbaas.DbAccessInfo;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.francetelecom.clara.cloud.commons.NotFoundException;

/**
 * ActivationPluginDBaasUtils
 * 
 * Last update : $LastChangedDate$ Last author : $Author$
 * 
 * @version : $Revision$
 */
public class ActivationPluginDBaasUtils {
	/**
	 * logger
	 */
	private static Logger logger = LoggerFactory.getLogger(ActivationPluginDBaasUtils.class);

	private Map<String, DBaasConsumer> dBaasConsumers;

	private ManageModelItem manageModelItem;

	@Transactional
	public TaskStatus createDatabase(int dbaasSubscriptionId, String envDescription) {
		DBaasSubscriptionV2 subscription = getDbaasSubscription(dbaasSubscriptionId);
		DBaasConsumer dBaasConsumer = getDBaasConsumer(subscription);
		logger.debug("createDatabase for dbaasSubscription#{} named {}", subscription.getId(), subscription.getDatabaseUUId());
		TaskStatus startStatus = dBaasConsumer.createDatabase(subscription, envDescription);
		logger.debug("fetchDatabaseDescription#{} {}", dbaasSubscriptionId, subscription.getDatabaseUUId());
		DbAccessInfo dbAccessInfo = dBaasConsumer.fetchDatabaseDescription(subscription.getDatabaseUUId());
		subscription.activate(dbAccessInfo);

		return startStatus;
	}

	private DBaasSubscriptionV2 getDbaasSubscription(int dbaasSubscriptionId) {
		try {
			return (DBaasSubscriptionV2) manageModelItem.findModelItem(dbaasSubscriptionId, DBaasSubscriptionV2.class);
		} catch (NotFoundException e) {
			throw new XaasSubscriptionNotFound(dbaasSubscriptionId, e);
		}
	}

	@Transactional(readOnly = true)
	public void firststart(int dbaasSubscriptionId) {
		// Create database RA-XML
		DBaasSubscriptionV2 dbaas = getDbaasSubscription(dbaasSubscriptionId);
		DBaasConsumer dBaasConsumer = getDBaasConsumer(dbaas);
		dBaasConsumer.launchPopulationScript(dbaas);
	}

	/**
	 * IOC
	 * 
	 * @param manageModelItem
	 *            model item
	 */
	public void setManageModelItem(ManageModelItem manageModelItem) {
		this.manageModelItem = manageModelItem;
	}

	/**
	 * IOC
	 * 
	 * @param dBaasConsumers
	 *            map of dbaas consumers
	 */
	@Required
	public void setdBaasConsumers(Map<String, DBaasConsumer> dBaasConsumers) {
		this.dBaasConsumers = dBaasConsumers;
	}

	/**
	 * Get a dbaas consumer of a given version : - if the version is null ro
	 * empty, a {@link TechnicalException} is thrown.
	 * 
	 * @param dbaasSubscription
	 * @return
	 */
	protected DBaasConsumer getDBaasConsumer(DBaasSubscriptionV2 dbaasSubscription) {
		if (dBaasConsumers == null) {
			throw new TechnicalException("No DbaasConsumers configured.");
		}
		if (dbaasSubscription == null) {
			throw new TechnicalException("DBaasSubscriptionV2 is null.");
		}
		return getDBaasConsumer(dbaasSubscription.getDbaasVersion());
	}

	/**
	 * Get a dbaas consumer of a given version : - if the version is null ro
	 * empty, a {@link TechnicalException} is thrown.
	 * 
	 * @param dbaasVersion
	 * @return
	 */
	public DBaasConsumer getDBaasConsumer(String dbaasVersion) {
		if (dbaasVersion == null || "".equals(dbaasVersion)) {
			throw new TechnicalException("Unable to get a DbaasConsumer for null or empty DBaas version :[" + dbaasVersion + "]");
		}
		logger.debug("Looking for dbaasConsumer for [{}]", dbaasVersion);
		logger.debug("  current map : {}", dBaasConsumers);
		if (!dBaasConsumers.containsKey(dbaasVersion)) {
			throw new TechnicalException("Unable to get a DbaasConsumer for DBaas version :[" + dbaasVersion + "]");
		}
		// If version is specified then use it
		return dBaasConsumers.get(dbaasVersion);
	}

	TaskStatus start(DBaasSubscriptionV2 dbaas) {
		return getDBaasConsumer(dbaas).startDatabase(dbaas);
	}

	TaskStatus stop(DBaasSubscriptionV2 dbaas) {
		return getDBaasConsumer(dbaas).stopDatabase(dbaas);
	}

	TaskStatus delete(DBaasSubscriptionV2 dbaas) {
		return getDBaasConsumer(dbaas).deleteDatabase(dbaas);
	}

}
