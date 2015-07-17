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

import com.francetelecom.clara.cloud.presentation.releases.SelectedReleasePage;
import com.francetelecom.clara.cloud.presentation.tools.BlockUIDecorator;
import com.francetelecom.clara.cloud.presentation.tools.DeleteConfirmationBlockUIDecorator;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;

import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.IndicatingAjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: lzxv3002
 * Date: 03/08/11
 * Time: 17:22
 * To change this template use File | Settings | File Templates.
 */
public class EnvironmentActionPanel<E extends EnvironmentDto> extends GenericPanel<E> {

    /*
     * serial UID
     */
    private static final long serialVersionUID = -6858797361884019223L;

    /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(EnvironmentActionPanel.class);

    private Page parentPage;

    AjaxLink<E> startBtn;
    AjaxLink<E> stopBtn;
    AjaxLink<E> deleteBtn;
    BookmarkablePageLink reloadBtn;

    WebMarkupContainer startDisableSpan;
    WebMarkupContainer stopDisableSpan;
    WebMarkupContainer deleteDisableSpan;
    WebMarkupContainer reloadSpan;

    public EnvironmentActionPanel(String id, IModel<E> model) {
        super(id, model);
    }

    @Override
    protected void onInitialize() {
        this.parentPage = getPage();
        createButtons();
        super.onInitialize();
    }

    @Override
    protected void onModelChanged() {
        startBtn.setModelObject(getModelObject());
        stopBtn.setModelObject(getModelObject());
        deleteBtn.setModelObject(getModelObject());
        reloadBtn.getPageParameters().set("envUid", getModelObject().getUid());
    }

    private void createButtons() {

        createStartDisableBtn();
        createStartEnableBtn();
        
        createStopDisableBtn();
        createStopEnableBtn();
        
        createDeleteDisableBtn();
        createDeleteEnableBtn();
        
        createReloadBtn();
    
    }

    /** START BUTTON ENABLE **/
    private void createStartEnableBtn() {

        startBtn = new IndicatingAjaxLink<E>("env-start-link", getModel()) {
            private static final long serialVersionUID = -3624723770141461652L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    String envUID = getModelObject().getUid();
                    if (parentPage instanceof SelectedEnvironmentPage) {
                        ((SelectedEnvironmentPage)parentPage).getManageEnvironment().startEnvironment(envUID);
                    } else if (parentPage instanceof EnvironmentsPage) {
                        ((EnvironmentsPage)parentPage).getManageEnvironment().startEnvironment(envUID);
                    } else {
                        ((SelectedReleasePage)parentPage).getManageEnvironment().startEnvironment(envUID);
                    }
                    propagateAjaxUpdate(target);

                } catch (ObjectNotFoundException e) {
                    String errMsg = getString("portal.environment.action.start.error.objectnotfound", new Model<Object[]>(new Object[]{ getModelObject().getLabel() }));
                    logger.error(errMsg);
                    error(errMsg);
                }
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                attributes.getAjaxCallListeners().add(new BlockUIDecorator(getString("portal.info.env.start"))); 
            }

