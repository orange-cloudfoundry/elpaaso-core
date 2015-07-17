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

import java.net.URL;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.link.ExternalLink;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;
import com.francetelecom.clara.cloud.services.dto.LinkDto;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 19/06/12
 */
public class EnvironmentOverallsPanel extends GenericPanel<EnvironmentDetailsDto> {

    Logger logger = LoggerFactory.getLogger(EnvironmentOverallsPanel.class);

    ExternalLink logsUrlBtn;
    ExternalLink monitoringUrlBtn;

    WebMarkupContainer logsUrlSpan;
    WebMarkupContainer monitoringUrlSpan;

    URL logsUrl;
    URL monitoringUrl;

    public EnvironmentOverallsPanel(String id, EnvironmentDetailsDto envDetailsDto) {
        super(id, new Model<>(envDetailsDto));
    }

    @Override
    protected void onBeforeRender() {
        initComponents();
        super.onBeforeRender();
    }

    private void initComponents() {
        initLogUrlBtn();
        initMonitoringBtn();
    }

    private void initMonitoringBtn() {
        monitoringUrlSpan = new WebMarkupContainer("monitoringUrlImg");

        if (getModelObject() != null) {
            monitoringUrl = getModelObject().getURLLinkFromType(LinkDto.LinkTypeEnum.METRICS_LINK);
        }
        
        monitoringUrlBtn = new ExternalLink("cell-monitoringUrl", (monitoringUrl != null) ? monitoringUrl.toString() : "") {

            private static final long serialVersionUID = 5630633258734623659L;

            @Override
            public boolean isVisible() {
                return monitoringUrl != null;
            }
        };

        monitoringUrlSpan.add(new AttributeAppender("class", new Model<String>((monitoringUrl != null) ? "monitoringUrl-enable" : "monitoringUrl-disable"), " "));
        monitoringUrlBtn.add(new AttributeModifier("id", new Model<String>("monitoringURL")));
        monitoringUrlBtn.add(new AttributeModifier("title", new Model<String>(getString("portal.environment.details.overalls.monitor.tooltip"))));
        monitoringUrlBtn.add(monitoringUrlSpan);
        addOrReplace(monitoringUrlBtn);
    }

    private void initLogUrlBtn() {

        /** log icon */
        logsUrlSpan = new WebMarkupContainer("logsUrlImg");

        if (getModelObject() != null) {
            LinkDto overallsLinkDto = getModelObject().getEnvironmentOverallsLinkDto();
            if (overallsLinkDto != null) {
                logsUrl = overallsLinkDto.getUrl();
            }
        }

        logsUrlBtn = new ExternalLink("cell-logsUrl", (logsUrl != null) ? logsUrl.toString() : "") {

            private static final long serialVersionUID = -7297970012605590790L;

            @Override
            public boolean isVisible() {
                return logsUrl != null;
            }
        };

        logsUrlSpan.add(new AttributeAppender("class", new Model<String>((logsUrl != null) ? "logsUrl-enable" : "logsUrl-disable"), " "));
        //logsUrlBtn.add(new AttributeAppender("target", new Model<String>("_blank"), " " ));
        logsUrlBtn.add(new AttributeModifier("id", new Model<String>("logsURL")));

        logsUrlBtn.add(new AttributeModifier("title", new Model<String>(getString("portal.environment.details.overalls.log.tooltip"))));

        logsUrlBtn.add(logsUrlSpan);
        addOrReplace(logsUrlBtn);

    }

}
