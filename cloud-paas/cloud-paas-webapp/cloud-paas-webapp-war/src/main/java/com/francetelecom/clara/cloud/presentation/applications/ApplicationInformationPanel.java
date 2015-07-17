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
package com.francetelecom.clara.cloud.presentation.applications;

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.presentation.common.BreadcrumbsUpdateEvent;
import com.francetelecom.clara.cloud.presentation.common.WicketUtils;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.presentation.tools.BusinessExceptionHandler;
import com.francetelecom.clara.cloud.presentation.tools.DeleteConfirmationDecorator;
import com.francetelecom.clara.cloud.presentation.tools.FieldFeedbackDecorator;
import com.francetelecom.clara.cloud.technicalservice.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.technicalservice.exception.PaasUserNotFoundException;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import java.util.List;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * ApplicationInformationPanel
 * Panel which show the application informations
 *
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Updated  : $LastChangedDate$
 * @author  : $Author$
 * @version : $Revision$
 */
public class ApplicationInformationPanel extends Panel {

    private static final long serialVersionUID = -8830804742137615642L;

    private boolean edit = false;

    private Form<Application> appForm;

    private WebMarkupContainer buttonContainer;
    private AjaxLink cancelButton;
    private AjaxLink editButton;
    private AjaxLink deleteButton;
    private AjaxSubmitLink updateButton;

    private SelectedAppPage parentPage;

    private boolean canBeDeleted = false;

    private TextField<String> label;
    private TextField<String> code;
    private TextArea<String> description;
    private RadioGroup<Boolean> appVisibility;
    private TextField<String> users;

    // Field must match a list of members separated by spaces,
    // the "central" regular expression must be the same as in SSOId class
    @NotNull
    @Pattern(regexp="(\\b[a-zA-Z]+[0-9]*\\b\\s*)+", message="{portal.application.members.label.javax.validation}")
    private String members;

    public ApplicationInformationPanel(String id, Application app, PageParameters params, SelectedAppPage parentPage) {
        super(id);
        this.parentPage = parentPage;
        if (params.getNamedKeys().contains("edit")) {
            this.edit = params.get("edit").toBoolean();
        }

        String applicationLabel = app.getLabel();
        Label appLabel = new Label("applicationLabel",
                new StringResourceModel("portal.application.information.title",
                        new Model<String[]>(new String[]{applicationLabel})));
        add(appLabel);
        createEditShowInformationComponent(app);

    }

    private StringResourceModel getStringResourceModel(java.lang.String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }

    private void createEditShowInformationComponent(Application app) {

        appForm = new Form<>("appForm");
        appForm.setDefaultModel(new CompoundPropertyModel<Application>(app));

        label = new TextField<>("label");
        label.setLabel(WicketUtils.getStringResourceModel(this, "portal.application.label.label"));
        label.add(new PropertyValidator<>());
        appForm.add(label);

        code = new TextField<>("code");
        code.setLabel(WicketUtils.getStringResourceModel(this, "portal.application.code.label"));
        code.add(new PropertyValidator<>());
        appForm.add(code);
        
        appVisibility = new RadioGroup<>("isPublic");
        appVisibility.add(new Radio<Boolean>("appVisibilityRadioGroup-public", new Model<>(Boolean.TRUE)));
        appVisibility.add(new Radio<Boolean>("appVisibilityRadioGroup-private", new Model<>(Boolean.FALSE)));
        appVisibility.add(new PropertyValidator<>());

        appVisibility.setLabel(WicketUtils.getStringResourceModel(this, "portal.application.visibility.label"));

        users = new TextField<>("members", new PropertyModel<String>(this, "members"));
        users.add(new PropertyValidator<>());
        appForm.add(users);
        appForm.add(new CacheActivatedImage("membersHelp", new ResourceModel("image.help").getObject()));

        appForm.add(appVisibility);

        description = new TextArea<>("description");
        description.setLabel(WicketUtils.getStringResourceModel(this, "portal.application.description.label"));
        description.add(new PropertyValidator<>());
        appForm.add(description);

        add(appForm);
        createButtons();
        manageButtonsVisibility();
        updateEditableInput();
    }

