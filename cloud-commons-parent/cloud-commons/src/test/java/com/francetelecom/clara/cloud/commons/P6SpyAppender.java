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

import com.p6spy.engine.logging.appender.P6Logger;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class to programmatically intercept JDBC calls, record some usefull stats, to make some assertions
 * on them during unit tests (e.g. number of joins or sql queries exec duration)
 *
 * To use it, reference it in the spy.properties file:
 *  <pre>
       appender=com.francetelecom.clara.cloud.commons.P6SpyAppender
 *  </pre>
 * then invoke {@link #reset()} and {@link #checkStats(boolean)}
 */
public class P6SpyAppender implements P6Logger {

	protected final static Logger LOG = LoggerFactory.getLogger(P6SpyAppender.class.getName());


    /**
     * Minimum JOIN count to log the queries. This correspond to the orange-fr best pratices to not go
     * over the specified join count
     */
    int thresholdJoin = 4;

    /**
     * Minimum duration time in ms to log the slowest queries
     */
    int thresholdDuration = 250;


    //
    // API for unit tests
    //

     /**
     * Used by Unit tests to asserts on collected JDBC stats:
     * - number of joins should be lower than 61 (to support running on mysql engine which has this built-in limitation)
     * - eacj JDBC query should be faster than 10s
     * @param enableAssert set to true to actually fail Junit asserts when
     */
    public synchronized void checkStats(boolean enableAssert) {
        if (getSlowestCount() > 0) {
            LOG.debug("Top slowest queries:");
            for (JDBCEntry entry : slowestQueries) {
                LOG.debug("  " + entry.getElapsed() + "ms ("+entry.getJoinCount()+" join): " + entry.getSql());
                if (enableAssert) {
                    Assert.assertTrue("Too slow query: " + entry.getElapsed() + "ms. Consider this as a bug and correct it ASAP! SQL=" + entry.getSql(), entry.getElapsed() <= (1000 * 10));
                }
            }
            if (getSlowestCount() > slowestQueries.size()) {
                LOG.debug((getSlowestCount() - slowestQueries.size()) + " more slow (>= "+getThresholdDuration()+"ms) queries...");
            }
        }
        if (getMaxJoinCount() > 0) {
            LOG.debug("Top JOIN queries:");
            for (JDBCEntry entry : maxJoinQueries) {
                LOG.debug("  " + entry.getElapsed() + "ms ("+entry.getJoinCount()+" join): " + entry.getSql());
                if (enableAssert) {
                    Assert.assertTrue("Too many JOIN: " + entry.getJoinCount() + ". MySQL do not support over 61 JOIN. SQL=" + entry.getSql(), entry.getJoinCount() <= 61);
                }
            }
            if (getMaxJoinCount() > maxJoinQueries.size()) {
                LOG.debug((getMaxJoinCount() - maxJoinQueries.size()) + " more JOIN (>= "+getThresholdJoin()+" join) queries...");
            }
        }
    }

    /**
     * Clears the previously collected stats.
     */
    public synchronized void reset() {
        slowestCount = 0;
        slowestQueries.clear();
        maxJoinCount = 0;
        maxJoinQueries.clear();
    }





    //
    // Internals
    //

    private static P6SpyAppender currentInstance = null;

    protected String lastEntry;


    /**
     * Utility class used to extract number of Joins in a JDBC statement, and duration ("elapsed")
     */
	public static class JDBCEntry {
		int connectionId;
		String now;
		long elapsed;
		String category;
		String prepared;
		String sql;
		int joinCount;

		public int getJoinCount() {
			return joinCount;
		}

		public JDBCEntry(int connectionId, String now, long elapsed,
				String category, String prepared, String sql) {
			super();
			this.connectionId = connectionId;
			this.now = now;
			this.elapsed = elapsed;
			this.category = category;
			this.prepared = prepared;
			this.sql = sql;
			int index = -1;
			this.joinCount = 0;
			do {
				index = sql.toLowerCase().indexOf(" join ", index + 1);
				if (index >= 0) {
					this.joinCount++;
				}
			} while (index >= 0);
		}

		public int getConnectionId() {
			return connectionId;
		}

		public String getNow() {
			return now;
		}

		public long getElapsed() {
			return elapsed;
		}

		public String getCategory() {
			return category;
		}

		public String getPrepared() {
			return prepared;
		}

		public String getSql() {
			return sql;
		}

		@Override
		public String toString() {
			return now + "|" + elapsed + "|"
					+ (connectionId == -1 ? "" : String.valueOf(connectionId))
					+ "|" + category + "|" + prepared + "|" + sql;
		}
	}

    /**
     * Used to sort the JDBCEntry by nb of joins , to collect the queries with highest nb of joins
     */
    public static final class JoinComparator implements Comparator<JDBCEntry> {
		@Override
		public int compare(JDBCEntry o1, JDBCEntry o2) {
			int cmp = o2.getJoinCount() - o1.getJoinCount();
			if (cmp == 0) {
				cmp = (int) (o2.getElapsed() - o1.getElapsed());
			}
			return cmp;
		}
	}

    /**
     * Used to sort the JDBCEntry by execution duration ("elapsed"), to collect the N slowest queries
     */
	public static final class DurationComparator implements Comparator<JDBCEntry> {
		@Override
		public int compare(JDBCEntry o1, JDBCEntry o2) {
			return (int) (o2.getElapsed() - o1.getElapsed());
		}
	}
	

	List<JDBCEntry> slowestQueries;

	int slowestCount = 0;
	
	Comparator<JDBCEntry> durationComparator = new DurationComparator();


	List<JDBCEntry> maxJoinQueries;

	int maxJoinCount = 0;
	
	Comparator<JDBCEntry> joinComparator = new JoinComparator();

	public P6SpyAppender() {
		super();
		currentInstance = this;
		slowestQueries = new ArrayList<JDBCEntry>();
		maxJoinQueries = new ArrayList<JDBCEntry>();
	}

	public static P6SpyAppender getCurrentInstance() {
		return currentInstance;
	}

	public int getMaxJoinCount() {
		return maxJoinCount;
	}

	public int getThresholdDuration() {
		return thresholdDuration;
	}

	public int getThresholdJoin() {
		return thresholdJoin;
	}

	public int getSlowestCount() {
		return slowestCount;
	}

    //
    // P6Logger Impl
    //

    @Override
    public String getLastEntry() {
        return lastEntry;
    }

    @Override
	public void logException(Exception e) {
		LOG.debug(e.getMessage());
	}

	@Override
	public void logText(String formattedLine) {
		LOG.debug(formattedLine);
	}

	@Override
	public synchronized void logSQL(int connectionId, String now, long elapsed, String category, String prepared, String sql) {
		JDBCEntry jdbcEntry = new JDBCEntry(connectionId, now, elapsed, category, prepared, sql);
		//LOG.debug(jdbcEntry.toString());
		if (thresholdDuration <= elapsed) {
			slowestQueries.add(jdbcEntry);
			slowestCount++;
			Collections.sort(maxJoinQueries, durationComparator);
			if (slowestQueries.size() > 10) {
				slowestQueries.remove(slowestQueries.size() - 1);
			}
		}
		if (thresholdJoin <= jdbcEntry.getJoinCount()) {
			maxJoinQueries.add(jdbcEntry);
			maxJoinCount++;
			Collections.sort(maxJoinQueries, joinComparator);
			if (maxJoinQueries.size() > 10) {
				maxJoinQueries.remove(maxJoinQueries.size() - 1);
			}
		}
	}


}
