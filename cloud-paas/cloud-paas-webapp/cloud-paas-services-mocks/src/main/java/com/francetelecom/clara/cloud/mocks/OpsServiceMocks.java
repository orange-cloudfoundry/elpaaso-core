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

import com.francetelecom.clara.cloud.commons.DateHelper;
import com.francetelecom.clara.cloud.coremodel.Environment;
import com.francetelecom.clara.cloud.service.OpsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.List;

@Service("opsService")
public class OpsServiceMocks implements OpsService {
    private static final Logger log = LoggerFactory.getLogger(OpsServiceMocks.class);

    @Autowired(required = true)
    private ManageEnvironmentMock manageEnvironmentMock;

    @Autowired(required = true)
    private ManageApplicationReleaseMock manageReleaseMock;

    @Autowired(required = true)
    private ManageApplicationMock manageApplicationMock;

    /**
     * should correspond to maven.build.timestamp.format
     */
    @Override
    public String getServerDate() {
        return "07/06/2013-10:41";
    }

    @Override
    public String getBuildUser() {
        return "don gorgio";
    }

    @Override
    public String getBuildDate() {
        return "07/06/2010-12:86"; // hey, what did you expect ? ;)
    }

    @Override
    public String getBuildVersion() {
        return "1.2.3-SNAP";
    }

    @Override
    public void purgeDatabase() {
        String action = "purge database : old environments";
        try {
            purgeEnvironments();
            action = "purge database : old releases";
            manageReleaseMock.purgeOldRemovedReleases();
            action = "purge database : old applications";
            manageApplicationMock.purgeOldRemovedApplications();
        } catch (Throwable throwable) {
            log.error("Exception while {} : {}", action, throwable.getMessage());
            log.error("Purge exception:", throwable);
            throwable.printStackTrace();
        }
    }

    private void purgeEnvironments() {
        List<Environment> oldRemovedEnvironments = manageEnvironmentMock.findOldRemovedEnvironments();
        for(Environment e : oldRemovedEnvironments) {
            try {
                manageEnvironmentMock.purgeRemovedEnvironment(e.getUID());
            } catch (Throwable e1) {
                log.error("Unable to purge environment {}", e.toString());
                e1.printStackTrace();
            }
        }
    }
}
