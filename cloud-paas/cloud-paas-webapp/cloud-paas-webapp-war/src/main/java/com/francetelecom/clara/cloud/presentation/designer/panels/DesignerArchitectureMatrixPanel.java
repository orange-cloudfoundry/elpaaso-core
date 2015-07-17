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
import java.util.Collections;
import java.util.List;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.ContainerFeedbackMessageFilter;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.GuiClassMapping;
import com.francetelecom.clara.cloud.logicalmodel.CFJavaProcessing;
import com.francetelecom.clara.cloud.logicalmodel.JeeProcessing;
import com.francetelecom.clara.cloud.logicalmodel.LogicalDeployment;
import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;
import com.francetelecom.clara.cloud.logicalmodel.LogicalService;
import com.francetelecom.clara.cloud.logicalmodel.LogicalSoapService;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerHelperPage;
import com.francetelecom.clara.cloud.presentation.designer.pages.DesignerPage;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDetailsDto;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 20/10/11
 * Time: 14:17
 * To change this template use File | Settings | File Templates.
 */
public class DesignerArchitectureMatrixPanel extends Panel {

	private static final long serialVersionUID = 1050291937613160217L;

	/**
     * Logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(DesignerArchitectureMatrixPanel.class);

    /**
     * List of all the execution nodes of the logical model
     */
    private List<ProcessingNode> colHeaders;

    /**
     * List of all the logical services of the logical model
     */
    private List<LogicalService> rowHeaders;

    /* Container which allows the refresh of the table */
    private WebMarkupContainer matrixContainer;

    private ListView<List<LogicalModelItem>> matrixContent;

    private FeedbackPanel matrixFeedback;

    private boolean readOnly = false;

    private boolean allowOverride;

    private EnvironmentDetailsDto envDetailsDto;

    private DesignerHelperPage parentPage;

    private int step = 2;

    private enum CellType {
        HEADER_COL,
        HEADER_ROW,
        DATA,
        CORNER
    }


    public DesignerArchitectureMatrixPanel(String id, DesignerHelperPage parentPage, boolean isArchitectureLocked, boolean allowOverride) {
        super(id);

        this.parentPage = parentPage;
        this.readOnly = isArchitectureLocked;
        this.allowOverride = allowOverride;

        initLists();

        updateTable();

        initComponents();

    }

    public DesignerArchitectureMatrixPanel(String id, DesignerHelperPage parentPage, EnvironmentDetailsDto selectedEnvDto, boolean isArchitectureLocked, boolean allowOverride) {
        super(id);

        this.parentPage = parentPage;
        this.readOnly = isArchitectureLocked;
        this.allowOverride = allowOverride;
        setEnvDetailsDto(selectedEnvDto);

        initLists();

        updateTable();

        initComponents();

    }

    public LogicalDeployment getLogicalDeployment() {
        return parentPage.getLogicalDeployment();
    }
    
    public void setEnvDetailsDto(EnvironmentDetailsDto envDetailsDto) {
        this.envDetailsDto = envDetailsDto;
    }

    private void initLists() {
        colHeaders = new ArrayList<ProcessingNode>();
        rowHeaders = new ArrayList<LogicalService>();
    }

    public int getIndexOfService(LogicalService service) {
        int index = 0;
        for (LogicalService serv : rowHeaders) {
            index++;
            if (service.equalsShallow(serv)) {
                return index;
            }
        }
        return -1;
    }

    public int getIndexOfNode(ProcessingNode node) {
        int index = 0;
        for (ProcessingNode execNode : colHeaders) {
            index++;
            if (execNode.equals(node)) {
                return index;
            }
        }
        return -1;
    }

