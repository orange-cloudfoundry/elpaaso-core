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
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * external MOM : Queue Receive Service
 * @author poyt7496
 *
 */
@SuppressWarnings("serial")
@XmlRootElement
@Entity
@Table(name = "LogicalQueueReceiveService")
@GuiClassMapping(serviceCatalogName = "Point-to-point messaging (QRS)", serviceCatalogNameKey = "ptp.messaging.qrs", status = GuiClassMapping.StatusType.BETA, isExternal = true)
public class LogicalQueueReceiveService extends LogicalService {
	
	// identifier function (queue)
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@Pattern(regexp = "[a-zA-Z]{1}[0-9a-zA-Z.]{2,18}")
    private String serviceName;
	
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
    @Size(min = 1) //String size larger than 1 (i.e. non empty). No FR-specific GoRoCo format imposed.
	private String serviceVersion;

	//Jndi Name of queue
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
	@Pattern(regexp = "[a-z]{1}[0-9a-z.]{2,18}")
	private  String jndiQueueName;
	
    /**
     * sla: maximum message size. Should be smaller than 10 MB.
     * This is measured as the number of bytes of serialized Java message object
     *
     * This information is leveraged by <ol>
     * <li>BOA to provision appropriately the persistent storage adapted for the size of the message.</li>
     * <li>the consummer application which is designed to handle messages smaller than this size.</li>
     * </ol>
     * -
     * -
     */
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
	@Max(value = 10000)
    private long msgMaxSizeKB; // <10Mo
	
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
    private long maxNbMsgPerDay; //expected to consume
	
	@NotNull
	@GuiMapping(status = GuiMapping.StatusType.SUPPORTED, functional = false)
	@Max(value = 5)
    private long nbRetentionDay; //max 5 days

	/**
	 * default constuctor for mapping
	 */
	public LogicalQueueReceiveService() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param serviceName
	 * @param serviceVersion
     * @param msgMaxSizeKB
     * @param maxNbMsgPerDay
     * @param nbRetentionDay
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
	 */
	public LogicalQueueReceiveService(String label, LogicalDeployment logicalDeployment, String serviceName,
			String serviceVersion, long msgMaxSizeKB, long maxNbMsgPerDay,
			long nbRetentionDay) {
		super(label, logicalDeployment);
		this.serviceName = serviceName;
		this.serviceVersion = serviceVersion;
		this.msgMaxSizeKB = msgMaxSizeKB;
		this.maxNbMsgPerDay = maxNbMsgPerDay;
		this.nbRetentionDay = nbRetentionDay;
	}

	/**
	 * @param serviceName
	 * @param serviceVersion
     * @param msgMaxSizeKB
     * @param maxNbMsgPerDay
     * @param nbRetentionDay
	 */
	public LogicalQueueReceiveService(String serviceName,
			String serviceVersion, long msgMaxSizeKB, long maxNbMsgPerDay,
			long nbRetentionDay) {
		super();
		this.serviceName = serviceName;
		this.serviceVersion = serviceVersion;
		this.msgMaxSizeKB = msgMaxSizeKB;
		this.maxNbMsgPerDay = maxNbMsgPerDay;
		this.nbRetentionDay = nbRetentionDay;
	}
	
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public long getMsgMaxSizeKB() {
		return msgMaxSizeKB;
	}

	public void setMsgMaxSizeKB(long msgMaxSizeKB) {
		this.msgMaxSizeKB = msgMaxSizeKB;
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

	public String getJndiQueueName() {
		return jndiQueueName;
	}

	public void setJndiQueueName(String jndiQueueName) {
		this.jndiQueueName = jndiQueueName;
	}

	public String createJndiQueueName(String applicationName, String basicatCode){
		
		this.jndiQueueName = applicationName.substring(0, 4) 
						+ "_" + basicatCode  
						+ "." + this.serviceName 
						+ "_" + this.serviceVersion 
						+ "." + "IN";
		
		return this.jndiQueueName;
	}
	
}
