package com.thinkbiganalytics.nifi.rest.support;

/**
 * Created by sr186054 on 8/18/16.
 */
public class NifiFeedConstants {

    public static String TRIGGER_FEED_PROCESSOR_CLASS = "com.thinkbiganalytics.nifi.v2.metadata.TriggerFeed";

    public static String DEFAULT_TIGGER_FEED_PROCESSOR_SCHEDULE = "5 sec";

    public enum SCHEDULE_STRATEGIES {
        CRON_DRIVEN("Cron Expression"), TIMER_DRIVEN("Timer"), TRIGGER_DRIVEN("Trigger Event");
        private String displayName;

        SCHEDULE_STRATEGIES(String displayName) {
            this.displayName = displayName;
        }
    }

}