    /**
     * Generates the list contained in the table, using headers lists, and generating other cells
     *
     * @return the list contained in the table
     */
    private List<List<LogicalModelItem>> getImprovedList() {
        List<List<LogicalModelItem>> res = new ArrayList<>();
        List<LogicalModelItem> firstRow = new ArrayList<>();

        /* Top left corner */
        firstRow.add(null);
        /* We copy that row, so that we split model (effective rows) and view (effective rows + corners) */
        Collections.sort(colHeaders);
        firstRow.addAll(colHeaders);
        res.add(firstRow);
        Collections.sort(rowHeaders);
        for (LogicalService rowHeader : rowHeaders) {
            List<LogicalModelItem> row = new ArrayList<>();
            row.add(rowHeader);
            for (int i=0; i<colHeaders.size(); i++) {
                row.add(null);
            }
            res.add(row);
        }
        return res;
    }

    /**
     * Gives the type of the cell at (indexCol, indexRow)
     *
     * @param indexRow index of the row
     * @param indexCol index of the column
     * @return the type of the cell
     */
    private CellType getCellTypeAt(int indexRow, int indexCol) {
        if (indexRow == 0) {
            if (indexCol == 0) {
                /* Top left corner */
                return CellType.CORNER;
            } else {
                /* First row : headers */
                return CellType.HEADER_COL;
            }
        } else if (indexCol == 0) {
            /* First col : headers */
            return CellType.HEADER_ROW;
        } else {
            /* Data */
            return CellType.DATA;
        }
    }

    public void updateTable() {
        logger.debug("update table");

        LogicalDeployment ld = parentPage.getLogicalDeployment();

        if (ld != null) {
            logger.debug("logicalDeployment : " + ld);
            logger.debug("services : " + ld.listLogicalServices());
            logger.debug("exec : " + ld.listProcessingNodes());
            colHeaders.clear();
            colHeaders.addAll(ld.listProcessingNodes());

            rowHeaders.clear();
            rowHeaders.addAll(ld.listLogicalServices());
            Collections.sort(rowHeaders);
            if(matrixContent != null) {
                matrixContent.setDefaultModelObject(getImprovedList());
            }
        } else {
//            parentPage.error(getString("portal.error.architecture.notFound"));
        }
        logger.debug("cols/rows :" + colHeaders + " // " + rowHeaders);
    }

    /**
     * Manages data cells. If the cell does not exist, it creates a new one and returns it, else it returns the existing one.
     *
     * @param model     model to give to the Panel
     * @param rowHeader header of the row of the cell
     * @param colHeader header of the column of the cell
     * @return the correct cell, created if necessary
     */
    private DesignerArchitectureMatrixCellDataPanel getPanelFor(LogicalService rowHeader, ProcessingNode colHeader) {
        //String headerName = colHeader.getLabel();
        //String rowName = rowHeader.getName();
        /*
        // @todo 2 different WebGUI services (with the same name) fix equals implementation &/or throw an exception (?)
        // bug when 2 different WebGUI services (with the same name) are attached to the same node
        // equals implementation seems not work
        // @link http://elpaaso_shp/index.php/Discuter:Portal#Logical_model
        */
        DesignerArchitectureMatrixCellDataPanel dataPanel = new DesignerArchitectureMatrixCellDataPanel("content", rowHeader, colHeader) {
            private static final long serialVersionUID = 1704692833383041873L;

            @Override
            public void onCheck(boolean selection, AjaxRequestTarget target) {
                String jeeProcessingLabel = getParentCol().getLabel();
                String serviceLabel = getParentRow().getLabel();

                if (selection) {
                    if (parentPage instanceof DesignerPage) {
                        ((DesignerPage)parentPage).addNodeServiceAssociation(jeeProcessingLabel, serviceLabel, target);
                    }
                } else {
                    if (parentPage instanceof DesignerPage) {
                        ((DesignerPage)parentPage).removeNodeServiceAssociation(jeeProcessingLabel, serviceLabel, target);
                    }
                }
                target.add(this);
            }
        };
        return dataPanel;
    }

    @Override
    protected void onModelChanged() {
        updateTable();
    }

