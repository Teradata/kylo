package com.thinkbiganalytics.nifi.provenance.model.stats;

import org.joda.time.DateTime;

/**
 * Created by sr186054 on 8/16/16.
 */
public class BaseStatistics {

    protected DateTime time;
    protected Long bytesIn = 0L;
    protected Long bytesOut = 0L;
    protected Long duration = 0L;
    protected Long totalCount = 1L;

    public DateTime getTime() {
        return time;
    }

    public void setTime(DateTime time) {
        this.time = time;
    }

    public Long getBytesIn() {
        return bytesIn;
    }

    public void setBytesIn(Long bytesIn) {
        this.bytesIn = bytesIn;
    }

    public Long getBytesOut() {
        return bytesOut;
    }

    public void setBytesOut(Long bytesOut) {
        this.bytesOut = bytesOut;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Long totalCount) {
        this.totalCount = totalCount;
    }

    public Double getAvgDuration() {
        return duration.doubleValue() / totalCount;
    }


}
