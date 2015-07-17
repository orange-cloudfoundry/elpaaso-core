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
package com.francetelecom.clara.cloud.presentation.designer.services.queuereceive;

import java.util.Arrays;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import com.francetelecom.clara.cloud.logicalmodel.LogicalQueueReceiveService;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 23/01/12
 */
public class LogicalQueueReceiveServicePanel extends LogicalServiceBasePanel<LogicalQueueReceiveService> {

    private static final Long[] maxMsgSizeList = {1L, 5L, 10L};
    private static final Long[] maxNbMsgPerDayList = {1L, 2L, 5L, 10L, 50L};
    private static final Long[] nbRetentionDayList = {1L, 2L, 3L, 4L, 5L};


    public LogicalQueueReceiveServicePanel(String id, IModel<LogicalQueueReceiveService> model, Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);

        getServiceForm().add(new CacheActivatedImage("logicalqueuereceiveServicePanelIcon",new ResourceModel("queuereceive.icon").getObject()));
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
        label.setLabel(new StringResourceModel("portal.designer.service.queue_receive.label",null));
        label.add(new PropertyValidator<>());
        getServiceForm().add(label);

        TextField<String> serviceName = new TextField<String>("serviceName");
        serviceName.setLabel(new StringResourceModel("portal.designer.service.queue_receive.serviceName",null));
        serviceName.add(new PropertyValidator<>());
        getServiceForm().add(serviceName);

        TextField serviceVersion = new TextField("serviceVersion");
        serviceVersion.setLabel(new StringResourceModel("portal.designer.service.queue_receive.serviceVersion",null));
        serviceVersion.add(new PropertyValidator<>());
        getServiceForm().add(serviceVersion);

        TextField<String> jndiQueueName = new TextField<String>("jndiQueueName");
        jndiQueueName.setLabel(new StringResourceModel("portal.designer.service.queue_receive.jndiQueueName",null));
        jndiQueueName.add(new AttributeAppender("title", new StringResourceModel("portal.designer.service.queue_receive.preferredJNDI", null), " "));
        jndiQueueName.add(new PropertyValidator<>());
        getServiceForm().add(jndiQueueName);

        DropDownChoice<Long> msgMaxSizeKB = new DropDownChoice<Long>("msgMaxSizeKB", Arrays.asList(maxMsgSizeList));
        msgMaxSizeKB.add(defaultDropDownUpdateBehavior());
        getServiceForm().add(msgMaxSizeKB);
        msgMaxSizeKB.setLabel(new StringResourceModel("portal.designer.service.queue_receive.msgMaxSizeKB",null));
        msgMaxSizeKB.add(new PropertyValidator<>());

        DropDownChoice<Long> maxNbMsgPerDay = new DropDownChoice<Long>("maxNbMsgPerDay", Arrays.asList(maxNbMsgPerDayList));
        maxNbMsgPerDay.add(defaultDropDownUpdateBehavior());
        getServiceForm().add(maxNbMsgPerDay);
        maxNbMsgPerDay.setLabel(new StringResourceModel("portal.designer.service.queue_receive.maxNbMsgPerDay",null));
        maxNbMsgPerDay.add(new PropertyValidator<>());

        DropDownChoice<Long> nbRetentionDay = new DropDownChoice<Long>("nbRetentionDay", Arrays.asList(nbRetentionDayList));
        nbRetentionDay.add(defaultDropDownUpdateBehavior());
        getServiceForm().add(nbRetentionDay);
        nbRetentionDay.setLabel(new StringResourceModel("portal.designer.service.queue_receive.nbRetentionDay",null));
        nbRetentionDay.add(new PropertyValidator<>());
    }

    /**
     * Behavior which does nothing but allows the update of a dropdown correctly
     * @return a behavior which fixes the update of dropdowns
     */
    private Behavior defaultDropDownUpdateBehavior() {
        return new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
            }
        };
    }
}
