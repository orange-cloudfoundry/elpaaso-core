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

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Intra-application async messaging service (MOM)
 * Allows asynchronous messaging to be exchanged within an application through a messaging oriented middleware (MOM) using the JMS API.
 *
 * JMS api version supported: 1.1
 *
 * Limitations w.r.t. JMS:
 * <ol>
 *     <li>JMS ReplyTo field is not supported</li>
 *     <li>Only queues are supported (topic are planned for future)</li>
 * </ol>
 *
 * An ExecutionNode is connected to one or more LogicalMomService. Both message reception and message sending may be
 * performed on the same service.
 *
 * FIXME: rename into LogicalInternalMomService
 * @author APOG7416
 */
@XmlRootElement
@Entity
@Table(name = "MOM_SERVICE")
@GuiClassMapping(isExternal = false, serviceCatalogName = "Internal Point-to-point messaging", serviceCatalogNameKey = "internal.ptp.messaging", status = GuiClassMapping.StatusType.BETA)
public class LogicalMomService extends LogicalService {

    /**
     * Jndi name at which the javax.jms.ConnectionFactory instance will be made available.
     * This ConnectionFactory supports XA transactions, and therefore may be involved into a
     * transaction with RDB service. The JTA transaction manager is available in the JNDI
     * in the default well-known location.
     *
     * Currently, a single default instance is available named "CF".
     */
    @GuiMapping(status = GuiMapping.StatusType.READ_ONLY)
    @NotNull
    @Size(min = 1)
    private String jmsConnectionFactoryJndiName = "CF";


    /**
     * Name of the JNDI property under which the javax.jms.Destination instance will be available.
     * Should be unique within all LogicalMomService connected in the architecture
     */
    @GuiMapping
    @NotNull
    @Size(min = 1, max=255)
    String destinationName = "";


    /**
     * Optional name of the JNDI property under which the deadLetter queue javax.jms.Queue instance will be available.
     * Should be unique within all LogicalMomService connected in the architecture.
     */
    @Size(min = 0, max=255)
    @GuiMapping
    String deadLetterQueueName = "";

    //
    // SLO
    //

    /**
     * Number of messages in the dead letter queue, after which an alert should be raised.
     */
    //@Transient
    //private int deadLetterQueueAlarmingThreshold = 100;

    /**
     * Hint to the paas for the sizing of the deadletter queue. Max number of messages to be hold in the queue.
     * When this capacity is exceeded, unprocessed messages are lost (app-ops may check logs for such occurence)
     */
    @GuiMapping
    @Min(value = 1)
    @Max(value = 100)
    private int deadLetterQueueCapacity = 100;

    /**
     * sla: maximum message size. Should be smaller than 10 MB.
     * This is measured as the number of bytes of serialized Java message object
     *
     * This information is leveraged to size the message persistence storage     *
     */
    @NotNull
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    @Min(value = 1)
    @Max(value = 10000)
    private int msgMaxSizeKB = 10;


    /**
     * Max number of message to be persisted in the queue when the receiver is not consumming
     * messages. This should include messages sent by all ExecNode using this MomService.
     *
     * Note that the queue may enforce this max number of messages. When this maximum capacity is reached,
     * the next message sending will be rejected.
     *
     * A large capacity can be rejected if insufficient persistent storage is available
     * to guarantee this SLO.
     */
    @GuiMapping(functional = false)
    @Min(value = 1000)
	int destinationCapacity = 1000;

     /**
     * Optionally enables the deadLetter queue to which dead messages would be added.
      * Dead messages are messages that errored during processeing
      * (e.g. a transaction rollback while processing the message, or an exception thrown).
      *
      * When enabled, the application is responsible for processing messages in the dead letter queue
      * (usually with a lower priority, possibly as a scheduled batch in a distinct ExecNode) to avoid being filled up.
      *
     */
    @GuiMapping(functional = false)
	boolean hasDeadLetterQueue = true;

    /**
     * On message reception error (e.g. a transaction rollback while processing the message, or an exception
     * thrown), the max number of times the message will be sent to the receiver.
     */
    @GuiMapping(functional = false)
    @Min(value = 0)
    @Max(value = 10)
    int retriesBeforeMovingToDeadLetterQueue = 3;

    /**
     * hints to the paas to indicate that some persistent messages will be sent. This is used as an optimization 
     * hint for the paas to not provision persistent capacity for applications that exchange only non persistent messages
     * Set this to false only for apps handling messages whose loss has no business impact.
     */
    @GuiMapping(functional = false)
	boolean persistentMessagesUsed = true;

    /**
     * In the future, controls whether high availability is requested for this queue. 
     */
    @GuiMapping(functional = false, status = GuiMapping.StatusType.READ_ONLY)
    boolean highAvailability = false; 

