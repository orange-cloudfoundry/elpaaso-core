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
package com.francetelecom.clara.cloud.presentation.admin;

import com.francetelecom.clara.cloud.presentation.common.PageTemplate;
import com.francetelecom.clara.cloud.presentation.tools.WicketSession;
import com.francetelecom.clara.cloud.scalability.helper.PaasStats;
import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.sort.SortOrder;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.StringResourceModel;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * StatsTablePanel
 * Class that hold admin stats table
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 * Sample usage : see ManageStatisticsImplTest
 * @version     : $Revision$
 */
public class StatsTablePanel extends Panel {
    private static final long serialVersionUID = 527385503166750788L;
    private static final transient org.slf4j.Logger logger = LoggerFactory.getLogger(StatsTablePanel.class);

    AjaxFallbackDefaultDataTable<PaasStats, String> dataTable;

    public StatsTablePanel(String id) {
        super(id);
        initTable();

    }

    private StringResourceModel getStringResourceModel(String key) {
        // BVA fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
        return new StringResourceModel(key, this, null);
    }

    private void initTable(){
        final
        List<IColumn<PaasStats, String>> columns = new ArrayList<IColumn<PaasStats, String>>();

        /*
        PropertyColumn creationDateColumn
          = new PropertyColumn<PaasStats>(
                getStringResourceModel("portal.stats.table.header.date"), "creationDateString");
         */
        AbstractColumn<PaasStats, String> psDate
          = new AbstractColumn<PaasStats, String>(
                getStringResourceModel("portal.stats.table.header.date")) {

            private static final long serialVersionUID = 6259234503151995554L;

            public void populateItem(Item<ICellPopulator<PaasStats>> cellItem, String componentId,
                IModel<PaasStats> rowModel)
            {
                PaasStats ps = rowModel.getObject();
                SimpleDateFormat sdf = new SimpleDateFormat();
                sdf.applyPattern(getString("portal.stats.date.format"));

                cellItem.add(new Label(componentId, sdf.format(ps.getCreationDate())));
            }

            @Override
            public String getCssClass() {
                return "width1500";
            }
        };
        columns.add(psDate);
        columns.add(new PropertyColumn<PaasStats, String>(
                getStringResourceModel("portal.stats.table.header.name"), "shortName")
        );

        /*
        columns.add(new PropertyColumn<PaasStats>(
                getStringResourceModel("portal.stats.table.header.details"), "htmlDetails")
        );
        */

        AbstractColumn<PaasStats, String> psDetails
          = new AbstractColumn<PaasStats, String>(
                getStringResourceModel("portal.stats.table.header.details")) {

            private static final long serialVersionUID = 5359675125271261783L;

            public void populateItem(Item<ICellPopulator<PaasStats>> cellItem, String componentId,
                IModel<PaasStats> rowModel)
            {
                PaasStats ps = rowModel.getObject();
                cellItem.add(new MultiLineLabel(componentId, ps.getDetails()));
            }
        };
        columns.add(psDetails);

        StatsProvider statsProv = new StatsProvider();
        statsProv.setSort("creationTime", SortOrder.ASCENDING);

        dataTable = new AjaxFallbackDefaultDataTable<PaasStats, String>("dataTable",columns,statsProv, PageTemplate.ROWS_PER_PAGE);
        // wait and see if needed // dataTable.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(10)));
        add(dataTable);
    }

    class StatsProvider extends SortableDataProvider<PaasStats, String> {

        private static final long serialVersionUID = -607185222587506578L;

        @Override
        public Iterator<PaasStats> iterator(long first, long count) {
            WicketSession ws = (WicketSession)getSession();
            return ws.getStats().iterator();
        }

        @Override
        public long size() {
            WicketSession ws = (WicketSession)getSession();
            return ws.getStats().size();
        }

        @Override
        public IModel<PaasStats> model(PaasStats object) {
            final Long sid = object.getCreationTime();
            return new LoadableDetachableModel<PaasStats>() {
                private static final long serialVersionUID = -3639867974705106409L;

                @Override
                protected PaasStats load() {
                    WicketSession ws = (WicketSession)getSession();
                    return ws.getStatById(sid);
                }

            };
        }

    }
}
