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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 10/05/11
 */
public class HeaderTitle extends Panel {
    private static final long serialVersionUID = 1916366179513534341L;
    private static final Logger logger = LoggerFactory.getLogger(HeaderTitle.class.getName());

    public HeaderTitle(String id) {
        super(id);
        logger.debug("create page title header");
        // Label pageTitle = new Label("portal_title", getString("portal.design.header.title"));
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        Label pageTitle = new Label("portal_title", getStringResourceModel("portal.design.header.title"));

        add(pageTitle);

    }

    private StringResourceModel getStringResourceModel(java.lang.String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }
}
