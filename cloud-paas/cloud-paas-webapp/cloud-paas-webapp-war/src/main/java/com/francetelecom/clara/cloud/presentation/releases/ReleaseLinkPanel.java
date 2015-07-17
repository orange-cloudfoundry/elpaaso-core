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

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 05/08/11
 */
public class ReleaseLinkPanel extends Panel {

    private static final long serialVersionUID = 1589018425872036477L;

    private PageParameters params = new PageParameters();

    public ReleaseLinkPanel(String id, IModel<ApplicationRelease> model) {
        super(id);
//        this.params = params;

        ApplicationRelease release = model.getObject();

        params.set("appUid", release.getApplication().getUID());
        params.set("releaseUid", release.getUID());

        BookmarkablePageLink releasePage = new BookmarkablePageLink("releaseLink", SelectedReleasePage.class, params);

        Label releaseNameLabel = new Label("releaseNameLabel", release.getApplication().getLabel() + " - " + release.getReleaseVersion());
        releasePage.add(releaseNameLabel);

        add(releasePage);

    }


}
