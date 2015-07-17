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
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.CFWicketCxfJpaLogicalModelCatalog;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppProperties;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.paas.projection.security.CryptService;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.Space;
import org.fest.assertions.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.URL;

@RunWith(MockitoJUnitRunner.class)
public class BindingServiceProjectionRuleTest {

    @Mock
    MvnRepoDao mvnRepoDao;

    @Mock
    CryptService cryptService;


    @Test
    public void should_bind_service_to_app() throws Exception {
        // given
        ServiceProjectionRule relationalServiceServiceProjectionRule = new RelationalServiceProjectionRule(cryptService, mvnRepoDao, "");

        MavenReference sqlScript = new MavenReference("groupId", "artifactId", "version");
        sqlScript.setAccessUrl(new URL("http://localhost"));
        Mockito.when(mvnRepoDao.resolveUrl(sqlScript)).thenReturn(sqlScript);
        Mockito.when(cryptService.generateRandomPassword()).thenReturn("azerty");

        LogicalDeployment logicalDeployment = new LogicalDeployment();
        CFWicketCxfJpaLogicalModelCatalog logicalModelCatalog = new CFWicketCxfJpaLogicalModelCatalog();
        SampleAppProperties sampleAppProperties = new SampleAppProperties();
        logicalModelCatalog.setSampleAppProperties(sampleAppProperties);
        logicalModelCatalog.populateLogicalDeployment(logicalDeployment);
        TechnicalDeployment td = new TechnicalDeployment("name");
        Space space = new Space(td);
        relationalServiceServiceProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        App app = new App(td, space, new MavenReference(), "joyn");
        app.setLogicalModelId(logicalDeployment.findProcessingNode("Cf-wicket-jpaSample").getName());

        AssociationProjectionRule defaultServiceBindingProjectionRule = new DefaultServiceBindingProjectionRule();

        defaultServiceBindingProjectionRule.apply(logicalDeployment, td, new DummyProjectionContext(space));

        Assertions.assertThat(app.getServiceNames()).containsExactly("postgres-db");
    }
}