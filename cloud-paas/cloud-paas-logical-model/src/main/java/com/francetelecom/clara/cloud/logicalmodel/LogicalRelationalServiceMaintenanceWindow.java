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

import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Indicates the preferred maintenance windows for database maintenance operations within a normal week.
 *
 * This is a modeled as a preferred start time in week, and preferred duration.
 */
//@Entity
//@Table(name="RDBMS_MAINTENANCE_WINDOW")
@Embeddable
@XmlRootElement

public class LogicalRelationalServiceMaintenanceWindow extends LogicalModelItem {

    public static final LogicalRelationalServiceMaintenanceWindow DEFAULT_WINDOW = new LogicalRelationalServiceMaintenanceWindow();

    //TODO: add fields to properly manage ddd:hh:mm-HH
    // This weekly time range in the following format
    //ddd:hh:mm-HH
    //with ddd the day when maintenance can occur
    //-	Mon : Monday
    //-	Tue : Tuesday
    //-	Wed : Wednesday
    //-	Thu : Thrusday
    //-	Fri : Friday
    //-	Sat : Saturday
    //-	Sun : Sunday
    //hh:mm is the time when maintenance can start this day, Greenwich time.
    //HH is the duration in hours for the maintance period
    //Exemple:
    //Sun:20:00-04
    //{String}


}
