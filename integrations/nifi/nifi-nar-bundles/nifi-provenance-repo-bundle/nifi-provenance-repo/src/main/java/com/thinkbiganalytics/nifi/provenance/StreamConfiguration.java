package com.thinkbiganalytics.nifi.provenance;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * A stream event is evaluated as follows
 * Events are initially delayed by the #processDelay and grouped together for evaluation as Stream or Batch
 *  - A Stream = Any processor(partitioned by feed) that receives #numberOfEventsToConsiderAStream or more events where the time between each event < #maxTimeBetweenEventsMillis
 *  - If the above criteria fails it will be considered and processed as a Batch event
 *
 *  @see com.thinkbiganalytics.nifi.provenance.processor.AbstractProvenanceEventProcessor
 * Created by sr186054 on 8/13/16.
 */
@Component
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
    @Value("${thinkbig.provenance.streamconfig.processDelay}")
    private long processDelay = 3000; //default to 3 sec wait before assessing

    /**
     * Max time between events for a given Feed Processor considered a Batch Anything under this time will be considered a Stream
     */
    @Value("${thinkbig.provenance.streamconfig.maxTimeBetweenEventsMillis}")
    private Long maxTimeBetweenEventsMillis = 2000L;

    /**
     * Number of events needed to be in queue/processing to be considered a stream that fall within the maxTimeBetweenEventsMillis for processing on a given Processor for a specific Feed
     */
    @Value("${thinkbig.provenance.streamconfig.numberOfEventsToConsiderAStream}")
    private Integer numberOfEventsToConsiderAStream = 3;

    @Value("${thinkbig.provenance.disabled:false}")
    private boolean disabled = false;


    /**
     * Time to wait before processing via jms
     */
    @Value("${thinkbig.provenance.jmsBatchDelay}")
    private long jmsBatchDelay = 3000; //default to 3 sec wait before grouping and sending to JMS


    public Long getMaxTimeBetweenEventsMillis() {
        return maxTimeBetweenEventsMillis;
    }

    public Integer getNumberOfEventsToConsiderAStream() {
        return numberOfEventsToConsiderAStream;
    }

    public long getProcessDelay() {
        return processDelay;
    }

    public long getJmsBatchDelay() {
        return jmsBatchDelay;
    }

    public static class Builder {

        private long processDelay = 3000; //default to 3 sec wait before processing

        private Long maxTimeBetweenEventsMillis = 2000L;

        private Integer numberOfEventsToConsiderAStream = 3;

        private long jmsBatchDelay = 3000;

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

        public Builder jmsBatchDelay(long jmsBatchDelay) {
            this.jmsBatchDelay = jmsBatchDelay;
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
