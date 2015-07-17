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
package com.francetelecom.clara.cloud.presentation.tools;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 07/07/11
 */
public class BreadcrumbsItem implements Serializable {

    private static final long serialVersionUID = 1054094421065347353L;

    private Class targetPageClass;
    private String targetPageLabel;
    private boolean lastItem;
    private PageParameters params;
    private String name;

    public BreadcrumbsItem(Class targetPageClass, String targetPageLabel, String name,  boolean lastItem) {
        this.targetPageClass = targetPageClass;
        this.targetPageLabel = targetPageLabel;
        this.name = name;
        this.lastItem = lastItem;
    }

    public BreadcrumbsItem(Class targetPageClass, PageParameters params, String targetPageLabel, String name, boolean lastItem) {
        this.targetPageClass = targetPageClass;
        this.params = params;
        this.targetPageLabel = targetPageLabel;
        this.name = name;
        this.lastItem = lastItem;
    }

    public Class getTargetPageClass() {
        return targetPageClass;
    }

    public void setTargetPageClass(Class targetPageClass) {
        this.targetPageClass = targetPageClass;
    }

    public PageParameters getPageParameters() {
        return params;
    }

    public void setPageParameters(PageParameters params) {
        this.params = params;
    }

    public String getTargetPageLabel() {
            return targetPageLabel;
    }

    public void setTargetPageLabel(String targetPageLabel) {
        this.targetPageLabel = targetPageLabel;
    }

    public boolean isLastItem() {
        return lastItem;
    }

    public void setLastItem(boolean lastItem) {
        this.lastItem = lastItem;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
