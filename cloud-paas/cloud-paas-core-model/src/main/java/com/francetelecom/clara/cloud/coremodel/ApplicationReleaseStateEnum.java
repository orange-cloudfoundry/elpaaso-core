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
package com.francetelecom.clara.cloud.coremodel;

/**
 * Describes the states on ApplicationRelease.
 */
public enum ApplicationReleaseStateEnum {

    /**
     * The release is being edited (its logical model is being refined). This is the default state.
     */
    EDITING,

    /**
     * The release was been edited and is in a consistent state (i.e. consistency controls were applied).
     * It may still go back to EDITING if needed.
     */
    VALIDATED,

    /**
     * The application release is locked and has thus can not be modified. This ensures
     * that the environments that host this release are consistent (e.g. a release validated
     * on a preproduction environment can safely be deployed in production without encountering
     * changes to its logical model or projection rules)
     */
    LOCKED,

    /**
     * This application release was discarded and replaced by a modified one. This may be because
     * it had a major defect, or this was a transient work-in-progress release. Entering this
     * state allows to free associated resources (environments along with associated resources such
     * as disk images).
     * Note the clean up of DISCARDED release is a planned to be global process (similar to a GC,
     * with pluggeable policies) and not triggered/managed by users.
     */
    DISCARDED,

    /** This application release has been removed, it must not appears in portal */
    REMOVED,
}
