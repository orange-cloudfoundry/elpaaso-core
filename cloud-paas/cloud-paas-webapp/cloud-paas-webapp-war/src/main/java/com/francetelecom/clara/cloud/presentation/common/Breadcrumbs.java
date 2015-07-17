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

import com.francetelecom.clara.cloud.presentation.tools.BreadcrumbsItem;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 07/07/11
 */
public class Breadcrumbs extends Panel {
    private static final long serialVersionUID = 1403160664367974273L;

    List<BreadcrumbsItem> breadcrumbsItems;
    ListView<BreadcrumbsItem> breadcrumbsListView;

    public Breadcrumbs(String id, List<BreadcrumbsItem> breadcrumbsItems) {
        super(id);
        this.breadcrumbsItems = breadcrumbsItems;
        createComponents(breadcrumbsItems);
    }

    private void createComponents(List<BreadcrumbsItem> breadcrumbsItems) {

        breadcrumbsListView = new ListView<BreadcrumbsItem>("breadcrumbsListView", breadcrumbsItems) {

            private static final long serialVersionUID = 1916336119332146890L;

            protected void populateItem(ListItem<BreadcrumbsItem> item) {

                BreadcrumbsItem breadcrumbsItem = item.getModelObject();

                BookmarkablePageLink link = new BookmarkablePageLink("breadcrumbsLink", breadcrumbsItem.getTargetPageClass(), breadcrumbsItem.getPageParameters());

                Label label;
                if (breadcrumbsItem.getName() != null)
                    label = new Label("breadcrumbsLinkLabel", getString(breadcrumbsItem.getTargetPageLabel())+" : "+breadcrumbsItem.getName());
                 else
                    label = new Label("breadcrumbsLinkLabel", getString(breadcrumbsItem.getTargetPageLabel()));

                if (breadcrumbsItem.isLastItem()) {
                    link.add(new AttributeModifier("class", "here"));
                    item.add(new Label("breadcrumbsSeparator", ""));
                } else {
                    link.add(new AttributeModifier("class", "way"));
                    item.add(new Label("breadcrumbsSeparator", " "+getString("portal.design.breadcrumbs.separator")+" "));
                }
                link.add(label);
                item.add(link);
            }
        };

        add(breadcrumbsListView);

    }

    @Override
    public void onEvent(IEvent<?> event) {
        super.onEvent(event);
        if (event.getPayload() instanceof BreadcrumbsUpdateEvent) {
            BreadcrumbsUpdateEvent breadcrumbsUpdateEvent = (BreadcrumbsUpdateEvent) event.getPayload();
            breadcrumbsItems = breadcrumbsUpdateEvent.getValue();
            breadcrumbsUpdateEvent.getTarget().add(this);
        }
    }
}
