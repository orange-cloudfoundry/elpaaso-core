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
package com.francetelecom.clara.cloud.presentation.releases;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.presentation.applications.SelectedAppPage;
import com.francetelecom.clara.cloud.presentation.common.BreadcrumbsUpdateEvent;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.presentation.tools.BusinessExceptionHandler;
import com.francetelecom.clara.cloud.presentation.tools.DeleteConfirmationDecorator;
import com.francetelecom.clara.cloud.presentation.tools.FieldFeedbackDecorator;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 24/08/11
 */
public class ReleaseInformationPanel extends GenericPanel<ApplicationRelease> {

    private static final long serialVersionUID = 7576874782887382667L;

    private PageParameters params;
    private boolean edit = false;

    private Form<ApplicationRelease> releaseForm;
    private ManageApplicationRelease manageApplicationRelease;

    private WebMarkupContainer buttonContainer;
    private AjaxLink<Void> cancelButton;
    private AjaxLink<Void> editButton;
    private AjaxLink<ApplicationRelease> deleteButton;
    private AjaxSubmitLink updateButton;
    private SelectedReleasePage parentPage;

    private boolean canBeDeleted = false;

    private TextField<String> version;
    private TextArea<String> description;
    private TextField<String> middlewareProfileVersion;

    public ReleaseInformationPanel(String id, IModel<ApplicationRelease> model, PageParameters params, ManageApplicationRelease manageApplicationRelease, SelectedReleasePage parentPage) {
        super(id, model);

        this.params = params;
        this.manageApplicationRelease = manageApplicationRelease;
        this.parentPage = parentPage;

        if (params.getNamedKeys().contains("edit")) {
            this.edit = params.get("edit").toBoolean();
        }

        Label releaseLabel = new Label("releaseLabel", new StringResourceModel("portal.release.information.title", new Model(new String[]{model.getObject().getApplication().getLabel()+ " - "+model.getObject().getReleaseVersion()})));
        add(releaseLabel);

        createEditShowInformationComponent(model);

    }

    private void createEditShowInformationComponent(IModel<ApplicationRelease> model) {

        releaseForm = new Form<>("releaseForm");
        releaseForm.setDefaultModel(new CompoundPropertyModel<ApplicationRelease>(model));

        version = new TextField<>("releaseVersion");
        version.setLabel(new StringResourceModel("portal.release.version.label",null));
        version.add(new PropertyValidator<>());
        releaseForm.add(version);

        description = new TextArea<>("description");
        description.setLabel(new StringResourceModel("portal.release.description.label", null));
        description.add(new PropertyValidator<>());
        releaseForm.add(description);

        middlewareProfileVersion = new TextField<>("middlewareProfileVersion");
        middlewareProfileVersion.setLabel(new StringResourceModel("portal.release.middlewareProfileVersion.label", null));
        middlewareProfileVersion.setEnabled(false);
        middlewareProfileVersion.add(new PropertyValidator<>());
        releaseForm.add(middlewareProfileVersion);

        add(releaseForm);
        createButtons();
        manageButtonsVisibility();
        updateEditableInput();
    }

    private void createButtons() {

        buttonContainer = new WebMarkupContainer("buttonContainer");
        editButton = new AjaxLink<Void>("releaseModifyLink") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setEditable(true, target);
            }
        };

        cancelButton = new AjaxLink<Void>("releaseCancelLink") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                setEditable(false, target);
            }
        };

        deleteButton = new AjaxLink<ApplicationRelease>("releaseDeleteLink", getModel()) {

            @Override
            public void onClick(AjaxRequestTarget target) {

                try {
                    manageApplicationRelease.deleteApplicationRelease(getModelObject().getUID());
                } catch (ObjectNotFoundException e) {
                    BusinessExceptionHandler handler = new BusinessExceptionHandler(parentPage);
                    handler.error(e);
                    target.add(parentPage.getFeedbackPanel());
                } catch (BusinessException e) {
                    BusinessExceptionHandler handler = new BusinessExceptionHandler(parentPage);
                    handler.error(e);
                    target.add(parentPage.getFeedbackPanel());
				}

                if (params.getNamedKeys().contains("releaseUid")) {
                    params.remove("releaseUid");
                }
                if (params.getNamedKeys().contains("edit")) {
                    params.remove("edit");
                }
                if (params.getNamedKeys().contains("step")) {
                    params.remove("step");
                }
                setResponsePage(SelectedAppPage.class, params);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                String releaseLabel = getModelObject().getApplication().getLabel() + " - " + getModelObject().getReleaseVersion();
                attributes.getAjaxCallListeners().add(new DeleteConfirmationDecorator(getString("portal.release.action.delete.confirm", new Model<String[]>(new String[]{ releaseLabel })))); 
            }

        };

        updateButton = new AjaxSubmitLink("releaseUpdateLink") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                try {
                    ApplicationRelease applicationRelease = manageApplicationRelease.updateApplicationRelease((ApplicationRelease) form.getModelObject());
                    form.setDefaultModel(new CompoundPropertyModel<ApplicationRelease>(applicationRelease));
                    target.add(form);
                } catch (ObjectNotFoundException e) {
                    BusinessExceptionHandler handler = new BusinessExceptionHandler(parentPage);
                    handler.error(e);
                    target.add(parentPage.getFeedbackPanel());
                }
                setEditable(false, target);

                List<BreadcrumbsItem> bci = ((SelectedReleasePage) getPage()).getBreadcrumbsItems();
                BreadcrumbsItem updatedItem = bci.get(2);
                ApplicationRelease updatedRelease = releaseForm.getModelObject();
                updatedItem.setName(updatedRelease.getReleaseVersion());
                bci.remove(2);
                bci.add(updatedItem);
                send(getPage(), Broadcast.BREADTH, new BreadcrumbsUpdateEvent(bci, target));
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                setEditable(true, target);
            }
        };

        cancelButton.add(new Label("cancelLabel", new StringResourceModel("portal.release.action.cancel",null)));
        editButton.add(new Label("modifyLabel", new StringResourceModel("portal.release.action.modify",null)));
        deleteButton.add(new Label("deleteLabel", new StringResourceModel("portal.release.action.delete",null)));
        updateButton.add(new Label("updateLabel", new StringResourceModel("portal.release.action.update",null)));

        buttonContainer.add(cancelButton);
        buttonContainer.add(editButton);
        buttonContainer.add(deleteButton);
        buttonContainer.add(updateButton);

        buttonContainer.setOutputMarkupId(true);

        releaseForm.add(buttonContainer);

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
            editButton.setVisible(getModelObject().getApplication().isEditable());
        }
    }

    private void updateEditableInput() {
        version.setEnabled(edit);
        description.setEnabled(edit);
    }

    public void setEditable(boolean editable, AjaxRequestTarget target) {
        setEditable(editable);
        target.add(buttonContainer);
        target.add(releaseForm);
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

    @Override
    protected void onInitialize() {
        releaseForm.visitChildren(FormComponent.class, new IVisitor<Component, Void>() {
            @Override
            public void component(Component object, IVisit<Void> visit) {
                object.add(new FieldFeedbackDecorator());
                visit.dontGoDeeper();
            }
        });
        super.onInitialize();
    }

}
