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
package com.francetelecom.clara.cloud.activation.plugin.dbaas;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentRepository;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionSqlDialectEnum;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasVersion;
import com.orange.clara.cloud.dbaas.wsdl.data.CreateDatabaseResponseObject;
import com.orange.clara.cloud.dbaas.wsdl.data.DatabaseUserInfo;
import com.orange.clara.cloud.dbaas.wsdl.data.DatabaseUserInfoWithState;
import com.orange.clara.cloud.dbaas.wsdl.data.JobInfo;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.*;
import com.orange.clara.cloud.dbaas.wsdl.response.DescribeDatabaseResponse;
import com.orange.clara.cloud.dbaas.wsdl.service.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.util.MockUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

/**
 * This class tests behavior of dbaas plugin with 1.0 version and new
 * transaction definition
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/META-INF/spring/mock-dbaas-context.xml")
public class ActivationPluginDBaas20Test {

	@Autowired
	private ActivationPluginDBaasUtils utils;

	@Autowired
	private TechnicalDeploymentRepository technicalDeploymentRepository;

	@Autowired
	private DbaasApiRemote dbaasApiRemote;

	private ActivationPluginDBaas20 plugin = new ActivationPluginDBaas20();

	private DBaasSubscriptionV2 subscription;

	@Mock
	private CreateDatabaseResponseObject createResponse;

	@Mock
	private JobInfo job;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this);

		plugin.setUtils(utils);
		assertTrue(new MockUtil().isMock(dbaasApiRemote));
		reset(dbaasApiRemote);
	}

	/**
	 * Check that if first call to createDatabase failed on DbaaS, nothing is
	 * done on db name on elpaaso side
	 */
	@Test
	@Transactional
	public void databasename_should_not_be_changed_if_creation_order_fail() throws Exception {
		subscription = createDBaasSubscription("fail");

		doThrow(new QuotaExceededFault("no more quota")).when(dbaasApiRemote).createDatabase(anyString(), anyString(), anyInt(), any(ServiceClassWsEnum.class),
				any(EngineWsEnum.class), anyString(), anyListOf(DatabaseUserInfo.class), any(SloWsEnum.class), anyBoolean(), any(UsageWsEnum.class), anyString(),
				any(NetworkZoneWsEnum.class), anyString(), anyString(), anyString(), any(BackupPlanWsEnum.class), anyString(), anyBoolean(), anyString());

		try {
			// transaction
			TaskStatus startStatus = utils.createDatabase(subscription.getId(), "test description");
			// utils.fetchDatabaseDescription(dbaas.getId());
			// transaction
			// DBaasConsumer dbaasConsumer =
			// utils.getDBaasConsumer(dbaas.getDbaasVersion());
			// transaction
			// String dbUUId = utils.getDatabaseUUId(dbaas.getId());
			// no transaction
			// utils.waitForStatus(dbUUId, dbaasConsumer, startStatus);
			fail("createDatabase should have failed");
		} catch (TechnicalException exception) {
			assertTrue(exception.getMessage().contains("no more quota"));
		}

		assertEquals("fail", subscription.getDatabaseUUId());
	}

	/**
	 * Check that if database creation is ok then the db name is changed with
	 * the one created by Dbaas
	 */
	@Test
	@Transactional
	public void databasename_should_be_ok_if_creation_is_ok() throws Exception {
		subscription = createDBaasSubscription("success");

		when(
				dbaasApiRemote.createDatabase(anyString(), anyString(), anyInt(), any(ServiceClassWsEnum.class), any(EngineWsEnum.class), anyString(),
						anyListOf(DatabaseUserInfo.class), any(SloWsEnum.class), anyBoolean(), any(UsageWsEnum.class), anyString(), any(NetworkZoneWsEnum.class), anyString(),
						anyString(), anyString(), any(BackupPlanWsEnum.class), anyString(), anyBoolean(), anyString())).thenReturn(createResponse);
		when(createResponse.getDatabaseUUId()).thenReturn("pg02test");

		when(dbaasApiRemote.getJobState(anyInt())).thenReturn(JobStateWsEnum.PROCESSING, JobStateWsEnum.FINISHED);

		DescribeDatabaseResponse description = new DescribeDatabaseResponse();
		description.setEndPointFQDN("EndPointFQDN");
		description.setEndPointTCPPort("9999");
		description.setDatabaseState(ServiceStateWsEnum.STARTING);
		description.setDatabaseName("pg02test");
		description.setDatabaseName("pg02test");
		GregorianCalendar c = new GregorianCalendar();
		c.setTime(new Date());
		XMLGregorianCalendar creationDate = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
		description.setCreationDate(creationDate);
		DatabaseUserInfoWithState databaseUserInfoWithState = new DatabaseUserInfoWithState();
		DatabaseUserInfo databaseUserInfo = new DatabaseUserInfo();
		databaseUserInfo.setDatabaseUserType(DatabaseUserTypeWsEnum.OWNER);
		databaseUserInfo.setLogin("dbowner");
		databaseUserInfo.setPassword("password");
		databaseUserInfoWithState.setDatabaseUserInfo(databaseUserInfo);
		description.getDatabaseUsers().add(databaseUserInfoWithState);

		when(dbaasApiRemote.describeDatabase(eq("pg02test"))).thenReturn(description);
		// transaction
		TaskStatus startStatus = utils.createDatabase(subscription.getId(), "test description");
		// utils.fetchDatabaseDescription(dbaas.getId());
		// transaction
		// DBaasConsumer dbaasConsumer =
		// utils.getDBaasConsumer(dbaas.getDbaasVersion());since
		// transaction
		// String dbUUId = utils.getDatabaseUUId(dbaas.getId());
		// no transaction
		// utils.waitForStatus(dbUUId, dbaasConsumer, startStatus);

		assertEquals("pg02test", subscription.getDatabaseUUId());
	}

	@Test
	@Transactional
	public void stopping_a_stopped_database_should_not_fail() throws Exception {

		// given subscription is stopped
		when(dbaasApiRemote.stopDatabase(anyString())).thenThrow(DatabaseAlreadyStoppedFault.class);
		subscription = createDBaasSubscription("success");

		// when I stop subscription
        TaskStatus status = plugin.stop(subscription);

		// then it should succeed
		assertTrue(status.hasSucceed());
	}

	@Test
	@Transactional
	public void starting_a_started_database_should_not_fail() throws Exception {

		// given subscription is started
		when(dbaasApiRemote.startDatabase(anyString())).thenThrow(DatabaseAlreadyStartedFault.class);
		subscription = createDBaasSubscription("success");

		// when I start subscription
        TaskStatus status = plugin.start(subscription);

		// then it should succeed
		assertTrue(status.hasSucceed());
	}

	@Test
	@Transactional
	public void deleting_a_removed_database_should_not_fail() throws Exception {

		// given subscription is deleted
		when(dbaasApiRemote.deleteDatabase(anyString())).thenThrow(DatabaseAlreadyDeletedFault.class);
		subscription = createDBaasSubscription("success");

		// when I delete subscription
        TaskStatus status = plugin.delete(subscription);

		// then it should succeed
		assertTrue(status.hasSucceed());
	}

	/**
	 * Insert a DbaasSubscription in the database with a db named as key
	 * parameter
	 * 
	 * @param key
	 *            name of the dbaas db
	 * @return a persisted subscription
	 */
	protected DBaasSubscriptionV2 createDBaasSubscription(String key) {
		TechnicalDeployment td = new TechnicalDeployment("td" + key);

		MavenReference reference = new MavenReference("test" + key, "test" + key, "1.0");

		DBaasSubscriptionV2 dbaasSubscription = new DBaasSubscriptionV2(td);
		dbaasSubscription.setDatabaseUUId(key);
		dbaasSubscription.setSqlDialect(DBaasSubscriptionSqlDialectEnum.POSTGRESQL_DEFAULT);
		dbaasSubscription.changeStorageCapacity(500);
		dbaasSubscription.setDescription("A description");
		dbaasSubscription.setDbaasVersion(DBaasVersion.DBAAS_10.name());
		dbaasSubscription.setInitialPopulationScript(reference);

		technicalDeploymentRepository.save(td);
		return dbaasSubscription;
	}
}
