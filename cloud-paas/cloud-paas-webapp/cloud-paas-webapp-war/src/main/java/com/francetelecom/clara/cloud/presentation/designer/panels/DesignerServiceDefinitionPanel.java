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
package com.francetelecom.clara.cloud.presentation.designer.panels;

import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 22/05/12
 */
public class DesignerServiceDefinitionPanel extends GenericPanel<List<LogicalModelItem>> {

    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(DesignerServiceDefinitionPanel.class);

    ListChoice<LogicalModelItem> servicesListChoice;

    final CompoundPropertyModel<LogicalModelItem> serviceModel = new CompoundPropertyModel<>(new Model<LogicalModelItem>(null));

    private DesignerPage parentPage;

    /**
     * Container of the service Form
     */
    private WebMarkupContainer serviceFormContainerPanel;

    /**
     * There service form panel
     */
    private Panel serviceFormPanel;

    private boolean architectureLocked;

    private int step;

    public DesignerServiceDefinitionPanel(String id, IModel<List<LogicalModelItem>> model, boolean architectureLocked, int step, DesignerPage parentPage) {
        super(id, model);

        this.architectureLocked = architectureLocked;
        this.step = step;
        this.parentPage = parentPage;
        initComponents();
    }

    @Override
    public boolean isVisible() {
        return !architectureLocked;
    }

    private StringResourceModel getStringResourceModel(java.lang.String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }

    private void initComponents() {

        initPanelTitleComponent(step);
        initServiceListChoiceComponent();
        initServiceFormContainerComponent();

    }

    private void initPanelTitleComponent(int step) {
        if (step == 0) {
            add(new Label("panelTitleLabel", getStringResourceModel("portal.design.service.select_external.title")));
        } else {
            add(new Label("panelTitleLabel", getStringResourceModel("portal.design.service.select_internal.title")));
        }
    }

    private void initServiceListChoiceComponent() {
        // This form is needed to automatically select a service in HMI tests
        Form<Void> selectForm = new Form<>("selectForm");
        add(selectForm);

        ChoiceRenderer<LogicalModelItem> choiceRenderer = new ChoiceRenderer<LogicalModelItem>() {
            private static final long serialVersionUID = -6313188726358374089L;

            @Override
            public Object getDisplayValue(LogicalModelItem modelItem) {
                return parentPage.getServiceCatalogName(modelItem);
            }

            @Override
            public String getIdValue(LogicalModelItem modelItem, int i) {
                return modelItem.getClass().getName();
            }
        };

        servicesListChoice = new ListChoice<LogicalModelItem>("logicalServicesListSelect", serviceModel, getModelObject(), choiceRenderer) {
            private static final long serialVersionUID = 4264246812205897235L;

            @Override
            protected boolean isDisabled(LogicalModelItem modelItem, int index, String selected) {
                return !parentPage.isServiceEnable(modelItem);
            }
        };

        servicesListChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = -913367640965160132L;

            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                updateServiceFormPanel(target, serviceModel.getObject(), parentPage, true);
            }
        });

//        servicesListChoice.setNullValid(true);
        servicesListChoice.setOutputMarkupId(true);
        selectForm.add(servicesListChoice);
    }

    private void initServiceFormContainerComponent() {

        serviceFormContainerPanel = new WebMarkupContainer("container");
        serviceFormContainerPanel.setOutputMarkupId(true);

        serviceFormPanel = parentPage.getServicePanel("logicalServicePanel", null, parentPage, true, false, false);
        serviceFormContainerPanel.add(serviceFormPanel);

        add(serviceFormContainerPanel);

    }

    public void updateServiceFormPanel(AjaxRequestTarget target, LogicalModelItem service, DesignerHelperPage parentPage, boolean isNew) {
        serviceFormPanel = parentPage.getServicePanel("logicalServicePanel", service, parentPage, isNew, false, false);
        serviceFormContainerPanel.addOrReplace(serviceFormPanel);
        target.add(serviceFormContainerPanel);
    }

}
