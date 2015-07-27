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

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.core.service.ManageApplication;
import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.exception.ApplicationNotFoundException;
import com.francetelecom.clara.cloud.core.service.exception.InvalidConfigOverrideException;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.PaasUser;
import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils.ConfigEntry;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent;
import com.francetelecom.clara.cloud.presentation.common.WicketUtils;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.panels.DesignerArchitectureMatrixPanel;
import com.francetelecom.clara.cloud.presentation.releases.SelectedReleasePage;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;
import com.francetelecom.clara.cloud.presentation.tools.*;
import com.francetelecom.clara.cloud.services.dto.ConfigOverrideDTO;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import org.apache.wicket.Component;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.bean.validation.PropertyValidator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;

/**
 * EnvironmentCreatePanel
 * Environment creation page
 *
 * Updated  : $LastChangedDate$
 * @author  : $Author$
 * @version : $Revision$
 */
public class EnvironmentCreatePanel extends Panel{

	private static final long serialVersionUID = -5260113851920407357L;

    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(EnvironmentCreatePanel.class);

    private ManageEnvironment manageEnvironment;

    private ManageApplicationRelease manageApplicationRelease;

    private ManageApplication manageApplication;

    private ApplicationRelease release;
    private PaasUser paasUser = WicketSession.get().getPaasUser();

    private Form<MyFormEnvironmentDto> envForm;

    private WebMarkupContainer container;

    private DesignerArchitectureMatrixPanel envArchitecturePanel;

    private AjaxCheckBox overridePropsCheckbox;

    private Map<String, LogicalConfigService> configOverrides = new HashMap<>();

    public EnvironmentCreatePanel(String id, ApplicationRelease release, ManageApplicationRelease manageApplicationRelease, ManageEnvironment manageEnvironment, ManageApplication manageApplication) {
        super(id);
        this.manageApplication = manageApplication;
        this.manageApplicationRelease = manageApplicationRelease;
        this.manageEnvironment = manageEnvironment;
        this.release = release;
    }

    private void initComponents() {
        createEnvironmentForm();
        setOutputMarkupId(true);
    }

