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

import org.apache.wicket.Component;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.StringResourceModel;

/**
 * WicketUtils
 * <p>
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 *
 * @version : $Revision$
 */
public class WicketUtils {

    /**
     * fix Localizer warning : cf. https://issues.apache.org/jira/browse/WICKET-990
     *  Tried to retrieve a localized string for a component that has not yet been added to the page.
     * @param key label property key
     * @return string resource model
     */
    public static StringResourceModel getStringResourceModel(Component component, java.lang.String key) {
        return new StringResourceModel(key, component, null);
    }

    public static StringResourceModel getStringResourceModel(Component component, java.lang.String key, Model model) {
        return new StringResourceModel(key, component, model);
    }
}