    private void initComponents() {

        matrixFeedback = new FeedbackPanel("matrixFeedback", new ContainerFeedbackMessageFilter(this));
        matrixFeedback.setOutputMarkupId(true);
        add(matrixFeedback);

        matrixContainer = new WebMarkupContainer("matrixContainer");


        /* ListView - first level */
        matrixContent = new ListView<List<LogicalModelItem>>("listRows", getImprovedList()) {
            private static final long serialVersionUID = -6363917228846486149L;

            @Override
            protected void populateItem(final ListItem<List<LogicalModelItem>> listItem) {
                /* ListView - secondLevel */
                listItem.add(new ListView<LogicalModelItem>("listCols", listItem.getModelObject()) {

                    private static final long serialVersionUID = -4827251147718587694L;

                    @Override
                    protected void populateItem(ListItem<LogicalModelItem> logicalServiceListItem) {
                        int rowIndex = listItem.getIndex();
                        int colIndex = logicalServiceListItem.getIndex();

                        CellType type = getCellTypeAt(rowIndex, colIndex);

                        final LogicalService service = rowIndex > 0 ? rowHeaders.get(rowIndex - 1) : null;
                        final ProcessingNode node = colIndex > 0 ? colHeaders.get(colIndex - 1) : null;

                        String cellBackGroundColor = "externalSrvBckGrd";
                        if (service != null) {
                            if (service.getClass().getAnnotation(GuiClassMapping.class).isExternal()) {
                                cellBackGroundColor = "externalSrvBckGrd";
                            } else {
                                cellBackGroundColor = "internalSrvBckGrd";
                            }
                        }

                        /* Population of the item depends on the type of the cell */
                        switch (type) {
                            case CORNER: /* top left corner */
                                logicalServiceListItem.add(new AttributeModifier("class", "cornerCell"));
                                logicalServiceListItem.add(new Label("content", " "));
                                logicalServiceListItem.setVisible(!colHeaders.isEmpty() || !rowHeaders.isEmpty());
                                break;
                            case HEADER_COL: /* first cell of the col */
                                DesignerArchitectureMatrixCellButtonPanel headerColCellPanel
                                        = new DesignerArchitectureMatrixCellButtonPanel("content", logicalServiceListItem.getModel(), envDetailsDto, readOnly, allowOverride) {

                                    private static final long serialVersionUID = 7989553592786367825L;

                                    @Override
                                    protected void onClickDelete(AjaxRequestTarget target) {
                                        if (parentPage instanceof DesignerPage) {
                                            ((DesignerPage)parentPage).removeLogicalService(node, target, DesignerArchitectureMatrixPanel.this);
                                        }
                                    }

                                    @Override
                                    protected void onClickEdit(AjaxRequestTarget target) {
                                        if (parentPage instanceof DesignerPage) {
                                            step = ((DesignerPage) parentPage).getLogicalServicesHelper().isLogicalServiceExternal(node) ? 0 : 1;
                                            ((DesignerPage)parentPage).managePageComponents(target, step, node);
                                        }
                                    }

                                    @Override
                                    protected void onClickView(AjaxRequestTarget target) {
                                        parentPage.openModalWindow(target, node, false);
                                    }

                                    @Override
                                    protected void onClickWspInfo(AjaxRequestTarget target) {
                                        // Can never happen on a JeeProcessing
                                    }

                                    @Override
                                    protected void onClickConfigOverride(AjaxRequestTarget target) {
                                        // Can not happen for now on a JeeProcessing
                                    }
                                };
                                logicalServiceListItem.add(new AttributeModifier("class","hColCell"));
                                logicalServiceListItem.add(new AttributeAppender("class", new Model<>("logicalService"), " "));
                                // add default ProcessingNode icon
                                String className = ProcessingNode.class.getSimpleName();
                                if (logicalServiceListItem.getModelObject() instanceof JeeProcessing) {
                                	className = JeeProcessing.class.getSimpleName();
                                } else if (logicalServiceListItem.getModelObject() instanceof CFJavaProcessing) {
                                	className = CFJavaProcessing.class.getSimpleName();
                                }
                                logicalServiceListItem.add(new AttributeAppender("class", new Model<>("Logical" + className), " "));
                                // if custom icon selected, display it
                                if (logicalServiceListItem.getModel() != null && ((ProcessingNode) logicalServiceListItem.getModelObject()).getIconUrl() != null && !((ProcessingNode) logicalServiceListItem.getModelObject()).getIconUrl().equals("")) {
                                	logicalServiceListItem.add(new AttributeModifier("style",new Model<>("background-image:url(\""+((ProcessingNode) logicalServiceListItem.getModelObject()).getIconUrl()+"\"); background-repeat:no-repeat; background-position:5px 5px; background-size:32px 32px;")));
                                }
                                logicalServiceListItem.add(headerColCellPanel);
                                break;
                            case HEADER_ROW: /* first cell of the row */
                                DesignerArchitectureMatrixCellButtonPanel headerRowCellPanel = new DesignerArchitectureMatrixCellButtonPanel("content", logicalServiceListItem.getModel(), envDetailsDto, readOnly, allowOverride) {

                                    private static final long serialVersionUID = -6883719251719480265L;

                                    @Override
                                    protected void onClickDelete(AjaxRequestTarget target) {
                                        if (parentPage instanceof DesignerPage) {
                                            ((DesignerPage)parentPage).removeLogicalService(service, target, DesignerArchitectureMatrixPanel.this);
                                        }
                                    }

                                    @Override
                                    protected void onClickEdit(AjaxRequestTarget target) {
                                        if (parentPage instanceof DesignerPage) {
                                            step = ((DesignerPage) parentPage).getLogicalServicesHelper().isLogicalServiceExternal(service) ? 0 : 1;
                                            ((DesignerPage)parentPage).managePageComponents(target, step, service);
                                        }
                                    }

                                    @Override
                                    protected void onClickView(AjaxRequestTarget target) {
                                        parentPage.openModalWindow(target, service, false);
                                    }

                                    @Override
                                    protected void onClickWspInfo(AjaxRequestTarget target) {
                                        parentPage.openWspInfoPanel(target, (LogicalSoapService) service, envDetailsDto);
                                    }

                                    @Override
                                    protected void onClickConfigOverride(AjaxRequestTarget target) {
                                        parentPage.openModalWindow(target, service, true);
                                    }
                                };
                                logicalServiceListItem.add(new AttributeModifier("class", cellBackGroundColor));
                                logicalServiceListItem.add(new AttributeAppender("class", new Model<>("hRowCell"), " "));
                                logicalServiceListItem.add(new AttributeAppender("class", new Model<>("logicalService"), " "));
                                logicalServiceListItem.add(new AttributeAppender("class", new Model<>(service.getClass().getSimpleName()), " "));

                                logicalServiceListItem.add(headerRowCellPanel);
                                break;
                            case DATA:
                                final DesignerArchitectureMatrixCellDataPanel dataPanel = getPanelFor(service, node);
                                dataPanel.setEnabled(!readOnly);
                                dataPanel.setOutputMarkupId(true);
                                logicalServiceListItem.add(new AttributeModifier("class", cellBackGroundColor));
                                logicalServiceListItem.add(dataPanel);
                                break;
                            default:
                                break;
                        }
                    }
                });
            }
        };
        matrixContent.setOutputMarkupId(true);

        matrixContainer.add(matrixContent);
        matrixContainer.setOutputMarkupId(true);
        add(matrixContainer);
        setOutputMarkupId(true);

        WebMarkupContainer matrixLegendContainer = new WebMarkupContainer("matrixLegendContainer") {
			private static final long serialVersionUID = 8725411660499502794L;

			@Override
            public boolean isVisible() {
                if (parentPage == null) return false;
                LogicalDeployment ld = parentPage.getLogicalDeployment();
                if (ld == null) return false;
                return !(ld.listProcessingNodes().isEmpty() && ld.listLogicalServices().isEmpty());
            }
        };
        add(matrixLegendContainer);
    }
}
