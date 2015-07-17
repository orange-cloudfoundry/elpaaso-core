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

import com.orange.clara.cloud.cleaner.db.spring.CleanerITContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CleanerITContext.class)
public class CleanerIT {

    @Autowired
    DataSource dataSource;

    @Autowired
    Cleaner  cleaner;

    @Before
    public void setup(){
    }

    @Test
    public void should_cleanup_on_init(){
        final JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("create table employee (id int, name varchar(255))");
        jdbcTemplate.execute("create table startup (id int, name varchar(255))");
        jdbcTemplate.execute("create table boss (id int, name varchar(255))");
        jdbcTemplate.execute("CREATE SEQUENCE hibernate_sequence INCREMENT 1  MINVALUE 1  MAXVALUE 9223372036854775807 START 33  CACHE 1;");
        cleaner.setEnabled(true);
        cleaner.init();

        DeleteStatistics statistics=cleaner.getStatistics();
        assertThat(statistics.getTablesCount()).isEqualTo(3);
        assertThat(statistics.getSequencesCount()).isEqualTo(1);

    }


}