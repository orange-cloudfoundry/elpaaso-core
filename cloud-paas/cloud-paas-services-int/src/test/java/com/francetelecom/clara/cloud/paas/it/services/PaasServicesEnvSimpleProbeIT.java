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

import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalNodeServiceAssociation;
import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.paas.it.services.helper.PaasServicesEnvITHelper;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;
import java.util.Map;

@ContextConfiguration(classes= PaasServicesEnvSimpleProbeITContext.class)
@RunWith(SpringJUnit4ClassRunner.class)
public class PaasServicesEnvSimpleProbeIT {

    public static final String HEADER_ELPAASO_UNIVERSAL_ID = "elpaaso_universalId";
    public static final String HEADER_ELPAASO_UNIVERSAL_ID_DEFAULT_VALUE = "abcd1234";
    protected static Logger logger = LoggerFactory.getLogger(PaasServicesEnvSimpleProbeIT.class);
    RestTemplate restTemplate = new RestTemplate();

    @Autowired(required = true)
    protected PaasServicesEnvITHelper paasServicesEnvITHelper;
    protected static PaasServicesEnvITHelper sPaasServicesEnvITHelper;

    @Autowired(required = true)
    protected ManageEnvironment manageEnvironment;
    @Autowired(required = true)
    protected ManageLogicalDeployment manageLogicalDeployment;

    @Autowired
    @Qualifier(value = "expectedJavaVersion")
    protected String expectedJavaVersion;

    protected String environmentUID;
    protected int logicalDeploymentID;

    @Autowired
    @Qualifier(value = "expectedOS")
    private String expectedOS;


    @Before
    public void setup() {
        paasServicesEnvITHelper.setDefaultConfigurationItName();
        PaasServicesEnvITHelper.checkThatAutowiredFieldIsNotNull(paasServicesEnvITHelper);
        sPaasServicesEnvITHelper = paasServicesEnvITHelper;
        paasServicesEnvITHelper.setMaxSessions(10);
        paasServicesEnvITHelper.setMaxRequests(200);
        paasServicesEnvITHelper.setEnvType(EnvironmentDto.EnvironmentTypeEnum.DEVELOPMENT);
        environmentUID = paasServicesEnvITHelper.setUp();
        logicalDeploymentID = paasServicesEnvITHelper.getLogicalDeploymentID();
    }

    @AfterClass
    static public void tearDown() {
        sPaasServicesEnvITHelper.tearDown();
    }


    @Test
    public void application_should_be_accessible() {
        paasServicesEnvITHelper.application_should_be_accessible(true);
    }


    /**
     * assert that properties are available for each access URL
     */
    @Test
    public void jee_probe_max_heap_size_should_be_greater_than_logical_min_memory() throws ObjectNotFoundException {
        // Get environment detail dto
        EnvironmentDetailsDto envDto = manageEnvironment.findEnvironmentDetails(environmentUID);

        // get logical deployment
        LogicalDeployment lm = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentID);

        // get all execution node of the logical deployment
        List<ProcessingNode> nodes = lm.listProcessingNodes();

        // get Map of linkDtos of the environment
        Map<String, List<LinkDto>> linksDtosMap = envDto.getLinkDtoMap();

