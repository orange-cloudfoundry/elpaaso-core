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
package com.francetelecom.clara.cloud.presentation.utils;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 16/01/12
 * Time: 11:30
 * To change this template use File | Settings | File Templates.
 */
public class CreateObjectsWithGUI {

    private static String formPath = NavigationUtils.selectFormPath;
    private static String paramsFormPath = NavigationUtils.designerParamFormPath;

    private static final Logger logger = LoggerFactory.getLogger(CreateObjectsWithGUI.class.getName());
    
    /**
     * Creates a JEE Processing (execution node)
     * @param myTester WicketTester of the current test
     * @param label label of the JEE Processing
     * @param groupId group id of the maven reference of the EAR
     * @param artifactId artifact id of the maven reference of the EAR
     * @param version version of the maven reference of the EAR
     * @param classifier classifier ef the maven reference of the EAR
     * @param optional TODO
     */
    public static void createJEEProcessing(PaasWicketTester myTester, String label, String groupId, String artifactId, String version, String classifier, boolean optional, int minMemoryMbHint) {

        // When adding jee processing we need mvnDao to chack maven reference
//        MvnRepoDao mvnDao = (MvnRepoDao) myTester.getPaasApplication().getContext().getBean("mvnDao");
//        MvnRepoDaoTestUtils.mockResolveUrl(mvnDao);

//        myTester.assertContains("jee processing");

        // Select JEE Processing
        selectService(myTester, JeeProcessing.class);
        // Fill the form and submit it
        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", label);
        creationForm.setValue("softwareReference.groupId", groupId);
        creationForm.setValue("softwareReference.artifactId", artifactId);
        creationForm.setValue("softwareReference.version", version);
        creationForm.setValue("softwareReference.classifier", classifier);
        creationForm.setValue("optionalSoftwareReference", optional);
        creationForm.setValue("minMemoryMbHint",String.valueOf(minMemoryMbHint));
        NavigationUtils.submitParamsForm(myTester);

    }

