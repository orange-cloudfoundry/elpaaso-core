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

import com.francetelecom.clara.cloud.presentation.HomePage;
import com.francetelecom.clara.cloud.presentation.applications.ApplicationsPage;
import com.francetelecom.clara.cloud.presentation.environments.EnvironmentsPage;
import com.francetelecom.clara.cloud.presentation.releases.ReleasesPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 07/07/11
 */
public class NavigationMenuFirstLevel extends Panel {
    private static final long serialVersionUID = 6615581221043208658L;

    PageParameters params;

    public NavigationMenuFirstLevel() {
        super("navigation_01");
        createMenu();
    }

    private void createMenu() {

        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        Label homeLinkLabel = new Label("homeLinkLabel", getStringResourceModel("portal.design.first.navigation.level.home"));
        Label appsLinkLabel = new Label("appsLinkLabel", getStringResourceModel("portal.design.first.navigation.level.apps"));
        Label releasesLinkLabel = new Label("releasesLinkLabel", getStringResourceModel("portal.design.first.navigation.level.releases"));
        Label envsLinkLabel = new Label("envsLinkLabel", getStringResourceModel("portal.design.first.navigation.level.envs"));


        BookmarkablePageLink homeLink = new BookmarkablePageLink("homeLink", HomePage.class);
        homeLink.add(homeLinkLabel);

        BookmarkablePageLink appsLink = new BookmarkablePageLink("appsLink", ApplicationsPage.class);
        appsLink.add(appsLinkLabel);

        BookmarkablePageLink releasesLink = new BookmarkablePageLink("releasesLink", ReleasesPage.class);
        releasesLink.add(releasesLinkLabel);

        BookmarkablePageLink envsLink = new BookmarkablePageLink("envsLink", EnvironmentsPage.class);
        envsLink.add(envsLinkLabel);

        add(homeLink);
        add(appsLink);
        add(releasesLink);
        add(envsLink);

    }

    private StringResourceModel getStringResourceModel(java.lang.String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }
}
