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
package com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure;

import com.francetelecom.clara.cloud.activation.plugin.dbaas.DBaasConsumer;
import com.francetelecom.clara.cloud.activation.plugin.dbaas.DBaasServiceStateEnum;
import com.francetelecom.clara.cloud.activation.plugin.dbaas.TestUtils;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionSqlDialectEnum;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.francetelecom.clara.cloud.techmodel.dbaas.DbOwner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
public abstract class DBaasConsumerTest {
	
	@Autowired
	protected DBaasConsumer dbaasConsumer;
	
	private DBaasSubscriptionSqlDialectEnum sqlVersion;
	
	private DBaasSubscriptionV2 dbaasSubscription;
	
	protected DBaasSubscriptionV2 createDBaasSubscription() {
		TechnicalDeployment td = new TechnicalDeployment("td");
		dbaasSubscription = new DBaasSubscriptionV2(td);
		dbaasSubscription.setDatabaseUUId("bddtest");
		dbaasSubscription.setSqlDialect(getSqlVersion());
		dbaasSubscription.changeStorageCapacity(5);
		dbaasSubscription.setDescription("A description");
		dbaasSubscription.changeDbOwner(new DbOwner("myuser", "MyPaas820"));
		return dbaasSubscription;
	}
	
	/**
	 * GIVEN a Mocked DBaas consumer implementation, a DBaasSubscription
	 * WHEN I run the complete lifecycle (create, stop, start, stop, delete)
	 * THEN giveCurrentStatus() must return a coherent state
	 */
	@Test
	public void normalLifecycle() {
		DBaasSubscriptionV2 dbaasSubscription = createDBaasSubscription();
		
		TaskStatus status = dbaasConsumer.createDatabase(dbaasSubscription, "DBaasConsumerTest.normalLifecycle()");
		TestUtils.waitForTaskCompletion(dbaasConsumer, status, dbaasSubscription, 200);
		
		// Refresh subscription with real information (database name can be changed for example)
		dbaasConsumer.fetchDatabaseDescription(dbaasSubscription.getDatabaseUUId());
		
		Assert.assertEquals("DB status after creation", DBaasServiceStateEnum.ACTIVE, dbaasConsumer.getStatus(dbaasSubscription.getDatabaseUUId()));

		TestUtils.waitForTaskCompletion(dbaasConsumer, dbaasConsumer.stopDatabase(dbaasSubscription), dbaasSubscription, 200);

		Assert.assertEquals("DB status after stop", DBaasServiceStateEnum.STOPPED, dbaasConsumer.getStatus(dbaasSubscription.getDatabaseUUId()));

		TestUtils.waitForTaskCompletion(dbaasConsumer, dbaasConsumer.startDatabase(dbaasSubscription), dbaasSubscription, 200);

		Assert.assertEquals("DB status after start", DBaasServiceStateEnum.ACTIVE, dbaasConsumer.getStatus(dbaasSubscription.getDatabaseUUId()));

		TestUtils.waitForTaskCompletion(dbaasConsumer, dbaasConsumer.stopDatabase(dbaasSubscription), dbaasSubscription, 200);

		Assert.assertEquals("DB status after 2nd stop", DBaasServiceStateEnum.STOPPED, dbaasConsumer.getStatus(dbaasSubscription.getDatabaseUUId()));

		TestUtils.waitForTaskCompletion(dbaasConsumer, dbaasConsumer.deleteDatabase(dbaasSubscription), dbaasSubscription, 200);

		Assert.assertEquals("DB status after deletion", DBaasServiceStateEnum.DELETED, dbaasConsumer.getStatus(dbaasSubscription.getDatabaseUUId()));
	}
	
	/**
	 * GIVEN a Mocked DBaas consumer implementation, a DBaasSubscription that represent an active database
	 * WHEN I delete the database multiple times no error is returned 
	 * THEN giveCurrentStatus() must return DELETED without error
	 */
	public void deleteIdemPotent() {
		DBaasSubscriptionV2 dbaasSubscription = createDBaasSubscription();

		TaskStatus status1 = dbaasConsumer.deleteDatabase(dbaasSubscription);
		TaskStatus status2 = dbaasConsumer.deleteDatabase(dbaasSubscription);
		
		TestUtils.waitForTaskCompletion(dbaasConsumer, status1, dbaasSubscription, 200);
		TestUtils.waitForTaskCompletion(dbaasConsumer, status2, dbaasSubscription, 200);

		Assert.assertEquals("DB status after 1st deletion", DBaasServiceStateEnum.DELETED, dbaasConsumer.getStatus(dbaasSubscription.getDatabaseUUId()));

		TestUtils.waitForTaskCompletion(dbaasConsumer, dbaasConsumer.deleteDatabase(dbaasSubscription), dbaasSubscription, 200);

		Assert.assertEquals("DB status after 2nd deletion", DBaasServiceStateEnum.DELETED, dbaasConsumer.getStatus(dbaasSubscription.getDatabaseUUId()));
	}

	public DBaasSubscriptionSqlDialectEnum getSqlVersion() {
		return sqlVersion;
	}

	public void setSqlVersion(DBaasSubscriptionSqlDialectEnum sqlVersion) {
		this.sqlVersion = sqlVersion;
	}

	public DBaasSubscriptionV2 getDbaasSubscription() {
		return dbaasSubscription;
	}

	public void setDbaasSubscription(DBaasSubscriptionV2 dbaasSubscription) {
		this.dbaasSubscription = dbaasSubscription;
	}
	
	
}
