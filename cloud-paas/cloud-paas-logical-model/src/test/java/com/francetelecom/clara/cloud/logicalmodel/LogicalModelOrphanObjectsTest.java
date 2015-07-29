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

import com.francetelecom.clara.cloud.PersistenceTestUtil;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.ConfigLogicalModelCatalog;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 15/02/12
 * Time: 10:18
 * To change this template use File | Settings | File Templates.
 */
@ContextConfiguration(locations = "application-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
public class LogicalModelOrphanObjectsTest {

    /**
     * Creates the default config app with empty constructor used for some specific test
     */
	@Autowired
    ConfigLogicalModelCatalog configLogicalModelCatalog;

    @Autowired
    LogicalDeploymentRepository logicalDeploymentRepository;

    @PersistenceContext
    EntityManager em;

    @Autowired
    PersistenceTestUtil persistenceTestUtil;

    @Test
    public void testAddAssociationToServiceNotInLogicalDeployment() {
        // Given: a sample config LogicalDeployment persisted in database
        LogicalDeployment logicalDeployment = configLogicalModelCatalog.populateLogicalDeployment(new LogicalDeployment());
        // When: I create a new LogicalConfigService without adding to the LogicalDeployment
        LogicalConfigService config = new LogicalConfigService();
        boolean hasFailed = false;
        try {
            // I try to add an association to this service
            logicalDeployment.listProcessingNodes().get(0).addLogicalServiceUsage(config, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);
        } catch (TechnicalException e) {
            hasFailed = true;
        }
        // Then: The request to add the service is rejected
        Assert.assertTrue("add of the service should have raised an exception", hasFailed);
    }

    @Test
    @Transactional
    public void testUUIDRandomCollision() {
        // Given: a sample config LogicalDeployment persisted in db, with a LogicalConfigService with name=id1
        LogicalDeployment logicalDeployment = configLogicalModelCatalog.populateLogicalDeployment(new LogicalDeployment());
        LogicalDeployment logicalDeployment2 = configLogicalModelCatalog.populateLogicalDeployment(new LogicalDeployment());
        boolean persistenceHasFailed = false;
        String persistenceError = "";
        try {
            Field field = LogicalEntity.class.getDeclaredField("name");
            field.setAccessible(true);
            field.set(logicalDeployment, "id1");
            logicalDeploymentRepository.save(logicalDeployment);

            Field field2 = LogicalEntity.class.getDeclaredField("name");
            field2.setAccessible(true);
            field2.set(logicalDeployment2, "id1");
            logicalDeploymentRepository.save(logicalDeployment2);
        } catch (DataIntegrityViolationException e) {
            persistenceHasFailed = true;
            Throwable sqlException = getSQLException(e);
            persistenceError = sqlException == null ? "" : sqlException.getMessage();
        } catch (Exception e){

        }
        // Then: the persistence fails with an error message that gives hint about UUID collision
        // the JPA persistence fails with the unique constraint on the name field
        Assert.assertTrue("merge should have failed", persistenceHasFailed);
        Assert.assertFalse("exception message should not be empty", "".equals(persistenceError));
    }

    /**
     * Recursive function which allows to find the SQLException in the stack
     * @param e the exception which should contain a SQLException in its stack
     * @return the first SQLException in the stack
     */
    private Throwable getSQLException(Throwable e) {
        if (e.equals(e.getCause())) {
            return null;
        }
        return (e instanceof SQLException) ? e : getSQLException(e.getCause());
    }

    @Test
    public void testDeletedServicesCantBeRetrieved() {
        // Given: a sample config LogicalDeployment in database
        LogicalDeployment logicalDeployment = configLogicalModelCatalog.populateLogicalDeployment(new LogicalDeployment());
        logicalDeploymentRepository.save(logicalDeployment);
        // When: I remove all services and exec nodes from the LogicalDeployment
        logicalDeployment.removeAllProcessingNodes();
        try {
            logicalDeployment.removeAllLogicalService();
        } catch (BusinessException e) {
        }
        // I try to persist the LogicalDeployment
        logicalDeploymentRepository.save(logicalDeployment);
        final LogicalDeployment finalLd = logicalDeployment;
        // Then: the original JeeProcessing and LogicalService can not be retrieved from the database
        persistenceTestUtil.executeWithinTransaction(new Runnable() {
            @Override
            public void run() {
                TypedQuery qNodes = em.createQuery("SELECT DISTINCT n FROM LogicalDeployment l JOIN l.processingNodes n WHERE l.id = :id", ProcessingNode.class);
                qNodes.setParameter("id", finalLd.getId());
                List<ProcessingNode> nodesFromBase = qNodes.getResultList();
                Assert.assertTrue("deleted execution nodes should not be found in the database", nodesFromBase.isEmpty());

                TypedQuery qServices = em.createQuery("SELECT DISTINCT s FROM LogicalDeployment l JOIN l.logicalServices s WHERE l.id = :id", LogicalService.class);
                qServices.setParameter("id", finalLd.getId());
                List<LogicalService> servicesFromBase = qServices.getResultList();
                Assert.assertTrue("logical deployment from base should not have services", servicesFromBase.isEmpty());
            }
        });
    }

    @Test
    public void testNodeNotAddedToLogicalDeploymentCantBeRetrieved() {
        // Given: an empty LogicalDeployment
        LogicalDeployment logicalDeployment = new LogicalDeployment();
        // When: I instantiate a JeeProcessing without adding it to the LogicalDeployment, and I persist the LogicalDeployment
        ProcessingNode node = new JeeProcessing();
        logicalDeploymentRepository.save(logicalDeployment);
        // Then: the JeeProcessing can't be retrieved from the database by its name
        // Then: the original JeeProcessing and LogicalService can not be retrieved from the database
        final ProcessingNode finalNode = node;
        persistenceTestUtil.executeWithinTransaction(new Runnable() {
            @Override
            public void run() {
                TypedQuery q = em.createQuery("SELECT n FROM JeeProcessing n WHERE n.name = :name", ProcessingNode.class);
                q.setParameter("name", finalNode.getName());
                List<ProcessingNode> nodesFromBase = q.getResultList();
                Assert.assertTrue("there should be no node retrieved from the base", nodesFromBase.isEmpty());
                // the id of the JeeProcessing has not been updated
                Assert.assertEquals("the node should have its id equal to 0", 0, finalNode.getId());
            }
        });
    }

    @Test
    public void testNodeAddedThenRemovedToLogicalDeploymentCantBeRetrieved() {
        // Given: an empty LogicalDeployment
        LogicalDeployment logicalDeployment = new LogicalDeployment();
        // When: I instantiate a JeeProcessing and add it to the LogicalDeployment
        ProcessingNode node1 = new JeeProcessing();
        node1.setLabel("node1");
        logicalDeployment.addExecutionNode(node1);
        ProcessingNode node2 = new JeeProcessing();
        node2.setLabel("node2");
        logicalDeployment.addExecutionNode(node2);
        // I remove the JeeProcessing from the LogicalDeployment
        logicalDeploymentRepository.save(logicalDeployment);
        logicalDeployment.removeProcessingNode(node1);
        // I persist the LogicalDeployment
        logicalDeploymentRepository.save(logicalDeployment);
        final ProcessingNode finalNode1 = node1;
        final ProcessingNode finalNode2 = node2;
        persistenceTestUtil.executeWithinTransaction(new Runnable() {
            @Override
            public void run() {
                TypedQuery q = em.createQuery("SELECT n FROM JeeProcessing n WHERE n.name = :name", ProcessingNode.class);
                // Then: the removed JeeProcessing can not be retrieved from the database by its name node1
                q.setParameter("name", finalNode1.getName());
                List<ProcessingNode> nodesFromBase = q.getResultList();
                Assert.assertTrue("there should be no node1 retrieved from the base", nodesFromBase.isEmpty());
                // Then: the non-removed JeeProcessing can still be retrieved from the database by its name node2
                q.setParameter("name", finalNode2.getName());
                nodesFromBase = q.getResultList();
                Assert.assertTrue("there should be only node2 retrieved from the base", nodesFromBase.get(0).equals(finalNode2));
            }
        });
    }
}
