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

import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 09/08/11
 */
public class EnvironmentDetailsLinkPanel extends GenericPanel<EnvironmentDto> {

    /**
     * serialUID
     */
    private static final long serialVersionUID = 6601840907037749784L;

     /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(EnvironmentDetailsLinkPanel.class);

    public EnvironmentDetailsLinkPanel(String id, final IModel<EnvironmentDto> model, ApplicationRelease release) {
        super(id, model);

        PageParameters params = new PageParameters();

        params.set("appUid", release.getApplication().getUID());
        params.set("releaseUid", release.getUID());
        params.set("envUid", model.getObject().getUid());

        BookmarkablePageLink envPage = new BookmarkablePageLink("envLink", SelectedEnvironmentPage.class, params);

        Label envLabel = new Label("envLabel", model.getObject().getLabel());
        envPage.add(envLabel);

        add(envPage);

    }
}
