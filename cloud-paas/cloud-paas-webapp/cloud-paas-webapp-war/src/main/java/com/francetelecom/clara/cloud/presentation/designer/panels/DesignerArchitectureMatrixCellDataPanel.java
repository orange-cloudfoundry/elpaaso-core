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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.logicalmodel.LogicalNodeServiceAssociation;
import com.francetelecom.clara.cloud.logicalmodel.LogicalService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 29/07/11
 * Time: 10:37
 * To change this template use File | Settings | File Templates.
 */
public class DesignerArchitectureMatrixCellDataPanel extends Panel {

    private static final long serialVersionUID = -4152873849736272145L;
    private LogicalService parentRow;
    private ProcessingNode parentCol;
    private Boolean selected; // used by the property model

    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(DesignerArchitectureMatrixCellDataPanel.class);

    public DesignerArchitectureMatrixCellDataPanel(String id, final LogicalService parentRow, final ProcessingNode parentCol) {
        super(id);
        this.parentRow = parentRow;
        this.parentCol = parentCol;
        this.selected = getParentsAssociation(getParentCol(), getParentRow()) != null;

        Form<Void> form = new Form<>("form");
        AjaxCheckBox associated = new AjaxCheckBox("associated", new PropertyModel<Boolean>(this, "selected")) {
            private static final long serialVersionUID = 3106596297110619383L;
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                onCheck((Boolean) getDefaultModelObject(), target);
            }
        };
        form.add(associated);
        add(form);
    }

    /**
     * Finds the association between the node and the service. To be overriden.
     * @param node
     * @param service
     */
    public LogicalNodeServiceAssociation getParentsAssociation(ProcessingNode node, LogicalService service) {
        // Only works in mock profile, override for dev.
        return getParentsAssociation();
    }

    /**
     * Searches whether the parents are associated or not
     * @return the LogicalNodeServiceAssociation between the two parents, if they are connected, else returns null.
     */
    protected LogicalNodeServiceAssociation getParentsAssociation() {
        for(LogicalNodeServiceAssociation assoc : parentCol.listLogicalServicesAssociations()) {
            if(assoc.getLogicalService().equals(parentRow)) {
                return assoc;
            }
        }
        return null;
    }

    /**
     * Getter for the row parent, i.e. the logical service
     * @return the row parent
     */
    public LogicalService getParentRow() {
        return parentRow;
    }

    /**
     * Getter for the column parent, i.e. the execution node
     * @return the col parent
     */
    public ProcessingNode getParentCol() {
        return parentCol;
    }

    /**
     * Used to perform some specific actions (e.g. persist the logical model)
     */
    public void onCheck(boolean selection, AjaxRequestTarget target) {

    }
}
