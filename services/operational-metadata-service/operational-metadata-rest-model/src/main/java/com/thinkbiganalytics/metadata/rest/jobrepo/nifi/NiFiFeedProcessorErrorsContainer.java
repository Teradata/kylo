package com.thinkbiganalytics.metadata.rest.jobrepo.nifi;

/*-
 * #%L
 * thinkbig-operational-metadata-rest-model
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

import com.thinkbiganalytics.metadata.api.jobrepo.nifi.NifiFeedProcessorStatisticsProvider;

import org.joda.time.DateTime;

import java.util.List;

/**
 * Created by sr186054 on 6/14/17.
 */
public class NiFiFeedProcessorErrorsContainer {

    DateTime startTime;

    DateTime endTime;

    List<NifiFeedProcessorStatsErrors> errors;

    public NiFiFeedProcessorErrorsContainer() {

    }

    public NiFiFeedProcessorErrorsContainer(DateTime startTime, DateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(DateTime startTime) {
        this.startTime = startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(DateTime endTime) {
        this.endTime = endTime;
    }

    public List<NifiFeedProcessorStatsErrors> getErrors() {
        return errors;
    }

    public void setErrors(List<NifiFeedProcessorStatsErrors> errors) {
        this.errors = errors;
    }
}
