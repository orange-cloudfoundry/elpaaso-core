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
package com.francetelecom.clara.cloud.commons;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation specifies how the elements of the logical model should be mapped in the current GUI (the web-based portal).
 *
 * This is currently used as a way to document things rather than a way to be processed automatically.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface GuiMapping {

    /**
     * Describes whether the field should be mapped
     */
    public StatusType status() default  StatusType.SUPPORTED;

    /**
     * Describes whether it is functional or non functional (i.e. SLO)
     */
    public boolean functional() default true;

   static enum StatusType {
       /** Not applicable: this field supports technical processes and should not be initiated with user-provided input */
       NA,

       /** skip for now: i.e. do not display this field in the UI (for various reasons: requires rework,
        not yet supported in underlying engine...) althrough it would eventually be mapped in the future */
       SKIPPED,

       /** display in the UI as greyed out with the default value of the field. */
       READ_ONLY,

       /** display enabled: display and apply input validation associated with jax validations */
       SUPPORTED,

   }


}
