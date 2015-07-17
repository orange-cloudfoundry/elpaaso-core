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
package com.francetelecom.clara.cloud.techmodel.bindings;

import static org.mockito.Mockito.mock;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.fest.assertions.Assertions;
import org.fest.assertions.MapAssert;
import org.junit.Test;

import com.francetelecom.clara.cloud.commons.TechnicalException;
import com.francetelecom.clara.cloud.model.DependantModelItem;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import com.francetelecom.clara.cloud.techmodel.dbaas.DbAccessInfo;
import com.francetelecom.clara.cloud.techmodel.dbaas.DbOwner;

public class DbaasUserProvidedServiceTest {

    @Test
    public void schedules_dbaas_before_cf_user_provided_service() {
        //given
        DBaasSubscriptionV2 dBaasSubscription = mock(DBaasSubscriptionV2.class);
        TechnicalDeployment td = new TechnicalDeployment("");
		DbaasUserProvidedService cfdBaasService = new DbaasUserProvidedService(td, "postgres-service", DbaasUserProvidedService.UriScheme.mysql, dBaasSubscription, new Space(td));

        //when
        Set<DependantModelItem> dependantModelItems = cfdBaasService.listDepedencies();

        //then
        Assertions.assertThat(dependantModelItems).contains(dBaasSubscription);
    }

    @Test
    public void generates_mysql_service_uri_from_db_connection_details() {
        //given
        DbAccessInfo cnx = new DbAccessInfo("dbaas-farm01", 3128, "frontend024");
        TechnicalDeployment td = new TechnicalDeployment("td-name");
		DBaasSubscriptionV2 dBaasSubscription = new DBaasSubscriptionV2(td);
        dBaasSubscription.changeDbOwner(new DbOwner("admin", "password"));
        dBaasSubscription.activate(cnx);

        DbaasUserProvidedService cfdBaasService = new DbaasUserProvidedService(td, "postgres-service", DbaasUserProvidedService.UriScheme.mysql, dBaasSubscription, new Space(td));

        //when
        String serviceUrl = cfdBaasService.getServiceUrl();

        //then
        Assertions.assertThat(serviceUrl).isEqualTo("mysql://admin:password@dbaas-farm01:3128/frontend024");
    }

    @Test
    public void generated_service_uri_contains_pg_uri_scheme_to_support_spring_cloud_cloudfoundry_data_source_connector() {
        //given
        DbAccessInfo cnx = new DbAccessInfo("dbaas-farm01", 3128, "frontend024");
        TechnicalDeployment td = new TechnicalDeployment("td-name");
		DBaasSubscriptionV2 dBaasSubscription = new DBaasSubscriptionV2(td);
        dBaasSubscription.changeDbOwner(new DbOwner("admin", "password"));
        dBaasSubscription.activate(cnx);

        DbaasUserProvidedService cfdBaasService = new DbaasUserProvidedService(td, "postgres-service", DbaasUserProvidedService.UriScheme.postgres, dBaasSubscription, new Space(td));

        //when
        String serviceUrl = cfdBaasService.getServiceUrl();

        //then
        Assertions.assertThat(serviceUrl).isEqualTo("postgres://admin:password@dbaas-farm01:3128/frontend024");
    }

    @Test(expected = TechnicalException.class)
    public void warns_if_service_urls_requested_before_dbaas_subcription_activated() {
        //given
        TechnicalDeployment td = new TechnicalDeployment("td-name");
		DBaasSubscriptionV2 dBaasSubscription = new DBaasSubscriptionV2(td);
        DbaasUserProvidedService cfdBaasService = new DbaasUserProvidedService(td, "postgres-service", DbaasUserProvidedService.UriScheme.mysql, dBaasSubscription, new Space(td));

        //when
        String serviceUrl = cfdBaasService.getServiceUrl();
    }

	@Test(expected=IllegalArgumentException.class)
	public void full_service_name_should_not_exceed_50_car() throws Exception {
		TechnicalDeployment td = new TechnicalDeployment("td-name");
		DBaasSubscriptionV2 dBaasSubscription = new DBaasSubscriptionV2(td);
        new DbaasUserProvidedService(td, StringUtils.repeat("C", 51), DbaasUserProvidedService.UriScheme.mysql, dBaasSubscription, new Space(td));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void service_name_should_not_be_empty() throws Exception {
		TechnicalDeployment td = new TechnicalDeployment("td-name");
		DBaasSubscriptionV2 dBaasSubscription = new DBaasSubscriptionV2(td);
        new DbaasUserProvidedService(td, "", DbaasUserProvidedService.UriScheme.mysql, dBaasSubscription, new Space(td));
	}
	
	@Test
	public void dbaas_service_credential_should_contain_uri_entry() {
	      //given
        DbAccessInfo cnx = new DbAccessInfo("dbaas-farm01", 3128, "frontend024");
        TechnicalDeployment td = new TechnicalDeployment("td-name");
		DBaasSubscriptionV2 dBaasSubscription = new DBaasSubscriptionV2(td);
        dBaasSubscription.changeDbOwner(new DbOwner("admin", "password"));
        dBaasSubscription.activate(cnx);

        DbaasUserProvidedService cfdBaasService = new DbaasUserProvidedService(td, "postgres-service", DbaasUserProvidedService.UriScheme.postgres, dBaasSubscription, new Space(td));

    	//should contain 1 entry
        Assertions.assertThat(cfdBaasService.getCredentials()).hasSize(6);
		//should contain syslog_drain_url entry and credentials
        Assertions.assertThat(cfdBaasService.getCredentials()).includes(MapAssert.entry("uri", "postgres://admin:password@dbaas-farm01:3128/frontend024"));
        Assertions.assertThat(cfdBaasService.getCredentials()).includes(MapAssert.entry("host", "dbaas-farm01"));
        //Assertions.assertThat(cfdBaasService.getCredentials()).includes(MapAssert.entry("port", "3128"));
        Assertions.assertThat(cfdBaasService.getCredentials()).includes(MapAssert.entry("username", "admin"));
        Assertions.assertThat(cfdBaasService.getCredentials()).includes(MapAssert.entry("password", "password"));
        Assertions.assertThat(cfdBaasService.getCredentials()).includes(MapAssert.entry("schema", "frontend024"));        
        
        
        
	}
}