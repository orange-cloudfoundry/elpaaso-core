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

import com.francetelecom.clara.cloud.commons.BusinessException;
import com.francetelecom.clara.cloud.commons.TechnicalException;
import org.slf4j.LoggerFactory;
import sun.rmi.runtime.Log;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * PaasStats
 * Class that handle an hibernate (paas oriented) statistics
 * Last update  : $LastChangedDate$
 * Last author  : $Author$
 * Sample usage : see ManageStatisticsImplTest
 * @version     : $Revision$
 */
public class PaasStats implements Serializable {
    /**
     * SerialUID
     */
    private static final long serialVersionUID = -722642664859386016L;
    /**
     * Logger
     */
    private static final transient org.slf4j.Logger logger
       = LoggerFactory.getLogger(PaasStats.class);

    enum SnapState {
        START,
        END
    }
    private long creationTime; // used as id
    private long deltaTime;
    private String name; // pageURI could be used for portal
    private SnapState state = SnapState.START;
    //~ snap stats : depend on PaasStats state :
    // - starting values when "START"
    // - delta values when "END"
    Map<String, Long> startValues;
    Map<String, Long> deltaValues;

    public PaasStats(String name, Map<String, Long> startStats) {
        this.creationTime = System.currentTimeMillis();
        this.name = name;
        this.startValues = startStats;
        this.state = SnapState.START;
    }

    public long getCreationTime() {
        return creationTime;
    }

    @SuppressWarnings({"UnusedDeclaration"}) // wicket reflexion StatsTablePanel
    public Date getCreationDate() {
        return new Date(creationTime);
    }
    @SuppressWarnings({"UnusedDeclaration"}) // wicket reflexion StatsTablePanel
    public String getCreationDateString() {
        SimpleDateFormat sdf = new SimpleDateFormat();
        sdf.applyPattern("MM/dd/yyyy HH:mm");
        return sdf.format(new Date(creationTime));
    }

    public String getName() {
        return name;
    }
    @SuppressWarnings({"UnusedDeclaration"}) // wicket reflexion StatsTablePanel
    public String getShortName() {
        int maxStrLength = 40;
        if (this.name != null && this.name.length() > maxStrLength) {
            return this.name.substring(0, maxStrLength-1);
        }
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Long> getDeltaValues() {
        return deltaValues;
    }

    public Map<String, Long> setEndStats(Map<String, Long> endStats) throws BusinessException {
        if (this.state != SnapState.START) {
           throw new BusinessException("unable to end stats which is not started");
        } else if (startValues == null) {
           throw new BusinessException("unable to end stats without starting values");
        }
        deltaValues = new HashMap<String, Long>();
        for(Map.Entry<String,Long> stat : endStats.entrySet()) {
            String curKey = stat.getKey();
            Long curVal = stat.getValue();
            Long startStatValue = startValues.get(curKey);
            if (startStatValue != null) {
                deltaValues.put(curKey, curVal - startStatValue);
            } else {
                logger.warn("unable to stat {} without starting value", curKey);
            }
        }
        this.deltaTime = (System.currentTimeMillis() - this.creationTime);
        this.state = SnapState.END;
        return deltaValues;
    }

    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        if (state.equals(SnapState.END)) {
              for(Map.Entry<String,Long> dVal : deltaValues.entrySet()) {
                  Long deltaValue = dVal.getValue();
                  if (deltaValue > 0) {
                    sb.append("\n\t").append(dVal.getKey()).append(" ").append(dVal.getValue());
                  }
              }
        }
        sb.append("\n\tServer time used : ").append(this.deltaTime).append("ms");
        return sb.toString();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PaasStat '").append(name); // .append("' [").append(state).append("] :");
        sb.append(getDetails());
        return sb.toString();
    }
}