    /**
     * Creates a CF Java Processing (execution node)
     * @param myTester WicketTester of the current test
     * @param label label of the CF Java Processing
     * @param groupId group id of the maven reference of the JAR
     * @param artifactId artifact id of the maven reference of the JAR
     * @param version version of the maven reference of the JAR
     * @param extension extension of the maven reference of the JAR
     * @param classifier classifier ef the maven reference of the JAR
     * @param optional TODO
     */
    public static void createCFJavaProcessing(PaasWicketTester myTester, String label, String groupId, String artifactId, String version, String extension, String classifier, boolean optional, int minMemoryMbHint) {

        // When adding jee processing we need mvnDao to chack maven reference
//        MvnRepoDao mvnDao = (MvnRepoDao) myTester.getPaasApplication().getContext().getBean("mvnDao");
//        MvnRepoDaoTestUtils.mockResolveUrl(mvnDao);

//        myTester.assertContains("cf java processing");

        // Select CF Java Processing
        selectService(myTester, CFJavaProcessing.class);
        // Fill the form and submit it
        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", label);
        creationForm.setValue("softwareReference.groupId", groupId);
        creationForm.setValue("softwareReference.artifactId", artifactId);
        creationForm.setValue("softwareReference.version", version);
        creationForm.setValue("softwareReference.extension", extension);
        creationForm.setValue("softwareReference.classifier", classifier);
        creationForm.setValue("optionalSoftwareReference", optional);
        creationForm.setValue("minMemoryMbHint",String.valueOf(minMemoryMbHint));
        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createWebGUIService(PaasWicketTester myTester, String name, String contextRoot, boolean stateful, boolean secure
            , int maxNumberSessions, int maxReqPerSeconds) {

        selectService(myTester, LogicalWebGUIService.class);
        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", name);
        creationForm.setValue("contextRoot.value", contextRoot);
        creationForm.setValue("stateful", stateful);
        creationForm.setValue("maxNumberSessions", String.valueOf(maxNumberSessions));
        creationForm.setValue("maxReqPerSeconds", String.valueOf(maxReqPerSeconds));
        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createWebServiceConsumerService(PaasWicketTester myTester, String name, String jndiPrefix, LogicalSoapConsumer.SoapServiceDomainEnum domain
    , String serviceProviderName, String serviceName, int serviceMajorVersion, int serviceMinorVersion) {

        selectService(myTester, LogicalSoapConsumer.class);
        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", name);
        creationForm.setValue("jndiPrefix", jndiPrefix);
        //creationForm.select("wsDomain", domain.ordinal());
        //tester.executeAjaxEvent(paramsFormPath + ":wsDomain", "onchange");
        creationForm.setValue("serviceProviderName", serviceProviderName);
        creationForm.setValue("serviceName", serviceName);
        creationForm.setValue("serviceMajorVersion", String.valueOf(serviceMajorVersion));
        creationForm.setValue("serviceMinorVersion", String.valueOf(serviceMinorVersion));
        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createSoapService(PaasWicketTester myTester,
                                         String label,
                                         String jndiPrefix,
                                         String serviceName,
                                         int serviceMajorVersion,
                                         int serviceMinorVersion,
                                         String description,
                                         LogicalAttachmentTypeEnum serviceAttachmentType,
                                         String contextRoot,
                                         MavenReference mavenReference,
                                         LogicalInboundAuthenticationPolicy inboundAuthenticationPolicy,
                                         LogicalOutboundAuthenticationPolicy outboundAuthenticationPolicy,
                                         LogicalIdentityPropagationEnum identityPropagationEnum, String servicePath) {

        selectService(myTester, LogicalSoapService.class);
        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", label);
        creationForm.setValue("jndiPrefix", jndiPrefix);
        creationForm.setValue("serviceName", serviceName);
        creationForm.setValue("serviceMajorVersion", String.valueOf(serviceMajorVersion));
        creationForm.setValue("serviceMinorVersion", String.valueOf(serviceMinorVersion));
        creationForm.setValue("description", description);
        creationForm.select("serviceAttachmentType", serviceAttachmentType.ordinal());
        creationForm.setValue("contextRoot.value", contextRoot);
        creationForm.setValue("servicePath.value", servicePath);
        creationForm.setValue("serviceAttachments.groupId",mavenReference.getGroupId());
        creationForm.setValue("serviceAttachments.artifactId",mavenReference.getArtifactId());
        creationForm.setValue("serviceAttachments.version",mavenReference.getVersion());
        creationForm.select("inboundAuthenticationPolicy.accessZone", inboundAuthenticationPolicy.getAccessZone().ordinal());
        creationForm.select("inboundAuthenticationPolicy.authenticationType",inboundAuthenticationPolicy.getAuthenticationType().ordinal());
        creationForm.select("inboundAuthenticationPolicy.protocol",inboundAuthenticationPolicy.getProtocol().ordinal());
        creationForm.select("outboundAuthenticationPolicy.authenticationType",outboundAuthenticationPolicy.getAuthenticationType().ordinal());
        creationForm.select("outboundAuthenticationPolicy.protocol",outboundAuthenticationPolicy.getProtocol().ordinal());
        creationForm.select("identityPropagation",identityPropagationEnum.ordinal());
        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createRelationalDatabaseService(PaasWicketTester myTester, String name, String serviceName
            , LogicalRelationalServiceSqlDialectEnum sqlVersion,
                                                       int capacityMo) {

        selectService(myTester, LogicalRelationalService.class);

        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", name);
        creationForm.setValue("serviceName", serviceName);
        int sqlIndex = sqlVersion.ordinal();

        myTester.assertVisible(paramsFormPath + ":sqlVersion");
        myTester.assertComponent(paramsFormPath + ":sqlVersion", DropDownChoice.class);
        creationForm.select("sqlVersion", sqlIndex);
        creationForm.setValue("capacityMo", String.valueOf(capacityMo));

        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createQueueSendService(PaasWicketTester myTester, String name, String target, String targetApplicationName, String targetApplicationVersion
    , String targetServiceName, String targetServiceVersion, String jndiQueueName
    , long msgMaxSizeKB, long maxNbMsgPerDay, long nbRetentionDay) {

        selectService(myTester, LogicalQueueSendService.class);
        
        List<String> applications = null;
        List<String> applicationVersions = null;
        List<String> services = null;
        List<String> serviceVersions = null;
        try {
            applications = ((ManageLogicalDeployment) myTester.getPaasApplication().getContext().getBean(ManageLogicalDeployment.class)).getQrsApplications(target);
            applicationVersions = ((ManageLogicalDeployment) myTester.getPaasApplication().getContext().getBean(ManageLogicalDeployment.class)).getQrsApplicationVersions(target, targetApplicationName);
            services = ((ManageLogicalDeployment) myTester.getPaasApplication().getContext().getBean(ManageLogicalDeployment.class)).getQrsServices(target, targetApplicationName, targetApplicationVersion);
            serviceVersions = ((ManageLogicalDeployment) myTester.getPaasApplication().getContext().getBean(ManageLogicalDeployment.class)).getQrsServicesVersions(target, targetApplicationName, targetApplicationVersion, targetServiceName);
        } catch (ObjectNotFoundException e) {
            Assert.fail("unable to fetch Qrs informations");
        }

        FormTester creationFormAjax = NavigationUtils.getParamsFormTester(myTester);
        simpleSelect(myTester, creationFormAjax, "targetApplicationName", applications.indexOf(targetApplicationName));
        simpleSelect(myTester, creationFormAjax, "targetApplicationVersion", applicationVersions.indexOf(targetApplicationVersion));
        simpleSelect(myTester, creationFormAjax, "targetServiceName", services.indexOf(targetServiceName));
        simpleSelect(myTester, creationFormAjax, "targetServiceVersion", serviceVersions.indexOf(targetServiceVersion));

        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", name);
        creationForm.setValue("jndiQueueName", jndiQueueName);
        creationForm.select("targetApplicationName",applications.indexOf(targetApplicationName));
        creationForm.select("targetApplicationVersion",applicationVersions.indexOf(targetApplicationVersion));
        creationForm.select("targetServiceName",services.indexOf(targetServiceName));
        creationForm.select("targetServiceVersion",serviceVersions.indexOf(targetServiceVersion));
        selectItem(myTester, creationForm, msgMaxSizeKB, "msgMaxSizeKB");
        selectItem(myTester, creationForm, maxNbMsgPerDay, "maxNbMsgPerDay");
        selectItem(myTester, creationForm, nbRetentionDay, "nbRetentionDay");


        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createQueueReceiveService(PaasWicketTester myTester, String name, String serviceName, String serviceVersion, String jndiQueuName
            , long msgMaxSizeKB, long maxNbMsgPerDay, long nbRetentionDay) {

        selectService(myTester, LogicalQueueReceiveService.class);
        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", name);
        creationForm.setValue("serviceName", serviceName);
        creationForm.setValue("serviceVersion", serviceVersion);
        creationForm.setValue("jndiQueueName", jndiQueuName);
        myTester.assertComponent(paramsFormPath + ":msgMaxSizeKB", DropDownChoice.class);
        myTester.assertComponent(paramsFormPath + ":maxNbMsgPerDay", DropDownChoice.class);
        myTester.assertComponent(paramsFormPath + ":nbRetentionDay", DropDownChoice.class);

        selectItem(myTester, creationForm, msgMaxSizeKB, "msgMaxSizeKB");
        selectItem(myTester, creationForm, maxNbMsgPerDay, "maxNbMsgPerDay");
        selectItem(myTester, creationForm, nbRetentionDay, "nbRetentionDay");
        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createOnlineStorage(PaasWicketTester myTester, String name, String jndiPrefix, int storageCapacityMb) {

        selectService(myTester, LogicalOnlineStorageService.class);
        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", name);
        creationForm.setValue("serviceName", jndiPrefix);
        creationForm.setValue("storageCapacityMb", String.valueOf(storageCapacityMb));
        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createConfig(PaasWicketTester myTester, String name, String jndiPrefix, String content) {

        selectService(myTester, LogicalConfigService.class);
        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", name);
        creationForm.setValue("keyPrefix", jndiPrefix);
        creationForm.setValue("configSetContent", content);
        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createMom(PaasWicketTester myTester, String label, String destinationName, int destinationCapacity, String deadLetterQueueName) {

        selectService(myTester, LogicalMomService.class);
        FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
        creationForm.setValue("label", label);
        creationForm.setValue("destinationName", destinationName);
        creationForm.setValue("destinationCapacity", String.valueOf(destinationCapacity));
        creationForm.setValue("deadLetterQueueName", deadLetterQueueName);
        NavigationUtils.submitParamsForm(myTester);

    }

    public static void createAssociationAtCell(PaasWicketTester myTester, int row, int col) {

        String path = NavigationUtils.getPathForCell(row, col) + ":form";
        FormTester cellForm = myTester.newFormTester(path);
        cellForm.setValue("associated", true);
        myTester.executeAjaxEvent(path + ":associated", "onclick");

    }

    public static void selectService(PaasWicketTester myTester, Class<? extends LogicalModelItem> serviceClass) {

        List list = ((ListChoice)myTester.getComponentFromLastRenderedPage(NavigationUtils.selectFormPath + ":" + NavigationUtils.selectServicePath)).getChoices();
        for (Object item : list) {
            if (item.getClass().equals(serviceClass)) {
                selectService(myTester, list.indexOf(item));
            }
        }
    }

    /**
     * Selects a service in the ListChoice of services
     * @param index index of the service in the ListChoice
     */
    private static void selectService(PaasWicketTester myTester, int index) {

        FormTester formTester = myTester.newFormTester(NavigationUtils.selectFormPath);
        formTester.select(NavigationUtils.selectServicePath, index);
        myTester.executeAjaxEvent(formPath + ":logicalServicesListSelect", "onchange");

        myTester.isVisible("firstPartContainer:serviceDefinitionPanel:container");
    }

    private static void simpleSelect(PaasWicketTester myTester, FormTester form, String id, int index) {

        form.select(id, index);
        myTester.executeAjaxEvent(paramsFormPath + ":" + id, "onchange");

    }

    private static void selectItem(PaasWicketTester myTester, FormTester form, Object item, String id) {

        List choices = ((DropDownChoice)myTester.getComponentFromLastRenderedPage(paramsFormPath + ":" + id)).getChoices();
        int index = choices.indexOf(item);
        form.select(id, index);

    }

    public static void createRabbitMQService(PaasWicketTester myTester,
    		String label, String serviceName) {
    	 selectService(myTester, LogicalRabbitService.class);
         FormTester creationForm = NavigationUtils.getParamsFormTester(myTester);
         creationForm.setValue("label", label);
         creationForm.setValue("serviceName", serviceName);
         NavigationUtils.submitParamsForm(myTester);
    }
}