    private void createEnvironmentForm() {

        MyFormEnvironmentDto envDto = new MyFormEnvironmentDto();
        List<ApplicationRelease> releaseList = null;

        if (release != null) {
            envDto.setRelease(release);
            releaseList = new ArrayList<ApplicationRelease>();
            releaseList.add(release);
        } else {
            releaseList = manageApplicationRelease.findMyApplicationReleases();
        }

        envForm = new Form<>("envForm", new CompoundPropertyModel<>(envDto));

//        ChoiceRenderer<ApplicationRelease> choiceRenderer = new ChoiceRenderer<ApplicationRelease>("releaseVersion", "name");
        CompoundChoiceRenderer<ApplicationRelease> compoundChoiceRenderer = new CompoundChoiceRenderer<ApplicationRelease>(new String[]{"application.label", "releaseVersion"}, "uid", " - ");
        final DropDownChoice<ApplicationRelease> envDtoDropDownChoice = new DropDownChoice<ApplicationRelease>("release", releaseList, compoundChoiceRenderer);
        envDtoDropDownChoice.add(new OnChangeAjaxBehavior() {

			private static final long serialVersionUID = -8967171434567279367L;

			@Override
            protected void onUpdate(AjaxRequestTarget target) {
                try {
                    ((DesignerHelperPage) getPage()).getLogicalDeploymentPersisted(envDtoDropDownChoice.getModelObject().getUID());
                } catch (ObjectNotFoundException e) {
                    return;
                }
                
                envArchitecturePanel.updateTable();
                configOverrides.clear();
                target.add(container);
            }
        });

        if (release != null) {
            envDtoDropDownChoice.setEnabled(false);
        }

        // release required
        envDtoDropDownChoice.add(new PropertyValidator<>());

        envForm.add(envDtoDropDownChoice);
        envForm.add(new CacheActivatedImage("imageHelp.releaseField", getString("image.help")));

        final RequiredTextField<String> label = new RequiredTextField<>("label");
        label.setLabel(new StringResourceModel("portal.environment.name.label",null));
        label.add(new PropertyValidator<>());
        label.add(new AbstractValidator<String>() {

 			private static final long serialVersionUID = 5714188138333004292L;

			@Override
            protected void onValidate(IValidatable<String> iValidatable) {
                boolean isUnique = false;
                try {
                    String releaseUID;
                    if (release != null) {
                        releaseUID = release.getUID();
                    } else {
                        releaseUID = envDtoDropDownChoice.getRawInput();
                    }
                    isUnique = manageEnvironment.isEnvironmentLabelUniqueForRelease(paasUser.getSsoId().getValue(), iValidatable.getValue(), releaseUID);
                } catch (BusinessException e) {
                    error(iValidatable);
                    logger.error(e.getMessage(), e);
                }

                if(!isUnique) {
                    error(iValidatable);
                }
            }

            @Override
            protected String resourceKey() {
                return "portal.environment.label.non.unique";
            }

            @Override
            protected Map<String, Object> variablesMap(IValidatable<String> stringIValidatable) {
                Map<String, Object> map = super.variablesMap(stringIValidatable);
                map.put("label", stringIValidatable.getValue());
                return map;

            }
        });

        DropDownChoice<EnvironmentDto.EnvironmentTypeEnum> envType = new DropDownChoice<EnvironmentDto.EnvironmentTypeEnum>("type", Arrays.asList(EnvironmentDto.EnvironmentTypeEnum.values()));
        envType.setLabel(new StringResourceModel("portal.environment.type.label",null));
        envType.add(new PropertyValidator<>());

        container = new WebMarkupContainer("propertiesContainer");
        container.setOutputMarkupPlaceholderTag(true);
        container.setVisible(false);

        envArchitecturePanel = new DesignerArchitectureMatrixPanel("env-design", (DesignerHelperPage)getPage(), true, true);
        container.add(envArchitecturePanel);

        overridePropsCheckbox = new AjaxCheckBox("overridePropertiesCheckbox", new Model<Boolean>()) {
 			private static final long serialVersionUID = 7096578812745292001L;

			@Override
            protected void onUpdate(AjaxRequestTarget target) {
                container.setVisible(getModelObject());
                target.add(container);
            }
        };
        overridePropsCheckbox.setLabel(WicketUtils.getStringResourceModel(this, "portal.environment.properties.override.label"));
        add(overridePropsCheckbox);

        createFormButtons(envForm);

        add(envForm);
        envForm.add(label);
        envForm.add(envType);
        envForm.add(container);
        envForm.add(overridePropsCheckbox);

    }

