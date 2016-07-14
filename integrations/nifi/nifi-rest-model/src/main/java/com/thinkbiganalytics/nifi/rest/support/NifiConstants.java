package com.thinkbiganalytics.nifi.rest.support;

/**
 * Created by sr186054 on 4/28/16.
 */
public class NifiConstants {

  public enum NIFI_PORT_TYPE {
  OUTPUT_PORT,INPUT_PORT
  }
  public enum NIFI_PROCESSOR_TYPE {
    PROCESSOR
  }

  public enum NIFI_COMPONENT_TYPE {
    OUTPUT_PORT, INPUT_PORT, PROCESSOR, PROCESS_GROUP, CONNECTION, TEMPLATE, CONTROLLER_SERVICE
  }

  public static String TRIGGER_FEED_PROCESSOR_CLASS = "com.thinkbiganalytics.nifi.v2.metadata.TriggerFeed";

  public static String DEFAULT_TIGGER_FEED_PROCESSOR_SCHEDULE  = "5 sec";

  public  enum SCHEDULE_STRATEGIES {
    CRON_DRIVEN("Cron Expression"), TIMER_DRIVEN("Timer"), TRIGGER_DRIVEN("Trigger Event");
    private String displayName;

         SCHEDULE_STRATEGIES(String displayName){
        this.displayName = displayName;
    }
  }

}
