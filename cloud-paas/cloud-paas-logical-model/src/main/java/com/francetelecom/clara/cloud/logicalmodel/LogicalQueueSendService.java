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
package com.francetelecom.clara.cloud.logicalmodel;

import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * External MOM : Queue Send Service.
 *
 * A subscription to a “queue send service” (QSS) allows one producer applications to send messages
 * to a queue. These messages are received by a single distinct consumer application
 * (either deployed in the PaaS or connected to the BMO or BOA infrastructures).
 *
 * Multiple producer applications may subscribe to the same QSS targeting a given consumer
 * application. The delivery of messages is guaranteed with at least once semantics
 * (i.e. a same message can be received more than once, but no less).
 * The order of messages is not guaranteed. A message is removed from the queue once consumed.
 * This service is exposed to application through the JMS 1.2 Destination API.

 * A queue is associated with a service name within the consumer application
 * (basicat code, name and version).
 *
 * A given queue can only be used by environment of the same type
 * (e.g. prod, preprod, dev, qualification). In other words, development environment for application A may not
 * send messages consumed by a production environment of application B.
 *
 * @author poyt7496
 *
 */
@SuppressWarnings("serial")
@XmlRootElement
@Entity
@Table(name = "LogicalQueueSendService")
@GuiClassMapping(serviceCatalogName = "Point-to-point messaging (QSS)", serviceCatalogNameKey = "ptp.messaging.qss", status = GuiClassMapping.StatusType.BETA, isExternal = true)
public class LogicalQueueSendService extends LogicalService {


    /**
     * Name of the target queue name : identifier function (queue)
     *
     * TODO: clarify restrictions on the name (size smaller than ?, accepted characters)
     */
	@NotNull
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = true)
    private String targetServiceName;

    /**
     * The version of the targetted service.
     * TODO: clarify restrictions on the name (size, accepted characters)
     */
	@NotNull
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = true)
	private String targetServiceVersion;

    /**
     * The ID of the application to which messages should be sent. This is formatted
     * in the Orange FR SI Basicat format.
     *
     * FIXME: Is this optional ? How do we support OBS/DPS applications? Do they all have basicat codes ? Rename into "application code"
     * Mandatory for BOA, then it is given by BOA
     */
	@NotNull
    @GuiMapping(status = GuiMapping.StatusType.SKIPPED, functional = true)
	private String targetBasicatCode = "NONE";

    /**
     * Prerequisite that the target application/version and associated service needs to be available in MOMaaS
     */
    @NotNull
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = true)
    private String targetApplicationName;
	
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = true)
    private String targetApplicationVersion;

    /**
     * The application-preferred name of the JNDI key whose look up returns a javax.jms.Destination instance used by
     * by the application to identify the queue to send messages to.
     *
     * FIXME: this name is set by the application architect and then used in the application. It should be
     * not null. conficts with generated name in {@link #createJndiQueueName(String, String, String)}
     *
     */
    //@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = true)
	@Pattern(regexp = "[a-z]{1}[0-9a-z.]{2,18}")
    private  String jndiQueueName;

    /**
     * The application-preferred name of the JNDI key whose look up returns a javax.jms.ConnectionFactory instance used by
     * by the application to obtain a javax.jms.Session suiteable for sending message to the provided Destination.
     *
     * If not specified, the default JNDI key in the jonas container  is used: "CF"
     * FIXME: add this into construtor
     */
    //@NotNull
    @GuiMapping(status = GuiMapping.StatusType.READ_ONLY, functional = true)
    private String jndiConnectionFactory = "CF";

    /**
     * sla: maximum message size. Should be smaller than 10 MB.
     * This is measured as the number of bytes of serialized Java message object
     *
     * This information is leveraged by BOA to select different transport adapted for
     * the size of the message.
     *
     */
    @NotNull
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    @Max(value = 10000)
    private long msgMaxSizeKB;

    /**
     * The expected maximum number of messages per day that the application plans to send.
     * When this number is greater than capacity of the consumer, the infrastructure will queue messages.
     *
     * This field is then used as a hint to provision storage capacity for retaining unconsumed messages.
     */
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    private long maxNbMsgPerDay;

    /**
     * The maximum duration that unconsumed messages will be retained by the infrastructure.
     */
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    @Max(value = 5)
    private long nbRetentionDay;
	
    
	/**
	 * default constuctor for mapping
	 */
	public LogicalQueueSendService() {
		// TODO Auto-generated constructor stub
	}
	
	
	/**
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
     */
	public LogicalQueueSendService(String label, LogicalDeployment logicalDeployment, String targetServiceName, String targetServiceVersion,
			String targetBasicatCode, String targetApplicationName,
			String targetApplicationVersion, long msgMaxSizeKB,
			long maxNbMsgPerDay, long nbRetentionDay) {
		super(label, logicalDeployment);
		this.targetServiceName = targetServiceName;
		this.targetServiceVersion = targetServiceVersion;
		this.targetBasicatCode = targetBasicatCode;
		this.targetApplicationName = targetApplicationName;
		this.targetApplicationVersion = targetApplicationVersion;
		this.msgMaxSizeKB = msgMaxSizeKB;
		this.maxNbMsgPerDay = maxNbMsgPerDay;
		this.nbRetentionDay = nbRetentionDay;
	}


    public String getTargetServiceName() {
		return targetServiceName;
	}


	public void setTargetServiceName(String targetServiceName) {
		this.targetServiceName = targetServiceName;
	}


	public String getTargetServiceVersion() {
		return targetServiceVersion;
	}


	public void setTargetServiceVersion(String targetServiceVersion) {
		this.targetServiceVersion = targetServiceVersion;
	}

	public long getMsgMaxSizeKB() {
		return msgMaxSizeKB;
	}


	public void setMsgMaxSizeKB(long msgMaxSizeKB) {
		this.msgMaxSizeKB = msgMaxSizeKB;
	}


	public String getTargetBasicatCode() {
		return targetBasicatCode;
	}


	public void setTargetBasicatCode(String targetBasicatCode) {
		this.targetBasicatCode = targetBasicatCode;
	}

