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
package com.francetelecom.clara.cloud.presentation.environments;

import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.presentation.designer.panels.DesignerArchitectureMatrixPanel;
import com.francetelecom.clara.cloud.presentation.tools.FieldFeedbackDecorator;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 08/08/11
 */
public class EnvironmentDetailsPanel extends GenericPanel<EnvironmentDetailsDto> {

    /**
     * serialUID
     */
    private static final long serialVersionUID = -4295463812796975308L;

    /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(EnvironmentDetailsPanel.class);

    private SelectedEnvironmentPage parentPage;

    private EnvironmentPercentPanel<EnvironmentDetailsDto> envProgressBarPanel;
    private EnvironmentActionPanel<EnvironmentDetailsDto> envActions;
    private Label envErrorMsgLabel;
    //
    private Label internalNameLabel;
    private Form<EnvironmentDetailsDto> envDetailForm;
    DesignerArchitectureMatrixPanel envArchitecturePanel;
    private WebMarkupContainer buttonContainer;
    private AjaxLink cancelButton;
    private AjaxLink editButton;
    private AjaxButton updateButton;
    private TextArea<String> comment;
    private boolean edit = false;
    private boolean isAllStatusMessageVisible = false;

    private WebMarkupContainer refreshContainer;
    
    private EnvironmentOverallsPanel environmentOverallsPanel;

    private Duration ajaxRefreshPeriod = Duration.milliseconds(10000);
    
    public EnvironmentDetailsPanel(String id, IModel<EnvironmentDetailsDto> model) {
        super(id, model);
    }

   /**
     * construct panel elements
     */
    protected void initComponents() {
        logger.debug("initComponents");
        logger.debug("selected environment name : "
                + (getModelObject()!= null ? getModelObject().getUid() : "(not set)"));
        setOutputMarkupId(true);
        refreshContainer = new WebMarkupContainer("refresh");

        initEnvTitle();
        initEnvOwner();
        initEnvActionsProgressBar();
        initEnvActionsButtons();
        initEnvErrorMessages();
        initEnvInternalName();
        initEnvCreationDate();
        initEnvOveralls();
        initEnvArchitectureMatrix();
        initEnvComment();
        add(refreshContainer);

        // add selfUpdaing behaviors on wanted components
        addBehaviorSelfUpdating();

    }

    private void initEnvTitle() {
        Label envTitleLabel = new Label("envTitle", getString("portal.environment.details.title", new Model<Object[]>(new Object[]{ getModelObject().getLabel() })));
        add(envTitleLabel);
    }

    private void initEnvOwner() {
        Label ownerLabel = new Label("env-owner", getModelObject().getOwnerName());
        refreshContainer.add(ownerLabel);
    }

    private void initEnvActionsProgressBar() {
        envProgressBarPanel = new EnvironmentPercentPanel<>("env-status-progressbar", getModel());
        envProgressBarPanel.setOutputMarkupId(true);
        refreshContainer.add(envProgressBarPanel);
    }

    private void initEnvActionsButtons() {
        envActions = new EnvironmentActionPanel<>("env-status-actions", getModel());
        envActions.setOutputMarkupId(true);
        refreshContainer.add(envActions);
    }

