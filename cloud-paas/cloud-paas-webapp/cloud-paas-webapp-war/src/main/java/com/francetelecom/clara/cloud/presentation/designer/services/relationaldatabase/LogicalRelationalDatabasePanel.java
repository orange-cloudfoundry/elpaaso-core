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
package com.francetelecom.clara.cloud.presentation.designer.services.relationaldatabase;

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

import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalRelationalServiceSqlDialectEnum;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 24/01/12
 */
public class LogicalRelationalDatabasePanel extends LogicalServiceBasePanel<LogicalRelationalService> {

    public LogicalRelationalDatabasePanel(String id, IModel<LogicalRelationalService> model, final Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);
        initComponents();
    }

    private void initComponents() {
    	 getServiceForm().add(new CacheActivatedImage("logicalRelationnalDatabasePanelIcon",new ResourceModel("onlinedatabase.icon").getObject()));
    	// Online help link
    	String completeHelpUrl = "";
    	try {
    		completeHelpUrl = new StringResourceModel("portal.designer.logical.service.online_manual.baseUrl", null).getString() 
        			+ new StringResourceModel("portal.designer.logical.service.online_manual.relationaldatabase", null).getString();
    	} catch (Exception e) {
    		//do nothing
    	}
    	ExternalLink onlineHelpLink = new ExternalLink("onlineHelpLink", completeHelpUrl);
    	getServiceForm().add(onlineHelpLink);
    	if (completeHelpUrl.isEmpty()) {
    		onlineHelpLink.setVisible(false);
    	}
    	
        // FUNCTIONNAL PARAMETERS
        final RequiredTextField<String> label = new RequiredTextField<String>("label");
        label.setLabel(new StringResourceModel("portal.designer.service.reldb.label",null));
        label.add(new PropertyValidator<>());
        getServiceForm().add(label);

        TextField<String> serviceName = new TextField<String>("serviceName");
        serviceName.setLabel(new StringResourceModel("portal.designer.service.reldb.serviceName",null));
        serviceName.add(new PropertyValidator<>());
        getServiceForm().add(serviceName);

        DropDownChoice<LogicalRelationalServiceSqlDialectEnum> sqlVersion = new DropDownChoice<LogicalRelationalServiceSqlDialectEnum>("sqlVersion", Arrays.asList(LogicalRelationalServiceSqlDialectEnum.values()));
        sqlVersion.setLabel(new StringResourceModel("portal.designer.service.reldb.sqlVersion",null));
        sqlVersion.add(new PropertyValidator<>());
        getServiceForm().add(sqlVersion);


        // SLO
        TextField capacityMo = new TextField("capacityMo");
        capacityMo.setLabel(new StringResourceModel("portal.designer.service.reldb.capacityMo",null));
        capacityMo.add(new AttributeModifier("class","small"));
        capacityMo.add(new PropertyValidator<>());
        getServiceForm().add(capacityMo);
    }

}