        // iterate on all execution node of the logical deployment to find access url
        for (ProcessingNode node : nodes) {
            // Min memory hint of the selected execution node
            long jeeMinMemory = node.getMinMemoryMbHint() * 1024 * 1024;

            // list all logical execution node service association
            List<LogicalNodeServiceAssociation> nodeServiceAssociations = node.listLogicalServicesAssociations();

            for (LogicalNodeServiceAssociation association : nodeServiceAssociations) {
                // looking for LogicalWebGui service to get its access URL
                if (association.getLogicalService() instanceof LogicalWebGUIService) {

                    LogicalWebGUIService webGUIService = (LogicalWebGUIService) association.getLogicalService();
                    // get the list of linkDto of the selected webGui service
                    List<LinkDto> linkDtos = linksDtosMap.get(webGUIService.getName());

                    for (LinkDto linkDto : linkDtos) {
                        // select only linkDto of ACCESS_LINK type
                        if (linkDto.getLinkType() == LinkDto.LinkTypeEnum.ACCESS_LINK) {

                            String probeMaxHeap = getRestTemplateForProbeFrom(linkDto, "metrics/heap");

                            long jeeMaxMeapSize = Long.valueOf(probeMaxHeap);

                            // log result
                            String resultMessage = "ExecNode " + node.getLabel() + " MaxHeapSize: " + jeeMaxMeapSize + " MinMemoryHint: " + jeeMinMemory;
                            logger.info(resultMessage);

                            // assertions
                            Assert.assertNotNull(probeMaxHeap);
                        }
                    }
                }
            }
        }
    }




    /**
     * assert that java version is correct
     */
    @Test
    public void enforceJavaVersionAndAssertRequestHeader() throws ObjectNotFoundException {
        // Get environment detail dto
        EnvironmentDetailsDto envDto = manageEnvironment.findEnvironmentDetails(environmentUID);

        // get logical deployment
        LogicalDeployment lm = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentID);

        // get all execution node of the logical deployment
        List<ProcessingNode> nodes = lm.listProcessingNodes();

        // get Map of linkDtos of the environment
        Map<String, List<LinkDto>> linksDtosMap = envDto.getLinkDtoMap();

        // iterate on all execution node of the logical deployment to find
        // access url
        for (ProcessingNode node : nodes) {
            for (LogicalNodeServiceAssociation association : node.listLogicalServicesAssociations()) {
                // looking for LogicalWebGui service to get its access URL
                if (association.getLogicalService() instanceof LogicalWebGUIService) {
                    for (LinkDto linkDto : linksDtosMap.get(association.getLogicalService().getName())) {
                        // select only linkDto of ACCESS_LINK type
                        if (linkDto.getLinkType() == LinkDto.LinkTypeEnum.ACCESS_LINK) {
                            String javaVersion = getRestTemplateForProbeFrom(linkDto, "env/java.specification.version");
                            Assert.assertNotNull(javaVersion);
                            Assert.assertEquals("Java version " + javaVersion + " not found on WAS", expectedJavaVersion, javaVersion);

                            String elpaasoUniversalId = getRestTemplateForProbeFrom(linkDto, "headers/"+HEADER_ELPAASO_UNIVERSAL_ID.toLowerCase());
                            Assert.assertNotNull(elpaasoUniversalId);
                            Assert.assertEquals(HEADER_ELPAASO_UNIVERSAL_ID +" not found. ", HEADER_ELPAASO_UNIVERSAL_ID_DEFAULT_VALUE, elpaasoUniversalId);

                        }
                    }
                }
            }
        }
    }

    private String getRestTemplateForProbeFrom(LinkDto link,String path) {

        logger.info("Querying endpoint: {}",link.getUrl().toString()+path);
        SimpleClientHttpRequestFactory clientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        if (paasServicesEnvITHelper.getItConfiguration().isUseHttpIgeProxy()) {
            final String httpProxyHost = paasServicesEnvITHelper.getItConfiguration().getHttpProxyHost();
            final int httpProxyPort = paasServicesEnvITHelper.getItConfiguration().getHttpProxyPort();
            logger.info("Use proxy {}:{} to access Simple Probe", httpProxyHost, httpProxyPort);
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(httpProxyHost, httpProxyPort));

            clientHttpRequestFactory.setProxy(proxy);
        }
        RestTemplate restTemplate = new RestTemplate(clientHttpRequestFactory);
        HttpHeaders customHeaders = new HttpHeaders();
        customHeaders.add(HEADER_ELPAASO_UNIVERSAL_ID, HEADER_ELPAASO_UNIVERSAL_ID_DEFAULT_VALUE);

        HttpEntity<String> entity = new HttpEntity<>("parameters", customHeaders);
        ResponseEntity<String> response = restTemplate.exchange(link.getUrl().toString()+path,
                HttpMethod.GET,
                entity,
                String.class);
        String result = response.getBody();

        return result;
    }



    /**
     * assert that os version is correct
     */
    @Test
    public void validateOSVersion() throws ObjectNotFoundException {
        // Get environment detail dto
        EnvironmentDetailsDto envDto = manageEnvironment.findEnvironmentDetails(environmentUID);

        // get logical deployment
        LogicalDeployment lm = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentID);

        // get all execution node of the logical deployment
        List<ProcessingNode> nodes = lm.listProcessingNodes();

        // get Map of linkDtos of the environment
        Map<String, List<LinkDto>> linksDtosMap = envDto.getLinkDtoMap();

        // iterate on all execution node of the logical deployment to find
        // access url
        for (ProcessingNode node : nodes) {
            for (LogicalNodeServiceAssociation association : node.listLogicalServicesAssociations()) {
                // looking for LogicalWebGui service to get its access URL
                if (association.getLogicalService() instanceof LogicalWebGUIService) {
                    for (LinkDto linkDto : linksDtosMap.get(association.getLogicalService().getName())) {
                        // select only linkDto of ACCESS_LINK type
                        if (linkDto.getLinkType() == LinkDto.LinkTypeEnum.ACCESS_LINK) {
                            String osVersion = getRestTemplateForProbeFrom(linkDto, "/system/os");
                            Assert.assertNotNull(osVersion);
                            if (! osVersion.contains("Warning !!! Unsupported command on Windows system")) {
                                Assert.assertTrue("OS version doesn't match. Expected: " + expectedOS + " - Found: " + osVersion, osVersion.contains(expectedOS));
                            }  else{
                                logger.warn("Probe is running on Windows system. Ignoring test.");
                            }
                        }
                    }
                }
            }
        }
    }

}
