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
package com.francetelecom.clara.cloud.techmodel.dbaas;

import org.fest.assertions.Assertions;
import org.junit.Test;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;

public class DBaasSubscriptionV2Test {

	@Test(expected = IllegalArgumentException.class)
	public void fail_to_activate_dbaas_subscription_with_no_connection_details() {
		// given dbaas subscription
		DBaasSubscriptionV2 dBaasSubscriptionV2 = new DBaasSubscriptionV2(new TechnicalDeployment("name"));

		// when I activate dbaas subscription without connection details
		dBaasSubscriptionV2.activate(null);

		// then it should fail
	}

	@Test
	public void default_db_owner_is_scott_and_default_password_is_tiger() {
		// given dbaas subscription
		DBaasSubscriptionV2 dBaasSubscriptionV2 = new DBaasSubscriptionV2(new TechnicalDeployment("name"));
		
		// when I activate dbaas subscription with connection details
		dBaasSubscriptionV2.activate(new DbAccessInfo("hostname", 1000, "dbname"));
		
		Assertions.assertThat(dBaasSubscriptionV2.getUserName()).isEqualTo("scott");
		Assertions.assertThat(dBaasSubscriptionV2.getUserPassword()).isEqualTo("TigerW11d");
		
	}

	@Test
	public void should_get_connection_details_when_dbaas_subscription_is_activated() {
		// given dbaas subscription
		DBaasSubscriptionV2 dBaasSubscriptionV2 = new DBaasSubscriptionV2(new TechnicalDeployment("name"));
		dBaasSubscriptionV2.changeDbOwner(new DbOwner("admin", "password"));
		
		// when I activate dbaas subscription with connection details
		dBaasSubscriptionV2.activate(new DbAccessInfo("hostname", 1000, "dbname"));

		//then subscription should be activated
		Assertions.assertThat(dBaasSubscriptionV2.isActivated()).isTrue();
		
		// then I should get connection details
		Assertions.assertThat(dBaasSubscriptionV2.getHostname()).isEqualTo("hostname");
		Assertions.assertThat(dBaasSubscriptionV2.getPort()).isEqualTo(1000);
		Assertions.assertThat(dBaasSubscriptionV2.getUserName()).isEqualTo("admin");
		Assertions.assertThat(dBaasSubscriptionV2.getUserPassword()).isEqualTo("password");
		Assertions.assertThat(dBaasSubscriptionV2.getDbname()).isEqualTo("dbname");

	}
	
	@Test(expected = TechnicalException.class)
	public void fail_to_change_dbowner_if_dbaas_subscription_is_activated() {
		// given dbaas subscription
		DBaasSubscriptionV2 dBaasSubscriptionV2 = new DBaasSubscriptionV2(new TechnicalDeployment("name"));
		
		// when I activate dbaas subscription with connection details
		dBaasSubscriptionV2.activate(new DbAccessInfo("hostname", 1000, "dbname"));
		
		dBaasSubscriptionV2.changeDbOwner(new DbOwner("admin", "password"));
	}

	

	@Test(expected = TechnicalException.class)
	public void fail_to_get_db_hostname_if_dbaas_subscription_is_not_activated() {
		// given dbaas subscription has not been activated
		DbConnectionDetails dBaasSubscriptionV2 = new DBaasSubscriptionV2(new TechnicalDeployment("name"));

		// when I get db hostname
		dBaasSubscriptionV2.getHostname();

		// then it should fail
	}

	@Test(expected = TechnicalException.class)
	public void fail_to_get_db_port_if_dbaas_subscription_is_not_activated() {
		// given dbaas subscription has not been activated
		DbConnectionDetails dBaasSubscriptionV2 = new DBaasSubscriptionV2(new TechnicalDeployment("name"));

		// when I get db hostname
		dBaasSubscriptionV2.getPort();

		// then it should fail
	}

	@Test(expected = TechnicalException.class)
	public void fail_to_get_db_name_if_dbaas_subscription_is_not_activated() {
		// given dbaas subscription has not been activated
		DbConnectionDetails dBaasSubscriptionV2 = new DBaasSubscriptionV2(new TechnicalDeployment("name"));

		// when I get db hostname
		dBaasSubscriptionV2.getDbname();

		// then it should fail
	}

	@Test
	public void can_change_storage_capacity_to_value_greater_or_equals_1MB() throws Exception {
		// given dbaas subscription has not been activated
		DBaasSubscriptionV2 dBaasSubscriptionV2 = new DBaasSubscriptionV2(new TechnicalDeployment("name"));
		dBaasSubscriptionV2.changeStorageCapacity(1);
		dBaasSubscriptionV2.changeStorageCapacity(2);
	}
	
	@Test(expected=TechnicalException.class)
	public void fail_to_change_storage_capacity_to_value_less_than_1MB() throws Exception {
		// given dbaas subscription has not been activated
		DBaasSubscriptionV2 dBaasSubscriptionV2 = new DBaasSubscriptionV2(new TechnicalDeployment("name"));
		dBaasSubscriptionV2.changeStorageCapacity(0);
	}
	
}
