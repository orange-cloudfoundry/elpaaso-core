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

import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.markup.html.form.IChoiceRenderer;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 02/03/12
 */
public class CompoundChoiceRenderer<T> implements IChoiceRenderer<T> {

    String[] displayExpressions;
    String idExpression;
    String separator;

    public CompoundChoiceRenderer(String[] displayExpressions, String idExpression, String separator) {
        super();
        this.displayExpressions = displayExpressions;
        this.idExpression = idExpression;
        this.separator = separator;
    }
    
    @Override
    public Object getDisplayValue(Object object) {
        String returnValue = "";
        if ((displayExpressions != null) && (object != null)) {

            for (int i = 0; i < displayExpressions.length; i++) {
                if (i != 0) {
                    returnValue += separator;

                }
                returnValue += PropertyResolver.getValue(displayExpressions[i], object);

            }

        }

        if (returnValue == null) {
            return "";
        }

        return returnValue;
    }

    @Override
    public String getIdValue(Object object, int index) {
        if (idExpression == null)
        {
            return Integer.toString(index);
        }

        if (object == null)
        {
            return "";
        }

        Object returnValue = PropertyResolver.getValue(idExpression, object);
        if (returnValue == null)
        {
            return "";
        }

        return returnValue.toString();
    }
    
}
