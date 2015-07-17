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

import com.francetelecom.clara.cloud.activation.plugin.dbaas.ActivationPluginDBaas20.IgnoredErrors;
import com.francetelecom.clara.cloud.activation.plugin.dbaas.DBaasConsumer;
import com.francetelecom.clara.cloud.activation.plugin.dbaas.DBaasInformations;
import com.francetelecom.clara.cloud.activation.plugin.dbaas.DBaasServiceStateEnum;
import com.francetelecom.clara.cloud.activation.plugin.dbaas.DbHelper;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionSqlDialectEnum;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.francetelecom.clara.cloud.techmodel.dbaas.DbAccessInfo;
import com.orange.clara.cloud.dbaas.wsdl.data.*;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.*;
import com.orange.clara.cloud.dbaas.wsdl.response.DescribeDatabaseResponse;
import com.orange.clara.cloud.dbaas.wsdl.service.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Required;

import java.lang.reflect.Proxy;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * DataBase as a Service consumer class
 */
public class DBaasConsumer20Impl implements DBaasConsumer {

	private static final Logger LOG = LoggerFactory.getLogger(DBaasConsumer20Impl.class);

	private DbaasApiRemote dbaasStub;
	private String dbaasGroupName;

	private Long receiveTimeout;

	/**
	 * Maven DAO to retrieve artifacts
	 */
	private MvnRepoDao mvnDao;

	static class PluginTaskStatus extends TaskStatus {

		private static final long serialVersionUID = -3605265516466711178L;

		private int tokenId = -1;

		private DeploymentStateEnum finalState = DeploymentStateEnum.UNKNOWN;

		/**
		 * @param id
		 */
		public PluginTaskStatus(long id) {
			super(id);
		}

		/**
		 * @param status
		 */
		public PluginTaskStatus(PluginTaskStatus status) {
			super(status);
			this.tokenId = status.tokenId;
		}

		/**
		 * @return
		 */
		public int getTokenId() {
			return this.tokenId;
		}

		/**
		 * @param tokenId
		 */
		public void setTokenId(int tokenId) {
			this.tokenId = tokenId;
		}

		/**
		 * @return
		 */
		public DeploymentStateEnum getFinalState() {
			return this.finalState;
		}