            @Override
            public boolean isVisible() {
                EnvironmentStatusEnum envStatus = getModelObject().getStatus();
                return getModelObject().isEditable() && (envStatus == EnvironmentStatusEnum.STOPPED ||
                                                         envStatus == EnvironmentStatusEnum.FAILED);
            }
        };

        add(startBtn);
    }

    /** START BUTTON DISABLE **/
    private void createStartDisableBtn() {
        startDisableSpan = new WebMarkupContainer("env-start-disable") {
            @Override
            public boolean isVisible() {
                EnvironmentStatusEnum envStatus = getModelObject().getStatus();
                return !(getModelObject().isEditable() && (envStatus == EnvironmentStatusEnum.STOPPED ||
                                                           envStatus == EnvironmentStatusEnum.FAILED));
            }
        };
        add(startDisableSpan);
    }

    /** STOP BUTTON ENABLE **/
    private void createStopEnableBtn() {
        stopBtn = new IndicatingAjaxLink<E>("env-stop-link", getModel()) {

            private static final long serialVersionUID = -7938349139632727052L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                try {
                    String envUID = getModelObject().getUid();
                    if (parentPage instanceof SelectedEnvironmentPage) {
                        ((SelectedEnvironmentPage)parentPage).getManageEnvironment().stopEnvironment(envUID);
                    } else if (parentPage instanceof EnvironmentsPage) {
                        ((EnvironmentsPage)parentPage).getManageEnvironment().stopEnvironment(envUID);
                    } else {
                        ((SelectedReleasePage)parentPage).getManageEnvironment().stopEnvironment(envUID);
                    }
                    propagateAjaxUpdate(target);

                } catch (ObjectNotFoundException e) {
                    String errMsg = getString("portal.environment.action.stop.error.objectnotfound", new Model<Object[]>(new Object[]{ getModelObject().getLabel() }));
                    logger.error(errMsg);
                    error(errMsg);
                }

            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                attributes.getAjaxCallListeners().add(new BlockUIDecorator(getString("portal.info.env.stop"))); 
            }

            @Override
            public boolean isVisible() {
                EnvironmentStatusEnum envStatus = getModelObject().getStatus();
                return getModelObject().isEditable() && (envStatus == EnvironmentStatusEnum.RUNNING ||
                                                         envStatus == EnvironmentStatusEnum.FAILED);
            }
        };

        add(stopBtn);
    }

    /** STOP BUTTON DISABLE **/
    private void createStopDisableBtn() {
        stopDisableSpan = new WebMarkupContainer("env-stop-disable") {
            @Override
            public boolean isVisible() {
                EnvironmentStatusEnum envStatus = getModelObject().getStatus();
                return !(getModelObject().isEditable() && (envStatus == EnvironmentStatusEnum.RUNNING ||
                                                           envStatus == EnvironmentStatusEnum.FAILED));
            }
        };
        add(stopDisableSpan);
    }

    /** DELETE BUTTON ENABLE **/
    private void createDeleteEnableBtn() {
        deleteBtn = new IndicatingAjaxLink<E>("env-delete-link", getModel()) {

            private static final long serialVersionUID = -8608226682718820756L;

            @Override
            public void onClick(AjaxRequestTarget target) {

                try {
                    String envUID = getModelObject().getUid();
                    if (parentPage instanceof SelectedEnvironmentPage) {
                        ((SelectedEnvironmentPage)parentPage).getManageEnvironment().deleteEnvironment(envUID);
                    } else if (parentPage instanceof EnvironmentsPage) {
                        ((EnvironmentsPage)parentPage).getManageEnvironment().deleteEnvironment(envUID);
                    } else {
                        ((SelectedReleasePage)parentPage).getManageEnvironment().deleteEnvironment(envUID);
                    }
                    propagateAjaxUpdate(target);

                } catch (ObjectNotFoundException e) {
                    String errMsg = getString("portal.environment.action.delete.error.objectnotfound", new Model<Object[]>(new Object[]{ getModelObject().getLabel() }));
                    logger.error(errMsg);
                    error(errMsg);
                }
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                attributes.getAjaxCallListeners().add(new DeleteConfirmationBlockUIDecorator(getString("portal.environment.action.delete.confirm", new Model<String[]>(new String[]{ getModelObject().getLabel() }))
                        , getString("portal.info.env.delete"))); 
            }

            @Override
            public boolean isVisible() {
                EnvironmentStatusEnum envStatus = getModelObject().getStatus();
                return getModelObject().isEditable() && (envStatus == EnvironmentStatusEnum.RUNNING ||
                                                         envStatus == EnvironmentStatusEnum.STOPPED ||
                                                         envStatus == EnvironmentStatusEnum.FAILED);
                
            }
        };

        add(deleteBtn);
    }

    /** DELETE BUTTON DISABLE **/
    private void createDeleteDisableBtn() {
        deleteDisableSpan = new WebMarkupContainer("env-delete-disable"){
            @Override
            public boolean isVisible() {
                EnvironmentStatusEnum envStatus = getModelObject().getStatus();
                return !(getModelObject().isEditable() && (envStatus == EnvironmentStatusEnum.RUNNING ||
                                                           envStatus == EnvironmentStatusEnum.STOPPED ||
                                                           envStatus == EnvironmentStatusEnum.FAILED));
            }
        };
        add(deleteDisableSpan);
    }

    /** RELOAD BUTTON **/
    private void createReloadBtn() {

        /** actions button from environment details page **/
        PageParameters params = new PageParameters();

        if (parentPage.getPageParameters().getNamedKeys().contains("appUid")) {
            params.set("appUid", parentPage.getPageParameters().get("appUid"));
        }
        if (parentPage.getPageParameters().getNamedKeys().contains("releaseUid")) {
            params.set("releaseUid", parentPage.getPageParameters().get("releaseUid"));
        }
        if (parentPage.getPageParameters().getNamedKeys().contains("envUid")) {
            params.set("envUid", getModelObject().getUid());
        }

        reloadBtn = new BookmarkablePageLink("env-reload-link", parentPage.getClass(), params);

        add(reloadBtn);
    }
    
    private void propagateAjaxUpdate(AjaxRequestTarget target) {
        
        if (parentPage instanceof EnvironmentsPage) {
            ((EnvironmentsPage)parentPage).getEnvironmentsTablePanel().updateEnvActionsPanelAndStatus(target);
        } else if (parentPage instanceof SelectedEnvironmentPage) {
            ((SelectedEnvironmentPage)parentPage).getEnvDetailsPanel().updateEnvDetailsPanel(target);
        } else {
            ((SelectedReleasePage)parentPage).getEnvironmentsTablePanel().updateEnvActionsPanelAndStatus(target);
        }
        
    }

}
