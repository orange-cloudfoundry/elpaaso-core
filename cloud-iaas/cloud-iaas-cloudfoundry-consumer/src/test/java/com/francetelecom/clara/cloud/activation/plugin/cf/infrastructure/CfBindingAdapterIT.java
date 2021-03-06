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
package com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.SimpleUserProvidedService;
import org.fest.assertions.Assertions;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.MalformedURLException;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class CfBindingAdapterIT {

    @Autowired
    private CfAdapter cfAdapter;

    @Autowired
    private SampleAppProperties sampleAppProperties;

    @Autowired
    private MvnRepoDao mvnRepo;

    @Value("${cf.ccng.space}")
    private String spaceName;

    @After
    public void teardown() throws MalformedURLException {
        // ensure every created resources will be deleted
        if (cfAdapter.appExists("joyn", spaceName)) {
            Space space = new Space();
            space.activate(new SpaceName(spaceName));
            cfAdapter.deleteApp(new App(space, new MavenReference(), "joyn"), spaceName);
        }
        cfAdapter.deleteAllServices(spaceName);
    }

    @Test
    public void should_create_postgres_service() {
        SimpleUserProvidedService postgresService = new SimpleUserProvidedService("postgres-dbname", "postgres://user:password@hostname:1234/dbname", new Space());

        cfAdapter.createService(postgresService, spaceName);

        Assertions.assertThat(cfAdapter.serviceExists(postgresService.getServiceName(), spaceName)).isEqualTo(true);
    }

    @Test
    public void should_delete_postgres_service() {
        SimpleUserProvidedService postgresService = new SimpleUserProvidedService("postgres-dbname", "postgres://user:password@hostname:1234/dbname", new Space());

        cfAdapter.createService(postgresService, spaceName);

        cfAdapter.deleteService(postgresService.getServiceName(), spaceName);

        Assertions.assertThat(cfAdapter.serviceExists(postgresService.getServiceName(), spaceName)).isEqualTo(false);

    }

    @Test
    public void should_create_session_replication_service() {
        ManagedService sessionReplicationService = new ManagedService("redis", "default", "joyn-session-replication", new Space());

        cfAdapter.createService(sessionReplicationService, spaceName);

        Assertions.assertThat(cfAdapter.serviceExists(sessionReplicationService.getServiceInstance(), spaceName)).isEqualTo(true);
    }

    @Test
    public void should_create_rabbitmq_managed_service() {

        ManagedService rabbitMQService = new ManagedService("p-rabbitmq", "standard", "rabbitmq-test", new Space());

        cfAdapter.createService(rabbitMQService, spaceName);

        Assertions.assertThat(cfAdapter.serviceExists(rabbitMQService.getServiceInstance(), spaceName)).isEqualTo(true);
    }


    @Test
    public void should_bind_postgres_service() {
        // given an application joyn pushed on cf
        MavenReference simpleProbe = mvnRepo.resolveUrl(sampleAppProperties.getMavenReference("simple-probe", "jar"));

        Space space = new Space();
        App application = new App(space, simpleProbe, "joyn");
        cfAdapter.createApp(application, spaceName);
        // given a postgres service created on cf
        SimpleUserProvidedService postgresService = new SimpleUserProvidedService("postgres-dbname", "postgres://user:password@hostname:1234/dbname", space);
        cfAdapter.createService(postgresService, spaceName);

        // when I bind postgres service to joyn application
        cfAdapter.bindService("joyn", postgresService.getServiceName(), spaceName);

        //then it should be bound
        Assertions.assertThat(cfAdapter.isServiceBound("joyn", postgresService.getServiceName(), spaceName)).isEqualTo(true);
    }

    @Test
    public void should_delete_space() {

        SpaceName spaceName = new SpaceName("myTestSpace");
        cfAdapter.createSpace(spaceName);

        Assertions.assertThat(cfAdapter.spaceExists(spaceName)).isTrue();

        cfAdapter.deleteSpace(spaceName);

        Assertions.assertThat(cfAdapter.spaceExists(spaceName)).isFalse();
    }

}
