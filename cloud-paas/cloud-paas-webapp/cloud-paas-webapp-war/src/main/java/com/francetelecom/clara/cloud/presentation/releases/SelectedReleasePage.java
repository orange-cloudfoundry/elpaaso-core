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
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.applications.SelectedAppPage;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.environments.EnvironmentCreatePanel;
import com.francetelecom.clara.cloud.presentation.environments.EnvironmentsTablePanel;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 23/08/11
 */
@MountPath("/release/appUid/${appUid}/releaseUid/${releaseUid}")
@AuthorizeInstantiation({"ROLE_USER","ROLE_ADMIN"})
public class SelectedReleasePage extends DesignerHelperPage {
    private static final long serialVersionUID = -1828934947292944878L;

    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(SelectedReleasePage.class);

    @SpringBean
    private ManageEnvironment manageEnvironment;

    @SpringBean
    private ManageApplication manageApplication;

    private ApplicationRelease release;

    private FeedbackPanel globalFeedback;

    private WebMarkupContainer buttonContainer;
    private WebMarkupContainer container;

    private AjaxLink newEnv;

    private EnvironmentsTablePanel envsTablePanel;

    private PageParameters designerParams;

    private List<BreadcrumbsItem> breadcrumbsItems;

    private EnvironmentCreatePanel envCreatePanel;

    /**
     * PageTemplate constructor
     *
     * @param params - page parameters map
     */
    public SelectedReleasePage(PageParameters params) {
        super(params);
        designerParams = new PageParameters();
        designerParams.add("appUid", params.get("appUid"));
        designerParams.add("releaseUid", params.get("releaseUid"));
        initComponents();
    }


    private void initComponents() {

        String releaseUid = getPageParameters().get("releaseUid").toString();
        try {
            release = manageApplicationRelease.findApplicationReleaseByUID(releaseUid);
        } catch (ObjectNotFoundException e) {
            logger.error("Application release not found ; releaseUid={}", releaseUid);
            throw new WicketRuntimeException(e);
        }

        setPagetitle();
        createFirstLevelNavigation();
        createBreadCrumbs();

//        createSecondLevelNavigation();
        createReleaseInformationPanel();
        createGlobalFeedbackPanel();
        createEditArchitectureButton();
        createNewEnvironmentButton();
        if (getPageParameters().getNamedKeys().contains("new")) {
            createEnvironmentFormPanel();
        } else {
            createEmptyEnvironmentFormPanel();
        }

        createEnvironmentsTable();
    }

    private void setPagetitle() {
        /* set head page title to display in browser title bar */
        add(new Label("head_page_title", getString("portal.design.web.title.application.home")));
    }

    private void createFirstLevelNavigation() {
        NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
        add(navFirstLvl);
    }

    private void createBreadCrumbs() {

        breadcrumbsItems = new ArrayList<BreadcrumbsItem>();

        breadcrumbsItems.add(new BreadcrumbsItem(HomePage.class, "portal.breadcrumb.home", null, false));

        PageParameters appPageParameters = new PageParameters();
        appPageParameters.add("appUid", release.getApplication().getUID());
        breadcrumbsItems.add(new BreadcrumbsItem(
                                    SelectedAppPage.class,
                                    appPageParameters,
                                    "portal.breadcrumb.selected.application",
                                    release.getApplication().getLabel(),
                                    false
                            ));

        PageParameters releasePageParameters = new PageParameters();
        releasePageParameters.add("appUid", release.getApplication().getUID());
        releasePageParameters.add("releaseUid", release.getUID());
        breadcrumbsItems.add(new BreadcrumbsItem(
                                    SelectedReleasePage.class,
                                    releasePageParameters,
                                    "portal.breadcrumb.selected.release",
                                    release.getReleaseVersion(),
                                    true
                            ));

        Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        breadcrumbs.setOutputMarkupId(true);
        add(breadcrumbs);

    }

    private void createGlobalFeedbackPanel() {
        globalFeedback = new FeedbackPanel("globalFeedback");
        globalFeedback.setOutputMarkupId(true);
        add(globalFeedback);
    }

    private void createReleaseInformationPanel() {
         // Display application information with modify and delete button
        ReleaseInformationPanel releaseInfoPanel = new ReleaseInformationPanel("releaseInfoPanel", new Model<ApplicationRelease>(release), getPageParameters(), manageApplicationRelease, this);
        try {
            releaseInfoPanel.setCanBeDeleted(manageApplicationRelease.canBeDeleted(release.getUID()));
        } catch (ObjectNotFoundException e) {
            releaseInfoPanel.setCanBeDeleted(false);
        }
        add(releaseInfoPanel);
    }

    private void createEmptyEnvironmentFormPanel() {
        createEnvContainer();
        container.add(new EmptyPanel("createEnvForm"));
        add(container);
    }

    private void createEnvContainer() {
        if (container == null) {
            container = new WebMarkupContainer("createEnvContainer");
            container.setOutputMarkupId(true);
        }
    }

    private void createEditArchitectureButton() {
        buttonContainer = new WebMarkupContainer("buttonContainer");
        buttonContainer.setOutputMarkupId(true);

        //create link
        BookmarkablePageLink editArchitectureBtn = new BookmarkablePageLink("editArchitectureLink", DesignerPage.class, designerParams);
        buttonContainer.add(editArchitectureBtn);
        add(buttonContainer);
    }

    private void createNewEnvironmentButton() {

        //create link
        newEnv = new AjaxLink("newEnvLink") {

            @Override
            public void onClick(AjaxRequestTarget target) {

                createEnvironmentFormPanel();
                target.add(container);

                this.setVisible(false);
                target.add(buttonContainer);

            }
        };
        newEnv.setVisible(release.getApplication().isEditable());

        buttonContainer.add(newEnv);

        add(buttonContainer);
    }

    private void createEnvironmentFormPanel() {
        createEnvContainer();

        envCreatePanel = new EnvironmentCreatePanel("createEnvForm", release, manageApplicationRelease, manageEnvironment, manageApplication);
        container.addOrReplace(envCreatePanel);
        newEnv.setVisible(false);

        add(container);
    }

    private void createEnvironmentsTable() {
        envsTablePanel = new EnvironmentsTablePanel("envsTablePanel", manageApplicationRelease, manageEnvironment, release);
        envsTablePanel.setOutputMarkupId(true);
        envsTablePanel.setVisible(release.getApplication().isPublic() || release.getApplication().isEditable());
        add(envsTablePanel);
    }

    public EnvironmentsTablePanel getEnvironmentsTablePanel() {
        return envsTablePanel;
    }

    public void cancelEnvironmentCreation(AjaxRequestTarget target) {
        resetPage(target);
    }

    private void resetPage(AjaxRequestTarget target) {
        newEnv.setVisible(release.getApplication().isEditable());
        target.add(buttonContainer);

        container.addOrReplace(new EmptyPanel("createEnvForm"));
        target.add(container);
    }

    public ManageEnvironment getManageEnvironment() {
        return manageEnvironment;
    }

    public List<BreadcrumbsItem> getBreadcrumbsItems() {
        return breadcrumbsItems;
    }

	@Override
    public FeedbackPanel getFeedbackPanel() {
        return globalFeedback;
    }

    public void addEnvironmentConfigOverride(LogicalConfigService logicalConfigOverride) {
        envCreatePanel.addEnvironmentConfigOverride(logicalConfigOverride);
    }

    public LogicalConfigService getEnvironmentConfigOverride(String label) {
        return envCreatePanel.getEnvironmentConfigOverride(label);
    }
}
