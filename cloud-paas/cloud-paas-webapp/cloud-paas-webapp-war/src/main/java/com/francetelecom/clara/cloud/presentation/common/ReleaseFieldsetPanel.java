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
package com.francetelecom.clara.cloud.presentation.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.releases.ReleaseForkSelectPanel;
import com.francetelecom.clara.cloud.presentation.releases.ReleaseOverrideProfilePanel;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;


/**
 * ReleaseFieldsetPanel
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public class ReleaseFieldsetPanel extends Panel {

    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ReleaseFieldsetPanel.class);

    private final ManageApplicationRelease manageApplicationRelease;
    private final ManageApplication manageApplication;


    private final Application app;
    private final DesignerHelperPage parentPage;

    private DropDownChoice<Application> applicationDropDownChoice;
    private TextField<String> releaseVersion;
    private ReleaseOverrideProfilePanel overrideProfilePanel;
    private AjaxCheckBox overrideProfileCheckbox;
    private boolean shouldFork = false;
    private ReleaseForkSelectPanel forkSelectPanel;

    /**
     * the dropdown selectapplication must be showed ?
     * - yes if we create a release from a selected application
     * - no if we create a first application release
     * - no if we create a new application release without selected application
     */
    private boolean showApplicationSelection;


    public ReleaseFieldsetPanel(String id, DesignerHelperPage parentPage,
                                Application app,
                                ManageApplication manageApplication,
                                ManageApplicationRelease manageApplicationRelease,
                                boolean showApplicationSelection) {
        super(id);
        this.parentPage = parentPage;
        this.app = app;
        this.manageApplication = manageApplication;
        this.manageApplicationRelease = manageApplicationRelease;
        this.showApplicationSelection = showApplicationSelection;
        initComponents();
    }

    public ReleaseFieldsetPanel(String id, DesignerHelperPage parentPage,ManageApplicationRelease manageApplicationRelease) {
        super(id);
        this.parentPage = parentPage;
        this.manageApplication = null;
        this.manageApplicationRelease = manageApplicationRelease;
        this.app = null;
        this.showApplicationSelection = false;
        initComponents();
    }

    private void initComponents() {
        WebMarkupContainer selectApplicationContainer = new WebMarkupContainer("selectApplication");

        initComponentsApplicationSelect(selectApplicationContainer);
        selectApplicationContainer.setVisible(showApplicationSelection);
        add(selectApplicationContainer);

        releaseVersion = new TextField<>("releaseVersion");
        releaseVersion.setLabel(WicketUtils.getStringResourceModel(this, "portal.release.version.label"));
        releaseVersion.add(new PropertyValidator<>());
        if (showApplicationSelection) {
            initReleaseVersionUniqueValidation();
        }
        add(releaseVersion);

        TextArea<String> releaseDescription = new TextArea<>("description");
        releaseDescription.setLabel(WicketUtils.getStringResourceModel(this, "portal.release.description.label"));
        releaseDescription.add(new PropertyValidator<>());
        add(releaseDescription);

        // clone panel
        forkSelectPanel = new ReleaseForkSelectPanel("forkPanel", parentPage);
        forkSelectPanel.setOutputMarkupId(true);
        forkSelectPanel.setOutputMarkupPlaceholderTag(true);
        forkSelectPanel.setVisible(false);
        add(forkSelectPanel);

        // clone checkbox
        AjaxCheckBox forkCheckbox = new AjaxCheckBox("forkCheckbox", new Model<Boolean>()) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                shouldFork = getModelObject();
                forkSelectPanel.setVisible(shouldFork);
                target.add(forkSelectPanel);
            }
        };
        forkCheckbox.setLabel(WicketUtils.getStringResourceModel(this, "portal.release.creation.fork"));
        add(forkCheckbox);

        // override profile panel
        overrideProfilePanel = new ReleaseOverrideProfilePanel("overrideProfilePanel");
        overrideProfilePanel.setOutputMarkupId(true);
        overrideProfilePanel.setOutputMarkupPlaceholderTag(true);
        overrideProfilePanel.setVisible(false);
        add(overrideProfilePanel);

        // override middleware profile version
        overrideProfileCheckbox = new AjaxCheckBox("overrideProfileCheckbox", new Model<Boolean>(Boolean.FALSE)) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                boolean showOverrideProfile = getModelObject().booleanValue();
                logger.debug("showOverrideProfile:{}", showOverrideProfile);
                overrideProfilePanel.setVisible(showOverrideProfile);
                target.add(overrideProfilePanel);
            }
        };
        //overrideProfileCheckbox.setLabel(WicketUtils.getStringResourceModel(this, "portal.release.creation.overrideProfile"));
        add(overrideProfileCheckbox);
        add(new CacheActivatedImage("overrideProfileHelp", new ResourceModel("image.help").getObject()));
    }

    private void initReleaseVersionUniqueValidation() {
        releaseVersion.add(new AbstractValidator<String>() {
            @Override
            protected void onValidate(IValidatable<String> iValidatable) {
                boolean isUnique;
                String appUID;
                if (app != null) {
                    appUID = app.getUID();
                } else {
                    appUID = applicationDropDownChoice.getRawInput();
                }
                isUnique = manageApplicationRelease.isReleaseVersionUniqueForApplication(appUID, iValidatable.getValue());
                if (!isUnique) {
                    error(iValidatable);
                }
            }

            @Override
            protected String resourceKey() {
                return "portal.release.version.non.unique";

            }

            @Override
            protected Map<String, Object> variablesMap(IValidatable<String> stringIValidatable) {
                Map<String, Object> map = super.variablesMap(stringIValidatable);
                map.put("releaseVersion", stringIValidatable.getValue());
                return map;

            }
        });
    }

    private void initComponentsApplicationSelect(WebMarkupContainer selectApplicationContainer) {
        List<Application> appList = new ArrayList<>();
        if (showApplicationSelection) {
            if (app == null) {
                appList = (List<Application>) manageApplication.findMyApplications();
            } else {
                appList = Arrays.asList(app);
            }
        }
        ChoiceRenderer<Application> choiceRenderer = new ChoiceRenderer<Application>("label", "uid");
        applicationDropDownChoice = new DropDownChoice<Application>("application", appList, choiceRenderer);

        if (app != null) {
            applicationDropDownChoice.setEnabled(false);
        }

        // app required
        applicationDropDownChoice.setRequired(true);
        applicationDropDownChoice.add(new PropertyValidator<>());

        selectApplicationContainer.add(applicationDropDownChoice);
        selectApplicationContainer.add(new CacheActivatedImage("applicationHelp",new ResourceModel("image.help").getObject()));
    }

    public boolean shouldFork() {
        return shouldFork;
    }

    public String getCurrentMiddlewareProfile() {
        if (overrideProfileCheckbox.getModelObject() != null
         && overrideProfileCheckbox.getModelObject()
         && overrideProfilePanel.getCurrentMiddlewareProfile() != null) {
            return overrideProfilePanel.getCurrentMiddlewareProfile().getVersion();
        }
        return null;
    }

    public ApplicationRelease getAppRelease() {
        return forkSelectPanel.getRelease();
    }
}
