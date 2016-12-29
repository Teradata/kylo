package com.thinkbiganalytics.nifi.provenance.jms;

/**
 * Created by sr186054 on 12/22/16.
 */
public interface JmsSendListener<T extends Object> {

    public String getDestination();

    public void successfulJmsMessage(String destination, T payload);

    public void errorJmsMessage(String destination, T payload, String message);
}
