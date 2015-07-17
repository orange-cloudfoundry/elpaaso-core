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
package com.francetelecom.clara.cloud.logicalmodel;

import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.commons.GuiMapping;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 09/01/12
 */
@ContextConfiguration(locations = "LogicalServicesTest-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class LogicalModelItemAnnotationsTest {


     /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(LogicalModelItemAnnotationsTest.class);

    /**
     * Spring automatically injects all know deans of type (@Link LogicalModelItem)
     */
    @Autowired
    Map<String, LogicalModelItem> logicalServicesMap;

    /**
     * Test if logical services list contains all logical services :
     *   - LogicalConfigService
     *   - JeeProcessing
     *   - LogicalInternalHttpCommunicationService
     *   - LogicalLogService
     *   - LogicalMomService
     *   - LogicalOnlineStorageService
     *   - LogicalQueueReceiveService
     *   - LogicalQueueSendService
     *   - LogicalRelationalService
     *   - LogicalSoapConsumer
     *   - LogicalWebGUIService
     */
    @Test
    public void testExpectedLogicalServicesList() {

        List<String> expectedServices = getAllExpectedServices();

        for (String name : expectedServices) {
            assertTrue(logicalServicesMap.containsKey(name));
        }

        assertEquals(expectedServices.size(), logicalServicesMap.size());

    }

    /**
     * Test if logical services list contains all external services
     *   - LogicalQueueReceiveService
     *   - LogicalQueueSendService
     *   - LogicalSoapConsumer
     *   - LogicalWebGUIService
     */
    @Test
    public void testExpectedExternalServices() {

        Map<String, LogicalModelItem> actualExternalServices = new HashMap<String, LogicalModelItem>(logicalServicesMap);

        for (LogicalModelItem service : logicalServicesMap.values()) {
            if (!getGuiServiceMappingAnnotation(service.getClass()).isExternal()) {
                actualExternalServices.remove(service.getClass().getSimpleName());
            }
        }

        assertEquals(getExpectedExternalServices().size(), actualExternalServices.size());


    }

    /**
     * Test if logical services list contains internal service
     */
    @Test
    public void testInternalServices() {

        Map<String, LogicalModelItem> actualInternalServices = new HashMap<String, LogicalModelItem>(logicalServicesMap);

        for (LogicalModelItem service : logicalServicesMap.values()) {
            if (getGuiServiceMappingAnnotation(service.getClass()).isExternal()) {
                actualInternalServices.remove(service.getClass().getSimpleName());
            }
        }

        assertEquals(getExpectedInternalServices().size(), actualInternalServices.size());

    }

    /**
     * Test if service is SUPPORTED / SKIPPED / BETA
     */
    @Test
    public void testServiceStatus() {

        for (LogicalModelItem service : logicalServicesMap.values()) {
            GuiClassMapping.StatusType expectedStatus = getServiceStatusType().get(service);
            GuiClassMapping.StatusType actualStatus = getGuiServiceMappingAnnotation(service.getClass()).status();

            assertEquals(expectedStatus, actualStatus);

        }

    }

    /**
     * Test parameters of web gui service class
     */
    @Test
    public void testServiceParameterEnable() {

        LogicalModelItem service = logicalServicesMap.get("LogicalWebGUIService");

        Map<String, GuiMapping.StatusType> webGuiParam = getLogicalWebGuiServiceParameter();

        Field[] serviceFields = service.getClass().getDeclaredFields();

        for (Field field : serviceFields) {
            String parameterName = field.getName();
            GuiMapping.StatusType parameterStatusType = getGuiServiceParameterMappingAnnotation(service.getClass(), field.getName()).status();

            GuiMapping.StatusType expectedType = getLogicalWebGuiServiceParameter().get(parameterName);
            if (expectedType != null) {
                assertEquals(expectedType, parameterStatusType);
            }
        }

    }


    private GuiClassMapping getGuiServiceMappingAnnotation(Class<? extends LogicalModelItem> introspectedClass) {
        return introspectedClass.getAnnotation(GuiClassMapping.class);
    }

    private GuiMapping getGuiServiceParameterMappingAnnotation(Class<? extends LogicalModelItem> introspectedClass, String name) {

        try {

            Field field = introspectedClass.getDeclaredField(name);
            GuiMapping clazz = field.getAnnotation(GuiMapping.class);
            logger.debug("Field "+field.getName()+" has status "+clazz.status());

            return clazz;

        } catch (NoSuchFieldException e) {
            logger.debug("Parameter "+name+" is not a "+introspectedClass.getSimpleName()+" field.");
        }
        return null;
    }


    /**
     * Create List of expected service :
     *   - LogicalConfigService
     *   - JeeProcessing
     *   - CFJavaProcessing
     *   - LogicalInternalHttpCommunicationService
     *   - LogicalLogService
     *   - LogicalMomService
     *   - LogicalOnlineStorageService
     *   - LogicalQueueReceiveService
     *   - LogicalQueueSendService
     *   - LogicalRelationalService
     *   - LogicalSoapConsumer
     *   - LogicalWebGUIService
     */
    private List<String> getAllExpectedServices() {
        List<String> expectedServices = new ArrayList<String>();

        expectedServices.add("LogicalConfigService");
        expectedServices.add("JeeProcessing");
        expectedServices.add("CFJavaProcessing");
        expectedServices.add("LogicalInternalHttpCommunicationService");
        expectedServices.add("LogicalLogService");
        expectedServices.add("LogicalMomService");
        expectedServices.add("LogicalOnlineStorageService");
        expectedServices.add("LogicalQueueReceiveService");
        expectedServices.add("LogicalQueueSendService");
        expectedServices.add("LogicalRelationalService");
        expectedServices.add("LogicalSoapConsumer");
        expectedServices.add("LogicalSoapService");
        expectedServices.add("LogicalWebGUIService");

        return expectedServices;
    }

    /**
     * Create List of expected service :
     *   - LogicalLogService
     *   - LogicalQueueReceiveService
     *   - LogicalQueueSendService
     *   - LogicalSoapConsumer
     *   - LogicalWebGUIService
     */
    private List<String> getExpectedExternalServices() {

        List<String> expectedExternalServices = new ArrayList<String>();

        expectedExternalServices.add("LogicalQueueReceiveService");
        expectedExternalServices.add("LogicalQueueSendService");
        expectedExternalServices.add("LogicalSoapConsumer");
        expectedExternalServices.add("LogicalSoapService");
        expectedExternalServices.add("LogicalWebGUIService");

        return expectedExternalServices;

    }

    /**
     * Create List of expected service :
     *   - LogicalLogService
     *   - LogicalConfigService
     *   - JeeProcessing
     *   - LogicalInternalHttpCommunicationService
     *   - LogicalMomService
     *   - LogicalOnlineStorageService
     *   - LogicalRelationalService
     */
    private List<String> getExpectedInternalServices() {
        List<String> expectedServices = new ArrayList<String>();

        expectedServices.add("LogicalLogService");
        expectedServices.add("LogicalConfigService");
        expectedServices.add("JeeProcessing");
        expectedServices.add("CFJavaProcessing");
        expectedServices.add("LogicalInternalHttpCommunicationService");
        expectedServices.add("LogicalMomService");
        expectedServices.add("LogicalOnlineStorageService");
        expectedServices.add("LogicalRelationalService");

        return expectedServices;
    }

    private Map<String, GuiMapping.StatusType> getLogicalWebGuiServiceParameter() {

        Map<String, GuiMapping.StatusType> parameters = new HashMap<String, GuiMapping.StatusType>();

        parameters.put("contextRoot", GuiMapping.StatusType.SUPPORTED);
        parameters.put("stateful", GuiMapping.StatusType.SUPPORTED);
        parameters.put("secure", GuiMapping.StatusType.READ_ONLY);
        parameters.put("maxNumberSessions", GuiMapping.StatusType.SUPPORTED);
        parameters.put("maxReqPerSeconds", GuiMapping.StatusType.READ_ONLY);
        parameters.put("jndiPrefix", GuiMapping.StatusType.SUPPORTED);

        return parameters;

    }

    private Map<LogicalModelItem, GuiClassMapping.StatusType> getServiceStatusType() {

        Map<LogicalModelItem, GuiClassMapping.StatusType> serviceStatusType = new HashMap<LogicalModelItem, GuiClassMapping.StatusType>();

        serviceStatusType.put(logicalServicesMap.get("LogicalConfigService"), GuiClassMapping.StatusType.SUPPORTED);
        serviceStatusType.put(logicalServicesMap.get("JeeProcessing"), GuiClassMapping.StatusType.SUPPORTED);
        serviceStatusType.put(logicalServicesMap.get("CFJavaProcessing"), GuiClassMapping.StatusType.SUPPORTED);
        serviceStatusType.put(logicalServicesMap.get("LogicalInternalHttpCommunicationService"), GuiClassMapping.StatusType.SKIPPED);
        serviceStatusType.put(logicalServicesMap.get("LogicalLogService"), GuiClassMapping.StatusType.PREVIEW);
        serviceStatusType.put(logicalServicesMap.get("LogicalMomService"), GuiClassMapping.StatusType.BETA);
        serviceStatusType.put(logicalServicesMap.get("LogicalOnlineStorageService"), GuiClassMapping.StatusType.BETA);
        serviceStatusType.put(logicalServicesMap.get("LogicalQueueReceiveService"), GuiClassMapping.StatusType.BETA);
        serviceStatusType.put(logicalServicesMap.get("LogicalQueueSendService"), GuiClassMapping.StatusType.BETA);
        serviceStatusType.put(logicalServicesMap.get("LogicalRelationalService"), GuiClassMapping.StatusType.SUPPORTED);
        serviceStatusType.put(logicalServicesMap.get("LogicalSoapConsumer"), GuiClassMapping.StatusType.BETA);
        serviceStatusType.put(logicalServicesMap.get("LogicalSoapService"), GuiClassMapping.StatusType.BETA);
        serviceStatusType.put(logicalServicesMap.get("LogicalWebGUIService"), GuiClassMapping.StatusType.SUPPORTED);

        return serviceStatusType;

    }




}
