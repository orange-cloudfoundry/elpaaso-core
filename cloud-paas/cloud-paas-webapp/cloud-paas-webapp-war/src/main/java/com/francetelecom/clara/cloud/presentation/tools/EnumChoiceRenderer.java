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

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.form.IChoiceRenderer;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 19/10/11
 */

public class EnumChoiceRenderer<T extends Enum<T>> implements IChoiceRenderer<T> {

    private static final long serialVersionUID = 1099689745481722182L;

    private final Component resourceProvider;

    public EnumChoiceRenderer(final Component resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    @Override
    public Object getDisplayValue(final T value) {
        // TODO : validate with FUT team and Product Owner that "Upper" letters are authorized
        final String key = value.getDeclaringClass().getCanonicalName()+ "." +value.toString();
        return resourceProvider.getString(key);
    }

    @Override
    public String getIdValue(T object, int index) {
        final Enum<?> enumValue = object;
        return enumValue.name();
    }
}
