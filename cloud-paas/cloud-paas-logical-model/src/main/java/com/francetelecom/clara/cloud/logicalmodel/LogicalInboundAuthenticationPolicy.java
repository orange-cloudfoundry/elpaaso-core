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

import com.francetelecom.clara.cloud.commons.GuiMapping;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * LogicalInboundAuthenticationPolicy
 *
 * Last updated : $LastChangedDate$
 * Last author  : $Author$
 * @version     : $Revision$
 */
@Embeddable
@XmlRootElement
public class LogicalInboundAuthenticationPolicy implements Serializable {

    @GuiMapping(status = GuiMapping.StatusType.READ_ONLY)
    @NotNull
    @Enumerated(EnumType.STRING)
    private LogicalAccessZoneEnum accessZone = LogicalAccessZoneEnum.SI;

    @GuiMapping(status = GuiMapping.StatusType.READ_ONLY)
    @NotNull
    @Enumerated(EnumType.STRING)
    private LogicalAuthenticationType authenticationType = LogicalAuthenticationType.BASIC_AUTH;

    @GuiMapping(status = GuiMapping.StatusType.READ_ONLY)
    @NotNull
    @Enumerated(EnumType.STRING)
    private LogicalProtocolEnum protocol = LogicalProtocolEnum.HTTP;  //imposed by the HTTPS constructor

	/**
	 *
	 */
	public LogicalInboundAuthenticationPolicy() {
		super();
	}

    /**
     * @return the accesZone
     */
    public LogicalAccessZoneEnum getAccessZone() {
        return accessZone;
    }

    public void setAccessZone(LogicalAccessZoneEnum accessZone) {
        this.accessZone = accessZone;
    }

    public LogicalAuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(LogicalAuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public LogicalProtocolEnum getProtocol() {
        return protocol;
    }

    public void setProtocol(LogicalProtocolEnum protocol) {
        this.protocol = protocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogicalInboundAuthenticationPolicy)) return false;

        LogicalInboundAuthenticationPolicy that = (LogicalInboundAuthenticationPolicy) o;

        if (accessZone != that.accessZone) return false;
        if (authenticationType != that.authenticationType) return false;
        if (protocol != that.protocol) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return accessZone != null ? accessZone.hashCode() : 0;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName()+"[");
        builder.append("accessZone="+accessZone+",");
        builder.append("authenticationType="+authenticationType+",");
        builder.append("protocol="+protocol);
        builder.append("]");

        return builder.toString();    //To change body of overridden methods use File | Settings | File Templates.
    }

}