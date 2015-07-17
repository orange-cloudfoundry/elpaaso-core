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
package com.francetelecom.clara.cloud.presentation.designer.services.webgui;

import com.francetelecom.clara.cloud.logicalmodel.LogicalWebGUIService;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 03/01/12
 */
public class LogicalWebGuiServicePanel extends LogicalServiceBasePanel<LogicalWebGUIService> {

    public LogicalWebGuiServicePanel(String id, IModel<LogicalWebGUIService> model, Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);
        initComponents();
    }

    private void initComponents() {

    	getServiceForm().add(new CacheActivatedImage("logicalWebGuiPanelIcon",new ResourceModel("webui.icon").getObject()));
    	// Online help link
    	String completeHelpUrl = "";
    	try {
    		completeHelpUrl = new StringResourceModel("portal.designer.logical.service.online_manual.baseUrl", null).getString() 
        			+ new StringResourceModel("portal.designer.logical.service.online_manual.webgui", null).getString();
    	} catch (Exception e) {
    		//do nothing
    	}
    	ExternalLink onlineHelpLink = new ExternalLink("onlineHelpLink", completeHelpUrl);
    	getServiceForm().add(onlineHelpLink);
    	if (completeHelpUrl.isEmpty()) {
    		onlineHelpLink.setVisible(false);
    	}
    	
        // FUNCTIONNAL PARAMETERS
        // Service label
        RequiredTextField<String> label = new RequiredTextField<String>("label");
        label.setLabel(new StringResourceModel("portal.designer.service.gui.label",null));
        label.add(new PropertyValidator<>());
        getServiceForm().add(label);

        // Service context root
        RequiredTextField<String> contextRoot = new RequiredTextField<String>("contextRoot.value");
        contextRoot.setLabel(new StringResourceModel("portal.designer.service.gui.contextroot",null));
        contextRoot.add(new PropertyValidator<>());
        getServiceForm().add(contextRoot);

        // Service stateful state
        CheckBox statefulCb = new CheckBox("stateful");
        statefulCb.setLabel(new StringResourceModel("portal.designer.service.gui.stateful",null));
        statefulCb.add(new PropertyValidator<>());
        getServiceForm().add(statefulCb);

        // SLO
        // Service secure state
        CheckBox secureCb = new CheckBox("secure");
//        secureCb.setEnabled(false);
        secureCb.setLabel(new StringResourceModel("portal.designer.service.gui.secure",null));
        secureCb.add(new AttributeAppender("title", new StringResourceModel("portal.designer.service.notAvailable", null), " "));
        secureCb.add(new PropertyValidator<>());
        getServiceForm().add(secureCb);

        // Service max number of sessions
        TextField maxNumberSessions = new TextField("maxNumberSessions");
        maxNumberSessions.setLabel(new StringResourceModel("portal.designer.service.gui.maxNumberSessions",null));
        maxNumberSessions.add(new AttributeModifier("class", "small"));
        maxNumberSessions.add(new PropertyValidator<>());
        getServiceForm().add(maxNumberSessions);

        // Service max request per seconds
        final TextField maxReqPerSeconds = new TextField("maxReqPerSeconds");
        maxReqPerSeconds.setLabel(new StringResourceModel("portal.designer.service.gui.maxReqPerSeconds",null));
        maxReqPerSeconds.add(new AttributeAppender("title", new StringResourceModel("portal.designer.service.notAvailable", null), " "));
        maxReqPerSeconds.add(new PropertyValidator<>());
        getServiceForm().add(maxReqPerSeconds);

    }



}
