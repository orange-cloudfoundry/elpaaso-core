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

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.presentation.tools.DeleteConfirmationDecorator;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 28/07/11
 * Time: 15:52
 * To change this template use File | Settings | File Templates.
 */
public abstract class DesignerArchitectureMatrixCellButtonPanel extends GenericPanel<LogicalModelItem> {
    /**
     * serialUID
     */
    private static final long serialVersionUID = 3994316940849636787L;
    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(DesignerArchitectureMatrixCellButtonPanel.class.getName());

    /**
     * edit icon
     */
    AjaxLink<Void> editBtn;
    AjaxLink<Void> viewBtn;
    AjaxLink<Void> configBtn;
    AjaxLink<Void> deleteBtn;
    AjaxLink<Void> wspInfoBtn;

    ExternalLink accessUrlBtn;
    ExternalLink logsUrlBtn;
    ExternalLink monitoringUrlBtn;

    WebMarkupContainer accessUrlSpan;
    WebMarkupContainer logsUrlSpan;
    WebMarkupContainer monitoringUrlSpan;
    WebMarkupContainer wspInfoSpan;

    private EnvironmentDetailsDto envDetailsDto;

    private boolean readOnly;
    private boolean allowOverride;

    URL accessUrl;
    URL logsUrl;
    URL monitoringUrl;
    LinkDto wspLinkDto;

    public DesignerArchitectureMatrixCellButtonPanel(String id, IModel<LogicalModelItem> model, final EnvironmentDetailsDto envDetailsDto, boolean readOnly, boolean allowOverride) {
        super(id, model);
        this.envDetailsDto = envDetailsDto;
        this.readOnly = readOnly;
        this.allowOverride = allowOverride;
        initComponents();
    }

    private void initComponents() {

        String label = getModelObject().getLabel();
        add(new Label("label", label));

        initEditBtn();
        initViewBtn();
        initConfigBtn();
        initDeleteBtn();
        initAccessUrlBtn();
        initLogUrlBtn();
        initMonitoringUtlBtn();
        initWspInfoBtn();
    }

    private void initWspInfoBtn() {
        wspLinkDto = null;
        if (envDetailsDto != null) {
            Map<String, List<LinkDto>> wspInfoDtoList = envDetailsDto.getLinkDtoMap();
            LogicalModelItem service = (LogicalModelItem) getDefaultModelObject();

            if (service instanceof LogicalSoapService) {
                List<LinkDto> linkDtos = wspInfoDtoList.get(service.getName());
                if (linkDtos != null) { // handling the beta status of the service which may be instanciated for demos but doesn't have associated DTOs
                    for (LinkDto linkDto : linkDtos) {
                        if (linkDto.getServiceBindings() != null) {
                            wspLinkDto = linkDto;
                        }
                    }
                }
            }
        }

        wspInfoSpan = new WebMarkupContainer("wspInfoImg");
        wspInfoBtn = new AjaxLink<Void>("cell-wspInfo") {

            private static final long serialVersionUID = -8840580243026807212L;

			@Override
            public void onClick(AjaxRequestTarget target) {
                logger.debug("WSP Info onClick");
                findParent(DesignerArchitectureMatrixCellButtonPanel.class).onClickWspInfo(target);
            }

            @Override
            public boolean isVisible() {
                return wspLinkDto != null;
            }

        };

        Model<String[]> labelModel = new Model<String[]>(new String[]{ getModelObject().getLabel() });
        StringResourceModel tooltipValue = new StringResourceModel("portal.designer.matrix.wspInfo.tooltip", labelModel);
        wspInfoBtn.add(new AttributeModifier("title", tooltipValue));
        wspInfoSpan.add(new AttributeAppender("class", new Model<String>((wspInfoBtn != null) ? "wspInfoImg-enable" : "wspInfoImg-disable"), " "));
        wspInfoBtn.add(wspInfoSpan);
        add(wspInfoBtn);

    }

