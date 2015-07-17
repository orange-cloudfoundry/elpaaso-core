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
package com.francetelecom.clara.cloud.presentation.common;

import java.util.List;

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackHeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.OddEvenItem;
import org.apache.wicket.model.IModel;

/**
 * Created with IntelliJ IDEA.
 * User: shjn2064
 * Date: 05/09/12
 * Time: 13:56
 * To change this template use File | Settings | File Templates.
 */
public class AjaxFallbackCustomDataTable<T, S> extends DataTable<T, S> {

	private static final long serialVersionUID = -7695265617541424256L;

	/**
     * Constructor
     *
     * @param id           component id
     * @param columns      list of columns
     * @param dataProvider data provider
     * @param rowsPerPage  number of rows per page
     */
    public AjaxFallbackCustomDataTable(String id, List<? extends IColumn<T, S>> columns, ISortableDataProvider<T, S> dataProvider, int rowsPerPage) {
        super(id, columns, dataProvider, rowsPerPage);
        setOutputMarkupId(true);
        setVersioned(false);
        addTopToolbar(new AjaxNavigationCustomToolbar(this));
        addTopToolbar(new AjaxFallbackHeadersToolbar<>(this, dataProvider));
        addBottomToolbar(new NoRecordsToolbar(this));
        addBottomToolbar(new AjaxNavigationCustomToolbar(this));
    }

    @Override
    protected Item<T> newRowItem(final String id, final int index, final IModel<T> model)
    {
        return new OddEvenItem<T>(id, index, model);
    }
}
