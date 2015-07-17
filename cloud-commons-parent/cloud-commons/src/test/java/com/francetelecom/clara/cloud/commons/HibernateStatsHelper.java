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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.stat.Statistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created with IntelliJ IDEA.
 * User: shjn2064
 * Date: 11/09/12
 * Time: 16:30
 * To change this template use File | Settings | File Templates.
 */
public class HibernateStatsHelper {

    protected static Logger logger = LoggerFactory.getLogger(HibernateStatsHelper.class.getName());
    
 
    /**
     * check all statistics including durations
     * @param refs
     * @param duration total duration of test
     * @param stats
     */
    public static void checkStats(Map<HibernateStatsReferenceType, Long> refs, long duration, Statistics stats) throws ObjectNotFoundException, MalformedURLException {
    	checkStats(refs, duration, stats, true);
    }

    /**
     * check statistics ignoring durations<br>
     * this method shall be used when we don't want to fail a test due to test environment slow response time
     * note: durations may still be provided in refs or stats but are not verified<br>
     * 
     * @param refs
     * @param stats
     */

    public static void checkStatsIgnoringDuration(Map<HibernateStatsReferenceType, Long> refs, Statistics stats) throws ObjectNotFoundException, MalformedURLException {
    	checkStats(refs, 0, stats, false);
    }
    	 
    
    /**
     * Ensure no regression higher than 5%
     * @param assertDuration if true durations are verified, if false durations are not verified
     */
    private static void checkStats(Map<HibernateStatsReferenceType, Long> refs, long duration, Statistics stats, boolean assertDuration) throws ObjectNotFoundException, MalformedURLException {
        List<AssertionError> failedAsserts = new ArrayList<AssertionError>();


        // Reference values: these must be updated when you optimize your code
        // or if new values are explained and normal.
        final long DURATION = refs.get(HibernateStatsReferenceType.DURATION);
        final int QUERY_COUNT = refs.get(HibernateStatsReferenceType.QUERY_COUNT).intValue();
        final int QUERY_MAX_TIME_MS = refs.get(HibernateStatsReferenceType.QUERY_MAX_TIME_MS).intValue();

        final int ENTITY_FETCH_COUNT = refs.get(HibernateStatsReferenceType.ENTITY_FETCH_COUNT).intValue();
        final int ENTITY_LOAD_COUNT = refs.get(HibernateStatsReferenceType.ENTITY_LOAD_COUNT).intValue();
        final int ENTITY_INSERT_COUNT = refs.get(HibernateStatsReferenceType.ENTITY_INSERT_COUNT).intValue();
        final int ENTITY_DELETE_COUNT = refs.get(HibernateStatsReferenceType.ENTITY_DELETE_COUNT).intValue();
        final int ENTITY_UPDATE_COUNT = refs.get(HibernateStatsReferenceType.ENTITY_UPDATE_COUNT).intValue();

        final int COLLECTION_FETCH_COUNT = refs.get(HibernateStatsReferenceType.COLLECTION_FETCH_COUNT).intValue();
        final int COLLECTION_LOAD_COUNT = refs.get(HibernateStatsReferenceType.COLLECTION_LOAD_COUNT).intValue();
        final int COLLECTION_RECREATE_COUNT = refs.get(HibernateStatsReferenceType.COLLECTION_RECREATE_COUNT).intValue();
        final int COLLECTION_REMOVE_COUNT = refs.get(HibernateStatsReferenceType.COLLECTION_REMOVE_COUNT).intValue();
        final int COLLECTION_UPDATE_COUNT = refs.get(HibernateStatsReferenceType.COLLECTION_UPDATE_COUNT).intValue();


        // The number of completed transactions (failed and successful) must
        // match number of transactions completed without failure
        preAssertEquals("There are transaction failures", stats.getTransactionCount(), stats.getSuccessfulTransactionCount(), failedAsserts);

        // Total number of queries executed.
        preAssertTrue("Total number of queries executed increased more than 5% (ref=" + QUERY_COUNT + "): " + stats.getQueryExecutionCount(),
                stats.getQueryExecutionCount() <= (QUERY_COUNT * 1.05), failedAsserts);
        if (stats.getQueryExecutionCount() < (QUERY_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value QUERY_COUNT (ref=" + QUERY_COUNT + ") to " + stats.getQueryExecutionCount());

        preAssertTrue("ENTITY_DELETE_COUNT increased more than 5% (ref=" + ENTITY_DELETE_COUNT + "): " + stats.getEntityDeleteCount(),
                stats.getEntityDeleteCount() <= (ENTITY_DELETE_COUNT * 1.05), failedAsserts);
        if (stats.getEntityDeleteCount() < (ENTITY_DELETE_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value ENTITY_DELETE_COUNT (ref=" + ENTITY_DELETE_COUNT + ") to " + stats.getEntityDeleteCount());
        preAssertTrue("ENTITY_UPDATE_COUNT increased more than 5% (ref=" + ENTITY_UPDATE_COUNT + "): " + stats.getEntityUpdateCount(),
                stats.getEntityUpdateCount() <= (ENTITY_UPDATE_COUNT * 1.05), failedAsserts);
        if (stats.getEntityUpdateCount() < (ENTITY_UPDATE_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value ENTITY_UPDATE_COUNT (ref=" + ENTITY_UPDATE_COUNT + ") to " + stats.getEntityUpdateCount());

        if (stats.getCollectionRecreateCount() < (COLLECTION_RECREATE_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value COLLECTION_RECREATE_COUNT (ref=" + COLLECTION_RECREATE_COUNT + ") to "
                    + stats.getCollectionRecreateCount());
        preAssertTrue("COLLECTION_REMOVE_COUNT increased more than 5% (ref=" + COLLECTION_REMOVE_COUNT + "): " + stats.getCollectionRemoveCount(),
                stats.getCollectionRemoveCount() <= (COLLECTION_REMOVE_COUNT * 1.05), failedAsserts);
        if (stats.getCollectionRemoveCount() < (COLLECTION_REMOVE_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value COLLECTION_REMOVE_COUNT (ref=" + COLLECTION_REMOVE_COUNT + ") to "
                    + stats.getCollectionRemoveCount());
        preAssertTrue("COLLECTION_UPDATE_COUNT increased more than 5% (ref=" + COLLECTION_UPDATE_COUNT + "): " + stats.getCollectionUpdateCount(),
                stats.getCollectionUpdateCount() <= (COLLECTION_UPDATE_COUNT * 1.05), failedAsserts);
        if (stats.getCollectionUpdateCount() < (COLLECTION_UPDATE_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value COLLECTION_UPDATE_COUNT (ref=" + COLLECTION_UPDATE_COUNT + ") to "
                    + stats.getCollectionUpdateCount());

        // Entities statistics
        preAssertTrue("ENTITY_FETCH_COUNT increased more than 5% (ref=" + ENTITY_FETCH_COUNT + "): " + stats.getEntityFetchCount(),
                stats.getEntityFetchCount() < (ENTITY_FETCH_COUNT * 1.05), failedAsserts);
        if (stats.getEntityFetchCount() < (ENTITY_FETCH_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value ENTITY_FETCH_COUNT (ref=" + ENTITY_FETCH_COUNT + ") to " + stats.getEntityFetchCount());
        preAssertTrue("ENTITY_LOAD_COUNT increased more than 5% (ref=" + ENTITY_LOAD_COUNT + "): " + stats.getEntityLoadCount(),
                stats.getEntityLoadCount() <= (ENTITY_LOAD_COUNT * 1.05), failedAsserts);
        if (stats.getEntityLoadCount() < (ENTITY_LOAD_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value ENTITY_LOAD_COUNT (ref=" + ENTITY_LOAD_COUNT + ") to " + stats.getEntityLoadCount());
        preAssertTrue("ENTITY_INSERT_COUNT increased more than 5% (ref=" + ENTITY_INSERT_COUNT + "): " + stats.getEntityInsertCount(),
                stats.getEntityInsertCount() <= (ENTITY_INSERT_COUNT * 1.05), failedAsserts);
        if (stats.getEntityInsertCount() < (ENTITY_INSERT_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value ENTITY_INSERT_COUNT (ref=" + ENTITY_INSERT_COUNT + ") to " + stats.getEntityInsertCount());


        // Collections statistics
        preAssertTrue("COLLECTION_FETCH_COUNT increased more than 5% (ref=" + COLLECTION_FETCH_COUNT + "): " + stats.getCollectionFetchCount(),
                stats.getCollectionFetchCount() <= (COLLECTION_FETCH_COUNT * 1.05), failedAsserts);
        if (stats.getCollectionFetchCount() < (COLLECTION_FETCH_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value COLLECTION_FETCH_COUNT (ref=" + COLLECTION_FETCH_COUNT + ") to "
                    + stats.getCollectionFetchCount());
        preAssertTrue("COLLECTION_LOAD_COUNT increased more than 5% (ref=" + COLLECTION_LOAD_COUNT + "): " + stats.getCollectionLoadCount(),
                stats.getCollectionLoadCount() <= (COLLECTION_LOAD_COUNT * 1.05), failedAsserts);
        if (stats.getCollectionLoadCount() < (COLLECTION_LOAD_COUNT * 0.95))
            logger.warn("/!\\ You should update reference value COLLECTION_LOAD_COUNT (ref=" + COLLECTION_LOAD_COUNT + ") to " + stats.getCollectionLoadCount());
        preAssertTrue("COLLECTION_RECREATE_COUNT increased more than 5% (ref=" + COLLECTION_RECREATE_COUNT + "): " + stats.getCollectionRecreateCount()
               , stats.getCollectionRecreateCount() <= (COLLECTION_RECREATE_COUNT * 1.05), failedAsserts);

        if(assertDuration) {
        	// Time of the slowest query executed.
        	preAssertTrue("Time of the slowest query executed increased more than 50% (ref=" + QUERY_MAX_TIME_MS + "): " + stats.getQueryExecutionMaxTime()
        			, stats.getQueryExecutionMaxTime() <= (QUERY_MAX_TIME_MS * 1.50), failedAsserts);
        	if (stats.getQueryExecutionMaxTime() < (QUERY_MAX_TIME_MS * 0.50))
        		logger.warn("/!\\ You should update reference value QUERY_MAX_TIME_MS (ref=" + QUERY_MAX_TIME_MS + ") to " + stats.getQueryExecutionMaxTime());

        	// Check test duration
        	preAssertTrue("Total duration of the test increased more than 5% (ref=" + DURATION + "): " + duration, duration < (DURATION * 1.05), failedAsserts);
        	if (duration <= (DURATION * 0.85))
        		logger.warn("/!\\ You should update reference value DURATION (ref=" + DURATION + ") to " + duration);
        }
        
        StringBuffer formattedFailedAsserts = new StringBuffer();
        for (AssertionError failedAssert : failedAsserts) {
            formattedFailedAsserts.append(failedAssert.getMessage());
            formattedFailedAsserts.append("\n");
        }
        String advice = "Analyse the code with your favorite profiler, then see where performances decrease and optimize the code. If you consider this new value as 'normal' then set a new reference value.";

        assertTrue(failedAsserts.size() + " Hibernate stats violations: \n" + formattedFailedAsserts.toString() + advice , failedAsserts.isEmpty());
    }
    
    private static void preAssertEquals(String msg, long expected, long actual, List<AssertionError> failedAsserts) {
        try {
            assertEquals(msg, expected, actual);
        } catch (AssertionError e) {
            failedAsserts.add(e);
        }
    }

    private static void preAssertTrue(String msg, boolean actual, List<AssertionError> failedAsserts) {
        try {
            assertTrue(msg, actual);
        } catch (AssertionError e) {
            failedAsserts.add(e);
        }
    }
}
