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
package com.francetelecom.clara.cloud.paas.projection;

import com.francetelecom.clara.cloud.commons.P6SpyAppender;
import com.francetelecom.clara.cloud.coremodel.*;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SampleAppFactory;
import com.francetelecom.clara.cloud.logicalmodel.samplecatalog.SimpleConfigProbeLogicalModelCatalog;
import com.francetelecom.clara.cloud.model.DeploymentProfileEnum;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentTemplate;
import com.francetelecom.clara.cloud.model.validators.ModelItemGenericValidationUtils;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDao;
import com.francetelecom.clara.cloud.mvn.consumer.MvnRepoDaoTestUtils;
import com.francetelecom.clara.cloud.paas.projection.cf.ProjectionPlanStrategy;
import com.francetelecom.clara.cloud.techmodel.cf.App;
import com.francetelecom.clara.cloud.techmodel.cf.EnvVariableKey;
import com.francetelecom.clara.cloud.techmodel.cf.EnvVariableValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

/**
 * Integrated tests for projection: asserts the created technical model respects expectations.
 * <p>
 * To keep it fast, this does not include persistence tests
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProjectionServiceIT {


    protected static Logger logger = LoggerFactory.getLogger(ProjectionServiceIT.class.getName());
    private static Logger loggerDump = LoggerFactory.getLogger("com.francetelecom.clara.cloud.dump");
    /**
     * Maven Dao is mocked for unit testing
     */
    @Autowired
    protected MvnRepoDao mvnRepoDaoMock;
    @Autowired
    @Qualifier("projectionService")
    MultiProfileProjectionServiceDispatcherImpl projectionService;
    /**
     * white box autowire, to place mock JonasScale
     */
    @Autowired
    @Qualifier("cfProjectionPlanProdStrategy")
    ProjectionPlanStrategy planProdStrategy;

    @Autowired
    @Qualifier("simpleConfigProbeLogicalModelCatalog")
    SampleAppFactory configLogicalModelCatalog;

    P6SpyAppender p6spyAppender = null;

    @Test
    public void testConfigDevProjection() throws JAXBException, IOException, UnsupportedProjectionException {
        generateAndAssertTechnicalModel(configLogicalModelCatalog, MiddlewareProfile.getDefault(), 1, 1, false, false);
    }

    @Test
    public void testConfigProdProjection() throws JAXBException, IOException, UnsupportedProjectionException {
        generateAndAssertTechnicalModel(configLogicalModelCatalog, MiddlewareProfile.getDefault(), 1, 2, /* Two Apps per JeeProcessing */ true, false);
    }

    @Before
    public void setUp() {

        // Mock MvnRepoDao.resolveUrl()
        MvnRepoDaoTestUtils.mockResolveUrl(mvnRepoDaoMock);

        p6spyAppender = P6SpyAppender.getCurrentInstance();
        if (p6spyAppender != null) {
            p6spyAppender.reset();
        }
    }

    @After
    public void tearDown() {
        p6spyAppender = P6SpyAppender.getCurrentInstance();
        if (p6spyAppender != null) {
            p6spyAppender.checkStats(false);
        }
    }

    /**
     * Entry point for assertions on the generated TDT, using
     * {@link ProjectionServiceIT.ApplicationModelEnum}
     *
     * @throws com.francetelecom.clara.cloud.paas.projection.UnsupportedProjectionException
     */
    public TechnicalDeploymentTemplate generateAndAssertTechnicalModel(SampleAppFactory configLogicalModelCatalog1, MiddlewareProfile middlewareProfileVersion, int requestedMaxNumberSessions, int expectedNbAppInstances, boolean isProd, boolean sizeWithConstraintSolver)
            throws JAXBException, IOException, UnsupportedProjectionException {

        final ApplicationRelease applicationRelease = generateApplicationRelease(middlewareProfileVersion, configLogicalModelCatalog1
        );

        if (sizeWithConstraintSolver) {
            assert isProd : "sizeWithConstraintSolver makes only sense in production projections";
        }

        DeploymentProfileEnum projectionProfile;
        if (isProd) {
            projectionProfile = DeploymentProfileEnum.PRODUCTION;
        } else {
            projectionProfile = DeploymentProfileEnum.DEVELOPMENT;
        }

        LogicalDeployment logicalDeployment = applicationRelease.getLogicalDeployment();

        TechnicalDeploymentTemplate tdt = projectionService.generateNewDeploymentTemplate(applicationRelease, projectionProfile);

        TechnicalDeployment td = tdt.getTechnicalDeployment();

        assertDefaultConfig(logicalDeployment, td);


        // Update is done at env creation time but is needed here to assert configuration in assertConfig
        TechnicalDeploymentInstance tdi = new TechnicalDeploymentInstance(tdt, td);
        ConfigRole configRole = new ConfigRole(applicationRelease.getApplication().getUID());
        configRole.setValues(asList(new ConfigValue(SimpleConfigProbeLogicalModelCatalog.WHOLE_APP_CONFIG, "enable.palier.for.open.launch", "true", "Launch is active!")));
        List<ConfigRole> configRoles = asList(configRole);
        projectionService.updateDeploymentTemplateInstance(tdi, applicationRelease, configRoles);

        assertOverridenConfigInAllApps(td, "enable.palier.for.open.launch", "true");

        if (loggerDump.isDebugEnabled()) { // enable when needed
            loggerDump.debug(td.dumpXml());
        }
        try {
            ModelItemGenericValidationUtils.validateModel(tdt, logger);
        } catch (Exception e) {
            fail("unexpected invalid model:" + e);
        }

        assertNotNull("expecting a name in the TD", td.getName());


        return tdt;
    }

    private void assertOverridenConfigInAllApps(TechnicalDeployment td, String key, String expectedValue) {
        Set<App> apps = td.listXaasSubscriptionTemplates(App.class);
        for (App app : apps) {
            String envVarValue = app.getEnvVarValue(key);
            assertEquals("invalid env vars " + app.listEnvVariables() + " in app:" + app.getAppName(), expectedValue, envVarValue);
        }

    }

    /**
     * Asserts that each ConfigService was properly filled in as Env vars
     */
    private void assertDefaultConfig(LogicalDeployment ld, TechnicalDeployment td) {
        List<ProcessingNode> executionNodes = ld.listProcessingNodes();
        for (ProcessingNode executionNode : executionNodes) {
            Set<App> apps = td.listXaasSubscriptionTemplates(App.class, executionNode.getName());

            for (App app : apps) {
                Set<Map.Entry<EnvVariableKey, EnvVariableValue>> envs = app.listEnvVariables().entrySet();
                Properties mergedConfigServicesProperties = null;
                try {
                    mergedConfigServicesProperties = executionNode.getMergedConfigServicesProperties();
                } catch (InvalidConfigServiceException e) {
                    fail("caught expected " + e);
                }
                Set<Map.Entry<Object, Object>> entries = mergedConfigServicesProperties.entrySet();
                for (Map.Entry<Object, Object> entry : entries) {
                    String configKey = (String) entry.getKey();
                    String configValue = (String) entry.getValue();

                    String context = "configKey=" + configKey + " configValue=" + configValue;
                    boolean matchFound = false;
                    for (Map.Entry<EnvVariableKey, EnvVariableValue> env : envs) {
                        boolean keysMatch = new EnvVariableKey(configKey).equals(env.getKey());
                        boolean valuesMatch = new EnvVariableValue(configValue).equals(env.getValue());
                        if (keysMatch && valuesMatch) {
                            assertFalse("duplicate env entry found: " + context, matchFound);
                            matchFound = true;
                        }
                    }
                    assertTrue("missing env entry for " + context, matchFound);
                }
            }
        }
    }

    /**
     * Utility method to generate an application release (along with the logical
     * deployment) corresponding
     */
    protected ApplicationRelease generateApplicationRelease(MiddlewareProfile middlewareProfileVersion, SampleAppFactory logicalModelCatalog) {
        ApplicationRelease applicationRelease = new ApplicationRelease(new Application("mon Appli", "code"), "G1R0C0");
        LogicalDeployment logicalDeploymentToUpdate = applicationRelease.getLogicalDeployment();

        logicalModelCatalog.populateLogicalDeployment(logicalDeploymentToUpdate);
        if (middlewareProfileVersion != null) {
            logger.info("set middleware profile version:{}", middlewareProfileVersion);
            applicationRelease.setMiddlewareProfileVersion(middlewareProfileVersion.getVersion());
        }

        return applicationRelease;
    }


    /**
     * Initial attempt to variabilize tests with different types of application
     * models.
     */
    public static enum ApplicationModelEnum {
        //
        CONFIG()
    }
}
