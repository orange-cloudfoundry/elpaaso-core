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
/**
 * 
 */
package com.francetelecom.clara.cloud.techmodel.dbaas;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.DeploymentStateEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.XaasSubscription;
import com.francetelecom.clara.cloud.techmodel.dbaas.StorageCapacity.SizeUnit;
import org.springframework.util.Assert;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.Set;

/**
 * Represents a DBaaS subscription. Depending on the deployment state, a
 * DBaasSubscription instance may represent a template for making a DBaaS
 * request (when deploymentState=transient and the instance), or represent the
 * result of a DBaaS request (when deploymentState=created)
 *
 * Note the renaming to V2 to fix:
 * <code>
 * Caused by: org.hibernate.AnnotationException: Use of the same entity name
 * twice: DBaasSubscription at
 * org.hibernate.cfg.annotations.EntityBinder.bindEntity(EntityBinder.java:403)
 * at org.hibernate.cfg.AnnotationBinder.bindClass(AnnotationBinder.java:584) at
 * org.hibernate.cfg.Configuration$MetadataSourceQueue.
 * processAnnotatedClassesQueue(Configuration.java:3443) at
 * org.hibernate.cfg.Configuration$MetadataSourceQueue
 * .processMetadata(Configuration.java:3397) at
 * org.hibernate.cfg.Configuration.secondPassCompile(Configuration.java:1341) at
 * org.hibernate.cfg.Configuration.buildSessionFactory(Configuration.java:1737)
 * at
 * org.hibernate.ejb.EntityManagerFactoryImpl.&lt;init&gt;(EntityManagerFactoryImpl.
 * java:94) at org.hibernate.ejb.Ejb3Configuration.buildEntityManagerFactory(
 * Ejb3Configuration.java:905) ... 62 more Caused by:
 * org.hibernate.DuplicateMappingException: duplicate import: DBaasSubscription
 * refers to both
 * com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscription and
 * com.francetelecom.clara.cloud.model.DBaasSubscription (try using
 * auto-import="false") at
 * org.hibernate.cfg.Configuration$MappingsImpl.addImport
 * (Configuration.java:2590) at
 * org.hibernate.cfg.annotations.EntityBinder.bindEntity(EntityBinder.java:396)
 * ... 69 more
 * </code>
 * @author apog7416
 */
@XmlRootElement
@Entity
@Table(name = "DBAAS_SUBSCRIPTION_V2")
public class DBaasSubscriptionV2 extends XaasSubscription implements DbConnectionDetails {

	private static final long serialVersionUID = -5723498659703029763L;

	/**
	 * The disk size in MB to allocate the DB instance (this includes all
	 * storage to be used, data, index, logs, ...)
	 */
	@Embedded
	@NotNull
	private StorageCapacity storageCapacityMb;

	@Embedded
	@NotNull
	private DbOwner dbOwner;

	@Embedded
	// ugly but only way to make dbAccessInfo optional
	// see https://java.net/jira/browse/JPA_SPEC-42
	@AttributeOverrides({ @AttributeOverride(name = "hostname", column = @Column(nullable = true)), @AttributeOverride(name = "port", column = @Column(nullable = true)),
			@AttributeOverride(name = "dbName", column = @Column(nullable = true)) })
	private DbAccessInfo dbAccessInfo;

	/**
	 * The optional user-provided database schema and data population script.
	 * May be null.
	 * 
	 *
	 *      TODO find a better place for this field which is common to a DB
	 *      instance obtained from DBaaS or hosted as a product.
	 */
	@Embedded
	@AttributeOverrides({ @AttributeOverride(name = "groupId", column = @Column(name = "SQLSCRIPT_GROUPID")),
			@AttributeOverride(name = "artifactId", column = @Column(name = "SQLSCRIPT_ARTIFACTID")),
			@AttributeOverride(name = "version", column = @Column(name = "SQLSCRIPT_VERSION")),
			@AttributeOverride(name = "extension", column = @Column(name = "SQLSCRIPT_EXTENSION")),
			@AttributeOverride(name = "accessUrl", column = @Column(name = "SQLSCRIPT_URL")),
			@AttributeOverride(name = "classifier", column = @Column(name = "SQLSCRIPT_CLASSIFIER")) })
	private MavenReference initialPopulationScript;

