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

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.InvalidMavenReferenceException;
import com.francetelecom.clara.cloud.commons.MavenReference;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.logicalmodel.*;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.applications.SelectedAppPage;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.common.WicketUtils;
import com.francetelecom.clara.cloud.presentation.designer.panels.*;
import com.francetelecom.clara.cloud.presentation.designer.support.LogicalServicesHelper;
import com.francetelecom.clara.cloud.presentation.releases.ReleasesPage;
import com.francetelecom.clara.cloud.presentation.releases.SelectedReleasePage;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.presentation.tools.BusinessExceptionHandler;
import org.apache.wicket.ajax.AjaxRequestHandler;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
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

@MountPath("/architecture/appUid/${appUid}/releaseUid/${releaseUid}")
@AuthorizeInstantiation({"ROLE_USER","ROLE_ADMIN"})
public class DesignerPage extends DesignerHelperPage {

	private static final long serialVersionUID = -807251722277505006L;

	/**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(DesignerPage.class);

    @SpringBean
    private LogicalServicesHelper logicalServicesHelper;

    /**
     * Global page feedback panel
     */
    FeedbackPanel feedback;

    /**
     * Selected Application Release
     */
    ApplicationRelease appRelease;

    private DesignerSteppedProcessPanel steppedProcess;
    private WebMarkupContainer firstPartContainer;
    private WebMarkupContainer lockIconContainer;
    private DesignerSteppedButtonsPanel stepButtonsPanel;
    private DesignerArchitectureMatrixPanel matrixPanel;

    private DesignerServiceDefinitionPanel serviceDefinitionPanel;

    private int stepProcess;

    /**
     * PageTemplate constructor
     *
     * @param params - page parameters map
     */
    public DesignerPage(PageParameters params) {
        super(params);
        this.stepProcess = 0;
        initComponents();

    }

    @Override
    protected void onBeforeRender() {
        AjaxRequestTarget target = new AjaxRequestHandler(this);
        managePageComponents(target, stepProcess, null);

        super.onBeforeRender();
    }

    private void initComponents() {
        setPageHeaderTitle("first");
        setBreadcrumb();
        setGlobalPageFeedback();
        setFirstLevelNavigationMenu();

        setProcessPipeline(stepProcess);
        initLockImg();
        initFirstPartContainer();
        initArchitectureMatrix();
        initStepButtons();
    }

    private void setPageHeaderTitle(String step) {
        add(new Label("head_page_title",
                WicketUtils.getStringResourceModel(this, "portal.design.web.title.release.architecture",
                        new Model<String[]>(new String[]{step}))));
    }

    private void setGlobalPageFeedback() {
        feedback = new FeedbackPanel("feedback");
        feedback.setOutputMarkupId(true);
        add(feedback);
    }

    private void setFirstLevelNavigationMenu() {
        NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
        add(navFirstLvl);
    }

    /**
     * Set the architecture step one breadcrumb
     */
    private void setBreadcrumb() {
        PageParameters params = getPageParameters();
        String releaseUid = params.get("releaseUid").toString();

        List<BreadcrumbsItem> breadcrumbsItems = new ArrayList<BreadcrumbsItem>();
        breadcrumbsItems.add(new BreadcrumbsItem(HomePage.class, "portal.design.breadcrumbs.homepage", null,false));

        try {
            logger.debug("init breadcrumb for release={}", releaseUid);
            appRelease  = manageApplicationRelease.findApplicationReleaseByUID(releaseUid);
            breadcrumbsItems.add(
                    new BreadcrumbsItem(
                            SelectedAppPage.class,
                            params,
                            "portal.design.breadcrumbs.application.home",
                            appRelease.getApplication().getLabel(),
                            false
                    ));
            breadcrumbsItems.add(
                    new BreadcrumbsItem(
                            SelectedReleasePage.class,
                            params,
                            "portal.design.breadcrumbs.release.home",
                            appRelease.getReleaseVersion(),
                            false
                    )
            );
            breadcrumbsItems.add(
                    new BreadcrumbsItem(
                            this.getClass(),
                            params,
                            "portal.design.breadcrumbs.release.architecture",
                            null,
                            true
                    )
            );
        } catch (ObjectNotFoundException e) {
            String errMsg = getString("portal.release.objectnotfound");
            logger.error("{} releaseUid={}", errMsg, releaseUid);
            error(errMsg);

            breadcrumbsItems.add(
                    new BreadcrumbsItem(
                            ReleasesPage.class,
                            "portal.design.breadcrumbs.release.home",
                            getString("portal.design.breadcrumbs.release.unknown"),
                            true
                    )
            );
        }
        Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        add(breadcrumbs);
    }



