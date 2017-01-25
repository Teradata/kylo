package com.thinkbiganalytics.nifi.provenance.jms;

/**
 * Listener that is called when the {@link ProvenanceEventActiveMqWriter} sends a JMS message
 */
public interface JmsSendListener<T extends Object> {

    public String getDestination();

    public void successfulJmsMessage(String destination, T payload);

    public void errorJmsMessage(String destination, T payload, String message);
}
