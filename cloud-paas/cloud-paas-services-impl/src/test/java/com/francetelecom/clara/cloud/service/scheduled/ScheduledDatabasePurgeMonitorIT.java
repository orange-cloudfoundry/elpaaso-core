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

import com.francetelecom.clara.cloud.core.service.ManageEnvironment;
import com.francetelecom.clara.cloud.core.service.exception.ObjectNotFoundException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.reset;

/**
 * ScheduledDatabasePurgeMonitorIT IT Test of the database purge monitoring
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Ignore("No runnable method after commit f4c41cc3348fa54c8de178b660cd8a75469131f9")
public class ScheduledDatabasePurgeMonitorIT {
    private final static Logger logger = LoggerFactory.getLogger(ScheduledDatabasePurgeMonitorIT.class.getName());
    /**
     * this should match the paas-scheduled-database-purge.properties (test)
     */
    public static final int PAAS_PURGE_SCHEDULER_CRON_PERIOD_IN_SECOND = 1;

    @Autowired
    ScheduledDatabasePurgeMonitor scheduledDatabasePurgeMonitor;

    @Autowired
    private String purgeRepeatIntervalCron;

    @Autowired
    ManageEnvironment manageEnvironment;

    
//    @Autowired
//    CronTriggerFactoryBean databasePurgeTrigger;


    @Before
    public void setup() throws ObjectNotFoundException {
        mockManageEnvironment();

    }

    private void mockManageEnvironment() throws ObjectNotFoundException {
        reset(manageEnvironment);
        // avoid the database purge here
        doNothing().when(manageEnvironment).purgeRemovedEnvironment(anyString());
    }

    /**
	 * GIVEN PaaS ScheduledDatabasePurgeMonitor implementation WHEN scheduler
	 * trigger THEN purge service is called
	 * 
     */
//	@Test
//	public void database_purge_monitor_should_be_periodically_invoked() throws ObjectNotFoundException {
//        // WHEN
//        waitForAScheduledTrigger();
//        // THEN
//        mockManageEnvironment();
//        logger.info("verify quartz cron call of purgeDatabase / configuration: {}", getPurgeRepeatIntervalCron());
//        int callNumber = 0;
//        for (int i=0; i<2 ; i++) {
//            logger.info("verify next call ({}) of purgeDatabase", callNumber);
//            verify(manageEnvironment, times(callNumber)).findOldRemovedEnvironments();
//            waitForNextScheduledUpdate();callNumber++;
//        }
//	}

//    private void waitForAScheduledTrigger() {
//        int maxWaitSec = 10;
//        // get the next scheduled trigger
//        
//
//        
//        Date startTime = databasePurgeTrigger.getStartTime();
//        
//        
//        Assume.assumeNotNull(startTime);
//        if (startTime.compareTo(new Date()) != 1) {
//            logger.info("waitForAScheduledTrigger OK (trigger already started)");
//            return;
//        }
//        logger.info("waitForAScheduledTrigger startTime={}", DateHelper.getDateLogFormat(startTime));
//        Date nowPlusMaxWaitSec = DateHelper.getDateDeltaSec(maxWaitSec);
//        Assume.assumeTrue(startTime.compareTo(nowPlusMaxWaitSec) == -1);
//        while(startTime.compareTo(new Date()) == 1) {
//            littleWait();
//        }
//        logger.info("waitForAScheduledTrigger DONE");
//    }

    private void littleWait() {
        try {
            long waitMs = 100;
            logger.debug("wait for {} ms", waitMs);
            Thread.sleep(waitMs);
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException oO");
        }
    }

    private void waitForNextScheduledUpdate() {
        try {
            long waitSec = PAAS_PURGE_SCHEDULER_CRON_PERIOD_IN_SECOND;
            logger.debug("wait for {} seconds", waitSec);
            Thread.sleep(waitSec*1000);
        } catch (InterruptedException e) {
            Assert.fail("InterruptedException oO");
        }
    }

    public ScheduledDatabasePurgeMonitor getScheduledDatabasePurgeMonitor() {
        return scheduledDatabasePurgeMonitor;
    }

    public void setScheduledDatabasePurgeMonitor(ScheduledDatabasePurgeMonitor scheduledDatabasePurgeMonitor) {
        this.scheduledDatabasePurgeMonitor = scheduledDatabasePurgeMonitor;
    }

    public String getPurgeRepeatIntervalCron() {
        return purgeRepeatIntervalCron;
    }

    public void setPurgeRepeatIntervalCron(String purgeRepeatIntervalCron) {
        this.purgeRepeatIntervalCron = purgeRepeatIntervalCron;
    }
}

