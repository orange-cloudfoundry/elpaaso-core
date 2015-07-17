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
 * LogicalOutboundAuthenticationPolicy
 * WebService authenticate policy
 *
 * Last updated : $LastChangedDate: 2012-06-04 08:59:18 +0200 (lun., 04 juin 2012) $
 * Last author  : $Author: tawe8231 $
 * @version     : $Revision: 17302 $
 */
@Embeddable
@XmlRootElement
public class LogicalOutboundAuthenticationPolicy implements Serializable {
    @GuiMapping(status = GuiMapping.StatusType.READ_ONLY)
    @NotNull
    @Enumerated(EnumType.STRING)
    private LogicalAuthenticationType authenticationType = LogicalAuthenticationType.NONE;

    @GuiMapping(status = GuiMapping.StatusType.READ_ONLY)
    @NotNull
    @Enumerated(EnumType.STRING)
    private LogicalProtocolEnum protocol = LogicalProtocolEnum.HTTP;  //imposed by the HTTPS constructor

    /**
     *
     */
    public LogicalOutboundAuthenticationPolicy() {
        super();
    }

    /**
     * @return the protocol
     */
    public LogicalProtocolEnum getProtocol() {
        return protocol;
    }


    /**
     * @return the authenticationType
     */
    public LogicalAuthenticationType getAuthenticationType() {
        return authenticationType;
    }


    public void setAuthenticationType(LogicalAuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public void setProtocol(LogicalProtocolEnum protocol) {
        this.protocol = protocol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LogicalOutboundAuthenticationPolicy)) return false;

        LogicalOutboundAuthenticationPolicy that = (LogicalOutboundAuthenticationPolicy) o;

        if (authenticationType != that.authenticationType) return false;
        if (protocol != that.protocol) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = authenticationType != null ? authenticationType.hashCode() : 0;
        result = 31 * result + (protocol != null ? protocol.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append(this.getClass().getName()+"[");
        builder.append("authenticationType="+authenticationType+",");
        builder.append("protocol="+protocol+",");
        builder.append("]");

        return builder.toString();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
