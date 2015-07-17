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
import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents internal HTTP (usually web service) exchanges within an application.
 *
 * User: Loic Descotte
 * Date: 28/01/11
 */
@Entity
@XmlRootElement
@GuiClassMapping(status = GuiClassMapping.StatusType.SKIPPED, isExternal = false)
public class LogicalInternalHttpCommunicationService extends LogicalService {

    @NotNull
    private String contextRoot;

    protected LogicalInternalHttpCommunicationService() {
    }

    /**
     * @deprecated Should not be called anymore, use empty constructor instead
     * followed by {@link LogicalDeployment#addLogicalService(LogicalService)}
     */
    public LogicalInternalHttpCommunicationService(String label, LogicalDeployment logicalDeployment, String contextRoot) {
        super(label, logicalDeployment);
        this.contextRoot = contextRoot;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }
}
