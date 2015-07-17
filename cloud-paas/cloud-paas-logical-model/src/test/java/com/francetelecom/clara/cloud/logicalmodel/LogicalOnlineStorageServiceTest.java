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

import org.fest.assertions.Assertions;
import org.junit.Test;

public class LogicalOnlineStorageServiceTest {

    @Test
    public void online_storage_service_default_name_is_storage() throws Exception {
        final LogicalOnlineStorageService logicalOnlineStorageService = new LogicalOnlineStorageService();
        Assertions.assertThat(logicalOnlineStorageService.getServiceName()).isEqualTo("storage");
    }

    @Test(expected = IllegalArgumentException.class)
    public void online_storage_service_name_is_should_not_be_empty() throws Exception {
        final LogicalOnlineStorageService logicalOnlineStorageService = new LogicalOnlineStorageService();
        logicalOnlineStorageService.setServiceName("");
    }

    @Test(expected = IllegalArgumentException.class)
    public void online_storage_service_name_is_should_not_be_null() throws Exception {
        final LogicalOnlineStorageService logicalOnlineStorageService = new LogicalOnlineStorageService();
        logicalOnlineStorageService.setServiceName(null);
    }

    @Test
    public void online_storage_default_capacity_is_10_mb() throws Exception {
        final LogicalOnlineStorageService logicalOnlineStorageService = new LogicalOnlineStorageService();
        Assertions.assertThat(logicalOnlineStorageService.getStorageCapacityMb()).isEqualTo(10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void online_storage_capacity_should_be_greater_than_1_mb() throws Exception {
        final LogicalOnlineStorageService logicalOnlineStorageService = new LogicalOnlineStorageService();
        logicalOnlineStorageService.setStorageCapacityMb(-1);
    }
}