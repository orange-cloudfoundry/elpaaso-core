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
package com.orange.clara.cloud.cleaner.db;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

/**
 * Created by WOOJ7232 on 04/02/2015.
 */
@Component
public class Cleaner {
    private static Logger LOGGER = LoggerFactory.getLogger(Cleaner.class);

    @Autowired
    @Qualifier("datasource")
    private DataSource dataSource;

    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DeleteStatistics statistics;

    @Value("${cleaner.db.enabler:false}")
    private boolean enabled=false;

    public void init()   {
        if (! enabled){
            LOGGER.info("DB Cleaner is disabled");
            return;
        }
        jdbcTemplate = new JdbcTemplate(dataSource);
        LOGGER.warn("DB Cleaner is ENABLED. Starting reset of database {}",dataSource);

        perfomCleanup();
    }

    public void perfomCleanup() {
        Validate.notNull(jdbcTemplate);
        dropAllTable();
        dropAllSequence();
        LOGGER.info("{}",statistics);
    }

    private void dropAllSequence() {
        final List<String> sequenceToDelete = jdbcTemplate.queryForList("select sequence_name from information_schema.sequences where sequence_schema='public' order by sequence_name;", String.class);
        LOGGER.info("Cleanup {} sequence(s)",sequenceToDelete.size());
        for (String sequence : sequenceToDelete) {
            LOGGER.info("Droping sequence {}", sequence);
            jdbcTemplate.execute("DROP SEQUENCE "+ sequence + " cascade;");
            statistics.deleteSequence();
        }
    }

    private void dropAllTable() {
        final List<String> tableToDelete = jdbcTemplate.queryForList("select table_name from information_schema.tables where table_schema='public' and table_type!='VIEW' order by table_name;", String.class);
        LOGGER.info("Cleanup {} table(s)",tableToDelete.size());
        for (String table : tableToDelete) {
            LOGGER.info("Droping table {}",table);
            jdbcTemplate.execute("DROP TABLE "+table+ " cascade;");
            statistics.deleteTable();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Cleaner setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public DeleteStatistics getStatistics() {
        return statistics;
    }


}
