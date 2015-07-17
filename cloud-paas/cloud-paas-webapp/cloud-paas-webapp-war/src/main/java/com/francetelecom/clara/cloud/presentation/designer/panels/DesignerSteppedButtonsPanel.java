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
package com.francetelecom.clara.cloud.presentation.designer.panels;

import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.presentation.tools.BlockUIDecorator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 23/05/12
 */
public class DesignerSteppedButtonsPanel extends Panel {

    private int step;
    private DesignerPage parentPage;
    private boolean isArchitectureLocked;

    public DesignerSteppedButtonsPanel(String id, DesignerPage parentPage, boolean isArchitectureLocked) {
        super(id);
        setStep(0);
        this.parentPage = parentPage;
        this.isArchitectureLocked = isArchitectureLocked;
        initComponents();
    }

    @Override
    public boolean isVisible() {
        return !isArchitectureLocked;
    }

    private void initComponents() {
        initPreviousStepButton();
        initNextStepButton();
        setOutputMarkupId(true);
    }

    private void initPreviousStepButton() {

        AjaxLink previousButton = new AjaxLink("previousButton") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                step = parentPage.previousStepProcess();
                parentPage.managePageComponents(target, step, null);
            }

            @Override
            public boolean isVisible() {
                return step != 0;
            }
        };

        add(previousButton);

    }

    private void initNextStepButton() {

        AjaxLink nextButton = new AjaxLink("nextButton") {

            @Override
            public void onClick(AjaxRequestTarget target) {
                step = parentPage.nextStepProcess();
                parentPage.managePageComponents(target, step, null);
            }

            @Override
            public boolean isVisible() {
                return step != 2;    //To change body of overridden methods use File | Settings | File Templates.
            }

            @Override
            protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
                if (step == 1) {
                    attributes.getAjaxCallListeners().add(new BlockUIDecorator(getString("portal.designer.architecture.validation"))); 
                }
            }

        };

        add(nextButton);

    }

    public void setStep(int step) {
        this.step = step;
    }

}
