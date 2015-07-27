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

import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.applications.SelectedAppPage;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.releases.SelectedReleasePage;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
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
@MountPath("/environment/appUid/${appUid}/releaseUid/${releaseUid}/envUid/${envUid}")
@AuthorizeInstantiation({"ROLE_USER","ROLE_ADMIN"})
public class SelectedEnvironmentPage extends DesignerHelperPage {

    private static final long serialVersionUID = 4190202394568650412L;

    /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(EnvironmentActionPanel.class);

    @SpringBean
    private ManageEnvironment manageEnvironment;

    private EnvironmentDetailsDto envDetailsDto;

    private PageParameters params;

    private FeedbackPanel globalFeedback;

    private EnvironmentDetailsPanel envDetailsPanel;

    /**
     * logger
     */
    private static transient Logger log = LoggerFactory.getLogger(SelectedEnvironmentPage.class.getName());

    /**
     * PageTemplate constructor
     *
     * @param params - page parameters map
     */
    public SelectedEnvironmentPage(PageParameters params) {
        super(params);
        this.params = params;

        String envUid = getPageParameters().get("envUid").toString();
        try {
            envDetailsDto = manageEnvironment.findEnvironmentDetails(envUid);
        } catch (ObjectNotFoundException e) {
            logger.error("Environment not found ; envUid={}", envUid);
            throw new WicketRuntimeException(e);
        }

        initComponents();
    }

    @Override
    public FeedbackPanel getFeedbackPanel() {
        return this.globalFeedback;
    }

    public void initComponents() {
        setFirstLevelNavigationMenu();
        setBreadcrumbs();
        setFeedbackPanel();

        setEnvDetailsPanel();
    }

    private void setEnvDetailsPanel() {
        envDetailsPanel = new EnvironmentDetailsPanel("envDetailsPanel", new Model<EnvironmentDetailsDto>(envDetailsDto));
        add(envDetailsPanel);
    }

    private void setFirstLevelNavigationMenu() {
        NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
        add(navFirstLvl);
    }

    private void setBreadcrumbs() {

        List<BreadcrumbsItem> breadcrumbsItems = new ArrayList<BreadcrumbsItem>();

        breadcrumbsItems.add(new BreadcrumbsItem(HomePage.class, "portal.breadcrumb.home", null, false));

        PageParameters appPageParameters = new PageParameters();

        appPageParameters.add("appUid", params.get("appUid").toString());
        breadcrumbsItems.add(new BreadcrumbsItem(
                                    SelectedAppPage.class,
                                    appPageParameters,
                                    "portal.breadcrumb.selected.application",
                                    envDetailsDto.getApplicationLabel(),
                                    false
                            ));

        PageParameters releasePageParameters = new PageParameters();

        releasePageParameters.add("appUid", params.get("appUid").toString());
        releasePageParameters.add("releaseUid", params.get("releaseUid").toString());
        breadcrumbsItems.add(new BreadcrumbsItem(
                                    SelectedReleasePage.class,
                                    releasePageParameters,
                                    "portal.breadcrumb.selected.release",
                                    envDetailsDto.getReleaseVersion(),
                                    false
                            ));

        PageParameters envPageParameters = new PageParameters();
        envPageParameters.add("appUid", params.get("appUid").toString());
        envPageParameters.add("releaseUid", params.get("releaseUid").toString());
        envPageParameters.add("envUid", envDetailsDto.getUid());
        breadcrumbsItems.add(new BreadcrumbsItem(
                                    this.getClass(),
                                    envPageParameters,
                                    "portal.breadcrumb.selected.environment",
                                    envDetailsDto.getLabel(),
                                    true
                            ));

        /* set head page title to display in browser title bar */
        add(new Label("head_page_title", getString("portal.design.web.title.environment.home", new Model<Object[]>(new Object[]{envDetailsDto.getLabel()}))));

        Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        add(breadcrumbs);

    }

    private void setFeedbackPanel() {
//        feedback = new FeedbackPanel("feedback", new ComponentFeedbackMessageFilter(this));
        globalFeedback = new FeedbackPanel("feedback");
        globalFeedback.setOutputMarkupId(true);
        add(globalFeedback);
    }

    public EnvironmentDetailsPanel getEnvDetailsPanel() {
        return envDetailsPanel;
    }

    public ManageEnvironment getManageEnvironment() {
        return manageEnvironment;
    }

}