//	public String getName() {
//		return name;
//	}

//	public void setName(String name) {
//		this.name = name;
//	}


	public String getTargetApplicationName() {
		return targetApplicationName;
	}

	public void setTargetApplicationName(String targetApplicationName) {
		this.targetApplicationName = targetApplicationName;
	}

	public long getMaxNbMsgPerDay() {
		return maxNbMsgPerDay;
	}

	public void setMaxNbMsgPerDay(long maxNbMsgPerDay) {
		this.maxNbMsgPerDay = maxNbMsgPerDay;
	}

	public long getNbRetentionDay() {
		return nbRetentionDay;
	}

	public void setNbRetentionDay(long nbRetentionDay) {
		this.nbRetentionDay = nbRetentionDay;
	}


	public String getTargetApplicationVersion() {
		return targetApplicationVersion;
	}


	public void setTargetApplicationVersion(String targetApplicationVersion) {
		this.targetApplicationVersion = targetApplicationVersion;
	}


	public String getJndiQueueName() {
		return jndiQueueName;
	}


	public void setJndiQueueName(String jndiQueueName) {
		this.jndiQueueName = jndiQueueName;
	}

	public String createJndiQueueName(String applicationName, String basicatCode, String version){
		
		this.jndiQueueName = applicationName.substring(0, 4) 
						+ "_" + basicatCode 
						+ "_"+ version 
						+ "." + this.targetApplicationName 
						+ "_" + this.targetBasicatCode 
						+ "." + this.targetServiceName 
						+ "_" + this.targetServiceVersion 
						+ "." + "CLO" + "." + "OUT";
		return this.jndiQueueName;
	}


    public String getJndiConnectionFactory() {
        return jndiConnectionFactory;
    }

    public void setJndiConnectionFactory(String jndiConnectionFactory) {
        this.jndiConnectionFactory = jndiConnectionFactory;
    }
}
