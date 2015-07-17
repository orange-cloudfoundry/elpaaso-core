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
package com.francetelecom.clara.cloud.service.scheduled;

import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.francetelecom.clara.cloud.service.OpsService;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledDatabasePurgeMonitorTest {
    @Spy
    private ScheduledDatabasePurgeMonitor scheduledDatabasePurgeMonitor;
    @Mock
    private OpsService opsService;

    @Before
    public void setup() {
        scheduledDatabasePurgeMonitor.setOpsService(opsService);
    }

    @Test
    public void should_purge_using_ops_service() {
        // WHEN
        scheduledDatabasePurgeMonitor.purgeDatabase();
        // THEN
        verify(opsService).purgeDatabase();
    }
}
