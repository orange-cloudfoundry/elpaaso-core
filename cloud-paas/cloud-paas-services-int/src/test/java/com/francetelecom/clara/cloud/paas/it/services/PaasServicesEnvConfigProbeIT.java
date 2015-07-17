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
package com.francetelecom.clara.cloud.paas.it.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.paas.it.services.helper.PaasServicesEnvITHelper;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import static org.junit.Assert.assertNotNull;

@ContextConfiguration(classes = PaasServicesEnvConfigProbeITContext.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PaasServicesEnvConfigProbeIT {
    protected static Logger logger = LoggerFactory.getLogger(PaasServicesEnvConfigProbeIT.class);


    RestTemplate restTemplate = new RestTemplate();

    @Autowired(required = true)
    private PaasServicesEnvITHelper paasServicesEnvITHelper;
    private static PaasServicesEnvITHelper sPaasServicesEnvITHelper;

    @Autowired(required = true)
    private ManageEnvironment manageEnvironment;

    @Autowired(required = true)
    private ManageLogicalDeployment manageLogicalDeployment;

    private String environmentUID;
    private int logicalDeploymentID;

    @Before
    public void setUp() {
        paasServicesEnvITHelper.setDefaultConfigurationItName();

        PaasServicesEnvITHelper.checkThatAutowiredFieldIsNotNull(paasServicesEnvITHelper);
        paasServicesEnvITHelper.setMaxSessions(10);
        paasServicesEnvITHelper.setMaxRequests(200);
        paasServicesEnvITHelper.setEnvType(EnvironmentDto.EnvironmentTypeEnum.DEVELOPMENT);
        paasServicesEnvITHelper.setUp();
        environmentUID = paasServicesEnvITHelper.getEnvironmentUID();
        logicalDeploymentID = paasServicesEnvITHelper.getLogicalDeploymentID();
        sPaasServicesEnvITHelper = paasServicesEnvITHelper;
    }

    @AfterClass
    static public void tearDown() {
        if (sPaasServicesEnvITHelper != null) {
            sPaasServicesEnvITHelper.tearDown();
        }
    }


    @Test
    public void application_should_be_accessible() {
        paasServicesEnvITHelper.application_should_be_accessible(true);
    }


    /**
     * assert that properties are available for each access URL
     */
    @Test
    public void config_properties_should_be_available_for_each_access_url() throws ObjectNotFoundException {
        EnvironmentDetailsDto envDto = manageEnvironment.findEnvironmentDetails(environmentUID);


        // for each access link we assert that properties are available
        for (LinkDto link : envDto.getSpecificLinkDto(LinkDto.LinkTypeEnum.ACCESS_LINK)) {
            logger.info("Checking config properties at :" + link.getUrl());

            Properties configProperties = getVarEnvAsProperties(link);

            Assert.assertNotNull(configProperties);
            Assert.assertNotNull(configProperties.stringPropertyNames());

            String propertiesListMessage = "Config properties available at " + link.getUrl();
            for (String name : configProperties.stringPropertyNames()) {
                propertiesListMessage += "\n" + name;
            }
            logger.info(propertiesListMessage);
        }
    }

    /**
     * assert that properties are the ones registered in logical model for each access URL
     */
    @Test
    public void config_properties_names_should_be_correct() throws ObjectNotFoundException {
        EnvironmentDetailsDto envDto = manageEnvironment.findEnvironmentDetails(environmentUID);

        // get logical deployment
        LogicalDeployment lm = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentID);

        // get all execution node of the logical deployment
        List<ProcessingNode> nodes = lm.listProcessingNodes();

        // get Map of linkDtos of the environment
        Map<String, List<LinkDto>> linksDtosMap = envDto.getLinkDtoMap();

        // iterate on all execution node of the logical deployment to find access url
        for (ProcessingNode node : nodes) {

            // list all logical execution node service association
            List<LogicalNodeServiceAssociation> nodeServiceAssociations = node.listLogicalServicesAssociations();

            for (LogicalNodeServiceAssociation association : nodeServiceAssociations) {
                // looking for LogicalWebGui service to get its access URL
                if (association.getLogicalService() instanceof LogicalWebGUIService) {

                    LogicalWebGUIService webGUIService = (LogicalWebGUIService) association.getLogicalService();
                    // get the list of linkDto of the selected webGui service
                    List<LinkDto> linkDtos = linksDtosMap.get(webGUIService.getName());

                    for (LinkDto link : linkDtos) {
                        // select only linkDto of ACCESS_LINK type
                        if (link.getLinkType() == LinkDto.LinkTypeEnum.ACCESS_LINK) {
                            logger.info("Checking config properties at :" + link.getUrl());

                            Properties configProperties = getVarEnvAsProperties(link);
                            Assert.assertNotNull(configProperties);


                            Properties expectedProperties = getExpectedConfigPropertiesForNode(node);

                            String missingProperties = "";
                            for (String name : expectedProperties.stringPropertyNames()) {
                                if (!configProperties.containsKey(convertPropertyNameToMatchVariableEnvironmentConstraint(name))) {
                                    missingProperties += name + ", ";
                                }
                            }

                            Assert.assertTrue("properties not found: " + missingProperties + " ;url=" + link.getUrl(), missingProperties.length() == 0);
                        }
                    }
                }
            }
        }

    }

    private String convertPropertyNameToMatchVariableEnvironmentConstraint(String key) {
        return   key.replace(".", "_");
    }

    private boolean containsProperty(Properties properties, String key) {
        String name=key;
        if (key.contains(".")) {
            //for performance, should usually match this case
            name = key.replace(".","_");
        }
        if (properties.contains(name)){
            return true;
        }
        //handle other case
        int count= StringUtils.countOccurrencesOf(key, ".");

        final String[] keysExplodedWithoutDot = key.split(".");
        StringBuilder newName=new StringBuilder(name);
        List<Integer> dotIndexes = new ArrayList<Integer>();
        for(int dotIndex = newName.indexOf(".");dotIndex<newName.length() && dotIndex != -1;) {
            dotIndexes.add(dotIndex);
                dotIndex = newName.indexOf(".", 1);
                newName.setCharAt(dotIndex,'_');
                logger.info("Trying with name {}",newName);
                if (properties.containsKey(newName.toString())){
                    return true;
                }
        }


        return false;
    }

    /**
     * assert that properties values are the ones registered in logical model for each access URL
     */
    @Test
    public void config_properties_values_should_be_correct() throws ObjectNotFoundException {
        EnvironmentDetailsDto envDto = manageEnvironment.findEnvironmentDetails(environmentUID);

        // get logical deployment
        LogicalDeployment lm = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentID);

        // get all execution node of the logical deployment
        List<ProcessingNode> nodes = lm.listProcessingNodes();

        // get Map of linkDtos of the environment
        Map<String, List<LinkDto>> linksDtosMap = envDto.getLinkDtoMap();

        // iterate on all execution node of the logical deployment to find access url
        for (ProcessingNode node : nodes) {

            // list all logical execution node service association
            List<LogicalNodeServiceAssociation> nodeServiceAssociations = node.listLogicalServicesAssociations();

            for (LogicalNodeServiceAssociation association : nodeServiceAssociations) {
                // looking for LogicalWebGui service to get its access URL
                if (association.getLogicalService() instanceof LogicalWebGUIService) {

                    LogicalWebGUIService webGUIService = (LogicalWebGUIService) association.getLogicalService();
                    // get the list of linkDto of the selected webGui service
                    List<LinkDto> linkDtos = linksDtosMap.get(webGUIService.getName());

                    for (LinkDto link : linkDtos) {
                        // select only linkDto of ACCESS_LINK type
                        if (link.getLinkType() == LinkDto.LinkTypeEnum.ACCESS_LINK) {
                            logger.info("Checking config properties at :" + link.getUrl());

                            Properties configProperties = getVarEnvAsProperties(link);
                            Assert.assertNotNull(configProperties);

                            Set<String> registeredProperties = configProperties.stringPropertyNames();

                            Properties expectedProperties = getExpectedConfigPropertiesForNode(node);

                            String missingProperties = "";
                            for (String name : expectedProperties.stringPropertyNames()) {
                                if (!configProperties.containsKey(convertPropertyNameToMatchVariableEnvironmentConstraint(name))) {
                                    missingProperties += name + ", ";
                                }
                                String expectedValue = expectedProperties.getProperty(name);
                                String actualValue = configProperties.getProperty(convertPropertyNameToMatchVariableEnvironmentConstraint(name));
                                Assert.assertEquals("property value is not the expected one; property name=" + name + " ;url=" + link.getUrl(), expectedValue, actualValue);
                            }
                            Assert.assertTrue("properties not found: " + missingProperties + " ;url=" + link.getUrl(), missingProperties.length() == 0);
                        }
                    }
                }
            }
        }
    }

    /*
    check here for existing CF VAR http://docs.run.pivotal.io/devguide/deploy-apps/environment-variable.html
     */
    @Test
    public void should_ensure_CF_expose_default_entry() throws ObjectNotFoundException, IOException {
        String EXPECTED_VCAP_APPLICATION_VARENV = "VCAP_APPLICATION";
        String EXPECTED_VCAP_SERVICES_VARENV = "VCAP_SERVICES";

        EnvironmentDetailsDto envDto = manageEnvironment.findEnvironmentDetails(environmentUID);

        Map<String, List<LinkDto>> linkDtosMap = envDto.getLinkDtoMap();

        LogicalDeployment logicalModel = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentID);

        // Iterate over all web gui services of the model
        Set<LogicalWebGUIService> webGuiServices = logicalModel.listLogicalServices(LogicalWebGUIService.class);
        Assert.assertTrue("This test assumes that at least one webgui service is defined in logical model", webGuiServices.size() > 0);
        for (LogicalWebGUIService webGuiService : webGuiServices) {
            logger.debug("Checking web gui url is registred as env property for web gui with label " + webGuiService.getLabel());

            // Retrieve access link
            List<LinkDto> linkDtos = linkDtosMap.get(webGuiService.getName());

            LinkDto webUiLink = null;

            for (LinkDto link : linkDtos) {
                if (link.getLinkType() == LinkDto.LinkTypeEnum.ACCESS_LINK) {
                    webUiLink = link;
                }
            }

            Assert.assertNotNull("No access link found for web gui " + webGuiService.getLabel(), webUiLink);
            logger.debug("Access link is:" + webUiLink.getUrl().toExternalForm());

            // Retrieve properties for access link
            Properties varEnvs = getVarEnvAsProperties(webUiLink);
            Assert.assertNotNull(varEnvs);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode vcapApplication = mapper.readTree(varEnvs.getProperty(EXPECTED_VCAP_APPLICATION_VARENV, "{" + EXPECTED_VCAP_APPLICATION_VARENV + " is NOT FOUND}"));
            final List<String> application_uris = vcapApplication.findValuesAsText("application_uris");
            String expectedWebUiUrl = webUiLink.getUrl().toExternalForm();
            boolean exposedUrlFound = false;
            for (String application_uri : application_uris) {
                if (expectedWebUiUrl.contains(application_uri)) {
                    exposedUrlFound = true;
                }
            }
            Assert.assertTrue("Should have found webUI " + expectedWebUiUrl + " in " + EXPECTED_VCAP_APPLICATION_VARENV, exposedUrlFound);

            mapper = new ObjectMapper();
            mapper.readTree(varEnvs.getProperty(EXPECTED_VCAP_SERVICES_VARENV, "{" + EXPECTED_VCAP_SERVICES_VARENV + " is NOT FOUND}"));
            // Assert that the webUiUrlJndi property is defined
            //       Assert.assertTrue("The jndi entry [" + expectedJndiPropertyName + "] corresponding to web gui url is not found as a config property", registeredVarEnvs.contains(expectedJndiPropertyName));

            // Assert that the webUiUrlJndi property value equals to access link
            //      Assert.assertEquals("Incorrect value for the jndi property " + expectedJndiPropertyName, expectedJndiPropertyValue, varEnvs.getProperty(expectedJndiPropertyName));
        }
    }

    /**
     * call getConfigProperties of the config probe for the specified access url
     */
    Properties getConfigPropertiesForUrl(LinkDto link) {

        Properties result = new Properties();
        Properties metaProperties = paasServicesEnvITHelper.executeRestRequest(link, "env/", Properties.class);
        assertNotNull(metaProperties);
        HashMap<String, String> systemProperties = (HashMap<String, String>) metaProperties.get("systemProperties");
        result.putAll(systemProperties);
        return result;

    }

    Properties getVarEnvAsProperties(LinkDto link) {
        Properties result = new Properties();
        Properties metaProperties = paasServicesEnvITHelper.executeRestRequest(link, "env/", Properties.class);
        assertNotNull(metaProperties);
        HashMap<String, String> systemEnvironment = (HashMap<String, String>) metaProperties.get("systemEnvironment");
        result.putAll(systemEnvironment);

        return result;
    }

    /**
     * return the list of config properties that are expected to be registered on an access url
     *
     * @param node JeeProcessing
     * @return
     * @throws ObjectNotFoundException
     */
    private Properties getExpectedConfigPropertiesForNode(
            ProcessingNode node) throws ObjectNotFoundException {
        Properties properties = new Properties();

        Assert.assertNotNull("no execution node found with name " + node.getName());

        // Getting config sets attached to this node
        List<LogicalConfigService> configSets = node.listLogicalServices(LogicalConfigService.class);
        for (LogicalConfigService configSet : configSets) {
            String keyPrefix = configSet.getKeyPrefix();
            Properties props = new Properties();
            try {
                props.load(new StringReader(configSet.getConfigSetContent()));
            } catch (IOException e) {
                String message = "Incorrect properties format for configSet=" + getClass().getSimpleName();
                logger.error(message, e);
            }
            for (String name : props.stringPropertyNames()) {
                properties.put(keyPrefix + name, props.getProperty(name));
            }
        }
        return properties;
    }
}