    private void initMonitoringUtlBtn() {

        /* monitoring icon*/
        monitoringUrlSpan = new WebMarkupContainer("monitoringUrlImg");
        monitoringUrl = null;

        monitoringUrlBtn = new ExternalLink("cell-monitoringUrl", (monitoringUrl != null) ? monitoringUrl.toString() : "") {

            private static final long serialVersionUID = 5630633258734623659L;

            @Override
            public boolean isVisible() {
                // TODO : when monitoring will be integrated this condition will have to change
                return monitoringUrl != null;
            }
        };

        monitoringUrlSpan.add(new AttributeAppender("class", new Model<String>((monitoringUrl != null) ? "monitoringUrl-enable" : "monitoringUrl-disable"), " "));
        //monitoringUrlBtn.add(new AttributeAppender("target", new Model<String>("_blank"), " " ));
        monitoringUrlBtn.add(new AttributeModifier("id", new Model<String>("monitoringURL")));
        monitoringUrlBtn.add(monitoringUrlSpan);
        add(monitoringUrlBtn);

    }

    private void initLogUrlBtn() {

        /** log icon */
        logsUrlSpan = new WebMarkupContainer("logsUrlImg");

        logsUrl = getUrlFromLinkDto(LinkDto.LinkTypeEnum.LOGS_LINK, getModelObject());

        logsUrlBtn = new ExternalLink("cell-logsUrl", (logsUrl != null) ? logsUrl.toString() : "") {

            private static final long serialVersionUID = -7297970012605590790L;

            @Override
            public boolean isVisible() {
                return logsUrl != null;
            }
        };

        logsUrlSpan.add(new AttributeAppender("class", new Model<String>((logsUrl != null) ? "logsUrl-enable" : "logsUrl-disable"), " "));
        //logsUrlBtn.add(new AttributeAppender("target", new Model<String>("_blank"), " " ));
        logsUrlBtn.add(new AttributeModifier("id", new Model<String>("logsURL")));

        Model<String[]> labelModel = new Model<String[]>(new String[]{ getModelObject().getLabel() });
        StringResourceModel tooltipValue = new StringResourceModel("portal.designer.matrix.logs.tooltip", labelModel);
        logsUrlBtn.add(new AttributeModifier("title", tooltipValue));

        logsUrlBtn.add(logsUrlSpan);
        add(logsUrlBtn);

    }

    private void initAccessUrlBtn() {

        /* accessUrl icon */
        accessUrlSpan = new WebMarkupContainer("accessUrlImg");
        accessUrl = getUrlFromLinkDto(LinkDto.LinkTypeEnum.ACCESS_LINK, getModelObject());

        accessUrlBtn = new ExternalLink("cell-accessUrl", (accessUrl != null) ? accessUrl.toString() : "") {

            private static final long serialVersionUID = -6833152311086936880L;

            @Override
            public boolean isVisible() {
                return accessUrl != null;
            }

        };

        accessUrlSpan.add(new AttributeAppender("class", new Model<String>((accessUrl != null) ? "accessUrl-enable" : "accessUrl-disable"), " "));
        //accessUrlBtn.add(new AttributeAppender("target", new Model<String>("_blank"), " " ));
        accessUrlBtn.add(accessUrlSpan);
        accessUrlBtn.add(new AttributeModifier("id", new Model<String>("webURL")));

        Model<String[]> labelModel = new Model<String[]>(new String[]{ getModelObject().getLabel() });
        StringResourceModel tooltipValue = new StringResourceModel("portal.designer.matrix.accessurl.tooltip", labelModel);
        accessUrlBtn.add(new AttributeModifier("title", tooltipValue));

        add(accessUrlBtn);

    }

    private void initDeleteBtn() {

        /** delete icon */
        deleteBtn = new AjaxLink<Void>("cell-delete") {
            private static final long serialVersionUID = -3624723770141461652L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                logger.debug("delete icon onClick");
                findParent(DesignerArchitectureMatrixCellButtonPanel.class).onClickDelete(target);
            }

            @Override
            public boolean isVisible() {
                return !readOnly;
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                attributes.getAjaxCallListeners().add(new DeleteConfirmationDecorator(getString("portal.designer.service.action.delete.confirm"))); 
            }
        };

