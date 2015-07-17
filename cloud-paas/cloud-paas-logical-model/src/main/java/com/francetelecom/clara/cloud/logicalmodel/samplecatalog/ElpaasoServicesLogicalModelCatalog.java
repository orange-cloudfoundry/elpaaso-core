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

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.*;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample application for elpaaso custom service brokers and feature
 */
public class ElpaasoServicesLogicalModelCatalog extends BaseReferenceLogicalModelsCatalog {

	private static final String DBAAS_BROKER = "elpaaso-brokers.dbaas";
	String SMTP_BROKER = "elpaaso-brokers.smtp";
	String LOG_BROKER = "elpaaso-brokers.log";
	String PACKETBEAT = "elpaaso-supervision.packetbeat";
	String LOGSEARCH_CF_LOGS = "elpaaso-supervision.logsearch";	

	@Override
	public LogicalDeployment createLogicalModel(String logicalModelName, LogicalDeployment existingLDToUpdate) {

		if (existingLDToUpdate == null) {
			existingLDToUpdate = new LogicalDeployment();
		}
		addSmtpBroker(existingLDToUpdate);
		addPacketBeat(existingLDToUpdate);
		addLogBroker(existingLDToUpdate);
		addLogSearch(existingLDToUpdate);
		addDbaasBroker(existingLDToUpdate);


		return existingLDToUpdate;

	}