	/**
	 * The RDBMS SQL dialect and variant that would be requested to DBaaS
	 * providers (such as DDSI or amazon RDS). This is typically mapped by the
	 * dbaasconsumer into provider-supported version in their catalog.
	 * 
	 */
	private DBaasSubscriptionSqlDialectEnum sqlDialect = DBaasSubscriptionSqlDialectEnum.POSTGRESQL_DEFAULT;

	/**
	 * The database UUId.
	 */
	private String databaseUUId;

	/**
	 * You can specify a DBaas version here. Plugin will switch to the correct
	 * DBaas consumer with this value
	 */
	private String dbaasVersion;

	/**
	 * jpa required private constructor
	 */
	protected DBaasSubscriptionV2() {
	}

	public DBaasSubscriptionV2(TechnicalDeployment td) {
		super(td);
		changeDbOwner(new DbOwner("scott", "TigerW11d"));
		changeStorageCapacity(100);
	}

	public DBaasSubscriptionV2(TechnicalDeployment td, DbOwner dbOwner) {
		super(td);
	}

	@Override
	public Set<DependantModelItem> listDepedencies() {
		return Collections.emptySet();
	}

	public void changeStorageCapacity(int diskSizeMb) {
		this.storageCapacityMb = new StorageCapacity(diskSizeMb, SizeUnit.MB);
	}

	public String getDatabaseUUId() {
		return databaseUUId;
	}

	/**
	 * Assigns the database UUId
	 * 
	 * @param databaseUUId
	 *            The name that must match the regexp
	 */
	public void setDatabaseUUId(String databaseUUId) {
		this.databaseUUId = databaseUUId;
	}

	public MavenReference getInitialPopulationScript() {
		return initialPopulationScript;
	}

	public void setInitialPopulationScript(MavenReference initialPopulationScript) {
		this.initialPopulationScript = initialPopulationScript;
	}

	public DBaasSubscriptionSqlDialectEnum getSqlDialect() {
		return sqlDialect;
	}

	public void setSqlDialect(DBaasSubscriptionSqlDialectEnum sqlVersion) {
		this.sqlDialect = sqlVersion;
	}

	/***
	 * Maps to the name of the LogicalRelationalService
	 * 
	 */
	@Override
	public String getLogicalModelId() {
		return super.getLogicalModelId();
	}

	public String getDbaasVersion() {
		return dbaasVersion;
	}

	public void setDbaasVersion(String dbaasVersion) {
		this.dbaasVersion = dbaasVersion;
	}

	protected void setDbAccessInfo(DbAccessInfo dbAccessInfo) {
		Assert.notNull(dbAccessInfo, "fail to activate dbaas subscription. no connection details have been provided.");
		this.dbAccessInfo = dbAccessInfo;
	}

	public void activate(DbAccessInfo dbAccessInfo) {
		// FIXME should be setDeploymentState(DeploymentStateEnum.CREATING);
		setDeploymentState(DeploymentStateEnum.CREATED);
		setDbAccessInfo(dbAccessInfo);
	}

	@Override
	public String getHostname() {
		if (isActivated()) {
			return dbAccessInfo.getHostname();
		} else {
			throw new TechnicalException("Cannot get database hostname. dbaas subscription has not been activated yet");
		}
	}

	@Override
	public int getPort() {
		if (isActivated()) {
			return dbAccessInfo.getPort();
		} else {
			throw new TechnicalException("Cannot get database port. dbaas subscription has not been activated yet");
		}
	}

	@Override
	public String getUserName() {
		return dbOwner.getName();
	}

	@Override
	public String getUserPassword() {
		return dbOwner.getPassword();
	}

	@Override
	public String getDbname() {
		if (isActivated()) {
			return dbAccessInfo.getDbName();
		} else {
			throw new TechnicalException("Cannot get database name. dbaas subscription has not been activated yet");
		}
	}

	public void changeDbOwner(DbOwner dbOwner) {
		if (isActivated())
			throw new TechnicalException("Cannot change database owner. dbaas subscription has already been activated");
		this.setDbOwner(dbOwner);
	}

	private void setDbOwner(DbOwner dbOwner) {
		Assert.notNull(dbOwner, "fail to set database owner. dbOwner value <" + dbOwner + "> is not valid.");
		this.dbOwner = dbOwner;
	}

	public int getStorageCapacityGb() {
		return storageCapacityMb.ceil(SizeUnit.GB);
	}

}
