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
package com.francetelecom.clara.cloud.presentation.designer.services.cfjavaprocessing;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.validation.validator.UrlValidator;

import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.logicalmodel.CFJavaProcessing;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceBasePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 03/01/12
 */
public class LogicalCfJavaProcessingServicePanel extends LogicalServiceBasePanel<CFJavaProcessing> {

	private static final long serialVersionUID = -6824380667161266251L;
	
	public LogicalCfJavaProcessingServicePanel(String id, IModel<CFJavaProcessing> model, Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model, parentPage, isNew, readOnly, configOverride);
        initComponents();
    }

    private void initComponents() {
 
    	getServiceForm().add(new CacheActivatedImage("cfjavaprocessing-icon",new ResourceModel("cfjavaprocessing.icon").getObject()));
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
        label.setLabel(new StringResourceModel("portal.designer.service.cfjava.label", null));
        label.add(new PropertyValidator<>());
        getServiceForm().add(label);

        RequiredTextField<String> groupId = new RequiredTextField<String>("softwareReference.groupId");
        groupId.setLabel(new StringResourceModel("portal.designer.service.cfjava.softwareReference.groupId", null));
        groupId.add(new PropertyValidator<>());
        getServiceForm().add(groupId);

        RequiredTextField<String> artifactId = new RequiredTextField<String>("softwareReference.artifactId");
        artifactId.setLabel(new StringResourceModel("portal.designer.service.cfjava.softwareReference.artifactId", null));
        artifactId.add(new PropertyValidator<>());
        getServiceForm().add(artifactId);

        RequiredTextField<String> version = new RequiredTextField<String>("softwareReference.version");        
        version.setLabel(new StringResourceModel("portal.designer.service.cfjava.softwareReference.version", null));
        version.add(new PropertyValidator<>());
        getServiceForm().add(version);

        RequiredTextField<String> extension = new RequiredTextField<String>("softwareReference.extension");
        if (getServiceForm().getModelObject().getSoftwareReference() == null) {
        	getServiceForm().getModelObject().setSoftwareReference(new MavenReference("", "", "", "war"));	
        }
        extension.setLabel(new StringResourceModel("portal.designer.service.cfjava.softwareReference.extension", null));
        extension.add(new PropertyValidator<>());
        getServiceForm().add(extension);
        /*List<String> extensionList = new ArrayList<String>();
        extensionList.add("jar");
        extensionList.add("war");
        extensionList.add("ear");
        DropDownChoice<String> extension = new DropDownChoice<String>("softwareReference.extension", extensionList);
        extension.setLabel(new StringResourceModel("portal.designer.service.cfjava.softwareReference.extension",null));
        extension.add(new PropertyValidator<>());
        getServiceForm().add(extension);*/

        TextField<String> classifier = new TextField<String>("softwareReference.classifier", String.class);
        classifier.setLabel(new StringResourceModel("portal.designer.service.cfjava.softwareReference.classifier", null));
        classifier.add(new PropertyValidator<>());
        getServiceForm().add(classifier);

        
        CheckBox optionalSoftwareReference=new CheckBox("optionalSoftwareReference");
        optionalSoftwareReference.setLabel(new StringResourceModel("portal.designer.service.cfjava.optionalSoftwareReference", null));
        getServiceForm().add(optionalSoftwareReference);
        
        
        
        TextField<Integer> minMemoryMbHint = new TextField<Integer>("minMemoryMbHint");
        minMemoryMbHint.setLabel(new StringResourceModel("portal.designer.service.cfjava.minMemoryMbHint", null));
        minMemoryMbHint.add(new AttributeModifier("class","small"));
        minMemoryMbHint.add(new PropertyValidator<>());
        getServiceForm().add(minMemoryMbHint);

        
        
        final TextField<String> iconUrl = new TextField<String>("iconUrl");
        iconUrl.setLabel(new StringResourceModel("portal.designer.service.cfjava.iconUrl", null));
        //iconUrl.add(new AttributeModifier("title", new StringResourceModel("portal.designer.service.cfjava.iconUrl.help",null)));
        //Add Wicket validation for URL
        iconUrl.add(new UrlValidator());
        iconUrl.add(new PropertyValidator<>());
        iconUrl.add(new OnChangeAjaxBehavior() {

            /**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
            protected void onUpdate(AjaxRequestTarget target) {
                // Important, DO NOT DELETE
                // By Calling OnChangeAjaxBehavior, we update iconUrl field model to can push button preview with updated model

            }

            @Override
            protected void onError(AjaxRequestTarget target, RuntimeException e) {
                iconUrl.updateModel();    // The feedback is handle when click on preview button so we need to updateModel when there is an error
            }
       });
       getServiceForm().add(iconUrl);
       getServiceForm().add(new CacheActivatedImage("imageHelp.iconUrl", getString("image.help")));


       WebMarkupContainer imageContainer = new WebMarkupContainer("imageContainer");

       final WebMarkupContainer icon = new WebMarkupContainer("icon");
       icon.setOutputMarkupId(true);
       icon.setOutputMarkupPlaceholderTag(true);
       if (iconUrl.getModelObject() == null || iconUrl.getModelObject().equals("")) {
           setDefaultIconAndUpdateFeedBack(icon, null, "");
       } else {
           setCustomIconAndUpdateFeedBack(icon, iconUrl, null);
       }
       imageContainer.add(icon);

       final Label feedback = new Label("feedbackError", new Model<String>(""));
       feedback.setOutputMarkupId(true);
       imageContainer.add(feedback);
       

       AjaxLink preview = new AjaxLink("preview", new Model()) {
            @Override
            public void onClick(AjaxRequestTarget target) {
                try {

                    //If null or empty when click on preview button, clean feedBack and display default image
                    if (iconUrl.getModelObject() == null || iconUrl.getModelObject().equals("")) {
                        setDefaultIconAndCleanFeedBack(icon, feedback);
                    } // Else try to display custom image
                    else {
                        //Construct URL to test integrity of what user typing
                        URL url = new URL(iconUrl.getModelObject().toString());
                        if (iconUrl.getModelObject() != null) {
                            setCustomIconAndUpdateFeedBack(icon, iconUrl, feedback);
                        }
                    }
                } catch (MalformedURLException e) {
                    setDefaultIconAndUpdateFeedBack(icon, feedback, e.getMessage());
                    e.printStackTrace();
                }
                target.add(icon);
                target.add(feedback);
            }
        };
        imageContainer.add(preview);
        getServiceForm().add(imageContainer);

    }

    private void setDefaultIconAndCleanFeedBack(WebMarkupContainer image, Label feedback) {
        image.add(new AttributeModifier("style",new Model<String>("background-image:url(\"../../../../../images/designer/cloudfoundry-icon.png\"); background-repeat:no-repeat; background-position:5px 5px")));
        if (feedback != null) {
            feedback.setDefaultModelObject("");
        }
    }

    private void setCustomIconAndUpdateFeedBack(WebMarkupContainer image, TextField<String> iconUrl, Label feedback) {
        image.add(new AttributeModifier("style",new Model<String>("background-image:url(\""+iconUrl.getModelObject().toString()+"\"); background-repeat:no-repeat; background-position:5px 5px; background-size:32px 32px;")));
        if (feedback != null) {
            feedback.setDefaultModelObject("");
        }
    }
    private void setDefaultIconAndUpdateFeedBack(WebMarkupContainer image, Label feedback, String detailedErrorMessage) {
        image.add(new AttributeModifier("style",new Model<String>("background-image:url(\"../../../../../images/designer/cloudfoundry-icon.png\"); background-repeat:no-repeat; background-position:5px 5px")));
        if (feedback != null) {
            feedback.setDefaultModel(new StringResourceModel("portal.designer.service.cfjava.iconUrl.preview.text.ko", null, new Object[]{ detailedErrorMessage}));
        }
    }

}
