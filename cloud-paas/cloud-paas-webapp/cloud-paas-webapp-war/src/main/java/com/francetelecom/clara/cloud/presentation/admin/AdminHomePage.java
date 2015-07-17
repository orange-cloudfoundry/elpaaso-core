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
package com.francetelecom.clara.cloud.presentation.admin;

import com.francetelecom.clara.cloud.presentation.WicketApplication;
import com.francetelecom.clara.cloud.presentation.common.Breadcrumbs;
import com.francetelecom.clara.cloud.presentation.common.NavigationMenuFirstLevel;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;
import com.francetelecom.clara.cloud.scalability.ManageStatistics;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.authroles.authorization.strategies.role.annotations.AuthorizeInstantiation;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.value.ValueMap;
import org.slf4j.LoggerFactory;
import org.wicketstuff.annotation.mount.MountPath;

import java.util.ArrayList;
import java.util.List;


/**
 * AdminHomePage
 * Class that hold admin home page
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 * Sample usage : see ManageStatisticsImplTest
 * @version     : $Revision$
 */
@MountPath("/admin")
@AuthorizeInstantiation("ROLE_ADMIN")
public class AdminHomePage extends PageTemplate {
    /**
     * Logger
     */
    private static final transient org.slf4j.Logger logger
       = LoggerFactory.getLogger(AdminHomePage.class);
    private static final long serialVersionUID = 8681583660824899469L;

    private FeedbackPanel feedback;

    /**
     * create home page
     * @param params - pageParameters map
     */
    public AdminHomePage(PageParameters params) {
        super(params);
        initComponents();
    }

    private void initComponents() {
        NavigationMenuFirstLevel navFirstLvl = new NavigationMenuFirstLevel();
        add(navFirstLvl);
        /* set head page title to display in browser title bar */
        add(new Label("head_page_title", getString("portal.design.web.title.homepage")));

        List<BreadcrumbsItem> breadcrumbsItems = new ArrayList<BreadcrumbsItem>();
        breadcrumbsItems.add(new BreadcrumbsItem(this.getClass(), "portal.design.breadcrumbs.homepage", null, true));
        Breadcrumbs breadcrumbs = new Breadcrumbs("breadcrumbs", breadcrumbsItems);
        add(breadcrumbs);

        feedback = new FeedbackPanel("feedback", new ContainerFeedbackMessageFilter(this));
        feedback.setOutputMarkupId(true);
        add(feedback);

        add(new ManageStatsForm("manageStatsForm"));

        StatsTablePanel statsTablePanel = new StatsTablePanel("statsTablePanel");
        add(statsTablePanel);

    }

    public FeedbackPanel getFeedback() {
        return this.feedback;
    }

    private void razSessionStats() {
        logger.debug("raz paas stats");
        WicketSession ws = (WicketSession)getSession();
        ws.razStats();
    }

  public final class ManageStatsForm extends Form
    {
        // El-cheapo model for form
        private final ValueMap properties = new ValueMap();
        private final static String STATS_ENABLE_KEY = "statsEnable";
        /**
         * Constructor
         *
         * @param id
         *            id of the form component
         */
        public ManageStatsForm(final String id) {
            super(id);
            ManageStatistics ms = ((WicketApplication)getApplication()).getManageStatistics();
            properties.add(STATS_ENABLE_KEY, Boolean.toString(ms.isStatEnable()));
            // Attach CheckBox components that edit properties map model
            add(new CheckBox("statsEnable", new PropertyModel<Boolean>(properties, STATS_ENABLE_KEY)));
            // Cancel first application add / update button
            AjaxButton razStatsButton = new AjaxButton("razStatsButton") {
                @Override
                protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                    razSessionStats();
                    setResponsePage(this.getWebPage());
                }

                @Override
                protected void onError(AjaxRequestTarget target, Form<?> form) {
                    
                }
            };

            razStatsButton.setDefaultFormProcessing(false);
            add(razStatsButton);
        }

        /**
         * @see org.apache.wicket.markup.html.form.Form#onSubmit()
         */
        @Override
        public final void onSubmit()
        {
            // Get session info
            ManageStatistics ms = ((WicketApplication)getApplication()).getManageStatistics();

            boolean statsEnabled = properties.getBoolean(STATS_ENABLE_KEY);
            ms.setStatsState(statsEnabled);
            info("statistics are now " + (statsEnabled ? "enabled":"disabled"));
        }
    }
}