    /**
     * set the process pipeline
     */
    private void setProcessPipeline(int processStep) {
        steppedProcess = new DesignerSteppedProcessPanel("process", processStep);
        add(steppedProcess);
        steppedProcess.setOutputMarkupId(true);
    }



    /**
     * initialize lock icon container which will contains lock icon png
     * This icon will be visible if architecture is in validated state
     */
    private void initLockImg() {
        lockIconContainer = new WebMarkupContainer("lockImg") {
			private static final long serialVersionUID = -122889791302680834L;

			@Override
            public boolean isVisible() {
                return isArchitectureLocked();
            }
        };
        lockIconContainer.setOutputMarkupPlaceholderTag(true);
        add(lockIconContainer);
    }


    /**
     * initialize web container which will contains
     *   - service selection component
     *   - service definition form
     *   - architecture validation result
     *   - config service validation result
     */
    private void initFirstPartContainer() {
        firstPartContainer = new WebMarkupContainer("firstPartContainer");
        firstPartContainer.setOutputMarkupId(true);
        add(firstPartContainer);
    }

    /**
     * initialize component which content button to go to next or previous step
     * of the process pipeline
     */
    private void initStepButtons() {
        stepButtonsPanel = new DesignerSteppedButtonsPanel("stepButtonsPanel", this, isArchitectureLocked());
        add(stepButtonsPanel);
    }

    /**
     * initialize the architecture matrix
     */
    private void initArchitectureMatrix() {
        Label summaryMatrixLabel = new Label("matrixPanelLabel", new Model<String>(getString("portal.design.service.configure.title")));
        add(summaryMatrixLabel);

        matrixPanel = new DesignerArchitectureMatrixPanel("matrix", this, isArchitectureLocked(), false);
        add(matrixPanel);
    }

    public void managePageComponents(AjaxRequestTarget target, int step, LogicalModelItem logicalService) {
        if (isArchitectureLocked()) {
            step = 2;
        }

        updateProcessPipelinePanel(target, step);
        updateFirstPartContainer(target, step, logicalService);
        updateProcessButtonPanel(target, step);
        updateMatrixPanel(target);
    }

    private void updateProcessPipelinePanel(AjaxRequestTarget target, int step) {
        steppedProcess.setCurrentStep(step);
        target.add(steppedProcess);
    }

    private void updateFirstPartContainer(AjaxRequestTarget target, int step, LogicalModelItem service) {
        if (step == 2) {

            PageParameters params = getPageParameters();

            EmptyPanel emptyPanel1 = new EmptyPanel("serviceDefinitionPanel");
            firstPartContainer.addOrReplace(emptyPanel1);

            DesignerArchitectureSummaryPanel summaryPanel = new DesignerArchitectureSummaryPanel("architectureSummaryPanel", params, isArchitectureLocked(), checkLogicalDeploymentOverallConsistency(), hasWritePermission());
            firstPartContainer.addOrReplace(summaryPanel);

            DesignerArchitectureConfigSetPanel configSummaryPanel = new DesignerArchitectureConfigSetPanel("configServiceSummaryPanel", this, params);
            firstPartContainer.addOrReplace(configSummaryPanel);

        } else {
            CompoundPropertyModel<List<LogicalModelItem>> compoundPropertyModel;
            if (step == 0) {
                compoundPropertyModel = new CompoundPropertyModel<>(logicalServicesHelper.getExternalServices());
            } else {
                compoundPropertyModel = new CompoundPropertyModel<>(logicalServicesHelper.getInternalServices());
            }
            serviceDefinitionPanel = new DesignerServiceDefinitionPanel("serviceDefinitionPanel", compoundPropertyModel, isArchitectureLocked(), step, this);
            firstPartContainer.addOrReplace(serviceDefinitionPanel);
            updateServiceDefinitionPanel(target, service, false);

            EmptyPanel emptyPanel2 = new EmptyPanel("architectureSummaryPanel");
            firstPartContainer.addOrReplace(emptyPanel2);

            EmptyPanel emptyPanel3 = new EmptyPanel("configServiceSummaryPanel");
            firstPartContainer.addOrReplace(emptyPanel3);
        }

        target.add(feedback);
        target.add(firstPartContainer);
        target.add(lockIconContainer);
    }

