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
package com.francetelecom.clara.cloud.service.scheduled;

import com.francetelecom.clara.cloud.service.OpsService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Service;

/**
 * ScheduledDatabasePurgeMonitor
 */
@Service
public class ScheduledDatabasePurgeMonitor extends QuartzJobBean implements StatefulJob, ApplicationContextAware {
    private final static Logger logger = LoggerFactory.getLogger(ScheduledDatabasePurgeMonitor.class.getName());
    private boolean welcomeDisplayed = false;

    @Autowired
    private OpsService opsService;

    private String purgeCronExpression;
    private Boolean enabled;


    private void welcomeMessage() {
        String welcomeMsg = "Scheduled database purge monitor *disabled*";
        if (getEnabled().booleanValue()) {
            welcomeMsg = "Scheduled database purge monitor *enabled* [cron expression:" + getPurgeCronExpression() + "]";
        }
        logger.info(welcomeMsg);
        welcomeDisplayed = true;
    }

    public void purgeDatabase() {
        logger.info("purgeDatabase");
        opsService.purgeDatabase();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (applicationContext != null && !welcomeDisplayed) {
            welcomeMessage();
        }
    }

    public void setPurgeCronExpression(String purgeCronExpression) {
        this.purgeCronExpression = purgeCronExpression;
    }

    public String getPurgeCronExpression() {
        return purgeCronExpression;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setOpsService(OpsService opsService) {
        this.opsService = opsService;
    }

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        purgeDatabase();
    }
}
