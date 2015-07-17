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
package com.francetelecom.clara.cloud.presentation.releases;

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
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.util.time.Duration;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.Application;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.presentation.common.AjaxFallbackCustomDataTable;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.presentation.common.WicketUtils;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;
import com.francetelecom.clara.cloud.presentation.tools.ReleaseProvider;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;

 /**
 * ReleasesTablePanel
 * Panel which list the application-releases
 *
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Updated  : $LastChangedDate$
 * @author  : $Author$
 * @version : $Revision$
 */
public class ReleasesTablePanel extends Panel {
    /**
     * SerialUID
     */
    private static final long serialVersionUID = 5846427047635208502L;
    /**
     * logger
     */
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(ReleasesTablePanel.class);

    private ManageApplicationRelease manageApplicationRelease;
    private Application app;

    AjaxFallbackCustomDataTable<ApplicationRelease, String> dataTable;

    CheckBox viewAllCheckBox;
    
    private List<ApplicationRelease> releasesList;
    private Form<?> searchCriteriaForm;
    private String searchCriteria;
    private WebMarkupContainer refreshContainer;

    public ReleasesTablePanel(String id, ManageApplicationRelease manageApplicationRelease, Application app) {
        super(id);
        this.manageApplicationRelease = manageApplicationRelease;
        this.app = app;
        initComponents();
//        initViewAllCheckBox();
    }
    private void initComponents() {
		refreshContainer = new WebMarkupContainer("refreshRealese");
		add(refreshContainer.setOutputMarkupId(true));
		
			
		}

    @Override
    protected void onBeforeRender() {
//    protected void initViewAllCheckBox() {
    	    	
    	createSearchCriteriaForm();
    	
        viewAllCheckBox = new AjaxCheckBox("allReleasesCheckbox", new Model<Boolean>(WicketSession.get().getViewAll())) {
			private static final long serialVersionUID = 3701907014721062208L;

			@Override
            protected void onUpdate(AjaxRequestTarget target) {
                WicketSession.get().setViewAll(getModelObject());
                getApplicationReleasesFromDB();
                //
                initTable();
                refreshContainer.replace(dataTable);
                target.add(refreshContainer);
            }
        };
        viewAllCheckBox.setLabel(WicketUtils.getStringResourceModel(this, "portal.release.table.header.all"));
        viewAllCheckBox.setVisible(app == null);
        searchCriteriaForm.addOrReplace(viewAllCheckBox);
        
        SimpleFormComponentLabel viewAllLabel = new SimpleFormComponentLabel("allReleasesLabel", viewAllCheckBox);
        viewAllLabel.setVisible(app == null);
        searchCriteriaForm.addOrReplace(viewAllLabel);

        getApplicationReleasesFromDB();
        
        initTable();
        refreshContainer.addOrReplace(dataTable) ;
        super.onBeforeRender();
    }

    private StringResourceModel getStringResourceModel(java.lang.String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }

    
    private void getApplicationReleasesFromDB() {
    	releasesList = new ArrayList<ApplicationRelease>();    	
		if (app != null) {
			releasesList = manageApplicationRelease.findApplicationReleasesByAppUID(app.getUID(), 0, 1000);
		} else {
			if (viewAllCheckBox.getModelObject()) {
				releasesList = manageApplicationRelease.findApplicationReleases(0, 1000);
			} else {
				releasesList = manageApplicationRelease.findMyApplicationReleases(0, 1000);
			}
		}				    	
    }

     
    private void createSearchCriteriaForm() {    	
    	searchCriteriaForm = new Form<Void>("searchCriteriaForm");
    	searchCriteriaForm.add(new CacheActivatedImage("imageHelp.searchField", getString("image.help")));
    	addOrReplace(searchCriteriaForm);
		searchCriteriaForm.add(new TextField<String>("searchCriteria", new PropertyModel<String>(this, "searchCriteria")));
		SubmitLink searchLink = new SubmitLink("searchLink");
		CacheActivatedImage imageSearch = new CacheActivatedImage("imageSearch", getString("image.search"));
		searchLink.add(imageSearch);
		searchCriteriaForm.add(searchLink);
    }

    private void initTable(){
        logger.debug("initTable()");
        final List<IColumn<ApplicationRelease, String>> columns = new ArrayList<>();

        columns.add(new PropertyColumn<ApplicationRelease, String>(getStringResourceModel("portal.release.table.header.label"), "label", "label") {
            private static final long serialVersionUID = 6169474346315297571L;

            public void populateItem(Item<ICellPopulator<ApplicationRelease>> cellItem, String componentId, IModel<ApplicationRelease> model) {
                cellItem.add(new ReleaseLinkPanel(componentId, model));
                cellItem.add(new AttributeAppender("title", model.getObject().getApplication().getLabel() + " - " + model.getObject().getReleaseVersion()));
            }

            @Override
            public String getCssClass() {
                return "releaseLabel ellipsis";
            }
        });

        columns.add(new PropertyColumn<ApplicationRelease, String>(getStringResourceModel("portal.release.table.header.description"), "description", "description"));

        columns.add(new PropertyColumn<ApplicationRelease, String>(getStringResourceModel("portal.release.table.header.lm.state"), "state", "state") {
            private static final long serialVersionUID = -3869130762585106725L;

            @Override
            public String getCssClass() {
                return "modelState lowercase";
            }

            @Override
            public void populateItem(Item<ICellPopulator<ApplicationRelease>> item, String componentId, IModel<ApplicationRelease> rowModel) {
                super.populateItem(item, componentId, rowModel);
                item.add(new AttributeAppender("title", rowModel.getObject().getState()));
            }
        });

        columns.add(new AbstractColumn<ApplicationRelease, String>(getStringResourceModel("portal.release.table.header.actions")) {
            private static final long serialVersionUID = -2548831025324697929L;

            @Override
            public void populateItem(
                    Item<ICellPopulator<ApplicationRelease>> iCellPopulatorItem,
                    String componentId,
                    IModel<ApplicationRelease> model) {
                ReleaseActionPanel releaseActPanel = new ReleaseActionPanel(componentId, model, dataTable, manageApplicationRelease);
                iCellPopulatorItem.add(releaseActPanel);
            }

            @Override
            public String getCssClass() {
                return "actionShort actionCenter";
            }
        });

        ReleaseProvider releaseProv = new ReleaseProvider(searchCriteria, releasesList);

        dataTable = new AjaxFallbackCustomDataTable<>("releasesDataTable",columns,releaseProv, PageTemplate.ROWS_PER_PAGE);
        dataTable.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(60)));
        addOrReplace(dataTable);
    }

}