    private void createButtons() {

        buttonContainer = new WebMarkupContainer("buttonContainer");

        editButton = new AjaxLink("appModifyLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setEditable(true, target);
            }
        };

        cancelButton = new AjaxLink("appCancelLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                setEditable(false, target);
            }
        };

        deleteButton = new AjaxLink("appDeleteLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                parentPage.deleteApplication(target, appForm.getModelObject());
                setResponsePage(ApplicationsPage.class);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                String applicationLabel = appForm.getModelObject().getLabel();
                attributes.getAjaxCallListeners().add(new DeleteConfirmationDecorator(getString("portal.application.action.delete.confirm", new Model<String[]>(new String[]{ applicationLabel })))); 
            }
        };

        updateButton = new AjaxSubmitLink("appUpdateLink") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    parentPage.updateApplication(target, (Form<Application>) form, members);
                    setEditable(false, target);

                    List<BreadcrumbsItem> bci = ((SelectedAppPage) getPage()).getBreadcrumbsItems();
                    BreadcrumbsItem updatedItem = bci.get(1);
                    updatedItem.setName(appForm.getModelObject().getLabel());
                    bci.remove(1);
                    bci.add(updatedItem);
                    send(getPage(), Broadcast.BREADTH, new BreadcrumbsUpdateEvent(bci, target));
                } catch (ApplicationNotFoundException | DuplicateApplicationException | PaasUserNotFoundException e) {
                    BusinessExceptionHandler.addError(target, parentPage.getFeedbackPanel(), e);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                setEditable(true, target);
            }
        };

        cancelButton.add(new Label("cancelLabel", getStringResourceModel("portal.application.action.cancel")));
        editButton.add(new Label("modifyLabel", getStringResourceModel("portal.application.action.modify")));
        deleteButton.add(new Label("deleteLabel", getStringResourceModel("portal.application.action.delete")));
        updateButton.add(new Label("updateLabel", getStringResourceModel("portal.application.action.update")));

        buttonContainer.add(cancelButton);
        buttonContainer.add(editButton);
        buttonContainer.add(deleteButton);
        buttonContainer.add(updateButton);

        buttonContainer.setOutputMarkupId(true);

        appForm.add(buttonContainer);

    }

    private void manageButtonsVisibility() {
        if (edit) {
            cancelButton.setVisible(true);
            updateButton.setVisible(true);
            deleteButton.setVisible(false);
            editButton.setVisible(false);
        } else {
            cancelButton.setVisible(false);
            updateButton.setVisible(false);
            deleteButton.setVisible(canBeDeleted);
            editButton.setVisible(appForm.getModelObject().isEditable());
        }
    }

    private void updateEditableInput() {
        label.setEnabled(edit);
        description.setEnabled(edit);
        code.setEnabled(edit);
        appVisibility.setEnabled(edit);
        users.setEnabled(edit);
    }

    public void setEditable(boolean editable, AjaxRequestTarget target) {
        setEditable(editable);
        target.add(buttonContainer);
        target.add(appForm);
    }

    public void setEditable(boolean editable) {
        this.edit = editable;
        manageButtonsVisibility();
        updateEditableInput();
    }

    public void setCanBeDeleted(boolean canBeDeleted, AjaxRequestTarget target) {
        setCanBeDeleted(canBeDeleted);
        target.add(buttonContainer);
    }

    public void setCanBeDeleted(boolean canBeDeleted) {
        this.canBeDeleted = canBeDeleted;
        manageButtonsVisibility();
    }

    public void setMembers(List<SSOId> membersList) {
        members = "";
        for (SSOId member : membersList) {
            members += member.getValue();
            members += " ";
        }
    }

    @Override
    protected void onInitialize() {
        appForm.visitChildren(FormComponent.class, new IVisitor<Component, Void>() {
            @Override
            public void component(Component object, IVisit<Void> visit) {
                object.add(new FieldFeedbackDecorator());
                visit.dontGoDeeper();
            }
        });
        super.onInitialize();
    }

}
