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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"application-context.xml"})
@DirtiesContext
public class LogicalDeploymentRepositoryTest {

    @Autowired
    private LogicalDeploymentRepository logicalDeploymentRepository;

    @Test
    @Transactional
    @Rollback(true)
    public void testPersist() {
        // test setup
        LogicalDeployment toBePersited = new LogicalDeployment();
        // test run
        logicalDeploymentRepository.save(toBePersited);
        // assertions
        Assert.assertNotNull("entity does not exist", logicalDeploymentRepository.findOne(toBePersited.getId()));
        logicalDeploymentRepository.flush();
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testPersistDuplicateLabel() {
        // test setup
        LogicalDeployment toBePersited1 = new LogicalDeployment();
        toBePersited1.setLabel("aLabel");
        System.out.println("toBePersited1" + toBePersited1.getId());
        LogicalDeployment toBePersited2 = new LogicalDeployment();
        toBePersited2.setLabel("aLabel");
        System.out.println("toBePersited2" + toBePersited2.getId());
        // test run
        logicalDeploymentRepository.save(toBePersited1);
        logicalDeploymentRepository.save(toBePersited2);
        logicalDeploymentRepository.flush();
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testRemove() {
        // test setup
        LogicalDeployment toBePersited = new LogicalDeployment();
        logicalDeploymentRepository.save(toBePersited);
        Assert.assertNotNull("entity does not exist", logicalDeploymentRepository.findOne(toBePersited.getId()));
        // test run
        logicalDeploymentRepository.delete(toBePersited);
        // assertions
        Assert.assertNull("entity should not exist", logicalDeploymentRepository.findOne(toBePersited.getId()));
        logicalDeploymentRepository.flush();
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testFind() {
        // test setup
        LogicalDeployment toBePersited = new LogicalDeployment();
        logicalDeploymentRepository.save(toBePersited);
        // test run
        LogicalDeployment entity = logicalDeploymentRepository.findOne(toBePersited.getId());
        // assertions
        Assert.assertNotNull("entity does not exist", entity);
//		Assert.assertEquals("ld-test", entity.getLabel());

        //Assert.assertTrue("expected different instances to be read and returned", entity != toBePersited);
        Assert.assertEquals(toBePersited.getName(), entity.getName());
        Assert.assertEquals(toBePersited, entity);

        logicalDeploymentRepository.flush();
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testMerge() {
        // test setup
        LogicalDeployment persisted = new LogicalDeployment();
        LogicalWebGUIService logicalService_persisted = new LogicalWebGUIService("LS1-test", persisted);
        logicalService_persisted.setContextRoot(new ContextRoot("/"));
        ProcessingNode jeeProcessing_persisted = new JeeProcessing("LEN1-test", persisted);

        logicalDeploymentRepository.save(persisted);

        LogicalDeployment logicalDeployment = logicalDeploymentRepository.findOne(persisted.getId());

        LogicalWebGUIService logicalService1 = new LogicalWebGUIService("LS1-XXX", logicalDeployment);
        logicalService1.setContextRoot(new ContextRoot("/"));
        LogicalWebGUIService logicalService2 = new LogicalWebGUIService("LS2-XXX", logicalDeployment);
        logicalService2.setContextRoot(new ContextRoot("/"));
        ProcessingNode jeeProcessing1 = new JeeProcessing("LEN1-XXX", logicalDeployment);
        ProcessingNode jeeProcessing2 = new JeeProcessing("LEN2-XXX", logicalDeployment);

        logicalDeploymentRepository.save(logicalDeployment);
        logicalDeploymentRepository.flush();
        // test run
        LogicalDeployment entity = logicalDeploymentRepository.findOne(persisted.getId());
        // assertions
        Assert.assertNotNull("entity does not exist", entity);
        Assert.assertEquals(logicalDeployment.getName(), entity.getName());
        Assert.assertEquals(3, entity.listLogicalServices().size());
        Assert.assertEquals(3, entity.listProcessingNodes().size());
    }

    @Test
    @Transactional
    @Rollback(true)
    public void testFindAll() {
        // test setup
        logicalDeploymentRepository.save(new LogicalDeployment());
        logicalDeploymentRepository.save(new LogicalDeployment());
        logicalDeploymentRepository.save(new LogicalDeployment());
        // test run
        List<LogicalDeployment> entities = logicalDeploymentRepository.findAll();
        // assertions
        Assert.assertNotNull("entities should not be null", entities);
        Assert.assertEquals("there should be 3 entities", 3, entities.size());
        logicalDeploymentRepository.flush();
    }



}