    private void updateProcessButtonPanel(AjaxRequestTarget target, int step) {

        stepButtonsPanel.setStep(step);
        target.add(stepButtonsPanel);

    }

    private void updateMatrixPanel(AjaxRequestTarget target) {
        matrixPanel.updateTable();
        target.add(matrixPanel);
    }

    @Override
    public FeedbackPanel getFeedbackPanel() {
        return this.feedback;
    }


    public void updateServiceDefinitionPanel(AjaxRequestTarget target, LogicalModelItem service, boolean isNew) {
        serviceDefinitionPanel.updateServiceFormPanel(target, service, this, isNew);
    }



    public String getServiceCatalogName(LogicalModelItem service) {
        String beta = logicalServicesHelper.isLogicalServiceBeta(service) ? " " + getString("portal.designer.logical.service.version.beta"): "" ;
        return getString("portal.designer.logical.service."+logicalServicesHelper.getLogicalServiceCatalogName(service)) + beta;
    }

    private boolean checkLogicalDeploymentOverallConsistency() {
        boolean isConsistent = false;

        // if architecture is locked, then checkOverallConsistencyAndUpdateLogicalDeployment has already been called and, architecture is consistent
        if (isArchitectureLocked()) {
            isConsistent = true;
        } else {

            try {
                logicalDeployment = manageLogicalDeployment.checkOverallConsistencyAndUpdateLogicalDeployment(logicalDeployment);
                isConsistent = true;
            } catch (BusinessException e) {
                BusinessExceptionHandler handler = new BusinessExceptionHandler(this);
                handler.error(e);
            }
        }
        return isConsistent;
    }

    private boolean isArchitectureLocked() {
        try {
            if (!hasWritePermission()) {
                return true;
            }
            if (appRelease == null) {
                return false;
            }
            if (manageApplicationRelease.findApplicationReleaseByUID(appRelease.getUID()).isLocked()) {
                return true;
            }
        } catch (BusinessException e) {
            BusinessExceptionHandler handler = new BusinessExceptionHandler(this);
            handler.error(e);
        }
        return false;
    }

    private boolean hasWritePermission() {
        if (appRelease == null)
            return false;
        return appRelease.getApplication().isEditable();
    }

    public void addNodeServiceAssociation(String jeeProcessingLabel, String serviceLabel, AjaxRequestTarget target) {

        ProcessingNode jeeProcessing = logicalDeployment.findProcessingNode(jeeProcessingLabel);
        LogicalService service = logicalDeployment.listLogicalServices(null, serviceLabel).iterator().next();

        jeeProcessing.addLogicalServiceUsage(service, LogicalServiceAccessTypeEnum.NOT_APPLICABLE);

        saveLogicalDeployment(logicalDeployment);

        matrixPanel.updateTable();
    }

    public void removeNodeServiceAssociation(String jeeProcessingLabel, String serviceLabel, AjaxRequestTarget target) {

    	ProcessingNode jeeProcessing = logicalDeployment.findProcessingNode(jeeProcessingLabel);
        LogicalService service = logicalDeployment.listLogicalServices(null, serviceLabel).iterator().next();

        for (LogicalNodeServiceAssociation assoc : jeeProcessing.listLogicalServicesAssociations()) {
            if (assoc.areAssociated(jeeProcessing, service)) {
                jeeProcessing.removeLogicalServiceUsage(assoc);
                break;
            }
        }

        saveLogicalDeployment(logicalDeployment);
        matrixPanel.updateTable();
    }

    /**
     * Save the logical deployment
     */
    public void saveLogicalDeployment(LogicalDeployment ld) {
        try {
            logicalDeployment = manageLogicalDeployment.updateLogicalDeployment(ld);
        } catch (ObjectNotFoundException exc) {
            error(getString("portal.logicaldeployment.objectnotfound"));
            logger.error("Error when trying to update logical deployment : "+ld.toString());
            logger.error(exc.getMessage());
        } catch (InvalidMavenReferenceException exc) {
            error(getString("portal.logicaldeployment.invalidmavenref"));
            logger.error("Error when trying to update logical deployment : "+ld.toString());
            logger.error(exc.getMessage());
        }
        logger.debug("after saving");
    }

