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

import com.orange.clara.cloud.cleaner.db.spring.CleanerTestContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;

import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CleanerTestContext.class)
public class CleanerTest {

    @Autowired
    DataSource dataSource;

    @Autowired
    Cleaner  cleaner;

    @Before
    public void setup(){

        cleaner = spy(cleaner);
    }

    @Test
    public void should_not_cleanup_on_init_if_disabled(){
        cleaner.init();
        verify(cleaner,never()).perfomCleanup();
    }

}