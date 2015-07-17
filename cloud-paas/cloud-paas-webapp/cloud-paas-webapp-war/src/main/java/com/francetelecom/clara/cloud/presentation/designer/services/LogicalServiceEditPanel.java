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
package com.francetelecom.clara.cloud.presentation.designer.services;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import com.francetelecom.clara.cloud.logicalmodel.LogicalModelItem;

import org.apache.wicket.Page;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogicalServiceEditPanel {

    private static final Logger logger = LoggerFactory.getLogger(LogicalServiceEditPanel.class.getName());

    private Class<LogicalServiceBasePanel<LogicalModelItem>> panelClass;

    private Class<LogicalModelItem> modelItemClass;

    public LogicalServiceEditPanel(Class<LogicalModelItem> modelClass, Class<LogicalServiceBasePanel<LogicalModelItem>> panelClass) {
        this.modelItemClass = modelClass;
        this.panelClass = panelClass;
    }

    public LogicalServiceBasePanel<LogicalModelItem> getPanel(String id, LogicalModelItem logicalModelItem, Page parentPage,
            boolean isNew, boolean readOnly, boolean configOverride) {

        if (modelItemClass.isInstance(logicalModelItem)) {

            try {
                Constructor<LogicalServiceBasePanel<LogicalModelItem>> panelConstructor = panelClass.getConstructor(String.class, IModel.class, Page.class, boolean.class, boolean.class, boolean.class);
                IModel<LogicalModelItem> model;
                if (isNew) {
                    model = new CompoundPropertyModel<>(modelItemClass.newInstance());
                } else {
                    model = new CompoundPropertyModel<>(logicalModelItem);
                }
                return panelConstructor.newInstance(id, model, parentPage, isNew, readOnly, configOverride);

            } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                logger.error("Error instantiating panel for logical model item " + modelItemClass.getSimpleName(), e);
                return null;
            }
        } else {
            return null;
        }
    }

}
