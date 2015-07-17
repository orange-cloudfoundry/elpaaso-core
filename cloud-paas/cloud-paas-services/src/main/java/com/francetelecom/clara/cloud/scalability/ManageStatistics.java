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
package com.francetelecom.clara.cloud.scalability;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.ResourceNotFoundException;
import com.francetelecom.clara.cloud.scalability.helper.PaasStats;

/**
 * ManageStatistics
 * Class ...
 * Sample usage :
 * Last update  : $
 *
 * @author : $
 * @version : §
 */
public interface ManageStatistics {
    /**
     * is hibernate statistics enable
     * @return
     */
    boolean isStatEnable();

    /**
     * activate/deactivate hibernate statistics
     * @param enable
     */
    void setStatsState(boolean enable);

    /**
     * start a new hibernate stat snapshot
     * @param snapShotName
     * @return snapshotId
     */
    long startSnapshot(String snapShotName);

    /**
     * stop the snapshot identified by snapshotId
     * @param snapshotId
     */
    PaasStats endSnapShot(long snapshotId) throws BusinessException;
}
