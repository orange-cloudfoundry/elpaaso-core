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
package com.francetelecom.clara.cloud.core.service;

import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.coremodel.EnvironmentRepository;
import com.francetelecom.clara.cloud.environment.impl.ManageEnvironmentImpl;
import com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.fest.assertions.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ManageEnvironmentImplPurgeTest {
    @Spy
    ManageEnvironmentImpl manageEnvironment = new ManageEnvironmentImpl();
    @Mock
    EnvironmentRepository environmentRepository;

    @Before
    public void setup() throws ObjectNotFoundException, MalformedURLException, UnsupportedProjectionException {
        manageEnvironment.setEnvironmentRepository(environmentRepository);
        manageEnvironment.setPurgeRetentionDelayInDay(Integer.valueOf(5));
    }

    @Test
    public void should_find_older_removed_environments() {
        // GIVEN
        Environment envA = mock(Environment.class);
        Environment envB = mock(Environment.class);
        List<Environment> olderEnvironments = Arrays.asList(envA, envB);
        doReturn(olderEnvironments).when(environmentRepository).findRemovedOlderThanNDays(any(Date.class));

        // WHEN
        List<Environment> oldRemovedEnvironments = manageEnvironment.findOldRemovedEnvironments();

        // THEN
        verify(environmentRepository).findRemovedOlderThanNDays(any(Date.class));
        assertThat(oldRemovedEnvironments).contains(envA, envB);
    }
}
