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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Log Service.
 *
 * Configures the preferences for handling application logs that are made available for reading by the PaaS.
 *
 * <ul>
 * <li>retention duration
 * <li>TBC application-specific aggregation levels (i.e. configuring distinct groups of logs: e.g. batches, front-end, admin
 * <br>TODO: detail the LogicalLogService sharing among ProcessingNodes (through LogicalNodeServiceAssociation),
 * or aggregation of logs among LogicalLogService instances
 * </ul>
 *
 * TODO: refine the programming interface with logs: what is the contract with the application to produce logs ? <ul>
 * <li>Java API: log4j, java.util.logging, other ?. In this case, the PaaS is responsible for providing the logging
 * implementation that captures those logs in a structured format, ways to set verbosity (as part of application
 * deployment and then dynamically at runtime).
 * <li>file system: a directory or list of files produced by the application which need to be indexed along with patterns to
 * parse those files.
 * </ul>
 *
 * As a comparison, google app engine uses the 1st option (java.util.logging), see http://code.google.com/appengine/docs/java/runtime.html#Logging
 * and with a limited logging.properties support (i.e. no java.util.logging.FileHandler)
 * http://code.google.com/p/ga-api-java-samples/source/browse/trunk/src/v1/appengine-sample/war/WEB-INF/logging.properties?r=6 and no standard
 * way to perform dynamic updates than the java.util.logging API that needs to be dynamically invoked by the application
 * (e.g. upon reception of admin HTTP requests)
 * 
 * @author POYT7496
 * 
 */

@XmlRootElement
@Entity
@Table(name = "LOG_SERVICE")
@GuiClassMapping(serviceCatalogName = "log service", serviceCatalogNameKey = "log", status = GuiClassMapping.StatusType.PREVIEW, isExternal = false)
public class LogicalLogService extends LogicalService {

	private static final long serialVersionUID = 1L;

	// peut etre le nom de la Destination JMS
	@NotNull
	private String logName;

    /**
     * Duration in seconds of logs before being archived.
     */
	private int logRetention;

    /**
     * FIXME: move this into the subscription and be controlled by the projection. The application does not need to
     * control how logs are stored.
     * @deprecated
     */
	private LogicalLogPersistentTypeEnum persistentType;

	/**
	 * private constructor for mapping
	 */
	@SuppressWarnings("unused")
	public LogicalLogService() {
		super();
	}

	/**
	 * Constructor
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
	 */
	public LogicalLogService(String label, LogicalDeployment logicalDeployment) {
		super(label, logicalDeployment);
	}

	public String getLogName() {
		return logName;
	}

	public void setLogName(String logName) {
		this.logName = logName;
	}

	public int getLogRetention() {
		return logRetention;
	}

	public void setLogRetention(int logRetention) {
		this.logRetention = logRetention;
	}

	public LogicalLogPersistentTypeEnum getPersitentType() {
		return persistentType;
	}

	public void setPersistentType(LogicalLogPersistentTypeEnum persistentType) {
		this.persistentType = persistentType;
	}

}
