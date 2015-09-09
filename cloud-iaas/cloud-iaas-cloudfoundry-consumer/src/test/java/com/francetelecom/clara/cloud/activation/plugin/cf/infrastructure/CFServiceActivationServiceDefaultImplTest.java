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

import com.francetelecom.clara.cloud.activation.plugin.cf.domain.CFServiceActivationService;
import com.francetelecom.clara.cloud.activation.plugin.cf.domain.ServiceActivationStatus;
import com.francetelecom.clara.cloud.activation.plugin.cf.infrastructure.CFServiceActivationServiceDefaultImpl.ServiceAlreadyExists;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.cf.SpaceName;
import com.francetelecom.clara.cloud.techmodel.cf.services.managed.ManagedService;
import com.francetelecom.clara.cloud.techmodel.cf.services.userprovided.SimpleUserProvidedService;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CFServiceActivationServiceDefaultImplTest {

	@Mock
	CfAdapter cfAdapter;

	@Test(expected = ServiceAlreadyExists.class)
	public void fail_to_create_and_bind_service_if_service_with_same_name_exists() {
		CFServiceActivationService cFServiceActivationService = new CFServiceActivationServiceDefaultImpl(cfAdapter);

		// given service postgres-joyndb already exi
		Space space = new Space();
		space.activate(new SpaceName("joyn-space"));
		SimpleUserProvidedService postgresService = new SimpleUserProvidedService("postgres-joyndb", "postgres://user:password@hostname:1234/joyndb", space);
		Mockito.doReturn(true).when(cfAdapter).serviceExists("postgres-joyndb", "joyn-space");

		// when I create service postgres-joyndb
		cFServiceActivationService.activate(postgresService);

		// then it should fail
	}

	@Test
	public void should_create_service_if_no_service_with_same_name_exists() {
		CFServiceActivationService cFServiceActivationService = new CFServiceActivationServiceDefaultImpl(cfAdapter);

		// given no service postgres-joyndb exists
		Space space = new Space();
		space.activate(new SpaceName("joyn-space"));
		SimpleUserProvidedService postgresService = new SimpleUserProvidedService("postgres-joyndb", "postgres://user:password@hostname:1234/joyndb", space);
		Mockito.doReturn(false).when(cfAdapter).serviceExists("postgres-joyndb", "joyn-space");

		// when I create service postgres-joyndb
		cFServiceActivationService.activate(postgresService);

		// then it should succeed
	}

	@Test
	public void should_not_fail_delete_service_if_service_does_not_exist() {
		CFServiceActivationService cFServiceActivationService = new CFServiceActivationServiceDefaultImpl(cfAdapter);

		// given service postgres-joyndb does not exist
		Space space = new Space();
		space.activate(new SpaceName("joyn-space"));
		SimpleUserProvidedService postgresService = new SimpleUserProvidedService("postgres-joyndb", "postgres://user:password@hostname:1234/joyndb", space);
		Mockito.doReturn(false).when(cfAdapter).serviceExists("postgres-joyndb", "joyn-space");

		// when I delete service postgres-joyndb
		cFServiceActivationService.delete(postgresService);

		// then it should not fail

	}

	@Test
	public void should_not_delete_managed_service_if_service_has_not_been_activated_yet() throws Exception {
		CFServiceActivationService cFServiceActivationService = new CFServiceActivationServiceDefaultImpl(cfAdapter);

		//given service exists
		//but has not been activated yet
		Space space = new Space();
		ManagedService service = new ManagedService("rabbit", "rabbit", "rabbit", space);

		final ServiceActivationStatus status = cFServiceActivationService.delete(service);

		Mockito.verify(cfAdapter, Mockito.never()).deleteService("rabbit", space.getSpaceName().getValue());
		Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
	}

	@Test
	public void should_not_delete_ups_if_service_has_not_been_activated_yet() throws Exception {
		CFServiceActivationService cFServiceActivationService = new CFServiceActivationServiceDefaultImpl(cfAdapter);

		//given service exists
		//but has not been activated yet
		Space space = new Space();
		SimpleUserProvidedService postgresService = new SimpleUserProvidedService("postgres-joyndb", "postgres://user:password@hostname:1234/joyndb", space);

		final ServiceActivationStatus status = cFServiceActivationService.delete(postgresService);

		Mockito.verify(cfAdapter, Mockito.never()).deleteService("postgres-joyndb",space.getSpaceName().getValue());
		Assertions.assertThat(status.hasSucceed()).isEqualTo(true);
	}

	@Test
	public void should_delete_service_if_service_exists() {
		CFServiceActivationService cFServiceActivationService = new CFServiceActivationServiceDefaultImpl(cfAdapter);

		// given service postgres-joyndb already exists
		Space space = new Space();
		space.activate(new SpaceName("joyn-space"));
		SimpleUserProvidedService postgresService = new SimpleUserProvidedService("postgres-joyndb", "postgres://user:password@hostname:1234/joyndb", space);
		Mockito.doReturn(true).when(cfAdapter).serviceExists("postgres-joyndb", "joyn-space");

		// when I delete service postgres-joyndb
		cFServiceActivationService.delete(postgresService);

		// then it should succeed
	}

	@Test
	public void should_get_service_activation_state() {
		CFServiceActivationService cFServiceActivationService = new CFServiceActivationServiceDefaultImpl(cfAdapter);

		// given service aDatabase activation has succeeded
		Mockito.doReturn(ServiceActivationStatus.ofService("mysql-db", "joyn-space").hasSucceeded()).when(cfAdapter).getServiceInstanceState("mysql-db", "joyn-space");

		// when I get service instance state
		final ServiceActivationStatus status = cFServiceActivationService.getServiceActivationStatus(ServiceActivationStatus.ofService("mysql-db", "joyn-space").isPending("in progress"));

		// then it should return in progress
		Assertions.assertThat(status.hasSucceed()).isTrue();

	}

}
