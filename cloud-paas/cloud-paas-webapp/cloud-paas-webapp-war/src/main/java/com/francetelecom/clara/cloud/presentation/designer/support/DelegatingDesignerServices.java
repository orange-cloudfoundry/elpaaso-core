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
package com.francetelecom.clara.cloud.presentation.designer.support;

import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.services.LogicalServiceEditPanel;

import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 04/01/12
 */

@Component
public class DelegatingDesignerServices {

    @Autowired
    private List<LogicalServiceEditPanel> delegates;

    public Panel createPanelFor(String id, LogicalModelItem modelItem, DesignerHelperPage parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {
        for(LogicalServiceEditPanel delegate : delegates) {
            Panel panel = delegate.getPanel(id, modelItem, parentPage, isNew, readOnly, configOverride);
            if (panel != null) {
                return panel;
            }
        }

        return new EmptyPanel(id);
    }

}
