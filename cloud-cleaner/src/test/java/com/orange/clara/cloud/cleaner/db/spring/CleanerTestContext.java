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

import com.orange.clara.cloud.cleaner.db.Cleaner;
import com.orange.clara.cloud.cleaner.db.DeleteStatistics;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;

/**
 * Created by WOOJ7232 on 04/02/2015.
 */
@Configuration
public class CleanerTestContext {
    @Bean(initMethod = "")
    public Cleaner getCleaner(){
        return new Cleaner().setEnabled(false);
    }

    @Bean(name = "datasource")
    public DataSource toto() {
        EmbeddedDatabaseBuilder embeddedDatabaseBuilder = new EmbeddedDatabaseBuilder();
        final EmbeddedDatabase database = embeddedDatabaseBuilder.setType(EmbeddedDatabaseType.HSQL).build();

        return database;
    }

    @Bean
    public DeleteStatistics getStatistics() {
        return new DeleteStatistics();
    }

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        //This is required to evaluate ${...} in @Value
        //http://docs.spring.io/spring-framework/docs/4.1.4.RELEASE/javadoc-api/org/springframework/context/annotation/PropertySource.html
        return new PropertySourcesPlaceholderConfigurer();
    }


}
