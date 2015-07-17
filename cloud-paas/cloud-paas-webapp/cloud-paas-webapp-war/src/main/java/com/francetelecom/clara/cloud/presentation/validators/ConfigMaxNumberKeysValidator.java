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

import java.util.Map;

import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.AbstractValidator;

import com.francetelecom.clara.cloud.logicalmodel.InvalidConfigServiceException;
import com.francetelecom.clara.cloud.logicalmodel.LogicalConfigServiceUtils;
import com.francetelecom.clara.cloud.logicalmodel.ProcessingNode;

/**
 * ConfigMaxNumberKeysValidator
 *
 * Last updated : $LastChangedDate: 2012-06-11 17:23:44 +0200 (lun., 11 juin 2012) $
 * Last author  : $Author: dwvd1206 $
 * @version     : $Revision: 17582 $
 */
public class ConfigMaxNumberKeysValidator extends AbstractValidator<String> {
    public static final String ERROR_MESSAGE_KEY = "ConfigMaxNumberKeysValidator";
    private static int maxNumberKeys = ProcessingNode.MAX_CONFIG_SET_ENTRIES_PER_EXEC_NODE;
    private static LogicalConfigServiceUtils utils = new LogicalConfigServiceUtils();
    private int numberKeys;

    @Override
    protected void onValidate(IValidatable<String> iValidatable) {
        String content = iValidatable.getValue();
        try {
            LogicalConfigServiceUtils.StructuredLogicalConfigServiceContent structuredLogicalConfigServiceContent = utils.parseConfigContent(content);
            numberKeys = structuredLogicalConfigServiceContent.getConfigEntries().size();
            if(numberKeys > maxNumberKeys) {
                error(iValidatable);
            }
        } catch (InvalidConfigServiceException e) {
            /* This validator does not care about duplicate keys */
        }
    }

    @Override
    protected Map<String, Object> variablesMap(IValidatable<String> iValidatable) {
        Map<String, Object> map = super.variablesMap(iValidatable);
        map.put("0", numberKeys);
        map.put("1", maxNumberKeys);
        return map;
    }

    @Override
    protected String resourceKey() {
        return ERROR_MESSAGE_KEY;
    }
}