        Model<String[]> labelModel = new Model<String[]>(new String[]{ getModelObject().getLabel() });
        StringResourceModel tooltipValue = new StringResourceModel("portal.designer.matrix.delete.tooltip", labelModel);
        deleteBtn.add(new AttributeModifier("title", tooltipValue));
        add(deleteBtn);

    }

    private void initEditBtn() {

        /** edit icon */
        editBtn = new AjaxLink<Void>("cell-edit") {
            private static final long serialVersionUID = 9004624464275531600L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                logger.debug("edit icon onClick");
                findParent(DesignerArchitectureMatrixCellButtonPanel.class).onClickEdit(target);
                // Move the page to the logicalServicesListSelect panel to view edited service
                target.appendJavaScript("$('html, body').animate({scrollTop: $('#logicalServicesListSelect').offset().top}, 200);");
            }
            
            @Override
            public boolean isVisible() {
                return !readOnly;
            }

        };

        Model<String[]> labelModel = new Model<String[]>(new String[]{ getModelObject().getLabel() });
        StringResourceModel tooltipValue = new StringResourceModel("portal.designer.matrix.edit.tooltip", labelModel);
        editBtn.add(new AttributeModifier("title", tooltipValue));
        add(editBtn);

    }
    
    private void initViewBtn() {

        /** view icon */
        viewBtn = new AjaxLink<Void>("cell-view") {

            private static final long serialVersionUID = 1220079179197689639L;

			@Override
            public void onClick(AjaxRequestTarget target) {
                logger.debug("view icon onClick");
                findParent(DesignerArchitectureMatrixCellButtonPanel.class).onClickView(target);
            }

            @Override
            public boolean isVisible() {
                return readOnly;
            }

        };

        Model<String[]> labelModel = new Model<String[]>(new String[]{ getModelObject().getLabel() });
        StringResourceModel tooltipValue = new StringResourceModel("portal.designer.matrix.details.tooltip", labelModel);
        viewBtn.add(new AttributeModifier("title", tooltipValue));

        add(viewBtn);

    }

    private void initConfigBtn() {

        /** view icon */
        configBtn = new AjaxLink<Void>("cell-config") {

            private static final long serialVersionUID = 6195327499501053622L;

            @Override
            public void onClick(AjaxRequestTarget target) {
                logger.debug("config icon onClick");
                findParent(DesignerArchitectureMatrixCellButtonPanel.class).onClickConfigOverride(target);
            }

            @Override
            public boolean isVisible() {
                return allowOverride && DesignerArchitectureMatrixCellButtonPanel.this.getModelObject() instanceof LogicalConfigService;
            }

        };

        Model<String[]> labelModel = new Model<String[]>(new String[]{ getModelObject().getLabel() });
        configBtn.add(new AttributeModifier("title", new StringResourceModel("portal.designer.matrix.details.tooltip", labelModel)));

        add(configBtn);

    }

    private URL getUrlFromLinkDto(LinkDto.LinkTypeEnum linkType, LogicalModelItem logicalModelItem) {
        URL url = null;
        if (envDetailsDto != null) {

            Map<String, List<LinkDto>> linkDtosMap = null;
            linkDtosMap = envDetailsDto.getLinkDtoMap();

            if (linkDtosMap != null) {
                List<LinkDto> linkDtosList = linkDtosMap.get(logicalModelItem.getName());
                if (linkDtosList != null) {
                    for (LinkDto linkDto : linkDtosList) {
                        if (linkDto.getLinkType() == linkType) {
                            url = linkDto.getUrl();
                        }
                    }
                }
            }

        }

        return url;
    }

    protected abstract void onClickDelete(AjaxRequestTarget target);
    protected abstract void onClickEdit(AjaxRequestTarget target);
    protected abstract void onClickView(AjaxRequestTarget target);
    protected abstract void onClickWspInfo(AjaxRequestTarget target);
    protected abstract void onClickConfigOverride(AjaxRequestTarget target);
}
