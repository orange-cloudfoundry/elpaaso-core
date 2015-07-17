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
package com.francetelecom.clara.cloud.scalability.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.SessionFactory;
import org.hibernate.stat.EntityStatistics;
import org.hibernate.stat.Statistics;
import org.slf4j.LoggerFactory;

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.ResourceNotFoundException;
import com.francetelecom.clara.cloud.model.TechnicalDeployment;
import com.francetelecom.clara.cloud.model.TechnicalDeploymentInstance;
import com.francetelecom.clara.cloud.scalability.ManageStatistics;
import com.francetelecom.clara.cloud.scalability.helper.PaasStats;
import com.francetelecom.clara.cloud.scalability.helper.StatisticsHelper;

/**
 * ManageStatisticsImpl
 * Class ...
 * Sample usage :
 * Last update  : $
 *
 * @author : $
 * @version : §
 */
public class ManageStatisticsImpl implements ManageStatistics {
    /**
     * Logger
     */
    private static final transient org.slf4j.Logger logger
       = LoggerFactory.getLogger(StatisticsHelper.class);

    Collection<Class> entitiesToStat;

    private Map<Long, PaasStats> paasStats = new HashMap<Long, PaasStats>();

    private boolean lastHibStatsState = false;

    private static int snapInProgress = 0;

    private SessionFactory sessionFactory;

    private Statistics getStats() {
        return sessionFactory != null ? sessionFactory.getStatistics() : null;
    }
    private void initEntitiesToStat() {
        // set the entities to stat on
        entitiesToStat = new ArrayList<Class>();
        entitiesToStat.add(TechnicalDeployment.class);
        entitiesToStat.add(TechnicalDeploymentInstance.class);
    }
    public ManageStatisticsImpl() {
        initEntitiesToStat();
        lastHibStatsState = false;
    }

    public  ManageStatisticsImpl(String statisticsEnabled, SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        lastHibStatsState = ("true".equals(statisticsEnabled));
        initEntitiesToStat();
        setStatsState(this.lastHibStatsState);
    }

    protected void storeSnap(PaasStats stats) {
        paasStats.put(stats.getCreationTime(), stats);
    }

    /**
     * we don't keep the retrieved snaps
     * @param snapId
     * @return
     */
    protected PaasStats retrieveSnap(long snapId) {
        PaasStats ps = paasStats.get(snapId);
        if (ps != null) {
            paasStats.remove(snapId);
        }
        return ps;
    }

    @Override
    public boolean isStatEnable() {
        return lastHibStatsState;
    }

    @Override
    public void setStatsState(boolean isStatsEnabled) {
        lastHibStatsState = isStatsEnabled;
        getStats().setStatisticsEnabled(isStatsEnabled);
        logger.info("statistics is {} enabled", (isStatsEnabled?"":"not"));
    }

    @Override
    public long startSnapshot(String snapShotName) {
        snapInProgress++;
        if (snapInProgress > 1) {
            logger.warn("{} snapshots in progress ", snapInProgress);
        }
        PaasStats pStats = new PaasStats(snapShotName, getStatsValues(entitiesToStat));
        storeSnap(pStats);
        long snapshotId = pStats.getCreationTime();
        logger.info("startSnapshot '{}', id={}", snapShotName, snapshotId);
        return snapshotId;
    }

    @Override
    public PaasStats endSnapShot(long snapshotId) throws BusinessException {
        logger.info("endSnapShot id={}", snapshotId);
        snapInProgress--;
        PaasStats pStats = retrieveSnap(snapshotId);
        if (pStats == null) {
            throw new ResourceNotFoundException("unable to retrieve stats snapshot");
        }
        pStats.setEndStats(getStatsValues(entitiesToStat));
        // logStats(stats);
        return pStats;
    }

    private Map<String, Long> getStatsValues(Collection<Class> entities) {
        Statistics stats = getStats();
        Map<String, Long> statistics = new HashMap<String, Long>();
        statistics.put("Number of connection requests", stats.getConnectCount());
        statistics.put("Sessions opened", stats.getSessionOpenCount());
        // statistics.put("Sessions closed", stats.getSessionCloseCount());
        statistics.put("Transactions", stats.getTransactionCount());
        // statistics.put("Successful transactions", stats.getSuccessfulTransactionCount());
        // statistics.put("Successful transactions", stats.getSuccessfulTransactionCount());
        statistics.put("Queries executed", stats.getQueryExecutionCount());
        for(Class entity : entities) {
            EntityStatistics eStats = stats.getEntityStatistics(entity.getName());
            statistics.put(entity.getSimpleName() + " Fetched", eStats.getFetchCount());
            statistics.put(entity.getSimpleName() + " Loaded", eStats.getLoadCount());
            statistics.put(entity.getSimpleName() + " Inserted", eStats.getInsertCount());
            statistics.put(entity.getSimpleName() + " Deleted", eStats.getDeleteCount());
            statistics.put(entity.getSimpleName() + " Updated", eStats.getUpdateCount());
        }
        return statistics;
    }
}
