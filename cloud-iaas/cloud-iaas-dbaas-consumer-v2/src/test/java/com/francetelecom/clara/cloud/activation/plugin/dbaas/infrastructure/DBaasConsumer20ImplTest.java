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

import static org.fest.assertions.Assertions.assertThat;
import static org.fest.assertions.Fail.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.activation.plugin.dbaas.infrastructure.DBaasConsumer20Impl;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatus;
import com.francetelecom.clara.cloud.commons.tasks.TaskStatusEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.orange.clara.cloud.dbaas.wsdl.service.DbaasApiRemote;

/**
 * DBaasConsumer10ImplTest
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
@RunWith(MockitoJUnitRunner.class)
public class DBaasConsumer20ImplTest {
    private static Logger logger = LoggerFactory.getLogger(DBaasConsumer20ImplTest.class);
    
    private static DbaasApiRemote dbaasClientToNowhere;
    
    @BeforeClass
    public static void init(){
    	JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
    	factory.setAddress("http://nowhere");
    	factory.setServiceClass(DbaasApiRemote.class);
    	
    	dbaasClientToNowhere = (DbaasApiRemote) factory.create();
    }
    
    @Spy
    private DBaasConsumer20Impl dbaasConsumer = new DBaasConsumer20Impl();
    
    @Mock
    private DbaasApiRemote dbaasClientMock;

    @Test
    public void test_give_current_task_status_should_throw_technical_exception_when_getjob_fail() throws Exception {
        // GIVEN
        String dbaasVersion = "dbaas 1.0";
        Integer tokenId = 12345;

        String sampleRootCauseMessage = "test runtime exception";
        RuntimeException rootCause = new RuntimeException(sampleRootCauseMessage);
        doThrow(new RuntimeException("intermediate error", rootCause))
                .when(dbaasClientMock).getJobState(anyInt());
        doNothing()
                .when(dbaasConsumer).updateTimeout();
        doReturn(dbaasVersion)
                .when(dbaasConsumer).getDBaaSVersion();

        dbaasConsumer.setDbaasStub(dbaasClientMock);
        DBaasConsumer20Impl.PluginTaskStatus taskStatusStub = new DBaasConsumer20Impl.PluginTaskStatus(3333);
        taskStatusStub.setTokenId(tokenId);

        // WHEN
        try {
            dbaasConsumer.giveCurrentTaskStatus(taskStatusStub);
        // THEN
            fail("an exception should be thrown here");
        } catch (TechnicalException technicalException) {
            String message = technicalException.getMessage();
            logger.info("Exception message was:{}", message);
            assertThat(message)
                    .as("an error message is needed on dbaas getTask call")
                    .isNotEmpty();
            assertThat(message)
                    .as("error message should display dbaas version")
                    .contains(dbaasVersion);
            assertThat(message)
                    .as("error message should display error root cause")
                    .contains(sampleRootCauseMessage);
        }
    }
    
    @Test
    public void test_dbaas_client_should_use_consumer_timeout_values_if_specified() throws Exception {
		dbaasConsumer.setDbaasStub(dbaasClientToNowhere);
    	dbaasConsumer.setTimeout(15000L);
		
    	Client client = ClientProxy.getClient(dbaasClientToNowhere);
		HTTPConduit conduit = (HTTPConduit) client.getConduit();
		assertEquals(15000L, conduit.getClient().getConnectionTimeout());
		assertEquals(15000L, conduit.getClient().getReceiveTimeout());
	}
    
    @Test
    public void test_dbaas_client_should_use_default_cxf_timeout_values_if_no_values_specified() throws Exception {
		dbaasConsumer.setDbaasStub(dbaasClientToNowhere);
		
    	Client client = ClientProxy.getClient(dbaasClientToNowhere);
		HTTPConduit conduit = (HTTPConduit) client.getConduit();
		assertEquals("connection timeout should be cxf default values. Have it changed ?", 30000L, conduit.getClient().getConnectionTimeout());
		assertEquals("receive timeout should be cxf default values. Have it changed ?", 60000L, conduit.getClient().getReceiveTimeout());
	}

	@Test
	public void should_stop_database_when_database_uuid_is_null() throws Exception {
		TechnicalDeployment td = new TechnicalDeployment("Dummy TD for dbaas UT");
		DBaasSubscriptionV2 dbaasSubscription = new DBaasSubscriptionV2(td);
		TaskStatus stopTask = dbaasConsumer.stopDatabase(dbaasSubscription);
		assertEquals(TaskStatusEnum.FINISHED_OK, stopTask.getTaskStatus());
	}
    
	@Test
	public void should_delete_database_when_database_uuid_is_null() throws Exception {
		TechnicalDeployment td = new TechnicalDeployment("Dummy TD for dbaas UT");
		DBaasSubscriptionV2 dbaasSubscription = new DBaasSubscriptionV2(td);
		TaskStatus deleteStatus = dbaasConsumer.deleteDatabase(dbaasSubscription);
		assertEquals(TaskStatusEnum.FINISHED_OK, deleteStatus.getTaskStatus());
	}

}
