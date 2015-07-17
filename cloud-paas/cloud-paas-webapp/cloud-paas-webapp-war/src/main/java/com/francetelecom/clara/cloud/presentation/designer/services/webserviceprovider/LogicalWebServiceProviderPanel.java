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
package com.francetelecom.clara.cloud.presentation.designer.services.webserviceprovider;

import java.util.Arrays;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import com.francetelecom.clara.cloud.logicalmodel.LogicalAccessZoneEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalAttachmentTypeEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalAuthenticationType;
import com.francetelecom.clara.cloud.logicalmodel.LogicalIdentityPropagationEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalProtocolEnum;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;

public class LogicalWebServiceProviderPanel extends LogicalServiceBasePanel<LogicalSoapService> {

    public LogicalWebServiceProviderPanel(String id, IModel<LogicalSoapService> model, final Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);
        initComponent();
    }

    private void initComponent() {

    	getServiceForm().add(new CacheActivatedImage("logicalWebServiceProviderPanelIcon",new ResourceModel("webserviceprovider.icon").getObject()));
    	// Online help link
    	String completeHelpUrl = "";
    	try {
    		completeHelpUrl = new StringResourceModel("portal.designer.logical.service.online_manual.baseUrl", null).getString() 
        			+ new StringResourceModel("portal.designer.logical.service.online_manual." + getLogicalModelType(), null).getString();
    	} catch (Exception e) {
    		//do nothing
    	}
    	ExternalLink onlineHelpLink = new ExternalLink("onlineHelpLink", completeHelpUrl);
    	getServiceForm().add(onlineHelpLink);
    	if (completeHelpUrl.isEmpty()) {
    		onlineHelpLink.setVisible(false);
    	}
    	
        // FUNCTIONNAL PARAMETERS
        RequiredTextField<String> label = new RequiredTextField<String>("label");
        label.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.label",null));
        label.add(new PropertyValidator<>());
        getServiceForm().add(label);

        TextField<String> jndiPrefix = new TextField<String>("jndiPrefix");
        jndiPrefix.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.jndiPrefix",null));
        jndiPrefix.add(new PropertyValidator<>());
        getServiceForm().add(jndiPrefix);

        TextField<String> serviceName = new TextField<String>("serviceName");
        serviceName.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.serviceName",null));
        serviceName.add(new PropertyValidator<>());
        getServiceForm().add(serviceName);

        TextField serviceMinorVersion = new TextField("serviceMinorVersion");
        serviceMinorVersion.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.serviceMinorVersion",null));
        serviceMinorVersion.add(new AttributeModifier("class","small"));
        serviceMinorVersion.add(new PropertyValidator<>());
        getServiceForm().add(serviceMinorVersion);

        TextField serviceMajorVersion = new TextField("serviceMajorVersion");
        serviceMajorVersion.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.serviceMajorVersion",null));
        serviceMajorVersion.add(new AttributeModifier("class","small"));
        serviceMajorVersion.add(new PropertyValidator<>());
        getServiceForm().add(serviceMajorVersion);

        TextArea<String> description = new TextArea<String>("description");
        description.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.description",null));
        description.add(new PropertyValidator<>());
        getServiceForm().add(description);

        DropDownChoice<LogicalAttachmentTypeEnum> serviceAttachmentType = new DropDownChoice<LogicalAttachmentTypeEnum>("serviceAttachmentType", Arrays.asList(LogicalAttachmentTypeEnum.values()));
        serviceAttachmentType.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.serviceAttachmentType",null));
        serviceAttachmentType.add(new PropertyValidator<>());
        getServiceForm().add(serviceAttachmentType);

        TextField<String> contextRoot = new TextField<String>("contextRoot.value");
        contextRoot.setRequired(true);
        contextRoot.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.contextRoot",null));
        contextRoot.add(new PropertyValidator<>());
        getServiceForm().add(contextRoot);
        
        TextField<String> servicePath = new TextField<String>("servicePath.value");
        servicePath.setRequired(true);
        servicePath.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.servicePath",null));
        servicePath.add(new PropertyValidator<>());
        getServiceForm().add(servicePath);

        //InboundAuthenticationPanel
        DropDownChoice<LogicalAccessZoneEnum> accessZone = new DropDownChoice<LogicalAccessZoneEnum>("inboundAuthenticationPolicy.accessZone", Arrays.asList(LogicalAccessZoneEnum.values()));
        accessZone.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.inboundAuthenticationPolicy.accessZone",null));
        accessZone.add(new PropertyValidator<>());
        getServiceForm().add(accessZone);

        DropDownChoice<LogicalAuthenticationType> inboundAuthenticationTypeDropDownChoice = new DropDownChoice<LogicalAuthenticationType>("inboundAuthenticationPolicy.authenticationType", Arrays.asList(LogicalAuthenticationType.values()));
        inboundAuthenticationTypeDropDownChoice.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.inboundAuthenticationPolicy.authenticationType",null));
        inboundAuthenticationTypeDropDownChoice.add(new PropertyValidator<>());
        getServiceForm().add(inboundAuthenticationTypeDropDownChoice);

        DropDownChoice<LogicalProtocolEnum> inboundProtocolEnumDropDownChoice = new DropDownChoice<LogicalProtocolEnum>("inboundAuthenticationPolicy.protocol", Arrays.asList(LogicalProtocolEnum.values()));
        inboundProtocolEnumDropDownChoice.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.inboundAuthenticationPolicy.protocol",null));
        inboundProtocolEnumDropDownChoice.add(new PropertyValidator<>());
        getServiceForm().add(inboundProtocolEnumDropDownChoice);

        //OutboundAuthenticationPanel
        DropDownChoice<LogicalAuthenticationType> outboundAuthenticationTypeDropDownChoice = new DropDownChoice<LogicalAuthenticationType>("outboundAuthenticationPolicy.authenticationType", Arrays.asList(LogicalAuthenticationType.values()));
        outboundAuthenticationTypeDropDownChoice.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.outboundAuthenticationPolicy.authenticationType",null));
        outboundAuthenticationTypeDropDownChoice.add(new PropertyValidator<>());
        getServiceForm().add(outboundAuthenticationTypeDropDownChoice);

        DropDownChoice<LogicalProtocolEnum> outboundProtocolEnumDropDownChoice = new DropDownChoice<LogicalProtocolEnum>("outboundAuthenticationPolicy.protocol", Arrays.asList(LogicalProtocolEnum.values()));
        outboundProtocolEnumDropDownChoice.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.outboundAuthenticationPolicy.protocol",null));
        outboundProtocolEnumDropDownChoice.add(new PropertyValidator<>());
        getServiceForm().add(outboundProtocolEnumDropDownChoice);

        DropDownChoice<LogicalIdentityPropagationEnum> logicalIdentityPropagation = new DropDownChoice<LogicalIdentityPropagationEnum>("identityPropagation", Arrays.asList(LogicalIdentityPropagationEnum.values()));
        logicalIdentityPropagation.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.identityPropagation",null));
        logicalIdentityPropagation.add(new PropertyValidator<>());
        getServiceForm().add(logicalIdentityPropagation);

        // Modifications not yet possible for followings fields
        accessZone.setEnabled(false);
        inboundAuthenticationTypeDropDownChoice.setEnabled(false);
        inboundProtocolEnumDropDownChoice.setEnabled(false);
        outboundAuthenticationTypeDropDownChoice.setEnabled(false);
        outboundProtocolEnumDropDownChoice.setEnabled(false);
        logicalIdentityPropagation.setEnabled(false);


        TextField<String> rootFileName = new TextField<String>("rootFileName");
        rootFileName.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.rootFileName",null));
        rootFileName.add(new AttributeModifier("title",new StringResourceModel("portal.designer.service.wsp_soap.serviceWsdlFile.help",null)));
        rootFileName.add(new PropertyValidator<>());
        getServiceForm().add(rootFileName);

        TextField<String> groupId = new RequiredTextField<String>("serviceAttachments.groupId");
        groupId.setRequired(true);
        groupId.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.groupId",null));
        groupId.add(new PropertyValidator<>());
        getServiceForm().add(groupId);

        TextField<String> artifactId = new RequiredTextField<String>("serviceAttachments.artifactId");
        artifactId.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.artifactId",null));
        artifactId.setRequired(true);
        artifactId.add(new PropertyValidator<>());
        getServiceForm().add(artifactId);

        TextField version = new RequiredTextField("serviceAttachments.version");
        version.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.version",null));
        version.setRequired(true);
        version.add(new PropertyValidator<>());
        getServiceForm().add(version);

        TextField classifier = new TextField("serviceAttachments.classifier", String.class);
        classifier.setLabel(new StringResourceModel("portal.designer.service.wsp_soap.classifier",null));
        classifier.add(new PropertyValidator<>());
        getServiceForm().add(classifier);

    }
}
