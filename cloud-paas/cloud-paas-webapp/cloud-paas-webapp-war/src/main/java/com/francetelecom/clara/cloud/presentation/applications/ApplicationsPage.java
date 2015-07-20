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

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import com.francetelecom.clara.cloud.commons.AuthorizationException;
import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.SSOId;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.common.WicketUtils;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.models.FirstApplicationReleaseInfos;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.presentation.tools.BusinessExceptionHandler;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationException;
import com.francetelecom.clara.cloud.coremodel.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.InvalidReleaseException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.PaasUserNotFoundException;

/**
 * ApplicationsPage
 * <p>
 * panel used to create an application release
 * <p>
 * Last update : $LastChangedDate$ Last author : $Author$
 * 
 * @version : $Revision$
 */
@MountPath("/applications")
@AuthorizeInstantiation({"ROLE_USER","ROLE_ADMIN"})
public class ApplicationsPage extends DesignerHelperPage {

	private static final long serialVersionUID = -1549651870557920802L;

	private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ApplicationsPage.class);

	public static SSOId[] toSSOIdsArray(String membersStr) {
		membersStr = membersStr.replaceAll("\\s+", " ").trim();
		String[] members = membersStr == null ? new String[0] : membersStr.split(" ");
		SSOId[] ssoIds = new SSOId[members.length];
		for (int i = 0; i < members.length; i++) {
			ssoIds[i] = new SSOId(members[i]);
		}
		return ssoIds;
	}
	public static Set<SSOId> toSSOIdsSet(String membersStr) {
		membersStr = membersStr.replaceAll("\\s+", " ").trim();
		String[] members = membersStr == null ? new String[0] : membersStr.split(" ");
		Set<SSOId> ssoIds = new HashSet<>();
		for (int i = 0; i < members.length; i++) {
			ssoIds.add(new SSOId(members[i]));
		}
		return ssoIds;
	}

	@SpringBean
	private ManageApplication manageApplication;

	private WebMarkupContainer container;

	private final PageParameters params = new PageParameters();

	private ApplicationCreatePanel appCreatePanel;

	AjaxLink newApp;
	WebMarkupContainer buttonContainer;
	ApplicationsTablePanel appsTablePanel;
	FeedbackPanel feedback;

	public ApplicationsPage(PageParameters params) {
		super(params);
		initComponents();
	}

	private void initComponents() {

		setPagetitle();
		createFirstLevelNavigation();
		createBreadCrumbs();
		createGlobalFeedbackPanel();
		createEmptyAppFormPanel();
		createNewAppButton();
		createAppsTable();

	}

	private void createFirstLevelNavigation() {
		NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
		add(navFirstLvl);
	}

	private void setPagetitle() {
		/* set head page title to display in browser title bar */
		add(new Label("head_page_title", WicketUtils.getStringResourceModel(this, "portal.design.web.title.applications.home")));
	}

	private void createBreadCrumbs() {

		List<BreadcrumbsItem> breadcrumbsItems = new ArrayList<BreadcrumbsItem>();

		breadcrumbsItems.add(new BreadcrumbsItem(HomePage.class, "portal.design.breadcrumbs.homepage", null, false));
		breadcrumbsItems.add(new BreadcrumbsItem(this.getClass(), params, "portal.design.breadcrumbs.applications.home", null, true));

		Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
		add(breadcrumbs);

	}

	private void createGlobalFeedbackPanel() {
		feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

	}

	private void createEmptyAppFormPanel() {

		container = new WebMarkupContainer("createAppContainer");
		container.setOutputMarkupId(true);
		container.setOutputMarkupPlaceholderTag(true);
		container.add(new EmptyPanel("appCreateForm"));

		add(container);
	}

	private void createNewAppButton() {

		buttonContainer = new WebMarkupContainer("buttonContainer");
		buttonContainer.setOutputMarkupId(true);

		// create link
		newApp = new AjaxLink("newAppLink") {

			private static final long serialVersionUID = 4466738576333544274L;

			@Override
			public void onClick(AjaxRequestTarget target) {

				appCreatePanel = new ApplicationCreatePanel("appCreateForm", manageApplicationRelease);

				// container.remove("appCreateForm");
				// container.add(appCreatePanel);

				container.replace(appCreatePanel);
				target.add(container);

				this.setVisible(false);
				target.add(buttonContainer);

			}
		};
		newApp.setMarkupId("newAppLink");
		buttonContainer.add(newApp);
		add(buttonContainer);

	}

	private void createAppsTable() {
		appsTablePanel = new ApplicationsTablePanel("appsTablePanel");
		appsTablePanel.setOutputMarkupId(true);
		add(appsTablePanel);
	}

	public void addFirstApplicationCreation(Form<?> form, AjaxRequestTarget target, ApplicationRelease appRelease, String currentMiddlewareProfile) {
		String action = "create first application ";
		FirstApplicationReleaseInfos appFirstApplicationReleaseInfos = (FirstApplicationReleaseInfos) form.getModelObject();

		String appUid;
		try {
			String applicationCode = appFirstApplicationReleaseInfos.getAppCode();
			String applicationLabel = appFirstApplicationReleaseInfos.getAppLabel();
			String applicationDescription = appFirstApplicationReleaseInfos.getAppDescription();
			Boolean applicationPublic = appFirstApplicationReleaseInfos.getAppPublic();
			SSOId[] members = toSSOIdsArray(appFirstApplicationReleaseInfos.getMembers());
			action += " appCode=" + applicationCode + " appLabel=" + applicationLabel + " appDescription=" + applicationDescription + " appPublic="
					+ applicationPublic + " users=" + Arrays.toString(members);
			logger.info(action);
			if(applicationPublic.booleanValue()) {
				appUid = manageApplication.createPublicApplication(applicationCode, applicationLabel, applicationDescription, null, members);
			} else {
				appUid = manageApplication.createPrivateApplication(applicationCode, applicationLabel, applicationDescription, null, members);
			}
			params.set("appUid", appUid);

		} catch (DuplicateApplicationException e) {
			logger.warn("Error (DuplicateApplicationException) while {} : {}", action, e.getMessage());
			BusinessExceptionHandler.addError(target, feedback, e);
			return;
		} catch (PaasUserNotFoundException e) {
			logger.warn("Error (PaasUserNotFoundException) while {} : {}", action, e.getMessage());
			BusinessExceptionHandler.addError(target, feedback, e);
			return;
		} catch (AuthorizationException e) {
			logger.warn("Error (AuthorizationException) while {} : {}", action, e.getMessage());
			BusinessExceptionHandler.addError(target, feedback, new BusinessException(getString("portal.application.notmember")));
			return;
		}

		String releaseUid;
		try {
			action = "create first application release";
			String releaseUser = WicketSession.get().getPaasUser().getSsoId().getValue();
			String releaseVersion = appFirstApplicationReleaseInfos.getReleaseVersion();
			String releaseDescription = appFirstApplicationReleaseInfos.getDescription();
			URL releaseVersionControlUrl = appFirstApplicationReleaseInfos.getVersionControlUrl();
			action += " releaseUser=" + releaseUser + " releaseVersion=" + releaseVersion + " releaseMiddlewareProfile=" + currentMiddlewareProfile
					+ " releaseDescription=" + releaseDescription;
			logger.info(action);
			// Creates and persists release
			releaseUid = manageApplicationRelease.createApplicationRelease(appUid, releaseUser, releaseVersion, releaseDescription,
					releaseVersionControlUrl, currentMiddlewareProfile);
			params.set("releaseUid", releaseUid);
		} catch (ObjectNotFoundException e) {
			BusinessExceptionHandler.addError(target, feedback, e);
			return;
		} catch (DuplicateApplicationReleaseException e) {
			BusinessExceptionHandler.addError(target, feedback, e);
			return;
		}

		try {
			if (appCreatePanel.shouldFork()) {
				action += " cloning " + appRelease.toString();
				manageLogicalDeployment.cloneLogicalDeployment(appRelease.getUID(), releaseUid);
			}
		} catch (InvalidReleaseException e) {
			logger.warn("Error (InvalidReleaseException) while {} : {}", action, e.getMessage());
			BusinessExceptionHandler.addError(target, feedback, e);
			return;
		}

		String successMsg = getString("portal.application.creation.successful",
				new Model<Object[]>(
						new Object[] { appFirstApplicationReleaseInfos.getAppLabel(), appFirstApplicationReleaseInfos.getReleaseVersion() }));
		try {
			action = "going to result page (designer)";
			DesignerPage designerPage = new DesignerPage(params);
			Session.get().info(successMsg);
			setResponsePage(designerPage);
		} catch (Throwable e) {
			logger.error("Exception while " + action + ":" + e.getMessage(), e);
		}
	}

	public void cancelFirstApplicationCreation(AjaxRequestTarget target) {
		resetPage(target);
	}

	private void resetPage(AjaxRequestTarget target) {
		newApp.setVisible(true);
		target.add(buttonContainer);

		container.replace(new EmptyPanel("appCreateForm"));
		target.add(container);
	}

	public void deleteApplication(AjaxRequestTarget target, Application app) {
		try {
			manageApplication.deleteApplication(app.getUID());
			target.add(appsTablePanel);
		} catch (ObjectNotFoundException e) {
			BusinessExceptionHandler handler = new BusinessExceptionHandler(feedback);
			handler.error(e);
		}
	}

	public ManageApplication getManageApplication() {
		return manageApplication;
	}

	// We need this method for wicket tester

	@Override
	public PageParameters getPageParameters() {
		return params;
	}

	@Override
	public FeedbackPanel getFeedbackPanel() {
		return this.feedback;
	}

	public boolean isApplicationLabelUnique(String appLabel) {
		try {
			return manageApplication.isApplicationLabelUnique(appLabel);
		} catch (BusinessException e) {
			BusinessExceptionHandler handler = new BusinessExceptionHandler(feedback);
			handler.error(e);
			return false;
		}
	}
}
