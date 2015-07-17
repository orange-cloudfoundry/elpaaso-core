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
package com.francetelecom.clara.cloud.presentation.designer.pages;

import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.event.Broadcast;
import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.presentation.common.CustomModalWindow;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.presentation.designer.support.DelegatingDesignerServices;
import com.francetelecom.clara.cloud.presentation.environments.EnvironmentWspInfoPanel;
import com.francetelecom.clara.cloud.presentation.tools.CodeMirrorTextArea;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;

/**
 * DesignerHelperPage
 *
 * panel used to create an application release
 *
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public abstract class DesignerHelperPage extends PageTemplate {

	private static final long serialVersionUID = 1449784253024125155L;

	/**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(DesignerHelperPage.class);

    @SpringBean
    protected ManageApplicationRelease manageApplicationRelease;

    @SpringBean
    protected ManageLogicalDeployment manageLogicalDeployment;

	@SpringBean
	protected DelegatingDesignerServices delegatingDesignerServices;

    protected LogicalDeployment logicalDeployment;

    private int logicalDeploymentId;

    /**
     * Modal windows to display service parameters
     */
    private ModalWindow modalServiceView;

    /**
     * PageTemplate constructor
     *
     * @param params - page parameters map
     */
    public DesignerHelperPage(final PageParameters params) {
        super(params);
        try {

            if (params.getNamedKeys().contains("releaseUid")) {
                logicalDeployment = getLogicalDeploymentPersisted(params.get("releaseUid").toString());
            }
        } catch (ObjectNotFoundException e) {
            error(e.getMessage());
        }

        modalServiceView = new CustomModalWindow("modalServiceView");
        modalServiceView.setWindowClosedCallback(new ModalWindow.WindowClosedCallback() {
            private static final long serialVersionUID = 1L;
            public void onClose(AjaxRequestTarget target) {

            }
        });
        add(modalServiceView);
    }


    public LogicalDeployment getLogicalDeploymentPersisted(String releaseUid) throws ObjectNotFoundException {

        logicalDeploymentId = manageApplicationRelease.findApplicationReleaseByUID(releaseUid).getLogicalDeployment().getId();
        logicalDeployment = manageLogicalDeployment.findLogicalDeployment(logicalDeploymentId);

        return logicalDeployment;

    }

    public LogicalDeployment getLogicalDeployment() {
        return logicalDeployment;
    }

    public void openModalWindow(AjaxRequestTarget ajaxRequestTarget, LogicalModelItem modelItem, boolean configOverride) {
    	modalServiceView.setUseInitialHeight(false);
    	modalServiceView.setInitialWidth(750);
        Panel serviceFormPanel = getServicePanel(modalServiceView.getContentId(), modelItem, this, false, true, configOverride);
        modalServiceView.setContent(serviceFormPanel);
        modalServiceView.show(ajaxRequestTarget);

        // Send a refresh event in case the modal window contains a CodeMirror instance
        send(serviceFormPanel, Broadcast.BREADTH, new CodeMirrorTextArea.CodeMirrorRefresh(ajaxRequestTarget));
    }

    public void openWspInfoPanel(AjaxRequestTarget ajaxRequestTarget, LogicalSoapService soapService, EnvironmentDetailsDto envDetailsDto) {

        List<LinkDto> linkDtos = envDetailsDto.getLinkDtoMap().get(soapService.getName());

        LinkDto linkDto = null;

        for (LinkDto dto : linkDtos) {
            if (dto.getLinkType() == LinkDto.LinkTypeEnum.ACCESS_LINK && dto.getServiceBindings() != null) {
                linkDto = dto;
                break;
            }
        }
        if (linkDto != null) {
            EnvironmentWspInfoPanel wspInfoPanel = new EnvironmentWspInfoPanel(modalServiceView.getContentId(), linkDto);
            modalServiceView.setInitialHeight(240);
            modalServiceView.setContent(wspInfoPanel);
            modalServiceView.show(ajaxRequestTarget);
        }
    }

    public Panel getServicePanel(String id, LogicalModelItem modelItem, DesignerHelperPage parentPage, boolean isNew, boolean readOnly, boolean configOverride) {
        return delegatingDesignerServices.createPanelFor(id, modelItem, parentPage, isNew, readOnly, configOverride);
    }

    public abstract FeedbackPanel getFeedbackPanel();
}
