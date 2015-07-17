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

import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.presentation.common.CustomModalWindow;
import com.francetelecom.clara.cloud.services.dto.LinkDto;
import com.francetelecom.clara.cloud.services.dto.WspInfoDto;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: shjn2064
 * Date: 23/05/12
 * Time: 15:51
 * To change this template use File | Settings | File Templates.
 */
public class EnvironmentWspInfoPanel extends Panel {

    public EnvironmentWspInfoPanel(String id, LinkDto linkDto) {
        super(id);

        Map<String, String> servicebindings = linkDto.getServiceBindings();

        Label project = new Label("project", (servicebindings != null) ? servicebindings.get("soap.service.subscription.projectcode") : "");
        add(project);

        Label serviceName = new Label("serviceName",(servicebindings != null) ? servicebindings.get("soap.service.subscription.servicename") : "");
        add(serviceName);

        Label serviceVersion = new Label("serviceVersion", (servicebindings != null) ? servicebindings.get("soap.service.subscription.version") : "");
        add(serviceVersion);

        Label accessUrl = new Label("accessUrl", (servicebindings != null) ? servicebindings.get("soap.service.subscription.url") : "");
        add(accessUrl);

        Label proxifiedAccessUrl = new Label("proxifiedAccessUrl", (servicebindings != null) ? servicebindings.get("soap.service.subscription.proxifiedserviceurl") : "");
        add(proxifiedAccessUrl);

       Form wspCloseForm = new Form<Void>("wspCloseForm");
       AjaxButton closeButton = new AjaxButton("closeButton") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                findParent(CustomModalWindow.class).close(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                findParent(CustomModalWindow.class).close(target);
            }
        };
        wspCloseForm.add(closeButton);
        add(wspCloseForm);

    }

    private boolean isLogicalServiceInfoEqualsToWspInfoDto(WspInfoDto dto, LogicalSoapService logicalSoapService) {
        return dto.getServiceName().equals(logicalSoapService.getServiceName()) && dto.getServiceVersion().equals(logicalSoapService.getServiceMajorVersion() + "-" + logicalSoapService.getServiceMinorVersion());
    }

}

