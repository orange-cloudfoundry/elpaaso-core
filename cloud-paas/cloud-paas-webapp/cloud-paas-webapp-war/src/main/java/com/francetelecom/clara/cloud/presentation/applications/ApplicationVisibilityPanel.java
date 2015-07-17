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

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;
import com.google.common.base.Joiner;

public class ApplicationVisibilityPanel extends GenericPanel<Application> {

    private static final long serialVersionUID = 2409545269912653783L;

	public static final String APP_VISIBILITY_PUBLIC = "app-visibility-public";
    public static final String APP_VISIBILITY_PRIVATE = "app-visibility-private";

    public ApplicationVisibilityPanel(String id,  final IModel<Application> model) {
        super(id, model);
    }

    @Override
    protected void onBeforeRender() {
        initComponent();
        super.onBeforeRender();
    }

    private void initComponent() {
        WebMarkupContainer applicationPublicSpan = new WebMarkupContainer(APP_VISIBILITY_PUBLIC);
        WebMarkupContainer applicationPrivateSpan = new WebMarkupContainer(APP_VISIBILITY_PRIVATE);
        Joiner joiner = Joiner.on(", ");
        String members = joiner.join(getModelObject().listMembers());
        applicationPublicSpan.add(AttributeModifier.replace("title", new ResourceModel("portal.application.members.label").getObject() + " : " + members));
        applicationPrivateSpan.add(AttributeModifier.replace("title", new ResourceModel("portal.application.members.label").getObject() + " : " + members));
        add(applicationPublicSpan);
        applicationPublicSpan.add(new CacheActivatedImage("applicationVisibilityPanelPublicImage",new ResourceModel("visibility-public").getObject()));
        add(applicationPrivateSpan);
        applicationPrivateSpan.add(new CacheActivatedImage("applicationVisibilityPanelPrivateImage",new ResourceModel("visibility-private").getObject()));

        boolean applicationIsPublic = getModelObject().isPublic();
        applicationPublicSpan.setVisible(applicationIsPublic);
        applicationPrivateSpan.setVisible(!applicationIsPublic);
    }
}
