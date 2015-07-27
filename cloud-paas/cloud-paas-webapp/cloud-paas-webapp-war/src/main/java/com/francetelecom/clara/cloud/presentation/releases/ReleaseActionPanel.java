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

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.presentation.applications.SelectedAppPage;
import com.francetelecom.clara.cloud.presentation.common.WicketUtils;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.tools.BusinessExceptionHandler;
import com.francetelecom.clara.cloud.presentation.tools.DeleteConfirmationDecorator;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 24/08/11
 */
public class ReleaseActionPanel extends GenericPanel<ApplicationRelease> {

    private static final long serialVersionUID = 6194821376650720511L;

    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ReleaseActionPanel.class);

    @SpringBean
    private final ManageApplicationRelease manageApplicationRelease;

    AjaxLink<ApplicationRelease> deleteBtn;

    WebMarkupContainer deleteSpan;
    WebMarkupContainer editSpan;
    WebMarkupContainer editArchitectureSpan;

//    private PageParameters params;

    public ReleaseActionPanel(String id, final IModel<ApplicationRelease> model, final DataTable dataTable, final ManageApplicationRelease manageApplicationRelease) {
        super(id, model);
        this.manageApplicationRelease = manageApplicationRelease;

        deleteBtn = new AjaxLink<ApplicationRelease>("release-delete", getModel()) {

            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    manageApplicationRelease.deleteApplicationRelease(getModelObject().getUID());
                    target.add(dataTable);
                    if (getPage() instanceof SelectedAppPage) {
                        ((SelectedAppPage) getPage()).updateAppInfopanel(target);
                    }
                } catch (ObjectNotFoundException e) {
                    BusinessExceptionHandler handler = new BusinessExceptionHandler(this);
                    handler.error(e);
                    target.add(this.getPage());
                } catch (BusinessException e) {
//                    String errMsg = getString("portal.release.action.delete.envexists");
                	String errMsg = new ResourceModel("portal.release.action.delete.envexists").getObject();
                    logger.error(errMsg);
                    error(errMsg);
                    }
                target.add(this.getPage());
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                String releaseName = getModelObject().getApplication().getLabel() + " - " + getModelObject().getReleaseVersion();
                attributes.getAjaxCallListeners().add(new DeleteConfirmationDecorator(WicketUtils.getStringResourceModel(this,"portal.release.action.delete.confirm", new Model<String[]>(new String[]{ releaseName })).getObject()));
//                attributes.getAjaxCallListeners().add(new DeleteConfirmationDecorator(getString("portal.release.action.delete.confirm", new Model<String[]>(new String[]{ releaseName }))));
            }

            @Override
            public boolean isVisible() {
                return canBeDeleted();
            }
        };
        deleteBtn.add(new AttributeAppender("title", new ResourceModel("portal.release.action.delete")));

        WebMarkupContainer deleteDisabledSpan = new WebMarkupContainer("rel-delete-disable");
        if (canBeDeleted()) {
            deleteDisabledSpan.setVisible(false);
        }
        add(deleteDisabledSpan);

        deleteSpan = new WebMarkupContainer("deleteImg");
        deleteBtn.add(deleteSpan);
        add(deleteBtn);

        Link<ApplicationRelease> editBtn = new Link<ApplicationRelease>("release-edit", getModel()) {

            @Override
			public void onClick() {
                PageParameters params = new PageParameters();
                params.set("appUid", getModelObject().getApplication().getUID());
                params.set("releaseUid", getModelObject().getUID());
                params.set("edit", "1");

                SelectedReleasePage releasePage = new SelectedReleasePage(params);
                setResponsePage(releasePage);
			}
            
            @Override
            public boolean isVisible() {
                return getModelObject().getApplication().isEditable();
            }
        };
        editBtn.add(new AttributeAppender("title", new ResourceModel("portal.release.action.edit")));
        
        editSpan = new WebMarkupContainer("editImg");
        editBtn.add(editSpan);
        add(editBtn);

        WebMarkupContainer editDisabledSpan = new WebMarkupContainer("release-edit-disable");
        if (getModelObject().getApplication().isEditable()) {
            editDisabledSpan.setVisible(false);
        }
        add(editDisabledSpan);

        Link<ApplicationRelease> editArchitectureBtn = new Link<ApplicationRelease>("release-edit-architecture", getModel()) {

            @Override
            public void onClick() {
                PageParameters params = new PageParameters();
                params.set("appUid", getModelObject().getApplication().getUID());
                params.set("releaseUid", getModelObject().getUID());

                DesignerPage designerPage = new DesignerPage(params);
                setResponsePage(designerPage);
            }
        };
//       
        editArchitectureBtn.add(new AttributeAppender("title", new ResourceModel("portal.release.designer.edit.button")));
        
        
        editArchitectureSpan = new WebMarkupContainer("editArchitectureImg");
        editArchitectureBtn.add(editArchitectureSpan);
        add(editArchitectureBtn);
    }

    private boolean canBeDeleted() {
        try {
            return manageApplicationRelease.canBeDeleted(getModelObject().getUID());
        } catch (ObjectNotFoundException e) {
            return false;
        }
    }
}
