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
package com.francetelecom.clara.cloud.presentation;

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.scalability.ManageScalability;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * ScalabilityPage
 * Page used to publish demo data using ScalabilityService
 *
 * Updated  : $LastChangedDate$
 * @author  : $Author$
 * @version : $Revision$
 * @link http://elpaaso_shp/index.php/Portal
 */

@MountPath("/scalability/${action}")
@AuthorizeInstantiation("ROLE_ADMIN")
public class ScalabilityPage extends PageTemplate {
    /**
     * serialUID
     */
    private static final long serialVersionUID = 7894818834200368614L;
    /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ScalabilityPage.class);
    /**
     * paas scalability manager
     */
    @SpringBean
    ManageScalability manageScalability;

    private FeedbackPanel feedback;

    public ScalabilityPage(PageParameters params) {
        super(params);
        logger.debug("ScalabilityPage");
    }

    @Override
    protected void onBeforeRender() {
        super.onBeforeRender();
        feedback = new FeedbackPanel("feedbackPanel");

        // menu
        NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
        add(navFirstLvl);
        // title
        add(new Label("head_page_title", getString("portal.design.web.title.homepage")));
        // breadcrumb
        List<BreadcrumbsItem> breadcrumbsItems = new ArrayList<BreadcrumbsItem>();
        breadcrumbsItems.add(new BreadcrumbsItem(HomePage.class, "portal.design.breadcrumbs.homepage", null, true));
        Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        add(breadcrumbs);
        // feedback
        add(feedback);


        try {
            logger.debug("populate initial demo data (one time only)");
            logger.info("manageScalability {}", (manageScalability != null ? "set" :"null"));
            int nbApp = 2;
            int nbReleasePerApp = 2;
            int nbEnvPerRelease = 2;
            Collection<ApplicationRelease> createdRelease = manageScalability.populatePortalPhase(nbApp, nbReleasePerApp, nbEnvPerRelease);
            String result = "created "
                     + nbApp + " apps x "
                     +  createdRelease.size() + " releases x "
                     + nbEnvPerRelease + " envs";
            feedback.info("populate result : " + result);
        } catch (Exception populateExc) {
            String errMsg = populateExc.getClass().getSimpleName();
            if (populateExc.getMessage() != null) {
                errMsg += "details : " + populateExc.getMessage();
            }
            populateExc.printStackTrace();
            feedback.error(errMsg);
        }
    }
}

