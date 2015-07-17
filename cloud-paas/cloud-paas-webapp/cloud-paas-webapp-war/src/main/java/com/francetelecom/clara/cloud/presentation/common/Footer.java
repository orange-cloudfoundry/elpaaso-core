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

import com.francetelecom.clara.cloud.presentation.WicketApplication;
import com.francetelecom.clara.cloud.presentation.models.ContactUsBean;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 12/05/11
 */
public class Footer extends Panel {
    private static final long serialVersionUID = -4707631131933530508L;
    private static final Logger logger = LoggerFactory.getLogger(Footer.class.getName());
	
    @SpringBean
    private ContactUsBean contactUsBean;

    /**
     * Constructor
     *
     * @param id - the footer id
     */
    public Footer(String id) {
        super(id);
    }

    private StringResourceModel getStringResourceModel(java.lang.String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }

    @Override
    protected void onBeforeRender() {
        logger.debug("onBeforeRender");
        if (!hasBeenRendered()) {
          initComponents();
        }
        super.onBeforeRender();
    }

    public void initComponents() {
    	
        ExternalLink contactUsLink = new ExternalLink ("contactUsLink", "mailto:" + contactUsBean.getMailTo());
        contactUsLink.add (new Label ("contactUsLabel", getStringResourceModel("portal.design.footer.contact.us")));
        add (contactUsLink);

        ExternalLink helpLink = new ExternalLink("helpLink", getStringResourceModel("portal.design.footer.help.link"));
        Label helpLinkLabel = new Label("helpLinkLabel", getStringResourceModel("portal.design.footer.help"));

        helpLink.add(helpLinkLabel);
        add(helpLink);
        boolean mock = Boolean.valueOf(WicketApplication.get().getInitParameter("mockMode"));
        String version =  "version " + getString("portal.build.version");

        String completeVersion = mock ? version + " (mock)" : version;
        Label versionLabel = new Label("version", completeVersion);

        versionLabel.add(new AttributeModifier("title",getString("portal.build.timestamp") + " " + getString("portal.build.user")));
        add(versionLabel);
    }
}
