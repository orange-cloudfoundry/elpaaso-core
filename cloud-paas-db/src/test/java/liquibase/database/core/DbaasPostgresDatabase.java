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
package liquibase.database.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * This class extends liquibase support of Postgresql Database for postgresql databases created with DBaaS<br>
 * By default liquibase assumes that all objects created in public schema are owned by the connected user<br>
 * But in postgresql databases created by DBaaS, some 'system' objects are created in public schema but are are not owned by the user<br>
 * This implementation enables to avoid those objects from being dropped by liquibase dropAll command used in tests<br>
 */
public class DbaasPostgresDatabase extends PostgresDatabase {
    private static Logger LOGGER = LoggerFactory.getLogger(DbaasPostgresDatabase.class);


    public DbaasPostgresDatabase() {
        super();
        LOGGER.info("Creating DbaasPostgresDatabase");
    }

    @Override
    public int getPriority() {
        return getHigherPriorityToReplaceDefaultPosgresqlDbImpl();
    }

    private int getHigherPriorityToReplaceDefaultPosgresqlDbImpl() {
        return PRIORITY_DATABASE;
    }

    @Override
    public Set<String> getSystemViews() {
        LOGGER.debug("getSystemViews from DbaasPostgresDatabase - adding pg_stat_statements")      ;
        Set<String> systemViews = super.getSystemViews();
        systemViews.add("pg_stat_statements");
        return systemViews;
    }


}
