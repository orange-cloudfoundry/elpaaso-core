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

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: lzxv3002
 * Date: 29/07/11
 * Time: 17:51
 * To change this template use File | Settings | File Templates.
 */
@MountPath("/environments")
@AuthorizeInstantiation({"ROLE_USER","ROLE_ADMIN"})
public class EnvironmentsPage extends DesignerHelperPage {
    /**
     * serialUID
     */
    private static final long serialVersionUID = -2297075104182302118L;
    /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(EnvironmentsPage.class);

    @SpringBean
    private ManageEnvironment manageEnvironment;

    @SpringBean
    private ManageApplication manageApplication;

    private WebMarkupContainer container;

    private FeedbackPanel globalFeedback;
    private AjaxLink newEnvironment;
    private WebMarkupContainer buttonContainer;

    private EnvironmentsTablePanel environmentsTablePanel;

    private EnvironmentCreatePanel envCreatePanel;

    public EnvironmentsPage(final PageParameters params) {
        super(params);
        initComponents();
    }

    private void initComponents() {
        setPagetitle();
        createFirstLevelNavigation();
        createBreadCrumbs();
        createGlobalFeedbackPanel();
        createEmptyEnvironmentsFormPanel();
        createNewEnvironmentButton();
        createEnvironmentsTable();
    }

    private void setPagetitle() {
        /* set head page title to display in browser title bar */
        add(new Label("head_page_title", getString("portal.design.web.title.environments.home")));
    }

    private void createFirstLevelNavigation() {
        NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
        add(navFirstLvl);
    }

    private void createBreadCrumbs() {
        List<BreadcrumbsItem> breadcrumbsItems = new ArrayList<>();
        breadcrumbsItems.add(new BreadcrumbsItem(HomePage.class, getPageParameters(), "portal.design.breadcrumbs.homepage", null, false));
        breadcrumbsItems.add(new BreadcrumbsItem(this.getClass(), getPageParameters(), "portal.design.breadcrumbs.release.environment.home", null, true));
        Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        add(breadcrumbs);
    }


    private void createGlobalFeedbackPanel() {
//        globalFeedback = new FeedbackPanel("globalFeedback", new ComponentFeedbackMessageFilter(this));
        globalFeedback = new FeedbackPanel("globalFeedback");
        globalFeedback.setOutputMarkupId(true);
        add(globalFeedback);
    }

    private void createEmptyEnvironmentsFormPanel() {
        container = new WebMarkupContainer("createEnvironmentContainer");
        container.setOutputMarkupId(true);
        container.add(new EmptyPanel("createEnvironmentForm"));
        add(container);
    }

    private void createNewEnvironmentButton() {
        buttonContainer = new WebMarkupContainer("buttonContainer");
        buttonContainer.setOutputMarkupId(true);

        //create link
        newEnvironment = new AjaxLink("newEnvLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {

                envCreatePanel = new EnvironmentCreatePanel("createEnvironmentForm", null, manageApplicationRelease, manageEnvironment, manageApplication);

                container.addOrReplace(envCreatePanel);
                target.add(container);

                this.setVisible(false);
                target.add(buttonContainer);

            }
        };
        newEnvironment.setMarkupId("newEnvLink");
        buttonContainer.add(newEnvironment);
        add(buttonContainer);
    }


    private void createEnvironmentsTable() {
        environmentsTablePanel = new EnvironmentsTablePanel("envsTablePanel", manageApplicationRelease, manageEnvironment, null);
        environmentsTablePanel.setOutputMarkupId(true);
        add(environmentsTablePanel);
    }

    public void cancelEnvironmentCreation(AjaxRequestTarget target) {
        resetPage(target);
    }

    private void resetPage(AjaxRequestTarget target) {
        newEnvironment.setVisible(true);
        target.add(buttonContainer);

        container.addOrReplace(new EmptyPanel("createEnvironmentForm"));
        target.add(container);
    }

    public FeedbackPanel getFeedbackPanel() {
        return globalFeedback;
    }

    public EnvironmentsTablePanel getEnvironmentsTablePanel() {
        return environmentsTablePanel;
    }

    public ManageEnvironment getManageEnvironment() {
        return manageEnvironment;
    }

    public void addEnvironmentConfigOverride(LogicalConfigService logicalConfigOverride) {
        envCreatePanel.addEnvironmentConfigOverride(logicalConfigOverride);
    }

    public LogicalConfigService getEnvironmentConfigOverride(String label) {
        return envCreatePanel.getEnvironmentConfigOverride(label);
    }
}
