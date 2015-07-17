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
package com.francetelecom.clara.cloud.presentation.designer.services.webserviceconsumer;

import java.util.Arrays;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapConsumer;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 24/01/12
 */
public class LogicalSoapConsumerPanel extends LogicalServiceBasePanel<LogicalSoapConsumer> {

    public LogicalSoapConsumerPanel(String id, IModel<LogicalSoapConsumer> model, final Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);
        initComponent();
    }

    private void initComponent() {

    	getServiceForm().add(new CacheActivatedImage("logicalSoapConsummerPanelIcon",new ResourceModel("webserviceconsumer.icon").getObject()));
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
        label.setLabel(new StringResourceModel("portal.designer.service.wsc_soap.label",null));
        label.add(new PropertyValidator<>());
        getServiceForm().add(label);

        TextField<String> jndiPrefix = new TextField<String>("jndiPrefix");
        jndiPrefix.setLabel(new StringResourceModel("portal.designer.service.wsc_soap.jndiPrefix",null));
        jndiPrefix.add(new PropertyValidator<>());
        getServiceForm().add(jndiPrefix);

        DropDownChoice<LogicalSoapConsumer.SoapServiceDomainEnum> domain = new DropDownChoice<LogicalSoapConsumer.SoapServiceDomainEnum>("wsDomain", Arrays.asList(LogicalSoapConsumer.SoapServiceDomainEnum.values()));
        domain.setLabel(new StringResourceModel("portal.designer.service.wsc_soap.wsdomain",null));
        domain.add(new PropertyValidator<>());
        getServiceForm().add(domain);

        TextField<String> serviceProviderName = new TextField<String>("serviceProviderName");
        serviceProviderName.setLabel(new StringResourceModel("portal.designer.service.wsc_soap.serviceProviderName",null));
        serviceProviderName.add(new PropertyValidator<>());
        getServiceForm().add(serviceProviderName);

        TextField<String> serviceName = new TextField<String>("serviceName");
        serviceName.setLabel(new StringResourceModel("portal.designer.service.wsc_soap.serviceName",null));
        serviceName.add(new PropertyValidator<>());
        getServiceForm().add(serviceName);

        TextField serviceMinorVersion = new TextField("serviceMinorVersion");
        serviceMinorVersion.setLabel(new StringResourceModel("portal.designer.service.wsc_soap.serviceMinorVersion",null));
        serviceMinorVersion.add(new AttributeModifier("class","small"));
        serviceMinorVersion.add(new PropertyValidator<>());
        getServiceForm().add(serviceMinorVersion);

        TextField serviceMajorVersion = new TextField("serviceMajorVersion");
        serviceMajorVersion.setLabel(new StringResourceModel("portal.designer.service.wsc_soap.serviceMajorVersion",null));
        serviceMajorVersion.add(new AttributeModifier("class","small"));
        serviceMajorVersion.add(new PropertyValidator<>());
        getServiceForm().add(serviceMajorVersion);

    }

}
