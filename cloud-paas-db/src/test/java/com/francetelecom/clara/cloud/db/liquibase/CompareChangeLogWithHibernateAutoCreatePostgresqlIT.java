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

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test compare a DB initialized using liquibase change log with a db initialiazed using hibernate automatic schema creation (hbm2ddl.auto = create)
 * 
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration()
public class CompareChangeLogWithHibernateAutoCreatePostgresqlIT extends CompareChangeLogWithHibernateAutoCreateIT {
}
