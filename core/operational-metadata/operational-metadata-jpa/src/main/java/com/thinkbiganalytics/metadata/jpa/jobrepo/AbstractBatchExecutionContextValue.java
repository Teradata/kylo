package com.thinkbiganalytics.metadata.jpa.jobrepo;

/*-
 * #%L
 * thinkbig-operational-metadata-jpa
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


import com.thinkbiganalytics.metadata.api.jobrepo.ExecutionConstants;
import com.thinkbiganalytics.metadata.jpa.support.NormalizeAndCleanString;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

/**
 * Base class allowing for execution context values
 * Currently only the  {@code stringVal} is populated
 */
@MappedSuperclass
public abstract class AbstractBatchExecutionContextValue {


    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_CD", length = 10, nullable = false)
    private ExecutionConstants.ParamType typeCode = ExecutionConstants.ParamType.STRING;


    @Type(type = "com.thinkbiganalytics.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
    @Column(name = "STRING_VAL")
    private String stringVal;

    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    @Column(name = "DATE_VAL")
    private DateTime dateVal;
    @Column(name = "LONG_VAL")
    private Long longVal;
    @Column(name = "DOUBLE_VAL")
    private Double doubleVal;

    public ExecutionConstants.ParamType getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(ExecutionConstants.ParamType typeCode) {
        this.typeCode = typeCode;
    }

    public String getStringVal() {
        return stringVal;
    }

    public void setStringVal(String stringVal) {
        this.stringVal = NormalizeAndCleanString.normalizeAndClean(stringVal);
        //this.stringVal = stringVal;
    }

    public DateTime getDateVal() {
        return dateVal;
    }

    public void setDateVal(DateTime dateVal) {
        this.dateVal = dateVal;
    }

    public Long getLongVal() {
        return longVal;
    }

    public void setLongVal(Long longVal) {
        this.longVal = longVal;
    }

    public Double getDoubleVal() {
        return doubleVal;
    }

    public void setDoubleVal(Double doubleVal) {
        this.doubleVal = doubleVal;
    }
}
