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
package com.francetelecom.clara.cloud.mocks;

import org.springframework.stereotype.Service;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.scalability.ManageStatistics;
import com.francetelecom.clara.cloud.scalability.helper.PaasStats;

/**
 * ManageStatisticsImpl
 * Class ...
 * Sample usage :
 * Last update  : $
 *
 * @author : $
 * @version : §
 */
@Service("manageStatistics")
public class ManageStatisticsMock implements ManageStatistics {
    @Override
    public boolean isStatEnable() {
        return false;
    }

    @Override
    public void setStatsState(boolean enable) {
        return;
    }

    @Override
    public long startSnapshot(String snapShotName) {
        return -1;
    }

    @Override
    public PaasStats endSnapShot(long snapshotId) throws BusinessException {
        return null;
    }
}
