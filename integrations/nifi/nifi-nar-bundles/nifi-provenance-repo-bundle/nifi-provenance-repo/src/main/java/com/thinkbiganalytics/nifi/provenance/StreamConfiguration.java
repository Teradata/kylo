package com.thinkbiganalytics.nifi.provenance;

/**
 * Created by sr186054 on 8/13/16.
 */
public class StreamConfiguration {

    public StreamConfiguration() {

    }

    public StreamConfiguration(long processDelay, Long maxTimeBetweenEventsMillis, Integer numberOfEventsToConsiderAStream) {
        this.processDelay = processDelay;
        this.maxTimeBetweenEventsMillis = maxTimeBetweenEventsMillis;
        this.numberOfEventsToConsiderAStream = numberOfEventsToConsiderAStream;
    }

    /**
     * Max time to wait before processing
     */
    private long processDelay = 3000; //default to 3 sec wait before processing

    /**
     * Max time between events for a given Feed Processor considered a Batch Anything under this time will be considered a Stream
     */
    private Long maxTimeBetweenEventsMillis = 2000L;

    /**
     * Number of events needed to be in queue/processing to be considered a stream that fall within the maxTimeBetweenEventsMillis for processing on a given Processor for a specific Feed
     */
    private Integer numberOfEventsToConsiderAStream = 3;


    public Long getMaxTimeBetweenEventsMillis() {
        return maxTimeBetweenEventsMillis;
    }

    public Integer getNumberOfEventsToConsiderAStream() {
        return numberOfEventsToConsiderAStream;
    }

    public long getProcessDelay() {
        return processDelay;
    }

    public static class Builder {

        private long processDelay = 3000; //default to 3 sec wait before processing

        private Long maxTimeBetweenEventsMillis = 2000L;

        private Integer numberOfEventsToConsiderAStream = 3;

        public Builder processDelay(long processDelay) {
            this.processDelay = processDelay;
            return this;
        }

        public Builder maxTimeBetweenEvents(Long maxTimeBetweenEventsMillis) {
            this.maxTimeBetweenEventsMillis = maxTimeBetweenEventsMillis;
            return this;
        }

        public Builder numberOfEventsForStream(int events) {
            this.numberOfEventsToConsiderAStream = events;
            return this;
        }

        public StreamConfiguration build() {
            return new StreamConfiguration(processDelay, maxTimeBetweenEventsMillis, numberOfEventsToConsiderAStream);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StreamConfiguration{");
        sb.append("processDelay=").append(processDelay);
        sb.append(", maxTimeBetweenEventsMillis=").append(maxTimeBetweenEventsMillis);
        sb.append(", numberOfEventsToConsiderAStream=").append(numberOfEventsToConsiderAStream);
        sb.append('}');
        return sb.toString();
    }
}
