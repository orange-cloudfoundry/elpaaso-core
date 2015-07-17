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
package com.francetelecom.clara.cloud.db.liquibase;

import liquibase.exception.LiquibaseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.sql.SQLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class GenerateFullChangelogIT {

    private static Logger logger = LoggerFactory.getLogger(GenerateFullChangelogIT.class.getName());

    @Value("jdbc:postgresql://${test.liquibase.futcopy.host}:${test.liquibase.futcopy.port}/${test.liquibase.futcopy.dbname}?autoReconnect=true")
    String dbUrl;

    @Value("${test.liquibase.futcopy.user}")
    String dbUser;
    @Value("${test.liquibase.futcopy.password}")
    String dbPassword;

    @Before
    public void setup() throws IOException {

    }

    @After
    public void cleanup() {
    }

    @Test
    public void should_generate_new_paas_db_changelog() throws LiquibaseException, SQLException, IOException, ParserConfigurationException {


        String changeLogFile = "target/paas-db-changelog-NEW-full.xml";
        LiquibaseTestWrapper liquibaseTestWrapper = new LiquibaseTestWrapper();
        boolean shouldExportData = false;
        liquibaseTestWrapper.generateChangeLog(dbUrl,dbUser,dbPassword, changeLogFile        );
    }


}
