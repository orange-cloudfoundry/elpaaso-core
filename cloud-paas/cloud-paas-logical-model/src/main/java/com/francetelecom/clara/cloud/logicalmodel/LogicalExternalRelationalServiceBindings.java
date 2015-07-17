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
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Provide environment bindings for LogicalExternalRelationalService
 */
@XmlRootElement
@Entity
@Table(name="EXT_RELATIONAL_SERVICE")
@GuiClassMapping(isExternal = false, serviceCatalogName = "Relational DB", status = GuiClassMapping.StatusType.SKIPPED)
public class LogicalExternalRelationalServiceBindings extends LogicalModelItem implements EnvironmentServiceBindingParameters<LogicalExternalRelationalService> {



    private String dbUser;
    private String dbPassword;
    private String dbFqdn;
    private String dbName;
    private long dbPort;

    /**
     * abstract accessor (implemented per child class)
     */
    public String getDbUser() {
        return dbUser;
    }


    public String getDbPassword() {
        return dbPassword;
    }


    public String getDbFqdn() {
        return dbFqdn;
    }


    public String getDbName() {
        return dbName;
    }


    public long getDbPort() {
        return dbPort;
    }

}
