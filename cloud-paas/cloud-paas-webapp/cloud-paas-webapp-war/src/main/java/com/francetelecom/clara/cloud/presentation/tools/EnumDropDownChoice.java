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

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.IModel;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Thomas Escalle - tawe8231
 * Entity : FT/OLNC/RD/MAPS/MEP/MSE
 * Date: 19/10/11
 */
public class EnumDropDownChoice<T extends Enum<T>> extends DropDownChoice<T> {

    private static final long serialVersionUID = -7459873310353555292L;

    public EnumDropDownChoice(String id, IModel<T> model) {
        super(id);
        setModel(model);
        setChoiceRenderer(new EnumChoiceRenderer<T>(this));
    }

    public EnumDropDownChoice(String id, IModel<T> model, EnumChoiceRenderer<T> choiceRenderer) {
        super(id);
        setModel(model);
        setChoiceRenderer(choiceRenderer);

    }

    @Override
    public List<? extends T> getChoices() {
        return Arrays.asList(getModelObject().getDeclaringClass().getEnumConstants());
    }

}
