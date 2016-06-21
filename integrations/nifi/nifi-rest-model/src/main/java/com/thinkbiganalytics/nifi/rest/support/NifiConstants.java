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

}