	protected void addSmtpBroker(LogicalDeployment existingLDToUpdate) {
		// processings
		CFJavaProcessing smtpBroker = createCFJavaProcessing(this, "smtp-broker", SMTP_BROKER, ArtefactType.jar);
		existingLDToUpdate.addExecutionNode(smtpBroker);

		// web access
		LogicalWebGUIService smtpBrokerUi = createLogicalWebGuiService("smtp-broker-ui", SMTP_BROKER, false, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(smtpBrokerUi);
		smtpBroker.addLogicalServiceUsage(smtpBrokerUi, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// config services
		{
			StringBuffer configSetContent = new StringBuffer();
			configSetContent.append("#broker\n");
			configSetContent.append("brokerUser=brokeruser" + "\n");
			configSetContent.append("brokerPassword=xxx" + "\n");
			configSetContent.append("#smtp backend" + "\n");
			configSetContent.append("dashboardUrl=http://splunk.elpaaso.org" + "\n");
			configSetContent.append("targetHost=elpaaso-mailxxxx.internal-qa.paas" + "\n");
			configSetContent.append("targetPort=25" + "\n");

			try {
				LogicalConfigService config = new LogicalConfigService("smtp-broker-config", existingLDToUpdate, configSetContent.toString());
				smtpBroker.addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			} catch (InvalidConfigServiceException e) {
				throw new TechnicalException(e);
			}

		}

	}

	protected void addPacketBeat(LogicalDeployment existingLDToUpdate) {
		// processings
		CFJavaProcessing packetBeat = createCFJavaProcessing(this, "packetbeat-kibana", PACKETBEAT, ArtefactType.jar);
		existingLDToUpdate.addExecutionNode(packetBeat);

		LogicalWebGUIService packetBeatUi = createLogicalWebGuiService("packetbeat-kibana-ui", PACKETBEAT, true, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(packetBeatUi);
		packetBeat.addLogicalServiceUsage(packetBeatUi, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		{
			StringBuffer configSetContent = new StringBuffer();
			configSetContent.append("#elasticsearch backend url" + "\n");
			configSetContent.append("elasticsearch_url=http://elpaaso-packetbeat.internal-qa.paas:9200" + "\n");

			try {
				LogicalConfigService config = new LogicalConfigService("kibana-config", existingLDToUpdate, configSetContent.toString());
				packetBeat.addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			} catch (InvalidConfigServiceException e) {
				throw new TechnicalException(e);
			}

		}
	}
	
	
	protected void addLogSearch(LogicalDeployment existingLDToUpdate) {
		// processings
		CFJavaProcessing logsearch = createCFJavaProcessing(this, "logsearch-kibana", LOGSEARCH_CF_LOGS, ArtefactType.jar);
		existingLDToUpdate.addExecutionNode(logsearch);

		LogicalWebGUIService logsearchUI = createLogicalWebGuiService("logsearch-kibana-ui", LOGSEARCH_CF_LOGS, true, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(logsearchUI);
		logsearch.addLogicalServiceUsage(logsearchUI, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		{
			StringBuffer configSetContent = new StringBuffer();
			
			configSetContent.append("#UAA Oauth2 parameters" + "\n");
			configSetContent.append("spring_oauth2_client_clientId=logsearch_for_cloudfoundry" + "\n");
			configSetContent.append("spring_oauth2_client_clientSecret=UAA-LOGSEARCH-CF-SECRET-xxxxxxxx" + "\n");
			configSetContent.append("logsearch_elasticsearchAdminUri=http://0.api.default.logsearch.bosh:9200" + "\n");
			configSetContent.append("zuul_routes_kibana_url=http://0.api.default.logsearch.bosh:5601" + "\n");
			
			try {
				LogicalConfigService config = new LogicalConfigService("logsearch-config", existingLDToUpdate, configSetContent.toString());
				logsearch.addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			} catch (InvalidConfigServiceException e) {
				throw new TechnicalException(e);
			}

		}
	}

	protected void addLogBroker(LogicalDeployment existingLDToUpdate) {
		// processings
		CFJavaProcessing logBroker = createCFJavaProcessing(this, "log-broker", LOG_BROKER, ArtefactType.jar);
		existingLDToUpdate.addExecutionNode(logBroker);

		// web access
		LogicalWebGUIService logBrokerUi = createLogicalWebGuiService("log-broker-ui", LOG_BROKER, false, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(logBrokerUi);
		logBroker.addLogicalServiceUsage(logBrokerUi, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// config services
		{
			StringBuffer configSetContent = new StringBuffer();
			configSetContent.append("#broker\n");
			configSetContent.append("log.syslogdrain.url=syslog://SPLUNK_HOST:SPLUNK_PORT" + "\n");
			configSetContent.append("log.server.url=https://splunk.elpaaso.org" + "\n");
			configSetContent.append("broker.log.user=user" + "\n");
			configSetContent.append("broker.log.password=xxxxxx" + "\n");
			try {
				LogicalConfigService config = new LogicalConfigService("log-broker-config", existingLDToUpdate, configSetContent.toString());
				logBroker.addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			} catch (InvalidConfigServiceException e) {
				throw new TechnicalException(e);
			}

		}

	}

	protected void addDbaasBroker(LogicalDeployment existingLDToUpdate) {
		// processings
		CFJavaProcessing dbaasBroker = createCFJavaProcessing(this, "dbaas-broker", DBAAS_BROKER, ArtefactType.jar);
		existingLDToUpdate.addExecutionNode(dbaasBroker);

		// web access
		LogicalWebGUIService logBrokerUi = createLogicalWebGuiService("dbaas-broker", DBAAS_BROKER, false, false, 1, 20, ArtefactType.jar);
		existingLDToUpdate.addLogicalService(logBrokerUi);
		dbaasBroker.addLogicalServiceUsage(logBrokerUi, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		//db
		LogicalMysqlService mysqlService = createLogicalMysqlService("db", "dbaas-broker-db");
		existingLDToUpdate.addLogicalService(mysqlService);
		dbaasBroker.addLogicalServiceUsage(mysqlService, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

		// config services
		{
			StringBuffer configSetContent = new StringBuffer();
			configSetContent.append("#broker\n");
			configSetContent.append("dbaas.api.url=http://DBAAS_HOST:DBAAS_PORT/cloud-orange-dbaas-ws-war/dbaasApi18" + "\n");
			configSetContent.append("dbaas.api.groupname=ElPaaso" + "\n");
			configSetContent.append("dbaas.api.username=DBAAS_USERNAME" + "\n");
			configSetContent.append("dbaas.api.password=DBAAS_PASSWORD" + "\n");
			configSetContent.append("dbaas.api.timeout=30000" + "\n");

			configSetContent.append("broker.dbaas.username=DBAAS_BROKER_USERNAME" + "\n");
			configSetContent.append("broker.dbaas.password=DBAAS_BROKER_PASSWORD" + "\n");
			try {
				LogicalConfigService config = new LogicalConfigService("dbaas-broker-config", existingLDToUpdate, configSetContent.toString());
				dbaasBroker.addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
			} catch (InvalidConfigServiceException e) {
				throw new TechnicalException(e);
			}

		}

	}

	@Override
	public boolean isInstantiable() {
		return true;
	}

	@Override
	public Map<String, String> getAppUrlsAndKeywords(URL baseUrl) {
		HashMap<String, String> urls = new HashMap<String, String>();
		return urls;
	}

	@Override
	public String getAppDescription() {
		return "elpaaso-brokers-app";
	}

	@Override
	public String getAppCode() {
		return "P2A-BROKERS";
	}

	@Override
	public String getAppLabel() {
		return "elpaaso-brokers-app";
	}

	@Override
	public String getAppReleaseDescription() {
		return "elpaaso-brokers release 2";
	}

	@Override
	public String getAppReleaseVersion() {
		return "2.0.0";
	}
}
