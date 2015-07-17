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
package com.francetelecom.clara.cloud.scalability.impl;

import java.util.Arrays;
import java.util.Collection;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.francetelecom.clara.cloud.TestHelper;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDaoTestUtils;
import com.francetelecom.clara.cloud.scalability.ManageScalability;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class ManageScalabilityImplDeleteTest {
    private static final transient org.slf4j.Logger logger
            = LoggerFactory.getLogger(ManageScalabilityImplDeleteTest.class);

    @Autowired(required = true)
    ManageScalability manageScalability;

    @Autowired(required = true)
    HsqlDbUtils hsqlDbUtils;

    /**
     * Maven Dao is mocked
     */
    @Autowired
    @Qualifier("mvnDao")
    protected MvnRepoDao mvnRepoDaoMock;

    @Before
    public void setup() {
    	// given admin is authenticated
		TestHelper.loginAsAdmin();

        // Configure MvnRepoDao Mock
        MvnRepoDaoTestUtils.mockResolveUrl(mvnRepoDaoMock);

    }

    @After
    public void cleanSecurityContext() {
        TestHelper.logout();
    }

    @Test
    public void should_populate_simple_app_release_env_and_delete_it() throws Exception {
        HsqlDbUtils.DbSnapshot dbSnapshotBefore = hsqlDbUtils.makeDatabaseSnapshot(true);
        String prefix = "simpleTst";
        logger.debug("");
        Collection<PaasUser> testUsers = manageScalability.createPaasUsers(prefix, 1);
        Assert.assertNotNull("no user collection created ?", testUsers);
        PaasUser firstUser = testUsers.iterator().next();
        Assert.assertNotNull("no user created ?", firstUser);
        Assert.assertEquals(new SSOId(prefix + "0"), firstUser.getSsoId());

        boolean createAnEnvironment = true;
        Application newApp = manageScalability.populateSimpleTestPhase(firstUser, createAnEnvironment);
        Assert.assertNotNull("no app created ?", newApp);
        Assert.assertNotNull("no app label created ?", newApp.getLabel());
        logger.info("app successfully created");

        manageScalability.razData();
        HsqlDbUtils.DbSnapshot dbSnapshotAfter = hsqlDbUtils.makeDatabaseSnapshot(true);
        boolean shouldRemoveSequence = true;
        hsqlDbUtils.assertSnapshotEquals("database should retrieve initial state (without data)",
                shouldRemoveSequence,
                dbSnapshotBefore, dbSnapshotAfter);
        hsqlDbUtils.removeSnapshot(Arrays.asList(dbSnapshotBefore, dbSnapshotAfter));
    }


}
