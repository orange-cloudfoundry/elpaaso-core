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
package com.francetelecom.clara.cloud.presentation.designer.services.internalMom;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import com.francetelecom.clara.cloud.logicalmodel.LogicalMomService;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;

/**
 * 
 * @author shjn2064
 */
public class LogicalInternalMomServicePanel extends LogicalServiceBasePanel<LogicalMomService> {


    private AjaxCheckBox hasDeadLetterQueue;
    private TextField<String> deadLetterQueueName;
    private TextField deadLetterQueueCapacity;
    private TextField retriesBeforeMovingToDeadLetterQueue;

    public LogicalInternalMomServicePanel(String id, IModel<LogicalMomService> model, Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);
        initComponents();
    }

    private void initComponents() {

    	getServiceForm().add(new CacheActivatedImage("logicalInternalMomIcon",new ResourceModel("internalMom-icon").getObject()));
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
        serviceLabel.setLabel(new StringResourceModel("portal.designer.service.mom.ml.label",null));
        serviceLabel.add(new PropertyValidator<>());
        getServiceForm().add(serviceLabel);

        TextField<String> destinationName = new TextField<String>("destinationName");
        //add help tooltip
        destinationName.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.mom.ml.destinationName.help",null)));
        destinationName.setLabel(new StringResourceModel("portal.designer.service.mom.ml.destinationName",null));
        destinationName.add(new PropertyValidator<>());
        getServiceForm().add(destinationName);

        // Connection factory JNDI Name
        TextField<String> jmsConnectionFactoryJndiName = new TextField<String>("jmsConnectionFactoryJndiName");
         //add help tooltip
        jmsConnectionFactoryJndiName.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.mom.ml.jmsConnectionFactoryJndiName.help",null)));
        jmsConnectionFactoryJndiName.setLabel(new StringResourceModel("portal.designer.service.mom.ml.jmsConnectionFactoryJndiName",null));
        jmsConnectionFactoryJndiName.add(new PropertyValidator<>());
        getServiceForm().add(jmsConnectionFactoryJndiName);

        // Non Functionnal attributes
        // Max size Kb
        TextField msgMaxSizeKB = new TextField("msgMaxSizeKB");
         //add help tooltip
        msgMaxSizeKB.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.mom.ml.msgMaxSizeKB.help",null)));
        msgMaxSizeKB.setLabel(new StringResourceModel("portal.designer.service.mom.ml.msgMaxSizeKB",null));
        msgMaxSizeKB.add(new PropertyValidator<>());
        getServiceForm().add(msgMaxSizeKB);

         // destination capacity (nb msg)
        TextField destinationCapacity = new TextField("destinationCapacity");
          //add help tooltip
        destinationCapacity.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.mom.ml.destinationCapacity.help",null)));
        destinationCapacity.setLabel(new StringResourceModel("portal.designer.service.mom.ml.destinationCapacity",null));
        destinationCapacity.add(new PropertyValidator<>());
        getServiceForm().add(destinationCapacity);

        // Persistent message used
        CheckBox persistentMessagesUsed = new CheckBox("persistentMessagesUsed");
        persistentMessagesUsed.setLabel(new StringResourceModel("portal.designer.service.mom.ml.persistentMessagesUsed",null));
        persistentMessagesUsed.add(new PropertyValidator<>());
        getServiceForm().add(persistentMessagesUsed);

        // High availability
        CheckBox highAvailability = new CheckBox("highAvailability");
        highAvailability.setLabel(new StringResourceModel("portal.designer.service.mom.ml.highAvailability",null));
        highAvailability.setEnabled(false);
           //add help tooltip
        highAvailability.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.mom.ml.highAvailability.help",null)));
        highAvailability.add(new PropertyValidator<>());
        getServiceForm().add(highAvailability);

       // Dead Letter
         // jndi queue name
        deadLetterQueueName = new TextField<String>("deadLetterQueueName", String.class);
        //add help tooltip
        deadLetterQueueName.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.mom.dl.deadLetterQueueName.help",null)));
        deadLetterQueueName.setOutputMarkupId(true);
        deadLetterQueueName.add(new PropertyValidator<>());
//        deadLetterQueueName.setLabel(new StringResourceModel("portal.designer.service.mom.dl.deadLetterQueueName",null));
       

         // queue capacity
        deadLetterQueueCapacity = new TextField("deadLetterQueueCapacity");
        //add help tooltip
        deadLetterQueueCapacity.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.mom.dl.deadLetterQueueCapacity.help",null)));
        deadLetterQueueCapacity.setOutputMarkupId(true);
        deadLetterQueueCapacity.add(new PropertyValidator<>());
//        deadLetterQueueCapacity.setLabel(new StringResourceModel("portal.designer.service.mom.dl.deadLetterQueueCapacity",null));
        

         // retries before deadqueue
        retriesBeforeMovingToDeadLetterQueue = new TextField("retriesBeforeMovingToDeadLetterQueue");
        //add help tooltip
        retriesBeforeMovingToDeadLetterQueue.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.mom.dl.retriesBeforeMovingToDeadLetterQueue.help",null)));
//        retriesBeforeMovingToDeadLetterQueue.setLabel(new StringResourceModel("portal.designer.service.mom.dl.retriesBeforeMovingToDeadLetterQueue",null));
        retriesBeforeMovingToDeadLetterQueue.setOutputMarkupId(true);
        retriesBeforeMovingToDeadLetterQueue.add(new PropertyValidator<>());

        // enable dead letter queue
        hasDeadLetterQueue = new AjaxCheckBox("hasDeadLetterQueue") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                deadLetterQueueName.setEnabled(getModelObject());
                deadLetterQueueCapacity.setEnabled(getModelObject());
                retriesBeforeMovingToDeadLetterQueue.setEnabled(getModelObject());

                target.add(deadLetterQueueName);
                target.add(deadLetterQueueCapacity);
                target.add(retriesBeforeMovingToDeadLetterQueue);
            }
        };
        hasDeadLetterQueue.setLabel(new StringResourceModel("portal.designer.service.mom.dl.hasDeadLetterQueue",null));
		hasDeadLetterQueue.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.mom.dl.hasDeadLetterQueue.help", null)));
        getServiceForm().add(hasDeadLetterQueue);

        if (hasDeadLetterQueue.getModelObject() != null) {
            deadLetterQueueName.setEnabled(hasDeadLetterQueue.getModelObject());
            deadLetterQueueCapacity.setEnabled(hasDeadLetterQueue.getModelObject());
            retriesBeforeMovingToDeadLetterQueue.setEnabled(hasDeadLetterQueue.getModelObject());
        }
        
        getServiceForm().add(retriesBeforeMovingToDeadLetterQueue);
        getServiceForm().add(deadLetterQueueCapacity);
        getServiceForm().add(deadLetterQueueName);

    }

}
