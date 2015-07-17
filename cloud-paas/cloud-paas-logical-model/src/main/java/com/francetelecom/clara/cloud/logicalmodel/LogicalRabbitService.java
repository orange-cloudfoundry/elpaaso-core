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
import org.springframework.util.Assert;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Intra-application async messaging service (AMQP)
 * Allows asynchronous messaging to be exchanged within an application through a AMQP messaging oriented middleware (Rabbitmq)
 *
 * An ExecutionNode is connected to one or more LogicalMomService. Both message reception and message sending may be
 * performed on the same service.
 *
 * @author APOG7416
 */
@XmlRootElement
@Entity
@Table(name = "RABBIT_SERVICE")
@GuiClassMapping(isExternal = false, serviceCatalogName = "Internal RabbitMQ Point-to-point messaging", serviceCatalogNameKey = "internal.rabbit.messaging", status = GuiClassMapping.StatusType.SUPPORTED)
public class LogicalRabbitService extends LogicalService {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3710910374791688708L;
	/**
     * cloud foundry service name.
     *
     */
    @GuiMapping(status = GuiMapping.StatusType.SUPPORTED)
    @NotNull
    @Size(min = 1)
    private String serviceName = "rabbit";


	/**
	 * private constructor for mapping
	 */
	public LogicalRabbitService() {
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
        Assert.hasText(serviceName, "Unable to set service name with empty value.");
        this.serviceName = serviceName;
	}

	
}
