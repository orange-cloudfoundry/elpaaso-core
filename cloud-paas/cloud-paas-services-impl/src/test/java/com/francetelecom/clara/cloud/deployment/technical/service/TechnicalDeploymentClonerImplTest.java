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
package com.francetelecom.clara.cloud.deployment.technical.service;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.coremodel.MiddlewareProfile;
import com.francetelecom.clara.cloud.model.*;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Route;
import com.francetelecom.clara.cloud.techmodel.cf.RouteUri;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.AbstractUserProvidedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.SimpleUserProvidedService;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/com/francetelecom/clara/cloud/services/application-context.xml" })
public class TechnicalDeploymentClonerImplTest {

	private static Logger logger = LoggerFactory.getLogger(TechnicalDeploymentClonerImplTest.class.getName());

	@PersistenceContext
	EntityManager em;

	@Autowired
	@Qualifier("technicalDeploymentCloner")
	private TechnicalDeploymentCloner cloner;

	@Test
	public void testTransientClone() {
		TechnicalDeploymentTestFactory technicalDeploymentTestFactory = new TechnicalDeploymentTestFactory();
		TechnicalDeployment td = technicalDeploymentTestFactory.createWicketJpaTD("test2", "foo.groupid:foo.artifactid:foo.version");

		TechnicalDeployment copy = this.cloner.deepCopy(td);
		// logger.debug("cloned td : \n"+copy.dumpXml());

	}

    /**
     * Specific for CfSubcriptions where there was doubts about loss in clone. Should probably be moved somewhere else
     */
    @Test
    public void testCfSubcriptionClone() {
        TechnicalDeployment td = new TechnicalDeployment("dummy");
        TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", MiddlewareProfile.DEFAULT_PROFILE);
        TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
        tdi.setReferenceKey("key");
        tdi.getReferenceKey();

		MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");

		Space space = new Space(td);
        
        App app = new App(td, space, mavenReference, "foo");
		app.bindService(new SimpleUserProvidedService("frontend-db", "http://localhost", td, space));
		app.bindService(new ManagedService("rabbitmq", "default", "myservice", space, td));
        Route route1 = new Route(new RouteUri("uri1"), null, space, td);
        Route route2 = new Route(new RouteUri("uri2"), null, space, td);
        app.mapRoute(route1);
        app.mapRoute(route2);
       
        TechnicalDeployment clonedTd = this.cloner.deepCopy(tdi.getTechnicalDeployment());

        Set<App> apps = clonedTd.listXaasSubscriptionTemplates(App.class);
        Assert.assertTrue(apps.size() == 1);
        App reloadedApp = apps.iterator().next();
        Assertions.assertThat(reloadedApp.getRouteURIs()).containsOnly("uri1","uri2");
        
        Set<Space> spaces = clonedTd.listXaasSubscriptionTemplates(Space.class);
        Assert.assertTrue(spaces.size() == 1);
        
        Set<ManagedService> managedServices = clonedTd.listXaasSubscriptionTemplates(ManagedService.class);
        Assert.assertTrue(managedServices.size() == 1);
        
        Set<AbstractUserProvidedService> userProvidedServices = clonedTd.listXaasSubscriptionTemplates(AbstractUserProvidedService.class);
        Assert.assertTrue(userProvidedServices.size() == 1);

    }

	@Test
	@Transactional
	@Rollback(false)
	public void testPersistantToTransientClone() {
		TechnicalDeploymentTestFactory technicalDeploymentTestFactory = new TechnicalDeploymentTestFactory();
		TechnicalDeployment td = technicalDeploymentTestFactory.createWicketJpaTD("test2", "foo.groupid:foo.artifactid:foo.version");

		logger.info("Persisting original TD");
		em.persist(td);
		em.flush();

		TechnicalDeployment copy = this.cloner.deepCopy(td);

		logger.info("Successfully copied original TD");

		// FIXME : wont work if td has been previously persisted

		em.persist(copy);
		em.flush();

		logger.info("Successfully persist and flush copy TD");

	}

}