		/**
		 * @param finalState
		 */
		public void setFinalState(DeploymentStateEnum finalState) {
			this.finalState = finalState;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure.
	 * DBaasConsumer#createDatabase
	 * (com.francetelecom.clara.cloud.model.DBaasSubscription, java.lang.String)
	 */
	@Override
	public TaskStatus createDatabase(DBaasSubscriptionV2 dbaasSubscription, String envDescription) {

		LOG.debug("activating dbaas for dbaasSubscription " + dbaasSubscription);

		final PluginTaskStatus status = new PluginTaskStatus(System.currentTimeMillis());
		status.setTitle("Activatingdbaas for dbaasSubscription#" + dbaasSubscription.getId());
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		status.setFinalState(DeploymentStateEnum.CREATED);

		try {

			// CREATE DATABASE

			// Size to pass to DBaaS stub is in Go
			int size = dbaasSubscription.getStorageCapacityGb();

			// TODO : set ServiceClassWsEnum from tpmc or set SLO from tpmc?
			ServiceClassWsEnum serviceClass;

			// TODO : how to get engine default version?
			EngineWsEnum engine;
			if (dbaasSubscription.getSqlDialect().equals(DBaasSubscriptionSqlDialectEnum.POSTGRESQL_DEFAULT)) {
				engine = EngineWsEnum.POSTGRESQL;
				serviceClass = ServiceClassWsEnum.XXXS;
			} else if (dbaasSubscription.getSqlDialect().equals(DBaasSubscriptionSqlDialectEnum.MYSQL_DEFAULT)) {
				engine = EngineWsEnum.MYSQL;
				serviceClass = ServiceClassWsEnum.XXXS;
			} else {
				throw new TechnicalException(dbaasSubscription.getSqlDialect() + " SQL engine is NOT available.");
			}

			// Database userName
			String userName = dbaasSubscription.getUserName();

			// Database password
			String password = dbaasSubscription.getUserPassword();

			// for now => Always pass Standard
			SloWsEnum sloEnum = SloWsEnum.STANDARD;

			// TODO : SOX : from LM.SOX + per tenant projection customization
			// (e.g. SOX
			// only for production env )
			boolean isSox = false;

			// usage / By default : DEVTEST
			UsageWsEnum usage = UsageWsEnum.DEVTEST;

			// datacenter
			String datacenter = "Cube";

			// NetworkZoneWsEnum n/a TBC after june
			NetworkZoneWsEnum dbaasNetworkZone = NetworkZoneWsEnum.RSC;

			// database parameter set name (e.g. charset (UTF-8), buffer
			// size).
			String databaseParametersSetName = "databaseParametersSetName";

			// TODO : maintenance window: LM.maintenance_window + projection
			// default
			// rule (apply only to production sites) + per env customization
			String maintenanceWindow = "Sun:20:00-04";

			// TODO : BackupPlan: LM.DBService.backupplan + projection per app
			// customization
			BackupPlanWsEnum backupPlan = BackupPlanWsEnum.NONE;

			// TODO : backup window
			String backupWindow = "20:00-04";

			// TODO : isAutoUpgrade : per tenant customization rules (assign
			// autoupgrade per environment type) + per application
			// customization. OOS for june.
			boolean isAutoUpgrade = false;

			String description = envDescription + " " + dbaasSubscription.getDescription();

			LOG.debug("Creating database with {} groupName [{}] size [{}] serviceClass [{}] userName[{}] password omited, sloEnum [{}] "
					+ "isSox [{}] dbaasUsageEnum [{}] datacenter [{}] dbaasNetworkZone [{}] databaseParametersSetName [{}] "
					+ "maintenanceWindow [{}] backupPlan [{}] backupWindow [{}] isAutoUpgrade [{}] description [{}].", new Object[] { engine,
					this.dbaasGroupName, size, serviceClass, userName, sloEnum, isSox, usage, datacenter, dbaasNetworkZone, databaseParametersSetName,
					maintenanceWindow, backupPlan, backupWindow, isAutoUpgrade, description });

			List<DatabaseUserInfo> users = new ArrayList<DatabaseUserInfo>();
			DatabaseUserInfo user = new DatabaseUserInfo();
			user.setLogin(userName);
			user.setPassword(password);
			user.setDatabaseUserType(DatabaseUserTypeWsEnum.OWNER);
			users.add(user);

			CreateDatabaseResponseObject taskObject = this.dbaasStub.createDatabase(null, this.dbaasGroupName, size, serviceClass, engine, null, users,
					sloEnum, isSox, usage, datacenter, dbaasNetworkZone, "RSC", databaseParametersSetName, maintenanceWindow, backupPlan, backupWindow,
					isAutoUpgrade, description);

			status.setTokenId(taskObject.getJobId());
			status.setTitle("Activatingdbaas for dbaasSubscription#" + dbaasSubscription.getId() + "(Database " + taskObject.getDatabaseUUId() + ")");
			LOG.debug("Generated database UUId is {}, name is {}", taskObject.getDatabaseUUId(), taskObject.getDatabaseName());

			dbaasSubscription.setDatabaseUUId(taskObject.getDatabaseUUId());
		}

		catch (RuntimeException e) {
			String errMsg = "Exception during createDatabase - message : " + e.getMessage();
			if (ExceptionUtils.getRootCause(e) instanceof SocketTimeoutException) {
				errMsg += " actual timeout (ms) : " + getTimeout();
			}
			LOG.error(errMsg, e);
			throw new TechnicalException(errMsg, e);
		} catch (UnknownDatabaseSecurityGroupFault | ServiceUnavailableFault | UnknownDatabaseParametersSetNameFault | OperationUnsupportedFault
				| DatabaseAlreadyExistsFault | QuotaExceededFault | InvalidParameterFault | NotEnoughCapacityFault | InternalFailureFault
				| InvalidParameterCombinationFault | UnknownDatabaseGroupFault | WrongUserRoleFault | IllegalPasswordFault | UnknownTenantFault
				| InvalidUserFault | UserLockedFault | UnknownFault | ConcurrentJobsFault | OperationNotPermittedFault | UserNameForbiddenFault e) {
			String errMsg = "Exception during createDatabase - message : " + e.getMessage();
			LOG.error(errMsg, e);
			throw new TechnicalException(errMsg, e);
		}

		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure.
	 * DBaasConsumer#startDatabase
	 * (com.francetelecom.clara.cloud.model.DBaasSubscription)
	 */
	@Override
	public TaskStatus startDatabase(DBaasSubscriptionV2 dbaasSubscription) {

		LOG.debug("Start dbaas for dbaasSubscription " + dbaasSubscription);

		// TODO TaskStatus id should be set by persistence
		final PluginTaskStatus status = new PluginTaskStatus(System.currentTimeMillis());
		status.setTitle("Starting dbaas for dbaasSubscription " + dbaasSubscription.getName());
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		status.setFinalState(DeploymentStateEnum.STARTED);

		String databaseUUId = dbaasSubscription.getDatabaseUUId();

		try {
			// start database task;
			int taskId = this.dbaasStub.startDatabase(databaseUUId);
			status.setTokenId(taskId);
		} catch (DatabaseAlreadyStartedFault e) {
			// database already stopped, make it silent
			String errMsg = "Exception during start database - database : " + databaseUUId + " is already started - message : " + e.getMessage();
			LOG.warn(errMsg);
			status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
			return status;
		} catch (Exception e) {
			String errMsg = "Exception during startDatabase - database: " + databaseUUId + " - message : " + e.getMessage();
			if (ExceptionUtils.getRootCause(e) instanceof SocketTimeoutException) {
				errMsg += " actual timeout (ms) : " + getTimeout();
			}
			throw new TechnicalException(errMsg);
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure.
	 * DBaasConsumer#stopDatabase
	 * (com.francetelecom.clara.cloud.model.DBaasSubscription)
	 */
	@Override
	public TaskStatus stopDatabase(DBaasSubscriptionV2 dbaasSubscription) {

		LOG.debug("Stop dbaas for dbaasSubscription " + dbaasSubscription);

		// TODO TaskStatus id should be set by persistence
		final PluginTaskStatus status = new PluginTaskStatus(System.currentTimeMillis());
		status.setTitle("Stopping dbaas for dbaasSubscription " + dbaasSubscription.getName());
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		status.setFinalState(DeploymentStateEnum.STOPPED);

		String databaseUUId = dbaasSubscription.getDatabaseUUId();
		if (databaseUUId == null) {
			status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
			return status;
		}
		try {
			// start database task;
			int taskId = this.dbaasStub.stopDatabase(databaseUUId);
			status.setTokenId(taskId);
		} catch (DatabaseAlreadyStoppedFault e) {
			// database already stopped, make it silent
			String errMsg = "Exception during stopDatabase - database : " + databaseUUId + " is already stopped - message : " + e.getMessage();
			LOG.warn(errMsg);
			status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
			return status;
		} catch (Exception e) {
			String errMsg = "Exception during stopDatabase - database : " + databaseUUId + " - message : " + e.getMessage();
			if (ExceptionUtils.getRootCause(e) instanceof SocketTimeoutException) {
				errMsg += " actual timeout (ms) : " + getTimeout();
			}
			throw new TechnicalException(errMsg);
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure.
	 * DBaasConsumer#deleteDatabase
	 * (com.francetelecom.clara.cloud.model.DBaasSubscription)
	 */
	@Override
	public TaskStatus deleteDatabase(DBaasSubscriptionV2 dbaasSubscription) {

		LOG.debug("delete dbaas for dbaasSubscription " + dbaasSubscription);

		// TODO TaskStatus id should be set by persistence
		final PluginTaskStatus status = new PluginTaskStatus(System.currentTimeMillis());
		status.setTitle("Deleting dbaas for dbaasSubscription " + dbaasSubscription.getName());
		status.setStartTime(System.currentTimeMillis());
		status.setTaskStatus(TaskStatusEnum.STARTED);
		status.setFinalState(DeploymentStateEnum.REMOVED);

		String databaseUUId = dbaasSubscription.getDatabaseUUId();
		if (databaseUUId == null) {
			status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
			return status;
		}

		try {
			// Stop database task;
			int taskId = this.dbaasStub.deleteDatabase(databaseUUId);
			status.setTokenId(taskId);
		} catch (UnknownDatabaseFault | DatabaseAlreadyDeletedFault ignoreDb) {
			LOG.warn("Ignoring exception during deleteDatabase, DB doesn't exist anymore - database: {} - message : {}", databaseUUId, ignoreDb.getMessage());
			status.setTaskStatus(TaskStatusEnum.FINISHED_OK);
			return status;
		} catch (ConcurrentJobsFault concurrentJobFault) {
			throw new TechnicalException("Exception during deleteDatabase - database: " + databaseUUId + " - message : " + IgnoredErrors.CONCURRENT_DELETION,
					concurrentJobFault);
		} catch (Exception e) {
			String errMsg = "Exception during deleteDatabase - database : " + databaseUUId + " - message : " + e.getMessage();
			if (ExceptionUtils.getRootCause(e) instanceof SocketTimeoutException) {
				errMsg += " actual timeout (ms) : " + getTimeout();
			}
			throw new TechnicalException(errMsg, e);
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure.
	 * DBaasConsumer# launchPopulationScript
	 * (com.francetelecom.clara.cloud.model.DBaasSubscription)
	 */
	@Override
	public void launchPopulationScript(DBaasSubscriptionV2 dbaasSubscription) {
		DbHelper.launchPopulationScript(dbaasSubscription, this.mvnDao);
	}

	@Override
	public String getDBaaSVersion() {
		return "DBaaS 1.0";
	}

	/**
	 * IOC
	 * 
	 * @param dbaasStub
	 */
	public void setDbaasStub(DbaasApiRemote dbaasStub) {
		this.dbaasStub = dbaasStub;
		LOG.info("DBAAS connection to " + dbaasStub.toString());
		this.updateTimeout();
	}

	/**
	 * 
	 */
	void updateTimeout() {
		if (this.dbaasStub != null && Proxy.isProxyClass(dbaasStub.getClass())) {
			// Set timeout for DBaas API
			Client client = ClientProxy.getClient(this.dbaasStub);
			if (client != null) {
				HTTPConduit conduit = (HTTPConduit) client.getConduit();
				HTTPClientPolicy policy = new HTTPClientPolicy();
				if (this.getTimeout() != null) {
					policy.setConnectionTimeout(this.getTimeout());
					policy.setReceiveTimeout(this.getTimeout());
				} else {
					policy.unsetConnectionTimeout();
					policy.unsetReceiveTimeout();
				}
				conduit.setClient(policy);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.francetelecom.clara.cloud.commons.tasks.PollTaskStateInterface#
	 * giveCurrentTaskStatus
	 * (com.francetelecom.clara.cloud.commons.tasks.TaskStatus)
	 */
	@Override
	public TaskStatus giveCurrentTaskStatus(TaskStatus taskStatus) {
		PluginTaskStatus pluginTaskStatus = new PluginTaskStatus((PluginTaskStatus) taskStatus);
		try {
			JobStateWsEnum jobstate = this.dbaasStub.getJobState(pluginTaskStatus.getTokenId());
			updatePluginTaskStatus(pluginTaskStatus, jobstate);
		} catch (Exception e) {
			Throwable rootCause = ExceptionUtils.getRootCause(e);
			String errorMessage = getDBaaSVersion() + " error on getJob : " + ExceptionUtils.getMessage(rootCause);
			if (rootCause instanceof SocketTimeoutException) {
				errorMessage += " actual timeout (ms) : " + getTimeout();
			}
			throw new TechnicalException(errorMessage, e);
		}
		return pluginTaskStatus;
	}

	/**
	 * Last DBaaS job status is set to the pluginTaskStatus
	 * 
	 * @param pluginTaskStatus
	 * @param job
	 */
	private void updatePluginTaskStatus(PluginTaskStatus pluginTaskStatus, JobStateWsEnum jobstate) throws Exception {
		switch (jobstate) {
		case FINISHED:
			pluginTaskStatus.setTaskStatus(TaskStatusEnum.FINISHED_OK);
			break;
		case CANCELLED:
		case ERROR:
			pluginTaskStatus.setTaskStatus(TaskStatusEnum.FINISHED_FAILED);
			String errorMessage = "";
			JobInfo job = this.dbaasStub.getJob(pluginTaskStatus.getTokenId());
			if (job.getMessages() != null) {
				for (JobMessage mess : job.getMessages()) {
					errorMessage += mess.getMessage() + "\n";
				}
			}
			pluginTaskStatus.setErrorMessage(errorMessage);
			break;
		case PROCESSING:
			pluginTaskStatus.setTaskStatus(TaskStatusEnum.STARTED);
			break;
		case WAITING:
		case SCHEDULED:
		default:
			pluginTaskStatus.setTaskStatus(TaskStatusEnum.TRANSIENT);
			break;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure.
	 * DBaasConsumer# fetchDatabaseDescription
	 * (com.francetelecom.clara.cloud.model.DBaasSubscription)
	 */
	@Override
	public DbAccessInfo fetchDatabaseDescription(String databaseUUId) {

		if (databaseUUId == null)
			throw new TechnicalException("Invalid database uuid<" + databaseUUId + ">. Cannot fetch database description.");

		try {

			DescribeDatabaseResponse describeDatabase = this.dbaasStub.describeDatabase(databaseUUId);

			String host = describeDatabase.getEndPointFQDN();
			int port = Integer.parseInt(describeDatabase.getEndPointTCPPort());
			String dbname = describeDatabase.getDatabaseName();
			Date creationDate = (describeDatabase.getCreationDate() != null) ? describeDatabase.getCreationDate().toGregorianCalendar().getTime() : null;

			DbAccessInfo description = new DbAccessInfo(host, port, dbname);

			return description;
		} catch (Exception e) {
			String errorMessage = "Exception during fetchDatabaseDescription - database: " + databaseUUId + " / Message = " + e.getMessage();
			if (ExceptionUtils.getRootCause(e) instanceof SocketTimeoutException) {
				errorMessage += " actual timeout (ms) : " + getTimeout();
			}
			LOG.error("Exception in fetchDatabaseDescription - " + e.getClass() + " / Message: " + e.getMessage());
			throw new TechnicalException(errorMessage, e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure.
	 * DBaasConsumer#listAll()
	 */
	@Override
	public Iterable<DBaasInformations> listAll() {
		List<DBaasInformations> infos = new ArrayList<DBaasInformations>();
		try {
			for (DatabaseInfo databaseInfo : this.dbaasStub.listDatabases(null, null, null)) {
				Date creationDate = (databaseInfo.getCreationDate() != null) ? databaseInfo.getCreationDate().toGregorianCalendar().getTime() : null;
				infos.add(new DBaasInformations(databaseInfo.getDatabaseUUId(), databaseInfo.getOwner(), creationDate, databaseInfo.getDatabaseState().value()));
			}
		} catch (Exception e) {
			String errMsg = "Exception during listAll: " + e.getMessage();
			if (ExceptionUtils.getRootCause(e) instanceof SocketTimeoutException) {
				errMsg += " actual timeout (ms) : " + getTimeout();
			}
			LOG.error("Exception in listAll - " + e.getClass() + " / Message: " + e.getMessage());
			throw new TechnicalException(errMsg, e);
		}
		return infos;
	}

	/**
	 * Return status of database
	 * 
	 * @param dbName
	 *            The db name
	 * @return DB status or null if database was not found
	 */
	@Override
	public DBaasServiceStateEnum getStatus(String dbName) {
		if (dbName == null || dbName.length() == 0) {
			return null;
		}

		try {
			DescribeDatabaseResponse describeDatabase = this.dbaasStub.describeDatabase(dbName);
			return this.mapState(describeDatabase.getDatabaseState());
		} catch (Exception e) {
			String errMsg = "Exception during fetchDatabaseDescription - databaseName : " + dbName + " - message : " + e.getMessage();
			if (ExceptionUtils.getRootCause(e) instanceof SocketTimeoutException) {
				errMsg += " actual timeout (ms) : " + getTimeout();
			}
			throw new TechnicalException(errMsg, e);
		}
	}

	/**
	 * @param state
	 * @return
	 */
	private DBaasServiceStateEnum mapState(ServiceStateWsEnum state) {
		switch (state) {
		case ACTIVE:
			return DBaasServiceStateEnum.ACTIVE;
		case CREATING:
			return DBaasServiceStateEnum.CREATING;
		case DELETED:
			return DBaasServiceStateEnum.DELETED;
		case DELETING:
			return DBaasServiceStateEnum.DELETING;
		case INCIDENT:
			return DBaasServiceStateEnum.INCIDENT;
		case MAINTENANCE:
			return DBaasServiceStateEnum.MAINTENANCE;
		case RESTORING:
			return DBaasServiceStateEnum.RESTORING;
		case STARTING:
			return DBaasServiceStateEnum.STARTING;
		case STOPPED:
			return DBaasServiceStateEnum.STOPPED;
		case STOPPING:
			return DBaasServiceStateEnum.STOPPING;
		default:
			return DBaasServiceStateEnum.INCIDENT;
		}
	}

	/**
	 * @return
	 */
	public Long getTimeout() {
		return this.receiveTimeout;
	}

	/**
	 * @param timeout
	 *            Timeout in ms
	 */
	public void setTimeout(Long timeout) {
		this.receiveTimeout = timeout;
		this.updateTimeout();
	}

	/**
	 * @param mvnDao
	 */
	@Required
	public void setMvnDao(MvnRepoDao mvnDao) {
		this.mvnDao = mvnDao;
	}

	/**
	 * @return the dbaasGroupName
	 */
	public String getDbaasGroupName() {
		return this.dbaasGroupName;
	}

	/**
	 * @param dbaasGroupName
	 *            the dbaasGroupName to set
	 */
	@Required
	public void setDbaasGroupName(String dbaasGroupName) {
		this.dbaasGroupName = dbaasGroupName;
	}
}
