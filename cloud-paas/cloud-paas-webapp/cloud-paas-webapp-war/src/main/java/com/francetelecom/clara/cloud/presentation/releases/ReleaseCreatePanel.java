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

import java.net.URL;

import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.application.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.presentation.applications.SelectedAppPage;
import com.francetelecom.clara.cloud.presentation.common.ReleaseFieldsetPanel;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.tools.BusinessExceptionHandler;
import com.francetelecom.clara.cloud.presentation.tools.FieldFeedbackDecorator;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;
import com.francetelecom.clara.cloud.technicalservice.exception.DuplicateApplicationReleaseException;
import com.francetelecom.clara.cloud.technicalservice.exception.InvalidReleaseException;
import com.francetelecom.clara.cloud.technicalservice.exception.ObjectNotFoundException;

/**
 * ReleaseCreatePanel
 *
 * panel used to create an application release
 *
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public class ReleaseCreatePanel extends Panel {
	/**
	 * serialUID
	 */
	private static final long serialVersionUID = -650534211762690554L;
	/**
	 * logger
	 */
	private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ReleaseCreatePanel.class);

	@SpringBean
	private ManageLogicalDeployment mangeLogicalDeployment;

	private final ManageApplication manageApplication;
	private final ManageApplicationRelease manageApplicationRelease;
	private final Application app;
	private final DesignerHelperPage parentPage;

    private Form<ApplicationRelease> releaseForm;
    private ReleaseFieldsetPanel releaseFieldsetPanel;

	public ReleaseCreatePanel(String id, Application app, ManageApplication manageApplication,
                              ManageApplicationRelease manageApplicationRelease,DesignerHelperPage parentPage) {
		super(id);
		this.manageApplication = manageApplication;
		this.manageApplicationRelease = manageApplicationRelease;
		this.app = app;
		this.parentPage = parentPage;
		initComponents();
	}

	private void initComponents() {
		createReleaseForm();
	}

	private void createReleaseForm() {

        ApplicationRelease release = new ApplicationRelease();
        if (app != null) {
            release.setApplication(app);
        }

        releaseForm = new Form<>("releaseForm", new CompoundPropertyModel<ApplicationRelease>(release));

        // if no app is selected then show the drop down
        boolean showSelectedApp = (app == null);
        releaseFieldsetPanel = new ReleaseFieldsetPanel("releaseFieldsetPanel", parentPage, app,
                manageApplication, manageApplicationRelease, showSelectedApp);
        releaseForm.add(releaseFieldsetPanel);

        createFormButtons(releaseForm, releaseFieldsetPanel);

		add(releaseForm);

	}

	private void createFormButtons(Form<ApplicationRelease> applicationReleaseForm, final ReleaseFieldsetPanel releaseFieldsetPanel) {

		// Add first release button
		AjaxButton addButton = new AjaxButton("addReleaseButton") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {

				addReleaseCreation(form, target, releaseFieldsetPanel);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}

		};
        addButton.setOutputMarkupId(true);
		applicationReleaseForm.add(addButton);

		// Cancel release add / update button
		AjaxButton cancelButton = new AjaxButton("cancelReleaseButton") {

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				if (getPage() instanceof SelectedAppPage) {
					((SelectedAppPage) getPage()).cancelReleaseCreation(target);
				} else {
					((ReleasesPage) getPage()).cancelReleaseCreation(target);
				}

			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form) {
				target.add(form);
			}
		};
        cancelButton.setOutputMarkupId(true);
		cancelButton.setDefaultFormProcessing(false);
		applicationReleaseForm.add(cancelButton);

	}

	public void addReleaseCreation(Form<?> form, AjaxRequestTarget target, ReleaseFieldsetPanel releaseFieldsetPanel) {
		PageParameters params = new PageParameters();

		ApplicationRelease releaseDisconnected = (ApplicationRelease) form.getModelObject();
		params.set("appUid", releaseDisconnected.getApplication().getUID());
		String releaseUid;
        String action = "create application release";
        String middlewareProfile = releaseFieldsetPanel.getCurrentMiddlewareProfile();
        boolean shouldFork = releaseFieldsetPanel.shouldFork();
        String applicationUid = releaseDisconnected.getApplication().getUID();
        String appReleaseAuthor = WicketSession.get().getPaasUser().getSsoId().getValue();
        String appReleaseVersion = releaseDisconnected.getReleaseVersion();
        String appReleaseDescription = releaseDisconnected.getDescription();
        URL appReleaseVersionControlUrl = releaseDisconnected.getVersionControlUrl();
        action += " uid="+ applicationUid
                +" appReleaseAuthor=" +appReleaseAuthor
                +" appReleaseVersion=" +appReleaseVersion
                +" middlewareProfile=" +middlewareProfile
                +" appReleaseDescription=" +appReleaseDescription;
        logger.info(action);

        try {
            releaseUid = manageApplicationRelease.createApplicationRelease(
                    applicationUid, appReleaseAuthor, appReleaseVersion,
                    appReleaseDescription, appReleaseVersionControlUrl,
                    middlewareProfile);
			params.set("releaseUid", releaseUid);
		} catch (ObjectNotFoundException e) {
            logger.warn("Error (objectNotFound) while {} : {}", action, e.getMessage());
            BusinessExceptionHandler.addError(target, parentPage.getFeedbackPanel(), e);
            return;
		} catch (DuplicateApplicationReleaseException e) {
            logger.warn("Error (DuplicateApplicationReleaseException) while {} : {}", action, e.getMessage());
            BusinessExceptionHandler.addError(target, parentPage.getFeedbackPanel(), e);
            return;
        }

		if (shouldFork) {
			try {
                ApplicationRelease appReleaseToClone = releaseFieldsetPanel.getAppRelease();
                action += " cloning " + appReleaseToClone.toString();
				mangeLogicalDeployment.cloneLogicalDeployment(appReleaseToClone.getUID(), releaseUid);
			} catch (InvalidReleaseException e) {
                logger.warn("Error (InvalidReleaseException) while {} : {}", action, e.getMessage());
                BusinessExceptionHandler.addError(target, parentPage.getFeedbackPanel(), e);
                return;
            }
		}

		String successMsg = getString("portal.release.creation.successful",
                new Model<Object[]>(new Object[] { releaseDisconnected.getApplication().getLabel(),
				releaseDisconnected.getReleaseVersion() }));
        try {
            action = "going to result page (designer)";
		    DesignerPage designerPage = new DesignerPage(params);
            Session.get().info(successMsg);
            setResponsePage(designerPage);
        } catch (Throwable e) {
			logger.error("Exception while " + action + ":" + e.getMessage(), e);
        }
	}

    @Override
	protected void onInitialize() {
		releaseForm.visitChildren(FormComponent.class, new IVisitor<Component, Void>() {
			@Override
			public void component(Component object, IVisit<Void> visit) {
				object.add(new FieldFeedbackDecorator());
				visit.dontGoDeeper();
			}
		});
		super.onInitialize();
	}
}