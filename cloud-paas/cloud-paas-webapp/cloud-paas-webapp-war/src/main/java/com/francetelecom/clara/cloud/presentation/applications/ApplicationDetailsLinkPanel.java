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
package com.francetelecom.clara.cloud.presentation.applications;

import com.francetelecom.clara.cloud.coremodel.Application;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 22/08/11
 */
public class ApplicationDetailsLinkPanel extends GenericPanel<Application> {

    private static final long serialVersionUID = 7097519987296283366L;

    private PageParameters params = new PageParameters();

    public ApplicationDetailsLinkPanel(String id, final IModel<Application> model) {
        super(id, model);

        params.set("appUid", model.getObject().getUID());

        BookmarkablePageLink appDetailsLink = new BookmarkablePageLink("appDetailsLink", SelectedAppPage.class, params);

        add(appDetailsLink);

        Label appLabel = new Label("appLabel", model.getObject().getLabel());
        appDetailsLink.add(appLabel);

    }
}