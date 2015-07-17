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
package com.orange.clara.cloud.cleaner.db.spring;

import com.francetelecom.clara.cloud.test.database.DbaasDatabase;
import com.francetelecom.clara.cloud.test.database.DbaasService;
import com.orange.clara.cloud.cleaner.db.Cleaner;
import com.orange.clara.cloud.cleaner.db.DeleteStatistics;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.EngineWsEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

/**
 * Created by WOOJ7232 on 04/02/2015.
 */
@Configuration
@ImportResource(value="classpath:/META-INF/test-database-dbaas-service-context.xml")
@PropertySource(value = "classpath:/com/francetelecom/clara/cloud/commons/testconfigurations/credentials-${datacenter:reference}.properties")
public class CleanerITContext {
    @Autowired
    private DbaasService dbaasService;

    @Value("${test.db.creation.user}")
    String dbUser;

    @Value("${test.db.creation.password}")
    String dbPassword;


    @Bean
    public DeleteStatistics getStatistics() {
        return new DeleteStatistics();
    }

    @Bean(initMethod = "")
    public Cleaner getCleaner(){
        return new Cleaner().setEnabled(false);
    }

    @Bean(name = "datasource")
    @DependsOn("dbCleanerIT")
    public DataSource toto() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        DbaasDatabase database = createDbaasDb();
        dataSource.setPassword(database.getPassword());
        dataSource.setUrl(database.getUrl());
        dataSource.setUsername(database.getUser());

        return dataSource;
    }

    @Bean(name="dbCleanerIT", initMethod="create", destroyMethod="delete")
    DbaasDatabase createDbaasDb() {
        final DbaasDatabase database = new DbaasDatabase();
        database.setDbaasService(dbaasService);
        database.setEngine(EngineWsEnum.POSTGRESQL);
        database.setUser(dbUser);
        database.setPassword(dbPassword);
        database.setDescription("Database used in cloud-cleaner integration tests - should be automatically deleted");

        return database;
        }



    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        //This is required to evaluate ${...} in @Value
        //http://docs.spring.io/spring-framework/docs/4.1.4.RELEASE/javadoc-api/org/springframework/context/annotation/PropertySource.html
        return new PropertySourcesPlaceholderConfigurer();
    }


}
