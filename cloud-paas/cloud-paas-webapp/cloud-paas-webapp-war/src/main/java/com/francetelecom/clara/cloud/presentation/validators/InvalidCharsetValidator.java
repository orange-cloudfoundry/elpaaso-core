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
package com.francetelecom.clara.cloud.presentation.validators;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: wwnl9733
 * Date: 03/01/12
 * Time: 10:45
 * To change this template use File | Settings | File Templates.
 */
public class InvalidCharsetValidator extends AbstractValidator<String> {
    private List<Character> invalidCharacters = new ArrayList<Character>();

    @Override
    protected void onValidate(IValidatable<String> iValidatable) {
        String content = iValidatable.getValue();
        for(int i=0; i<content.length(); i++) {
            if(!Character.isDefined(content.codePointAt(i))) {
                invalidCharacters.add(content.charAt(i));
            }
        }
        if(invalidCharacters.size() > 0) {
            error(iValidatable);
        }
    }

    @Override
    protected String resourceKey() {
        return "InvalidCharsetValidator";
    }

    @Override
    protected Map<String, Object> variablesMap(IValidatable<String> iValidatable) {
        Map<String, Object> map = super.variablesMap(iValidatable);
        map.put("invalidCharacters", invalidCharacters);
        return map;
    }
}
