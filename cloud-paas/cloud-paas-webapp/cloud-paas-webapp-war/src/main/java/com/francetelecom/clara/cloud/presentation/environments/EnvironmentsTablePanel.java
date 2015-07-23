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

import com.francetelecom.clara.cloud.core.service.ManageApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.ApplicationRelease;
import com.francetelecom.clara.cloud.coremodel.exception.ApplicationReleaseNotFoundException;
import com.francetelecom.clara.cloud.coremodel.exception.ObjectNotFoundException;
import com.francetelecom.clara.cloud.environment.ManageEnvironment;
import com.francetelecom.clara.cloud.presentation.common.AjaxFallbackCustomDataTable;
import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.presentation.common.WicketUtils;
import com.francetelecom.clara.cloud.presentation.resource.CacheActivatedImage;
import com.francetelecom.clara.cloud.presentation.tools.BusinessExceptionHandler;
import com.francetelecom.clara.cloud.presentation.tools.EnvironmentDtoProvider;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;
import com.francetelecom.clara.cloud.services.dto.EnvironmentDto;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * EnvironmentsTablePanel Panel which list the environments
 *
 * User: Thomas Escalle - tawe8231 Entity : FT/OLNC/RD/MAPS/MEP/MSE Updated :
 * $LastChangedDate$
 * 
 * @author : $Author$
 * @version : $Revision$
 */
public class EnvironmentsTablePanel extends Panel {

	private static final long serialVersionUID = -2237439072861854585L;

	private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(EnvironmentsTablePanel.class);

	ManageEnvironment manageEnvironment;
	ManageApplicationRelease manageApplicationRelease;

	private ApplicationRelease release;
	AjaxFallbackCustomDataTable<EnvironmentDto, String> dataTable;

	AjaxCheckBox viewAllCheckBox;
	private WebMarkupContainer refreshContainer;
	private Duration ajaxRefreshPeriod = Duration.milliseconds(10000);

	private EnvironmentDtoProvider envDtoProvider;

	private List<EnvironmentDto> envList;
	private Form<?> searchCriteriaForm;
	private String searchCriteria;

	public EnvironmentsTablePanel(String id, ManageApplicationRelease manageApplicationRelease, ManageEnvironment manageEnvironment, ApplicationRelease release) {
		super(id);
		this.release = release;
		this.manageEnvironment = manageEnvironment;
		this.manageApplicationRelease = manageApplicationRelease;
		initComponents();
		
	}

	private void initComponents() {
		refreshContainer = new WebMarkupContainer("refresh");
		add(refreshContainer.setOutputMarkupId(true));

	}

	@Override
	protected void onBeforeRender() {

		createSearchCriteriaForm();

		viewAllCheckBox = new AjaxCheckBox("allEnvironmentsCheckbox", new Model<Boolean>(WicketSession.get().getViewAll())) {
			private static final long serialVersionUID = 6279581094195253824L;

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				WicketSession.get().setViewAll(getModelObject());
				
				updateEnvDetailsPanel(target);

			}
		};
		viewAllCheckBox.setLabel(WicketUtils.getStringResourceModel(this, "portal.environment.table.header.all"));
		viewAllCheckBox.setVisible(release == null);
		searchCriteriaForm.addOrReplace(viewAllCheckBox);

		SimpleFormComponentLabel viewAllLabel = new SimpleFormComponentLabel("allEnvironmentsLabel", viewAllCheckBox);
		viewAllLabel.setVisible(release == null);
		searchCriteriaForm.addOrReplace(viewAllLabel);

		getEnvironmentsFromDB();

