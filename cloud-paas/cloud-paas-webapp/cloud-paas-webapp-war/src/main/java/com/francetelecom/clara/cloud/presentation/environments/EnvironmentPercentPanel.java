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

import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto.EnvironmentStatusEnum;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class EnvironmentPercentPanel<E extends EnvironmentDto> extends GenericPanel<E> {

	private static final long serialVersionUID = 5770483727834173216L;

    private Label envStatusPercentLabel;
    private Label envStatusLabel;
    private Label envStatusLabelAlone;
    
	public EnvironmentPercentPanel(String id, IModel<E> model) {
		super(id, model);
		createComponent();
	}

	@Override
	protected void onModelChanged() {
        EnvironmentStatusEnum envStatus = getModelObject().getStatus();
        int envStatusPercent = getModelObject().getStatusPercent() >= 0 ? getModelObject().getStatusPercent() : 0;

        envStatusPercentLabel.setDefaultModelObject(envStatusPercent);
        envStatusLabel.setDefaultModelObject(envStatus);
        envStatusLabelAlone.setDefaultModelObject(envStatus);
	}

    private void createComponent() {

        EnvironmentStatusEnum envStatus = getModelObject().getStatus();
        int envStatusPercent = getModelObject().getStatusPercent() >= 0 ? getModelObject().getStatusPercent() : 0;

        envStatusPercentLabel = new Label("env-percent-label", new Model<Integer>(envStatusPercent));
        envStatusLabel = new Label("env-status-label", new Model<EnvironmentStatusEnum>(envStatus));
        envStatusLabelAlone = new Label("env-status-label-alone", new Model<EnvironmentStatusEnum>(envStatus));

        /** % bar **/
        final WebMarkupContainer envBar = new WebMarkupContainer("env-bar");

		/** scrollbar */
		WebMarkupContainer envScrollbar = new WebMarkupContainer("env-scrollbar") {

            @Override
            public boolean isVisible() {
                if (getModelObject().getStatus().name().endsWith("ING") && !getModelObject().getStatus().equals(EnvironmentStatusEnum.RUNNING)) {
                    return true;
                } else {
                    return false;
                }
            }

            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
                envBar.add(new AttributeModifier("style", new Model<String>("width:" + envStatusPercentLabel.getDefaultModelObjectAsString() + "%; background-color:green;white-space:nowrap")));
                envBar.add(new AttributeModifier("title", new Model<String>(getModelObject().getStatusMessage())));
            }
        };

		/** Percent **/

		envBar.add(envStatusPercentLabel);
		/** Status **/
		envBar.add(envStatusLabel);
		envScrollbar.add(envBar);
        add(envScrollbar);

        WebMarkupContainer envStatusContainer = new WebMarkupContainer("env-status-container") {
            @Override
            public boolean isVisible() {
                if (envStatusLabelAlone.getDefaultModelObjectAsString().endsWith("ING") && !envStatusLabelAlone.getDefaultModelObject().equals(EnvironmentStatusEnum.RUNNING)) {
                    return false;
                } else {
                    return true;
                }
            }
        };

        envStatusContainer.add(envStatusLabelAlone);

        add(envStatusContainer);
	}

}
