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
package com.francetelecom.clara.cloud.logicalmodel;

import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;
import com.francetelecom.clara.cloud.commons.MavenReference;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Relational DataBase Service.
 *
 * This represents a RDMS service regardless of whether this is provided as a product (i.e. instanciated within
 * the vapp) or as a service (i.e. a runtime service provides a DB instance which is part of shared infrastructure).
 *
 * @author APOG7416
 * */
@XmlRootElement
@Entity
@Table(name="RELATIONAL_SERVICE")
@GuiClassMapping(serviceCatalogName = "Relational DB", serviceCatalogNameKey = "rdb", status = GuiClassMapping.StatusType.SUPPORTED, isExternal = false)
public class LogicalRelationalService extends LogicalService {

    /**
     * name of the CF service that will be bound to the processing. Will be used by the code.
     *
     * TODO: refine the regexp on the jndi name beyond not null, not empty, see {@link LogicalOnlineStorageService#serviceName}
     *
     * FIXME: add control for preventing to collision of JNDI name on the same ExecutionNode
     */
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = true)
    @Size(min = 1)
    @NotNull(message = "The datasource needs to be looked up by the application through a JNDI name")
	String serviceName;

    /**
     * The SQL dialect and version that is required by the application.
     */
    @XmlAttribute
    @NotNull
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = true)
    LogicalRelationalServiceSqlDialectEnum sqlVersion = LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT;

    /**
     * The preferred maintenance windows for database maintenance operations within a normal week.
     *
     * TODO: precise the minimal duration supported by the relational service
     */
    @Transient //FIXME: add persistence once we support it
    //@Embedded //not enough to make it persistent, at least @Transient preverses invocation of the default initializer
    @GuiMapping(status = GuiMapping.StatusType.SKIPPED, functional = false)
    LogicalRelationalServiceMaintenanceWindow maintenanceWindow = LogicalRelationalServiceMaintenanceWindow.DEFAULT_WINDOW;

    /**
     * This optionally points to a .SQL script that is executed by the PaaS when the RDBMS service is first instanciated.
     * This provides the ability to perform initial database administration commands such as schema creation and initial
     * data population. This script would be run with administrative privileges on the RDBMS.
     *
     * Assumptions: this script is encoded in UTF-8 format, with Unix-style line feeds, matches the SQL dialect requested,
     * is self contained, syntaxically correct, and terminates in a reasonable duration.
     */
    @Embedded
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    @Valid
    MavenReference initialPopulationScript;

	/**
	 * Disk size in MB. Includes all data, and any indexes required additional disk space (such as redo logs) to run the database instance.
	 */
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    @Min(value = 1)
	int capacityMo=1000;

    /**
     * Number db replicas (read only slaves or read/write masters)
     *
     * TODO: remove this and replace with RTO and RPO, and have some of this determined at projection time by the
     * usage made of the DB (read or write accesses by multiple execnodes that require HA)
     */
    @GuiMapping(status = GuiMapping.StatusType.SKIPPED, functional = false)
    int relationalReplicaNumber=0;

    //TODO: rework this with refined SLOs
    @GuiMapping(status = GuiMapping.StatusType.SKIPPED, functional = false)
    int maxConnection=50;


    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String jndiName) {
        this.serviceName = jndiName;
    }

    public LogicalRelationalServiceSqlDialectEnum getSqlVersion() {
        return sqlVersion;
    }

    public void setSqlVersion(LogicalRelationalServiceSqlDialectEnum sqlVersion) {
        this.sqlVersion = sqlVersion;
    }

    public LogicalRelationalServiceMaintenanceWindow getMaintenanceWindow() {
        return maintenanceWindow;
    }

   /* public void setMaintenanceWindow(LogicalRelationalServiceMaintenanceWindow maintenanceWindow) {
        this.maintenanceWindow = maintenanceWindow;
    }
*/
    public MavenReference getInitialPopulationScript() {
        return initialPopulationScript;
    }

    public void setInitialPopulationScript(MavenReference initialPopulationScript) {
        this.initialPopulationScript = initialPopulationScript;
    }


	/**
	 * default constructor
	 */
    public LogicalRelationalService(){
		
	}
	
	
	/**
	 * Constructor
	 * @param label
	 * @param logicalDeployment
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
	 */
	public LogicalRelationalService(String label,LogicalDeployment logicalDeployment){
		super(label,logicalDeployment);
	}


	public int getCapacityMo() {
		return capacityMo;
	}

	public void setCapacityMo(int capacityMo) {
		this.capacityMo = capacityMo;
	}

	public int getRelationalReplicaNumber() {
		return relationalReplicaNumber;
	}

	public void setRelationalReplicaNumber(int relationalReplicaNumber) {
		this.relationalReplicaNumber = relationalReplicaNumber;
	}

	public int getMaxConnection() {
		return maxConnection;
	}

	public void setMaxConnection(int maxConnection) {
		this.maxConnection = maxConnection;
	}
	
}