    public void removeLogicalService(LogicalModelItem service, AjaxRequestTarget target, DesignerArchitectureMatrixPanel matrixPanel) {
        try {
            if (service instanceof LogicalService) {
                logicalDeployment.removeLogicalService((LogicalService)service);
            } else if (service instanceof ProcessingNode) {
                logicalDeployment.removeProcessingNode((ProcessingNode) service);
            }
            saveLogicalDeployment(logicalDeployment);
        } catch (BusinessException e) {
            // TODO - message should be "internazionalisable" and not thrown by logicalDeployment
            logger.error(e.getMessage());
            matrixPanel.error(e.getMessage());
            target.add(matrixPanel);
        } catch (Exception e) {
            logger.error(e.getMessage());
            matrixPanel.error(e.getMessage());
            target.add(matrixPanel);
        }
        managePageComponents(target, stepProcess, null);
    }

    public List<String> getQrsApplicationVersions(String todo, String defaultModelObjectAsString) {
        try {
            return manageLogicalDeployment.getQrsApplicationVersions(todo, defaultModelObjectAsString);  //To change body of created methods use File | Settings | File Templates.
        } catch (ObjectNotFoundException e) {
            // TODO : add resource message
            error("no qrs application version found");
            logger.debug("unable to get qrs application : {}", e);
            return null;
        }
    }

    public List<String> getQrsApplications(String todo) {
        try {
            return manageLogicalDeployment.getQrsApplications(todo);  //To change body of created methods use File | Settings | File Templates.
        } catch (ObjectNotFoundException e) {
            // TODO : add resource message
            error("no qrs application found");
            logger.debug("unable to get qrs application : {}", e);
            return null;
        }

    }

    public List<String> getQrsServices(String todo, String defaultModelObjectAsString, String defaultModelObjectAsString1) {
        try {
            return manageLogicalDeployment.getQrsServices(todo, defaultModelObjectAsString, defaultModelObjectAsString1);  //To change body of created methods use File | Settings | File Templates.
        } catch (ObjectNotFoundException e) {
            // TODO : add resource message
            error("no qrs service found");
            logger.debug("unable to get qrs application : {}", e);
            return null;
        }
    }

    public List<String> getQrsServicesVersions(String todo, String defaultModelObjectAsString, String defaultModelObjectAsString1, String defaultModelObjectAsString2) {
        try {
            return manageLogicalDeployment.getQrsServicesVersions(todo, defaultModelObjectAsString, defaultModelObjectAsString1, defaultModelObjectAsString2);  //To change body of created methods use File | Settings | File Templates.
        } catch (ObjectNotFoundException e) {
            // TODO : add resource message
            error("no qrs service version found");
            logger.debug("unable to get qrs application : {}", e);
            return null;
        }
    }

    public boolean isServiceEnable(LogicalModelItem service) {
        return logicalServicesHelper.isLogicalServiceEnable(service);
    }

    public boolean isServiceParameterEnable(LogicalModelItem service, String name) {
        boolean isEnable = true;
        if (service instanceof LogicalService) {
            isEnable = logicalServicesHelper.isServiceParameterEnable(service, name);
        } else if (service instanceof ProcessingNode) {
            isEnable = logicalServicesHelper.isServiceParameterEnable(service, name);
        }
        return isEnable;
    }

    public boolean checkServiceExistence(LogicalModelItem service) {
        boolean exist = false;
        if (service instanceof LogicalService) {
            exist =  existsServiceWithName((LogicalService)service);
        } else if (service instanceof ProcessingNode) {
            exist =  existsExecutionNodeWithName((ProcessingNode) service);
        }
        return exist;
    }

    /**
     * Searches for a service with a specific name in the LogicalDeployment (could be in the service layer)
     *
     * @param service searched
     * @return true if the LogicalDeployment contains a service named LogicalModelItem.name
     */
    private boolean existsServiceWithName(LogicalService service) {

        boolean exist = false;

        try {
            LogicalDeployment logicalDeploymentPersisted = manageLogicalDeployment.findLogicalDeployment(logicalDeployment.getId());

            // TODO : modify this when label / name will be correctly identify
            for (LogicalService servicePersisted : logicalDeploymentPersisted.listLogicalServices(service.getClass())) {
                if (!servicePersisted.getName().equals(service.getName()) && servicePersisted.getLabel().equals(service.getLabel())) {
                    exist = true;
                }
            }
        } catch (ObjectNotFoundException e) {
            exist = false;
        }
        return exist;
    }

