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

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.core.service.exception.PaasUserNotFoundException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.releases.ReleaseCreatePanel;
import com.francetelecom.clara.cloud.presentation.releases.ReleasesTablePanel;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.presentation.tools.BusinessExceptionHandler;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA. User: Thomas Escalle - tawe8231 Entity :
 * FT/OLNC/RD/MAPS/MEP/MSE Date: 23/08/11
 */
@MountPath("/application/appUid/${appUid}")
@AuthorizeInstantiation({"ROLE_USER","ROLE_ADMIN"})
public class SelectedAppPage extends DesignerHelperPage {

	private static final long serialVersionUID = -7169645938227997056L;
	private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(SelectedAppPage.class);
	@SpringBean
	private ManageApplication manageApplication;
	private WebMarkupContainer buttonContainer;
	private WebMarkupContainer container;
	private ApplicationInformationPanel appInfoPanel;
	private AjaxLink newRelease;
	private Application app;
	private FeedbackPanel globalFeedback;
	private ReleasesTablePanel releasesTablePanel;
    private List<BreadcrumbsItem> breadcrumbsItems;

	public SelectedAppPage(final PageParameters params) {
		super(params);
	}

    @Override
    protected void onInitialize() {
        initComponents();
        setOutputMarkupId(true);
        super.onInitialize();
    }

    @Override
    public FeedbackPanel getFeedbackPanel() {
        return this.globalFeedback;
    }

    private void initComponents() {
        loadSelectedApplication();
		setPagetitle();
		createFirstLevelNavigation();
		createBreadCrumbs();
		createGlobalFeedbackPanel();
		createApplicationInformationPanel();
		createEmptyReleaseFormPanel();
		createNewReleaseButton();
		createReleasesTable();
	}

    private void loadSelectedApplication() {
        String appUid = getPageParameters().get("appUid").toString();
        try {
            app = manageApplication.findApplicationByUID(appUid);
		} catch (ApplicationNotFoundException e) {
            logger.error("Application not found ; appUid={}", appUid);
            throw new WicketRuntimeException(e);
		}
    }

	private void createReleasesTable() {
		releasesTablePanel = new ReleasesTablePanel("releasesTablePanel", manageApplicationRelease, app);
		releasesTablePanel.setOutputMarkupId(true);
		add(releasesTablePanel);
		
		releasesTablePanel.setVisible(app.isPublic() || app.isEditable());
	}

	private void createNewReleaseButton() {
		buttonContainer = new WebMarkupContainer("buttonContainer");
		buttonContainer.setOutputMarkupId(true);

		// create link
		newRelease = new AjaxLink("newReleaseLink") {

			@Override
			public void onClick(AjaxRequestTarget target) {

				ReleaseCreatePanel releaseCreatePanel = new ReleaseCreatePanel("createReleaseForm", app, manageApplication, manageApplicationRelease, SelectedAppPage.this);

				container.addOrReplace(releaseCreatePanel);
				target.add(container);

				this.setVisible(false);
				target.add(buttonContainer);

			}
		};
		newRelease.setMarkupId("newReleaseLink");
		buttonContainer.add(newRelease);
		
		buttonContainer.setVisible(app.isEditable());
		
		add(buttonContainer);
	}

	private void createApplicationInformationPanel() {
		// Display application information with modify and delete button
		appInfoPanel = new ApplicationInformationPanel("appInfoPanel", app, getPageParameters(), this);
		appInfoPanel.setMembers(app.listMembers());
		try {
			appInfoPanel.setCanBeDeleted(manageApplication.canBeDeleted(app.getUID()));
		} catch (ApplicationNotFoundException e) {
			appInfoPanel.setCanBeDeleted(false);
		}
		add(appInfoPanel);
	}
	
	public void updateAppInfopanel(AjaxRequestTarget target) {
		try {
			appInfoPanel.setCanBeDeleted(manageApplication.canBeDeleted(app.getUID()), target);
		} catch (ApplicationNotFoundException e) {
			appInfoPanel.setCanBeDeleted(false, target);
		}
	}

	private void createEmptyReleaseFormPanel() {
		container = new WebMarkupContainer("createReleaseContainer");
		container.setOutputMarkupId(true);
		container.add(new EmptyPanel("createReleaseForm"));
		add(container);
	}

	private void createGlobalFeedbackPanel() {
		globalFeedback = new FeedbackPanel("globalFeedback");
		globalFeedback.setOutputMarkupId(true);
		add(globalFeedback);
	}

	private void createBreadCrumbs() {
		breadcrumbsItems = new ArrayList<>();
		breadcrumbsItems.add(new BreadcrumbsItem(HomePage.class, "portal.breadcrumb.home", null, false));
		PageParameters appPageParameters = new PageParameters();
		appPageParameters.add("appUid", app.getUID());
		breadcrumbsItems.add(new BreadcrumbsItem(SelectedAppPage.class, appPageParameters, "portal.breadcrumb.selected.application", app.getLabel(), true));
		Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        breadcrumbs.setOutputMarkupId(true);
        add(breadcrumbs);

	}

	public void deleteApplication(AjaxRequestTarget target, Application appToDelete) {
		try {
			manageApplication.deleteApplication(appToDelete.getUID());
		} catch (ApplicationNotFoundException e) {
			BusinessExceptionHandler.addError(target, globalFeedback, e);
		}
	}

	public void updateApplication(AjaxRequestTarget target, Form<Application> form, String members) throws ApplicationNotFoundException,
	        DuplicateApplicationException, PaasUserNotFoundException {

		Application appToUpdate = form.getModelObject();
		appToUpdate.setMembers(ApplicationsPage.toSSOIdsSet(members));

		logger.debug("Update application {}", appToUpdate);
		app = manageApplication.updateApplication(appToUpdate);

		form.setDefaultModel(new CompoundPropertyModel<Application>(app));
		info(getString("portal.application.update.successful", new Model<Object[]>(new Object[] { app.getLabel() })));
		target.add(globalFeedback);
		target.add(releasesTablePanel);
	}

	private void createFirstLevelNavigation() {
		NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
		add(navFirstLvl);
	}

	private void setPagetitle() {
		/* set head page title to display in browser title bar */
		add(new Label("head_page_title", getString("portal.design.web.title.application.home")));
	}

	public void cancelReleaseCreation(AjaxRequestTarget target) {
		resetPage(target);
	}

	private void resetPage(AjaxRequestTarget target) {
		newRelease.setVisible(true);
		target.add(buttonContainer);
		container.addOrReplace(new EmptyPanel("createReleaseForm"));
		target.add(container);
	}

    public List<BreadcrumbsItem> getBreadcrumbsItems() {
        return breadcrumbsItems;
    }
}
