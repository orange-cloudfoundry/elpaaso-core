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
package com.francetelecom.clara.cloud.test.database;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.mockito.stubbing.OngoingStubbing;

import com.orange.clara.cloud.dbaas.wsdl.data.CreateDatabaseResponseObject;
import com.orange.clara.cloud.dbaas.wsdl.data.DatabaseUserInfo;
import com.orange.clara.cloud.dbaas.wsdl.data.JobMessage;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.BackupPlanWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.EngineWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.JobStateWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.NetworkZoneWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.ServiceClassWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.SloWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.enumeration.UsageWsEnum;
import com.orange.clara.cloud.dbaas.wsdl.response.DescribeDatabaseResponse;
import com.orange.clara.cloud.dbaas.wsdl.service.DbaasApiRemote;

public class DbaasServiceTestUtils {

	public static void configureDbaasStubMockGetJob(DbaasApiRemote dbaasStub, final JobStateWsEnum jobState, final String message) throws Exception {
		JobMessage jobMessage = new JobMessage();
		jobMessage.setMessage(message);
		
		// configure mock
		when(dbaasStub.getJob(anyInt()).getMessages()).thenReturn(Collections.singletonList(jobMessage));
		when(dbaasStub.getJobState(anyInt())).thenReturn(jobState);
	}
	
	
	public static void configureDbaasStubMockGetDatabaseDetails(DbaasApiRemote dbaasStub, final String databaseIp, final String databasePort) throws Exception{
		DescribeDatabaseResponse describeDatabase = new DescribeDatabaseResponse();
		describeDatabase.setEndPointFQDN(databaseIp);
		describeDatabase.setEndPointTCPPort(databasePort);
		
		when(dbaasStub.describeDatabase(anyString())).thenReturn(describeDatabase);

	}
	
	public static Matcher<Throwable> getExceptionCauseMatcher(final Class<?> expectedExceptionClass) {
		return new BaseMatcher<Throwable>() {
			public boolean matches(Object arg0) {
				Throwable e = (Throwable)arg0;
				while( e != null) {
					if (expectedExceptionClass.isInstance(e)) return true;
					e = e.getCause();
				} ;
				return false;
			}
			public void describeTo(Description desc) {
				desc.appendText("a cause "+expectedExceptionClass.getName());
			}
		};
	}


	/**
	 * wrap when(dbaasStub.createDatabase( ...any parameters... ))
	 * @param dbaasStub
	 * @return a stub object representing and a call to the createDatabase method with any parameters
	 * @throws Exception
	 */
	public static OngoingStubbing<CreateDatabaseResponseObject> whenCreateDatabase(DbaasApiRemote dbaasStub) throws Exception {
		return when(dbaasStub.createDatabase(
				anyString(), 
				anyString(),
				anyInt(), 
				any(ServiceClassWsEnum.class), 
				any(EngineWsEnum.class), 
				anyString(), 
				anyListOf(DatabaseUserInfo.class),
				any(SloWsEnum.class), 
				anyBoolean(), 
				any(UsageWsEnum.class), 
				anyString(), 
				any(NetworkZoneWsEnum.class), 
				anyString(), 
				anyString(), 
				anyString(), 
				any(BackupPlanWsEnum.class), 
				anyString(), 
				anyBoolean(), 
				anyString()));
	}


	public static void configureDbaasStubCreateDatabase(DbaasApiRemote dbaasStub, String databaseName, String databaseUUId) throws Exception {
		CreateDatabaseResponseObject createDataResponse = new CreateDatabaseResponseObject();
		
		createDataResponse.setJobId(1);
		createDataResponse.setDatabaseName(databaseName);
		createDataResponse.setDatabaseUUId(databaseUUId);
		
		whenCreateDatabase(dbaasStub).thenReturn(createDataResponse);
		
	}

}
