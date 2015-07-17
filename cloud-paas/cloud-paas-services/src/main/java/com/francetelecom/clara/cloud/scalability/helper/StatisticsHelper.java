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
package com.francetelecom.clara.cloud.scalability.helper;

import org.hibernate.stat.*;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * StatisticsHelper
 * Class ...
 * Sample usage :
 * Last update  : $
 *
 * @author : $
 * @version : §
 */
public class StatisticsHelper {
    /**
     * Logger
     */
    private static final transient org.slf4j.Logger logger
            = LoggerFactory.getLogger(StatisticsHelper.class);

    /**
     * Log the current statistics
     *
     * @param stats hibernate statistics
     */
    public static void logStats(Statistics stats) {

        logger.info("Database statistics");

        logger.info("  Number of connection requests : " + stats.getConnectCount());
        logger.info("  Session flushes : " + stats.getFlushCount());
        logger.info("  Transactions : " + stats.getTransactionCount());
        logger.info("  Successful transactions : " + stats.getSuccessfulTransactionCount());
        logger.info("  Sessions opened : " + stats.getSessionOpenCount());
        logger.info("  Sessions closed : " + stats.getSessionCloseCount());
        logger.info("  Queries executed : " + stats.getQueryExecutionCount());
        logger.info("  Max query time : " + stats.getQueryExecutionMaxTime());
        logger.info("  Max time query : " + stats.getQueryExecutionMaxTimeQueryString());

        logger.info("Collection statistics");

        logger.info("  Collections fetched : " + stats.getCollectionFetchCount());
        logger.info("  Collections loaded : " + stats.getCollectionLoadCount());
        logger.info("  Collections rebuilt : " + stats.getCollectionRecreateCount());
        logger.info("  Collections batch deleted : " + stats.getCollectionRemoveCount());
        logger.info("  Collections batch updated : " + stats.getCollectionUpdateCount());

        logger.info("Object statistics");

        logger.info("  Objects fetched : " + stats.getEntityFetchCount());
        logger.info("  Objects loaded : " + stats.getEntityLoadCount());
        logger.info("  Objects inserted : " + stats.getEntityInsertCount());
        logger.info("  Objects deleted : " + stats.getEntityDeleteCount());
        logger.info("  Objects updated : " + stats.getEntityUpdateCount());

        logger.info("Cache statistics");

        double chit = stats.getQueryCacheHitCount();
        double cmiss = stats.getQueryCacheMissCount();

        logger.info("  Cache hit count : " + chit);
        logger.info("  Cache miss count : " + cmiss);
        logger.info("  Cache hit ratio : " + (chit / (chit + cmiss)));

        String[] entityNames = stats.getEntityNames();
        Arrays.sort(entityNames);
        for (String entityName : entityNames) {
            Class<?> entityClass = null;
            try {
                entityClass = Class.forName(entityName);
            } catch (ClassNotFoundException e) {
                logger.error("Unable to load class for " + entityName, e);
            }
            entityStats(stats, entityClass);
        }
        //Uncomment these lines to trace every query (can generate a lot of logs)
        String[] qs = stats.getQueries();
        for (String q : qs) {
            queryStats(stats, q);
        }

        String[] slcrn = stats.getSecondLevelCacheRegionNames();
        for (String s : slcrn) {
            secondLevelStats(stats, s);
        }
    }

    private static void entityStats(Statistics stats, Class cl) {
        String name = cl.getName();


        EntityStatistics eStats = stats.getEntityStatistics(name);

        String fetched = "";
        if (eStats.getFetchCount() > 0) {
            fetched = " Fetched=" + eStats.getFetchCount();
        }
        String loaded = "";
        if (eStats.getLoadCount() > 0) {
            loaded = " Loaded=" + eStats.getLoadCount();
        }
        String inserted = "";
        if (eStats.getInsertCount() > 0) {
            inserted = " Inserted= " + eStats.getInsertCount();
        }
        String deleted = "";
        if (eStats.getDeleteCount() > 0) {
            deleted = " Deleted= " + eStats.getDeleteCount();
        }
        String updated = "";
        if (eStats.getUpdateCount() > 0) {
            updated = " Updated= " + eStats.getUpdateCount();
        }
        String allStats = loaded + fetched + inserted + deleted + updated;
        if (!allStats.isEmpty()) {
            logger.info("Statistics for " + name + allStats);
        }

    }

    private static void collectionStats(Statistics stats, Class cl, String cname) {
        String name = cl.getName() + "." + cname;

        logger.info("Statistics for " + name);

        CollectionStatistics cStats = stats.getCollectionStatistics(name);

        logger.info("  Fetched : " + cStats.getFetchCount());
        logger.info("  Loaded : " + cStats.getLoadCount());
        logger.info("  Recreated : " + cStats.getRecreateCount());
        logger.info("  Removed : " + cStats.getRemoveCount());
        logger.info("  Updated : " + cStats.getUpdateCount());
    }

    private static void queryStats(Statistics stats, String q) {
        logger.info("Query statistics for " + q);

        QueryStatistics qStats = stats.getQueryStatistics(q);

        logger.info("  Execution ct : " + qStats.getExecutionCount());
        logger.info("  Cache hits : " + qStats.getCacheHitCount());
        logger.info("  Cache puts : " + qStats.getCachePutCount());
        logger.info("  Cache misses : " + qStats.getCacheMissCount());
        logger.info("  Execution row ct : " + qStats.getExecutionRowCount());
        logger.info("  Execution avg millis : " + qStats.getExecutionAvgTime());
        logger.info("  Execution max millis : " + qStats.getExecutionMaxTime());
        logger.info("  Execution min millis : " + qStats.getExecutionMinTime());
    }

    private static void secondLevelStats(Statistics stats, String name) {
        logger.info("Second level statistics for " + name);

        SecondLevelCacheStatistics slStats = stats.getSecondLevelCacheStatistics(name);

        logger.info("  Elements in memory : " + slStats.getElementCountInMemory());
        logger.info("  Element on disk : " + slStats.getElementCountOnDisk());
        logger.info("  Entries : " + slStats.getEntries());
        logger.info("  Hit count : " + slStats.getHitCount());
        logger.info("  Miss count : " + slStats.getMissCount());
        logger.info("  Put count : " + slStats.getPutCount());
        logger.info("  Memory size : " + slStats.getSizeInMemory());
    }
}

