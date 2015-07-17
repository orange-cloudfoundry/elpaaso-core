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

import com.francetelecom.clara.cloud.presentation.tools.component.OAjaxBigPagingNavigator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * Created with IntelliJ IDEA.
 * User: shjn2064
 * Date: 05/09/12
 * Time: 14:08
 * To change this template use File | Settings | File Templates.
 */
public class AjaxNavigationCustomToolbar extends NavigationToolbar {

	private static final long serialVersionUID = 7152648877331659233L;

	public static final int NUMBER_PAGE_DISPLAYED = 5;

    public AjaxNavigationCustomToolbar(final DataTable<?, ?> table) {
        super(table);
    }


    @Override
    protected PagingNavigator newPagingNavigator(String navigatorId, final DataTable<?, ?> table) {

        OAjaxBigPagingNavigator oAjaxBigPagingNavigator = new OAjaxBigPagingNavigator(navigatorId, table) {
            private static final long serialVersionUID = 1L;

            /**
             * Implement our own ajax event handling in order to update the datatable itself, as the
             * default implementation doesn't support DataViews.
             *
             * @see AjaxPagingNavigator#onAjaxEvent(org.apache.wicket.ajax.AjaxRequestTarget)
             */
            @Override
            protected void onAjaxEvent(final AjaxRequestTarget target) {
                target.add(table);
            }

            @Override
            protected PagingNavigation newNavigation(String id, IPageable pageable, IPagingLabelProvider labelProvider) {
                PagingNavigation nv = super.newNavigation(id, pageable, labelProvider);
                nv.setViewSize(NUMBER_PAGE_DISPLAYED);
                return nv;
            }

        };
        return oAjaxBigPagingNavigator;
    }


}
