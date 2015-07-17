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

import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.presentation.common.ReleaseFieldsetPanel;
import com.francetelecom.clara.cloud.presentation.common.WicketUtils;
import com.francetelecom.clara.cloud.presentation.models.FirstApplicationReleaseInfos;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;
import com.francetelecom.clara.cloud.presentation.tools.FieldFeedbackDecorator;

public class ApplicationCreatePanel extends Panel{

    private static final long serialVersionUID = 6343641923671807395L;

    private final ManageApplicationRelease manageApplicationRelease;

    private Form<FirstApplicationReleaseInfos> appForm;

    private ApplicationsPage parentPage;

    private ReleaseFieldsetPanel releaseFiedsetPanel;

    public ApplicationCreatePanel(String id, ManageApplicationRelease manageApplicationRelease) {
        super(id);
        this.manageApplicationRelease = manageApplicationRelease;
    }

    private void initComponents() {
        createAppForm();
    }

    private void createAppForm() {

        appForm = new Form<>("appForm", new CompoundPropertyModel<>(new FirstApplicationReleaseInfos()));

        TextField<String> appLabel = new TextField<>("appLabel");
        appLabel.setLabel(WicketUtils.getStringResourceModel(this, "portal.application.label.label"));

        appLabel.add(new AbstractValidator<String>() {
            @Override
            protected void onValidate(IValidatable<String> iValidatable) {
                if(!parentPage.isApplicationLabelUnique(iValidatable.getValue())) {
                    error(iValidatable);
                }
            }

            @Override
            protected String resourceKey() {
                return "portal.application.label.non.unique";
            }

            @Override
            protected Map<String, Object> variablesMap(IValidatable<String> stringIValidatable) {
                Map<String, Object> map = super.variablesMap(stringIValidatable);
                map.put("label", stringIValidatable.getValue());
                return map;
            }
        });
        appLabel.add(new PropertyValidator<>());
        appForm.add(appLabel);

        TextField<String> appCode = new TextField<>("appCode");
        appCode.setLabel(WicketUtils.getStringResourceModel(this, "portal.application.code.label"));
        appCode.add(new PropertyValidator<>());
        appForm.add(appCode);

        TextArea<String> appDescription = new TextArea<>("appDescription");
        appDescription.setLabel(WicketUtils.getStringResourceModel(this, "portal.application.description.label"));
        appDescription.add(new PropertyValidator<>());
        appForm.add(appDescription);

        RadioGroup<Boolean> appVisibility = new RadioGroup<>("appPublic");
        appVisibility.add(new Radio<Boolean>("appVisibilityRadioGroup-public", new Model<>(Boolean.TRUE)));
        appVisibility.add(new Radio<Boolean>("appVisibilityRadioGroup-private", new Model<>(Boolean.FALSE)));
        appVisibility.add(new PropertyValidator<>());
        appForm.add(appVisibility);
        appForm.add(new CacheActivatedImage("imageHelp.visibilityField", new ResourceModel("image.help").getObject()));

        TextField<String> members = new TextField<>("members");
        members.add(new PropertyValidator<>());
        appForm.add(members);
        appForm.add(new CacheActivatedImage("imageHelp.membersField", new ResourceModel("image.help").getObject()));

        releaseFiedsetPanel = new ReleaseFieldsetPanel("releaseFieldsetPanel", parentPage, manageApplicationRelease);
        appForm.add(releaseFiedsetPanel);

        createFormButtons(appForm);

        // set default visibility to private
        appForm.getModelObject().setAppPublic(Boolean.FALSE);

        add(appForm);
    }

    private void createFormButtons(Form<FirstApplicationReleaseInfos> appForm) {

        // Add first application button
        AjaxButton addButton = new AjaxButton("addAppButton") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                parentPage.addFirstApplicationCreation(form, target,
                        releaseFiedsetPanel.getAppRelease(),
                        releaseFiedsetPanel.getCurrentMiddlewareProfile());
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }

        };
        appForm.add(addButton);

        // Cancel first application add / update button
        AjaxButton cancelButton = new AjaxButton("cancelAppButton") {

            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                parentPage.cancelFirstApplicationCreation(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                 target.add(form);
            }
        };

        cancelButton.setDefaultFormProcessing(false);
        appForm.add(cancelButton);

    }

    @Override
    protected void onInitialize() {
        parentPage = (ApplicationsPage) getPage();
        initComponents();
        appForm.visitChildren(FormComponent.class, new IVisitor<Component, Void>() {
            @Override
            public void component(Component object, IVisit<Void> visit) {
                object.add(new FieldFeedbackDecorator());
                visit.dontGoDeeper();
            }
        });
        super.onInitialize();
    }
    public boolean shouldFork() {
        return releaseFiedsetPanel.shouldFork();
    }

}
