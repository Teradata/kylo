package com.thinkbiganalytics.jobrepo.nifi.provenance;

/**
 * Created by sr186054 on 5/11/16.
 */
public interface ProvenanceEventJobExecutionStartupListener {

    void onEventsInitialized();

    void onStartupConnectionError();


}
