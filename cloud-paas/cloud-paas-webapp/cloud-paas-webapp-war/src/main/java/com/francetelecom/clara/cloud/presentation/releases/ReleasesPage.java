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

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;
import java.util.List;

/**
 * ReleasesPage
 *
 * panel used to display all releases and create a new application release
 *
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
@MountPath("/releases")
@AuthorizeInstantiation({"ROLE_USER","ROLE_ADMIN"})
public class ReleasesPage extends DesignerHelperPage {

    private static final long serialVersionUID = 6987787041668276885L;

    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ReleasesPage.class);

    @SpringBean
    private ManageApplication manageApplication;

    private WebMarkupContainer newReleaseContainer;

    private Application app;

    private AjaxLink newRelease;
    private WebMarkupContainer buttonContainer;
    private final DesignerHelperPage parentPage;
    private ReleaseCreatePanel releaseCreatePanel;
    private FeedbackPanel globalFeedback;

    /**
     * PageTemplate constructor
     *
     * @param params - page parameters map
     */
    public ReleasesPage(final PageParameters params) {
        super(params);
        this.parentPage = this;
        initComponents();
        setOutputMarkupId(true);
        app = null;
    }

    @Override
    public FeedbackPanel getFeedbackPanel() {
        return this.globalFeedback;
    }

    private void initComponents() {
        setPageTitle();
        createFirstLevelNavigation();
        createBreadCrumbs();
        createGlobalFeedbackPanel();
        createReleaseFormPanel();
        createNewReleaseButton();
        createReleasesTable();
    }

    private void setPageTitle() {
        /* set head page title to display in browser title bar */
        add(new Label("head_page_title", getString("portal.design.web.title.releases.home")));
    }

    private void createFirstLevelNavigation() {
        NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
        add(navFirstLvl);
    }

    private void createBreadCrumbs() {
        List<BreadcrumbsItem> breadcrumbsItems = new ArrayList<BreadcrumbsItem>();
        breadcrumbsItems.add(new BreadcrumbsItem(HomePage.class, "portal.design.breadcrumbs.homepage", null, false));

        PageParameters params = getPageParameters();
        if (params.getNamedKeys().contains("appUid")) {
            try {
                app = manageApplication.findApplicationByUID(params.get("appUid").toString());
                breadcrumbsItems.add(new BreadcrumbsItem(this.getClass(), params, "portal.design.breadcrumbs.application.release.home", app.getLabel(), true));
            } catch (ObjectNotFoundException e) {
                String errMsg = getString("portal.release.objectnotfound");
                logger.error(errMsg);
                error(errMsg);

                breadcrumbsItems.add(new BreadcrumbsItem(this.getClass(), params, "portal.design.breadcrumbs.releases.home", null, true));
            }

        } else {
//            releasesTablePanel = new ReleasesTablePanel("releasesTablePanel", manageApplicationRelease);
            breadcrumbsItems.add(new BreadcrumbsItem(this.getClass(), params, "portal.design.breadcrumbs.releases.home", null, true));
        }

        Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        add(breadcrumbs);

    }

    private void createGlobalFeedbackPanel() {
        globalFeedback = new FeedbackPanel("globalFeedback");
        globalFeedback.setOutputMarkupId(true);
        add(globalFeedback);
    }

    private void createReleaseFormPanel() {
        newReleaseContainer = new WebMarkupContainer("createReleaseContainer");
        add(newReleaseContainer);
        newReleaseContainer.setOutputMarkupId(true);

        releaseCreatePanel = new ReleaseCreatePanel("createReleaseForm", app,
                manageApplication, manageApplicationRelease, parentPage);

        releaseCreatePanel.setOutputMarkupId(true);
        releaseCreatePanel.setVisible(false);
        newReleaseContainer.add(releaseCreatePanel);
    }

    private void createNewReleaseButton() {
        buttonContainer = new WebMarkupContainer("buttonContainer");
        buttonContainer.setOutputMarkupId(true);

        //create link
        newRelease = new AjaxLink("newReleaseLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                this.setVisible(false);
                target.add(buttonContainer);
                releaseCreatePanel.setVisible(true);
                newReleaseContainer.addOrReplace(releaseCreatePanel);
                target.add(newReleaseContainer);
            }
        };
        newRelease.setMarkupId("newReleaseLink");
        buttonContainer.add(newRelease);
        add(buttonContainer);
    }

    public void cancelReleaseCreation(AjaxRequestTarget target) {
        resetPage(target);
    }

    private void resetPage(AjaxRequestTarget target) {
        // display new release button
        newRelease.setVisible(true);
        target.add(buttonContainer);
        // hide ReleaseCreatePanel
        releaseCreatePanel.setVisible(false);
        newReleaseContainer.addOrReplace(releaseCreatePanel);
        target.add(newReleaseContainer);
    }

    private void createReleasesTable() {
        ReleasesTablePanel releasesTablePanel = new ReleasesTablePanel("releasesTablePanel", manageApplicationRelease, null);
        releasesTablePanel.setOutputMarkupId(true);
        add(releasesTablePanel);
    }
}