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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 23/08/11
 * Time: 10:53
 * To change this template use File | Settings | File Templates.
 */

/**
 * The stepped process is cut into two parts : DesignerSteppedProcessPanel and DesignerSteppedProcessButtons, so that we simply choose the location of both parts
 */
public class DesignerSteppedProcessPanel extends Panel {

    private static final long serialVersionUID = -4298293538487642415L;
    /**
     * Number of steps in the process
     */
    int stepsNumber = 3;
    /**
     * The current step in the process
     */
    int currentStep = 0;

    ListView<String> list;

    /**
     * Model in charge of retrieving list of items of the process
     */
    IModel<? extends List<String>> stepItemsModel;

    public DesignerSteppedProcessPanel(String id, int currentStep) {
        super(id);
        this.currentStep = currentStep;

        initStepsList();

        initComponents();
    }

    private void initComponents() {

        WebMarkupContainer container = new WebMarkupContainer("container");
        add(container);

        list = new ListView<String>("repeater", stepItemsModel) {
            private static final long serialVersionUID = -49889646848608542L;

            @Override
            protected void populateItem(ListItem<String> listItem) {
                Label stepNumber = new Label("stepNumber", new Model<String>(String.valueOf(listItem.getIndex()+1)));
                stepNumber.setRenderBodyOnly(true);
                listItem.add(stepNumber);
                listItem.add(new Label("stepName", listItem.getDefaultModelObjectAsString()));

                updateProcessStep(listItem, currentStep);

            }
        };
        container.add(list);

    }

    public void initStepsList() {
        // Stepped list creation
        stepItemsModel = new Model<ArrayList<String>>(){
        	@Override
        	public ArrayList<String> getObject() {
        		ArrayList<String> stepItems = new ArrayList<String>();
    	        stepItems.add(getString("portal.designer.process.pipeline.step.first"));
    	        stepItems.add(getString("portal.designer.process.pipeline.step.second"));
    	        stepItems.add(getString("portal.designer.process.pipeline.step.third"));
    	        return stepItems;
        	}
        };
        
    }

    public void updateProcessStep(ListItem<String> listItem, int step) {

        /* CSS Management */
        if(listItem.getIndex() < currentStep) {
            listItem.add(new AttributeAppender("class", new Model<>("step_visited"), ""));
        } else if(listItem.getIndex() == currentStep) {
            listItem.add(new AttributeAppender("class", new Model<>("step_current"), ""));
        } else if(listItem.getIndex() > currentStep) {
            listItem.add(new AttributeAppender("class", new Model<>("step_future"), ""));
        }

        if(listItem.getIndex() == 0) {
            listItem.add(new AttributeAppender("class", new Model<>(" first"), ""));
        }

        if(listItem.getIndex() == stepsNumber - 1) {
            listItem.add(new AttributeAppender("class", new Model<>(" last"), ""));
        }

    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }
}
