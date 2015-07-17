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
package com.francetelecom.clara.cloud.paas.projection.cf;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SpringooLogicalModelCatalog;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.paas.projection.security.CryptService;
import com.francetelecom.clara.cloud.techmodel.bindings.DbaasUserProvidedService;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionSqlDialectEnum;
import com.francetelecom.clara.cloud.techmodel.dbaas.DBaasSubscriptionV2;
import org.fest.assertions.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import static org.fest.assertions.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RelationalServiceProjectionRuleTest {

    @Mock
    private CryptService cryptService;

    @Mock
    private MvnRepoDao mvnRepoDao;

    private static final String DBAASVERSION = "1";

    @Test
    public void should_generate_standalone_pg_dbaassubscription() throws Exception {

        MavenReference sqlScript = new MavenReference("groupId", "artifactId", "version");
        sqlScript.setAccessUrl(new URL("http://localhost"));

        Mockito.when(mvnRepoDao.resolveUrl(sqlScript)).thenReturn(sqlScript);
        Mockito.when(cryptService.generateRandomPassword()).thenReturn("azerty");

        RelationalServiceProjectionRule relationalServiceProjectionRule = new RelationalServiceProjectionRule(cryptService, mvnRepoDao, DBAASVERSION);
        // given lrs
        LogicalRelationalService lrs = new LogicalRelationalService();
        lrs.setServiceName("postgres-frontend");
        lrs.setCapacityMo(1000);
        lrs.setAvailable(true);
        lrs.setInitialPopulationScript(sqlScript);
        lrs.setMaxConnection(9999);
        lrs.setSqlVersion(LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT);
        LogicalDeployment ld = new LogicalDeployment();
        ld.addLogicalService(lrs);
        // given td
        TechnicalDeployment td = new TechnicalDeployment("name");
        Space space = new Space(td);
        // when I generate
        relationalServiceProjectionRule.apply(ld, td, new DummyProjectionContext(space));

        // then there should be 1 dbaasSubscription
        Set<DBaasSubscriptionV2> dbaasSubscriptions = td.listXaasSubscriptionTemplates(DBaasSubscriptionV2.class);
        Assert.assertEquals(dbaasSubscriptions.size(), 1);

        DBaasSubscriptionV2 dBaasSubscription = dbaasSubscriptions.iterator().next();

        Assert.assertEquals(DBaasSubscriptionSqlDialectEnum.POSTGRESQL_DEFAULT, dBaasSubscription.getSqlDialect());
        Assert.assertEquals(sqlScript, dBaasSubscription.getInitialPopulationScript());

        Assert.assertEquals("azerty", dBaasSubscription.getUserPassword());

        Assert.assertEquals("dbowner", dBaasSubscription.getUserName());
        Assert.assertEquals(DBAASVERSION, dBaasSubscription.getDbaasVersion());
    }

    @Test
    public void binds_dbaassubcription_into_cfapp_through_cfdbaasservice_for_cfjavaprocessing_and_spring_reconfig() throws MalformedURLException {
        // given
        MavenReference sqlScript = new MavenReference("groupId", "artifactId", "version");
        sqlScript.setAccessUrl(new URL("http://localhost"));

        Mockito.when(mvnRepoDao.resolveUrl(sqlScript)).thenReturn(sqlScript);
        Mockito.when(cryptService.generateRandomPassword()).thenReturn("azerty");

        RelationalServiceProjectionRule relationalServiceProjectionRule = new RelationalServiceProjectionRule(cryptService, mvnRepoDao, DBAASVERSION);

        LogicalDeployment logicalDeployment = new LogicalDeployment();
        SpringooLogicalModelCatalog logicalModelCatalog = new SpringooLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        LogicalRelationalService relationalService = logicalDeployment.listLogicalServices(LogicalRelationalService.class).iterator().next();
        relationalService.setServiceName("postgres-frontend");
        TechnicalDeployment td = new TechnicalDeployment("name");

        Space space = new Space(td);

        relationalServiceProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));
        Set<DbaasUserProvidedService> generatedServices = td.listXaasSubscriptionTemplates(DbaasUserProvidedService.class);
        Assertions.assertThat(generatedServices.size()).isEqualTo(1);

        // when

        // then
        DbaasUserProvidedService cfdBaasService = generatedServices.iterator().next();
        assertThat(cfdBaasService.getServiceName()).isEqualTo("postgres-frontend");
        assertThat(cfdBaasService.getUriScheme()).isEqualTo(DbaasUserProvidedService.UriScheme.postgres);

        //simulate app generation
        App app = new App(td, space, new MavenReference(), "joyn");
        app.setLogicalModelId(logicalDeployment.findProcessingNode("Springoo_Jee_processing").getName());

        AssociationProjectionRule associationProjectionRule = new DefaultServiceBindingProjectionRule();

        associationProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        //then a relational services should be bound to the app
        Assertions.assertThat(app.getServiceNames()).hasSize(1);
        Assertions.assertThat(app.getServiceNames()).containsOnly("postgres-frontend");
    }

    @Test
    public void maps_lrdb_dialects_into_spring_cloud_uri_schemes() {
        RelationalServiceProjectionRule relationalServiceProjectionRule = new RelationalServiceProjectionRule();

        // when
        DbaasUserProvidedService.UriScheme uriScheme = relationalServiceProjectionRule.getUriScheme(LogicalRelationalServiceSqlDialectEnum.MYSQL_DEFAULT);

        // then
        assertThat(uriScheme).isEqualTo(DbaasUserProvidedService.UriScheme.mysql);

        // when
        uriScheme = relationalServiceProjectionRule.getUriScheme(LogicalRelationalServiceSqlDialectEnum.POSTGRESQL_DEFAULT);

        // then
        assertThat(uriScheme).isEqualTo(DbaasUserProvidedService.UriScheme.postgres);

        // when
        uriScheme = relationalServiceProjectionRule.getUriScheme(LogicalRelationalServiceSqlDialectEnum.DEFAULT);

        // then
        assertThat(uriScheme).isEqualTo(DbaasUserProvidedService.UriScheme.postgres);
    }

}
