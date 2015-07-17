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
package com.francetelecom.clara.cloud.techmodel.cf;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.model.*;
import com.francetelecom.clara.cloud.techmodel.AbstractPersistenceTest;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.SimpleUserProvidedService;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * This class test the xaas subscription persist
 * 
 * last changed : $LastChangedDate: 2011-11-03 17:25:06 +0100 (jeu., 03 nov.
 * 2011) $ last author : $Author: dwvd1206 $
 * 
 * @version : $Revision: 11019 $
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TransactionConfiguration(defaultRollback = true)
public class SubscriptionV2PersistenceTest extends AbstractPersistenceTest {

	/**
	 * Simply checks that the listXaasSubscriptionTemplates() properly works
	 * with generics
	 * 
	 * @param td
	 *            TechnicalDeployment
	 */
	private static void assertListSubscriptions(TechnicalDeployment td) {
		// TODO: move this to another test
		Set<XaasSubscription> xaasSubscriptions = td.listXaasSubscriptionTemplates();
		td.listXaasSubscriptionTemplates(null);

		for (XaasSubscription xaasSubscription : xaasSubscriptions) {
			System.out.println(xaasSubscription);
		}
	}

	@Test
	@DirtiesContext
	public void should_save_and_update_app_and_routes() throws Exception {
		Integer tdiId = persistenceTestUtil.executeWithinTransaction(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				TechnicalDeployment td = new TechnicalDeployment("name");
				TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", "1.0.0");

				validateAndPersistModel(tdt);

				/*
				 * second parameter 'td' should be a copy but td cloner is in
				 * daos and we don't want cyclic dependancies
				 */
				TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);

				MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");
				Space space = new Space(td);
				App app = new App(td, space, mavenReference, "foo");
				Route route1 = new Route(new RouteUri("uri1"), null, space, td);
				Route route2 = new Route(new RouteUri("uri2"), null, space, td);
				app.mapRoute(route1);
				app.mapRoute(route2);

				assertListSubscriptions(td);
				TechnicalDeploymentInstance reloadedTdi = validateAndPersistModel(tdi, false);
				return reloadedTdi.getId();
			}
		});

		TechnicalDeploymentInstance reloadedTdi = persistenceTestUtil.reloadEntity(TechnicalDeploymentInstance.class, tdiId, true);

		Set<App> apps = reloadedTdi.getTechnicalDeployment().listXaasSubscriptionTemplates(App.class);
		Assert.assertTrue(apps.size() == 1);
		final App reloadedApp = apps.iterator().next();
		// assert uris are properly saved persistence steps
		Assertions.assertThat(reloadedApp.getRouteURIs()).hasSize(2);
		Assertions.assertThat(reloadedApp.getRouteURIs()).contains("uri1");
		Assertions.assertThat(reloadedApp.getRouteURIs()).contains("uri2");
	}

	@Test
	@DirtiesContext
	public void should_save_bound_services() throws Exception {
		Integer tdiId = persistenceTestUtil.executeWithinTransaction(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				TechnicalDeployment td = new TechnicalDeployment("name");
				TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", "1.0.0");

				validateAndPersistModel(tdt);

				/*
				 * second parameter 'td' should be a copy but td cloner is in
				 * daos and we don't want cyclic dependancies
				 */
				TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);

				MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");

				Space space = new Space(td);

				SimpleUserProvidedService service1 = new SimpleUserProvidedService("serviceName", "serviceUrl", td, space);
				SimpleUserProvidedService service2 = new SimpleUserProvidedService("serviceName", "serviceUrl", td, space);

				App app = new App(td, space, mavenReference, "foo");
				app.bindService(service1);
				app.bindService(service2);

				assertListSubscriptions(td);
				TechnicalDeploymentInstance reloadedTdi = validateAndPersistModel(tdi, false);
				return reloadedTdi.getId();
			}
		});

		TechnicalDeploymentInstance reloadedTdi = persistenceTestUtil.reloadEntity(TechnicalDeploymentInstance.class, tdiId, true);

		Set<App> apps = reloadedTdi.getTechnicalDeployment().listXaasSubscriptionTemplates(App.class);
		Assert.assertTrue(apps.size() == 1);
		final App reloadedApp = apps.iterator().next();

		Assertions.assertThat(reloadedApp.getServiceNames().size()).isEqualTo(2);

	}
	
	@Test
	@DirtiesContext
	public void should_save_apps_using_same_ups() throws Exception {
		Integer tdiId = persistenceTestUtil.executeWithinTransaction(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				TechnicalDeployment td = new TechnicalDeployment("name");
				TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", "1.0.0");

				validateAndPersistModel(tdt);

				/*
				 * second parameter 'td' should be a copy but td cloner is in
				 * daos and we don't want cyclic dependancies
				 */
				TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);

				MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");

				Space space = new Space(td);

				SimpleUserProvidedService service1 = new SimpleUserProvidedService("serviceName", "serviceUrl", td, space);
				SimpleUserProvidedService service2 = new SimpleUserProvidedService("serviceName", "serviceUrl", td, space);

				App app = new App(td, space, mavenReference, "foo");
				app.bindService(service1);
				app.bindService(service2);
				
				App app2 = new App(td, space, mavenReference, "foo2");
				app2.bindService(service1);
				app2.bindService(service2);

				assertListSubscriptions(td);
				TechnicalDeploymentInstance reloadedTdi = validateAndPersistModel(tdi, false);
				return reloadedTdi.getId();
			}
		});

		TechnicalDeploymentInstance reloadedTdi = persistenceTestUtil.reloadEntity(TechnicalDeploymentInstance.class, tdiId, true);

		Set<App> apps = reloadedTdi.getTechnicalDeployment().listXaasSubscriptionTemplates(App.class);
		Assert.assertTrue(apps.size() == 2);
		for (App app : apps) {
			Assertions.assertThat(app.getServiceNames().size()).isEqualTo(2);
		}

	}

	@Test
	@DirtiesContext
	public void should_save_managed_services() throws Exception {
		final Integer tdiId = persistenceTestUtil.executeWithinTransaction(new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				TechnicalDeployment td = new TechnicalDeployment("name");
				TechnicalDeploymentTemplate tdt = new TechnicalDeploymentTemplate(td, DeploymentProfileEnum.DEVELOPMENT, "releaseId", "1.0.0");

				validateAndPersistModel(tdt);

				/*
				 * second parameter 'td' should be a copy but td cloner is in
				 * daos and we don't want cyclic dependancies
				 */
				TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);

				MavenReference mavenReference = MavenReference.fromGavString("foo.groupid:foo.artifactid:foo.version");
				Space space = new Space(td);
				ManagedService rabbitMQService = new ManagedService("rabbitmq","default","rabbitmq-service", space, td);

				App app = new App(td, space, mavenReference, "foo");
				app.bindService(rabbitMQService);

				assertListSubscriptions(td);
				TechnicalDeploymentInstance reloadedTdi = validateAndPersistModel(tdi, false);
				return reloadedTdi.getId();
			}
		});

		TechnicalDeploymentInstance reloadedTdi = persistenceTestUtil.reloadEntity(TechnicalDeploymentInstance.class, tdiId, true);

		Set<App> apps = reloadedTdi.getTechnicalDeployment().listXaasSubscriptionTemplates(App.class);
		Assert.assertTrue(apps.size() == 1);
		final App reloadedApp = apps.iterator().next();

		final List<String> managedServices = reloadedApp.getServiceNames();
		Assertions.assertThat(managedServices.size()).isEqualTo(1);

	}

}
