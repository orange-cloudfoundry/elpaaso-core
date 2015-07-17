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
 * Describes the externally visible status of an environment while hidding internals when irrelevant.
 *
 * TODO: rename into EnvironmentStatusEnum for naming consistency
 */
public enum EnvironmentStatus {

    /* Initial state. The environment creation is in progress */
    CREATING,

    /* The environment is starting / initializing OS, networks, communication to services */
    STARTING,

    /* The environment is fully working
     *
     * The environment is started and ready to be used. This may be the case either following its initial creation,
     * or following a stop/start cycle.
     *
     * TODO: consider if refinements with a new substate for the case of an environment being partially started, such as DBaaS powered down
     * while the other XaaS (e.g. IaaS, store) are properly up.
     */
    RUNNING,

    /** The environment is stopping **/
    STOPPING,

    /**
     * The environment was properly created and is current stopped and can not be currently used without being started.
     * This is typically the case when trying to save power and stopping environment during inactivity periods
     * (especially non-production environments).
     */
    STOPPED,

    /** The environment is being removed. All vm's, network connections and services will be removed */
    REMOVING,

    /** The environment and all vm's, network connections and services have been removed.
     * Final state. */
    REMOVED,

    /**
     * The creation of the environment failed hard. The environment may be kept in this state to perform manual diagnosis
     * by the PaaS team. Usually, the environment would not be repaired, but may be removed. This is not a transient
     * error (such as DBaaS powered down and the environment is in a corrupted state)
     */
    FAILED,

    /** the environment is in unknown state. Seems to map Activation UNKNOWN state.
     * TODO: should it be instead of synonym for FAILED ? */
    UNKNOWN,


}
