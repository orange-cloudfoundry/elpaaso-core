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
 * External Relational DataBase Service.
 *
 * Represents a database service which lives independently of the environment. 
 * It is typically shared among environment instances. 
 * 
 * This is typically for the paas internal deployment (dog fooding) and not yet exposed to projects.
 */
@XmlRootElement
@Entity
@Table(name="EXT_RELATIONAL_SERVICE")
@GuiClassMapping(isExternal = false, serviceCatalogName = "Relational DB", status = GuiClassMapping.StatusType.SKIPPED)
public class LogicalExternalRelationalService extends LogicalRelationalService {

    //TODO: consider adding the network on which the database is connected

}
