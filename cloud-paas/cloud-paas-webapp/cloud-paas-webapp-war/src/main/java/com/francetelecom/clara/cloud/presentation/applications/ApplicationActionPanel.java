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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.presentation.common.WicketUtils;
import com.francetelecom.clara.cloud.presentation.tools.DeleteConfirmationDecorator;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 22/08/11
 */
public class ApplicationActionPanel extends GenericPanel<Application> {

    private static final long serialVersionUID = -4261292120373977646L;

    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ApplicationActionPanel.class);

    private boolean canBeDeleted;

    WebMarkupContainer deleteSpan;
    WebMarkupContainer editSpan;
    ApplicationsPage parentPage;

    public ApplicationActionPanel(String id, final IModel<Application> model, final boolean canBeDeleted) {
        super(id, model);
        this.canBeDeleted = canBeDeleted;
    }

    private void initComponent() {
        AjaxLink<Application> deleteBtn = new AjaxLink<Application>("app-delete", getModel()) {
            private static final long serialVersionUID = -3624723770141461652L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                parentPage.deleteApplication(target, getModelObject());
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                attributes.getAjaxCallListeners().add(new DeleteConfirmationDecorator(WicketUtils.getStringResourceModel(this,"portal.application.action.delete.confirm", new Model<String[]>(new String[]{ getModelObject().getLabel() })).getObject()));
                 
            }

            @Override
            public boolean isVisible() {
                return canBeDeleted;
            }
        };

        WebMarkupContainer deleteDisabledSpan = new WebMarkupContainer("app-delete-disable");
        if (canBeDeleted) {
            deleteDisabledSpan.setVisible(false);
        }
        add(deleteDisabledSpan);

        deleteSpan = new WebMarkupContainer("deleteImg");
        deleteBtn.add(deleteSpan);
        add(deleteBtn);

        Link<Application> editBtn = new Link<Application>("app-edit", getModel()) {

            private static final long serialVersionUID = -5024306917768033030L;

            @Override
            public void onClick() {
                PageParameters params = new PageParameters();
                params.set("appUid", getModelObject().getUID());
                params.set("edit", "1");

                SelectedAppPage appPage = new SelectedAppPage(params);
                setResponsePage(appPage);
            }
            
            @Override
            public boolean isVisible() {
                return getModelObject().isEditable();
            }
        };
        editBtn.add(new AttributeAppender("title", new ResourceModel("portal.application.action.modify")));

        WebMarkupContainer editDisabledSpan = new WebMarkupContainer("app-edit-disable");
        if (getModelObject().isEditable()) {
            editDisabledSpan.setVisible(false);
        }
        add(editDisabledSpan);

        editSpan = new WebMarkupContainer("editImg");
        editBtn.add(editSpan);
        add(editBtn);
    }

    @Override
    protected void onBeforeRender() {
        parentPage = (ApplicationsPage) getPage();
        initComponent();

        super.onBeforeRender();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
