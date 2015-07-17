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
 * This annotation specifies how the Classes of the logical model should be mapped in the current UI (the web-based portal).
 *
 * This is currently used both as a way to document and a programmatic hint to the PaaS UI on how to layout elements
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface GuiClassMapping {

    /**
     * Describes whether the service is external (i.e. allows external access to the applications)
     */
    public boolean isExternal() default true;

    /**
     * Describes the level of support of the service in the current paas implementation
     * @return
     */
    public StatusType status() default StatusType.SUPPORTED;

    /**
     * Describe if the service parameter must be enable or disable
     * @author return true if service paramter is enable
     */
    public boolean isFunctional() default false;

    /**
     * Maps the current class to the name used by end-users, and published into the service catalog
     * @return an optional name. In the case of {@link StatusType#SKIPPED} this may be not mentionned.
     */
    public String serviceCatalogName() default "Unnamed (probably a SKIPPED service) ";

    /**
     * Maps the current class to the key used to define name display to end-users
     */
    public String serviceCatalogNameKey() default "noname";


    static enum StatusType {
        /** Not applicable: this class supports technical processes and should not be initiated with user-provided input */
        NA,

        /** skip for now: i.e. do not display this service in the UI (for various reasons: requires rework,
         not yet supported in underlying engine...) althrough it would eventually be mapped in the future */
        SKIPPED,

        /** display in the UI as beta e.g. "QSS (beta)" to indicate the user that the service is not yet stable */
        BETA,


        /** display in the UI as a greyed out preview: i.e. only within the list of services but not the service details */
        PREVIEW,

        /** display enabled: display and apply input validation. The logical model is ready for being used in the WebGui.
         * WebGui may choose to not display it if GUI code is not yet ready */
        SUPPORTED,

    }

}
