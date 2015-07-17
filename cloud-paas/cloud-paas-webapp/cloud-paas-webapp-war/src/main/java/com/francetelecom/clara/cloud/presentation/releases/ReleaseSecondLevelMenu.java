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

import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.environments.EnvironmentsPage;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 07/07/11
 */
public class ReleaseSecondLevelMenu extends Panel {
    private static final long serialVersionUID = 3598668573502967529L;

    public ReleaseSecondLevelMenu(String id) {
        super(id);

        BookmarkablePageLink designerLink = new BookmarkablePageLink("designerLink", DesignerPage.class);
        add(designerLink);

        BookmarkablePageLink envLink = new BookmarkablePageLink("envsLink", EnvironmentsPage.class);
        add(envLink);

    }

    public ReleaseSecondLevelMenu(String id, PageParameters params) {
        super(id);

        params.set("step", "0");

        BookmarkablePageLink designerLink = new BookmarkablePageLink("designerLink", DesignerPage.class, params);
        add(designerLink);

        BookmarkablePageLink envsLink = new BookmarkablePageLink("envsLink", EnvironmentsPage.class, params);
        add(envsLink);

    }
}