		initTable();
		refreshContainer.addOrReplace(dataTable);
		super.onBeforeRender();
	}

	private StringResourceModel getStringResourceModel(java.lang.String key) {
		// BVA fix Localizer warning : cf.
		// https://issues.apache.org/jira/browse/WICKET-990
		return new StringResourceModel(key, this, null);
	}

	private void getEnvironmentsFromDB() {
		if (release != null) {
			try {

				envList = manageEnvironment.findEnvironmentsByAppRelease(release.getUID());

			} catch (ApplicationReleaseNotFoundException e) {
				BusinessExceptionHandler handler = new BusinessExceptionHandler(this);
				handler.error(e);
			}
		} else {
			if (viewAllCheckBox.getModelObject()) {
				envList = manageEnvironment.findEnvironments();
			} else {

				envList = manageEnvironment.findMyEnvironments();
			}
		}
		
		envDtoProvider = new EnvironmentDtoProvider(searchCriteria, envList);
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

	private void initTable() {

		final List<IColumn<EnvironmentDto, String>> columns = new ArrayList<IColumn<EnvironmentDto, String>>();

		columns.add(new PropertyColumn<EnvironmentDto, String>(getStringResourceModel("portal.environment.table.header.release"), "applicationReleaseLabel",
				"applicationReleaseLabel") {
			private static final long serialVersionUID = -8946588888634720477L;

			@Override
			public void populateItem(Item<ICellPopulator<EnvironmentDto>> cellItem, String componentId, IModel<EnvironmentDto> rowModel) {
				String releaseName = rowModel.getObject().getApplicationLabel() + " - " + rowModel.getObject().getReleaseVersion();
				cellItem.add(new Label(componentId, releaseName));
				cellItem.add(new AttributeAppender("title", releaseName));
			}

			@Override
			public String getCssClass() {
				return "releaseLabel ellipsis";
			}
		});

		columns.add(new PropertyColumn<EnvironmentDto, String>(getStringResourceModel("portal.environment.table.header.label"), "label", "label") {

			private static final long serialVersionUID = -5548238873027346512L;

			@Override
			public void populateItem(Item<ICellPopulator<EnvironmentDto>> cellItem, String id, IModel<EnvironmentDto> environmentIModel) {
				ApplicationRelease envRelease = null;
				try {
					envRelease = manageApplicationRelease.findApplicationReleaseByUID(environmentIModel.getObject().getReleaseUID());
				} catch (ObjectNotFoundException e) {
					logger.warn("release with uid " + environmentIModel.getObject().getReleaseUID() + " does not exist.");
				}

				EnvironmentDetailsLinkPanel envDetailsLinkPanel = new EnvironmentDetailsLinkPanel(id, environmentIModel, envRelease);
				cellItem.add(envDetailsLinkPanel);
				cellItem.add(new AttributeAppender("title", environmentIModel.getObject().getLabel()));
			}

			@Override
			public String getCssClass() {
				return "ellipsis";
			}
		});

		columns.add(new PropertyColumn<EnvironmentDto, String>(getStringResourceModel("portal.environment.table.header.type"), "type", "type") {
			private static final long serialVersionUID = 7844363857397373689L;

			@Override
			public String getCssClass() {
				return "envType lowercase ellipsis";
			}

			@Override
			public void populateItem(Item<ICellPopulator<EnvironmentDto>> item, String componentId, IModel<EnvironmentDto> rowModel) {
				super.populateItem(item, componentId, rowModel);
				item.add(new AttributeAppender("title", rowModel.getObject().getType()));
			}

		});

		columns.add(new PropertyColumn<EnvironmentDto, String>(getStringResourceModel("portal.environment.table.header.owner"), "ownerName", "ownerName") {
			private static final long serialVersionUID = -5782673799227635269L;

			@Override
			public String getCssClass() {
				return "envOnwer ellipsis";
			}

			@Override
			public void populateItem(Item<ICellPopulator<EnvironmentDto>> item, String componentId, IModel<EnvironmentDto> rowModel) {
				super.populateItem(item, componentId, rowModel);
				item.add(new AttributeAppender("title", rowModel.getObject().getOwnerName()));
			}
		});

		
		columns.add(new PropertyColumn<EnvironmentDto, String>(getStringResourceModel("portal.environment.table.header.date"), "creationDate", "creationDate") {
			private static final long serialVersionUID = 2536737828925838465L;

			@Override
			public void populateItem(Item<ICellPopulator<EnvironmentDto>> cellItem, String componentId, IModel<EnvironmentDto> environmentIModel) {
				SimpleDateFormat sdf = new SimpleDateFormat();
				sdf.applyPattern(getString("portal.environment.creation.date.format"));
				Label date = new Label(componentId, sdf.format(environmentIModel.getObject().getCreationDate()));
				cellItem.add(date);
				cellItem.add(new AttributeAppender("title", environmentIModel.getObject().getCreationDate()));
			}

			@Override
			public String getCssClass() {
				return "envDate ellipsis";
			}
		});

		columns.add(new PropertyColumn<EnvironmentDto, String>(getStringResourceModel("portal.environment.table.header.status"), "status", "status") {

			private static final long serialVersionUID = 2429816459286842726L;

			@Override
			public void populateItem(Item<ICellPopulator<EnvironmentDto>> iCellPopulatorItem, String componentId, IModel<EnvironmentDto> environmentIModel) {

				EnvironmentPercentPanel<EnvironmentDto> envPercentPanel = new EnvironmentPercentPanel<>(componentId, environmentIModel);

				iCellPopulatorItem.add(envPercentPanel);
				iCellPopulatorItem.add(new AttributeAppender("title", environmentIModel.getObject().getStatus()));

			}

			@Override
			public String getCssClass() {
				return "envStatus actionCenter";
			}
		});

		columns.add(new AbstractColumn<EnvironmentDto, String>(getStringResourceModel("portal.environment.table.header.actions")) {
			private static final long serialVersionUID = -2548831025324697929L;

			@Override
			public void populateItem(Item<ICellPopulator<EnvironmentDto>> iCellPopulatorItem, String componentId, IModel<EnvironmentDto> environmentIModel) {
				EnvironmentActionPanel<EnvironmentDto> envActPanel = new EnvironmentActionPanel<>(componentId, environmentIModel);
				iCellPopulatorItem.add(envActPanel);
			}

			@Override
			public String getCssClass() {
				return "actionLong actionCenter";
			}
		});

		envDtoProvider = new EnvironmentDtoProvider(searchCriteria, envList);

		dataTable = new AjaxFallbackCustomDataTable<EnvironmentDto, String>("dataTable", columns, envDtoProvider, PageTemplate.ROWS_PER_PAGE);

		dataTable.add(new AjaxSelfUpdatingTimerBehavior(ajaxRefreshPeriod) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onPostProcessTarget(AjaxRequestTarget target) {
				// on va en base recuperer les données
				updateEnvDetailsPanel(target);

			}

		});
		addOrReplace(dataTable);
		refreshContainer.addOrReplace(dataTable);

	}

	private void updateEnvDetailsPanel(AjaxRequestTarget target) {
		getEnvironmentsFromDB();
		initTable();

		refreshContainer.replace(dataTable);
		target.add(refreshContainer);

	}

	public void updateEnvActionsPanelAndStatus(AjaxRequestTarget target) {
		target.add(this);
		
	}

}
