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

/**
 * Represents the RDBMS SQL dialect and variant that would be requested in the LogicalRelationalService.
 */
public enum LogicalRelationalServiceSqlDialectEnum {

    @GuiMapping
    POSTGRESQL_DEFAULT, /** Default latest available version of the PostgreSQL data base */

    @GuiMapping
    MYSQL_DEFAULT,      /** Default latest available version of the MYSQL data base */

    @GuiMapping (status = GuiMapping.StatusType.SKIPPED)
    DEFAULT,            /** Use this value when no specific dependency on the SQL dialect is required by the application
                            This may be useful in the future to abstract completly the dialect from the app. However,
                            the app needs to either autoadapt to the dialect or to make sure that it has no dependencies
                            on a specific dialect */

}