    /**
     * @deprecated TODO: remove this,this is builtin.
     */
	boolean loadBalancing = false; // not yet implemented

	/**
	 * private constructor for mapping
	 */
	public LogicalMomService() {

	}

	/**
	 * Constructor
	 * 
	 * @param label
	 * @param logicalDeployment
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
	 */
	public LogicalMomService(String label, LogicalDeployment logicalDeployment) {
		super(label, logicalDeployment);
	}

	/**
	 * 
	 * @return
	 */
	public String getDestinationName() {
		return destinationName;
	}

	/**
	 * 
	 * @param destinationName
	 */
	public void setDestinationName(String destinationName) {
		this.destinationName = destinationName;
	}

	/**
	 * 
	 * @return
	 */
	public int getDestinationCapacity() {
		return destinationCapacity;
	}

	/**
	 * 
	 * @param destinationCapacity
	 */
	public void setDestinationCapacity(int destinationCapacity) {
		this.destinationCapacity = destinationCapacity;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isHasDeadLetterQueue() {
		return hasDeadLetterQueue;
	}

	/**
	 * 
	 * @param hasDeadLetterQueue
	 */
	public void setHasDeadLetterQueue(boolean hasDeadLetterQueue) {
		this.hasDeadLetterQueue = hasDeadLetterQueue;
	}

    /**
	 *
	 * @param persistent
	 */
	public void setPersistent(boolean persistent) {
		this.persistentMessagesUsed = persistent;
	}

	public Boolean getPersistent() {
		return this.persistentMessagesUsed;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isHighAvailability() {
		return highAvailability;
	}

	/**
	 * 
	 * @param highAvailability
	 */
	public void setHighAvailability(boolean highAvailability) {
		this.highAvailability = highAvailability;
	}

	/**
	 * 
	 * @return
	 */
	public boolean isLoadBalancing() {
		return loadBalancing;
	}

	/**
	 * 
	 * @param loadBalancing
	 */
	public void setLoadBalancing(boolean loadBalancing) {
		this.loadBalancing = loadBalancing;
	}

	/**
	 * @return the jmsConnectionFactoryJndiName
	 */
	public String getJmsConnectionFactoryJndiName() {
		return jmsConnectionFactoryJndiName;
	}

	/**
	 * @param jmsConnectionFactoryJndiName the jmsConnectionFactoryJndiName to set
	 */
	public void setJmsConnectionFactoryJndiName(String jmsConnectionFactoryJndiName) {
		this.jmsConnectionFactoryJndiName = jmsConnectionFactoryJndiName;
	}

	/**
	 * @return the deadLetterQueueName
	 */
	public String getDeadLetterQueueName() {
		return deadLetterQueueName;
	}

	/**
	 * @param deadLetterQueueName the deadLetterQueueName to set
	 */
	public void setDeadLetterQueueName(String deadLetterQueueName) {
		this.deadLetterQueueName = deadLetterQueueName;
	}

	/**
	 * @return the deadLetterQueueCapacity
	 */
	public int getDeadLetterQueueCapacity() {
		return deadLetterQueueCapacity;
	}

	/**
	 * @param deadLetterQueueCapacity the deadLetterQueueCapacity to set
	 */
	public void setDeadLetterQueueCapacity(int deadLetterQueueCapacity) {
		this.deadLetterQueueCapacity = deadLetterQueueCapacity;
	}

	/**
	 * @return the msgMaxSizeKB
	 */
	public int getMsgMaxSizeKB() {
		return msgMaxSizeKB;
	}

	/**
	 * @param msgMaxSizeKB the msgMaxSizeKB to set
	 */
	public void setMsgMaxSizeKB(int msgMaxSizeKB) {
		this.msgMaxSizeKB = msgMaxSizeKB;
	}

	/**
	 * @return the retriesBeforeMovingToDeadLetterQueue
	 */
	public int getRetriesBeforeMovingToDeadLetterQueue() {
		return retriesBeforeMovingToDeadLetterQueue;
	}

	/**
	 * @param retriesBeforeMovingToDeadLetterQueue the retriesBeforeMovingToDeadLetterQueue to set
	 */
	public void setRetriesBeforeMovingToDeadLetterQueue(int retriesBeforeMovingToDeadLetterQueue) {
		this.retriesBeforeMovingToDeadLetterQueue = retriesBeforeMovingToDeadLetterQueue;
	}

	/**
	 * @return the persistentMessagesUsed
	 */
	public boolean isPersistentMessagesUsed() {
		return persistentMessagesUsed;
	}

	/**
	 * @param persistentMessagesUsed the persistentMessagesUsed to set
	 */
	public void setPersistentMessagesUsed(boolean persistentMessagesUsed) {
		this.persistentMessagesUsed = persistentMessagesUsed;
	}

}
