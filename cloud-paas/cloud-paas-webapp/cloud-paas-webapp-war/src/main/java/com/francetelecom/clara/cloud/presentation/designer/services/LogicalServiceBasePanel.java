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
package com.francetelecom.clara.cloud.presentation.designer.services;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;

import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.presentation.common.CustomModalWindow;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.designer.services.webserviceprovider.LogicalWebServiceProviderPanel;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.tools.BlockUIDecorator;
import com.francetelecom.clara.cloud.presentation.tools.FieldFeedbackDecorator;


/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 06/01/12
 */
public class LogicalServiceBasePanel<L extends LogicalModelItem> extends GenericPanel<L> {

	private static final long serialVersionUID = -4154303051453698255L;

	private Form<L> serviceForm;

    private Page parentPage;

    private AjaxButton addUpdateButton;
    private AjaxButton cancelCloseButton;

    private CheckBox fullValidation;

    private boolean isNew;

    protected boolean readOnly;
    
    protected boolean configOverride;

    private String logicalModelType;
    
    @SpringBean
    private LogicalServicesHelper logicalServicesHelper;

	public LogicalServiceBasePanel(String id, IModel<L> model, final Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        super(id, model);

        this.parentPage = parentPage;
        this.isNew = isNew;
        this.readOnly = readOnly;
        this.configOverride = configOverride;

        logicalModelType = logicalServicesHelper.getLogicalServiceCatalogName(getModelObject());
        initComponents(model);

    }

    private void initComponents(IModel<L> model) {

        serviceForm = new Form<>("serviceForm", model);

        // Add service button
        addUpdateButton = new AjaxButton("addUpdateButton") {

 			private static final long serialVersionUID = 7572256727897511356L;

			@Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (parentPage instanceof DesignerPage) {
                    ((DesignerPage)parentPage).addOrUpdateLogicalService(form, target, isNew);
                } else if (configOverride) {
                    applyOverrides();
                    findParent(CustomModalWindow.class).close(target);
                }
                target.add(form);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                attributes.getAjaxCallListeners().add(new BlockUIDecorator(getString("portal.info.default.wait"))); 
            }
        };

        String addOrUpdateKey;

        if (configOverride) {
            addOrUpdateKey = "portal.designer.service.button.override.apply";
        } else if (isNew) {
        	addOrUpdateKey = "portal.designer.service.button.add";
        } else {
        	addOrUpdateKey = "portal.designer.service.button.update";
        }

        Label addLabel = new Label("addUpdateLabel", new StringResourceModel(addOrUpdateKey, null));
        addUpdateButton.add(addLabel);
        serviceForm.add(addUpdateButton);

        // Cancel service button
        cancelCloseButton = new AjaxButton("cancelCloseButton") {

 			private static final long serialVersionUID = -513024373510393946L;

			@Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (!readOnly) {
                    if (parentPage instanceof DesignerPage) {
                        ((DesignerPage)parentPage).cancelServiceEdit(target);
                    }
                } else {
                    findParent(CustomModalWindow.class).close(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        };

        String cancelOrCloseKey;

        if (configOverride) {
            cancelOrCloseKey = "portal.designer.service.button.override.cancel";
        } else if (readOnly) {
            cancelOrCloseKey = "portal.designer.service.button.close";
            addUpdateButton.setVisible(false);
        } else {
            cancelOrCloseKey = "portal.designer.service.button.cancel";
        }

        Label cancelLabel = new Label("cancelLabel", new StringResourceModel(cancelOrCloseKey, null));

        cancelCloseButton.setDefaultFormProcessing(false);
        cancelCloseButton.add(cancelLabel);
        serviceForm.add(cancelCloseButton);

        /****
         * fullValidation displayed only for WSP (LogicalWebServiceProviderPanel)
         ****/
        WebMarkupContainer fullvalidationContent = new WebMarkupContainer("fullvalidationContent"){
 			private static final long serialVersionUID = 8118606464182451428L;

			@Override
            public boolean isVisible() {
                return this.getParent().getParent() instanceof LogicalWebServiceProviderPanel;
            }
        };

        fullValidation = new CheckBox("fullvalidation", new Model<Boolean>(Boolean.TRUE));
        fullvalidationContent.add(fullValidation);

        serviceForm.add(fullvalidationContent);

        add(serviceForm);

    }

    public Form<L> getServiceForm() {
        return serviceForm;
    }

    @Override
    protected void onInitialize() {
        serviceForm.visitChildren(FormComponent.class, new IVisitor<Component, Void>() {
            @Override
            public void component(Component component, IVisit<Void> visit) {
                component.add(new FieldFeedbackDecorator());

                if (!component.equals(addUpdateButton) && !component.equals(cancelCloseButton)) {

                    boolean serviceParameterEnable = true;

                    if (parentPage instanceof DesignerPage) {
                        serviceParameterEnable = ((DesignerPage)parentPage).isServiceParameterEnable(serviceForm.getModelObject(), component.getId());
                    }

                    if (!serviceParameterEnable) {
                        component.setEnabled(false);
                    }

                }

                // To disable all field except close button in readOnly mode
                if (readOnly) {
                    if (!component.equals(cancelCloseButton)) {
                        component.setEnabled(false);
                    }
                }
                // overridable components are re-enabled in subclasses, validation button here
                if (configOverride) {
                    addUpdateButton.setEnabled(true);
                }

                visit.dontGoDeeper();
            }

        });

        super.onInitialize();

    }

    public CheckBox getFullValidation() {
        return fullValidation;
    }

    protected void applyOverrides() {
    };

    public String getLogicalModelType() {
		return logicalModelType;
	}

}
