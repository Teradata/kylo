package com.thinkbiganalytics.nifi.provenance;

/**
 * Created by sr186054 on 8/13/16.
 */
public class StreamConfiguration {

    /**
     * Max time to wait before processing
     */
    private long processDelay = 3000; //default to 3 sec wait before processing


    /**
     * Max time between events between considered a Batch Anything under this time will be considered a Stream
     */
    private Long maxTimeBetweenEventsMillis = 1000L;

    private Integer numberOfEventsToConsiderAStream = 3;


    public Long getMaxTimeBetweenEventsMillis() {
        return maxTimeBetweenEventsMillis;
    }

    public void setMaxTimeBetweenEventsMillis(Long maxTimeBetweenEventsMillis) {
        this.maxTimeBetweenEventsMillis = maxTimeBetweenEventsMillis;
    }

    public Integer getNumberOfEventsToConsiderAStream() {
        return numberOfEventsToConsiderAStream;
    }

    public void setNumberOfEventsToConsiderAStream(Integer numberOfEventsToConsiderAStream) {
        this.numberOfEventsToConsiderAStream = numberOfEventsToConsiderAStream;
    }

    public long getProcessDelay() {
        return processDelay;
    }

    public void setProcessDelay(long processDelay) {
        this.processDelay = processDelay;
    }
}
