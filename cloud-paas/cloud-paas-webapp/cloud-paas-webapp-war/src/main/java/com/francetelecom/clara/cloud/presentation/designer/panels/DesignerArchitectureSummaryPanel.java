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

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.presentation.releases.SelectedReleasePage;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 06/09/11
 * Time: 15:40
 * To change this template use File | Settings | File Templates.
 */
public class DesignerArchitectureSummaryPanel extends Panel {

    /**
     * Logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(DesignerArchitectureSummaryPanel.class);

    private PageParameters params;
    private Label statusLabel;
    private boolean isLogicalDeploymentConsistent = false;
    private boolean canCreateEnvironment = false;
    private boolean isLocked;

    public DesignerArchitectureSummaryPanel(String id, PageParameters params, boolean isLocked, boolean isLogicalDeploymentConsistent, boolean canCreateEnvironment) {
        super(id);
        this.params = params;
        this.isLogicalDeploymentConsistent = isLogicalDeploymentConsistent;
        this.canCreateEnvironment = canCreateEnvironment;
        this.isLocked = isLocked;
        initComponents();
    }

    private void initComponents() {

            Label summaryResultLabel = new Label("summaryResultLabel",new StringResourceModel("portal.design.service.summary_result.title", null));
            add(summaryResultLabel);

            String key = "";
            logger.debug("looking for key");

            if (!canCreateEnvironment) {
                key = "portal.design.service.summary.read.only";
            } else if (isLocked) {
                key = "portal.design.service.summary.locked";
            } else if (isLogicalDeploymentConsistent) {
                key = "portal.design.service.summary.validated";
            } else {
                key = "portal.design.service.summary.errors";
            }
            
            statusLabel = new Label("archiValidatedLabel", new StringResourceModel(key, null));
            add(statusLabel);
            PageParameters newEnvPageParameters = new PageParameters(params);
            newEnvPageParameters.set("new", "1");

            BookmarkablePageLink<Page> newEnv = new BookmarkablePageLink<Page>("newEnvLink", SelectedReleasePage.class, newEnvPageParameters);
            newEnv.setVisible(isLogicalDeploymentConsistent && canCreateEnvironment);
            add(newEnv);

    }

}
