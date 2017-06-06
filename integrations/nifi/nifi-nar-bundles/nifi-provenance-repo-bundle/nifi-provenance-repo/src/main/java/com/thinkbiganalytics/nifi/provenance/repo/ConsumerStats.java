package com.thinkbiganalytics.nifi.provenance.repo;

/*-
 * #%L
 * thinkbig-nifi-provenance-repo
 * %%
 * Copyright (C) 2017 ThinkBig Analytics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class ConsumerStats {

    private static final Logger log = LoggerFactory.getLogger(ConsumerStats.class);

    private List<Long> processTimes = new ArrayList<>();

    private List<Long> conversionTimes = new ArrayList<>();

    private List<Long> eventTimes = new ArrayList<>();

    private Long eventCount = 0L;

    private List<Long> sortTimes = new ArrayList<>();

    private List<Long> totalTimes = new ArrayList<>();


    public void addSortTime(Long time){
        sortTimes.add(time);
    }

    public void addConversionTime(Long time){
        conversionTimes.add(time);
    }

    public void addProcessTime(Long time){
        processTimes.add(time);
    }

    public void addEventTime(Long time){
        eventTimes.add(time);
    }

    public void addTotalTime(Long time){
        totalTimes.add(time);
    }

    public void incrementEventCount(){
        eventCount++;
    }

    private void resetTimes(){
        processTimes.clear();
        conversionTimes.clear();
        eventCount = 0L;
        sortTimes.clear();
        totalTimes.clear();
    }

    public void log(){
        log.info("Averages for {} events.  Sort: {}, conversion: {}, process: {}, event: {}, totalTime: {}, ",eventCount, (sortTimes.stream().mapToDouble(Long::doubleValue).sum()/eventCount),
                 conversionTimes.stream().mapToDouble(Long::doubleValue).average(),
                 processTimes.stream().mapToDouble(Long::doubleValue).average(),
                 eventTimes.stream().mapToDouble(Long::doubleValue).average(),
                 (totalTimes.stream().mapToDouble(Long::doubleValue).sum()/eventCount));
    }


}