    private void createFormButtons(Form<MyFormEnvironmentDto> envform) {

        AjaxButton addButton = new AjaxButton("addEnvButton") {

 			private static final long serialVersionUID = 2435430716213738894L;

			@Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                addEnvCreation((Form<MyFormEnvironmentDto>) form, target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                attributes.getAjaxCallListeners().add(new BlockUIDecorator(getString("portal.info.env.create"))); 
            }

        };
        envform.add(addButton);

        // Cancel release add / update button
        AjaxButton cancelButton = new AjaxButton("cancelEnvButton") {

			private static final long serialVersionUID = -4353787741035247426L;

			@Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (getPage() instanceof SelectedReleasePage) {
                    ((SelectedReleasePage)getPage()).cancelEnvironmentCreation(target);
                } else {
                    ((EnvironmentsPage)getPage()).cancelEnvironmentCreation(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        };

        cancelButton.setDefaultFormProcessing(false);
        envform.add(cancelButton);
    }

    public void addEnvCreation(Form<MyFormEnvironmentDto> form, AjaxRequestTarget target) {
        PageParameters params = new PageParameters();
        MyFormEnvironmentDto env = form.getModelObject();

        params.set("appUid", env.getRelease().getApplication().getUID());
        params.set("releaseUid", env.getRelease().getUID());

        try {
            String configRoleUID = createConfigRole(env.getRelease().getApplication().getUID(), env.getLabel());
            String envUid;
            if (configRoleUID != null) {
                envUid = manageEnvironment.createEnvironment(env.release.getUID(), env.type, paasUser.getSsoId().getValue() , env.label, Arrays.asList(configRoleUID));
            } else {
                envUid = manageEnvironment.createEnvironment(env.release.getUID(), env.type, paasUser.getSsoId().getValue() , env.label);
            }
            
            params.set("envUid", envUid);
        } catch (BusinessException e) {

            FeedbackPanel globalFeedback = ((DesignerHelperPage) this.findPage()).getFeedbackPanel();

            BusinessExceptionHandler handler = new BusinessExceptionHandler(globalFeedback);
            handler.error(e);

            target.add(globalFeedback);
            return;
        }

        String successMsg = getString("portal.environment.creation.successful", new Model<Object[]>(new Object[]{env.getLabel(), env.getRelease().getApplication().getLabel()+" - "+env.getRelease().getReleaseVersion()}));
        Session.get().info(successMsg);
        setResponsePage(SelectedEnvironmentPage.class, params);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        initComponents();
        envForm.visitChildren(FormComponent.class, new IVisitor<Component, Void>() {
            @Override
            public void component(Component object, IVisit<Void> visit) {
                object.add(new FieldFeedbackDecorator());
                visit.dontGoDeeper();
            }
        });
    }

    class MyFormEnvironmentDto implements Serializable {
        private static final long serialVersionUID = 5874638410811831883L;
        @NotNull
        private ApplicationRelease release;
        @NotNull
        private String label;
        @NotNull
        private EnvironmentDto.EnvironmentTypeEnum type;

        MyFormEnvironmentDto() {

        }

        public ApplicationRelease getRelease() {
            return release;
        }

        public void setRelease(ApplicationRelease release) {
            this.release = release;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public EnvironmentDto.EnvironmentTypeEnum getType() {
            return type;
        }

        public void setType(EnvironmentDto.EnvironmentTypeEnum type) {
            this.type = type;
        }
    }

    public void addEnvironmentConfigOverride(LogicalConfigService logicalConfigOverride) {
        configOverrides.put(logicalConfigOverride.getLabel(), logicalConfigOverride);
    }

    public LogicalConfigService getEnvironmentConfigOverride(String label) {
        return configOverrides.get(label);
    }

    private String createConfigRole(String applicationUID, String envName) throws InvalidConfigServiceException, ApplicationNotFoundException, InvalidConfigOverrideException {
        if (logger.isDebugEnabled()) {
            logger.debug("create config role for env " + envName);
        }
        if (configOverrides.size() == 0) {
            return null;
        }
        if (overridePropsCheckbox.getModelObject().equals(Boolean.FALSE)) {
            return null;
        }
        LogicalConfigServiceUtils util = new LogicalConfigServiceUtils();
        List<ConfigOverrideDTO> configOverrideDTOs = new ArrayList<>();
        for (LogicalConfigService configService : configOverrides.values()) {
            StructuredLogicalConfigServiceContent content = util.parseConfigContent(configService.getConfigSetContent());
            for (ConfigEntry configEntry : content.getConfigEntries()) {
                ConfigOverrideDTO configOverrideDTO = new ConfigOverrideDTO();
                configOverrideDTO.setComment(configEntry.getComment());
                configOverrideDTO.setConfigSet(configService.getLabel());
                configOverrideDTO.setKey(configEntry.getKey());
                configOverrideDTO.setValue(configEntry.getValue());
                configOverrideDTOs.add(configOverrideDTO);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("create config role with overriden values " + configOverrideDTOs);
        }
        return manageApplication.createConfigRole(applicationUID, "implicit role for env " + envName, configOverrideDTOs);
    }

}


