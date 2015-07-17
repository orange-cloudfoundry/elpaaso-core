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
package com.francetelecom.clara.cloud.presentation.designer.services.config;

import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.environments.EnvironmentsPage;
import com.francetelecom.clara.cloud.presentation.releases.SelectedReleasePage;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;
import com.francetelecom.clara.cloud.presentation.tools.CodeMirrorTextArea;
import com.francetelecom.clara.cloud.presentation.validators.ConfigDuplicateKeysValidator;
import com.francetelecom.clara.cloud.presentation.validators.ConfigMaxNumberKeysValidator;
import com.francetelecom.clara.cloud.presentation.validators.ConfigMaxSizeValidator;
import com.francetelecom.clara.cloud.presentation.validators.InvalidCharsetValidator;

import org.apache.wicket.Page;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 24/01/12
 */
public class LogicalConfigServicePanel extends LogicalServiceBasePanel<LogicalConfigService> {

    private static final long serialVersionUID = 5611122953942366873L;

	private CodeMirrorTextArea<String> configSetContent;
	
	
    public LogicalConfigServicePanel(String id, IModel<LogicalConfigService> model, final Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);

        initComponents();
    }

    private void initComponents() {

    	getServiceForm().add(new CacheActivatedImage("logicalconfig-icon",new ResourceModel("cfconfigservice.icon").getObject()));
    	// Online help link
    	String completeHelpUrl = "";
    	try {
    		completeHelpUrl = new StringResourceModel("portal.designer.logical.service.online_manual.baseUrl", null).getString() 
        			+ new StringResourceModel("portal.designer.logical.service.online_manual." + getLogicalModelType(), null).getString();
    	} catch (Exception e) {
    		//do nothing
    	}
    	ExternalLink onlineHelpLink = new ExternalLink("onlineHelpLink", completeHelpUrl);
    	ExternalLink onlineHelpLinkOverride = new ExternalLink("onlineHelpLinkOverride", completeHelpUrl);

    	if (completeHelpUrl.isEmpty()) {
    		onlineHelpLink.setVisible(false);
    		onlineHelpLinkOverride.setVisible(false);
    	}

    	// Creates 2 blocks only for good displaying : Read more... juste after description (not possible with span wicket:id=descriptionLabel).
    	WebMarkupContainer descriptionBlock = new WebMarkupContainer("descriptionBlock");
    	descriptionBlock.add(onlineHelpLink);
    	getServiceForm().add(descriptionBlock);
    	
    	WebMarkupContainer overrideDescriptionBlock = new WebMarkupContainer("overrideDescriptionBlock");
    	overrideDescriptionBlock.add(onlineHelpLinkOverride);
    	getServiceForm().add(overrideDescriptionBlock);

        if (configOverride) {
        	descriptionBlock.setVisible(false);
        } else {
        	overrideDescriptionBlock.setVisible(false);
        }

    	
        // FUNCTIONNAL PARAMETERS    
        RequiredTextField<String> label = new RequiredTextField<>("label");
        label.setLabel(new StringResourceModel("portal.designer.service.config.label",null));
        label.add(new PropertyValidator<>());
        getServiceForm().add(label);

        TextField<String> keyPrefix = new TextField<>("keyPrefix", String.class);
        keyPrefix.setConvertEmptyInputStringToNull(false);
        keyPrefix.setLabel(new StringResourceModel("portal.designer.service.config.keyPrefix", null));
        keyPrefix.add(new PropertyValidator<>());
        getServiceForm().add(keyPrefix);

        configSetContent = new CodeMirrorTextArea<>("configSetContent", readOnly && !configOverride);

        configSetContent.setLabel(new StringResourceModel("portal.designer.service.config.configSetContent", null));
        configSetContent.add(new PropertyValidator<>());
        getServiceForm().add(configSetContent);

        configSetContent.add(new ConfigDuplicateKeysValidator());
        configSetContent.add(new ConfigMaxSizeValidator());
        configSetContent.add(new ConfigMaxNumberKeysValidator());
        configSetContent.add(new InvalidCharsetValidator());

    }
    
    @Override
    protected void onInitialize() {
        super.onInitialize();

        if (configOverride) {

            // Get previous changes
            LogicalConfigService overridenConfigService = null;
            if (getPage() instanceof SelectedReleasePage) {
                overridenConfigService = ((SelectedReleasePage) getPage()).getEnvironmentConfigOverride(getServiceForm().getModelObject().getLabel());
            } else if (getPage() instanceof EnvironmentsPage) {
                overridenConfigService = ((EnvironmentsPage) getPage()).getEnvironmentConfigOverride(getServiceForm().getModelObject().getLabel());
            }

            if (overridenConfigService == null) {
                LogicalConfigService originalConfigService = getServiceForm().getModelObject();
                overridenConfigService = new LogicalConfigService();
                overridenConfigService.setAvailable(originalConfigService.isAvailable());
                overridenConfigService.setKeyPrefix(originalConfigService.getKeyPrefix());
                overridenConfigService.setLabel(originalConfigService.getLabel());

                String configSetContent = originalConfigService.getConfigSetContent();
                String explanation = getString("portal.designer.service.config.override.explanation");
                configSetContent = explanation + "\n\n" + configSetContent;
                // Comment each line
                configSetContent = Pattern.compile("^", Pattern.MULTILINE).matcher(configSetContent).replaceAll("#");

                try {
                    overridenConfigService.setConfigSetContent(configSetContent);
                } catch (InvalidConfigServiceException e) {
                    // Config content is coming from a previous config service, so it should be ok
                    throw new IllegalArgumentException("Invalid Config Service.", e);
                }
            }
            getServiceForm().setModel(new CompoundPropertyModel<LogicalConfigService>(overridenConfigService));

            // The config set content is the only field overridable
            configSetContent.setEnabled(true);
        }
    }

    @Override
    protected void applyOverrides() {
        if (getPage() instanceof SelectedReleasePage) {
            ((SelectedReleasePage) getPage()).addEnvironmentConfigOverride(getServiceForm().getModelObject());
        } else if (getPage() instanceof EnvironmentsPage) {
            ((EnvironmentsPage) getPage()).addEnvironmentConfigOverride(getServiceForm().getModelObject());
        }
    }
}
