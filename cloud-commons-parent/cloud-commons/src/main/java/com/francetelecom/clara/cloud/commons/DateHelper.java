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

import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DateHelper
 *
 * Sample usage :
 *  - hack Environment creation date (set into the constructor)
 *    cf. EnvironmentDaoJpaImplTest::should_purge_environments
 */
public class DateHelper {

    private static Date nowDate = null;


    /**
     * get now with e delta of nbDay
     * @param nbDay delta day number (could be negative)
     * @return altered date
     */
    public static Date getDateDeltaDay(int nbDay) {
        Date now = getNow();
        Date deltaDate = DateUtils.addDays(now, nbDay);
        return deltaDate;
    }

    /**
     * get now with e delta of nbSec
     * @param nbSec delta day number (could be negative)
     * @return altered date
     */
    public static Date getDateDeltaSec(int nbSec) {
        Date now = getNow();
        Date deltaDate = DateUtils.addSeconds(now, nbSec);
        return deltaDate;
    }

    /**
     * fix getNow() result for all next call until resetNow() is called
     * @param newNowDate date to return
     */
    public static void setNowDate(Date newNowDate) {
        nowDate = newNowDate;
    }

    /**
     * reset getNow() hack : getNow following call will return the current date
     */
    public static void resetNow() {
        nowDate = null;
    }

    /**
     * get now
     * @return the current date or setNowDate argument
     */
    public static Date getNow() {
        if (nowDate != null) {
            return nowDate;
        }
        return new Date();
    }

    public static String getDateLogFormat(Date myDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ");
        return dateFormat.format(myDate);
    }
}
