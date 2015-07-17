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
package com.francetelecom.clara.cloud.presentation.designer.services.rabbitmq;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.logicalmodel.LogicalRabbitService;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;

/**
 * 
 * @author amcu6536
 */
public class LogicalRabbitMQServicePanel extends LogicalServiceBasePanel<LogicalRabbitService> {

	private static final long serialVersionUID = 8968010530409127526L;
	private static final Logger logger = LoggerFactory.getLogger(LogicalRabbitMQServicePanel.class);

	public LogicalRabbitMQServicePanel(String id, IModel<LogicalRabbitService> model, Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);
        initComponents();
    }

    private void initComponents() {

    	getServiceForm().add(new CacheActivatedImage("logicalRabbitMQPanelIcon",new ResourceModel("rabbitmq.icon").getObject()));
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
    	RequiredTextField<String> serviceLabel = new RequiredTextField<String>("label");
        serviceLabel.setLabel(new StringResourceModel("portal.designer.service.rabbitmq.service.label",null));
        serviceLabel.add(new PropertyValidator<>());
        getServiceForm().add(serviceLabel);

        // Cloudfoundry rabbitMQ service name
        RequiredTextField<String> serviceName = new RequiredTextField<String>("serviceName");
         //add help tooltip
        serviceName.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.rabbitmq.service.name.help",null)));
        serviceName.setLabel(new StringResourceModel("portal.designer.service.rabbitmq.service.name",null));
        serviceName.add(new PropertyValidator<>());
        getServiceForm().add(serviceName);
       
     

    }

}