    /**
     * Searches for an execution node with a specific name in the LogicalDeployment (could be in the service layer)
     *
     * @param service searched
     * @return true if the LogicalDeployment contains an execution node named name
     */
    private boolean existsExecutionNodeWithName(ProcessingNode service) {

        boolean exist = false;

        try {
            LogicalDeployment logicalDeploymentPersisted = manageLogicalDeployment.findLogicalDeployment(logicalDeployment.getId());
            for (ProcessingNode servicePersisted : logicalDeploymentPersisted.listProcessingNodes()) {
                if (!servicePersisted.getName().equals(service.getName()) && servicePersisted.getLabel().equals(service.getLabel())) {
                    exist = true;
                }
            }
        } catch (ObjectNotFoundException e) {
            exist = false;
        }
        return exist;
    }

    public void addOrUpdateLogicalService(Form<?> form, AjaxRequestTarget target, boolean isNew) {

        LogicalModelItem service = (LogicalModelItem)form.getModelObject();

        if(!checkServiceExistence(service)) {
            // TODO REFACTOR.... Too bad to set maven reference extension here
            if (service instanceof  LogicalSoapService) {
                MavenReference mavenReference = ((LogicalSoapService)service).getServiceAttachments();
                mavenReference.setExtension("jar");

                try{
                    WebMarkupContainer fullvalidationContent = (WebMarkupContainer) form.get("fullvalidationContent");
                    CheckBox fullvalidation = (CheckBox) fullvalidationContent.get("fullvalidation");
                    manageLogicalDeployment.checkLogicalSoapServiceConsistency((LogicalSoapService)service, fullvalidation.getModelObject());
                } catch(BusinessException ex) {
                    BusinessExceptionHandler handler = new BusinessExceptionHandler(this);
                    handler.error(ex);
                    target.add(getFeedbackPanel());
                }
            }

            if (service instanceof LogicalRelationalService && ((LogicalRelationalService)service).getInitialPopulationScript() != null) {
                MavenReference sqlInitScript = ((LogicalRelationalService)service).getInitialPopulationScript();

                if (sqlInitScript.getGroupId() == null && sqlInitScript.getArtifactId() == null && sqlInitScript.getVersion() == null) {
                    ((LogicalRelationalService) service).setInitialPopulationScript(null);
                } else {
                    if (sqlInitScript.getClassifier() == null) {
                        sqlInitScript.setClassifier("");
                        sqlInitScript.setExtension("sql");
                    }
                    sqlInitScript.setExtension("sql");
                }

            }

            if (isNew) {
                if (service instanceof LogicalService) {
                    logicalDeployment.addLogicalService((LogicalService)service);
                } else if (service instanceof ProcessingNode) {   
                	if (service instanceof JeeProcessing) {
                		((ProcessingNode)service).getSoftwareReference().setExtension("ear");	
                	}                	
                    logicalDeployment.addExecutionNode((ProcessingNode)service);
                }
            }

            saveLogicalDeployment(logicalDeployment);

//            DesignerArchitectureMatrixPanel matrixPanel = ((DesignerArchitectureMatrixPanel) get("matrix"));
//            matrixPanel.updateTable();

            serviceDefinitionPanel.updateServiceFormPanel(target, null, this, true);

            updateMatrixPanel(target);

//            target.addComponent(matrixPanel);

        } else {
            form.get("label").error(getString("portal.designer.service.already.exist.error", new Model<String[]>(new String[]{((RequiredTextField) form.get("label")).getInput()})));
            target.add(form);
        }

    }

    public void cancelServiceEdit(AjaxRequestTarget target) {
        serviceDefinitionPanel.updateServiceFormPanel(target, null, this, true);
//        updateServiceFormPanel(target, null, this, true);
//        getSelectServicesPanel().updateServiceListChoices(target);
    }

    public LogicalServicesHelper getLogicalServicesHelper() {
        return logicalServicesHelper;
    }

    public int getStepProcess() {
        return stepProcess;
    }

    public void setStepProcess(int stepProcess) {
        this.stepProcess = stepProcess;
    }
    
    public int nextStepProcess() {
        stepProcess++;
        if(stepProcess > 2) stepProcess = 2;
        return stepProcess;
    }
    
    public int previousStepProcess() {
        stepProcess--;
        if(stepProcess < 0) stepProcess = 0;
        return stepProcess;
    }
}
