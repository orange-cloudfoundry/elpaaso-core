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
package com.francetelecom.clara.cloud.presentation.designer.services.queuesend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.logicalmodel.LogicalQueueSendService;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 09/01/12
 */
public class LogicalQueueSendServicePanel extends LogicalServiceBasePanel<LogicalQueueSendService> {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(LogicalQueueSendServicePanel.class.getName());

    private DropDownChoice<String> targetApplicationName;
    private DropDownChoice<String> targetApplicationVersion;
    private DropDownChoice<String> targetServiceName;
    private DropDownChoice<String> targetServiceVersion;

    private List<String> applicationNameList;
    private List<String> applicationVersionList;
    private List<String> serviceNameList;
    private List<String> serviceVersionList;

    private static final Long[] maxMsgSizeList = {1L, 5L, 10L};
    private static final Long[] maxNbMsgPerDayList = {1L, 2L, 5L, 10L, 50L};
    private static final Long[] nbRetentionDayList = {1L, 2L, 3L, 4L, 5L};

    private DesignerPage parentPage;

    public LogicalQueueSendServicePanel(String id, IModel<LogicalQueueSendService> model, Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);

        this.parentPage = (DesignerPage)parentPage;
        initLists();

        initComponents();
    }

    private void initComponents() {

//    	getServiceForm().add(new CacheActivatedImage("logicalqueuesendServicePanelIcon",new ResourceModel("queuesend.icon").getObject()));
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
        label.setLabel(new StringResourceModel("portal.designer.service.queue_send.label",null));
        label.add(new PropertyValidator<>());
        getServiceForm().add(label);

        List<String> basicatCodeList = new ArrayList<String>();
        DropDownChoice<String> targetBasicatCode = new DropDownChoice<String>("targetBasicatCode", basicatCodeList);
        targetBasicatCode.setLabel(new StringResourceModel("portal.designer.service.queue_send.targetBasicatCode",null));
        targetBasicatCode.setEnabled(false);
        targetBasicatCode.add(new PropertyValidator<>());
        getServiceForm().add(targetBasicatCode);

        /* prevent null errors */
        List<String> emptyList = new ArrayList<>();

        targetApplicationName = new DropDownChoice<>("targetApplicationName", (applicationNameList != null) ? applicationNameList : emptyList);
        targetApplicationName.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                List<String> list = parentPage.getQrsApplicationVersions("cloud", targetApplicationName.getDefaultModelObjectAsString());
                targetApplicationVersion.setChoices(list);
                if (list == null) {
                    targetApplicationVersion.warn("no qrs application version found.");
                }
                target.add(targetApplicationVersion);
            }
        });
//        targetApplicationName.setLabel(new StringResourceModel("portal.designer.service.queue_send.targetApplicationName",null));

        if (applicationNameList == null) {
            targetApplicationName.warn("no qrs application found.");
        }
        targetApplicationName.add(new PropertyValidator<>());
        getServiceForm().add(targetApplicationName);

        targetApplicationVersion = new DropDownChoice<>("targetApplicationVersion", (applicationVersionList != null) ? applicationVersionList : emptyList);
//        targetApplicationVersion.setLabel(new StringResourceModel("portal.designer.service.queue_send.targetApplicationVersion",null));
        targetApplicationVersion.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                List list = parentPage.getQrsServices("cloud", targetApplicationName.getDefaultModelObjectAsString(), targetApplicationVersion.getDefaultModelObjectAsString());
                targetServiceName.setChoices(list);
                if (list == null) {
                    targetServiceName.warn("no qrs service version found.");
                }
                target.add(targetServiceName);
            }
        });
        targetApplicationVersion.add(new PropertyValidator<>());
        getServiceForm().add(targetApplicationVersion);

        targetServiceName = new DropDownChoice<String>("targetServiceName", (serviceNameList != null) ? serviceNameList : emptyList);
//        targetServiceName.setLabel(new StringResourceModel("portal.designer.service.queue_send.targetServiceName",null));
        targetServiceName.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                List list = parentPage.getQrsServicesVersions("cloud", targetApplicationName.getDefaultModelObjectAsString(), targetApplicationVersion.getDefaultModelObjectAsString(), targetServiceName.getDefaultModelObjectAsString());
                targetServiceVersion.setChoices(list);
                if (list == null) {
                    targetServiceVersion.warn("no qrs service version found.");
                }
                target.add(targetServiceVersion);
            }
        });
        targetServiceName.add(new PropertyValidator<>());
        getServiceForm().add(targetServiceName);

        targetServiceVersion = new DropDownChoice<String>("targetServiceVersion", (serviceVersionList != null) ? serviceVersionList : emptyList);
//        targetServiceVersion.setLabel(new StringResourceModel("portal.designer.service.queue_send.targetServiceVersion",null));

        //Forces update
        targetServiceVersion.add(defaultDropDownUpdateBehavior());
        targetServiceVersion.add(new PropertyValidator<>());
        getServiceForm().add(targetServiceVersion);

        TextField<String> jndiQueueName = new TextField<String>("jndiQueueName");
        jndiQueueName.setLabel(new StringResourceModel("portal.designer.service.queue_send.jndiQueueName",null));
        jndiQueueName.add(new PropertyValidator<>());
        getServiceForm().add(jndiQueueName);

        DropDownChoice<Long> msgMaxSizeKB = new DropDownChoice<Long>("msgMaxSizeKB", Arrays.asList(maxMsgSizeList));
        msgMaxSizeKB.add(defaultDropDownUpdateBehavior());
        msgMaxSizeKB.add(new PropertyValidator<>());
        getServiceForm().add(msgMaxSizeKB);
        msgMaxSizeKB.setLabel(new StringResourceModel("portal.designer.service.queue_send.msgMaxSizeKB",null));

        DropDownChoice<Long> maxNbMsgPerDay = new DropDownChoice<Long>("maxNbMsgPerDay",Arrays.asList(maxNbMsgPerDayList));
        maxNbMsgPerDay.add(defaultDropDownUpdateBehavior());
        maxNbMsgPerDay.add(new PropertyValidator<>());
        getServiceForm().add(maxNbMsgPerDay);
        maxNbMsgPerDay.setLabel(new StringResourceModel("portal.designer.service.queue_send.maxNbMsgPerDay",null));

        DropDownChoice<Long> nbRetentionDay = new DropDownChoice<Long>("nbRetentionDay", Arrays.asList(nbRetentionDayList));
        nbRetentionDay.add(defaultDropDownUpdateBehavior());
        nbRetentionDay.add(new PropertyValidator<>());
        getServiceForm().add(nbRetentionDay);
        nbRetentionDay.setLabel(new StringResourceModel("portal.designer.service.queue_send.nbRetentionDay",null));

    }

    private void initLists() {

        applicationNameList = parentPage.getQrsApplications("cloud");
        String applicationName = getModelObject().getTargetApplicationName();
        String applicationVersion = getModelObject().getTargetApplicationVersion();
        String serviceName = getModelObject().getTargetServiceName();

		if (applicationName != null) {
            applicationVersionList = parentPage.getQrsApplicationVersions("cloud", applicationName);
        }
		if (applicationVersion != null) {
            serviceNameList = parentPage.getQrsServices("cloud", applicationName, applicationVersion);
        }
		if (serviceName != null) {
            serviceVersionList = parentPage.getQrsServicesVersions("cloud", applicationName, applicationVersion, serviceName);
        }
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