    private void initEnvErrorMessages() {

        String envErrorMsg = getModelObject().getStatusMessage();
        AjaxLink showHideEllipsis = new AjaxLink("env-status-link") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                if (!isAllStatusMessageVisible)  {
                    target.appendJavaScript("document.getElementById('"+envErrorMsgLabel.getMarkupId()+"').removeAttribute('style',0);");
                    isAllStatusMessageVisible = true;
                } else {
                    envErrorMsgLabel.add(new AttributeModifier("style",new Model<String>("max-height:50px")));
                    isAllStatusMessageVisible = false;
                }
                target.add(envErrorMsgLabel);
            }
        };
        add(showHideEllipsis);

        if ("".equals(envErrorMsg) || envErrorMsg == null) {
            envErrorMsgLabel = new Label("env-activation-error", new Model(getString("portal.environment.details.activation.nostatus.msg")));
        } else {
            envErrorMsgLabel = new Label("env-activation-error", getModelObject().getStatusMessage());
        }
        envErrorMsgLabel.setOutputMarkupId(true);
        envErrorMsgLabel.add(new AttributeModifier("title", new Model<String>(envErrorMsg)));
        envErrorMsgLabel.add(new AttributeModifier("style",new Model<String>("max-height:50px")));
        showHideEllipsis.add(envErrorMsgLabel);
        refreshContainer.add(showHideEllipsis);
    }

    private void initEnvInternalName() {
    	
        String internalName = getModelObject().getInternalName();
       	
        internalNameLabel = new Label("env-internal-name", internalName);
        internalNameLabel.setOutputMarkupId(true);
        refreshContainer.add(internalNameLabel);
    }

    private void initEnvCreationDate() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern(getString("portal.environment.creation.date.format"));

        Label creationDateLabel;
        creationDateLabel = new Label("env-creation-date", sdf.format(getModelObject().getCreationDate()));
        refreshContainer.add(creationDateLabel);
    }

    private void initEnvOveralls() {
        environmentOverallsPanel = new EnvironmentOverallsPanel("env-overalls", getModelObject());
        environmentOverallsPanel.setOutputMarkupId(true);
        refreshContainer.add(environmentOverallsPanel);
    }

    private void initEnvArchitectureMatrix() {
        envArchitecturePanel = new DesignerArchitectureMatrixPanel("env-design", (SelectedEnvironmentPage)getPage(), getModelObject(), true, false);
        add(envArchitecturePanel);
    }

    private void initEnvComment() {

        envDetailForm = new Form<>("envDetailForm", new CompoundPropertyModel<EnvironmentDetailsDto>(getModel()));

        comment = new TextArea<>("comment");
        comment.setEnabled(false);
        comment.add(new PropertyValidator<>());
        envDetailForm.add(comment);
        createButtons();
        manageButtonsVisibility();

        add(envDetailForm);

    }

    private void addBehaviorSelfUpdating() {
        refreshContainer.add(new AjaxSelfUpdatingTimerBehavior(ajaxRefreshPeriod) {
            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                updateEnvDetailsPanel(target);
            }
        });
    }

    public void updateEnvDetailsPanel(AjaxRequestTarget target) {
        EnvironmentDetailsDto selectedEnvDto;
        try {
            selectedEnvDto = parentPage.getManageEnvironment().findEnvironmentDetails(getModelObject().getUid());
        } catch (ObjectNotFoundException e) {
            logger.warn("Environment with uid " + getModelObject().getUid() + " does not exist");
            return;
        }
        
        setModelObject(selectedEnvDto);

        envActions.setModelObject(selectedEnvDto);
        envActions.modelChanged();
        envProgressBarPanel.setModelObject(selectedEnvDto);
        envProgressBarPanel.modelChanged();
        
        String message = selectedEnvDto.getStatusMessage();
        if ("".equals(message) || message == null) {
            envErrorMsgLabel.setDefaultModel(new Model<String>(getString("portal.environment.details.activation.nostatus.msg")));
        } else {
            envErrorMsgLabel.setDefaultModel(new Model<String>(message));
        }
        
        envErrorMsgLabel.add(new AttributeModifier("title", new Model<String>(selectedEnvDto.getStatusMessage())));
        
        internalNameLabel.setDefaultModel(new Model<String>(selectedEnvDto.getInternalName()));
 
        environmentOverallsPanel.setDefaultModel(new Model<EnvironmentDetailsDto>(selectedEnvDto));
                
        envArchitecturePanel.setEnvDetailsDto(selectedEnvDto);
        envArchitecturePanel.updateTable();

        target.add(internalNameLabel);
        
        //test
        target.add(environmentOverallsPanel);
        
        target.add(envActions);
        target.add(envProgressBarPanel);
        target.add(envErrorMsgLabel);
        target.add(envArchitecturePanel);
    }
    
    @Override
    protected void onModelChanged() {
        super.onModelChanged();
    }

    private void createButtons() {

        buttonContainer = new WebMarkupContainer("buttonContainer");

        editButton = new AjaxLink("releaseModifyLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {

                applyModificationOnComponents(true, target);

            }
        };

        cancelButton = new AjaxLink("releaseCancelLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {

                applyModificationOnComponents(false, target);

            }
        };

        updateButton = new AjaxButton("releaseUpdateLink") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                parentPage.getManageEnvironment().update(EnvironmentDetailsPanel.this.getModelObject());
                applyModificationOnComponents(false, target);
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
				applyModificationOnComponents(true, target);                
				target.add(form);
            }
        };

        cancelButton.add(new Label("cancelLabel", new Model(getString("portal.action.cancel"))));
        editButton.add(new Label("modifyLabel", new Model(getString("portal.action.modify"))));
        updateButton.add(new Label("updateLabel", new Model(getString("portal.action.update"))));

        buttonContainer.add(cancelButton);
        buttonContainer.add(editButton);
        buttonContainer.add(updateButton);

        buttonContainer.setOutputMarkupId(true);
        buttonContainer.setVisible(getModelObject().isEditable());

        envDetailForm.add(buttonContainer);

    }

    private void applyModificationOnComponents(boolean edit, AjaxRequestTarget target) {
        this.edit = edit;
        manageButtonsVisibility();
        comment.setEnabled(edit);
        target.add(comment);
        target.add(buttonContainer);
    }

    private void manageButtonsVisibility() {
        cancelButton.setVisible(edit);
        updateButton.setVisible(edit);
        editButton.setVisible(!edit);
    }

    @Override
    protected void onInitialize() {
        this.parentPage = (SelectedEnvironmentPage) getPage();
        initComponents();
        envDetailForm.visitChildren(FormComponent.class, new IVisitor<Component, Void>() {
            @Override
            public void component(Component object, IVisit<Void> visit) {
                object.add(new FieldFeedbackDecorator());
                visit.dontGoDeeper();
            }
        });
        super.onInitialize();
    }

}
