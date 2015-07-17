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

import org.hibernate.stat.Statistics;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

/**
 * Verifies the formatting and assertions done by HibernateStatsHelper
 */
@RunWith(MockitoJUnitRunner.class)
public class HibernateStatsHelperTest {

    @Mock
    private Statistics stats;
    private Map<HibernateStatsReferenceType,Long> refs;

    private Map<HibernateStatsReferenceType,Long> actuals;

    @Before
    public void setUp() {
        // Init some stats
        // Set reference values
        refs = new HashMap<HibernateStatsReferenceType, Long>();
        refs.put(HibernateStatsReferenceType.DURATION, Long.valueOf(3000));
        refs.put(HibernateStatsReferenceType.QUERY_COUNT, Long.valueOf(4));
        refs.put(HibernateStatsReferenceType.QUERY_MAX_TIME_MS, Long.valueOf(250));

        refs.put(HibernateStatsReferenceType.ENTITY_FETCH_COUNT, Long.valueOf(6));
        refs.put(HibernateStatsReferenceType.ENTITY_LOAD_COUNT, Long.valueOf(73));
        refs.put(HibernateStatsReferenceType.ENTITY_INSERT_COUNT, Long.valueOf(0));
        refs.put(HibernateStatsReferenceType.ENTITY_DELETE_COUNT, Long.valueOf(0));
        refs.put(HibernateStatsReferenceType.ENTITY_UPDATE_COUNT, Long.valueOf(0));

        refs.put(HibernateStatsReferenceType.COLLECTION_FETCH_COUNT, Long.valueOf(19));
        refs.put(HibernateStatsReferenceType.COLLECTION_LOAD_COUNT, Long.valueOf(20));
        refs.put(HibernateStatsReferenceType.COLLECTION_RECREATE_COUNT, Long.valueOf(0));
        refs.put(HibernateStatsReferenceType.COLLECTION_REMOVE_COUNT, Long.valueOf(0));
        refs.put(HibernateStatsReferenceType.COLLECTION_UPDATE_COUNT, Long.valueOf(0));

        //By default actuals match expected refs
        actuals = new HashMap<HibernateStatsReferenceType, Long>(refs);

        setUpMock();
    }

    private void setUpMock() {

        when(stats.getQueryExecutionCount()).thenReturn(getActual(HibernateStatsReferenceType.QUERY_COUNT));
        when(stats.getEntityInsertCount()).thenReturn(getActual(HibernateStatsReferenceType.ENTITY_INSERT_COUNT));
        when(stats.getEntityDeleteCount()).thenReturn(getActual(HibernateStatsReferenceType.ENTITY_DELETE_COUNT));
        when(stats.getEntityUpdateCount()).thenReturn(getActual(HibernateStatsReferenceType.ENTITY_UPDATE_COUNT));
        when(stats.getCollectionRecreateCount()).thenReturn(getActual(HibernateStatsReferenceType.COLLECTION_RECREATE_COUNT));
        when(stats.getCollectionRemoveCount()).thenReturn(getActual(HibernateStatsReferenceType.COLLECTION_REMOVE_COUNT));
        when(stats.getCollectionUpdateCount()).thenReturn(getActual(HibernateStatsReferenceType.COLLECTION_UPDATE_COUNT));
        when(stats.getEntityFetchCount()).thenReturn(getActual(HibernateStatsReferenceType.ENTITY_FETCH_COUNT));
        when(stats.getEntityLoadCount()).thenReturn(getActual(HibernateStatsReferenceType.ENTITY_LOAD_COUNT));
        when(stats.getEntityInsertCount()).thenReturn(getActual(HibernateStatsReferenceType.ENTITY_INSERT_COUNT));
        when(stats.getCollectionFetchCount()).thenReturn(getActual(HibernateStatsReferenceType.COLLECTION_FETCH_COUNT));
        when(stats.getCollectionLoadCount()).thenReturn(getActual(HibernateStatsReferenceType.COLLECTION_LOAD_COUNT));
        when(stats.getQueryExecutionMaxTime()).thenReturn(getActual(HibernateStatsReferenceType.QUERY_MAX_TIME_MS));
    }

    private Long getActual(HibernateStatsReferenceType metric) {
        return actuals.get(metric);
    }
    private Long getRef(HibernateStatsReferenceType metric) {
        return refs.get(metric);
    }

    @Test
    public void testPassingCheckStats() {
        try {
            HibernateStatsHelper.checkStats(refs, getRef(HibernateStatsReferenceType.DURATION), stats);
        } catch (Throwable e) {
            fail("unexpected exception:" + e);
        }
    }
    
    @Test
    public void testPassingCheckStatsIgnoringDuration() {
        refs.put(HibernateStatsReferenceType.QUERY_MAX_TIME_MS, Long.valueOf(0));
        try {
            HibernateStatsHelper.checkStatsIgnoringDuration(refs, stats);
        } catch (Throwable e) {
            fail("unexpected exception:" + e);
        }
    }

    @Test
    public void testSingleFailureCheckStats() throws MalformedURLException {
        HibernateStatsReferenceType metric = HibernateStatsReferenceType.COLLECTION_LOAD_COUNT;
        increaseActual(metric);
        setUpMock();
        assertExpectedFailure(metric);
    }

    /**
     * Mostly useful to check output formatting manually
     * @throws MalformedURLException
     */
    @Test
    public void testMultipleFailureCheckStats() throws MalformedURLException {
        increaseActual(HibernateStatsReferenceType.COLLECTION_LOAD_COUNT);
        increaseActual(HibernateStatsReferenceType.ENTITY_INSERT_COUNT);
        increaseActual(HibernateStatsReferenceType.COLLECTION_REMOVE_COUNT);
        setUpMock();
        assertExpectedFailure(HibernateStatsReferenceType.ENTITY_INSERT_COUNT);
    }


    private void assertExpectedFailure(HibernateStatsReferenceType metric) throws MalformedURLException {
        boolean failedAssertReported = false;
        try {
            HibernateStatsHelper.checkStats(refs, getRef(HibernateStatsReferenceType.DURATION), stats);
        } catch (AssertionError e) {
            failedAssertReported = true;
            e.printStackTrace();
        }
        assertTrue("expected failed metric:" + metric, failedAssertReported);
    }

    /**
     * Decrement expected reference by 20 %to force a test to fail with standard value
     */
    private void increaseActual(HibernateStatsReferenceType metric) {
        long original = actuals.get(metric);
        long expected = refs.get(metric);
        long modifiedToFail;
        if (expected >0) {
            modifiedToFail = (long) (original * 1.20f);
        } else {
            modifiedToFail = 10;
        }
        actuals.put(metric, modifiedToFail);
    }
}
