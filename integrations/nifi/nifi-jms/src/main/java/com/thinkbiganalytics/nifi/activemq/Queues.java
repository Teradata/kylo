package com.thinkbiganalytics.nifi.activemq;

/**
 * Created by Jeremy Merrifield on 8/1/16.
 */
public interface Queues {
    public static final String FEED_MANAGER_QUEUE = "thinkbig.feed-manager";

    public static final String PROVENANCE_EVENT_STATS_QUEUE = "thinkbig.provenance-event-stats";
    public static final String PROVENANCE_EVENT_QUEUE = "thinkbig.provenance-event";
}
