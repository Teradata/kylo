/**
 *
 */
package com.thinkbiganalytics.metadata.rest.model.op;

/*-
 * #%L
 * thinkbig-metadata-rest-model
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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.joda.ser.DateTimeSerializer;

import org.joda.time.DateTime;

import java.io.Serializable;

/**
 *
 */
@SuppressWarnings("serial")
@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME, include = As.PROPERTY)
@JsonSubTypes({
                  @JsonSubTypes.Type(value = HiveTablePartitions.class),
                  @JsonSubTypes.Type(value = FileList.class),
              }
)
public class ChangeSet implements Serializable {

    @JsonSerialize(using = DateTimeSerializer.class)
    private DateTime intrinsicTime;

    private String intrinsicPeriod;
    private double incompletenessFactor;


    public DateTime getIntrinsicTime() {
        return intrinsicTime;
    }

    public void setIntrinsicTime(DateTime intrinsicTime) {
        this.intrinsicTime = intrinsicTime;
    }

    public String getIntrinsicPeriod() {
        return intrinsicPeriod;
    }

    public void setIntrinsicPeriod(String intrinsicPeriod) {
        this.intrinsicPeriod = intrinsicPeriod;
    }

    public double getIncompletenessFactor() {
        return incompletenessFactor;
    }

    public void setIncompletenessFactor(double incompletenessFactor) {
        this.incompletenessFactor = incompletenessFactor;
    }

}
