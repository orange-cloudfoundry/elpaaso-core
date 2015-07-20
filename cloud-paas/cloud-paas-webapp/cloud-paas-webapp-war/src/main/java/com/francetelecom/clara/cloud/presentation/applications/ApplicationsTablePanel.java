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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.time.Duration;

import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.presentation.common.AjaxFallbackCustomDataTable;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;
import com.francetelecom.clara.cloud.presentation.tools.ApplicationProvider;
import com.francetelecom.clara.cloud.presentation.tools.BusinessExceptionHandler;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;

/**
 * ApplicationsTablePanel
 * Panel which show the applications
 *
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Updated  : $LastChangedDate$
 * @author  : $Author$
 * @version : $Revision$
 */
public class ApplicationsTablePanel extends Panel {
	
    private static final long serialVersionUID = 527385503166750788L;
    
    //private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ApplicationsTablePanel.class);

    
    AjaxFallbackCustomDataTable<Application, String> dataTable;

    CheckBox viewAllCheckBox;

    private ApplicationsPage parentPage;

    private List<Application> applicationsList;
    private Form<?> searchCriteriaForm;
    private String searchCriteria;
    
    private WebMarkupContainer refreshContainer;

    
    public ApplicationsTablePanel(String id) {
        super(id);
        initComponents();
        
    }
    
    private void initComponents() {
		refreshContainer = new WebMarkupContainer("refreshApplication");
		add(refreshContainer.setOutputMarkupId(true));
		
			
		}

    private StringResourceModel getStringResourceModel(java.lang.String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }

    private void initTable(){
        final List<IColumn<Application, String>> columns = new ArrayList<IColumn<Application, String>>();

        columns.add(new PropertyColumn<Application, String>(getStringResourceModel("portal.application.table.header.name"), "label", "label") {
            private static final long serialVersionUID = -5548238873027346512L;

            @Override
            public void populateItem(Item<ICellPopulator<Application>> cellItem, String id, IModel<Application> appIModel) {
                cellItem.add(new ApplicationDetailsLinkPanel(id, appIModel));
                cellItem.add(new AttributeAppender("title", appIModel.getObject().getLabel()));
            }

            @Override
            public String getCssClass() {
                return "label ellipsis";
            }
        });

        columns.add(new PropertyColumn<Application, String>(getStringResourceModel("portal.application.table.header.appCode"), "code", "code") {
			private static final long serialVersionUID = 2743741360422459728L;

			@Override
            public void populateItem(Item<ICellPopulator<Application>> item, String componentId, IModel<Application> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(new AttributeAppender("title", rowModel.getObject().getCode()));
            }

            @Override
            public String getCssClass() {
                return "label ellipsis";
            }
        });

        columns.add(new PropertyColumn<Application, String>(getStringResourceModel("portal.application.table.header.description"), "description", "description"));

        columns.add(new PropertyColumn<Application, String>(getStringResourceModel("portal.application.table.header.visibility"), "visibility", "visibility") {
 			private static final long serialVersionUID = -2362613895284301548L;

			@Override
            public void populateItem(Item<ICellPopulator<Application>> iCellPopulatorItem, String componentId, IModel<Application> appIModel) {
                iCellPopulatorItem.add(new ApplicationVisibilityPanel(componentId, appIModel));
            }

            @Override
            public String getCssClass() {
                return "actionShort actionCenter";
            }
        });

        columns.add(new AbstractColumn<Application, String>(getStringResourceModel("portal.application.table.header.actions")) {
            private static final long serialVersionUID = -2548831025324697929L;

            @Override
            public void populateItem(Item<ICellPopulator<Application>> iCellPopulatorItem, String componentId, IModel<Application> appIModel) {
                ApplicationActionPanel appActPanel = null;
                try {
                    appActPanel = new ApplicationActionPanel(componentId, appIModel, parentPage.getManageApplication().canBeDeleted(appIModel.getObject().getUID()));
                } catch (ObjectNotFoundException e) {
                    BusinessExceptionHandler handler = new BusinessExceptionHandler(parentPage);
                    handler.error(e);
                }
                iCellPopulatorItem.add(appActPanel);
            }

            @Override
            public String getCssClass() {
                return "actionShort actionCenter";
            }
        });

        //ApplicationProvider appProv = new ApplicationProvider(this, viewAllCheckBox.getModel(), parentPage.getManageApplication());
        ApplicationProvider appProv = new ApplicationProvider(searchCriteria, applicationsList);

        dataTable = new AjaxFallbackCustomDataTable<>("applicationsDataTable",columns,appProv, PageTemplate.ROWS_PER_PAGE);
        dataTable.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(60)));
        addOrReplace(dataTable);
    }

    @Override
    protected void onBeforeRender() {

        this.parentPage = (ApplicationsPage) getPage();

        createSearchCriteriaForm();
        
        viewAllCheckBox = new AjaxCheckBox("allAppsCheckbox", new Model<Boolean>(WicketSession.get().getViewAll())) {
			private static final long serialVersionUID = -336651607302799133L;

			@Override
            protected void onUpdate(AjaxRequestTarget target) {
                WicketSession.get().setViewAll(getModelObject());
                getApplicationsFromDB();
                //
                initTable();
                refreshContainer.replace(dataTable);
                target.add(refreshContainer);
               
            }
        };
        searchCriteriaForm.addOrReplace(viewAllCheckBox);

        getApplicationsFromDB();
        
        initTable();
        refreshContainer.addOrReplace(dataTable) ;
        super.onBeforeRender();
    }
    
    
    private void getApplicationsFromDB() {
    	applicationsList = new ArrayList<Application>();
    	if (viewAllCheckBox.getModelObject()) {
    		applicationsList = parentPage.getManageApplication().findApplications(0, 1000, "label", "ASC");
        } else {
        	applicationsList = parentPage.getManageApplication().findMyApplications(0, 1000, "label", "ASC");
        }
    	
    }


    private void createSearchCriteriaForm() {
    	searchCriteriaForm = new Form<Void>("searchCriteriaForm");
    	searchCriteriaForm.add(new CacheActivatedImage("imageHelp.searchField",  new ResourceModel("image.help").getObject()));
    	addOrReplace(searchCriteriaForm);
		searchCriteriaForm.add(new TextField<String>("searchCriteria", new PropertyModel<String>(this, "searchCriteria")));
		SubmitLink searchLink = new SubmitLink("searchLink");
		CacheActivatedImage imageSearch = new CacheActivatedImage("imageSearch",  new ResourceModel("image.help").getObject());
		searchLink.add(imageSearch);
		searchCriteriaForm.add(searchLink);
    }

}
