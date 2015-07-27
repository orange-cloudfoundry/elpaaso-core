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
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.deployment.logical.service.ManageLogicalDeployment;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.panels.DesignerArchitectureMatrixPanel;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * User: wwnl9733
 * Date: 25/10/11
 * Time: 15:31
 */
public class ReleaseForkSelectPanel extends Panel {
    private static final long serialVersionUID = 8165234888427238880L;

	private static final Logger logger = LoggerFactory.getLogger(ReleaseForkSelectPanel.class);

    @SpringBean
    private ManageApplication manageApplication;

    @SpringBean
    private ManageApplicationRelease manageApplicationRelease;

    @SpringBean
    private ManageLogicalDeployment manageLogicalDeployment;

    private DropDownChoice<ApplicationRelease> releaseSelect;
    private DropDownChoice<Application> applicationSelect;
    private WebMarkupContainer appContainer;
    private WebMarkupContainer releaseContainer;
    private WebMarkupContainer appDescriptionContainer;

    private Label appDescriptionLabel;
    private WebMarkupContainer architectureContainer;

    private DesignerHelperPage parentPage;
    private DesignerArchitectureMatrixPanel envArchitecturePanel;

    public ReleaseForkSelectPanel(String id, DesignerHelperPage parentPage) {
        super(id);
        this.parentPage = parentPage;
        initComponents();
    }

    private void initComponents() {
        initContainers();
        //Select appliction choice
        ChoiceRenderer<Application> applicationChoice = new ChoiceRenderer<>("label", "uid");
        applicationSelect = new DropDownChoice<Application>("appSelect", new Model<Application>(), getApplicationList(), applicationChoice);
        applicationSelect.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                Application selectedApp = applicationSelect.getModelObject();
                boolean isSelected = selectedApp != null;
                updateContainerVisibility(isSelected, false, target);
                if (isSelected) {
                    updateReleaseSelect(selectedApp.getUID(), target);
                    updateAppDescription(selectedApp.getUID(), target);
                }
            }
        });
        applicationSelect.setNullValid(true);
        appContainer.add(applicationSelect);

        //Select Release version choice
        ChoiceRenderer<ApplicationRelease> applicationReleaseVersionChoice = new ChoiceRenderer<>("releaseVersion", "uid");
        releaseSelect = new DropDownChoice<ApplicationRelease>("releaseSelect", new Model<ApplicationRelease>(), (List) null, applicationReleaseVersionChoice);
        releaseSelect.add(new OnChangeAjaxBehavior() {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                ApplicationRelease selectedRelease = releaseSelect.getModelObject();
                boolean isSelected = selectedRelease != null;
                updateMatrixContainerVisibility(isSelected, target);
                if (isSelected) {
                    try {
                        parentPage.getLogicalDeploymentPersisted(selectedRelease.getUID());
                        envArchitecturePanel.updateTable();
                    } catch (ObjectNotFoundException e) {
						logger.info("Logical deployment not found for release: " + selectedRelease.getUID(), e);
                    }
                }
            }
        });
        releaseSelect.setNullValid(true);
        releaseContainer.add(releaseSelect);

        //Description
        appDescriptionLabel = new Label("appDescription", "placeholder");
        appDescriptionContainer.add(appDescriptionLabel);

        //Matrix
        envArchitecturePanel = new DesignerArchitectureMatrixPanel("archi", parentPage, true, false);
        architectureContainer.add(envArchitecturePanel);

    }

    private List<Application> getApplicationList() {
        return (List<Application>) manageApplication.findAccessibleApplications();
    }


    private void initContainers() {
        appContainer = new WebMarkupContainer("appContainer");
        appContainer.setOutputMarkupPlaceholderTag(true);
        appContainer.setOutputMarkupId(true);
        add(appContainer);

        releaseContainer = new WebMarkupContainer("releaseContainer");
        releaseContainer.setOutputMarkupPlaceholderTag(true);
        releaseContainer.setOutputMarkupId(true);
        releaseContainer.setVisible(false);
        add(releaseContainer);

        appDescriptionContainer = new WebMarkupContainer("appDescriptionContainer");
        appDescriptionContainer.setOutputMarkupPlaceholderTag(true);
        appDescriptionContainer.setOutputMarkupId(true);
        appDescriptionContainer.setVisible(false);
        add(appDescriptionContainer);

        architectureContainer = new WebMarkupContainer("architectureContainer");
        architectureContainer.setOutputMarkupPlaceholderTag(true);
        architectureContainer.setOutputMarkupId(true);
        architectureContainer.setVisible(false);
        add(architectureContainer);
    }


    private void updateContainerVisibility(boolean isVisible, boolean isReleaseSelected, AjaxRequestTarget target) {
        releaseContainer.setVisible(isVisible);
        appDescriptionContainer.setVisible(isVisible);
        if (!isVisible || isReleaseSelected) {
            updateMatrixContainerVisibility(isVisible, target);
        }
        addContainersToTarget(target);
    }

    private void addContainersToTarget(AjaxRequestTarget target) {
        target.add(releaseContainer);
        target.add(appDescriptionContainer);
        target.add(architectureContainer);
    }

    private void updateMatrixContainerVisibility(boolean isVisible, AjaxRequestTarget target) {
        architectureContainer.setVisible(isVisible);
        addContainersToTarget(target);
    }

    private void updateReleaseSelect(String appUid, AjaxRequestTarget target) {
        List<ApplicationRelease> releaseList = null;
        releaseSelect.setChoices(releaseList);
        updateMatrixContainerVisibility(false, target);
        try {
            releaseList = manageApplicationRelease.findApplicationReleasesByAppUID(appUid);
            releaseSelect.setChoices(releaseList);
        } catch (ObjectNotFoundException e) {
			logger.info("Application not found with AppId: " + appUid, e);
		}
        addContainersToTarget(target);
    }

    private void updateAppDescription(String appUid, AjaxRequestTarget target){
        try {
            String description = manageApplication.findApplicationByUID(appUid).getDescription();
            appDescriptionLabel.setDefaultModelObject(description == null ? getString("portal.release.creation.fork.app.description.empty") : description);
        } catch (ObjectNotFoundException e) {
			logger.info("Application not found for application uid: " + appUid, e);
        }
        addContainersToTarget(target);
    }

    public ApplicationRelease getRelease() {
        return releaseSelect.getModelObject();
    }
}
