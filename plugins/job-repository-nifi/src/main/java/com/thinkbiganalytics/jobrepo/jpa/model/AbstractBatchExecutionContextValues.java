package com.thinkbiganalytics.jobrepo.jpa.model;

import com.thinkbiganalytics.jobrepo.jpa.ExecutionConstants;

import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

/**
 * Created by sr186054 on 9/1/16.
 */

@MappedSuperclass
public abstract class AbstractBatchExecutionContextValues {


    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE_CD", length = 10, nullable = false)
    private ExecutionConstants.ParamType typeCode = ExecutionConstants.ParamType.STRING;


    @Type(type = "com.thinkbiganalytics.jobrepo.jpa.TruncateStringUserType", parameters = {@Parameter(name = "length", value = "250")})
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
        this.stringVal = stringVal;
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
